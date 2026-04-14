package com.swanie.portfolio.data.remote

import android.content.Context
import android.util.Log
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

    /**
     * 🛡️ SOVEREIGN INITIALIZATION: Initializes the Drive Service using a credential
     * provided by the OS-level Account Manager. No legacy Sign-In Client required.
     */
    fun initializeDriveService(credential: GoogleAccountCredential) {
        Log.d("VAULT_DEBUG", "Initializing Drive Service via Professional Bridge.")

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Swanie's Portfolio")
            .build()
        Log.d("VAULT_DEBUG", "Drive Service initialized successfully.")
    }

    suspend fun checkVaultFolderExists(): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: run {
            Log.w("VAULT_DEBUG", "checkVaultFolderExists: Drive Service is null.")
            return@withContext false
        }
        try {
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            !files.isNullOrEmpty()
        } catch (e: Exception) {
            Log.e("VAULT_DEBUG", "Check Vault Error", e)
            false
        }
    }

    /**
     * 🛰️ CLOUD PUSH: Uploads local holdings to the user's hidden AppData folder.
     * This keeps you liability-free as the data stays in the user's own Drive.
     */
    suspend fun uploadFullVaultBackup(assets: List<AssetEntity>): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: run {
            Log.w("VAULT_DEBUG", "Sync Skipped: Drive Service NOT initialized.")
            return@withContext false
        }
        try {
            Log.d("VAULT_DEBUG", "Starting Cloud Sync for ${assets.size} assets...")
            val jsonContent = gson.toJson(assets)

            val existingResult = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'vault_backup.json'")
                .setFields("files(id, name)")
                .execute()

            val existingFile = existingResult.files?.firstOrNull()
            val contentStream = ByteArrayContent.fromString("application/json", jsonContent)

            if (existingFile == null) {
                val fileMetadata = File().apply {
                    name = "vault_backup.json"
                    parents = listOf("appDataFolder")
                }
                service.files().create(fileMetadata, contentStream).execute()
                Log.d("VAULT_DEBUG", "✅ SUCCESS: New Vault Backup Created.")
            } else {
                service.files().update(existingFile.id, null, contentStream).execute()
                Log.d("VAULT_DEBUG", "✅ SUCCESS: Vault Backup Updated.")
            }
            true
        } catch (e: Exception) {
            Log.e("VAULT_DEBUG", "❌ FAILURE: Cloud Sync Failed", e)
            false
        }
    }

    /**
     * 📥 CLOUD PULL: Restores the database from the user's hidden vault.
     */
    suspend fun restoreFullVault(): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: run {
            Log.w("VAULT_DEBUG", "Restore Skipped: Drive Service NOT initialized.")
            return@withContext false
        }
        try {
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            val files = result.files ?: emptyList()
            val file = files.find { it.name == "vault_backup.json" }
                ?: return@withContext false

            val outputStream = ByteArrayOutputStream()
            service.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            val jsonString = outputStream.toString()

            val type = object : TypeToken<List<AssetEntity>>() {}.type
            val restoredAssets: List<AssetEntity> = gson.fromJson(jsonString, type)

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
        val service = driveService ?: run {
            Log.w("VAULT_DEBUG", "Create Manifest Skipped: Drive Service NOT initialized.")
            return@withContext false
        }
        try {
            val fileMetadata = File().apply {
                name = "vault_metadata.json"
                parents = listOf("appDataFolder")
            }
            val contentStream = ByteArrayContent.fromString("application/json", jsonContent)
            service.files().create(fileMetadata, contentStream).setFields("id").execute()
            true
        } catch (e: Exception) {
            Log.e("VAULT_DEBUG", "Create Manifest Error", e)
            false
        }
    }
}