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

/**
 * VaultMetadata holds the non-sensitive configuration for the Sovereign Vault.
 * This is stored in the hidden appDataFolder as vault_metadata.json.
 * ⚠️ WE NEVER STORE THE VAULT PASSWORD HERE.
 */
data class VaultMetadata(
    val fullName: String,
    val baseCurrency: String,
    val language: String,
    val passwordHint: String
)

/**
 * AuthViewModel handles the business logic for the Sovereign Vault Handshake.
 * It coordinates with GoogleDriveService to initialize the zero-knowledge sync.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    val googleDriveService: GoogleDriveService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    /**
     * Handles the result of the Google Sign-In intent.
     * Triggers the Drive Handshake if an account is provided.
     */
    fun handleSignInResult(account: GoogleSignInAccount?) {
        // PRECISION FIX: Set loading state IMMEDIATELY to trigger UI feedback.
        _authState.value = AuthState.Loading
        
        Log.d("AUTH_DEBUG", "Account name: ${account?.displayName}")

        if (account == null) {
            _authState.value = AuthState.Error("Sign-in failed: No account selected.")
            return
        }

        viewModelScope.launch {
            try {
                // Step 1: Initialize the Remote Service with the credentials
                googleDriveService.initializeDriveService(account)

                // Step 2: Check if a vault already exists for this Sovereign user
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
     * Initializes a new Sovereign Vault by creating the vault_metadata.json file.
     */
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
        object Authenticated : AuthState() // User is fully set up and ready to enter the app
        object VaultFound : AuthState()
        object NewVaultRequired : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
