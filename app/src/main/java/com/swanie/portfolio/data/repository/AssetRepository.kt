package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val searchRegistry: SearchEngineRegistry,
    private val syncCoordinator: DataSyncCoordinator
) {
    val allAssets = assetDao.getAllAssets()

    suspend fun refreshAssets(force: Boolean = false) {
        if (!syncCoordinator.canRefresh(force)) return
        
        var isSuccess = false
        try {
            syncCoordinator.startSync()
            val assets = assetDao.getAllAssetsOnce()
            if (assets.isEmpty()) {
                isSuccess = true
                return
            }

            assets.groupBy { it.priceSource }.forEach { (sourceName, providerAssets) ->
                val provider = searchRegistry.getProvider(sourceName) ?: return@forEach
                
                val idString = if (sourceName.equals("CoinGecko", ignoreCase = true)) {
                    providerAssets.joinToString(",") { it.apiId }
                } else {
                    providerAssets.joinToString(",") { it.symbol }
                }

                val updatedList = provider.getPrices(idString)

                providerAssets.forEach { existing ->
                    updatedList.find { it.symbol.equals(existing.symbol, ignoreCase = true) }?.let { update ->
                        assetDao.upsertAsset(existing.copy(
                            officialSpotPrice = update.officialSpotPrice,
                            priceChange24h = update.priceChange24h,
                            sparklineData = if (update.sparklineData.isNotEmpty()) update.sparklineData else existing.sparklineData,
                            imageUrl = if (existing.imageUrl.isEmpty()) update.imageUrl else existing.imageUrl,
                            lastUpdated = System.currentTimeMillis()
                        ))
                    }
                }
            }
            isSuccess = true
        } catch (e: Exception) {
            Log.e("REPO_REFRESH", "Error: ${e.message}")
            isSuccess = false
        } finally {
            syncCoordinator.endSync(isSuccess)
        }
    }

    /**
     * SURGICAL: Live data fetch for a single asset.
     * Used by AmountEntryViewModel to ensure assets land with data.
     */
    suspend fun fetchLiveAssetData(asset: AssetEntity): AssetEntity {
        return try {
            val provider = searchRegistry.getProvider(asset.priceSource) ?: return asset
            
            // Dispatch correctly: CoinGecko needs ID, others need Symbol
            val idString = if (asset.priceSource.equals("CoinGecko", true)) asset.apiId else asset.symbol
            
            val updates = provider.getPrices(idString)
            val liveMatch = updates.find { it.symbol.equals(asset.symbol, true) }
            
            if (liveMatch != null) {
                asset.copy(
                    officialSpotPrice = liveMatch.officialSpotPrice,
                    priceChange24h = liveMatch.priceChange24h,
                    sparklineData = liveMatch.sparklineData,
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                asset
            }
        } catch (e: Exception) {
            Log.e("REPO_LIVE_FETCH", "Failed: ${e.message}")
            asset
        }
    }

    suspend fun addAsset(asset: AssetEntity) {
        assetDao.upsertAsset(asset)
    }

    suspend fun deleteAsset(asset: AssetEntity) {
        assetDao.deleteAssetById(asset.coinId)
    }

    suspend fun updateAssetOrder(assets: List<AssetEntity>) {
        assetDao.updateAssetOrder(assets)
    }

    suspend fun updateAssetEntity(asset: AssetEntity) {
        assetDao.updateAssetEntity(asset)
    }

    suspend fun toggleWidgetVisibility(id: String, isVisible: Boolean) {
        assetDao.updateWidgetVisibility(id, isVisible)
    }

    suspend fun executeSurgicalAdd(asset: AssetEntity, callback: (Boolean, String) -> Unit) {
        try {
            assetDao.upsertAsset(asset)
            callback(true, "Asset added successfully")
        } catch (e: Exception) {
            callback(false, e.message ?: "Unknown error")
        }
    }

    suspend fun fetchMarketPrice(symbol: String): MarketPriceData {
        val provider = searchRegistry.getProvider("YahooFinance")
        return if (provider is com.swanie.portfolio.data.api.impl.MetalSearchProvider) {
            provider.fetchMarketData(symbol)
        } else {
            MarketPriceData()
        }
    }

    suspend fun refreshMarketWatch() {
        refreshAssets(force = true)
    }
}