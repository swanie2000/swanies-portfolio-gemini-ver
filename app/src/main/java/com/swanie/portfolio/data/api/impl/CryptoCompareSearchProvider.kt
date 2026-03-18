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
        return try {
            val response = cryptoCompareApiService.getPriceFull(fsyms = ids)
            if (response.isSuccessful) {
                val rawData = response.body()?.raw ?: emptyMap()
                rawData.map { (symbol, priceMap) ->
                    val usdData = priceMap["USD"]
                    AssetEntity(
                        coinId = symbol,
                        symbol = symbol,
                        name = symbol,
                        imageUrl = if (usdData?.imageUrl != null) "https://www.cryptocompare.com${usdData.imageUrl}" else "",
                        category = AssetCategory.CRYPTO,
                        currentPrice = usdData?.price ?: 0.0,
                        priceChange24h = usdData?.changePct24h ?: 0.0,
                        sparklineData = emptyList(),
                        baseSymbol = symbol,
                        apiId = symbol,
                        iconUrl = if (usdData?.imageUrl != null) "https://www.cryptocompare.com${usdData.imageUrl}" else "",
                        priceSource = name
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ADD_TRACE", "CryptoCompare Price Error: ${e.message}")
            emptyList()
        }
    }
}
