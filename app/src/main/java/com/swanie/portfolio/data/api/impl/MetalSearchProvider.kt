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
        for ((symbol, ticker) in metalTickers) {
            try {
                delay(500)
                val response = yahooApiService.getTickerData(ticker)
                val result = response.chart.result?.firstOrNull()
                val meta = result?.meta
                val closePrices = result?.indicators?.quote?.firstOrNull()?.close?.filterNotNull() ?: emptyList()

                if (meta?.regularMarketPrice != null) {
                    results.add(AssetEntity(
                        coinId = symbol, 
                        symbol = symbol, 
                        name = symbol, 
                        category = AssetCategory.METAL, 
                        officialSpotPrice = meta.regularMarketPrice, // ALIGNED V6
                        priceChange24h = meta.regularMarketChangePercent ?: 0.0, 
                        sparklineData = closePrices, 
                        baseSymbol = symbol, 
                        priceSource = name
                    ))
                }
            } catch (e: Exception) { Log.e("MetalSearchProvider", "Failed: ${e.message}") }
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

            MarketPriceData(
                officialSpotPrice = meta?.regularMarketPrice ?: 0.0, // ALIGNED V6
                dayHigh = meta?.regularMarketDayHigh ?: 0.0,
                dayLow = meta?.regularMarketDayLow ?: 0.0,
                changePercent = meta?.regularMarketChangePercent ?: 0.0,
                sparkline = closePrices
            )
        } catch (e: Exception) { MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList()) }
    }
}