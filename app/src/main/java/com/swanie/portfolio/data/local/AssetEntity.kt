package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey
    val coinId: String,
    val symbol: String,
    val name: String,
    val amountHeld: Double,
    val currentPrice: Double,    // Price per unit
    val change24h: Double,
    val displayOrder: Int,
    val lastUpdated: Long        // Epoch timestamp of the last price update
)
