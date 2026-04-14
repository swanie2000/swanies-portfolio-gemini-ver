package com.swanie.portfolio.ui.features

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🛡️ SOVEREIGN AUTH: Replaces legacy Google Sign-In with local Biometric Security.
 * This file is now clean of all unresolved Google references.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val securityManager: SecurityManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        Log.d("VAULT_DEBUG", "Sovereign AuthViewModel Initialized.")
        checkSecurityRequirement()
    }

    /**
     * Checks local preferences to see if the Biometric Lock is enabled.
     * If enabled, we set state to Locked to trigger the biometric prompt.
     */
    private fun checkSecurityRequirement() {
        viewModelScope.launch {
            themePreferences.isBiometricEnabled.collect { enabled ->
                if (enabled) {
                    _authState.value = AuthState.Locked
                } else {
                    _authState.value = AuthState.Authenticated
                }
            }
        }
    }

    /**
     * Resets the authentication state. Called when authentication is successful.
     */
    fun setAuthenticated() {
        _authState.value = AuthState.Authenticated
    }

    /**
     * Resets the authentication state to Locked.
     */
    fun setLocked() {
        _authState.value = AuthState.Locked
    }

    /**
     * 🛡️ HARDWARE BRIDGE: Triggers the biometric prompt and updates state on success.
     */
    fun triggerBiometricUnlock(activity: androidx.fragment.app.FragmentActivity) {
        securityManager.authenticate(
            activity = activity,
            onSuccess = { setAuthenticated() },
            onError = { /* Errors are handled by the system dialog usually */ }
        )
    }

    /**
     * Allows manual locking of the vault (e.g., when the app goes to background).
     */
    fun lockVault() {
        viewModelScope.launch {
            if (themePreferences.isBiometricEnabled.run { true }) { // Check flow value if needed
                _authState.value = AuthState.Locked
            }
        }
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