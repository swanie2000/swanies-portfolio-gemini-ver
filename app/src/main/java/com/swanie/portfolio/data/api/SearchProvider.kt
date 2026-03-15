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
        coinId = id, // Primary Key remains the unique ID from provider
        symbol = symbol,
        name = name,
        imageUrl = imageUrl,
        category = category,
        baseSymbol = symbol,
        apiId = id,     // THE DATA CHAIN OF CUSTODY: Explicit API ID
        iconUrl = imageUrl // THE DATA CHAIN OF CUSTODY: Explicit Icon URL
    )
}

/**
 * Interface for pluggable search and price providers.
 */
interface SearchProvider {
    val name: String
    suspend fun search(query: String): List<SearchSymbol>
    suspend fun getPrices(ids: String): List<AssetEntity>
}
