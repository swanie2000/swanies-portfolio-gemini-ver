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
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.swanie.portfolio.R
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.*
import com.swanie.portfolio.security.SecurityManager
import com.swanie.portfolio.widget.PortfolioWidget
import com.swanie.portfolio.widget.PortfolioWidgetReceiver
import com.swanie.portfolio.widget.SparklineDrawUtils
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
import kotlinx.coroutines.NonCancellable
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

    // 🛡️ SECURITY PREFERENCE: Track if the biometric lock is active
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

    /**
     * 🛡️ THE SECURITY HANDSHAKE:
     * Requires the user to authenticate before enabling or disabling biometric security.
     */
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
            // Require authentication to DISABLE security
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

    suspend fun saveWidgetAppearance(vaultId: Int, bg: String, bgText: String, card: String, cardText: String) {
        vaultDao.updateWidgetColors(vaultId, bg, bgText, card, cardText)
        triggerWidgetUpdate(vaultId)
    }

    suspend fun getVaultByAppWidgetId(appWidgetId: Int): VaultEntity? {
        return vaultDao.getVaultByAppWidgetId(appWidgetId)
    }

    suspend fun getVaultById(vaultId: Int): VaultEntity? {
        return vaultDao.getVaultById(vaultId)
    }

    fun clearAllAssets(vaultId: Int) {
        viewModelScope.launch {
            assetDao.deleteAll()
            vaultDao.updateSelectedWidgetAssets(vaultId, "")
            userConfigDao.updateLastSync(0L)
            triggerWidgetUpdate(vaultId)
        }
    }

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

    suspend fun forceImmediateRemoteViewsUpdate(vaultId: Int, appWidgetId: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val vault = vaultDao.getVaultById(vaultId) ?: return
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout_fallback)
        val bgColor = vault.widgetBgColor.toColorInt()
        val bgTextColor = vault.widgetBgTextColor.toColorInt()
        remoteViews.setInt(R.id.widget_root, "setBackgroundColor", bgColor)
        remoteViews.setTextColor(R.id.vault_name_text, bgTextColor)
        remoteViews.setTextColor(R.id.total_balance_text, bgTextColor)
        remoteViews.setTextColor(R.id.widget_loading_msg, bgTextColor)
        remoteViews.setTextViewText(R.id.vault_name_text, vault.name.uppercase())
        if (vault.showWidgetTotal) {
            val total = assetDao.getAssetsByVaultOnce(vaultId).sumOf { (it.officialSpotPrice * it.weight * it.amountHeld) + it.premium }
            remoteViews.setTextViewText(R.id.total_balance_text, NumberFormat.getCurrencyInstance(Locale.US).format(total))
        } else {
            remoteViews.setTextViewText(R.id.total_balance_text, "")
        }
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    suspend fun triggerWidgetUpdate(vaultId: Int? = null) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, PortfolioWidgetReceiver::class.java)
        val idsToUpdate = if (vaultId != null) {
            val vault = vaultDao.getVaultById(vaultId)
            if (vault?.appWidgetId != null && vault.appWidgetId != -1) intArrayOf(vault.appWidgetId!!)
            else appWidgetManager.getAppWidgetIds(componentName)
        } else {
            appWidgetManager.getAppWidgetIds(componentName)
        }
        if (idsToUpdate.isEmpty()) return
        val vault = vaultId?.let { vaultDao.getVaultById(it) }
        val glanceManager = GlanceAppWidgetManager(context)
        
        // 🚀 NON-CANCELLABLE DELIVERY: Ensure the suitcase is packed even if the VM is cleared
        withContext(NonCancellable) {
            idsToUpdate.forEach { id ->
                val v = if (vaultId != null) vault else vaultDao.getVaultByAppWidgetId(id)
                v?.let { vSafe ->
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
                            val allAssetsForVault = assetDao.getAssetsByVaultOnce(vSafe.id)
                            val selectedIds = vSafe.selectedWidgetAssets.split(",").filter { it.isNotBlank() }
                            val filteredAssets = if (selectedIds.isEmpty()) allAssetsForVault.take(10) else allAssetsForVault.filter { it.coinId in selectedIds }
                            val serializedAssets = withContext(Dispatchers.IO) {
                                filteredAssets.map { asset ->
                                    val iconSource = when {
                                        asset.category == AssetCategory.METAL || asset.isMetal -> "res:ic_${asset.symbol.lowercase()}"
                                        asset.imageUrl.startsWith("file:") -> asset.imageUrl
                                        asset.localIconPath != null -> "file:${asset.localIconPath}"
                                        else -> asset.imageUrl
                                    }
                                    val assetValue = (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
                                    val formattedTotal = formatBoutiquePrice(assetValue)

                                    // 📈 Generate Sparkline for Widget
                                    val history = database.priceHistoryDao().getRecentHistory(asset.coinId).map { it.price }.reversed()
                                    val sparklinePath = if (history.size >= 2) {
                                        val color = if (asset.priceChange24h >= 0) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
                                        val bitmap = SparklineDrawUtils.drawSparklineBitmap(history, color)
                                        val file = File(context.cacheDir, "spark_${asset.coinId}.png")
                                        FileOutputStream(file).use { out ->
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                        }
                                        file.absolutePath
                                    } else "none"

                                    // 🛡️ Packing 10 parts: coinId|symbol|displayName|imageUrl|officialSpotPrice|priceChange24h|weight|amountHeld|calculatedTotal|sparklinePath
                                    val safeSymbol = asset.symbol.replace("|", " ").replace("\n", "").trim()
                                    val safeDisplayName = (asset.displayName.ifBlank { asset.name }).replace("|", " ").replace("\n", "").trim()
                                    
                                    // 🎯 DYNAMIC PRECISION: Bulletproof Boutique Formatter
                                    val price = asset.officialSpotPrice
                                    val formattedPrice = formatBoutiquePrice(price)
                                    Log.d("SWANIE_PRECISION", "Asset: $safeSymbol | Raw Bits: ${java.lang.Double.doubleToLongBits(price)} | Raw: $price | Formatted: $formattedPrice")
                                    
                                    "${asset.coinId}|$safeSymbol|$safeDisplayName|$iconSource|$formattedPrice|${asset.priceChange24h}|${asset.weight}|${asset.amountHeld}|$formattedTotal|$sparklinePath"
                                }.joinToString("||")
                            }
                            if (serializedAssets.isNotBlank()) {
                                this.remove(PortfolioWidget.ASSETS_DATA_KEY)
                                this[PortfolioWidget.ASSETS_DATA_KEY] = serializedAssets
                            }
                            this[PortfolioWidget.LAST_UPDATED_KEY] = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                            val total = allAssetsForVault.sumOf { (it.officialSpotPrice * it.weight * it.amountHeld) + it.premium }
                            this[PortfolioWidget.STATIC_TOTAL_BALANCE_KEY] = NumberFormat.getCurrencyInstance(Locale.US).format(total)
                        }.toPreferences()
                    }
                    PortfolioWidget().update(context, glanceId)
                }
            }
            
            // 🚚 Using ProcessLifecycleOwner to ensure the update isn't tied to the fragment's death
            ProcessLifecycleOwner.get().lifecycleScope.launch {
                PortfolioWidget().updateAll(context)
            }

            context.sendBroadcast(Intent(context, PortfolioWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsToUpdate)
            })
        }
    }

    private fun generateSparklineBitmap(history: List<Double>, isPositive: Boolean): Bitmap {
        val width = 140; val height = 60
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = if (isPositive) android.graphics.Color.GREEN else android.graphics.Color.RED
            style = Paint.Style.STROKE; strokeWidth = 4f; isAntiAlias = true
        }
        if (history.size < 2) return bitmap
        val min = history.minOrNull() ?: 0.0; val max = history.maxOrNull() ?: 1.0; val range = if (max - min > 0) max - min else 1.0
        val path = Path(); val stepX = width.toFloat() / (history.size - 1)
        history.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (((value - min) / range).toFloat() * (height - 8) + 4)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)
        return bitmap
    }

    private fun formatBoutiquePrice(price: Double): String {
        return when {
            price >= 0.10 -> String.format(Locale.US, "%.2f", price)
            price >= 0.0001 -> String.format(Locale.US, "%.5f", price)
            else -> String.format(Locale.US, "%.8f", price)
        }
    }
}
