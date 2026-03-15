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
                Log.d("ADD_TRACE", "MARKET_WATCH: Fetching $symbol")
                fetchMarketPrice(symbol) 
            }
        } catch (e: Exception) {
            Log.e("AssetRepository", "refreshMarketWatch failed: ${e.message}")
        }
    }

    /**
     * Bridge function to fetch individual metal prices.
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
        
        val allLocalAssets = assetDao.getAllAssetsOnce()
        
        val hasHoles = allLocalAssets.any { it.currentPrice <= 0.0 && it.category == AssetCategory.CRYPTO }
        val shouldBypass = force || hasHoles

        if (!syncCoordinator.canRefresh(shouldBypass)) {
            Log.d("SWAN_SYNC", "Repository: Refresh blocked by Rate Limiter.")
            return@coroutineScope
        }
        
        if (hasHoles) {
            Log.w("API_TRACE", "REPOSITORY: Assets with NO PRICE detected. Bypassing rate limiter.")
        }

        syncCoordinator.startSync()
        var success = true

        try {
            Log.d("API_TRACE", "REPOSITORY: Found ${allLocalAssets.size} assets in DB for sync.")
            
            if (allLocalAssets.isNotEmpty()) {
                // 1. Refresh Crypto
                val cryptoAssets = allLocalAssets.filter { it.category == AssetCategory.CRYPTO }
                if (cryptoAssets.isNotEmpty()) {
                    try {
                        val ids = cryptoAssets.joinToString(",") { it.apiId.ifBlank { it.coinId } }
                        val provider = searchRegistry.getDefaultProvider()
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
            }
        } catch (e: Exception) {
            success = false
        } finally {
            syncCoordinator.endSync(success)
        }
    }

    /**
     * THE SURGICAL ROUTINE (ISOLATION PROTOCOL)
     *
     * This function is the exclusive network path for adding new assets.
     * It ensures a 1:1 ratio between user action and API hits by fetching 
     * only the specific asset being added. It includes localized 429 
     * retry logic to prevent global sync failures during high traffic.
     * 
     * This routine guarantees that additions do not trigger global refreshes,
     * maintaining strict data isolation.
     */
    suspend fun executeSurgicalAdd(
        asset: AssetEntity,
        onStatusUpdate: (Float, String) -> Unit
    ) {
        val apiId = asset.apiId.ifBlank { asset.coinId }
        
        // Step A: Initial Save (Price 0.0)
        Log.d("ADD_TRACE", "STEP 3: DB_SAVE_INITIAL: ID=$apiId")
        onStatusUpdate(0.2f, "Securing asset in local vault...")
        assetDao.upsertAsset(asset.copy(currentPrice = 0.0, lastUpdated = System.currentTimeMillis()))
        
        // Step B & C: Network Fetch with 429 Handling
        Log.d("ADD_TRACE", "STEP 4: API_HIT triggered by Surgical Routine")
        onStatusUpdate(0.4f, "Fetching real-time market data...")
        
        var freshData: CoinMarketResponse? = null
        var attempts = 0
        while (attempts < 2) {
            val response = coinGeckoApiService.getCoinMarkets(ids = apiId)
            if (response.isSuccessful) {
                freshData = response.body()?.firstOrNull()
                break
            } else if (response.code() == 429) {
                Log.w("ADD_TRACE", "429 Hit. Retrying in 3s...")
                onStatusUpdate(0.5f, "Market data busy... retrying in 3 seconds...")
                delay(3000)
                attempts++
            } else {
                break
            }
        }
        
        // Step D: Final Update
        if (freshData != null) {
            val price = freshData.currentPrice ?: 0.0
            Log.d("ADD_TRACE", "STEP 5: DB_PRICE_UPDATE: ID=$apiId, Price=$price")
            onStatusUpdate(0.9f, "Success! Finalizing portfolio...")
            
            val updated = asset.copy(
                currentPrice = price,
                priceChange24h = freshData.priceChangePercentage24h ?: 0.0,
                sparklineData = freshData.sparklineIn7d?.price ?: emptyList()
            )
            assetDao.upsertAsset(updated)
        } else {
            Log.e("ADD_TRACE", "STEP 5: DB_PRICE_UPDATE FAILED: No data returned for $apiId")
            onStatusUpdate(1.0f, "Landing with last known data...")
        }
    }

    suspend fun fetchLivePriceForAsset(
        coinId: String,
        onRetry: (String) -> Unit = {}
    ): CoinMarketResponse? {
        var attempt = 0
        while (attempt < 3) {
            try {
                val response = coinGeckoApiService.getCoinMarkets(ids = coinId)
                if (response.isSuccessful) {
                    return response.body()?.firstOrNull()
                } else if (response.code() == 429) {
                    onRetry("Market busy. Retrying in 3s...")
                    delay(3000)
                    attempt++
                } else {
                    return null
                }
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAsset(asset.coinId)
    suspend fun updateAssetOrder(assets: List<AssetEntity>) = assetDao.updateAssetOrder(assets)
    suspend fun updateAssetEntity(asset: AssetEntity) = assetDao.updateAssetEntity(asset)
}
