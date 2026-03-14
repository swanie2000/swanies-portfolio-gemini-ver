package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.CoinMarketResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data model for metal market details, required by AssetViewModel and MetalsAuditScreen.
 */
data class MarketPriceData(
    val current: Double, val high: Double, val low: Double,
    val changePercent: Double, val sparkline: List<Double>
)

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val coinGeckoApiService: CoinGeckoApiService,
    private val syncCoordinator: DataSyncCoordinator,
    private val searchEngineRegistry: SearchEngineRegistry
) {
    val allAssets = assetDao.getAllAssets()

    /**
     * Bridge function to fetch individual metal prices.
     * Restored to maintain Signature Integrity for the ViewModel and Audit Screen.
     */
    suspend fun fetchMarketPrice(symbol: String): MarketPriceData {
        return try {
            val provider = searchEngineRegistry.getMetalProvider()
            // We use the provider's logic to fetch all prices and find the one we need.
            // This maintains the "Firewall" while satisfying the UI's specific request.
            val freshMetals = provider.getPrices("") 
            val match = freshMetals.find { it.baseSymbol == symbol }
            
            if (match != null) {
                MarketPriceData(
                    current = match.currentPrice,
                    high = 0.0, // Provider doesn't currently expose high/low, keeping as 0.0 for now
                    low = 0.0,
                    changePercent = match.priceChange24h,
                    sparkline = match.sparklineData
                )
            } else {
                MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
            }
        } catch (e: Exception) {
            Log.e("AssetRepository", "fetchMarketPrice failed for $symbol: ${e.message}")
            MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
        }
    }

    suspend fun refreshAssets(force: Boolean = false) = coroutineScope {
        if (!syncCoordinator.canRefresh(force)) {
            Log.d("SWAN_SYNC", "Repository: Refresh blocked by Rate Limiter.")
            return@coroutineScope
        }

        syncCoordinator.startSync()
        var success = true

        try {
            val allLocalAssets = allAssets.first()

            // 1. Refresh Crypto
            val cryptoAssets = allLocalAssets.filter { it.category == AssetCategory.CRYPTO }
            if (cryptoAssets.isNotEmpty()) {
                try {
                    val ids = cryptoAssets.joinToString(",") { it.coinId }
                    val provider = searchEngineRegistry.getDefaultProvider()
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
            try {
                val metalProvider = searchEngineRegistry.getMetalProvider()
                val freshMetals = metalProvider.getPrices("")
                val metalDataMap = freshMetals.associateBy { it.baseSymbol }
                
                val metalAssets = allLocalAssets.filter { it.category == AssetCategory.METAL }
                if (metalAssets.isNotEmpty()) {
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
                }
            } catch (e: Exception) {
                Log.e("AssetRepository", "Metals refresh failed: ${e.message}")
                success = false
            }

        } catch (e: Exception) {
            success = false
        } finally {
            syncCoordinator.endSync(success)
        }
    }

    suspend fun fetchLivePriceForAsset(coinId: String): CoinMarketResponse? =
        try { coinGeckoApiService.getCoinMarkets(ids = coinId).firstOrNull() } catch (e: Exception) { null }

    suspend fun saveAsset(asset: AssetEntity) = assetDao.upsertAsset(asset.copy(lastUpdated = System.currentTimeMillis()))
    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAsset(asset.coinId)
    suspend fun updateAssetOrder(assets: List<AssetEntity>) = assetDao.updateAssetOrder(assets)
    suspend fun updateAssetEntity(asset: AssetEntity) = assetDao.updateAssetEntity(asset)
}
