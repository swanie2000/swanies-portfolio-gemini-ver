package com.swanie.portfolio.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.data.local.UserConfigEntity
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.widget.PortfolioWidget
import com.swanie.portfolio.widget.PortfolioWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    fun updateShowWidgetTotal(show: Boolean) {
        viewModelScope.launch {
            userConfigDao.updateShowWidgetTotal(show)
            triggerWidgetUpdate()
        }
    }

    // 🌐 GLOBAL VISTA: Manual Save Action for Widget Configuration (Vault-Specific)
    fun saveWidgetConfiguration(vaultId: Int, selectedIds: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val idsString = selectedIds.joinToString(",")
            vaultDao.updateSelectedWidgetAssets(vaultId, idsString)
            triggerWidgetUpdate()
            _isSaving.value = false
            onComplete()
        }
    }

    // 🛡️ PER-VAULT APPEARANCE: Saves custom widget colors for a specific vault
    suspend fun saveWidgetAppearance(vaultId: Int, bg: String, bgText: String, card: String, cardText: String) {
        vaultDao.updateWidgetColors(vaultId, bg, bgText, card, cardText)
        triggerWidgetUpdate()
    }

    // 🛡️ NUCLEAR RESET: Deletes ALL assets, resets widget selections, and resets timestamp.
    fun clearAllAssets(vaultId: Int) {
        viewModelScope.launch {
            assetDao.deleteAll()
            vaultDao.updateSelectedWidgetAssets(vaultId, "")
            userConfigDao.updateLastSync(0L)
            triggerWidgetUpdate()
        }
    }

    // 🛡️ SURGICAL RESET: ONLY clears widget selection string for specific vault.
    fun clearWidgetSelection(vaultId: Int) {
        viewModelScope.launch {
            vaultDao.updateSelectedWidgetAssets(vaultId, "")
            triggerWidgetUpdate()
        }
    }

    fun updateSelectedWidgetAssets(vaultId: Int, assets: String) {
        viewModelScope.launch {
            vaultDao.updateSelectedWidgetAssets(vaultId, assets)
            triggerWidgetUpdate()
        }
    }

    fun updateWidgetBgColor(vaultId: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            vaultDao.updateWidgetColors(vaultId, color, v.widgetBgTextColor, v.widgetCardColor, v.widgetCardTextColor)
            triggerWidgetUpdate()
        }
    }

    fun updateWidgetBgTextColor(vaultId: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, color, v.widgetCardColor, v.widgetCardTextColor)
            triggerWidgetUpdate()
        }
    }

    fun updateWidgetCardColor(vaultId: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, v.widgetBgTextColor, color, v.widgetCardTextColor)
            triggerWidgetUpdate()
        }
    }

    fun updateWidgetCardTextColor(vaultId: Int, color: String) {
        viewModelScope.launch {
            val v = vaultDao.getVaultById(vaultId) ?: return@launch
            vaultDao.updateWidgetColors(vaultId, v.widgetBgColor, v.widgetBgTextColor, v.widgetCardColor, color)
            triggerWidgetUpdate()
        }
    }

    /**
     * 🚀 THE TRIGGER: Forces an immediate refresh across the Glance framework
     * and sends a manual broadcast to the Widget Receiver to bypass OS caching.
     */
    fun triggerWidgetUpdate() {
        viewModelScope.launch {
            // 1. Update Glance Internal State
            PortfolioWidget().updateAll(context)
            
            // 2. Manual Broadcast to ensure OS acknowledges the change
            val intent = Intent(context, PortfolioWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, PortfolioWidgetReceiver::class.java))
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
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
