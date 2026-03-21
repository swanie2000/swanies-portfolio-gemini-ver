package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CryptoCompareApiService
import javax.inject.Inject

class CryptoCompareSearchProvider @Inject constructor(
    private val cryptoCompareApiService: CryptoCompareApiService
) : SearchProvider {
    override val name: String = "CryptoCompare"

    override suspend fun search(query: String): List<SearchResult> {
        Log.d("SEARCH_TRACE", "SEARCH: Querying [$query] via provider [$name]")
        return try {
            val response = cryptoCompareApiService.getAllCoins()
            response.data.values
                .filter { 
                    it.symbol.contains(query, ignoreCase = true) || 
                    it.fullName.contains(query, ignoreCase = true) 
                }
                .take(20)
                .map { coin ->
                    SearchResult(
                        id = coin.symbol,
                        symbol = coin.symbol,
                        name = coin.fullName,
                        imageUrl = "https://www.cryptocompare.com${coin.imageUrl ?: ""}",
                        category = AssetCategory.CRYPTO,
                        priceSource = name
                    )
                }
        } catch (e: Exception) {
            Log.e("SEARCH_TRACE", "CryptoCompare Search Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val results = mutableListOf<AssetEntity>()
        try {
            Log.d("ADD_TRACE", "PROVIDER_FETCH: Fetching price for $ids...")
            val response = cryptoCompareApiService.getPriceFull(fsyms = ids)
            if (response.isSuccessful) {
                val rawData = response.body()?.raw ?: emptyMap()
                for ((symbol, priceMap) in rawData) {
                    val usdData = priceMap["USD"]
                    
                    // SPARKLINE RESTORATION: Fetch history for each symbol
                    // Use a 24-hour window for the sparkline
                    val sparkline = try {
                        fetchSparkline(symbol)
                    } catch (e: Exception) {
                        Log.e("CRYPTO_TRACE", "Sparkline failed for $symbol: ${e.message}")
                        emptyList()
                    }

                    results.add(AssetEntity(
                        coinId = symbol,
                        symbol = symbol,
                        name = symbol,
                        imageUrl = if (usdData?.imageUrl != null) "https://www.cryptocompare.com${usdData.imageUrl}" else "",
                        category = AssetCategory.CRYPTO,
                        officialSpotPrice = usdData?.price ?: 0.0,
                        priceChange24h = usdData?.changePct24h ?: 0.0,
                        sparklineData = sparkline, 
                        baseSymbol = symbol,
                        apiId = symbol,
                        iconUrl = if (usdData?.imageUrl != null) "https://www.cryptocompare.com${usdData.imageUrl}" else "",
                        priceSource = name,
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
            } else {
                Log.e("ADD_TRACE", "CryptoCompare Price Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ADD_TRACE", "CryptoCompare Price Exception: ${e.message}")
        }
        return results
    }

    /**
     * Implementation of sparkline fetch for CryptoCompare with specialized field validation.
     */
    suspend fun fetchSparkline(symbol: String): List<Double> {
        // Ensure symbol is uppercase for the history endpoint
        val cleanSymbol = symbol.uppercase()
        Log.d("CRYPTO_TRACE", "Attempting fetch for $cleanSymbol")
        return try {
            val response = cryptoCompareApiService.getHistoryHour(fsym = cleanSymbol, tsym = "USD", limit = 24)
            
            if (response.isSuccessful) {
                val body = response.body()
                val historyData = body?.data?.data ?: emptyList()
                val sparkline = historyData.map { it.close }
                Log.d("CRYPTO_TRACE", "Successfully fetched ${sparkline.size} points for $cleanSymbol")
                sparkline
            } else {
                Log.e("CRYPTO_TRACE", "HTTP_ERROR: ${response.code()} for $cleanSymbol")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("CRYPTO_TRACE", "EXCEPTION for $cleanSymbol: ${e.message}")
            emptyList()
        }
    }
}