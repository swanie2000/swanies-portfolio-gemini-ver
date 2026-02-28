package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetCategory {
    CRYPTO,
    METAL
}

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey
    val coinId: String, // e.g., "ripple", "bitcoin"
    val symbol: String,   // e.g., "XRP", "BTC"
    val name: String,
    val amountHeld: Double,
    val currentPrice: Double,
    val change24h: Double,
    val displayOrder: Int,
    val lastUpdated: Long,
    val imageUrl: String,
    val category: AssetCategory = AssetCategory.CRYPTO,
    val sparklineData: List<Double> = emptyList(),
    val marketCapRank: Int = 0,
    val priceChange24h: Double = 0.0,
    val weight: Double = 1.0,
    val premium: Double = 0.0,
    val isCustom: Boolean = false,
    val baseSymbol: String = "",
    val decimalPreference: Int = 2 // New field for decimal display preference
)
