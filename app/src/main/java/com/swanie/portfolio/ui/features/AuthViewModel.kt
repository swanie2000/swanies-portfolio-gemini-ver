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
import java.security.MessageDigest
import javax.inject.Inject

/**
 * VaultMetadata holds the configuration for the Vault.
 * This is stored in the hidden appDataFolder as vault_metadata.json.
 */
data class VaultMetadata(
    val fullName: String,
    val baseCurrency: String,
    val language: String,
    val passwordHint: String,
    val passwordHash: String // SHA-256 Hash for verification
)

/**
 * AuthViewModel handles the business logic for the Vault Handshake and Decryption.
 * Part of the Zero-Knowledge Sovereign Vault protocol.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    val googleDriveService: GoogleDriveService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _passwordHint = MutableStateFlow("")
    val passwordHint = _passwordHint.asStateFlow()

    private val _vaultMetadata = MutableStateFlow<VaultMetadata?>(null)
    val vaultMetadata = _vaultMetadata.asStateFlow()

    /**
     * Handles the result of the Google Sign-In intent.
     * Triggers the Drive Handshake and identifies if a vault exists.
     */
    fun handleSignInResult(account: GoogleSignInAccount?) {
        _authState.value = AuthState.Loading
        
        Log.d("AUTH_DEBUG", "Processing Sign-In for: ${account?.displayName}")

        if (account == null) {
            _authState.value = AuthState.Error("Sign-in failed: No account selected.")
            return
        }

        viewModelScope.launch {
            try {
                // Step 1: Initialize the Remote Service
                googleDriveService.initializeDriveService(account)

                // Step 2: Check for existing vault
                val vaultExists = googleDriveService.checkVaultFolderExists()

                if (vaultExists) {
                    // LOGIC FIX: Download metadata IMMEDIATELY to avoid "Metadata not loaded" gap
                    fetchVaultMetadataInternal()
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
     * Internal helper to fetch metadata from Drive.
     */
    private suspend fun fetchVaultMetadataInternal() {
        val json = googleDriveService.downloadVaultMetadata()
        if (json != null) {
            try {
                val metadata = Gson().fromJson(json, VaultMetadata::class.java)
                _vaultMetadata.value = metadata
                _passwordHint.value = metadata.passwordHint
            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Metadata parsing failed", e)
            }
        }
    }

    /**
     * Hashes the password using SHA-256 for secure comparison.
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Verifies the input password against the stored SHA-256 hash.
     */
    fun verifyAndUnlockVault(password: String) {
        val currentMetadata = _vaultMetadata.value
        
        // LOGIC GAP FIX: If metadata is still downloading, show loading and retry
        if (currentMetadata == null) {
            _authState.value = AuthState.Loading
            viewModelScope.launch {
                fetchVaultMetadataInternal()
                if (_vaultMetadata.value != null) {
                    verifyAndUnlockVault(password) // Retry with loaded data
                } else {
                    _authState.value = AuthState.Error("Security data could not be retrieved.")
                }
            }
            return
        }

        val inputHash = hashPassword(password)
        if (inputHash == currentMetadata.passwordHash) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Error("Incorrect Password. Please try again.")
        }
    }

    /**
     * Establishes a new vault with metadata and password hash.
     */
    fun initializeNewVault(fullName: String, currency: String, language: String, hint: String, password: String) {
        _authState.value = AuthState.InitializingNewVault
        
        viewModelScope.launch {
            try {
                val metadata = VaultMetadata(
                    fullName = fullName,
                    baseCurrency = currency,
                    language = language,
                    passwordHint = hint,
                    passwordHash = hashPassword(password)
                )
                val json = Gson().toJson(metadata)
                val success = googleDriveService.createVaultManifest(json)
                
                if (success) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Failed to establish vault.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Initialization failed: ${e.message}")
            }
        }
    }

    fun clearError() {
        if (_vaultMetadata.value != null) {
            _authState.value = AuthState.VaultFound
        } else {
            _authState.value = AuthState.Idle
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
