package com.swanie.portfolio.billing

import com.swanie.portfolio.data.ProUnlockState
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BetaUnlockAccessTest {

    private val today = LocalDate.of(2026, 5, 23)
    private val future = today.plusDays(10).toEpochDay()
    private val past = today.minusDays(1).toEpochDay()

    @Test
    fun unlockOnlyWhenNotSuperseded() {
        val unlock = ProUnlockState(email = "a@b.com", expiryEpochDay = future)
        assertTrue(
            BetaUnlockAccess.resolveIsProUser(
                revenueCatPro = false,
                unlock = unlock,
            ),
        )
    }

    @Test
    fun supersededStillAllowsUnlockWhenRevenueCatInactive() {
        val unlock = ProUnlockState(email = "a@b.com", expiryEpochDay = future)
        assertTrue(
            BetaUnlockAccess.resolveIsProUser(
                revenueCatPro = false,
                unlock = unlock,
            ),
        )
    }

    @Test
    fun supersededUsesRevenueCatPro() {
        val unlock = ProUnlockState(email = "a@b.com", expiryEpochDay = past)
        assertTrue(
            BetaUnlockAccess.resolveIsProUser(
                revenueCatPro = true,
                unlock = unlock,
            ),
        )
    }

    @Test
    fun revenueCatProWithoutSupersededStillPro() {
        val unlock = ProUnlockState()
        assertTrue(
            BetaUnlockAccess.resolveIsProUser(
                revenueCatPro = true,
                unlock = unlock,
            ),
        )
    }
}
