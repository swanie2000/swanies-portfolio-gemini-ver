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
    val currentPrice: Double,
    val change24h: Double,
    val displayOrder: Int
)
