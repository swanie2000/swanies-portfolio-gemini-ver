package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val imageUrl: String // THE FIX: Add image URL field
)
