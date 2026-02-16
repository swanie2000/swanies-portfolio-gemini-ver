package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.MarketData
import kotlinx.coroutines.flow.first

class AssetRepository(
    private val assetDao: AssetDao,
    private val coinGeckoApiService: CoinGeckoApiService
) {

    val allAssets = assetDao.getAllAssets()

    private fun getCanonicalApiId(asset: AssetEntity): String {
        return when (asset.symbol.uppercase()) {
            "XRP" -> "ripple"
            "BTC" -> "bitcoin"
            "ETH" -> "ethereum"
            else -> asset.coinId
        }
    }

    suspend fun refreshAssetPrices() {
        val heldAssets = allAssets.first()
        if (heldAssets.isEmpty()) return

        val apiIds = heldAssets.map { getCanonicalApiId(it) }.joinToString(",")

        try {
            val priceMap = coinGeckoApiService.getSimplePrice(ids = apiIds)
            val updatedAssets = heldAssets.map { asset ->
                val canonicalId = getCanonicalApiId(asset)
                val newPrice = priceMap[canonicalId]?.get("usd")
                asset.copy(
                    currentPrice = newPrice ?: asset.currentPrice,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            updatedAssets.forEach { assetDao.insertAsset(it) }
        } catch (e: Exception) {
            Log.e("AssetRepository", "Failed to refresh asset prices", e)
        }
    }

    suspend fun searchCoinsWithPrices(query: String): List<MarketData> {
        if (query.length < 2) return emptyList()
        return try {
            val searchResult = coinGeckoApiService.search(query)
            if (searchResult.coins.isEmpty()) return emptyList()
            val ids = searchResult.coins.joinToString(",") { it.id }
            coinGeckoApiService.getMarkets(ids = ids)
        } catch (e: Exception) {
            Log.e("AssetRepository", "Failed to search coins with prices: $query", e)
            emptyList()
        }
    }

    /**
     * THE FIX: Simple function to insert/update a single asset.
     */
    suspend fun saveAsset(asset: AssetEntity) {
        assetDao.insertAsset(asset)
    }
}
