package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.api.impl.MetalSearchProvider
import com.swanie.portfolio.data.api.impl.MexcSearchProvider
import com.swanie.portfolio.data.api.impl.CryptoCompareSearchProvider
import com.swanie.portfolio.data.local.*
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.CoinMarketResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val transactionDao: TransactionDao,
    private val coinGeckoApiService: CoinGeckoApiService,
    private val syncCoordinator: DataSyncCoordinator,
    private val searchRegistry: SearchEngineRegistry
) {
    val allAssets = assetDao.getAllAssets()

    private var lastMetalsRefreshTime = 0L
    private val METALS_REFRESH_THRESHOLD = 30_000L // 30 Seconds

    /**
     * Strictly handles the "Big 4" (XAU, XAG, XPT, XPD).
     * Used only for the Market Watch screen cache.
     */
    suspend fun refreshMarketWatch() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRefresh = currentTime - lastMetalsRefreshTime
        
        if (timeSinceLastRefresh < METALS_REFRESH_THRESHOLD) {
            val remaining = (METALS_REFRESH_THRESHOLD - timeSinceLastRefresh) / 1000
            Log.d("ADD_TRACE", "METALS_SHIELD: Refresh blocked. Cooldown active (Remaining: ${remaining}s)")
            return
        }

        Log.d("API_TRACE", "REPOSITORY: refreshMarketWatch triggered")
        try {
            val metals = listOf("XAU", "XAG", "XPT", "XPD")
            metals.forEach { symbol ->
                Log.d("ADD_TRACE", "MARKET_WATCH: Fetching $symbol")
                fetchMarketPrice(symbol) 
            }
            lastMetalsRefreshTime = System.currentTimeMillis()
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
                // Multi-Source Sync logic
                val groupedBySource = allLocalAssets.groupBy { it.priceSource }
                
                groupedBySource.forEach { (source, assets) ->
                    val provider = searchRegistry.getProvider(source) ?: searchRegistry.getDefaultProvider()
                    Log.d("API_TRACE", "REPOSITORY: Refreshing ${assets.size} assets via ${provider.name}")
                    
                    try {
                        val ids = if (assets.any { it.category == AssetCategory.METAL }) "" 
                                 else assets.joinToString(",") { it.apiId.ifBlank { it.coinId } }
                        
                        val freshData = provider.getPrices(ids)
                        val dataMap = freshData.associateBy { if (it.category == AssetCategory.METAL) it.baseSymbol else it.coinId }
                        
                        val updatedAssets = assets.map { asset ->
                            val key = if (asset.category == AssetCategory.METAL) asset.baseSymbol else asset.coinId
                            dataMap[key]?.let { fresh ->
                                // GLOBAL_SYNC: Patching missing sparkline for non-CoinGecko sources
                                val updatedSparkline = if (asset.sparklineData.isEmpty()) {
                                    when (source) {
                                        "MEXC" -> {
                                            if (provider is MexcSearchProvider) {
                                                val points = provider.fetchSparkline(asset.apiId.ifBlank { asset.coinId })
                                                Log.d("ADD_TRACE", "DEEP_TRACE: MEXC Sparkline results for ${asset.symbol}: ${points.size} points.")
                                                if (points.isNotEmpty()) points else asset.sparklineData
                                            } else asset.sparklineData
                                        }
                                        "CryptoCompare" -> {
                                            if (provider is CryptoCompareSearchProvider) {
                                                val points = provider.fetchSparkline(asset.apiId.ifBlank { asset.coinId })
                                                Log.d("ADD_TRACE", "DEEP_TRACE: CryptoCompare Sparkline results for ${asset.symbol}: ${points.size} points.")
                                                if (points.isNotEmpty()) points else asset.sparklineData
                                            } else asset.sparklineData
                                        }
                                        else -> if (fresh.sparklineData.isNotEmpty()) fresh.sparklineData else asset.sparklineData
                                    }
                                } else {
                                    if (fresh.sparklineData.isNotEmpty()) fresh.sparklineData else asset.sparklineData
                                }

                                val finalAsset = asset.copy(
                                    currentPrice = fresh.currentPrice,
                                    sparklineData = updatedSparkline,
                                    priceChange24h = fresh.priceChange24h,
                                    lastUpdated = System.currentTimeMillis()
                                )
                                Log.d("ADD_TRACE", "REPO_PRE_SAVE: Symbol ${finalAsset.symbol} (Global Sync) is about to be saved. Sparkline size: ${finalAsset.sparklineData.size}")
                                finalAsset
                            } ?: asset
                        }
                        assetDao.upsertAll(updatedAssets)
                    } catch (e: Exception) {
                        Log.e("AssetRepository", "Refresh failed for source $source: ${e.message}")
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
     * only the specific asset being added.
     */
    suspend fun executeSurgicalAdd(
        asset: AssetEntity,
        onStatusUpdate: (Float, String) -> Unit
    ) {
        val apiId = asset.apiId.ifBlank { asset.coinId }
        val source = asset.priceSource.ifBlank { "CoinGecko" }
        
        // MEXC_TRACE: Log surgical add start
        if (source == "MEXC") {
            Log.d("MEXC_TRACE", "Surgical Add triggered for ${asset.symbol}")
        }

        // Step A: Initial Save (Price 0.0)
        Log.d("ADD_TRACE", "STEP 3: DB_SAVE_INITIAL: ID=$apiId, SOURCE=$source")
        onStatusUpdate(0.2f, "Securing asset in local vault...")
        assetDao.upsertAsset(asset.copy(currentPrice = 0.0, lastUpdated = System.currentTimeMillis()))
        
        // Step B & C: Network Fetch via Multi-Source Provider
        Log.d("ADD_TRACE", "STEP 4: API_HIT triggered by Surgical Routine for $source")
        onStatusUpdate(0.4f, "Verifying listing on $source...")
        
        try {
            val provider = searchRegistry.getProvider(source) ?: searchRegistry.getDefaultProvider()
            val freshList = provider.getPrices(apiId)
            val freshData = freshList.firstOrNull()

            // Step D: Final Update
            if (freshData != null) {
                onStatusUpdate(0.6f, "Retrieving market trends...")
                
                // MULTI-SOURCE BRIDGE: Fetch Sparkline
                val finalSparkline = when (source) {
                    "MEXC" -> {
                        if (provider is MexcSearchProvider) {
                            val points = provider.fetchSparkline(apiId)
                            Log.d("ADD_TRACE", "DEEP_TRACE: MEXC Sparkline results for ${asset.symbol}: ${points.size} points.")
                            points
                        } else emptyList()
                    }
                    "CryptoCompare" -> {
                        if (provider is CryptoCompareSearchProvider) {
                            val points = provider.fetchSparkline(apiId)
                            Log.d("ADD_TRACE", "DEEP_TRACE: CryptoCompare Sparkline results for ${asset.symbol}: ${points.size} points.")
                            points
                        } else emptyList()
                    }
                    else -> freshData.sparklineData
                }

                Log.d("ADD_TRACE", "STEP 5: DB_PRICE_UPDATE: ID=$apiId, Price=${freshData.currentPrice}")
                onStatusUpdate(0.9f, "Success! Finalizing portfolio...")
                
                val updated = asset.copy(
                    currentPrice = freshData.currentPrice,
                    priceChange24h = freshData.priceChange24h,
                    sparklineData = finalSparkline,
                    lastUpdated = System.currentTimeMillis()
                )
                Log.d("ADD_TRACE", "REPO_PRE_SAVE: Symbol ${updated.symbol} (Surgical) is about to be saved. Sparkline size: ${updated.sparklineData.size}")
                assetDao.upsertAsset(updated)

                // Step E: Black Box Logging
                Log.d("ADD_TRACE", "BLACK_BOX: Initial transaction logged for ${asset.symbol} at price ${freshData.currentPrice}")
                transactionDao.insertTransaction(
                    TransactionEntity(
                        assetId = asset.coinId,
                        type = "INITIAL_ADD",
                        amount = asset.amountHeld,
                        priceAtTime = freshData.currentPrice,
                        timestamp = System.currentTimeMillis(),
                        source = source
                    )
                )
            } else {
                Log.e("ADD_TRACE", "STEP 5: DB_PRICE_UPDATE FAILED: No data returned for $apiId from $source")
                onStatusUpdate(1.0f, "Landing with last known data...")
            }
        } catch (e: Exception) {
            Log.e("ADD_TRACE", "STEP 4/5: SURGICAL_ADD_ERROR: ${e.message}")
            onStatusUpdate(1.0f, "Error fetching data from $source")
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
