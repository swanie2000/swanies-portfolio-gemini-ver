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
    private val coinbaseApi: CoinbaseApiService
) : SearchProvider {
    override val name: String = "Coinbase"

    private var masterListCache: List<SearchResult> = emptyList()

    override suspend fun search(query: String): List<SearchResult> {
        return try {
            if (masterListCache.isEmpty()) {
                val currencies = coinbaseApi.getExchangeCurrencies()
                masterListCache = currencies.map { crypto ->
                    val base = crypto.id.uppercase()
                    // ICON FIX: Standardizing on a more reliable community source for icons
                    val iconUrl = "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${base.lowercase()}.png"
                    SearchResult(
                        id = "CB_$base",
                        symbol = base,
                        name = crypto.name,
                        imageUrl = iconUrl,
                        category = AssetCategory.CRYPTO,
                        priceSource = name
                    )
                }
            }
            masterListCache.filter {
                it.symbol.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true)
            }.take(15)
        } catch (e: Exception) {
            Log.e("COINBASE_SEARCH", "Search failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val results = mutableListOf<AssetEntity>()
        val symbols = ids.split(",")

        for (symbol in symbols) {
            if (symbol.isBlank()) continue
            val cleanSymbol = symbol.trim().uppercase()
            try {
                // 1. Fetch current Spot Price
                val pair = "$cleanSymbol-USD"
                val priceResponse = try {
                    coinbaseApi.getSpotPrice(pair)
                } catch (e: Exception) {
                    Log.e("COINBASE_PRICE", "Spot price failed for $pair: ${e.message}")
                    null
                }
                
                val currentPrice = priceResponse?.data?.amount?.toDoubleOrNull() ?: 0.0

                // 2. SPARKLINE RESTORATION
                val candles = try {
                    coinbaseApi.getExchangeCandles(pair)
                } catch (e: Exception) {
                    Log.e("COINBASE_PRICE", "Candles failed for $pair: ${e.message}")
                    emptyList()
                }

                val sparkline = candles.take(24).mapNotNull { it.getOrNull(4) }.reversed()
                
                // ICON FIX: Standardizing on a more reliable community source
                val iconUrl = "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${cleanSymbol.lowercase()}.png"

                results.add(
                    AssetEntity(
                        coinId = "CB_$cleanSymbol",
                        symbol = cleanSymbol,
                        name = cleanSymbol,
                        imageUrl = iconUrl,
                        category = AssetCategory.CRYPTO,
                        officialSpotPrice = currentPrice,
                        sparklineData = sparkline,
                        priceSource = name,
                        lastUpdated = System.currentTimeMillis(),
                        apiId = "CB_$cleanSymbol"
                    )
                )
            } catch (e: Exception) {
                Log.e("COINBASE_PRICE", "Overall failure for $symbol: ${e.message}")
            }
        }
        return results
    }
}