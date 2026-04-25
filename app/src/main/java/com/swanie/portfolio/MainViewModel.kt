package com.swanie.portfolio

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.UserDao
import com.swanie.portfolio.data.local.UserProfileEntity
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
    private val vaultDao: VaultDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    /** 3s splash fallback; cancelled when vault handshake finishes or activity stops. */
    private var emergencyDataReadyFallback: Job? = null

    // 🌐 GLOBAL VISTA: Vault State
    val allVaults = vaultDao.getAllVaultsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentVaultId = themePreferences.currentVaultId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val defaultVaultId = themePreferences.defaultVaultId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val starredVaultId = vaultDao.getStarredVaultIdFlow()
        .map { it ?: 1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val hasAcceptedTOS = userDao.getUserProfileFlow()
        .map { it?.hasAcceptedTOS ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
    val isHighVisibilityMode = themePreferences.isHighVisibilityMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isDarkMode = themePreferences.isDarkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val confirmDelete = themePreferences.confirmDelete.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val isBiometricEnabled = themePreferences.isBiometricEnabled.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        runBlocking { themePreferences.isBiometricEnabled.first() }
    )
    val loginResumeTimeoutSeconds = themePreferences.loginResumeTimeoutSeconds.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        runBlocking { themePreferences.loginResumeTimeoutSeconds.first() }
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

                    val defId = themePreferences.defaultVaultId.first()
                    val lastId = themePreferences.currentVaultId.first()
                    val starredId = vaultDao.getStarredVaultId() ?: -1
                    val fallbackDefault = when {
                        starredId > 0 -> starredId
                        defId > 0 -> defId
                        else -> 1
                    }

                    val targetId = when {
                        vaults.isEmpty() -> fallbackDefault
                        vaults.any { it.id == fallbackDefault } -> fallbackDefault
                        vaults.any { it.id == lastId } -> lastId
                        else -> vaults.first().id
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
        vaultDao.setStarredVault(id)
        themePreferences.saveDefaultVaultId(id)
    }

    fun updateTOSAccepted(accepted: Boolean) = viewModelScope.launch {
        val existing = userDao.getUserProfile()
        val next = (existing ?: UserProfileEntity(id = 1)).copy(hasAcceptedTOS = accepted)
        userDao.upsertUserProfile(next)
    }

    fun updateUserProfileBasics(displayName: String, email: String, password: String, acceptedTOS: Boolean) = viewModelScope.launch {
        val existing = userDao.getUserProfile()
        val normalizedUserName = displayName.trim().replace("\\s".toRegex(), "")
        val normalizedPassword = password.trim().replace("\\s".toRegex(), "")
        val next = (existing ?: UserProfileEntity(id = 1)).copy(
            userName = normalizedUserName,
            displayName = displayName.trim(),
            email = email.trim(),
            loginPassword = normalizedPassword,
            hasAcceptedTOS = acceptedTOS
        )
        userDao.upsertUserProfile(next)
    }

    suspend fun createOrUpdateUserProfileAndFetchFirst(
        displayName: String,
        email: String,
        password: String,
        acceptedTOS: Boolean
    ): UserProfileEntity? {
        val existing = userDao.getFirstUser()
        val normalizedUserName = displayName.trim().replace("\\s".toRegex(), "")
        val normalizedPassword = password.trim().replace("\\s".toRegex(), "")
        val next = (existing ?: UserProfileEntity(id = 1)).copy(
            userName = normalizedUserName,
            displayName = displayName.trim(),
            email = email.trim(),
            loginPassword = normalizedPassword,
            hasAcceptedTOS = acceptedTOS
        )
        userDao.upsertUserProfile(next)
        return userDao.getFirstUser()
    }

    suspend fun verifyCredentials(name: String, password: String): Boolean {
        val normalizedName = name.trim().replace("\\s".toRegex(), "")
        val normalizedPassword = password.trim().replace("\\s".toRegex(), "")
        if (normalizedName.isBlank() || normalizedPassword.isBlank()) {
            _isVaultUnlocked.value = false
            return false
        }

        val profile = userDao.getFirstUser()
        if (profile == null) {
            _isVaultUnlocked.value = false
            return false
        }

        val storedUserName = profile.userName
            .ifBlank { profile.displayName.trim().replace("\\s".toRegex(), "") }
            .replace("\\s".toRegex(), "")
            .lowercase()
        val storedDisplayName = profile.displayName
            .trim()
            .replace("\\s".toRegex(), "")
            .lowercase()
        val storedEmail = profile.email
            .trim()
            .lowercase()
        val inputUserName = normalizedName.lowercase()
        Log.d(
            "VAULT_AUTH",
            "Input Username: $inputUserName, DB Username: $storedUserName, DB Email: ${profile.email}, DB Password: ${profile.loginPassword}"
        )

        val userMatch = inputUserName == storedUserName ||
            inputUserName == storedDisplayName ||
            inputUserName == storedEmail
        val isMatch = userMatch &&
            profile.loginPassword == normalizedPassword
        Log.d("VAULT_AUTH", "Comparison Result: $isMatch")
        _isVaultUnlocked.value = isMatch
        return isMatch
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

    fun setHighVisibilityMode(enabled: Boolean) = viewModelScope.launch {
        themePreferences.saveIsHighVisibilityMode(enabled)
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