package com.swanie.portfolio.data.local

/**
 * Precious-metal spot is treated as **USD per troy ounce** (Yahoo / typical bullion feeds).
 * [AssetEntity.weight] is mass **per line** (one bar, one coin, etc.) in [AssetEntity.weightUnit].
 */
object MetalSpotMath {

    /** Grams in one troy ounce (international standard). */
    const val GRAMS_PER_TROY_OUNCE: Double = 31.1034768

    /**
     * Converts [weight] in [weightUnit] to troy ounces.
     * OZ (and unknown units) are treated as **troy oz**, matching common bullion apps.
     */
    fun massInTroyOunces(weight: Double, weightUnit: String): Double {
        if (weight <= 0.0) return 0.0
        return when (weightUnit.uppercase().trim()) {
            "GRAM", "GRAMS", "G" -> weight / GRAMS_PER_TROY_OUNCE
            "KILO", "KILOS", "KG" -> (weight * 1000.0) / GRAMS_PER_TROY_OUNCE
            else -> weight
        }
    }

    /** USD value at spot for [weight] of metal (one position line). */
    fun spotUsdForMass(spotUsdPerTroyOz: Double, weight: Double, weightUnit: String): Double =
        spotUsdPerTroyOz * massInTroyOunces(weight, weightUnit)
}

/**
 * Portfolio-facing valuation: combines metal troy-oz conversion with crypto’s per-unit model.
 */
object AssetValuation {

    /** Spot value of the full holding (no premium). */
    fun spotMassHoldingsUsd(asset: AssetEntity): Double =
        when (asset.category) {
            AssetCategory.METAL ->
                MetalSpotMath.spotUsdForMass(asset.officialSpotPrice, asset.weight, asset.weightUnit) * asset.amountHeld
            AssetCategory.CRYPTO ->
                asset.officialSpotPrice * asset.weight * asset.amountHeld
        }

    /** Spot + premium (typical portfolio line value). */
    fun holdingValueUsd(asset: AssetEntity): Double =
        spotMassHoldingsUsd(asset) + asset.premium

    /** Value shown in asset card “Price” row (one token, or spot for one metal line’s mass). */
    fun cardPriceRowUsd(asset: AssetEntity): Double =
        when (asset.category) {
            AssetCategory.METAL ->
                MetalSpotMath.spotUsdForMass(asset.officialSpotPrice, asset.weight, asset.weightUnit)
            AssetCategory.CRYPTO -> asset.officialSpotPrice
        }
}
