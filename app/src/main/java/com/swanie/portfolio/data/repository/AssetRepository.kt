package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.api.impl.MetalSearchProvider
import com.swanie.portfolio.data.api.impl.KuCoinSearchProvider
import com.swanie.portfolio.data.api.impl.CoinbaseSearchProvider
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

    suspend fun refreshMarketWatch() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRefresh = currentTime - lastMetalsRefreshTime
        
        if (timeSinceLastRefresh < METALS_REFRESH_THRESHOLD) {
            val remaining = (METALS_REFRESH_THRESHOLD - timeSinceLastRefresh) / 1000
            Log.d("ADD_TRACE", "METALS_SHIELD: Refresh blocked. Cooldown active (Remaining: ${remaining}s)")
            return
        }

        try {
            val metals = listOf("XAU", "XAG", "XPT", "XPD")
            metals.forEach { symbol ->
                fetchMarketPrice(symbol) 
            }
            lastMetalsRefreshTime = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("AssetRepository", "refreshMarketWatch failed: ${e.message}")
        }
    }

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

    suspend fun refreshAssets(force: Boolean = false) = coroutineScope {
        val allLocalAssets = assetDao.getAllAssetsOnce()
        val hasHoles = allLocalAssets.any { it.currentPrice <= 0.0 && it.category == AssetCategory.CRYPTO }
        val shouldBypass = force || hasHoles

        if (!syncCoordinator.canRefresh(shouldBypass)) {
            Log.d("SWAN_SYNC", "Repository: Refresh blocked by Rate Limiter.")
            return@coroutineScope
        }
        
        syncCoordinator.startSync()
        var success = true

        try {
            if (allLocalAssets.isNotEmpty()) {
                val groupedBySource = allLocalAssets.groupBy { it.priceSource }
                
                groupedBySource.forEach { (source, assets) ->
                    val provider = searchRegistry.getProvider(source) ?: searchRegistry.getDefaultProvider()
                    
                    try {
                        val ids = if (assets.any { it.category == AssetCategory.METAL }) "" 
                                 else assets.joinToString(",") { it.apiId.ifBlank { it.coinId } }
                        
                        val freshData = provider.getPrices(ids)
                        val dataMap = freshData.associateBy { if (it.category == AssetCategory.METAL) it.baseSymbol else it.coinId }
                        
                        val updatedAssets = assets.map { asset ->
                            val key = if (asset.category == AssetCategory.METAL) asset.baseSymbol else asset.coinId
                            dataMap[key]?.let { fresh ->
                                // BRIDGE: Fetch Sparkline if missing
                                val updatedSparkline = if (asset.sparklineData.isEmpty()) {
                                    when (source) {
                                        "KuCoin" -> {
                                            if (provider is KuCoinSearchProvider) {
                                                provider.fetchSparkline(asset.apiId.ifBlank { asset.coinId })
                                            } else asset.sparklineData
                                        }
                                        "Coinbase" -> {
                                            if (provider is CoinbaseSearchProvider) {
                                                provider.fetchSparkline(asset.apiId.ifBlank { asset.coinId })
                                            } else asset.sparklineData
                                        }
                                        "CryptoCompare" -> {
                                            if (provider is CryptoCompareSearchProvider) {
                                                provider.fetchSparkline(asset.apiId.ifBlank { asset.coinId })
                                            } else asset.sparklineData
                                        }
                                        else -> if (fresh.sparklineData.isNotEmpty()) fresh.sparklineData else asset.sparklineData
                                    }
                                } else {
                                    if (fresh.sparklineData.isNotEmpty()) fresh.sparklineData else asset.sparklineData
                                }

                                asset.copy(
                                    currentPrice = fresh.currentPrice,
                                    sparklineData = updatedSparkline,
                                    priceChange24h = fresh.priceChange24h,
                                    lastUpdated = System.currentTimeMillis()
                                )
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

    suspend fun executeSurgicalAdd(
        asset: AssetEntity,
        onStatusUpdate: (Float, String) -> Unit
    ) {
        val apiId = asset.apiId.ifBlank { asset.coinId }
        val source = asset.priceSource.ifBlank { "CoinGecko" }
        
        onStatusUpdate(0.2f, "Securing asset in local vault...")
        assetDao.upsertAsset(asset.copy(currentPrice = 0.0, lastUpdated = System.currentTimeMillis()))
        
        onStatusUpdate(0.4f, "Verifying listing on $source...")
        
        try {
            val provider = searchRegistry.getProvider(source) ?: searchRegistry.getDefaultProvider()
            val freshList = provider.getPrices(apiId)
            val freshData = freshList.firstOrNull()

            if (freshData != null) {
                onStatusUpdate(0.6f, "Retrieving market trends...")
                
                // MULTI-SOURCE BRIDGE: Fetch Sparkline
                val finalSparkline = when (source) {
                    "KuCoin" -> {
                        if (provider is KuCoinSearchProvider) {
                            provider.fetchSparkline(apiId)
                        } else emptyList()
                    }
                    "Coinbase" -> {
                        if (provider is CoinbaseSearchProvider) {
                            provider.fetchSparkline(apiId)
                        } else emptyList()
                    }
                    "CryptoCompare" -> {
                        if (provider is CryptoCompareSearchProvider) {
                            provider.fetchSparkline(apiId)
                        } else emptyList()
                    }
                    else -> freshData.sparklineData
                }

                onStatusUpdate(0.9f, "Success! Finalizing portfolio...")
                
                val updated = asset.copy(
                    currentPrice = freshData.currentPrice,
                    priceChange24h = freshData.priceChange24h,
                    sparklineData = finalSparkline,
                    lastUpdated = System.currentTimeMillis()
                )
                assetDao.upsertAsset(updated)

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
                onStatusUpdate(1.0f, "Landing with last known data...")
            }
        } catch (e: Exception) {
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
