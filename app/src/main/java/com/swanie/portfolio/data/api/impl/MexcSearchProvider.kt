package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.MexcApiService
import com.swanie.portfolio.data.network.MexcSymbol
import com.swanie.portfolio.data.network.MexcTicker24h
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MexcSearchProvider @Inject constructor(
    private val api: MexcApiService
) : SearchProvider {
    override val name: String = "MEXC"

    private var lastBulkPriceHit = 0L
    private var cachedSymbols: List<MexcSymbol>? = null

    private suspend fun getSymbols(): List<MexcSymbol> {
        return cachedSymbols ?: try {
            Log.d("MEXC_SEARCH", "Loading exchangeInfo (first fetch)")
            val info = api.getExchangeInfo()
            cachedSymbols = info.symbols
            info.symbols
        } catch (e: Exception) {
            Log.e("MEXC_SEARCH", "exchangeInfo failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun search(query: String): List<SearchResult> {
        Log.d("MEXC_SEARCH", "Searching for [$query]")
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
                                id = "MX_${symbolInfo.symbol}",
                                symbol = symbol,
                                name = "$symbol (MEXC)",
                                imageUrl = iconUrl,
                                category = AssetCategory.CRYPTO,
                                priceSource = name
                            )
                        }
                }
            }
        } catch (e: Exception) {
            Log.e("MEXC_SEARCH", "Search failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val currentTime = System.currentTimeMillis()
        val pairs = ids.split(",").filter { it.isNotBlank() }
        val isBulk = pairs.size > 1

        if (isBulk && currentTime - lastBulkPriceHit < 10000) {
            Log.w("MEXC_PRICE", "Bulk refresh cooldown active")
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
                    Log.e("MEXC_PRICE", "HTTP ${response.code()} for $cleanTicker")
                }
            } catch (e: Exception) {
                Log.e("MEXC_PRICE", "Failed for $cleanTicker: ${e.message}")
            }
        }
        return results
    }

    /** Accept MX_ATLAUSDT, ATLAUSDT, or ATLA from legacy rows. */
    private fun normalizePairSymbol(raw: String): String {
        val trimmed = raw.trim().removePrefix("MX_")
        return when {
            trimmed.endsWith("USDT", ignoreCase = true) -> trimmed.uppercase()
            trimmed.isNotBlank() -> "${trimmed.uppercase()}USDT"
            else -> ""
        }
    }

    suspend fun fetchSparkline(symbol: String): List<Double> {
        return try {
            // MEXC kline enum uses 60m (not Binance 1h).
            val klines = api.getKlines(symbol, "60m", 168)
            if (klines.isEmpty()) {
                val fallback = api.getKlines(symbol, "4h", 42)
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
        val baseSymbol = ticker.symbol.replace("USDT", "", ignoreCase = true)
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
