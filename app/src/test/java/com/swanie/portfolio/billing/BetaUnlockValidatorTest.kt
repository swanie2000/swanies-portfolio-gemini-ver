package com.swanie.portfolio.billing

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BetaUnlockValidatorTest {

    private val secret = "test-secret-for-unit-tests-only"
    private val email = "tester@gmail.com"
    private val today = LocalDate.of(2026, 5, 22)
    private val expiry = LocalDate.of(2027, 5, 22)

    @Test
    fun validCodeForMatchingEmail() {
        val code = generateTestCode(email, expiry, secret)
        val result = BetaUnlockValidator.validate(
            rawCode = code,
            loggedInEmail = email,
            secret = secret,
            programEndIso = "2027-06-01",
            today = today,
        )
        assertTrue(result is BetaUnlockValidator.Result.Valid)
    }

    @Test
    fun wrongEmailFailsSignature() {
        val code = generateTestCode(email, expiry, secret)
        val result = BetaUnlockValidator.validate(
            rawCode = code,
            loggedInEmail = "other@gmail.com",
            secret = secret,
            programEndIso = "2027-06-01",
            today = today,
        )
        assertEquals(
            BetaUnlockValidator.Reason.INVALID_SIGNATURE,
            (result as BetaUnlockValidator.Result.Invalid).reason,
        )
    }

    @Test
    fun expiredCodeFails() {
        val code = generateTestCode(email, LocalDate.of(2025, 1, 1), secret)
        val result = BetaUnlockValidator.validate(
            rawCode = code,
            loggedInEmail = email,
            secret = secret,
            programEndIso = "2027-06-01",
            today = today,
        )
        assertEquals(
            BetaUnlockValidator.Reason.EXPIRED,
            (result as BetaUnlockValidator.Result.Invalid).reason,
        )
    }

    @Test
    fun programEndedRejectsRedemption() {
        val code = generateTestCode(email, expiry, secret)
        val result = BetaUnlockValidator.validate(
            rawCode = code,
            loggedInEmail = email,
            secret = secret,
            programEndIso = "2026-01-01",
            today = today,
        )
        assertEquals(
            BetaUnlockValidator.Reason.PROGRAM_ENDED,
            (result as BetaUnlockValidator.Result.Invalid).reason,
        )
    }

    private fun generateTestCode(
        email: String,
        expiry: LocalDate,
        secret: String,
    ): String {
        val payload = "${email.trim().lowercase()}|${expiry}"
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(javax.crypto.spec.SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        val digest = mac.doFinal(payload.toByteArray())
        val tag = digest.take(4).joinToString("") { b -> "%02X".format(b) }
        val compact =
            "${expiry.year}${expiry.monthValue.toString().padStart(2, '0')}${expiry.dayOfMonth.toString().padStart(2, '0')}"
        return "SWANIE-$compact-$tag"
    }
}
