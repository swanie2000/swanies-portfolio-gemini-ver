package com.swanie.portfolio.billing

import com.swanie.portfolio.data.ProUnlockState

/**
 * Beta unlock is temporary until RevenueCat entitlement takes over (promotional or purchase).
 * If RC was active then lapsed, a valid local unlock can apply again (family / recovery).
 */
object BetaUnlockAccess {

    const val CODE_VALIDITY_DAYS = 30

    fun resolveIsProUser(
        revenueCatPro: Boolean,
        unlock: ProUnlockState,
    ): Boolean {
        if (revenueCatPro) return true
        return unlock.isActive()
    }
}
