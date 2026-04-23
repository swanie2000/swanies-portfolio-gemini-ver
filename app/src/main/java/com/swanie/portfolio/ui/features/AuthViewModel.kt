package com.swanie.portfolio.ui.features

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🛡️ AUTH VIEWMODEL: Manages vault security and initialization.
 * The auto-lock gate has been removed to allow the Home Screen animation to play first.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val securityManager: SecurityManager,
    private val vaultDao: VaultDao
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    val isBiometricEnabled: StateFlow<Boolean> =
        themePreferences.isBiometricEnabled.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false,
        )

    init {
        Log.d("VAULT_DEBUG", "AuthViewModel Initialized. Ready for login.")
    }

    /**
     * Resets the authentication state. Called when hardware auth is successful.
     */
    fun setAuthenticated() {
        _authState.value = AuthState.Authenticated
    }

    /**
     * 🏛️ VAULT INITIALIZATION: Creates the primary vault.
     * After creation, we move directly to Authenticated to enter the app.
     */
    fun initializeNewVault(vaultName: String = "My Vault") {
        viewModelScope.launch {
            val existing = vaultDao.getAllVaultsFlow().first()
            if (existing.isEmpty()) {
                vaultDao.upsertVault(
                    VaultEntity(
                        name = vaultName,
                        baseCurrency = "USD",
                        vaultColor = "#000416",
                        sortOrder = 0
                    )
                )
            }
            setAuthenticated()
        }
    }

    /**
     * 🛡️ HARDWARE BRIDGE: Triggered manually by the Login button.
     */
    fun triggerBiometricUnlock(
        activity: androidx.fragment.app.FragmentActivity,
        forcePrompt: Boolean = false,
    ) {
        if (!isBiometricEnabled.value && !forcePrompt) {
            // Direct pass: never initialize biometric prompt when setting is disabled.
            setAuthenticated()
            return
        }
        securityManager.authenticate(
            activity = activity,
            onSuccess = { setAuthenticated() },
            onError = { Log.e("AUTH_ERROR", "Biometric unlock failed: $it") }
        )
    }

    fun setLocked() {
        _authState.value = AuthState.Locked
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Locked : AuthState()
        object Authenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }
}