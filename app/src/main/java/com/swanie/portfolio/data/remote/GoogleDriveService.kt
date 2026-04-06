package com.swanie.portfolio.data.remote

import android.content.Context
import android.util.Log
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val assetDao: AssetDao
) {

    private var driveService: Drive? = null
    private val gson = Gson()

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

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

    suspend fun checkVaultFolderExists(): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext false
        try {
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            !files.isNullOrEmpty()
        } catch (e: Exception) {
            Log.e("DRIVE_SERVICE", "Check Vault Error", e)
            false
        }
    }

    /**
     * 🛰️ CLOUD PUSH: Uploads the current local holdings to the Sovereign Vault.
     * This creates the 'vault_backup.json' file for future restores.
     */
    suspend fun uploadFullVaultBackup(assets: List<AssetEntity>): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext false
        try {
            val jsonContent = gson.toJson(assets)

            // 1. Check for existing backup to update or create
            val existingResult = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'vault_backup.json'")
                .setFields("files(id, name)")
                .execute()

            val existingFile = existingResult.files?.firstOrNull()
            val contentStream = ByteArrayContent.fromString("application/json", jsonContent)

            if (existingFile == null) {
                // Create New
                val fileMetadata = File().apply {
                    name = "vault_backup.json"
                    parents = listOf("appDataFolder")
                }
                service.files().create(fileMetadata, contentStream).execute()
                Log.d("VAULT_DEBUG", "New Vault Backup Created.")
            } else {
                // Update Existing
                service.files().update(existingFile.id, null, contentStream).execute()
                Log.d("VAULT_DEBUG", "Existing Vault Backup Updated.")
            }
            true
        } catch (e: Exception) {
            Log.e("VAULT_DEBUG", "Cloud Sync Failed", e)
            false
        }
    }

    suspend fun restoreFullVault(): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext false
        try {
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            val files = result.files ?: emptyList()
            Log.d("VAULT_DEBUG", "Files found in Cloud: ${files.map { it.name }}")

            val file = files.find { it.name == "vault_backup.json" }
                ?: files.find { it.name == "vault_metadata.json" }
                ?: return@withContext false

            Log.d("VAULT_DEBUG", "Targeting file for restore: ${file.name}")

            val outputStream = ByteArrayOutputStream()
            service.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            val jsonString = outputStream.toString()

            val type = object : TypeToken<List<AssetEntity>>() {}.type
            val restoredAssets: List<AssetEntity> = try {
                gson.fromJson(jsonString, type)
            } catch (e: Exception) {
                Log.e("VAULT_DEBUG", "Corrupt file or wrong format: ${file.name}")
                return@withContext false
            }

            if (restoredAssets.isNotEmpty()) {
                assetDao.upsertAll(restoredAssets)
                Log.d("VAULT_DEBUG", "Successfully restored ${restoredAssets.size} assets.")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("VAULT_DEBUG", "Restoration Failure", e)
            false
        }
    }

    suspend fun createVaultManifest(jsonContent: String): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext false
        try {
            val fileMetadata = File().apply {
                name = "vault_metadata.json"
                parents = listOf("appDataFolder")
            }
            val contentStream = ByteArrayContent.fromString("application/json", jsonContent)
            service.files().create(fileMetadata, contentStream).setFields("id").execute()
            true
        } catch (e: Exception) {
            Log.e("DRIVE_SERVICE", "Create Manifest Error", e)
            false
        }
    }
}