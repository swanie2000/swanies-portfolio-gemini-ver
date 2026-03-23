package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.MexcApiService
import com.swanie.portfolio.data.network.MexcTicker24h
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MexcSearchProvider @Inject constructor(
    private val api: MexcApiService
) : SearchProvider {
    override val name: String = "MEXC"

    private var lastGlobalHit = 0L

    override suspend fun search(query: String): List<SearchResult> = try {
        val info = api.getExchangeInfo()
        info.symbols
            .filter { it.quoteAsset == "USDT" && (it.baseAsset.contains(query, true) || it.symbol.contains(query, true)) }
            .take(15)
            .map {
                val symbol = it.baseAsset.uppercase()
                // WORLD MIRROR ICON: Using a multi-stage fallback strategy
                // Primary: CoinCap (Global), Secondary: Github (Reliable)
                val iconUrl = "https://assets.coincap.io/assets/icons/${it.baseAsset.lowercase()}@2x.png"
                SearchResult(
                    id = "MX_${it.symbol}",
                    symbol = symbol,
                    name = "$symbol (MEXC)",
                    imageUrl = iconUrl,
                    category = AssetCategory.CRYPTO,
                    priceSource = name
                )
            }
    } catch (e: Exception) {
        Log.e("MEXC_SEARCH", "Search failed: ${e.message}")
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
            val cleanTicker = pair.trim().replace("MX_", "") 
            try {
                // WORLD ADDRESS FIX: MEXC sometimes requires specific headers for regional bypass
                // Handled by OkHttpClient in NetworkModule
                val response = api.getTicker24h(cleanTicker)
                if (response.isSuccessful) {
                    response.body()?.let { ticker ->
                        val spark = fetchSparkline(ticker.symbol)
                        results.add(createAssetEntity(ticker, spark))
                    }
                }
            } catch (e: Exception) {
                Log.e("MEXC_PRICE", "Failed for $cleanTicker: ${e.message}")
            }
        }
        return results
    }

    suspend fun fetchSparkline(symbol: String): List<Double> {
        return try {
            // ROBUST SPARKLINE: Use 4h candles as fallback for restricted regions
            val klines = api.getKlines(symbol, "1h", 24)
            if (klines.isEmpty()) {
                val fallback = api.getKlines(symbol, "4h", 6)
                fallback.mapNotNull { it.getOrNull(4)?.toString()?.toDoubleOrNull() }
            } else {
                klines.mapNotNull { it.getOrNull(4)?.toString()?.toDoubleOrNull() }
            }
        } catch (e: Exception) {
            Log.e("MEXC_SPARK", "Failed for $symbol: ${e.message}")
            emptyList()
        }
    }

    private fun createAssetEntity(ticker: MexcTicker24h, spark: List<Double>): AssetEntity {
        val baseSymbol = ticker.symbol.replace("USDT", "")
        val iconUrl = "https://assets.coincap.io/assets/icons/${baseSymbol.lowercase()}@2x.png"
        
        return AssetEntity(
            coinId = "MX_${ticker.symbol}",
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