package com.swanie.portfolio.data.repository

import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinGeckoApiService
import kotlinx.coroutines.flow.first

class AssetRepository(
    private val assetDao: AssetDao,
    private val coinGeckoApiService: CoinGeckoApiService
) {

    // Expose the Flow of assets from the DAO
    val allAssets = assetDao.getAllAssets()

    // The main sync function
    suspend fun refreshAssetPrices() {
        // Get the list of currently held coin IDs from the database
        val heldAssetIds = allAssets.first().map { it.coinId }.joinToString(",")

        if (heldAssetIds.isNotEmpty()) {
            try {
                // Fetch the latest prices from the API
                val priceMap = coinGeckoApiService.getPrices(heldAssetIds)

                // Create a list of updated entities
                val updatedAssets = allAssets.first().mapNotNull { asset ->
                    val newPriceData = priceMap[asset.coinId]
                    val newPrice = newPriceData?.get("usd")
                    if (newPrice != null) {
                        asset.copy(
                            currentPrice = newPrice,
                            lastUpdated = System.currentTimeMillis()
                        )
                    } else {
                        null // Asset not found in API response
                    }
                }

                // Save the updated assets back to the database
                updatedAssets.forEach { assetDao.insertAsset(it) }

            } catch (e: Exception) {
                // Handle potential network errors or API issues
                // For now, we'll just print the error, but in a real app, you might want to show a toast.
                e.printStackTrace()
            }
        }
    }
}
