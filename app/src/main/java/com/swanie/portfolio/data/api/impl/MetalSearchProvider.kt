package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.YahooFinanceApiService
import kotlinx.coroutines.delay
import javax.inject.Inject

class MetalSearchProvider @Inject constructor(
    private val yahooApiService: YahooFinanceApiService
) : SearchProvider {
    override val name: String = "YahooFinance"

    private val metalTickers = mapOf(
        "XAU" to "GC=F", "XAG" to "SI=F", "XPT" to "PL=F", "XPD" to "PA=F"
    )

    override suspend fun search(query: String): List<SearchResult> {
        // Simple filter-based search for metals based on tickers
        return metalTickers.keys
            .filter { it.contains(query, ignoreCase = true) }
            .map { symbol ->
                SearchResult(
                    id = symbol,
                    symbol = symbol,
                    name = when(symbol) {
                        "XAU" -> "Gold"
                        "XAG" -> "Silver"
                        "XPT" -> "Platinum"
                        "XPD" -> "Palladium"
                        else -> symbol
                    },
                    imageUrl = "",
                    category = AssetCategory.METAL,
                    priceSource = name
                )
            }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val results = mutableListOf<AssetEntity>()
        for ((symbol, ticker) in metalTickers) {
            try {
                Log.d("API_TRACE", "API_HIT: MetalSearchProvider -> Calling Yahoo for $symbol")
                // Safety Delay to mitigate Rate Limits
                delay(500)
                val response = yahooApiService.getTickerData(ticker)
                val result = response.chart.result?.firstOrNull()
                val meta = result?.meta
                val closePrices = result?.indicators?.quote?.firstOrNull()?.close?.filterNotNull() ?: emptyList()

                if (meta?.regularMarketPrice != null) {
                    results.add(
                        AssetEntity(
                            coinId = symbol, // Using symbol as ID for metals
                            symbol = symbol,
                            name = symbol,
                            imageUrl = "",
                            category = AssetCategory.METAL,
                            currentPrice = meta.regularMarketPrice,
                            priceChange24h = meta.regularMarketChangePercent ?: 0.0,
                            sparklineData = closePrices,
                            baseSymbol = symbol,
                            officialSpotPrice = meta.regularMarketPrice,
                            priceSource = name
                            // Note: High/Low are currently not stored in AssetEntity, 
                            // they are fetched on-demand for the Audit Screen.
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("MetalSearchProvider", "Fetch failed for $symbol: ${e.message}")
            }
        }
        return results
    }

    /**
     * Internal helper to fetch full market data including high/low.
     */
    suspend fun fetchMarketData(symbol: String): com.swanie.portfolio.data.repository.MarketPriceData {
        val ticker = metalTickers[symbol] ?: return com.swanie.portfolio.data.repository.MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
        return try {
            Log.d("API_TRACE", "API_HIT: MetalSearchProvider -> Calling Yahoo for $symbol (Full Data)")
            val response = yahooApiService.getTickerData(ticker)
            val result = response.chart.result?.firstOrNull()
            val meta = result?.meta
            val closePrices = result?.indicators?.quote?.firstOrNull()?.close?.filterNotNull() ?: emptyList()

            com.swanie.portfolio.data.repository.MarketPriceData(
                current = meta?.regularMarketPrice ?: 0.0,
                dayHigh = meta?.regularMarketDayHigh ?: 0.0,
                dayLow = meta?.regularMarketDayLow ?: 0.0,
                changePercent = meta?.regularMarketChangePercent ?: 0.0,
                sparkline = closePrices
            )
        } catch (e: Exception) {
            Log.e("MetalSearchProvider", "fetchMarketData failed for $symbol: ${e.message}")
            com.swanie.portfolio.data.repository.MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
        }
    }
}
