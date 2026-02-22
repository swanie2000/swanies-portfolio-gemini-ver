package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.CoinMarketResponse
import kotlinx.coroutines.flow.first

class AssetRepository(
    private val assetDao: AssetDao,
    private val coinGeckoApiService: CoinGeckoApiService
) {

    val allAssets = assetDao.getAllAssets()

    suspend fun refreshAssets() {
        val heldAssetIds = assetDao.getAllCoinIds()
        if (heldAssetIds.isEmpty()) return

        try {
            val marketData: List<CoinMarketResponse> = coinGeckoApiService.getCoinMarkets(ids = heldAssetIds.joinToString(","))

            // Create a map for quick lookups
            val marketDataMap = marketData.associateBy { it.id }

            // Get the current state of assets from the database to preserve user-specific data like amountHeld
            val currentAssets = allAssets.first().associateBy { it.coinId }

            val updatedAssets = heldAssetIds.mapNotNull { coinId ->
                val marketInfo = marketDataMap[coinId]
                val currentAsset = currentAssets[coinId]

                if (marketInfo != null && currentAsset != null) {
                    currentAsset.copy(
                        currentPrice = marketInfo.currentPrice ?: currentAsset.currentPrice,
                        priceChange24h = marketInfo.priceChange24h ?: currentAsset.priceChange24h,
                        marketCapRank = marketInfo.marketCapRank ?: currentAsset.marketCapRank,
                        sparklineData = marketInfo.sparklineIn7d?.price ?: currentAsset.sparklineData,
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    null // This coin is no longer in the market data, or we don't have it locally.
                }
            }

            assetDao.upsertAll(updatedAssets)

        } catch (e: Exception) {
            Log.e("AssetRepository", "Failed to refresh assets from market data", e)
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
                    lastUpdated = 0L,
                    sparklineData = emptyList(),
                    marketCapRank = 0,
                    priceChange24h = 0.0
                )
            }
        } catch (e: Exception) {
            Log.e("AssetRepository", "Failed to search coins for query: $query", e)
            emptyList()
        }
    }

    suspend fun saveAsset(asset: AssetEntity) {
        assetDao.insertAsset(asset)
    }
}
