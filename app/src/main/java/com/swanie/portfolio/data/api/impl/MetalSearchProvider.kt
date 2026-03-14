package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchSymbol
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

    override suspend fun search(query: String): List<SearchSymbol> {
        // Simple filter-based search for metals based on tickers
        return metalTickers.keys
            .filter { it.contains(query, ignoreCase = true) }
            .map { symbol ->
                SearchSymbol(
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
                    category = AssetCategory.METAL
                )
            }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val results = mutableListOf<AssetEntity>()
        for ((symbol, ticker) in metalTickers) {
            try {
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
                            baseSymbol = symbol
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("MetalSearchProvider", "Fetch failed for $symbol: ${e.message}")
            }
        }
        return results
    }
}
