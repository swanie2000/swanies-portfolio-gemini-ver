package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val coinId: String,
    val symbol: String,
    val name: String,
    val imageUrl: String = "",
    val category: AssetCategory,
    val officialSpotPrice: Double = 0.0, // ALIGNED V6
    val priceChange24h: Double = 0.0,
    val sparklineData: List<Double> = emptyList(),
    val baseSymbol: String = "",
    val apiId: String = "",
    val iconUrl: String? = null,
    val priceSource: String = "CoinGecko",
    val weight: Double = 1.0,
    val amountHeld: Double = 0.0,
    val premium: Double = 0.0,
    val decimalPreference: Int = 2,
    val displayOrder: Int = 0, // ALIGNED V6
    val lastUpdated: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val showOnWidget: Boolean = false
)

enum class AssetCategory {
    CRYPTO, METAL
}