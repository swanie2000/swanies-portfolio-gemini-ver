package com.swanie.portfolio.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.graphics.toColorInt
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.R
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.*
import com.swanie.portfolio.security.SecurityManager
import com.swanie.portfolio.widget.PortfolioWidget
import com.swanie.portfolio.widget.PortfolioWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themePreferences: ThemePreferences,
    private val database: AppDatabase,
    private val securityManager: SecurityManager
) : ViewModel() {

    private val userConfigDao = database.userConfigDao()
    private val assetDao = database.assetDao()
    private val vaultDao = database.vaultDao()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // 🎯 PRO PLACEHOLDER: State for subscription logic
    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    // 🎯 THE REGISTRY: Track the currently targeted vault for widget configuration
    private val _targetVaultId = MutableStateFlow<Int>(-1)
    val targetVaultId: StateFlow<Int> = _targetVaultId.asStateFlow()

    val isDarkMode: StateFlow<Boolean> = themePreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isCompactViewEnabled: StateFlow<Boolean> = themePreferences.isCompactViewEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val confirmDelete: StateFlow<Boolean> = themePreferences.confirmDelete
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isBiometricEnabled: StateFlow<Boolean> = themePreferences.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userConfig: StateFlow<UserConfigEntity?> = userConfigDao.getUserConfig()
        .onEach { config ->
            if (config == null) {
                viewModelScope.launch {
                    userConfigDao.insertConfig(UserConfigEntity(id = 1))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allVaults: StateFlow<List<VaultEntity>> = vaultDao.getAllVaultsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val targetVault: StateFlow<VaultEntity?> = combine(_targetVaultId, allVaults) { id, vaults ->
        vaults.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val targetVaultAssets: StateFlow<List<AssetEntity>> = _targetVaultId
        .flatMapLatest { id ->
            if (id == -1) flowOf(emptyList())
            else assetDao.getAssetsByVault(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTargetVaultId(id: Int) {
        _targetVaultId.value = id
    }

    /**
     * 🚀 THE SMART RESOLVER: Forces a UI reset and resolves appWidgetIds if needed.
     * If the appWidgetId is new/unbound, it defaults to Vault 1 to avoid the infinite spinner.
     */
    fun forceVaultSwitch(id: Int, isAppWidgetId: Boolean = false) {
        viewModelScope.launch {
            _targetVaultId.value = -1
            delay(10)
            if (isAppWidgetId) {
                val bound = vaultDao.getVaultByAppWidgetId(id)
                _targetVaultId.value = bound?.id ?: 1
            } else {
                _targetVaultId.value = id
            }
        }
    }

    fun clearTargetVault() {
        _targetVaultId.value = -1
    }

    fun saveIsDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            themePreferences.saveIsDarkMode(isDark)
        }
    }

    fun saveIsCompactViewEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            themePreferences.saveIsCompactViewEnabled(isEnabled)
        }
    }

    fun saveConfirmDelete(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.saveConfirmDelete(enabled)
        }
    }

    fun toggleBiometricLock(activity: FragmentActivity, enabled: Boolean, onError: (String) -> Unit) {
        if (enabled) {
            if (securityManager.canAuthenticate()) {
                securityManager.authenticate(
                    activity = activity,
                    onSuccess = {
                        viewModelScope.launch {
                            themePreferences.saveIsBiometricEnabled(true)
                        }
                    },
                    onError = onError
                )
            } else {
                onError("Biometric authentication not available on this device.")
            }
        } else {
            // If disabling, we still want a security check before turning it off
            if (isBiometricEnabled.value) {
                securityManager.authenticate(
                    activity = activity,
                    onSuccess = {
                        viewModelScope.launch {
                            themePreferences.saveIsBiometricEnabled(false)
                        }
                    },
                    onError = onError
                )
            } else {
                viewModelScope.launch {
                    themePreferences.saveIsBiometricEnabled(false)
                }
            }
        }
    }

    suspend fun updateShowWidgetTotal(vaultId: Int, show: Boolean) {
        vaultDao.updateShowWidgetTotal(vaultId, show)
        triggerWidgetUpdate(vaultId)
    }

    // 🌐 GLOBAL VISTA: Manual Save Action for Widget Configuration (Vault-Specific)
    // 🛡️ REGISTRATION LOCK: Links the hardware appWidgetId to the VaultEntity in Room.
    fun saveWidgetConfiguration(vaultId: Int, appWidgetId: Int?, selectedIds: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            
            // 🛡️ 1-TO-1 REGISTRATION: Clear this widget ID from any other vaults first
            if (appWidgetId != null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                vaultDao.clearAppWidgetId(appWidgetId)
                vaultDao.updateAppWidgetId(vaultId, appWidgetId)
            }

            val idsString = selectedIds.joinToString(",")
            vaultDao.updateSelectedWidgetAssets(vaultId, idsString)
            triggerWidgetUpdate(vaultId)
            _isSaving.value = false
            onComplete()
        }
    }

    // 🛡️ PER-VAULT APPEARANCE: Saves custom widget colors for a specific vault
    suspend fun saveWidgetAppearance(vaultId: Int, bg: String, bgText: String, card: String, cardText: String) {
        android.util.Log.d("DATABASE_SAVE", "Attempting to save colors for Vault ID: $vaultId with BG: $bg")
        vaultDao.updateWidgetColors(vaultId, bg, bgText, card, cardText)
        triggerWidgetUpdate(vaultId)
    }

    suspend fun getVaultByAppWidgetId(appWidgetId: Int): VaultEntity? {
        return vaultDao.getVaultByAppWidgetId(appWidgetId)
    }

    // 🛡️ NUCLEAR RESET: Deletes ALL assets, resets widget selections, and resets timestamp.
    fun clearAllAssets(vaultId: Int) {
        viewModelScope.launch {
            assetDao.deleteAll()
            vaultDao.updateSelectedWidgetAssets(vaultId, "")
            userConfigDao.updateLastSync(0L)
            triggerWidgetUpdate(vaultId)
        }
    }

    // 🛡️ SURGICAL RESET: ONLY clears widget selection string for specific vault.
    fun clearWidgetSelection(vaultId: Int) {
        viewModelScope.launch {
            vaultDao.updateSelectedWidgetAssets(vaultId, "")
            triggerWidgetUpdate(vaultId)
        }
    }

    fun updateSelectedWidgetAssets(vaultId: Int, assets: String) {
        viewModelScope.launch {
            vaultDao.updateSelectedWidgetAssets(vaultId, assets)
            triggerWidgetUpdate(vaultId)
        }
    }

    fun updateVaultColor(vaultId: Int, target: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            when (target) {
                0 -> vaultDao.updateWidgetColors(vaultId, color, v.widgetBgTextColor, v.widgetCardColor, v.widgetCardTextColor)
                1 -> vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, color, v.widgetCardColor, v.widgetCardTextColor)
                2 -> vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, v.widgetBgTextColor, color, v.widgetCardTextColor)
                3 -> vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, v.widgetBgTextColor, v.widgetCardColor, color)
            }
            triggerWidgetUpdate(vaultId)
        }
    }

    fun updateWidgetBgColor(vaultId: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            vaultDao.updateWidgetColors(vaultId, color, v.widgetBgTextColor, v.widgetCardColor, v.widgetCardTextColor)
            triggerWidgetUpdate(vaultId)
        }
    }

    fun updateWidgetBgTextColor(vaultId: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, color, v.widgetCardColor, v.widgetCardTextColor)
            triggerWidgetUpdate(vaultId)
        }
    }

    fun updateWidgetCardColor(vaultId: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, v.widgetBgTextColor, color, v.widgetCardTextColor)
            triggerWidgetUpdate(vaultId)
        }
    }

    fun updateWidgetCardTextColor(vaultId: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, v.widgetBgTextColor, v.widgetCardColor, color)
            triggerWidgetUpdate(vaultId)
        }
    }

    /**
     * 🚀 THE PIXEL ENGINE: Manually update the RemoteViews for instant "Direct Draw".
     * This bypasses the Glance read-queue and allows for an immediate color/text refresh.
     */
    suspend fun forceImmediateRemoteViewsUpdate(vaultId: Int, appWidgetId: Int) {
        Log.d("DIRECT_DRAW", "Pushing to Widget ID: $appWidgetId for Vault: $vaultId")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val vault = vaultDao.getVaultById(vaultId) ?: return

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout_fallback)
        
        // --- 🎨 DIRECT COLOR DRAW ---
        val bgColor = vault.widgetBgColor.toColorInt()
        val bgTextColor = vault.widgetBgTextColor.toColorInt()
        
        remoteViews.setInt(R.id.widget_root, "setBackgroundColor", bgColor)
        remoteViews.setTextColor(R.id.vault_name_text, bgTextColor)
        remoteViews.setTextColor(R.id.total_balance_text, bgTextColor)
        remoteViews.setTextColor(R.id.widget_loading_msg, bgTextColor)

        // --- 🏷️ DIRECT TEXT DRAW ---
        remoteViews.setTextViewText(R.id.vault_name_text, vault.name.uppercase())

        if (vault.showWidgetTotal) {
            val total = assetDao.getAssetsByVaultOnce(vaultId).sumOf { 
                (it.officialSpotPrice * it.weight * it.amountHeld) + it.premium 
            }
            remoteViews.setTextViewText(R.id.total_balance_text, NumberFormat.getCurrencyInstance(Locale.US).format(total))
        } else {
            remoteViews.setTextViewText(R.id.total_balance_text, "")
        }

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    /**
     * 🚀 THE SURGICAL TRIGGER: Forces an immediate refresh for a specific vault's widget
     * or all widgets if no vaultId is provided.
     */
    suspend fun triggerWidgetUpdate(vaultId: Int? = null) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, PortfolioWidgetReceiver::class.java)

        val idsToUpdate = if (vaultId != null) {
            val vault = vaultDao.getVaultById(vaultId)
            if (vault?.appWidgetId != null && vault.appWidgetId != -1) {
                intArrayOf(vault.appWidgetId!!)
            } else {
                appWidgetManager.getAppWidgetIds(componentName)
            }
        } else {
            appWidgetManager.getAppWidgetIds(componentName)
        }

        if (idsToUpdate.isEmpty()) return

        val vault = vaultId?.let { vaultDao.getVaultById(it) }

        // 1. Update Glance Internal State for specific IDs
        val glanceManager = GlanceAppWidgetManager(context)
        idsToUpdate.forEach { id ->
            val v = if (vaultId != null) vault else {
                vaultDao.getVaultByAppWidgetId(id)
            }

            v?.let { vSafe ->
                // 🚀 DATASTORE HANDSHAKE: Write directly to PreferencesGlanceStateDefinition
                val glanceId = glanceManager.getGlanceIdBy(id)
                androidx.glance.appwidget.state.updateAppWidgetState(context, androidx.glance.state.PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[PortfolioWidget.VAULT_ID_KEY] = vSafe.id
                        this[PortfolioWidget.STATIC_VAULT_NAME_KEY] = vSafe.name
                        this[PortfolioWidget.WIDGET_BG_COLOR_KEY] = vSafe.widgetBgColor
                        this[PortfolioWidget.WIDGET_BG_TEXT_COLOR_KEY] = vSafe.widgetBgTextColor
                        this[PortfolioWidget.WIDGET_CARD_COLOR_KEY] = vSafe.widgetCardColor
                        this[PortfolioWidget.WIDGET_CARD_TEXT_COLOR_KEY] = vSafe.widgetCardTextColor
                        this[PortfolioWidget.SHOW_TOTAL_KEY] = vSafe.showWidgetTotal
                        
                        // 🚀 THREADED PULSE: Serialize Top 10 Assets with Sparkline Generation on IO
                        val allAssetsForVault = assetDao.getAssetsByVaultOnce(vSafe.id)
                        val selectedIds = vSafe.selectedWidgetAssets.split(",").filter { it.isNotBlank() }
                        val filteredAssets = if (selectedIds.isEmpty()) {
                            allAssetsForVault.take(10)
                        } else {
                            allAssetsForVault.filter { it.coinId in selectedIds }
                        }

                        val serializedAssets = withContext(Dispatchers.IO) {
                            filteredAssets.map { asset ->
                                val iconSource = when {
                                    asset.category == AssetCategory.METAL || asset.isMetal -> {
                                        val metalName = asset.symbol.lowercase()
                                        "res:ic_$metalName"
                                    }
                                    asset.imageUrl.startsWith("file:") -> asset.imageUrl
                                    asset.localIconPath != null -> "file:${asset.localIconPath}"
                                    else -> asset.imageUrl // Fallback to URL or empty
                                }

                                val history = database.priceHistoryDao().getRecentHistory(asset.coinId)
                                val historyData = history.map { it.price }
                                var sparkPath = "none"
                                if (historyData.size >= 2) {
                                    val isPositive = historyData.last() >= historyData.first()
                                    val bitmap = generateSparklineBitmap(historyData, isPositive)
                                    val file = File(context.cacheDir, "spark_${asset.coinId}.png")
                                    try {
                                        FileOutputStream(file).use { out ->
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                        }
                                        sparkPath = file.absolutePath
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                val assetValue = (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
                                "${asset.coinId}|${asset.symbol}|${asset.displayName.ifBlank { asset.name }}|$iconSource|$assetValue|${asset.priceChange24h}|$sparkPath"
                            }.joinToString("||")
                        }
                        this[PortfolioWidget.ASSETS_DATA_KEY] = serializedAssets
                        
                        // Calculate total for static balance
                        val total = allAssetsForVault.sumOf {
                            (it.officialSpotPrice * it.weight * it.amountHeld) + it.premium 
                        }
                        this[PortfolioWidget.STATIC_TOTAL_BALANCE_KEY] = NumberFormat.getCurrencyInstance(Locale.US).format(total)
                    }.toPreferences()
                }

                // 🚀 CRITICAL: Update PortfolioWidget in the SAME scope to bypass throttle
                PortfolioWidget().update(context, glanceId)
                
                // Keep Double-Stamp for redundancy with system options
                val options = appWidgetManager.getAppWidgetOptions(id)
                options.putInt("vault_id", vSafe.id)
                options.putString("static_vault_name", vSafe.name)
                options.putBoolean("static_show_total", vSafe.showWidgetTotal)
                options.putString("static_bg_color", vSafe.widgetBgColor)
                options.putString("static_bg_text_color", vSafe.widgetBgTextColor)
                options.putString("static_card_color", vSafe.widgetCardColor)
                options.putString("static_card_text_color", vSafe.widgetCardTextColor)
                appWidgetManager.updateAppWidgetOptions(id, options)
            }
        }

        // 2. Manual Broadcast to ensure OS acknowledges the change for these IDs
        val intent = Intent(context, PortfolioWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsToUpdate)
        }
        context.sendBroadcast(intent)
    }

    fun saveDefaultTheme() {
        viewModelScope.launch {
            themePreferences.saveIsDarkMode(true)
            themePreferences.saveIsCompactViewEnabled(false)
            themePreferences.saveConfirmDelete(true)
        }
    }

    private fun generateSparklineBitmap(history: List<Double>, isPositive: Boolean): Bitmap {
        val width = 140 // 70dp * 2
        val height = 60 // 30dp * 2
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = if (isPositive) android.graphics.Color.GREEN else android.graphics.Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        if (history.size < 2) return bitmap

        val min = history.minOrNull() ?: 0.0
        val max = history.maxOrNull() ?: 1.0
        val range = if (max - min > 0) max - min else 1.0
        val path = Path()
        val stepX = width.toFloat() / (history.size - 1)

        history.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = ((value - min) / range).toFloat()
            val y = height - (normalizedY * (height - 8) + 4)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)
        return bitmap
    }

    fun getVaultById(vaultId: Int) = viewModelScope.launch {
        vaultDao.getVaultById(vaultId)
    }
}