package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinbaseApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinbaseSearchProvider @Inject constructor(
    private val coinbaseApiService: CoinbaseApiService
) : SearchProvider {
    override val name: String = "Coinbase"

    // THE MASTER LIST CACHE
    private var cachedCurrencies: Map<String, String>? = null

    private suspend fun getCurrencyNames(): Map<String, String> {
        if (cachedCurrencies != null) return cachedCurrencies!!
        
        return try {
            Log.d("DIAGNOSTIC", "Coinbase: Fetching master list from Exchange API...")
            val response = coinbaseApiService.getExchangeCurrencies()
            val map = response.associate { it.id to it.name }
            Log.d("DIAGNOSTIC", "Coinbase: Master list loaded with ${map.size} assets.")
            cachedCurrencies = map
            map
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "Coinbase: Failed to fetch master list: ${e.message}")
            emptyMap()
        }
    }

    override suspend fun search(query: String): List<SearchResult> {
        Log.d("SEARCH_TRACE", "SEARCH: Querying [$query] via provider [$name]")
        return try {
            val names = getCurrencyNames()
            val filtered = names.filter { entry -> 
                entry.key.contains(query, ignoreCase = true) || entry.value.contains(query, ignoreCase = true) 
            }
                .toList()
                .take(20)
                .map { (id, fullName) ->
                    val iconUrl = "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${id.lowercase()}.png"
                    SearchResult(
                        id = id,
                        symbol = id,
                        name = "$fullName (Coinbase)",
                        imageUrl = iconUrl,
                        category = AssetCategory.CRYPTO,
                        priceSource = name
                    )
                }
            
            Log.d("DIAGNOSTIC", "Coinbase: SUCCESS found ${filtered.size} assets for query [$query]")
            filtered
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "Coinbase Search Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val symbolList = ids.split(",")
        return symbolList.mapNotNull { id ->
            try {
                val pair = "${id}-USD"
                Log.d("DIAGNOSTIC", "Coinbase Calling: prices/$pair/spot")
                val response = coinbaseApiService.getSpotPrice(pair)
                val price = response.data.amount.toDoubleOrNull() ?: 0.0
                
                val iconUrl = "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${id.lowercase()}.png"
                
                AssetEntity(
                    coinId = id,
                    symbol = id,
                    name = id,
                    imageUrl = iconUrl,
                    category = AssetCategory.CRYPTO,
                    currentPrice = price,
                    priceChange24h = 0.0,
                    sparklineData = emptyList(), // Filled by fetchSparkline if needed in repo
                    baseSymbol = id,
                    apiId = id,
                    iconUrl = iconUrl,
                    priceSource = name
                )
            } catch (e: Exception) {
                Log.e("DIAGNOSTIC", "Coinbase Price Error for $id: ${e.message}")
                null
            }
        }
    }

    /**
     * Coinbase Sparkline fetcher (Exchange API)
     * Data: [time, low, high, open, close, volume]
     * Close Price is index 4.
     */
    suspend fun fetchSparkline(symbol: String): List<Double> {
        val pair = if (symbol.contains("-")) symbol else "${symbol}-USD"
        Log.d("DIAGNOSTIC", "Coinbase Calling: candles for $pair")
        return try {
            val response = coinbaseApiService.getCandles(pair)
            // Coinbase returns points in reverse chronological order
            val points = response.take(168).mapNotNull { it.getOrNull(4) }.reversed()
            Log.d("DIAGNOSTIC", "Coinbase Sparkline Points: ${points.size}")
            points
        } catch (e: Exception) {
            Log.e("DIAGNOSTIC", "Coinbase Sparkline Error for $symbol: ${e.message}")
            emptyList()
        }
    }
}
