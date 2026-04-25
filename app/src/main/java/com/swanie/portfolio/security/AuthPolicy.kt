package com.swanie.portfolio.security

import com.swanie.portfolio.data.local.UserProfileEntity

object AuthPolicy {
    fun normalizeIdentity(input: String): String = input.trim().replace("\\s".toRegex(), "").lowercase()

    fun normalizePassword(input: String): String = input.trim().replace("\\s".toRegex(), "")

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
