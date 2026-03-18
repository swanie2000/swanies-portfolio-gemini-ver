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
                    id = coin.id,
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
        if (currentTime - lastGlobalHit < 5000) {
            Log.w("API_TRACE", "STRAITJACKET: API hit rejected. Less than 5s since last call.")
            return emptyList()
        }
        lastGlobalHit = currentTime

        return try {
            delay(500)
            val response = coinGeckoApiService.getCoinMarkets(ids = ids)
            val url = response.raw().request.url.toString()
            val code = response.code()
            Log.d("ADD_TRACE", "STEP 4: API_HIT: ID=$ids, URL=$url, CODE=$code")

            if (response.isSuccessful) {
                val marketData = response.body() ?: emptyList()
                marketData.map { fresh ->
                    AssetEntity(
                        coinId = fresh.id,
                        symbol = fresh.symbol,
                        name = fresh.name,
                        imageUrl = fresh.image ?: "",
                        category = AssetCategory.CRYPTO,
                        currentPrice = fresh.currentPrice ?: 0.0,
                        priceChange24h = fresh.priceChangePercentage24h ?: 0.0,
                        sparklineData = fresh.sparklineIn7d?.price ?: emptyList(),
                        baseSymbol = fresh.symbol,
                        apiId = fresh.id,
                        iconUrl = fresh.image,
                        priceSource = name
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ADD_TRACE", "STEP 4: API_ERROR: ${e.message}")
            emptyList()
        }
    }
}
