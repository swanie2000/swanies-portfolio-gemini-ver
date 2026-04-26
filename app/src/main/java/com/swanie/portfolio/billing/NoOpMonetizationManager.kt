package com.swanie.portfolio.billing

import android.app.Activity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class NoOpMonetizationManager @Inject constructor() : MonetizationManager {

    private val _entitlement = MutableStateFlow(EntitlementSnapshot())
    override val entitlement: StateFlow<EntitlementSnapshot> = _entitlement.asStateFlow()

    override suspend fun refreshEntitlement() {
        // Intentionally no-op while RevenueCat wiring is in progress.
    }

    override suspend fun restorePurchases(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun fetchPackages(): Result<List<MonetizationPackage>> {
        return Result.success(emptyList())
    }

    override suspend fun purchasePackage(
        activity: Activity,
        packageIdentifier: String
    ): Result<Unit> {
        return Result.failure(IllegalStateException("Billing is not configured."))
    }
}

