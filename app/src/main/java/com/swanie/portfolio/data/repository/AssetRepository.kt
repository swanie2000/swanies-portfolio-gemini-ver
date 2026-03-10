package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.YahooFinanceApiService
import com.swanie.portfolio.data.network.CoinMarketResponse
import com.swanie.portfolio.data.network.YahooFinanceResponse
import com.swanie.portfolio.data.network.Meta
import com.swanie.portfolio.data.network.ChartResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified Market Data Model for UI consumption.
 */
data class MarketPriceData(
    val current: Double,
    val high: Double,
    val low: Double,
    val changePercent: Double,
    val sparkline: List<Double>
)

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val coinGeckoApiService: CoinGeckoApiService,
    private val yahooApiService: YahooFinanceApiService,
    private val syncCoordinator: DataSyncCoordinator // NEW: The Tower arrives
) {
    val allAssets = assetDao.getAllAssets()

    // Single source of truth for metal symbols mapping to Yahoo tickers
    private val metalTickers = mapOf(
        "XAU" to "GC=F",
        "XAG" to "SI=F",
        "XPT" to "PL=F",
        "XPD" to "PA=F"
    )

    /**
     * Fetches current market data for a given metal symbol (XAU, XAG, etc.) from Yahoo Finance.
     */
    suspend fun fetchMarketPrice(symbol: String): MarketPriceData {
        return try {
            val ticker = metalTickers[symbol] ?: return MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
            val response: YahooFinanceResponse = yahooApiService.getTickerData(ticker)
            val result: ChartResult? = response.chart.result?.firstOrNull()
            val meta: Meta? = result?.meta

            // Extract closing prices for sparkline (filter nulls for stability)
            val closePrices: List<Double> = result?.indicators?.quote?.firstOrNull()?.close?.filterNotNull() ?: emptyList()

            MarketPriceData(
                current = meta?.regularMarketPrice ?: 0.0,
                high = meta?.regularMarketDayHigh ?: 0.0,
                low = meta?.regularMarketDayLow ?: 0.0,
                changePercent = meta?.regularMarketChangePercent ?: 0.0,
                sparkline = closePrices
            )
        } catch (e: Exception) {
            Log.e("AssetRepository", "Fetch failed for $symbol: ${e.message}")
            MarketPriceData(0.0, 0.0, 0.0, 0.0, emptyList())
        }
    }

    /**
     * Synchronizes all holdings with live market data, governed by the SyncCoordinator.
     */
    suspend fun refreshAssets() = coroutineScope {
        // Check with the Tower before proceeding
        if (!syncCoordinator.canRefresh()) {
            Log.d("SWAN_SYNC", "Repository: Refresh blocked by Rate Limiter.")
            return@coroutineScope
        }

        syncCoordinator.startSync()
        var success = true

        try {
            val allLocalAssets = allAssets.first()

            // 1. Refresh Crypto Holdings via CoinGecko
            val cryptoAssets = allLocalAssets.filter { it.category == AssetCategory.CRYPTO }
            if (cryptoAssets.isNotEmpty()) {
                try {
                    val ids = cryptoAssets.joinToString(",") { it.coinId }
                    val marketData = coinGeckoApiService.getCoinMarkets(ids = ids)
                    val dataMap = marketData.associateBy { it.id }
                    val updatedCrypto = cryptoAssets.map { asset ->
                        dataMap[asset.coinId]?.let { fresh ->
                            asset.copy(
                                currentPrice = fresh.currentPrice ?: asset.currentPrice,
                                sparklineData = fresh.sparklineIn7d?.price ?: asset.sparklineData,
                                priceChange24h = fresh.priceChangePercentage24h ?: asset.priceChange24h
                            )
                        } ?: asset
                    }
                    assetDao.upsertAll(updatedCrypto)
                } catch (e: Exception) {
                    Log.e("AssetRepository", "Crypto refresh failed: ${e.message}")
                    success = false
                }
            }

            // 2. Refresh Metal Holdings via Yahoo Finance (Parallel)
            metalTickers.map { (symbol, _) ->
                async {
                    val data = fetchMarketPrice(symbol)
                    if (data.current > 0.0) {
                        val owned = allLocalAssets.filter { it.baseSymbol == symbol }
                        if (owned.isNotEmpty()) {
                            assetDao.upsertAll(owned.map {
                                it.copy(
                                    currentPrice = data.current,
                                    sparklineData = data.sparkline,
                                    priceChange24h = data.changePercent
                                )
                            })
                        }
                    } else {
                        success = false
                    }
                }
            }.awaitAll()

        } catch (e: Exception) {
            Log.e("AssetRepository", "Global refresh failed: ${e.message}")
            success = false
        } finally {
            syncCoordinator.endSync(success)
        }
    }

    /**
     * Search for crypto assets via CoinGecko.
     */
    suspend fun searchCoins(query: String): List<AssetEntity> = try {
        val result = coinGeckoApiService.search(query)
        result.coins.map { coin ->
            AssetEntity(
                coinId = coin.id, symbol = coin.symbol ?: "", name = coin.name ?: "",
                imageUrl = coin.large ?: "", category = AssetCategory.CRYPTO,
                baseSymbol = coin.symbol ?: ""
            )
        }
    } catch (e: Exception) { emptyList() }

    /**
     * Get live price for a specific crypto asset before adding to holdings.
     */
    suspend fun fetchLivePriceForAsset(coinId: String): CoinMarketResponse? =
        try { coinGeckoApiService.getCoinMarkets(ids = coinId).firstOrNull() } catch (e: Exception) { null }

    suspend fun saveAsset(asset: AssetEntity) = assetDao.upsertAsset(asset.copy(lastUpdated = System.currentTimeMillis()))
    suspend fun deleteAsset(asset: AssetEntity) = assetDao.deleteAsset(asset.coinId)
    suspend fun updateAssetOrder(assets: List<AssetEntity>) = assetDao.updateAssetOrder(assets)
    suspend fun updateAssetEntity(asset: AssetEntity) = assetDao.updateAssetEntity(asset)
}