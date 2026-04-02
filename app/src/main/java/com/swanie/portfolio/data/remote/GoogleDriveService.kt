package com.swanie.portfolio.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GoogleDriveService handles the "Handshake" between the app and the user's hidden Google Drive App Folder.
 * Part of the V7.5.0 "Sovereign Vault" Zero-Knowledge Architecture.
 */
@Singleton
class GoogleDriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var driveService: Drive? = null

    /**
     * Provides a GoogleSignInClient configured for the Zero-Knowledge Vault.
     * Uses the DRIVE_APPDATA scope to ensure the developer cannot access other user files.
     */
    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Initializes the Drive Service using the authenticated Google Account.
     * This is the technical "Handshake" that unlocks the Sovereign Vault.
     */
    fun initializeDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = account.account
        }

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Swanie's Portfolio")
            .build()
    }

    /**
     * Queries the hidden appDataFolder to see if the encrypted vault exists.
     * This verifies if the user is a returning vault holder or a new "Sovereign."
     *
     * @return Boolean true if the vault file is found.
     */
    suspend fun checkVaultFolderExists(): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext false
        try {
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            files != null && files.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Creates the initial vault metadata file in the hidden appDataFolder.
     * This is the "Birth" of the Sovereign Vault.
     */
    suspend fun createVaultManifest(jsonContent: String): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext false
        try {
            val fileMetadata = File().apply {
                name = "vault_metadata.json"
                parents = listOf("appDataFolder")
            }

            val contentStream = ByteArrayContent.fromString("application/json", jsonContent)

            service.files().create(fileMetadata, contentStream)
                .setFields("id")
                .execute()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
