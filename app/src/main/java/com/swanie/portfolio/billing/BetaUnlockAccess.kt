package com.swanie.portfolio.billing

import com.swanie.portfolio.data.ProUnlockState

/**
 * Beta unlock is temporary until RevenueCat entitlement takes over (promotional or purchase).
 */
object BetaUnlockAccess {

    const val CODE_VALIDITY_DAYS = 30

    fun resolveIsProUser(
        revenueCatPro: Boolean,
        unlock: ProUnlockState,
        supersededByRevenueCat: Boolean,
    ): Boolean =
        if (supersededByRevenueCat) {
            revenueCatPro
        } else {
            revenueCatPro || unlock.isActive()
        }
}
