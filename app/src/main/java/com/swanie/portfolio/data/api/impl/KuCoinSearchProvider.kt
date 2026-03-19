package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.KuCoinApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KuCoinSearchProvider @Inject constructor(
    private val kuCoinApiService: KuCoinApiService
) : SearchProvider {
    override val name: String = "KuCoin"

    // THE CACHE: Store currency metadata (icons) to avoid redundant global fetches.
    private var iconCache: Map<String, String?>? = null

    private suspend fun getIconMap(): Map<String, String?> {
        return iconCache ?: try {
            Log.d("DIAGNOSTIC", "KuCoin Calling: currencies")
            val response = kuCoinApiService.getCurrencies()
            val map = response.data?.associate { it.currency to it.iconUrl } ?: emptyMap()
            iconCache = map
            map
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "KuCoin Icon Fetch Failed: ${e.message}")
            emptyMap()
        }
    }

    override suspend fun search(query: String): List<SearchResult> {
        Log.d("SEARCH_TRACE", "SEARCH: Querying [$query] via provider [$name]")
        return try {
            val iconMap = getIconMap()
            Log.d("DIAGNOSTIC", "KuCoin Calling: symbols")
            val symbolsResponse = kuCoinApiService.getSymbols()
            val symbols = symbolsResponse.data ?: emptyList()

            symbols
                .filter { it.quoteCurrency == "USDT" && (it.baseCurrency.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true)) }
                .take(20)
                .map { symbol ->
                    val iconUrl = iconMap[symbol.baseCurrency] ?: "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${symbol.baseCurrency.lowercase()}.png"
                    SearchResult(
                        id = symbol.symbol, // THE HYPHENATED VERSION (e.g. RAY-USDT)
                        symbol = symbol.baseCurrency,
                        name = "${symbol.baseCurrency} (KuCoin)",
                        imageUrl = iconUrl,
                        category = AssetCategory.CRYPTO,
                        priceSource = name
                    )
                }
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "KuCoin Search Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        return try {
            val symbols = ids.split(",")
            val iconMap = getIconMap()

            if (symbols.size == 1) {
                // AUTO-FIX: Ensure symbol is hyphenated (e.g., RAYUSDT -> RAY-USDT)
                val rawId = symbols[0]
                val formattedId = if (rawId.contains("-")) {
                    rawId
                } else if (rawId.endsWith("USDT")) {
                    rawId.replace("USDT", "-USDT")
                } else {
                    "${rawId}-USDT"
                }

                Log.d("DIAGNOSTIC", "KuCoin Calling: ticker for $formattedId")
                val response = kuCoinApiService.getTicker(formattedId)
                val ticker = response.data
                if (ticker != null) {
                    // PASSING IDENTITY: If ticker symbol is null, use our formattedId to preserve name
                    listOf(createAssetEntity(ticker, iconMap, fallbackSymbol = formattedId))
                } else {
                    Log.w("DIAGNOSTIC", "KuCoin: No ticker found for $formattedId")
                    emptyList()
                }
            } else {
                Log.d("DIAGNOSTIC", "KuCoin Calling: allTickers")
                val response = kuCoinApiService.getAllTickers()
                val allTickers = response.data?.ticker ?: emptyList()
                val symbolSet = symbols.toSet()
                
                allTickers.filter { ticker ->
                    ticker.symbol != null && (symbolSet.contains(ticker.symbol) || (ticker.symbol != null && symbolSet.contains(ticker.symbol!!.replace("-", ""))))
                }.map { createAssetEntity(it, iconMap) }
            }
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "KuCoin Price Error: ${e.message}")
            emptyList()
        }
    }

    /**
     * KuCoin Sparkline fetcher.
     * Maps their candlestick data to a closing price list.
     * Data format: [time, open, close, high, low, volume, amount]
     */
    suspend fun fetchSparkline(symbol: String): List<Double> {
        val fullPair = if (symbol.contains("-")) {
            symbol
        } else if (symbol.endsWith("USDT")) {
            symbol.replace("USDT", "-USDT")
        } else {
            "${symbol}-USDT"
        }

        Log.d("DIAGNOSTIC", "KuCoin Calling: klines for $fullPair")
        return try {
            val response = kuCoinApiService.getKlines(fullPair, "1hour")
            val candles = response.data ?: emptyList()
            // Index 2 is Close.
            val points = candles.mapNotNull { it.getOrNull(2)?.toDoubleOrNull() }.reversed()
            Log.d("DIAGNOSTIC", "KuCoin Sparkline Points: ${points.size}")
            points
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "KuCoin Sparkline Error for $symbol: ${e.message}")
            emptyList()
        }
    }

    private fun createAssetEntity(
        ticker: com.swanie.portfolio.data.network.KuCoinTicker, 
        iconMap: Map<String, String?>,
        fallbackSymbol: String? = null
    ): AssetEntity {
        // IDENTITY RECOVERY: Priority to ticker.symbol, then fallbackSymbol
        val rawSymbol = ticker.symbol ?: fallbackSymbol ?: "UNKNOWN-USDT"
        
        val baseSymbol = if (rawSymbol.contains("-")) {
            rawSymbol.split("-").first()
        } else if (rawSymbol.endsWith("USDT")) {
            rawSymbol.replace("USDT", "")
        } else {
            rawSymbol
        }

        // Standardized lowercase icons for GitHub repo
        val iconUrl = iconMap[baseSymbol] ?: "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${baseSymbol.lowercase()}.png"
        
        Log.d("DIAGNOSTIC", "KuCoin: SUCCESS mapped $baseSymbol (Native Icon: ${iconMap[baseSymbol] != null})")

        return AssetEntity(
            coinId = rawSymbol,
            symbol = baseSymbol,
            name = baseSymbol,
            imageUrl = iconUrl,
            category = AssetCategory.CRYPTO,
            currentPrice = ticker.last?.toDoubleOrNull() ?: 0.0,
            priceChange24h = (ticker.changeRate?.toDoubleOrNull() ?: 0.0) * 100,
            sparklineData = emptyList(),
            baseSymbol = baseSymbol,
            apiId = rawSymbol,
            iconUrl = iconUrl,
            priceSource = name
        )
    }
}
