package com.swanie.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val themePreferences: ThemePreferences,
    private val vaultDao: VaultDao
) : ViewModel() {

    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

    /** 3s splash fallback; cancelled when vault handshake finishes or activity stops. */
    private var emergencyDataReadyFallback: Job? = null

    // 🌐 GLOBAL VISTA: Vault State
    val allVaults = vaultDao.getAllVaultsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentVaultId = themePreferences.currentVaultId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val defaultVaultId = themePreferences.defaultVaultId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val resetToDefaultOnStart = themePreferences.resetToDefaultOnStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // 🛡️ REFACTORED: The "Source of Truth" for the UI
    // This flow now explicitly waits until allVaults is not empty and isDataReady is true
    val activeVault: StateFlow<VaultEntity> = combine(
        allVaults,
        currentVaultId,
        isDataReady
    ) { vaults, currentId, ready ->
        if (!ready || vaults.isEmpty()) {
            VaultEntity(name = "LOADING...", id = -1)
        } else {
            vaults.find { it.id == currentId } ?: vaults.first()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        VaultEntity(name = "INITIALIZING...", id = -1)
    )

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
    val isBiometricEnabled = themePreferences.isBiometricEnabled.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        runBlocking { themePreferences.isBiometricEnabled.first() }
    )

    init {
        viewModelScope.launch {
            supervisorScope {
                emergencyDataReadyFallback = launch {
                    delay(3000L) // EMERGENCY TIMEOUT: Force ready after 3s
                    _isDataReady.value = true
                }
                launch {
                    // Resolve once DB is available, even if it's currently empty.
                    val vaults = vaultDao.getAllVaultsFlow().first()

                    val resetOnStart = themePreferences.resetToDefaultOnStart.first()
                    val defId = themePreferences.defaultVaultId.first()
                    val lastId = themePreferences.currentVaultId.first()

                    val targetId = when {
                        vaults.isEmpty() -> defId
                        resetOnStart -> defId
                        vaults.any { it.id == lastId } -> lastId
                        else -> defId
                    }

                    // Atomically set the ID and signal that the UI is ready to render
                    themePreferences.saveCurrentVaultId(targetId)
                    _isDataReady.value = true
                    emergencyDataReadyFallback?.cancel()
                    emergencyDataReadyFallback = null
                }
            }
        }
    }

    /**
     * Called from [MainActivity.onStop]: cancels the splash "force ready" timer so it does not
     * fire while the activity is not visible (avoids orphaned handshake work).
     */
    fun cancelEmergencyDataReadyFallback() {
        emergencyDataReadyFallback?.cancel()
        emergencyDataReadyFallback = null
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
        val nextOrder = (allVaults.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
        vaultDao.upsertVault(VaultEntity(name = name, baseCurrency = "USD", vaultColor = "#000416", sortOrder = nextOrder))
    }

    // 🛠️ Management logic for Default and Delete
    fun setDefaultVault(id: Int) = viewModelScope.launch {
        themePreferences.saveDefaultVaultId(id)
    }

    fun setResetToDefault(enabled: Boolean) = viewModelScope.launch {
        themePreferences.saveResetToDefaultOnStart(enabled)
    }

    fun deleteVault(vault: VaultEntity) = viewModelScope.launch {
        if (allVaults.value.size > 1) {
            if (activeVault.value.id == vault.id) {
                val fallback = allVaults.value.firstOrNull { it.id != vault.id }
                fallback?.let { selectVault(it.id) }
            }
            vaultDao.deleteVault(vault)
        }
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

    fun updateVaultOrder(newList: List<VaultEntity>) {
        viewModelScope.launch {
            newList.forEachIndexed { index, vault ->
                vaultDao.upsertVault(vault.copy(sortOrder = index))
            }
        }
    }
}