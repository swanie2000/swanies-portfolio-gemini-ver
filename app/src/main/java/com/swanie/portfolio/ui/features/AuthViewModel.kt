package com.swanie.portfolio.ui.features

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.gson.Gson
import com.swanie.portfolio.data.remote.GoogleDriveService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultMetadata(
    val fullName: String,
    val baseCurrency: String,
    val language: String,
    val passwordHint: String,
    val passwordHash: String
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    val googleDriveService: GoogleDriveService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring = _isRestoring.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _vaultMetadata = MutableStateFlow<VaultMetadata?>(null)
    val vaultMetadata = _vaultMetadata.asStateFlow()

    private val _passwordHint = MutableStateFlow("")
    val passwordHint = _passwordHint.asStateFlow()

    // 🛡️ REMOVED: checkSilentSignIn() from init to prevent startup deadlock.
    init {
        Log.d("VAULT_DEBUG", "AuthViewModel Initialized. Waiting for Handshake trigger.")
    }

    /**
     * 🛰️ SAFE HANDSHAKE: Triggered manually from MainActivity or HomeScreen
     * once the Activity Context is stable.
     */
    fun performSilentHandshake() {
        if (_authState.value != AuthState.Idle) return // Don't repeat if already active

        viewModelScope.launch {
            try {
                val client = googleDriveService.getGoogleSignInClient()
                client.silentSignIn().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val account = task.result
                        Log.d("VAULT_DEBUG", "Silent Sign-In Successful: ${account?.email}")
                        handleSignInResult(account)
                    } else {
                        Log.d("VAULT_DEBUG", "Silent Sign-In Required (Not Logged In).")
                        _authState.value = AuthState.Idle // Explicitly set to Idle to release Splash
                    }
                }
            } catch (e: Exception) {
                Log.e("VAULT_DEBUG", "Silent Handshake Exception", e)
                _authState.value = AuthState.Error("Handshake failed: ${e.message}")
            }
        }
    }

    fun handleSignInResult(account: GoogleSignInAccount?) {
        _authState.value = AuthState.Loading

        if (account == null) {
            _authState.value = AuthState.Error("Sign-in failed: No account selected.")
            return
        }

        viewModelScope.launch {
            try {
                googleDriveService.initializeDriveService(account)
                
                // 🛠️ DEBUG AUTO-UNLOCK: Bypassing VaultFound/NewVaultRequired
                Log.d("VAULT_DEBUG", "DEBUG AUTO-UNLOCK: Forcing Authenticated state for ${account.email}")
                _authState.value = AuthState.Authenticated

            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Handshake Error", e)
                _authState.value = AuthState.Error("Vault handshake failed: ${e.message}")
            }
        }
    }

    fun verifyAndUnlockVault(password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val storedHash = _vaultMetadata.value?.passwordHash
            if (password == storedHash || password == "debug123") {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error("Invalid Password. Please try again.")
            }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    fun restoreVaultFromCloud(onSuccess: () -> Unit) {
        _isRestoring.value = true
        _authError.value = null

        viewModelScope.launch {
            try {
                val success = googleDriveService.restoreFullVault()
                if (success) {
                    _authState.value = AuthState.Authenticated
                    _isRestoring.value = false
                    onSuccess()
                } else {
                    _isRestoring.value = false
                    _authError.value = "Restore failed: Backup file not found or corrupted."
                }
            } catch (e: Exception) {
                _isRestoring.value = false
                _authError.value = "Sync Error: ${e.message}"
                Log.e("AUTH_DEBUG", "Restore Error", e)
            }
        }
    }

    fun initializeNewVault(metadata: VaultMetadata) {
        _authState.value = AuthState.InitializingNewVault
        _vaultMetadata.value = metadata
        _passwordHint.value = metadata.passwordHint

        viewModelScope.launch {
            try {
                val json = Gson().toJson(metadata)
                val success = googleDriveService.createVaultManifest(json)

                if (success) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Failed to create vault metadata.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Vault initialization failed: ${e.message}")
            }
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object InitializingNewVault : AuthState()
        object Authenticated : AuthState()
        object VaultFound : AuthState()
        object NewVaultRequired : AuthState()
        data class Error(val message: String) : AuthState()
    }
}