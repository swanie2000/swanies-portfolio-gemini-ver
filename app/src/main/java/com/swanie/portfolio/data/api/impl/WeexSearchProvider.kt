package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.WeexApiService
import com.swanie.portfolio.data.network.WeexTicker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeexSearchProvider @Inject constructor(
    private val api: WeexApiService
) : SearchProvider {
    override val name: String = "WEEX"

    private var lastGlobalHit = 0L
    private var symbolCache: List<SearchResult> = emptyList()

    override suspend fun search(query: String): List<SearchResult> = try {
        if (symbolCache.isEmpty()) {
            val response = api.getExchangeInfo()
            if (response.code == "00000") {
                symbolCache = response.data.map {
                    val base = it.baseAsset.uppercase()
                    SearchResult(
                        id = "WX_${it.symbol}",
                        symbol = base,
                        name = "$base (WEEX)",
                        imageUrl = "https://assets.coincap.io/assets/icons/${base.lowercase()}@2x.png",
                        category = AssetCategory.CRYPTO,
                        priceSource = name
                    )
                }
            }
        }
        symbolCache.filter { it.symbol.contains(query, true) || it.name.contains(query, true) }.take(15)
    } catch (e: Exception) {
        Log.e("WEEX_SEARCH", "Search failed: ${e.message}")
        emptyList()
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val currentTime = System.currentTimeMillis()
        val pairs = ids.split(",")
        val isBulk = pairs.size > 1
        
        if (isBulk && currentTime - lastGlobalHit < 10000) return emptyList()
        if (isBulk) lastGlobalHit = currentTime

        val results = mutableListOf<AssetEntity>()

        for (pair in pairs) {
            if (pair.isBlank()) continue
            val cleanPair = pair.trim().replace("WX_", "") 
            try {
                val response = api.getTicker24h(cleanPair)
                if (response.code == "00000") {
                    val ticker = response.data
                    val sparkResponse = api.getKlines(cleanPair)
                    val spark = if (sparkResponse.code == "00000") {
                        sparkResponse.data.mapNotNull { it.getOrNull(4)?.toDoubleOrNull() }
                    } else emptyList()
                    
                    results.add(createAssetEntity(ticker, spark))
                }
            } catch (e: Exception) {
                Log.e("WEEX_PRICE", "Failed for $pair: ${e.message}")
            }
        }
        return results
    }

    private fun createAssetEntity(ticker: com.swanie.portfolio.data.network.WeexTicker, spark: List<Double>): AssetEntity {
        val baseSymbol = ticker.symbol.replace("USDT", "")
        val iconUrl = "https://assets.coincap.io/assets/icons/${baseSymbol.lowercase()}@2x.png"
        
        return AssetEntity(
            coinId = "WX_${ticker.symbol}",
            symbol = baseSymbol,
            name = baseSymbol,
            imageUrl = iconUrl,
            category = AssetCategory.CRYPTO,
            officialSpotPrice = ticker.lastPrice.toDoubleOrNull() ?: 0.0,
            priceChange24h = ticker.priceChangePercent.replace("%", "").toDoubleOrNull() ?: 0.0,
            sparklineData = spark,
            baseSymbol = baseSymbol,
            apiId = ticker.symbol,
            priceSource = name,
            lastUpdated = System.currentTimeMillis()
        )
    }
}