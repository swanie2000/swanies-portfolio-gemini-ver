package com.swanie.portfolio.security

import com.swanie.portfolio.data.local.UserProfileEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthPolicyTest {

    private val profile = UserProfileEntity(
        id = 1,
        userName = "SwaniePrime",
        displayName = "Swanie Prime",
        email = "swanie@example.com",
        loginPassword = "SecurePass1!"
    )

    @Test
    fun `matchesCredentials succeeds with username`() {
        val result = AuthPolicy.matchesCredentials(
            inputIdentity = " swanieprime ",
            inputPassword = "SecurePass1!",
            profile = profile
        )

        assertTrue(result)
    }

    @Test
    fun `matchesCredentials succeeds with display name`() {
        val result = AuthPolicy.matchesCredentials(
            inputIdentity = "swanie prime",
            inputPassword = "SecurePass1!",
            profile = profile
        )

        assertTrue(result)
    }

    @Test
    fun `matchesCredentials succeeds with email`() {
        val result = AuthPolicy.matchesCredentials(
            inputIdentity = "swanie@example.com",
            inputPassword = "SecurePass1!",
            profile = profile
        )

        assertTrue(result)
    }

    @Test
    fun `matchesCredentials fails for wrong password`() {
        val result = AuthPolicy.matchesCredentials(
            inputIdentity = "swanieprime",
            inputPassword = "WrongPass",
            profile = profile
        )

        assertFalse(result)
    }

    @Test
    fun `matchesCredentials falls back to display name when username is blank`() {
        val profileWithBlankUsername = profile.copy(
            userName = "",
            displayName = "Swanie Prime"
        )

        val result = AuthPolicy.matchesCredentials(
            inputIdentity = "swanieprime",
            inputPassword = "SecurePass1!",
            profile = profileWithBlankUsername
        )

        assertTrue(result)
    }

    @Test
    fun `matchesCredentials normalizes whitespace in password input`() {
        val result = AuthPolicy.matchesCredentials(
            inputIdentity = "swanieprime",
            inputPassword = " Secure Pass1! ",
            profile = profile
        )

        assertTrue(result)
    }

    @Test
    fun `shouldLockAfterResume respects Never timeout`() {
        val shouldLock = AuthPolicy.shouldLockAfterResume(
            timeoutSeconds = -1,
            elapsedMs = 999_999L,
            isAuthenticated = true
        )

        assertFalse(shouldLock)
    }

    @Test
    fun `shouldLockAfterResume locks only after threshold`() {
        val shouldNotLock = AuthPolicy.shouldLockAfterResume(
            timeoutSeconds = 60,
            elapsedMs = 59_000L,
            isAuthenticated = true
        )
        val shouldLock = AuthPolicy.shouldLockAfterResume(
            timeoutSeconds = 60,
            elapsedMs = 61_000L,
            isAuthenticated = true
        )

        assertFalse(shouldNotLock)
        assertTrue(shouldLock)
    }

    @Test
    fun `shouldLockAfterResume never locks when user is not authenticated`() {
        val shouldLock = AuthPolicy.shouldLockAfterResume(
            timeoutSeconds = 15,
            elapsedMs = 1_000_000L,
            isAuthenticated = false
        )

        assertFalse(shouldLock)
    }

    @Test
    fun `evaluatePasswordStrength returns valid for strong password`() {
        val strength = AuthPolicy.evaluatePasswordStrength("StrongPass1!")

        assertTrue(strength.hasMinLength)
        assertTrue(strength.hasCapital)
        assertTrue(strength.hasNumber)
        assertTrue(strength.hasSymbol)
        assertTrue(strength.isValid)
    }

    @Test
    fun `evaluatePasswordStrength returns invalid when requirements missing`() {
        val strength = AuthPolicy.evaluatePasswordStrength("weakpass")

        assertTrue(strength.hasMinLength)
        assertFalse(strength.hasCapital)
        assertFalse(strength.hasNumber)
        assertFalse(strength.hasSymbol)
        assertFalse(strength.isValid)
    }

    @Test
    fun `evaluatePasswordStrength normalizes whitespace before evaluation`() {
        val strength = AuthPolicy.evaluatePasswordStrength(" Strong Pass 1! ")

        assertTrue(strength.isValid)
        assertTrue(strength.normalized == "StrongPass1!")
    }
}
