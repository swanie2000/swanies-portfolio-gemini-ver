package com.swanie.portfolio.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClosedTestProAccessTest {

    @Test
    fun grantActiveWhenNowBeforeUntil() {
        val until = 1_000_000_000_000L
        assertTrue(ClosedTestProAccess.isGrantActive(until, nowMs = until - 1))
    }

    @Test
    fun grantInactiveWhenUntilZeroOrPast() {
        assertFalse(ClosedTestProAccess.isGrantActive(0L))
        assertFalse(ClosedTestProAccess.isGrantActive(100L, nowMs = 200L))
    }

    @Test
    fun resolveProFromRevenueCatOrGrant() {
        val until = 2_000_000_000_000L
        val now = 1_000_000_000_000L
        assertTrue(
            ClosedTestProAccess.resolveIsProUser(
                revenueCatPro = true,
                untilEpochMs = 0L,
                nowMs = now,
            ),
        )
        assertTrue(
            ClosedTestProAccess.resolveIsProUser(
                revenueCatPro = false,
                untilEpochMs = until,
                nowMs = now,
            ),
        )
        assertFalse(
            ClosedTestProAccess.resolveIsProUser(
                revenueCatPro = false,
                untilEpochMs = until,
                nowMs = until + 1,
            ),
        )
    }
}
