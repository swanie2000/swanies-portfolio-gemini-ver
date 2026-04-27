package com.swanie.portfolio.billing

import android.app.Activity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.swanie.portfolio.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class RevenueCatMonetizationManager @Inject constructor() : MonetizationManager {

    private val entitlementId = BuildConfig.REVENUECAT_PRO_ENTITLEMENT
    private val offeringId = BuildConfig.REVENUECAT_OFFERING_ID
    private val _entitlement = MutableStateFlow(EntitlementSnapshot())
    override val entitlement: StateFlow<EntitlementSnapshot> = _entitlement.asStateFlow()
    private val packageCache = mutableMapOf<String, Package>()
    private var currentAppUserId: String? = null

    init {
        purchasesOrNull()?.updatedCustomerInfoListener =
            object : UpdatedCustomerInfoListener {
                override fun onReceived(customerInfo: CustomerInfo) {
                    _entitlement.value = customerInfo.toEntitlementSnapshot(entitlementId)
                }
            }
    }

    override suspend fun setAppUser(appUserId: String?) {
        val purchases = purchasesOrNull() ?: return
        val normalized = appUserId?.trim().orEmpty()
        if (normalized.isBlank() || normalized == currentAppUserId) return

        runCatching {
            val loginResult = purchases.awaitLogIn(normalized)
            currentAppUserId = normalized
            _entitlement.value = loginResult.customerInfo.toEntitlementSnapshot(entitlementId)
        }
    }

    override suspend fun refreshEntitlement() {
        val purchases = purchasesOrNull()
        if (purchases == null) {
            _entitlement.value = EntitlementSnapshot()
            return
        }

        val customerInfo = purchases.awaitCustomerInfo()
        _entitlement.value = customerInfo.toEntitlementSnapshot(entitlementId)
    }

    override suspend fun restorePurchases(): Result<Unit> {
        val purchases = purchasesOrNull() ?: return Result.success(Unit)
        return runCatching {
            val customerInfo = purchases.awaitRestore()
            _entitlement.value = customerInfo.toEntitlementSnapshot(entitlementId)
        }.map { Unit }
    }

    override suspend fun fetchPackages(): Result<List<MonetizationPackage>> {
        val purchases = purchasesOrNull() ?: return Result.success(emptyList())
        return runCatching {
            val offerings = purchases.awaitOfferings()
            val targetOffering = offerings[offeringId] ?: offerings.current
            val availablePackages = targetOffering?.availablePackages.orEmpty()
            packageCache.clear()
            availablePackages.forEach { packageCache[it.identifier] = it }
            availablePackages.map {
                MonetizationPackage(
                    identifier = it.identifier,
                    title = it.product.title,
                    priceText = it.product.price.formatted
                )
            }
        }
    }

    override suspend fun purchasePackage(
        activity: Activity,
        packageIdentifier: String
    ): Result<Unit> {
        val purchases = purchasesOrNull() ?: return Result.failure(
            IllegalStateException("RevenueCat API key is not configured.")
        )
        return runCatching {
            val packageToPurchase = packageCache[packageIdentifier]
                ?: run {
                    fetchPackages().getOrNull()
                    packageCache[packageIdentifier]
                }
                ?: throw IllegalStateException("Package not found: $packageIdentifier")

            val purchaseResult = purchases.awaitPurchase(
                PurchaseParams.Builder(activity, packageToPurchase).build()
            )
            _entitlement.value = purchaseResult.customerInfo.toEntitlementSnapshot(entitlementId)
        }.map { Unit }
    }

    private fun purchasesOrNull(): Purchases? {
        if (BuildConfig.REVENUECAT_API_KEY.isBlank()) return null
        return runCatching { Purchases.sharedInstance }.getOrNull()
    }
}

private fun CustomerInfo.toEntitlementSnapshot(entitlementId: String): EntitlementSnapshot {
    val activeEntitlement = entitlements.active[entitlementId]
    val isPro = activeEntitlement?.isActive == true
    return EntitlementSnapshot(
        tier = if (isPro) AccessTier.PRO else AccessTier.FREE,
        isActive = isPro,
        source = "revenuecat"
    )
}

