package com.swanie.portfolio.data.local

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()

    suspend fun downloadIcon(symbol: String, url: String): String? = withContext(Dispatchers.IO) {
        if (url.isEmpty()) return@withContext null

        val iconDir = File(context.filesDir, "icons")
        if (!iconDir.exists()) iconDir.mkdirs()

        val fileName = "${symbol.uppercase()}.png"
        val iconFile = File(iconDir, fileName)

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.let { body ->
                    FileOutputStream(iconFile).use { output ->
                        output.write(body.bytes())
                    }
                    Log.d("ICON_MANAGER", "Icon saved for $symbol: ${iconFile.absolutePath}")
                    return@withContext iconFile.absolutePath
                }
            }
        } catch (e: Exception) {
            Log.e("ICON_MANAGER", "Failed to download icon for $symbol: ${e.message}")
        }
        null
    }

    fun customIconFile(coinId: String): File {
        val safeId = coinId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val dir = File(context.filesDir, "custom_icons")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$safeId.png")
    }

    /** DB path if valid; heal from disk only when DB still references a custom icon. */
    fun resolvedCustomIconPath(coinId: String, storedPath: String?): String? {
        if (storedPath.isNullOrBlank()) return null
        val stored = File(storedPath)
        if (stored.exists()) return storedPath
        val onDisk = customIconFile(coinId)
        return if (onDisk.exists()) onDisk.absolutePath else null
    }

    suspend fun persistCustomIconFromUri(coinId: String, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val dest = customIconFile(coinId)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            } ?: return@withContext null
            Log.d("ICON_MANAGER", "Custom icon saved for $coinId -> ${dest.absolutePath}")
            dest.absolutePath
        } catch (e: Exception) {
            Log.e("ICON_MANAGER", "persistCustomIconFromUri failed: ${e.message}")
            null
        }
    }

    suspend fun deleteCustomAssetIcon(coinId: String) = withContext(Dispatchers.IO) {
        try {
            val f = customIconFile(coinId)
            if (f.exists() && f.delete()) {
                Log.d("ICON_MANAGER", "Removed custom icon for $coinId")
            }
        } catch (e: Exception) {
            Log.e("ICON_MANAGER", "deleteCustomAssetIcon failed: ${e.message}")
        }
    }
}
