package com.swanie.portfolio.widget

import com.swanie.portfolio.billing.AccessTier
import com.swanie.portfolio.billing.EntitlementSnapshot

/**
 * Home-screen widget: how many holdings rows we show and persist per tier.
 * Free users use the default widget path (no Widget Manager); Pro uses Widget Manager.
 */
object WidgetAssetLimits {
    const val FREE_MAX = 3
    const val PRO_MAX = 8

    fun capFor(snapshot: EntitlementSnapshot): Int =
        if (snapshot.tier == AccessTier.PRO && snapshot.isActive) PRO_MAX else FREE_MAX
}
