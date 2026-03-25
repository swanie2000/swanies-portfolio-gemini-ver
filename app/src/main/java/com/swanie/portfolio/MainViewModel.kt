package com.swanie.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val themePreferences: ThemePreferences,
    private val vaultDao: VaultDao
) : ViewModel() {

    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

    // 🌐 GLOBAL VISTA: Vault State
    val allVaults = vaultDao.getAllVaultsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentVaultId = themePreferences.currentVaultId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    
    val activeVault = combine(allVaults, currentVaultId) { vaults, id ->
        vaults.find { it.id == id } ?: vaults.firstOrNull() ?: VaultEntity(name = "MAIN PORTFOLIO")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VaultEntity(name = "MAIN PORTFOLIO"))

    // Preferences observed as StateFlows
    val siteBackgroundColor = themePreferences.siteBackgroundColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#000416")
    val siteTextColor = themePreferences.siteTextColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#FFFFFF")
    val cardBackgroundColor = themePreferences.cardBackgroundColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#121212")
    val cardTextColor = themePreferences.cardTextColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#FFFFFF")
    val useGradient = themePreferences.useGradient.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val gradientAmount = themePreferences.gradientAmount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)
    val isCompactViewEnabled = themePreferences.isCompactViewEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isDarkMode = themePreferences.isDarkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val confirmDelete = themePreferences.confirmDelete.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        _isDataReady.value = true
    }

    // 🌐 GLOBAL VISTA: Vault Logic
    fun selectVault(id: Int) = viewModelScope.launch {
        themePreferences.saveCurrentVaultId(id)
    }

    fun updateVaultName(id: Int, newName: String) = viewModelScope.launch {
        vaultDao.updateVaultName(id, newName)
    }

    fun updateVaultCurrency(id: Int, code: String) = viewModelScope.launch {
        vaultDao.updateVaultCurrency(id, code)
    }

    fun createNewVault(name: String) = viewModelScope.launch {
        vaultDao.upsertVault(VaultEntity(name = name, baseCurrency = "USD", vaultColor = "#000416"))
    }

    fun setConfirmDelete(enabled: Boolean) = viewModelScope.launch {
        themePreferences.saveConfirmDelete(enabled)
    }

    fun toggleCompactView() = viewModelScope.launch {
        themePreferences.saveIsCompactViewEnabled(!isCompactViewEnabled.value)
    }

    fun setUseGradient(enabled: Boolean) = viewModelScope.launch {
        themePreferences.saveUseGradient(enabled)
    }

    fun setGradientAmount(amount: Float) = viewModelScope.launch {
        themePreferences.saveGradientAmount(amount)
    }
}
