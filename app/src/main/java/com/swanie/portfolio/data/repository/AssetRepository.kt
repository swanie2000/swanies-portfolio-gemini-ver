package com.swanie.portfolio.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.PriceHistoryDao
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.widget.SparklineDrawUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.abs

@Singleton
class AssetRepository @Inject constructor(
    private val context: Context,
    private val assetDao: AssetDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val userConfigDao: UserConfigDao,
    private val searchRegistry: SearchEngineRegistry,
    private val syncCoordinator: DataSyncCoordinator
) {
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssetsFlow()

    fun getAssetsForPortfolio(portfolioId: String) = assetDao.getAssetsByPortfolio(portfolioId)

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
            val assets = if (portfolioId.all { it.isDigit() }) {
                assetDao.getAssetsByVaultOnce(portfolioId.toInt())
            } else {
                assetDao.getAllAssetsOnce(portfolioId)
            }
            if (assets.isEmpty()) {
                isSuccess = true
                return
            }

            assets.groupBy { it.priceSource }.forEach { (sourceName, providerAssets) ->
                val provider = searchRegistry.getProvider(sourceName) ?: return@forEach
                val idString = providerAssets.joinToString(",") { it.apiId }
                val updatedList = provider.getPrices(idString)

                val updatesToSave = providerAssets.mapNotNull { existing ->
                    updatedList.find {
                        it.apiId.equals(existing.apiId, true) || it.symbol.equals(existing.symbol, true)
                    }?.let { update ->
                        val isMetal = existing.category == AssetCategory.METAL

                        val finalDisplayName = if (isMetal) {
                            cleanMetalName(update.name.ifEmpty { existing.name }, existing.symbol, existing.weight, existing.weightUnit)
                        } else {
                            update.name.ifEmpty { existing.name }
                        }

                        existing.copy(
                            name = update.name.ifEmpty { existing.name },
                            displayName = finalDisplayName,
                            isMetal = isMetal,
                            officialSpotPrice = update.officialSpotPrice,
                            priceChange24h = update.priceChange24h,
                            sparklineData = if (update.sparklineData.isNotEmpty()) update.sparklineData else existing.sparklineData,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                }
                
                if (updatesToSave.isNotEmpty()) {
                    assetDao.upsertAll(updatesToSave)
                }
            }
            isSuccess = true
            userConfigDao.updateLastSync(System.currentTimeMillis())
            
            // 🚀 DATABASE-FIRST REFRESH: Push to widget ONLY after DB write is confirmed
            // Add a small delay to ensure SQLite WAL is fully committed
            kotlinx.coroutines.delay(500)
            pushAssetsToWidget(context, portfolioId)
        } catch (e: Exception) {
            Log.e("REPO_REFRESH", "Error: ${e.message}")
            isSuccess = false
        } finally {
            syncCoordinator.endSync(isSuccess)
        }
    }

    suspend fun upsertAsset(asset: AssetEntity) {
        val isMetal = asset.category == AssetCategory.METAL
        val finalDisplayName = if (isMetal) {
            cleanMetalName(asset.name, asset.symbol, asset.weight, asset.weightUnit)
        } else {
            asset.name
        }

        val finalForm = if (asset.physicalForm.contains("Bar", true)) "Bar" else asset.physicalForm

        val sanitizedAsset = asset.copy(
            displayName = finalDisplayName,
            isMetal = isMetal,
            physicalForm = finalForm,
            portfolioId = if (asset.portfolioId.isEmpty()) "MAIN" else asset.portfolioId
        )
        assetDao.upsertAsset(sanitizedAsset)
    }

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

    suspend fun pushAssetsToWidget(context: android.content.Context, portfolioId: String) {
        val finalId = if (portfolioId == "0" || portfolioId.isBlank()) "1" else portfolioId
        try {
            var assets = if (finalId.all { it.isDigit() }) {
                assetDao.getAssetsByVaultOnce(finalId.toInt())
            } else {
                assetDao.getAllAssetsOnce(finalId)
            }

            if (assets.isEmpty()) {
                Log.d("SWANIE_PIPE", "DB Fetch empty for portfolioId: $finalId. Trying global...")
                assets = assetDao.getAllAssetsGlobal()
            }

            Log.d("SWANIE_PIPE", "DB Fetch Success: ${assets.size} assets found for ID: $finalId. Top Price: ${assets.firstOrNull()?.officialSpotPrice}")

            // 🛡️ Packing the "Suitcase" (Limit to 8 to prevent Binder Transaction limit crashes)
            // Format: coinId|symbol|displayName|imageUrl|officialSpotPrice|priceChange24h|weight|amountHeld|calculatedTotal|sparklinePath
            val assetsData = assets.filter { it.showOnWidget }.take(5).map { asset ->
                Log.d("SWANIE_PIPE", "Packing asset: ${asset.symbol} | Price: ${asset.officialSpotPrice}")
                val calculatedTotal = (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
                
                val iconSource = when {
                    asset.category == AssetCategory.METAL || asset.isMetal -> "res:ic_${asset.symbol.lowercase()}"
                    asset.imageUrl.startsWith("file:") -> asset.imageUrl
                    asset.localIconPath != null -> "file:${asset.localIconPath}"
                    else -> asset.imageUrl
                }

                // 📈 Generate Sparkline for Widget
                val sparklinePath = try {
                    val history = priceHistoryDao.getRecentHistory(asset.coinId).map { it.price }.reversed()
                    if (history.size >= 2) {
                        val color = if (asset.priceChange24h >= 0) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
                        val bitmap = SparklineDrawUtils.drawSparklineBitmap(history, color)
                        val file = File(context.cacheDir, "spark_${asset.coinId}.png")
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        file.absolutePath
                    } else "none"
                } catch (e: Exception) {
                    Log.e("SWANIE_PIPE", "Sparkline failed for ${asset.symbol}", e)
                    "none"
                }

                val safeSymbol = asset.symbol.replace("|", " ").replace("\n", "").trim()
                val safeDisplayName = (asset.displayName.ifBlank { asset.name }).replace("|", " ").replace("\n", "").trim()

                "${asset.coinId}|$safeSymbol|$safeDisplayName|$iconSource|${asset.officialSpotPrice}|${asset.priceChange24h}|${asset.weight}|${asset.amountHeld}|$calculatedTotal|$sparklinePath"
            }.joinToString("||")

            if (assetsData.isBlank() && assets.isNotEmpty()) {
                Log.d("SWANIE_PIPE", "Suitcase is blank but assets found. Emergency Pack triggered.")
                // EMERGENCY PACK: No sparklines, just raw data
                val emergencyData = assets.filter { it.showOnWidget }.take(5).map { asset ->
                    val calculatedTotal = (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
                    val iconSource = if (asset.category == AssetCategory.METAL || asset.isMetal) "res:ic_${asset.symbol.lowercase()}" else asset.imageUrl
                    val safeSymbol = asset.symbol.replace("|", " ").replace("\n", "").trim()
                    val safeDisplayName = (asset.displayName.ifBlank { asset.name }).replace("|", " ").replace("\n", "").trim()
                    "${asset.coinId}|$safeSymbol|$safeDisplayName|$iconSource|${asset.officialSpotPrice}|${asset.priceChange24h}|${asset.weight}|${asset.amountHeld}|$calculatedTotal|none"
                }.joinToString("||")
                
                if (emergencyData.isNotBlank()) {
                    pushFinalSuitcase(context, emergencyData)
                }
                return
            }

            if (assetsData.isBlank()) {
                Log.d("SWANIE_PIPE", "Suitcase is blank and no assets found, aborting push.")
                return
            }

            Log.d("SWANIE_PIPE", "Suitcase Packed: $assetsData")
            pushFinalSuitcase(context, assetsData)
        } catch (e: Exception) {
            Log.e("SWANIE_WIDGET", "Push failed", e)
        }
    }

    private suspend fun pushFinalSuitcase(context: android.content.Context, assetsData: String) {
        Log.d("SWANIE_SYNC", "Pushing to Glance: $assetsData")
        val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(com.swanie.portfolio.widget.PortfolioWidget::class.java)

        glanceIds.forEach { id ->
            androidx.glance.appwidget.state.updateAppWidgetState(context, androidx.glance.state.PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this.remove(com.swanie.portfolio.widget.PortfolioWidget.ASSETS_DATA_KEY)
                    this[com.swanie.portfolio.widget.PortfolioWidget.ASSETS_DATA_KEY] = assetsData
                    this[com.swanie.portfolio.widget.PortfolioWidget.LAST_UPDATED_KEY] = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                }.toPreferences()
            }
            com.swanie.portfolio.widget.PortfolioWidget().update(context, id)
        }
    }
}