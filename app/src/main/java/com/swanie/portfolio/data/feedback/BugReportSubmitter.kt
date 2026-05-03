package com.swanie.portfolio.data.feedback

import android.util.Log
import com.swanie.portfolio.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Posts bug/feedback in the background via [FormSubmit](https://formsubmit.co) (no mail client).
 *
 * Uses FormSubmit’s **form id** (opaque token) in the URL — not the raw inbox address — so
 * submissions match the form you activated for **swanies.portfolio@gmail.com**.
 *
 * Logcat: filter **`SwanieBugReport`** (case-sensitive) to see every attempt, HTTP status, and errors.
 */
@Singleton
class BugReportSubmitter @Inject constructor(
    @Named("Feedback") private val okHttpClient: OkHttpClient,
) {
    companion object {
        /** Single tag so Logcat search `SwanieBugReport` always finds this flow. */
        const val LOG_TAG = "SwanieBugReport"

        /**
         * FormSubmit “invisible form” id (from their activation email). Replaces the naked email in
         * `/ajax/...` and `/{id}` paths; deliveries still go to the linked Gmail after you tap **Activate Form**.
         */
        private const val FORMSUBMIT_FORM_ID = "6a72ac79f262ea55b696d51c2f75eb4c"
    }

    suspend fun submit(
        accountEmail: String,
        userMessage: String,
        localeTag: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i(LOG_TAG, "submit() start version=${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        val fullMessage = buildString {
            append("App: ").append(BuildConfig.VERSION_NAME).append(" (")
                .append(BuildConfig.VERSION_CODE).append(")\n")
            append("Locale: ").append(localeTag).append("\n")
            append("Account email (on file): ").append(accountEmail.ifBlank { "(none)" }).append("\n\n")
            append("Message:\n").append(userMessage)
        }
        val replyEmail = accountEmail.trim().ifBlank { "anonymous@example.com" }

        val ajaxUrl = HttpUrl.Builder()
            .scheme("https")
            .host("formsubmit.co")
            .addPathSegment("ajax")
            .addPathSegment(FORMSUBMIT_FORM_ID)
            .build()

        val classicUrl = HttpUrl.Builder()
            .scheme("https")
            .host("formsubmit.co")
            .addPathSegment(FORMSUBMIT_FORM_ID)
            .build()

        // Each POST must use its own FormBody — OkHttp RequestBody is not always safe to reuse.
        Log.i(LOG_TAG, "Trying FormSubmit AJAX url=$ajaxUrl")
        val ajaxResult = postForm(ajaxUrl, buildFormBody(replyEmail, fullMessage))
        if (ajaxResult.isSuccess) {
            Log.i(LOG_TAG, "AJAX endpoint succeeded")
            return@withContext ajaxResult
        }
        ajaxResult.exceptionOrNull()?.let { ex ->
            Log.e(LOG_TAG, "AJAX attempt failed: ${ex.message}", ex)
        }

        Log.i(LOG_TAG, "Trying FormSubmit classic url=$classicUrl")
        val classicResult = postForm(classicUrl, buildFormBody(replyEmail, fullMessage))
        classicResult.onFailure { ex ->
            Log.e(LOG_TAG, "Classic attempt failed: ${ex.message}", ex)
        }
        classicResult.onSuccess {
            Log.i(LOG_TAG, "Classic endpoint succeeded")
        }
        classicResult
    }

    private fun buildFormBody(replyEmail: String, fullMessage: String): FormBody =
        FormBody.Builder()
            .add("_subject", "Swanie's Portfolio — bug/feedback (${BuildConfig.VERSION_NAME})")
            .add("name", "Portfolio user")
            .add("email", replyEmail)
            .add("message", fullMessage)
            .add("_captcha", "false")
            .build()

    private fun postForm(url: HttpUrl, body: FormBody): Result<Unit> {
        val result = runCatching {
            val request = Request.Builder()
                .url(url)
                .post(body)
                .header("Origin", "https://formsubmit.co")
                .header("Referer", "https://formsubmit.co/")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val code = response.code
                val text = response.body?.string().orEmpty()
                Log.i(LOG_TAG, "HTTP $code from $url bodyLen=${text.length} preview=${text.take(120).replace('\n', ' ')}")

                if (!response.isSuccessful) {
                    error("HTTP $code: ${text.take(400)}")
                }
                val trimmed = text.trimStart()
                if (trimmed.startsWith("{")) {
                    val jo = runCatching { JSONObject(text) }.getOrNull()
                    if (jo != null && !parseSuccessFlag(jo)) {
                        error("FormSubmit rejected JSON: ${text.take(400)}")
                    }
                }
            }
        }
        result.exceptionOrNull()?.let { ex ->
            Log.e(LOG_TAG, "postForm exception for $url: ${ex.javaClass.simpleName} ${ex.message}", ex)
        }
        return result
    }

    private fun parseSuccessFlag(jo: JSONObject): Boolean {
        if (!jo.has("success")) return true
        return when (val s = jo.opt("success")) {
            is Boolean -> s
            is String -> s.equals("true", ignoreCase = true)
            is Int -> s != 0
            else -> true
        }
    }
}
