package com.swanie.portfolio.data.api

import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity

/**
 * Common data model for search results to decouple UI/ViewModel from API-specific models.
 */
data class SearchSymbol(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val category: AssetCategory
) {
    fun toAssetEntity(): AssetEntity = AssetEntity(
        coinId = id,
        symbol = symbol,
        name = name,
        imageUrl = imageUrl,
        category = category,
        baseSymbol = symbol
    )
}

/**
 * Interface for pluggable search engines (CoinGecko, Yahoo, MEXC, etc.)
 */
interface SearchProvider {
    val name: String
    suspend fun search(query: String): List<SearchSymbol>
}
