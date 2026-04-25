package com.swanie.portfolio.security

import com.swanie.portfolio.data.local.UserProfileEntity

object AuthPolicy {
    data class PasswordStrength(
        val normalized: String,
        val hasMinLength: Boolean,
        val hasCapital: Boolean,
        val hasNumber: Boolean,
        val hasSymbol: Boolean
    ) {
        val isValid: Boolean
            get() = hasMinLength && hasCapital && hasNumber && hasSymbol
    }

    fun normalizeIdentity(input: String): String = input.trim().replace("\\s".toRegex(), "").lowercase()

    fun normalizePassword(input: String): String = input.trim().replace("\\s".toRegex(), "")

    fun evaluatePasswordStrength(input: String): PasswordStrength {
        val normalized = normalizePassword(input)
        return PasswordStrength(
            normalized = normalized,
            hasMinLength = normalized.length >= 8,
            hasCapital = normalized.any { it.isUpperCase() },
            hasNumber = normalized.any { it.isDigit() },
            hasSymbol = normalized.any { !it.isLetterOrDigit() }
        )
    }

    fun matchesCredentials(
        inputIdentity: String,
        inputPassword: String,
        profile: UserProfileEntity
    ): Boolean {
        val normalizedIdentity = normalizeIdentity(inputIdentity)
        val normalizedPassword = normalizePassword(inputPassword)
        if (normalizedIdentity.isBlank() || normalizedPassword.isBlank()) return false

        val storedUserName = normalizeIdentity(
            if (profile.userName.isBlank()) profile.displayName else profile.userName
        )
        val storedDisplayName = normalizeIdentity(profile.displayName)
        val storedEmail = normalizeIdentity(profile.email)

        val identityMatch = normalizedIdentity == storedUserName ||
            normalizedIdentity == storedDisplayName ||
            normalizedIdentity == storedEmail

        return identityMatch && profile.loginPassword == normalizedPassword
    }

    fun shouldLockAfterResume(
        timeoutSeconds: Int,
        elapsedMs: Long,
        isAuthenticated: Boolean
    ): Boolean {
        if (!isAuthenticated) return false
        if (timeoutSeconds < 0) return false // "Never" option
        return elapsedMs > timeoutSeconds * 1000L
    }
}
