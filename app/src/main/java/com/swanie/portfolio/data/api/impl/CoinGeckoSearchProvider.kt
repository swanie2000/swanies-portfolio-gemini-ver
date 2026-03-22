package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinGeckoApiService
import kotlinx.coroutines.delay
import javax.inject.Inject

class CoinGeckoSearchProvider @Inject constructor(
    private val coinGeckoApiService: CoinGeckoApiService
) : SearchProvider {
    override val name: String = "CoinGecko"

    private var lastGlobalHit = 0L

    override suspend fun search(query: String): List<SearchResult> {
        Log.d("SEARCH_TRACE", "Searching for: $query via $name")
        return try {
            val result = coinGeckoApiService.search(query)
            result.coins.map { coin ->
                SearchResult(
                    id = "CG_${coin.id}", // UNIQUE PREFIX
                    symbol = coin.symbol,
                    name = coin.name,
                    imageUrl = coin.large,
                    category = AssetCategory.CRYPTO,
                    priceSource = name
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastGlobalHit < 10000) {
            Log.w("API_TRACE", "COINGECKO: Rate limit cooldown.")
            return emptyList()
        }
        lastGlobalHit = currentTime

        return try {
            delay(500)
            // Strip CG_ prefix for the API call
            val cleanIds = ids.split(",").joinToString(",") { it.replace("CG_", "") }
            val response = coinGeckoApiService.getCoinMarkets(ids = cleanIds)
            
            if (response.isSuccessful) {
                val marketData = response.body() ?: emptyList()
                marketData.map { fresh ->
                    AssetEntity(
                        coinId = "CG_${fresh.id}", // UNIQUE PK
                        symbol = fresh.symbol,
                        name = fresh.name,
                        imageUrl = fresh.image ?: "",
                        category = AssetCategory.CRYPTO,
                        officialSpotPrice = fresh.currentPrice ?: 0.0,
                        priceChange24h = fresh.priceChangePercentage24h ?: 0.0,
                        sparklineData = fresh.sparklineIn7d?.price ?: emptyList(),
                        baseSymbol = fresh.symbol,
                        apiId = fresh.id, // Raw ID for fetch logic
                        iconUrl = fresh.image,
                        priceSource = name,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}