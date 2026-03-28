package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.UserConfigDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import kotlin.math.abs

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val userConfigDao: UserConfigDao,
    private val searchRegistry: SearchEngineRegistry,
    private val syncCoordinator: DataSyncCoordinator
) {
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssetsFlow()

    fun getAssetsForPortfolio(portfolioId: String) = assetDao.getAssetsByPortfolio(portfolioId)

    /**
     * Phase 1: Data Sanitization Utility (V18 - Precision Force)
     * Now leverages explicit weightUnit for perfect labels.
     */
    private fun cleanMetalName(rawName: String, symbol: String, weight: Double, unit: String): String {
        val upperSymbol = symbol.uppercase(Locale.ROOT)
        val upperName = rawName.uppercase(Locale.ROOT)

        val metalType = when {
            upperSymbol.contains("GC=F") || upperName.contains("GOLD") -> "Gold"
            upperSymbol.contains("SI=F") || upperSymbol == "SILVER" || upperName.contains("SILVER") -> "Silver"
            upperSymbol.contains("PL=F") || upperName.contains("PLATINUM") -> "Platinum"
            upperSymbol.contains("PA=F") || upperName.contains("PALLADIUM") -> "Palladium"
            else -> symbol.replace("=F", "")
        }

        // 🛠️ V18: TRUST EXPLICIT UNIT FIRST
        val unitLabel = when (unit.uppercase(Locale.ROOT)) {
            "KILO" -> "(1kg)"
            "GRAM" -> "(1g)"
            "OZ" -> {
                when {
                    abs(weight - 100.0) < 0.001 -> "(100oz)"
                    abs(weight - 10.0) < 0.001 -> "(10oz)"
                    abs(weight - 1.0) < 0.001 -> "(1oz)"
                    abs(weight - 0.1) < 0.001 -> "(1/10oz)"
                    else -> ""
                }
            }
            else -> ""
        }

        return if (unitLabel.isEmpty()) metalType else "$metalType $unitLabel"
    }

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
                val updatedList = provider.getPrices(idString)

                providerAssets.forEach { existing ->
                    updatedList.find {
                        it.apiId.equals(existing.apiId, true) || it.symbol.equals(existing.symbol, true)
                    }?.let { update ->
                        val isMetal = existing.category == AssetCategory.METAL

                        // 🛠️ V18: High-precision label refresh
                        val finalDisplayName = if (isMetal) {
                            cleanMetalName(update.name.ifEmpty { existing.name }, existing.symbol, existing.weight, existing.weightUnit)
                        } else {
                            update.name.ifEmpty { existing.name }
                        }

                        assetDao.upsertAsset(existing.copy(
                            name = update.name.ifEmpty { existing.name },
                            displayName = finalDisplayName,
                            isMetal = isMetal,
                            officialSpotPrice = update.officialSpotPrice,
                            priceChange24h = update.priceChange24h,
                            sparklineData = if (update.sparklineData.isNotEmpty()) update.sparklineData else existing.sparklineData,
                            lastUpdated = System.currentTimeMillis()
                        ))
                    }
                }
            }
            isSuccess = true
            userConfigDao.updateLastSync(System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e("REPO_REFRESH", "Error: ${e.message}")
            isSuccess = false
        } finally {
            syncCoordinator.endSync(isSuccess)
        }
    }

    suspend fun upsertAsset(asset: AssetEntity) {
        val isMetal = asset.category == AssetCategory.METAL
        // 🛠️ V18: FORCE SANITIZATION ON EVERY UPSERT
        val finalDisplayName = if (isMetal) {
            cleanMetalName(asset.name, asset.symbol, asset.weight, asset.weightUnit)
        } else {
            asset.name
        }

        // 🛡️ V18: SCHEMA HARDENING - Sanitize the physicalForm string
        val finalForm = if (asset.physicalForm.contains("Bar", true)) "Bar" else asset.physicalForm

        val sanitizedAsset = asset.copy(
            displayName = finalDisplayName,
            isMetal = isMetal,
            // 🛡️ V18: Saving the sanitized form
            physicalForm = finalForm,
            portfolioId = if (asset.portfolioId.isEmpty()) "MAIN" else asset.portfolioId
        )
        assetDao.upsertAsset(sanitizedAsset)
    }

    // --- Helpers (No changes) ---
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
