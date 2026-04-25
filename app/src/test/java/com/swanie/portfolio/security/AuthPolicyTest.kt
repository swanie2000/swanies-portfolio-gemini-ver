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
}
