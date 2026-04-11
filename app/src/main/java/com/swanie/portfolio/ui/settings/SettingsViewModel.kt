package com.swanie.portfolio.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.graphics.toColorInt
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.R
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.*
import com.swanie.portfolio.widget.PortfolioWidget
import com.swanie.portfolio.widget.PortfolioWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val themePreferences: ThemePreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val userConfigDao = database.userConfigDao()
    private val assetDao = database.assetDao()
    private val vaultDao = database.vaultDao()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // 🎯 THE REGISTRY: Track the currently targeted vault for widget configuration
    private val _targetVaultId = MutableStateFlow<Int>(-1)
    val targetVaultId: StateFlow<Int> = _targetVaultId.asStateFlow()

    val isDarkMode: StateFlow<Boolean> = themePreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isCompactViewEnabled: StateFlow<Boolean> = themePreferences.isCompactViewEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val confirmDelete: StateFlow<Boolean> = themePreferences.confirmDelete
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

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

    suspend fun updateShowWidgetTotal(vaultId: Int, show: Boolean) {
        vaultDao.updateShowWidgetTotal(vaultId, show)
        triggerWidgetUpdate(vaultId)
    }

    // 🌐 GLOBAL VISTA: Manual Save Action for Widget Configuration (Vault-Specific)
    fun saveWidgetConfiguration(vaultId: Int, selectedIds: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
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

    // 🛡️ SURGICAL BINDING: Links a vault to a specific appWidgetId in the DB
    fun bindVaultToWidget(vaultId: Int, appWidgetId: Int) {
        viewModelScope.launch {
            vaultDao.updateBoundAppWidgetId(vaultId, appWidgetId)
        }
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
            if (vault?.boundAppWidgetId != null && vault.boundAppWidgetId != -1) {
                intArrayOf(vault.boundAppWidgetId)
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
            vault?.let { v ->
                // 🚀 DATASTORE HANDSHAKE: Write directly to PreferencesGlanceStateDefinition
                val glanceId = glanceManager.getGlanceIdBy(id)
                androidx.glance.appwidget.state.updateAppWidgetState(context, androidx.glance.state.PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[PortfolioWidget.VAULT_ID_KEY] = v.id
                        this[PortfolioWidget.STATIC_VAULT_NAME_KEY] = v.name
                        this[PortfolioWidget.WIDGET_BG_COLOR_KEY] = v.widgetBgColor
                        this[PortfolioWidget.WIDGET_BG_TEXT_COLOR_KEY] = v.widgetBgTextColor
                        this[PortfolioWidget.WIDGET_CARD_COLOR_KEY] = v.widgetCardColor
                        this[PortfolioWidget.WIDGET_CARD_TEXT_COLOR_KEY] = v.widgetCardTextColor
                        this[PortfolioWidget.SHOW_TOTAL_KEY] = v.showWidgetTotal
                        
                        // 🚀 FULL-FREIGHT HANDSHAKE: Serialize Top 10 Assets with File-Aware Icons
                        val selectedIds = v.selectedWidgetAssets.split(",").filter { it.isNotBlank() }
                        val allAssets = assetDao.getAssetsByVaultOnce(v.id)
                        val topAssets = if (selectedIds.isEmpty()) allAssets.take(10)
                        else selectedIds.mapNotNull { id -> allAssets.find { it.coinId == id } }.take(10)

                        val serializedAssets = topAssets.joinToString("||") { asset ->
                            val iconSource = when {
                                asset.category == AssetCategory.METAL || asset.isMetal -> {
                                    val metalName = asset.symbol.lowercase()
                                    "res:ic_$metalName"
                                }
                                asset.imageUrl.startsWith("file:") -> asset.imageUrl
                                asset.localIconPath != null -> "file:${asset.localIconPath}"
                                else -> asset.imageUrl // Fallback to URL or empty
                            }
                            val assetValue = (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
                            "${asset.coinId}|${asset.symbol}|${asset.displayName.ifBlank { asset.name }}|$iconSource|$assetValue|${asset.priceChange24h}"
                        }
                        this[PortfolioWidget.ASSETS_DATA_KEY] = serializedAssets
                        
                        // Calculate total for static balance
                        val total = allAssets.sumOf {
                            (it.officialSpotPrice * it.weight * it.amountHeld) + it.premium 
                        }
                        this[PortfolioWidget.STATIC_TOTAL_BALANCE_KEY] = NumberFormat.getCurrencyInstance(Locale.US).format(total)
                    }.toPreferences()
                }

                // 🚀 CRITICAL: Update PortfolioWidget in the SAME scope to bypass throttle
                PortfolioWidget().update(context, glanceId)
                
                // Keep Double-Stamp for redundancy with system options
                val options = appWidgetManager.getAppWidgetOptions(id)
                options.putInt("vault_id", v.id)
                options.putString("static_vault_name", v.name)
                options.putBoolean("static_show_total", v.showWidgetTotal)
                options.putString("static_bg_color", v.widgetBgColor)
                options.putString("static_bg_text_color", v.widgetBgTextColor)
                options.putString("static_card_color", v.widgetCardColor)
                options.putString("static_card_text_color", v.widgetCardTextColor)
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

    fun getVaultById(vaultId: Int) = viewModelScope.launch {
        vaultDao.getVaultById(vaultId)
    }
}
