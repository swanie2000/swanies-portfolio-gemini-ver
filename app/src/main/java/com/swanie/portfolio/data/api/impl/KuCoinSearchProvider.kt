package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.KuCoinApiService
import javax.inject.Inject

class KuCoinSearchProvider @Inject constructor(
    private val api: KuCoinApiService
) : SearchProvider {
    override val name: String = "KuCoin"

    override suspend fun search(q: String): List<SearchResult> = try {
        api.getSymbols().data
            .filter { it.symbol.contains(q, true) || it.baseCurrency.contains(q, true) }
            .distinctBy { it.baseCurrency }
            .take(15)
            .map {
                val base = it.baseCurrency.uppercase()
                // ICON FIX: Standardizing on a more reliable community source for icons
                val iconUrl = "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${base.lowercase()}.png"
                SearchResult(
                    id = "KC_$base",
                    symbol = base,
                    name = it.name,
                    imageUrl = iconUrl,
                    category = AssetCategory.CRYPTO,
                    priceSource = name
                )
            }
    } catch (e: Exception) { 
        Log.e("KUCOIN_TRACE", "Search failed: ${e.message}")
        emptyList() 
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        val results = mutableListOf<AssetEntity>()
        val symbols = ids.split(",")
        
        for (sym in symbols) {
            if (sym.isBlank()) continue
            val cleanSym = sym.trim().uppercase()
            try {
                // 1. Fetch current price
                val pair = "$cleanSym-USDT"
                val tickerResponse = api.getTicker(pair)
                val price = tickerResponse.data.price.toDoubleOrNull() ?: 0.0

                // 2. SPARKLINE FIX: Fetch last 24 hours of price history
                val klines = api.getKLines(pair)
                val spark = klines.data.take(24).mapNotNull { it.getOrNull(2)?.toDoubleOrNull() }.reversed()

                val iconUrl = "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${cleanSym.lowercase()}.png"

                results.add(AssetEntity(
                    coinId = "KC_$cleanSym",
                    symbol = cleanSym,
                    name = cleanSym,
                    imageUrl = iconUrl,
                    category = AssetCategory.CRYPTO,
                    officialSpotPrice = price,
                    sparklineData = spark,
                    priceSource = name,
                    lastUpdated = System.currentTimeMillis(),
                    apiId = "KC_$cleanSym"
                ))
            } catch (e: Exception) { 
                Log.e("KUCOIN_TRACE", "Failed for $sym: ${e.message}")
            }
        }
        return results
    }
}