package com.swanie.portfolio.widget

import com.swanie.portfolio.billing.AccessTier
import com.swanie.portfolio.billing.EntitlementSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetAssetLimitsTest {

    private val freeSnapshot = EntitlementSnapshot(tier = AccessTier.FREE, isActive = false)

    @Test
    fun capUsesClosedTestGrantWhenRevenueCatFree() {
        val until = 2_000_000_000_000L
        assertEquals(WidgetAssetLimits.PRO_MAX, WidgetAssetLimits.capFor(freeSnapshot, until))
        assertTrue(WidgetAssetLimits.isProForWidget(freeSnapshot, until))
    }

    @Test
    fun capFreeWhenGrantInactive() {
        assertEquals(WidgetAssetLimits.FREE_MAX, WidgetAssetLimits.capFor(freeSnapshot, 0L))
        assertFalse(WidgetAssetLimits.isProForWidget(freeSnapshot, 0L))
    }
}
