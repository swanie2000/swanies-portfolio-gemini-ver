package com.swanie.portfolio.widget

import com.swanie.portfolio.BuildConfig
import com.swanie.portfolio.billing.AccessTier
import com.swanie.portfolio.billing.ClosedTestProAccess
import com.swanie.portfolio.billing.EntitlementSnapshot

/**
 * Home-screen widget: how many holdings rows we show and persist per tier.
 * Free users use the default widget path (no Widget Manager); Pro uses Widget Manager.
 */
object WidgetAssetLimits {
    const val FREE_MAX = 3
    const val PRO_MAX = 8

    fun isProForWidget(
        snapshot: EntitlementSnapshot,
        closedTestProUntilEpochMs: Long,
    ): Boolean {
        val revenueCatPro = snapshot.tier == AccessTier.PRO && snapshot.isActive
        return ClosedTestProAccess.resolveIsProUser(
            revenueCatPro = revenueCatPro,
            untilEpochMs = closedTestProUntilEpochMs,
        )
    }

    fun isProForWidget(snapshot: EntitlementSnapshot): Boolean =
        isProForWidget(snapshot, BuildConfig.CLOSED_TEST_PRO_UNTIL_EPOCH_MS)

    fun capFor(snapshot: EntitlementSnapshot, closedTestProUntilEpochMs: Long): Int =
        if (isProForWidget(snapshot, closedTestProUntilEpochMs)) PRO_MAX else FREE_MAX

    fun capFor(snapshot: EntitlementSnapshot): Int =
        capFor(snapshot, BuildConfig.CLOSED_TEST_PRO_UNTIL_EPOCH_MS)
}
