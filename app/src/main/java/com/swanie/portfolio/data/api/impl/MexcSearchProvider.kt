package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.MexcApiService
import retrofit2.HttpException
import javax.inject.Inject

class MexcSearchProvider @Inject constructor(
    private val mexcApiService: MexcApiService
) : SearchProvider {
    override val name: String = "MEXC"

    override suspend fun search(query: String): List<SearchResult> {
        Log.d("SEARCH_TRACE", "SEARCH: Querying [$query] via provider [$name]")
        return try {
            val info = mexcApiService.getExchangeInfo()
            // Filter for spot USDT pairs and match query
            info.symbols
                .filter { it.quoteAsset == "USDT" && (it.baseAsset.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true)) }
                .take(20)
                .map { symbol ->
                    SearchResult(
                        id = symbol.symbol,
                        symbol = symbol.baseAsset,
                        name = "${symbol.baseAsset} (MEXC)",
                        imageUrl = "", // MEXC API doesn't provide icons in exchangeInfo
                        category = AssetCategory.CRYPTO,
                        priceSource = name
                    )
                }
        } catch (e: Exception) {
            Log.e("SEARCH_TRACE", "MEXC Search Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        return try {
            val symbols = ids.split(",")
            if (symbols.size == 1) {
                val response = mexcApiService.getTicker24h(symbols[0])
                if (response.isSuccessful) {
                    val ticker = response.body()
                    if (ticker != null) {
                        listOf(createAssetEntity(ticker))
                    } else emptyList()
                } else emptyList()
            } else {
                // Bulk refresh logic
                val response = mexcApiService.getTickers24h()
                if (response.isSuccessful) {
                    val allTickers = response.body() ?: emptyList()
                    val symbolSet = symbols.toSet()
                    allTickers.filter { symbolSet.contains(it.symbol) }
                        .map { createAssetEntity(it) }
                } else emptyList()
            }
        } catch (e: Exception) {
            Log.e("ADD_TRACE", "MEXC Price Error: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches K-line data from MEXC and maps it to a sparkline list.
     * Ensures the symbol is mapped to the full pair (e.g., ATLA becomes ATLAUSDT).
     * Includes diagnostic logging, interval fallback, and 400 error retry.
     */
    suspend fun fetchSparkline(symbol: String): List<Double> {
        Log.d("ADD_TRACE", "PROVIDER_FETCH: Fetching sparkline for $symbol...")
        val fullPair = if (symbol.endsWith("USDT")) symbol else "${symbol}USDT"
        Log.d("ADD_TRACE", "MEXC_URL: Fetching K-Lines for $fullPair")

        return try {
            fetchKlinesWithFallback(fullPair, 168)
        } catch (e: Exception) {
            Log.e("ADD_TRACE", "MEXC Sparkline Error for $symbol: ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchKlinesWithFallback(pair: String, limit: Int): List<Double> {
        return try {
            val klines = mexcApiService.getKlines(pair, "1h", limit)
            Log.d("ADD_TRACE", "MEXC_PAYLOAD (1h): Received ${klines.size} points for $pair.")
            
            if (klines.isEmpty() && limit == 168) {
                Log.w("ADD_TRACE", "MEXC_FALLBACK: 1h payload empty. Retrying with 4h interval...")
                val klines4h = mexcApiService.getKlines(pair, "4h", 42)
                return klines4h.mapNotNull { it.getOrNull(4)?.toString()?.toDoubleOrNull() }
            }
            
            klines.mapNotNull { it.getOrNull(4)?.toString()?.toDoubleOrNull() }
        } catch (e: HttpException) {
            if (e.code() == 400 && limit > 48) {
                Log.w("ADD_TRACE", "MEXC_RETRY: 400 Error for $pair. Falling back to 48h limit.")
                return fetchKlinesWithFallback(pair, 48)
            }
            Log.e("ADD_TRACE", "MEXC HTTP Error: ${e.code()} - ${e.message()}")
            emptyList()
        } catch (e: Exception) {
            Log.e("ADD_TRACE", "MEXC Fetch Error: ${e.message}")
            emptyList()
        }
    }

    private fun createAssetEntity(ticker: com.swanie.portfolio.data.network.MexcTicker24h): AssetEntity {
        return AssetEntity(
            coinId = ticker.symbol,
            symbol = ticker.symbol.replace("USDT", ""),
            name = ticker.symbol.replace("USDT", ""),
            imageUrl = "",
            category = AssetCategory.CRYPTO,
            currentPrice = ticker.lastPrice.toDoubleOrNull() ?: 0.0,
            priceChange24h = ticker.priceChangePercent.replace("%", "").toDoubleOrNull() ?: 0.0,
            sparklineData = emptyList(),
            baseSymbol = ticker.symbol.replace("USDT", ""),
            apiId = ticker.symbol,
            iconUrl = "",
            priceSource = name
        )
    }
}
