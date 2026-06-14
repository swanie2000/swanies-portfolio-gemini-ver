package com.swanie.portfolio.data.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.swanie.portfolio.billing.MonetizationManager
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetValuation
import com.swanie.portfolio.data.local.IconManager
import com.swanie.portfolio.data.local.PriceHistoryDao
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.widget.PortfolioWidget
import com.swanie.portfolio.widget.PortfolioWidgetReceiver
import com.swanie.portfolio.widget.SparklineDrawUtils
import com.swanie.portfolio.widget.WidgetAssetLimits
import com.swanie.portfolio.widget.appWidgetIdsForPortfolioVault
import com.swanie.portfolio.widget.resolveVaultForAppWidgetId
import com.swanie.portfolio.widget.writeWidgetPackedAssetRows
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Singleton
class AssetRepository @Inject constructor(
    private val context: Context,
    private val assetDao: AssetDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val userConfigDao: UserConfigDao,
    private val vaultDao: VaultDao,
    private val searchRegistry: SearchEngineRegistry,
    private val syncCoordinator: DataSyncCoordinator,
    private val iconManager: IconManager,
    private val monetizationManager: MonetizationManager,
) {
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssetsFlow()

    fun getAssetsForPortfolio(portfolioId: String) = assetDao.getAssetsByPortfolio(portfolioId)

    private fun cleanMetalName(rawName: String, symbol: String, weight: Double, unit: String): String {
        val upperSymbol = symbol.uppercase(Locale.ROOT)
        val upperName = rawName.uppercase(Locale.ROOT)

        val metalType = when {
            upperSymbol == "XAU" || upperSymbol.contains("GC=F") || upperName.contains("GOLD") -> "Gold"
            upperSymbol == "XAG" || upperSymbol.contains("SI=F") || upperSymbol == "SILVER" || upperName.contains("SILVER") -> "Silver"
            upperSymbol == "XPT" || upperSymbol.contains("PL=F") || upperName.contains("PLATINUM") -> "Platinum"
            upperSymbol == "XPD" || upperSymbol.contains("PA=F") || upperName.contains("PALLADIUM") -> "Palladium"
            else -> symbol.replace("=F", "").trim().ifBlank { "Metal" }
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

    /** CryptoCompare prices are keyed by ticker; apiId may be stale (e.g. CG_ from old healMetadata). */
    private fun providerPriceQueryId(asset: AssetEntity): String = when (asset.priceSource) {
        "CryptoCompare" -> asset.symbol
        else -> asset.apiId
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
                val idString = providerAssets.joinToString(",") { providerPriceQueryId(it) }
                val updatedList = try { provider.getPrices(idString) } catch (e: Exception) { emptyList() }

                val updatesToSave = providerAssets.map { existing ->
                    val update = updatedList.find {
                        it.apiId.equals(existing.apiId, true) || it.symbol.equals(existing.symbol, true)
                    }
                    if (update != null) {
                        val isMetal = existing.category == AssetCategory.METAL
                        val finalDisplayName = if (isMetal) {
                            if (existing.displayName.isNotBlank()) {
                                existing.displayName.trim()
                            } else {
                                cleanMetalName(update.name.ifEmpty { existing.name }, existing.symbol, existing.weight, existing.weightUnit)
                            }
                        } else {
                            update.name.ifEmpty { existing.name }
                        }

                        existing.copy(
                            name = if (isMetal && existing.displayName.isNotBlank()) {
                                existing.name.ifBlank { update.name.ifEmpty { existing.name } }
                            } else {
                                update.name.ifEmpty { existing.name }
                            },
                            displayName = finalDisplayName,
                            isMetal = isMetal,
                            apiId = if (existing.priceSource == "CryptoCompare") {
                                "CC_${existing.symbol.uppercase(Locale.ROOT)}"
                            } else {
                                existing.apiId
                            },
                            officialSpotPrice = update.officialSpotPrice,
                            priceChange24h = update.priceChange24h,
                            sparklineData = if (update.sparklineData.isNotEmpty()) update.sparklineData else existing.sparklineData,
                            imageUrl = update.imageUrl.ifBlank { existing.imageUrl },
                            iconUrl = (update.iconUrl ?: update.imageUrl ?: "").ifBlank { existing.iconUrl },
                            lastUpdated = System.currentTimeMillis()
                        )
                    } else {
                        null
                    }
                }.filterNotNull()
                
                if (updatesToSave.isNotEmpty()) {
                    val merged = updatesToSave.map { update ->
                        val current = assetDao.getAssetByCoinId(update.coinId)
                        if (current == null) {
                            update
                        } else {
                            update.copy(
                                localIconPath = iconManager.resolvedCustomIconPath(
                                    current.coinId,
                                    current.localIconPath,
                                ),
                                displayName = current.displayName.ifBlank { update.displayName },
                                name = if (current.displayName.isNotBlank()) current.name else update.name,
                                weight = current.weight,
                                weightUnit = current.weightUnit,
                                physicalForm = current.physicalForm,
                                amountHeld = current.amountHeld,
                                premium = current.premium,
                                decimalPreference = current.decimalPreference,
                            )
                        }
                    }
                    assetDao.upsertAll(merged)
                }
            }
            isSuccess = true
            userConfigDao.updateLastSync(System.currentTimeMillis())

            val freshForWidget = if (portfolioId.all { it.isDigit() }) {
                assetDao.getAssetsByVaultOnce(portfolioId.toInt())
            } else {
                assetDao.getAllAssetsOnce(portfolioId)
            }
            pushFreshAssetsToWidget(context.applicationContext, portfolioId, freshForWidget)
        } catch (e: Exception) {
            Log.e("REPO_REFRESH", "Error: ${e.message}")
            isSuccess = false
        } finally {
            syncCoordinator.endSync(isSuccess)
        }
    }

    /** Push latest DB assets to one widget instance (e.g. refresh tap on a specific Glance id). */
    suspend fun pushAssetsToGlance(context: Context, glanceId: GlanceId) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        val vaultId = prefs[PortfolioWidget.VAULT_ID_KEY]?.takeIf { it > 0 } ?: return
        val assets = assetDao.getAssetsByVaultOnce(vaultId)
        pushFreshAssetsToWidget(context, vaultId.toString(), assets, targetGlanceId = glanceId)
    }

    suspend fun pushFreshAssetsToWidget(
        context: Context,
        portfolioId: String,
        freshAssets: List<AssetEntity>,
        targetGlanceId: GlanceId? = null,
    ) {
        val glanceManager = GlanceAppWidgetManager(context)

        val vaultIdInt = portfolioId.toIntOrNull() ?: 1
        if (vaultDao.getVaultById(vaultIdInt) == null) return

        val targetWidgetId = targetGlanceId?.let { glanceManager.getAppWidgetId(it) }
        val idsToUpdate = appWidgetIdsForPortfolioVault(context, vaultIdInt, vaultDao, targetWidgetId)

        if (idsToUpdate.isEmpty()) return

        idsToUpdate.forEach { id ->
            val vSafe = resolveVaultForAppWidgetId(context, id, vaultDao) ?: return@forEach
            if (vSafe.id != vaultIdInt) return@forEach

            val glanceId = try { glanceManager.getGlanceIdBy(id) } catch (e: Exception) { null } ?: return@forEach
            val assetsForVault = assetDao.getAssetsByVaultOnce(vSafe.id)

            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[PortfolioWidget.VAULT_ID_KEY] = vSafe.id
                    this[PortfolioWidget.STATIC_VAULT_NAME_KEY] = vSafe.name
                    this[PortfolioWidget.WIDGET_BG_COLOR_KEY] = vSafe.widgetBgColor
                    this[PortfolioWidget.WIDGET_BG_TEXT_COLOR_KEY] = vSafe.widgetBgTextColor
                    this[PortfolioWidget.WIDGET_CARD_COLOR_KEY] = vSafe.widgetCardColor
                    this[PortfolioWidget.WIDGET_CARD_TEXT_COLOR_KEY] = vSafe.widgetCardTextColor
                    this[PortfolioWidget.SHOW_TOTAL_KEY] = vSafe.showWidgetTotal

                    val entitlement = monetizationManager.entitlement.value
                    val tierCap = WidgetAssetLimits.capFor(entitlement)
                    this[PortfolioWidget.IS_PRO_USER_KEY] = WidgetAssetLimits.isProForWidget(entitlement)

                    val selectedIds = vSafe.selectedWidgetAssets.split(",").filter { it.isNotBlank() }
                    
                    // 🚀 SEQUENTIAL ORDER FIX: Map the selectedIds list to maintain user numbering/sorting
                    val filteredAssets = if (selectedIds.isEmpty()) {
                        assetsForVault.filter { it.portfolioId == vSafe.id.toString() || it.portfolioId == "MAIN" }
                            .take(tierCap)
                    } else {
                        selectedIds.take(tierCap).mapNotNull { coinId ->
                            assetsForVault.find { it.coinId == coinId }
                        }
                    }

                    val rowLines = filteredAssets.map { asset ->
                        val iconSource = when {
                            // Custom metal (or any) photo icons must win over packaged res:ic_* metal art.
                            asset.imageUrl.startsWith("file:") -> asset.imageUrl
                            asset.localIconPath != null -> "file:${asset.localIconPath}"
                            asset.category == AssetCategory.METAL || asset.isMetal -> "__METAL_DEFAULT__"
                            else -> asset.imageUrl
                        }
                        val assetValue = AssetValuation.holdingValueUsd(asset)
                        val formattedTotal = formatBoutiquePrice(assetValue)
                        
                        // Use history table first, then provider sparkline as fallback.
                        val history = try { priceHistoryDao.getRecentHistory(asset.coinId).map { it.price }.reversed() } catch (e: Exception) { emptyList() }
                        val sparklinePoints = when {
                            history.size >= 2 -> history
                            asset.sparklineData.size >= 2 -> asset.sparklineData
                            else -> emptyList()
                        }
                        val sparklinePath = if (sparklinePoints.size >= 2) {
                            val color = if (asset.priceChange24h >= 0) androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red
                            val bitmap = SparklineDrawUtils.drawSparklineBitmap(sparklinePoints, color)
                            persistWidgetSparkline(asset.coinId, bitmap) ?: "none"
                        } else "none"

                        val safeSymbol = asset.symbol.replace("|", " ").replace("\n", "").trim()
                        val safeDisplayName = (asset.displayName.ifBlank { asset.name }).replace("|", " ").replace("\n", "").trim()
                        
                        // 🎯 DYNAMIC PRECISION: Bulletproof Boutique Formatter (per-line spot for metals)
                        val linePrice = AssetValuation.cardPriceRowUsd(asset)
                        val formattedPrice = formatBoutiquePrice(linePrice)
                        Log.d("SWANIE_PRECISION", "Asset: $safeSymbol | Line price: $linePrice | Formatted: $formattedPrice")
                        
                        "${asset.coinId}|$safeSymbol|$safeDisplayName|$iconSource|$formattedPrice|${asset.priceChange24h}|${asset.weight}|${asset.amountHeld}|$formattedTotal|$sparklinePath"
                    }

                    if (rowLines.isEmpty() && selectedIds.isNotEmpty()) {
                        Log.w(
                            "SWANIE_WIDGET",
                            "Skipping empty widget push for widget $id vault ${vSafe.id} — selection not in vault assets",
                        )
                        return@updateAppWidgetState prefs
                    }

                    // 🚀 ANTI-GHOSTING: Always update the state, even if empty, to ensure deleted assets are cleared from view
                    writeWidgetPackedAssetRows(rowLines)
                    
                    this[PortfolioWidget.LAST_UPDATED_KEY] = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                    this[PortfolioWidget.FORCE_UPDATE_KEY] = System.currentTimeMillis()
                    val total = assetDao.getAssetsByVaultOnce(vSafe.id).sumOf { AssetValuation.holdingValueUsd(it) }
                    this[PortfolioWidget.STATIC_TOTAL_BALANCE_KEY] = NumberFormat.getCurrencyInstance(Locale.US).format(total)
                }.toPreferences()
            }
            PortfolioWidget().update(context, glanceId)
        }
    }

    suspend fun upsertAsset(asset: AssetEntity) {
        val isMetal = asset.category == AssetCategory.METAL
        // Architect / funnel set a human label on displayName — do not replace it with cleanMetalName().
        val finalDisplayName = if (isMetal) {
            if (asset.displayName.isNotBlank()) {
                asset.displayName.trim()
            } else {
                cleanMetalName(asset.name, asset.symbol, asset.weight, asset.weightUnit)
            }
        } else {
            asset.name
        }

        val finalName = if (isMetal) {
            when {
                asset.displayName.isNotBlank() -> asset.displayName.trim()
                asset.name.isNotBlank() -> asset.name.trim()
                else -> finalDisplayName
            }
        } else {
            asset.name
        }

        val finalForm = if (asset.physicalForm.contains("Bar", true)) "Bar" else asset.physicalForm

        val sanitizedAsset = asset.copy(
            name = finalName,
            displayName = finalDisplayName,
            isMetal = isMetal,
            physicalForm = finalForm,
            portfolioId = if (asset.portfolioId.isEmpty()) "MAIN" else asset.portfolioId
        )
        assetDao.upsertAsset(sanitizedAsset)
    }

    suspend fun healMetadata(asset: AssetEntity): AssetEntity {
        if (asset.priceSource == "CoinGecko"
            || asset.priceSource == "CryptoCompare"
            || asset.category == AssetCategory.METAL) return asset
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
            val updates = provider.getPrices(providerPriceQueryId(asset))
            val liveMatch = updates.find { it.apiId.equals(asset.apiId, true) || it.symbol.equals(asset.symbol, true) }
            if (liveMatch != null) {
                asset.copy(
                    officialSpotPrice = liveMatch.officialSpotPrice,
                    priceChange24h = liveMatch.priceChange24h,
                    sparklineData = if (liveMatch.sparklineData.isNotEmpty()) liveMatch.sparklineData else asset.sparklineData,
                    imageUrl = liveMatch.imageUrl.ifBlank { asset.imageUrl },
                    iconUrl = (liveMatch.iconUrl ?: liveMatch.imageUrl ?: "").ifBlank { asset.iconUrl },
                    lastUpdated = System.currentTimeMillis()
                )
            } else asset
        } catch (e: Exception) { asset }
    }

    suspend fun deleteAsset(asset: AssetEntity) {
        iconManager.deleteCustomAssetIcon(asset.coinId)
        assetDao.deleteAssetById(asset.coinId)
        // 🧹 CASCADE CLEANUP: Remove from Widget Selection across all vaults
        cleanAssetFromWidgetSelection(asset.coinId)
        // 🚀 IMMEDIATE UPDATE: Force widget refresh to remove ghost
        pushAssetsToWidget(context, asset.vaultId.toString())
    }

    suspend fun deleteAsset(id: String) {
        // Fetch asset info before deletion to know which vault to update
        val allAssets = assetDao.getAllAssetsGlobal()
        val asset = allAssets.find { it.coinId == id }

        iconManager.deleteCustomAssetIcon(id)
        assetDao.deleteAssetById(id)
        // 🧹 CASCADE CLEANUP: Remove from Widget Selection across all vaults
        cleanAssetFromWidgetSelection(id)

        // 🚀 IMMEDIATE UPDATE: Force widget refresh
        asset?.let {
            pushAssetsToWidget(context, it.vaultId.toString())
        }
    }

    private suspend fun cleanAssetFromWidgetSelection(coinId: String) {
        try {
            val vaults = vaultDao.getAllVaults()
            vaults.forEach { vault ->
                val currentIds = vault.selectedWidgetAssets.split(",").filter { it.isNotBlank() }
                if (coinId in currentIds) {
                    val newIds = currentIds.filter { it != coinId }.joinToString(",")
                    vaultDao.updateSelectedWidgetAssets(vault.id, newIds)
                    Log.d("REPO_CLEANUP", "Removed ghost asset $coinId from vault ${vault.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("REPO_CLEANUP", "Failed to clean widget selection: ${e.message}")
        }
    }

    suspend fun updateAssetOrder(assets: List<AssetEntity>) { assetDao.updateAssetOrder(assets) }

    /** Move [coinId] to the top of [vaultId] holdings list (displayOrder 0). */
    suspend fun prependAssetToVaultTop(vaultId: Int, coinId: String) {
        val assets = assetDao.getAssetsByVaultOnce(vaultId).sortedBy { it.displayOrder }
        val target = assets.find { it.coinId == coinId } ?: return
        val reordered = listOf(target) + assets.filter { it.coinId != coinId }
        assetDao.updateAssetOrder(reordered)
    }
    suspend fun updateAssetEntity(asset: AssetEntity) {
        val resolvedIcon = iconManager.resolvedCustomIconPath(asset.coinId, asset.localIconPath)
        assetDao.updateAssetEntity(
            asset.copy(
                localIconPath = resolvedIcon,
                displayName = asset.displayName.ifBlank {
                    assetDao.getAssetByCoinId(asset.coinId)?.displayName.orEmpty()
                },
            ),
        )
    }
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

    // This method is now legacy, replaced by pushFreshAssetsToWidget for better data consistency
    suspend fun pushAssetsToWidget(context: Context, portfolioId: String) {
        val assets = if (portfolioId.all { it.isDigit() }) {
            assetDao.getAssetsByVaultOnce(portfolioId.toInt())
        } else {
            assetDao.getAllAssetsOnce(portfolioId)
        }
        pushFreshAssetsToWidget(context, portfolioId, assets)
    }

    private fun persistWidgetSparkline(coinId: String, bitmap: Bitmap): String? {
        return try {
            val dir = File(context.filesDir, "widget_sparklines")
            if (!dir.exists() && !dir.mkdirs()) return null
            val target = File(dir, "spark_$coinId.png")
            val tmp = File(dir, "spark_$coinId.tmp")
            FileOutputStream(tmp).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            if (!tmp.renameTo(target)) {
                tmp.copyTo(target, overwrite = true)
                tmp.delete()
            }
            target.absolutePath
        } catch (e: Exception) {
            Log.w("SWANIE_WIDGET", "Failed to persist sparkline for $coinId: ${e.message}")
            null
        }
    }

    private fun formatBoutiquePrice(price: Double): String {
        return when {
            price >= 0.10 -> String.format(Locale.US, "%.2f", price)
            price >= 0.0001 -> String.format(Locale.US, "%.5f", price)
            else -> String.format(Locale.US, "%.8f", price)
        }
    }
}
