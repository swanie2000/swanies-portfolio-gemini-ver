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
    val passwordHint: String
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    val googleDriveService: GoogleDriveService // 🔓 Changed from private to val to fix access error
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // 🏛️ UI Helper states for the Restore Screen
    private val _isRestoring = MutableStateFlow(false)
    val isRestoring = _isRestoring.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    fun handleSignInResult(account: GoogleSignInAccount?) {
        _authState.value = AuthState.Loading

        if (account == null) {
            _authState.value = AuthState.Error("Sign-in failed: No account selected.")
            return
        }

        viewModelScope.launch {
            try {
                googleDriveService.initializeDriveService(account)
                val vaultExists = googleDriveService.checkVaultFolderExists()

                if (vaultExists) {
                    _authState.value = AuthState.VaultFound
                } else {
                    _authState.value = AuthState.NewVaultRequired
                }
            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Handshake Error", e)
                _authState.value = AuthState.Error("Vault handshake failed: ${e.message}")
            }
        }
    }

    /**
     * 🚀 SOVEREIGN RESTORE: Pulls the backup and populates the local Room DB.
     */
    fun restoreVaultFromCloud(onSuccess: () -> Unit) {
        _isRestoring.value = true
        _authError.value = null

        viewModelScope.launch {
            try {
                // Trigger the download through the service
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