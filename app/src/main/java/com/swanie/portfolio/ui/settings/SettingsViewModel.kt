package com.swanie.portfolio.ui.settings

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.data.local.UserConfigEntity
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.widget.PortfolioWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val themePreferences: ThemePreferences,
    private val userConfigDao: UserConfigDao,
    private val assetDao: AssetDao,
    private val vaultDao: VaultDao
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
            updateWidget()
        }
    }

    // 🌐 GLOBAL VISTA: Manual Save Action for Widget Configuration (Vault-Specific)
    fun saveWidgetConfiguration(vaultId: Int, selectedIds: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val idsString = selectedIds.joinToString(",")
            vaultDao.updateSelectedWidgetAssets(vaultId, idsString)
            updateWidget()
            _isSaving.value = false
            onComplete()
        }
    }

    // 🛡️ PER-VAULT APPEARANCE: Saves custom widget colors for a specific vault
    suspend fun saveWidgetAppearance(vaultId: Int, bg: String, bgText: String, card: String, cardText: String) {
        vaultDao.updateWidgetColors(vaultId, bg, bgText, card, cardText)
        updateWidget()
    }

    // 🛡️ NUCLEAR RESET: Deletes ALL assets, resets widget selections, and resets timestamp.
    fun clearAllAssets(vaultId: Int) {
        viewModelScope.launch {
            assetDao.deleteAll()
            vaultDao.updateSelectedWidgetAssets(vaultId, "")
            userConfigDao.updateLastSync(0L)
            updateWidget()
        }
    }

    // 🛡️ SURGICAL RESET: ONLY clears widget selection string for specific vault.
    fun clearWidgetSelection(vaultId: Int) {
        viewModelScope.launch {
            vaultDao.updateSelectedWidgetAssets(vaultId, "")
            updateWidget()
        }
    }

    fun updateSelectedWidgetAssets(vaultId: Int, assets: String) {
        viewModelScope.launch {
            vaultDao.updateSelectedWidgetAssets(vaultId, assets)
            updateWidget()
        }
    }

    fun updateWidgetBgColor(color: String) {
        viewModelScope.launch {
            userConfigDao.updateWidgetBgColor(color)
            updateWidget()
        }
    }

    fun updateWidgetBgTextColor(color: String) {
        viewModelScope.launch {
            userConfigDao.updateWidgetBgTextColor(color)
            updateWidget()
        }
    }

    fun updateWidgetCardColor(color: String) {
        viewModelScope.launch {
            userConfigDao.updateWidgetCardColor(color)
            updateWidget()
        }
    }

    fun updateWidgetCardTextColor(color: String) {
        viewModelScope.launch {
            userConfigDao.updateWidgetCardTextColor(color)
            updateWidget()
        }
    }

    private suspend fun updateWidget() {
        PortfolioWidget().updateAll(context)
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

    val allVaults: StateFlow<List<VaultEntity>> = vaultDao.getAllVaultsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
