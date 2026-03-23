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
                
                // DATA DISPATCHER: Use apiId for all engines to ensureTechnical Ticker accuracy
                val idString = providerAssets.joinToString(",") { it.apiId }

                Log.d("SWAN_DEBUG", "REPO: Refreshing $sourceName with IDs: $idString")
                val updatedList = provider.getPrices(idString)
                Log.d("SWAN_DEBUG", "REPO: Received ${updatedList.size} updates from $sourceName")

                providerAssets.forEach { existing ->
                    // Match by apiId or Symbol for maximum resilience
                    updatedList.find { 
                        it.apiId.equals(existing.apiId, true) || it.symbol.equals(existing.symbol, true) 
                    }?.let { update ->
                        Log.d("SWAN_DEBUG", "REPO: Applying update for ${existing.symbol}: Price=${update.officialSpotPrice}")
                        assetDao.upsertAsset(existing.copy(
                            officialSpotPrice = update.officialSpotPrice,
                            priceChange24h = update.priceChange24h,
                            sparklineData = if (update.sparklineData.isNotEmpty()) update.sparklineData else existing.sparklineData,
                            imageUrl = if (existing.imageUrl.isEmpty() || existing.imageUrl.contains("coincap")) update.imageUrl else existing.imageUrl,
                            lastUpdated = System.currentTimeMillis()
                        ))
                    } ?: Log.w("SWAN_DEBUG", "REPO: No match found in update list for ${existing.symbol}")
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

    suspend fun healMetadata(asset: AssetEntity): AssetEntity {
        if (asset.priceSource == "CoinGecko" || asset.category == AssetCategory.METAL) return asset
        
        return try {
            val cgProvider = searchRegistry.getProvider("CoinGecko") ?: return asset
            val results = cgProvider.search(asset.symbol)
            val match = results.find { it.symbol.equals(asset.symbol, ignoreCase = true) }
            
            if (match != null) {
                asset.copy(
                    apiId = match.id,
                    imageUrl = match.imageUrl,
                    iconUrl = match.imageUrl
                )
            } else asset
        } catch (e: Exception) { asset }
    }

    suspend fun fetchLiveAssetData(asset: AssetEntity): AssetEntity {
        Log.d("SWAN_DEBUG", "REPO: Pre-flight fetch for ${asset.symbol} (ID: ${asset.apiId})")
        return try {
            val provider = searchRegistry.getProvider(asset.priceSource) ?: return asset
            
            // Dispatch technical ID
            val idString = asset.apiId
            
            Log.d("SWAN_DEBUG", "REPO: Calling provider.getPrices with technical ID: $idString")
            val updates = provider.getPrices(idString)
            
            // Match against technical ID
            val liveMatch = updates.find { 
                it.apiId.equals(asset.apiId, true) || it.symbol.equals(asset.symbol, true) 
            }
            
            if (liveMatch != null) {
                Log.d("SWAN_DEBUG", "REPO: Live match found! New Price: ${liveMatch.officialSpotPrice}")
                asset.copy(
                    officialSpotPrice = liveMatch.officialSpotPrice,
                    priceChange24h = liveMatch.priceChange24h,
                    sparklineData = liveMatch.sparklineData,
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                Log.w("SWAN_DEBUG", "REPO: Failed to find live match for ${asset.symbol}")
                asset
            }
        } catch (e: Exception) { asset }
    }

    suspend fun addAsset(asset: AssetEntity) { assetDao.upsertAsset(asset) }
    suspend fun deleteAsset(asset: AssetEntity) { assetDao.deleteAssetById(asset.coinId) }
    suspend fun updateAssetOrder(assets: List<AssetEntity>) { assetDao.updateAssetOrder(assets) }
    suspend fun updateAssetEntity(asset: AssetEntity) { assetDao.updateAssetEntity(asset) }
    suspend fun toggleWidgetVisibility(id: String, isVisible: Boolean) { assetDao.updateWidgetVisibility(id, isVisible) }

    suspend fun executeSurgicalAdd(asset: AssetEntity, callback: (Boolean, String) -> Unit) {
        try {
            Log.d("SWAN_DEBUG", "REPO: DB Write for ${asset.coinId}")
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
        } else MarketPriceData()
    }

    suspend fun refreshMarketWatch() {
        refreshAssets(force = false)
    }
}