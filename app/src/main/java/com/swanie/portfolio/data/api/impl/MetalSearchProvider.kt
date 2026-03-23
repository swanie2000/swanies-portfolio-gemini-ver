package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.YahooFinanceApiService
import com.swanie.portfolio.data.repository.MarketPriceData
import kotlinx.coroutines.delay
import javax.inject.Inject

class MetalSearchProvider @Inject constructor(
    private val yahooApiService: YahooFinanceApiService
) : SearchProvider {
    override val name: String = "YahooFinance"

    private val metalTickers = mapOf("XAU" to "GC=F", "XAG" to "SI=F", "XPT" to "PL=F", "XPD" to "PA=F")

    override suspend fun search(query: String): List<SearchResult> = metalTickers.keys
        .filter { it.contains(query, ignoreCase = true) }
        .map { symbol -> SearchResult(id = symbol, symbol = symbol, name = symbol, imageUrl = "", category = AssetCategory.METAL, priceSource = name) }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val results = mutableListOf<AssetEntity>()
        val requestedSymbols = ids.split(",")
        
        for (symbol in requestedSymbols) {
            if (symbol.isBlank()) continue
            val cleanSymbol = symbol.trim().uppercase()
            val ticker = metalTickers[cleanSymbol] ?: continue
            try {
                delay(300)
                val response = yahooApiService.getTickerData(ticker)
                val result = response.chart.result?.firstOrNull()
                val meta = result?.meta
                val closePrices = result?.indicators?.quote?.firstOrNull()?.close?.filterNotNull() ?: emptyList()

                if (meta?.regularMarketPrice != null) {
                    // 🛠️ DERIVED TREND CALCULATION:
                    // If regularMarketChangePercent is null, calculate it from chartPreviousClose
                    val current = meta.regularMarketPrice
                    val previous = meta.chartPreviousClose ?: current
                    val derivedChange = if (previous != 0.0) ((current - previous) / previous) * 100.0 else 0.0
                    val finalChange = meta.regularMarketChangePercent ?: derivedChange

                    results.add(AssetEntity(
                        coinId = cleanSymbol, 
                        symbol = cleanSymbol, 
                        name = cleanSymbol, 
                        category = AssetCategory.METAL, 
                        officialSpotPrice = current,
                        priceChange24h = finalChange,
                        sparklineData = closePrices, 
                        baseSymbol = cleanSymbol, 
                        apiId = cleanSymbol,
                        priceSource = name,
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
            } catch (e: Exception) { Log.e("MetalSearchProvider", "Failed for $symbol: ${e.message}") }
        }
        return results
    }

    suspend fun fetchMarketData(symbol: String): MarketPriceData {
        val ticker = metalTickers[symbol] ?: return MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
        return try {
            val response = yahooApiService.getTickerData(ticker)
            val result = response.chart.result?.firstOrNull()
            val meta = result?.meta
            val closePrices = result?.indicators?.quote?.firstOrNull()?.close?.filterNotNull() ?: emptyList()

            val current = meta?.regularMarketPrice ?: 0.0
            val previous = meta?.chartPreviousClose ?: current
            val derivedChange = if (previous != 0.0) ((current - previous) / previous) * 100.0 else 0.0
            val finalChange = meta?.regularMarketChangePercent ?: derivedChange

            MarketPriceData(
                officialSpotPrice = current,
                dayHigh = meta?.regularMarketDayHigh ?: 0.0,
                dayLow = meta?.regularMarketDayLow ?: 0.0,
                changePercent = finalChange,
                sparkline = closePrices
            )
        } catch (e: Exception) { MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList()) }
    }
}