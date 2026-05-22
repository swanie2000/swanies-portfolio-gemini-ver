package com.swanie.portfolio.billing

import com.swanie.portfolio.BuildConfig
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Email-bound beta unlock codes. Must match website/js/beta-unlock-code.js and scripts/beta-unlock-code.mjs.
 * Format: SWANIE-YYYYMMDD-HHHHHHHH
 */
object BetaUnlockValidator {

    private val codePattern = Regex("^SWANIE-(\\d{8})-([A-F0-9]{8})$", RegexOption.IGNORE_CASE)
    private val compactDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    sealed class Result {
        data class Valid(val expiryDate: LocalDate, val normalizedEmail: String) : Result()
        data class Invalid(val reason: Reason) : Result()
    }

    enum class Reason {
        NOT_CONFIGURED,
        PROGRAM_ENDED,
        MALFORMED_CODE,
        WRONG_EMAIL,
        EXPIRED,
        INVALID_SIGNATURE,
    }

    fun validate(
        rawCode: String,
        loggedInEmail: String?,
        secret: String = BuildConfig.BETA_UNLOCK_SECRET,
        programEndIso: String = BuildConfig.BETA_UNLOCK_PROGRAM_END,
        today: LocalDate = LocalDate.now(ZoneId.systemDefault()),
    ): Result {
        if (secret.isBlank()) return Result.Invalid(Reason.NOT_CONFIGURED)

        val programEnd = parseIsoDate(programEndIso)
        if (programEnd != null && today.isAfter(programEnd)) {
            return Result.Invalid(Reason.PROGRAM_ENDED)
        }

        val normalizedCode = rawCode.trim().uppercase()
        val match = codePattern.matchEntire(normalizedCode)
            ?: return Result.Invalid(Reason.MALFORMED_CODE)

        val compactExpiry = match.groupValues[1]
        val providedTag = match.groupValues[2].uppercase()

        val expiryDate =
            runCatching { LocalDate.parse(compactExpiry, compactDateFormatter) }.getOrNull()
                ?: return Result.Invalid(Reason.MALFORMED_CODE)

        if (today.isAfter(expiryDate)) return Result.Invalid(Reason.EXPIRED)

        val email = loggedInEmail?.trim()?.lowercase().orEmpty()
        if (email.isBlank() || !email.contains("@")) {
            return Result.Invalid(Reason.WRONG_EMAIL)
        }

        val payload = "$email|${expiryDate.format(isoDateFormatter)}"
        val expectedTag = hmacTag(secret, payload)
        if (providedTag != expectedTag) return Result.Invalid(Reason.INVALID_SIGNATURE)

        return Result.Valid(expiryDate = expiryDate, normalizedEmail = email)
    }

    fun isUnlockStillActive(
        expiryDate: LocalDate,
        today: LocalDate = LocalDate.now(ZoneId.systemDefault()),
    ): Boolean = !today.isAfter(expiryDate)

    private fun hmacTag(secret: String, payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val digest = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
        return digest.take(4).joinToString("") { byte -> "%02X".format(byte) }
    }

    private fun parseIsoDate(iso: String): LocalDate? =
        if (iso.isBlank()) {
            null
        } else {
            runCatching { LocalDate.parse(iso.trim(), isoDateFormatter) }.getOrNull()
        }
}
