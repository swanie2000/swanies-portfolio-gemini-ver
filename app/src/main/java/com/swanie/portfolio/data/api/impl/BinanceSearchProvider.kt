package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.BinanceApiService
import com.swanie.portfolio.data.network.BinanceSymbol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinanceSearchProvider @Inject constructor(
    private val binanceApiService: BinanceApiService
) : SearchProvider {
    override val name: String = "Binance"

    // CACHE: exchangeInfo is massive (MBs). Fetch once.
    private var cachedSymbols: List<BinanceSymbol>? = null

    private suspend fun getSymbols(): List<BinanceSymbol> {
        return cachedSymbols ?: try {
            Log.d("DIAGNOSTIC", "Binance Calling: exchangeInfo (Initial Load)")
            val info = binanceApiService.getExchangeInfo()
            cachedSymbols = info.symbols
            info.symbols
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "Binance exchangeInfo Fetch Failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun search(query: String): List<SearchResult> {
        Log.d("SEARCH_TRACE", "SEARCH: Querying [$query] via provider [$name]")
        return try {
            // PREVENTION: 5s timeout to prevent UI hang
            withTimeout(5000L) {
                val allSymbols = getSymbols()
                
                // PARSING: Move filter/map to IO thread to prevent UI stutter on large lists
                withContext(Dispatchers.IO) {
                    allSymbols
                        .filter { it.quoteAsset == "USDT" && (it.baseAsset.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true)) }
                        .take(20)
                        .map { symbol ->
                            val iconUrl = "https://bin.bnbstatic.com/static/assets/logos/${symbol.baseAsset.uppercase()}.png"
                            SearchResult(
                                id = symbol.symbol,
                                symbol = symbol.baseAsset,
                                name = "${symbol.baseAsset} (Binance)",
                                imageUrl = iconUrl,
                                category = AssetCategory.CRYPTO,
                                priceSource = name
                            )
                        }
                }
            }
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "Binance Search Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        return try {
            val symbols = ids.split(",")
            if (symbols.size == 1) {
                val fullPair = if (symbols[0].endsWith("USDT")) symbols[0] else "${symbols[0]}USDT"
                Log.d("DIAGNOSTIC", "Binance Calling: ticker for $fullPair")
                val response = binanceApiService.getTicker24h(fullPair)
                if (response.isSuccessful) {
                    val ticker = response.body()
                    if (ticker != null) {
                        listOf(createAssetEntity(ticker))
                    } else emptyList()
                } else {
                    Log.e("DIAGNOSTIC", "Binance Ticker Error: " + (response.errorBody()?.string() ?: "Unknown error"))
                    emptyList()
                }
            } else {
                Log.d("DIAGNOSTIC", "Binance Calling: allTickers")
                val response = binanceApiService.getTickers24h()
                if (response.isSuccessful) {
                    val allTickers = response.body() ?: emptyList()
                    val symbolSet = symbols.toSet()
                    allTickers.filter { symbolSet.contains(it.symbol) }
                        .map { createAssetEntity(it) }
                } else {
                    Log.e("DIAGNOSTIC", "Binance Bulk Tickers Error: " + (response.errorBody()?.string() ?: "Unknown error"))
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "Binance Price Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchSparkline(symbol: String): List<Double> {
        val fullPair = if (symbol.endsWith("USDT")) symbol else "${symbol}USDT"
        Log.d("DIAGNOSTIC", "Binance Calling: klines for $fullPair")
        return try {
            val klines = binanceApiService.getKlines(fullPair, "1h", 168)
            klines.mapNotNull { it.getOrNull(4)?.toString()?.toDoubleOrNull() }
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "Binance Sparkline Error for $symbol: ${e.message}")
            emptyList()
        }
    }

    private fun createAssetEntity(ticker: com.swanie.portfolio.data.network.BinanceTicker): AssetEntity {
        val baseSymbol = ticker.symbol.replace("USDT", "")
        val iconUrl = "https://bin.bnbstatic.com/static/assets/logos/${baseSymbol.uppercase()}.png"
        return AssetEntity(
            coinId = ticker.symbol,
            symbol = baseSymbol,
            name = baseSymbol,
            imageUrl = iconUrl,
            category = AssetCategory.CRYPTO,
            officialSpotPrice = ticker.lastPrice.toDoubleOrNull() ?: 0.0, // ALIGNED V6
            priceChange24h = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0,
            sparklineData = emptyList(),
            baseSymbol = baseSymbol,
            apiId = ticker.symbol,
            iconUrl = iconUrl,
            priceSource = name
        )
    }
}