package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val searchRegistry: SearchEngineRegistry,
    private val syncCoordinator: DataSyncCoordinator
) {
    // 🛡️ FIX: Restore the 'allAssets' property using the new DAO naming convention
    // Points to the 'MAIN' portfolio by default to satisfy the ViewModel
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssetsFlow()

    // 🚀 V8 Multi-Portfolio access
    fun getAssetsForPortfolio(portfolioId: String) = assetDao.getAssetsByPortfolio(portfolioId)

    suspend fun refreshAssets(force: Boolean = false, portfolioId: String = "MAIN") {
        if (!syncCoordinator.canRefresh(force)) return

        var isSuccess = false
        try {
            syncCoordinator.startSync()
            val assets = assetDao.getAllAssetsOnce(portfolioId)
            if (assets.isEmpty()) {
                isSuccess = true
                return
            }

            assets.groupBy { it.priceSource }.forEach { (sourceName, providerAssets) ->
                val provider = searchRegistry.getProvider(sourceName) ?: return@forEach

                val idString = providerAssets.joinToString(",") { it.apiId }

                Log.d("SWAN_DEBUG", "REPO: Refreshing $sourceName with IDs: $idString")
                val updatedList = provider.getPrices(idString)

                providerAssets.forEach { existing ->
                    updatedList.find {
                        it.apiId.equals(existing.apiId, true) || it.symbol.equals(existing.symbol, true)
                    }?.let { update ->
                        assetDao.upsertAsset(existing.copy(
                            officialSpotPrice = update.officialSpotPrice,
                            priceChange24h = update.priceChange24h,
                            sparklineData = if (update.sparklineData.isNotEmpty()) update.sparklineData else existing.sparklineData,
                            imageUrl = if (existing.imageUrl.isEmpty() || existing.imageUrl.contains("coincap")) update.imageUrl else existing.imageUrl,
                            lastUpdated = System.currentTimeMillis(),
                            portfolioId = existing.portfolioId,
                            widgetOrder = existing.widgetOrder
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

    // 🛡️ RESTORED: Metadata Healing for the ViewModel
    suspend fun healMetadata(asset: AssetEntity): AssetEntity {
        if (asset.priceSource == "CoinGecko" || asset.category == AssetCategory.METAL) return asset
        return try {
            val cgProvider = searchRegistry.getProvider("CoinGecko") ?: return asset
            val results = cgProvider.search(asset.symbol)
            val match = results.find { it.symbol.equals(asset.symbol, ignoreCase = true) }
            if (match != null) {
                asset.copy(apiId = match.id, imageUrl = match.imageUrl, iconUrl = match.imageUrl)
            } else asset
        } catch (e: Exception) { asset }
    }

    // 🛡️ RESTORED: Live Data Fetch for the ViewModel
    suspend fun fetchLiveAssetData(asset: AssetEntity): AssetEntity {
        return try {
            val provider = searchRegistry.getProvider(asset.priceSource) ?: return asset
            val updates = provider.getPrices(asset.apiId)
            val liveMatch = updates.find { it.apiId.equals(asset.apiId, true) || it.symbol.equals(asset.symbol, true) }
            if (liveMatch != null) {
                asset.copy(
                    officialSpotPrice = liveMatch.officialSpotPrice,
                    priceChange24h = liveMatch.priceChange24h,
                    sparklineData = liveMatch.sparklineData,
                    lastUpdated = System.currentTimeMillis()
                )
            } else asset
        } catch (e: Exception) { asset }
    }

    suspend fun upsertAsset(asset: AssetEntity) {
        val sanitizedAsset = if (asset.portfolioId.isEmpty()) asset.copy(portfolioId = "MAIN") else asset
        assetDao.upsertAsset(sanitizedAsset)
    }

    // 🛡️ V8 Overloads for Deletion Compatibility
    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAssetById(asset.coinId)
    suspend fun deleteAsset(id: String) = assetDao.deleteAssetById(id)

    suspend fun updateAssetOrder(assets: List<AssetEntity>) { assetDao.updateAssetOrder(assets) }
    suspend fun updateAssetEntity(asset: AssetEntity) { assetDao.updateAssetEntity(asset) }
    suspend fun toggleWidgetVisibility(id: String, isVisible: Boolean) { assetDao.updateWidgetVisibility(id, isVisible) }

    suspend fun executeSurgicalAdd(asset: AssetEntity, callback: (Boolean, String) -> Unit) {
        try {
            upsertAsset(asset)
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