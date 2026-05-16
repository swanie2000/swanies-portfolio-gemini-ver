package com.swanie.portfolio.data.feedback

import android.util.Log
import com.swanie.portfolio.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Posts bug/feedback in the background via [Web3Forms](https://web3forms.com) (no mail client).
 *
 * Set **`WEB3FORMS_ACCESS_KEY`** in **`local.properties`** (free key from web3forms.com → same inbox).
 * The access key is safe in the public website script when domain-restricted in the Web3Forms dashboard.
 *
 * Logcat: filter **`SwanieBugReport`** (case-sensitive) to see every attempt, HTTP status, and errors.
 */
@Singleton
class BugReportSubmitter @Inject constructor(
    @Named("Feedback") private val okHttpClient: OkHttpClient,
) {
    companion object {
        const val LOG_TAG = "SwanieBugReport"
        private const val WEB3FORMS_SUBMIT_URL = "https://api.web3forms.com/submit"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun submit(
        accountEmail: String,
        userMessage: String,
        localeTag: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val accessKey = BuildConfig.WEB3FORMS_ACCESS_KEY.trim()
        if (accessKey.isBlank()) {
            Log.e(LOG_TAG, "WEB3FORMS_ACCESS_KEY is missing — add it to local.properties")
            return@withContext Result.failure(IllegalStateException("WEB3FORMS_ACCESS_KEY missing"))
        }

        Log.i(LOG_TAG, "submit() start version=${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        val fullMessage = buildString {
            append("App: ").append(BuildConfig.VERSION_NAME).append(" (")
                .append(BuildConfig.VERSION_CODE).append(")\n")
            append("Locale: ").append(localeTag).append("\n")
            append("Account email (on file): ").append(accountEmail.ifBlank { "(none)" }).append("\n\n")
            append("Message:\n").append(userMessage)
        }
        val replyEmail = accountEmail.trim().ifBlank { "anonymous@example.com" }

        val payload = JSONObject().apply {
            put("access_key", accessKey)
            put("subject", "Swanie's Portfolio — bug/feedback (${BuildConfig.VERSION_NAME})")
            put("name", "Portfolio user")
            put("email", replyEmail)
            put("message", fullMessage)
            put("botcheck", "")
        }

        postJson(payload)
    }

    private fun postJson(payload: JSONObject): Result<Unit> {
        val result = runCatching {
            val request = Request.Builder()
                .url(WEB3FORMS_SUBMIT_URL)
                .post(payload.toString().toRequestBody(JSON_MEDIA))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                Log.i(
                    LOG_TAG,
                    "HTTP ${response.code} from Web3Forms bodyLen=${text.length} preview=${text.take(160).replace('\n', ' ')}",
                )

                if (!response.isSuccessful) {
                    error("HTTP ${response.code}: ${text.take(400)}")
                }

                val trimmed = text.trimStart()
                if (trimmed.startsWith("{")) {
                    val jo = JSONObject(text)
                    if (!jo.optBoolean("success", false)) {
                        val msg = jo.optString("message", "Web3Forms rejected the request")
                        error(msg)
                    }
                }
            }
        }
        result.exceptionOrNull()?.let { ex ->
            Log.e(LOG_TAG, "postJson exception: ${ex.javaClass.simpleName} ${ex.message}", ex)
        }
        return result
    }
}
