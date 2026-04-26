package com.swanie.portfolio.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface MonetizationManager {
    val entitlement: StateFlow<EntitlementSnapshot>

    suspend fun refreshEntitlement()

    suspend fun restorePurchases(): Result<Unit>

    suspend fun fetchPackages(): Result<List<MonetizationPackage>>

    suspend fun purchasePackage(
        activity: Activity,
        packageIdentifier: String
    ): Result<Unit>
}

