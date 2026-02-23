package com.swanie.portfolio.data

import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity

object MetalsProvider {

    val preciousMetals = listOf(
        AssetEntity(
            coinId = "gold",
            symbol = "XAU",
            name = "Gold",
            amountHeld = 0.0,
            currentPrice = 2000.0,
            change24h = 0.0,
            displayOrder = 1001,
            lastUpdated = System.currentTimeMillis(),
            imageUrl = "",
            category = AssetCategory.METAL
        ),
        AssetEntity(
            coinId = "silver",
            symbol = "XAG",
            name = "Silver",
            amountHeld = 0.0,
            currentPrice = 25.0,
            change24h = 0.0,
            displayOrder = 1002,
            lastUpdated = System.currentTimeMillis(),
            imageUrl = "",
            category = AssetCategory.METAL
        ),
        AssetEntity(
            coinId = "platinum",
            symbol = "XPT",
            name = "Platinum",
            amountHeld = 0.0,
            currentPrice = 1000.0,
            change24h = 0.0,
            displayOrder = 1003,
            lastUpdated = System.currentTimeMillis(),
            imageUrl = "",
            category = AssetCategory.METAL
        ),
        AssetEntity(
            coinId = "palladium",
            symbol = "XPD",
            name = "Palladium",
            amountHeld = 0.0,
            currentPrice = 1200.0,
            change24h = 0.0,
            displayOrder = 1004,
            lastUpdated = System.currentTimeMillis(),
            imageUrl = "",
            category = AssetCategory.METAL
        )
    )

    fun searchMetals(query: String): List<AssetEntity> {
        if (query.isBlank()) {
            return emptyList()
        }
        val lowerCaseQuery = query.lowercase()
        return preciousMetals.filter {
            it.name.lowercase().contains(lowerCaseQuery) ||
            it.symbol.lowercase().contains(lowerCaseQuery)
        }
    }
}
