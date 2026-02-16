package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinGeckoApiService
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

    suspend fun getSingleCoinPrice(coinId: String): Double {
        return try {
            val priceMap = coinGeckoApiService.getSimplePrice(ids = coinId)
            priceMap[coinId]?.get("usd") ?: 0.0
        } catch (e: Exception) {
            Log.e("AssetRepository", "Failed to get single coin price for $coinId", e)
            0.0
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

    suspend fun searchCoins(query: String): List<AssetEntity> {
        if (query.length < 2) return emptyList()
        return try {
            val searchResult = coinGeckoApiService.search(query)
            searchResult.coins.map { coin ->
                AssetEntity(
                    coinId = coin.id,
                    symbol = coin.symbol,
                    name = coin.name,
                    imageUrl = coin.large,
                    amountHeld = 0.0,
                    currentPrice = 0.0,
                    change24h = 0.0,
                    displayOrder = 0,
                    lastUpdated = 0L
                )
            }
        } catch (e: Exception) {
            Log.e("AssetRepository", "Failed to search coins for query: $query", e)
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