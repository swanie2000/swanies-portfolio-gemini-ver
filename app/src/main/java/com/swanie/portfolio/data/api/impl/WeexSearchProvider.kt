package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.WeexApiService
import com.swanie.portfolio.data.network.WeexSymbol
import com.swanie.portfolio.data.network.WeexTicker24h
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeexSearchProvider @Inject constructor(
    private val api: WeexApiService
) : SearchProvider {
    override val name: String = "WEEX"

    private var lastBulkPriceHit = 0L
    private var cachedSymbols: List<WeexSymbol>? = null

    private suspend fun getSymbols(): List<WeexSymbol> {
        return cachedSymbols ?: try {
            Log.d("WEEX_SEARCH", "Loading exchangeInfo (first fetch)")
            val info = api.getExchangeInfo()
            cachedSymbols = info.symbols
            info.symbols
        } catch (e: Exception) {
            Log.e("WEEX_SEARCH", "exchangeInfo failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun search(query: String): List<SearchResult> {
        Log.d("WEEX_SEARCH", "Searching for [$query]")
        return try {
            withTimeout(8000L) {
                val allSymbols = getSymbols()
                withContext(Dispatchers.Default) {
                    allSymbols
                        .filter {
                            it.quoteAsset == "USDT" &&
                                (it.baseAsset.contains(query, ignoreCase = true) ||
                                    it.symbol.contains(query, ignoreCase = true))
                        }
                        .take(15)
                        .map { symbolInfo ->
                            val symbol = symbolInfo.baseAsset.uppercase()
                            val iconUrl =
                                "https://assets.coincap.io/assets/icons/${symbolInfo.baseAsset.lowercase()}@2x.png"
                            SearchResult(
                                id = "WX_${symbolInfo.symbol}",
                                symbol = symbol,
                                name = "$symbol (WEEX)",
                                imageUrl = iconUrl,
                                category = AssetCategory.CRYPTO,
                                priceSource = name
                            )
                        }
                }
            }
        } catch (e: Exception) {
            Log.e("WEEX_SEARCH", "Search failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val currentTime = System.currentTimeMillis()
        val pairs = ids.split(",").filter { it.isNotBlank() }
        val isBulk = pairs.size > 1

        if (isBulk && currentTime - lastBulkPriceHit < 10000) {
            Log.w("WEEX_PRICE", "Bulk refresh cooldown active")
            return emptyList()
        }
        if (isBulk) lastBulkPriceHit = currentTime

        val results = mutableListOf<AssetEntity>()

        for (pair in pairs) {
            val cleanTicker = normalizePairSymbol(pair)
            if (cleanTicker.isBlank()) continue
            try {
                val response = api.getTicker24h(cleanTicker)
                if (response.isSuccessful) {
                    response.body()?.let { ticker ->
                        val spark = fetchSparkline(ticker.symbol)
                        results.add(createAssetEntity(ticker, spark))
                    }
                } else {
                    Log.e("WEEX_PRICE", "HTTP ${response.code()} for $cleanTicker")
                }
            } catch (e: Exception) {
                Log.e("WEEX_PRICE", "Failed for $cleanTicker: ${e.message}")
            }
        }
        return results
    }

    /** Accept WX_BTCUSDT, BTCUSDT, or BTC from legacy rows. */
    private fun normalizePairSymbol(raw: String): String {
        val trimmed = raw.trim().removePrefix("WX_")
        return when {
            trimmed.endsWith("USDT", ignoreCase = true) -> trimmed.uppercase()
            trimmed.isNotBlank() -> "${trimmed.uppercase()}USDT"
            else -> ""
        }
    }

    suspend fun fetchSparkline(symbol: String): List<Double> {
        return try {
            val klines = api.getKlines(symbol, "1h", 168)
            if (klines.isEmpty()) {
                val fallback = api.getKlines(symbol, "4h", 42)
                fallback.mapNotNull { it.getOrNull(4)?.toString()?.toDoubleOrNull() }
            } else {
                klines.mapNotNull { it.getOrNull(4)?.toString()?.toDoubleOrNull() }
            }
        } catch (e: Exception) {
            Log.e("WEEX_SPARK", "Failed for $symbol: ${e.message}")
            emptyList()
        }
    }

    private fun createAssetEntity(ticker: WeexTicker24h, spark: List<Double>): AssetEntity {
        val baseSymbol = ticker.symbol.replace("USDT", "", ignoreCase = true)
        val iconUrl = "https://assets.coincap.io/assets/icons/${baseSymbol.lowercase()}@2x.png"

        return AssetEntity(
            coinId = "WX_${ticker.symbol}",
            symbol = baseSymbol,
            name = baseSymbol,
            imageUrl = iconUrl,
            category = AssetCategory.CRYPTO,
            officialSpotPrice = ticker.lastPrice.toDoubleOrNull() ?: 0.0,
            priceChange24h = parsePriceChangePercent(ticker.priceChangePercent),
            sparklineData = spark,
            baseSymbol = baseSymbol,
            apiId = ticker.symbol,
            priceSource = name,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /** WEEX v3 returns a decimal ratio (e.g. 0.0219 = +2.19%); UI expects percent points. */
    private fun parsePriceChangePercent(raw: String): Double {
        val trimmed = raw.replace("%", "").trim()
        val value = trimmed.toDoubleOrNull() ?: return 0.0
        return if (raw.contains("%")) value else value * 100.0
    }
}
