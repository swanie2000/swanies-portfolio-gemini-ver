package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.api.impl.MetalSearchProvider
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.CoinMarketResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val coinGeckoApiService: CoinGeckoApiService,
    private val syncCoordinator: DataSyncCoordinator,
    private val searchRegistry: SearchEngineRegistry
) {
    val allAssets = assetDao.getAllAssets()

    /**
     * Strictly handles the "Big 4" (XAU, XAG, XPT, XPD).
     * Used only for the Market Watch screen cache.
     */
    suspend fun refreshMarketWatch() {
        Log.d("API_TRACE", "REPOSITORY: refreshMarketWatch triggered")
        try {
            val metals = listOf("XAU", "XAG", "XPT", "XPD")
            metals.forEach { symbol ->
                Log.d("API_TRACE", "MARKET_WATCH: Fetching $symbol")
                fetchMarketPrice(symbol) 
            }
        } catch (e: Exception) {
            Log.e("AssetRepository", "refreshMarketWatch failed: ${e.message}")
        }
    }

    /**
     * Bridge function to fetch individual metal prices.
     * Restored to maintain Signature Integrity for the ViewModel and Audit Screen.
     */
    suspend fun fetchMarketPrice(symbol: String): MarketPriceData {
        return try {
            val provider = searchRegistry.getMetalProvider() as? MetalSearchProvider
            if (provider != null) {
                provider.fetchMarketData(symbol)
            } else {
                MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
            }
        } catch (e: Exception) {
            Log.e("AssetRepository", "fetchMarketPrice failed for $symbol: ${e.message}")
            MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
        }
    }

    /**
     * Dynamic refresh logic: Loops through actual symbols in the database.
     */
    suspend fun refreshAssets(force: Boolean = false) = coroutineScope {
        Log.d("API_TRACE", "REPOSITORY: refreshAssets triggered")
        if (!syncCoordinator.canRefresh(force)) {
            Log.d("SWAN_SYNC", "Repository: Refresh blocked by Rate Limiter.")
            return@coroutineScope
        }

        syncCoordinator.startSync()
        var success = true

        try {
            val allLocalAssets = allAssets.first()
            if (allLocalAssets.isNotEmpty()) {
                // 1. Refresh Crypto
                val cryptoAssets = allLocalAssets.filter { it.category == AssetCategory.CRYPTO }
                if (cryptoAssets.isNotEmpty()) {
                    try {
                        val ids = cryptoAssets.joinToString(",") { it.coinId }
                        val provider = searchRegistry.getDefaultProvider()
                        
                        cryptoAssets.forEach { Log.d("API_TRACE", "SYNC_HOLDING: Fetching ${it.symbol}") }
                        
                        val freshData = provider.getPrices(ids)
                        val dataMap = freshData.associateBy { it.coinId }
                        val updatedCrypto = cryptoAssets.map { asset ->
                            dataMap[asset.coinId]?.let { fresh ->
                                asset.copy(
                                    currentPrice = fresh.currentPrice,
                                    sparklineData = fresh.sparklineData,
                                    priceChange24h = fresh.priceChange24h
                                )
                            } ?: asset
                        }
                        assetDao.upsertAll(updatedCrypto)
                    } catch (e: Exception) {
                        Log.e("AssetRepository", "Crypto refresh failed: ${e.message}")
                        success = false
                    }
                }

                // 2. Refresh Metals
                val metalAssets = allLocalAssets.filter { it.category == AssetCategory.METAL }
                if (metalAssets.isNotEmpty()) {
                    try {
                        val metalProvider = searchRegistry.getMetalProvider()
                        
                        metalAssets.forEach { Log.d("API_TRACE", "SYNC_HOLDING: Fetching ${it.symbol}") }
                        
                        val freshMetals = metalProvider.getPrices("")
                        val metalDataMap = freshMetals.associateBy { it.baseSymbol }
                        
                        val updatedMetals = metalAssets.map { asset ->
                            metalDataMap[asset.baseSymbol]?.let { fresh ->
                                asset.copy(
                                    currentPrice = fresh.currentPrice,
                                    sparklineData = fresh.sparklineData,
                                    priceChange24h = fresh.priceChange24h
                                )
                            } ?: asset
                        }
                        assetDao.upsertAll(updatedMetals)
                    } catch (e: Exception) {
                        Log.e("AssetRepository", "Metals refresh failed: ${e.message}")
                        success = false
                    }
                }
            } else {
                Log.d("API_TRACE", "REPOSITORY: No holdings to sync.")
            }
        } catch (e: Exception) {
            success = false
        } finally {
            syncCoordinator.endSync(success)
        }
    }

    /**
     * Performs a surgical refresh for a single asset.
     * Used for new additions to ensure they land with real data.
     */
    suspend fun refreshSingleAsset(asset: AssetEntity) {
        Log.d("API_TRACE", "REPOSITORY: Fetching initial price for NEW asset: ${asset.symbol}")
        try {
            if (asset.category == AssetCategory.CRYPTO) {
                val fresh = fetchLivePriceForAsset(asset.coinId)
                if (fresh != null) {
                    val updated = asset.copy(
                        currentPrice = fresh.currentPrice ?: 0.0,
                        priceChange24h = fresh.priceChangePercentage24h ?: 0.0,
                        sparklineData = fresh.sparklineIn7d?.price ?: emptyList()
                    )
                    assetDao.upsertAsset(updated)
                }
            } else if (asset.category == AssetCategory.METAL) {
                val metalProvider = searchRegistry.getMetalProvider()
                val freshMetals = metalProvider.getPrices("")
                val match = freshMetals.find { it.baseSymbol == asset.baseSymbol }
                if (match != null) {
                    val updated = asset.copy(
                        currentPrice = match.currentPrice,
                        priceChange24h = match.priceChange24h,
                        sparklineData = match.sparklineData
                    )
                    assetDao.upsertAsset(updated)
                }
            }
        } catch (e: Exception) {
            Log.e("AssetRepository", "refreshSingleAsset failed for ${asset.symbol}: ${e.message}")
        }
    }

    suspend fun fetchLivePriceForAsset(coinId: String): CoinMarketResponse? =
        try { coinGeckoApiService.getCoinMarkets(ids = coinId).firstOrNull() } catch (e: Exception) { null }

    suspend fun saveAsset(asset: AssetEntity) = assetDao.upsertAsset(asset.copy(lastUpdated = System.currentTimeMillis()))
    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAsset(asset.coinId)
    suspend fun updateAssetOrder(assets: List<AssetEntity>) = assetDao.updateAssetOrder(assets)
    suspend fun updateAssetEntity(asset: AssetEntity) = assetDao.updateAssetEntity(asset)
}
