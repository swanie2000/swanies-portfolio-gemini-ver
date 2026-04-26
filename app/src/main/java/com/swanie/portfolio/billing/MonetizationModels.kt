package com.swanie.portfolio.billing

enum class AccessTier {
    FREE,
    PRO
}

data class EntitlementSnapshot(
    val tier: AccessTier = AccessTier.FREE,
    val isActive: Boolean = false,
    val source: String = "none"
)

data class MonetizationPackage(
    val identifier: String,
    val title: String,
    val priceText: String
)

