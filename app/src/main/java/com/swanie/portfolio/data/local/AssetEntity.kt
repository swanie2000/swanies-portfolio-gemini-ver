package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetCategory { CRYPTO, METAL }

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val coinId: String = "",
    val symbol: String = "",
    val name: String = "",
    val amountHeld: Double = 0.0,
    val currentPrice: Double = 0.0,
    val change24h: Double = 0.0,
    val displayOrder: Int = 0,
    val lastUpdated: Long = 0L,
    val imageUrl: String = "",
    val category: AssetCategory = AssetCategory.CRYPTO,
    val sparklineData: List<Double> = emptyList(),
    val marketCapRank: Int = 0,
    val priceChange24h: Double = 0.0,
    val weight: Double = 1.0,
    val premium: Double = 0.0,
    val isCustom: Boolean = false,
    val baseSymbol: String = "",
    val decimalPreference: Int = 8,
    val officialSpotPrice: Double = 0.0,
    val officialSpotTimestamp: Long = 0L
)