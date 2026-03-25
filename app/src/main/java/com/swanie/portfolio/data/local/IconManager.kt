package com.swanie.portfolio.data.local

import android.content.Context
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
}
