package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.swanie.portfolio.MainActivity
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.data.local.PriceHistoryDao
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.action.actionStartActivity

class PortfolioWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PortfolioWidgetEntryPoint {
        fun assetRepository(): AssetRepository
        fun userConfigDao(): UserConfigDao
        fun priceHistoryDao(): PriceHistoryDao
        fun vaultDao(): VaultDao
        fun assetDao(): AssetDao
        fun themePreferences(): ThemePreferences
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            PortfolioWidgetEntryPoint::class.java
        )
        val repository = entryPoint.assetRepository()
        val userConfigDao = entryPoint.userConfigDao()
        val priceHistoryDao = entryPoint.priceHistoryDao()
        val vaultDao = entryPoint.vaultDao()
        val assetDao = entryPoint.assetDao()
        val themePrefs = entryPoint.themePreferences()

        val userConfig = userConfigDao.getUserConfig().first()
        val currentVaultId = themePrefs.currentVaultId.first()
        val vault = vaultDao.getVaultById(currentVaultId)
        val vaultName = vault?.name ?: "SWANIE"

        // 🌐 GLOBAL VISTA: Pull assets for specific vault
        val allVaultAssets = assetDao.getAssetsByVault(currentVaultId).first()
        
        val selectedIds = userConfig?.selectedWidgetAssets?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        
        val filteredAssets = if (selectedIds.isEmpty()) {
            allVaultAssets.take(10)
        } else {
            selectedIds.mapNotNull { assetId -> allVaultAssets.find { it.coinId == assetId } }.take(10)
        }

        val assetHistoryMap = filteredAssets.associate { asset ->
            asset.coinId to priceHistoryDao.getRecentHistory(asset.coinId).map { it.price }.reversed()
        }

        var totalValue = 0.0
        var totalChangeWeighted = 0.0

        allVaultAssets.forEach { asset ->
            val multiplier = when {
                asset.name.contains("KILO", ignoreCase = true) -> 32.1507
                asset.name.contains("GRAM", ignoreCase = true) -> 0.0321507
                else -> 1.0
            }
            val assetBaseValue = asset.officialSpotPrice * multiplier * asset.weight * asset.amountHeld
            val assetTotalValue = assetBaseValue + asset.premium
            
            totalValue += assetTotalValue
            totalChangeWeighted += (assetBaseValue * asset.priceChange24h)
        }

        val aggregateChangePercent = if (totalValue > 0) totalChangeWeighted / totalValue else 0.0
        val showTotal = userConfig?.showWidgetTotal == true
        val displayTotalValue = if (showTotal) {
            NumberFormat.getCurrencyInstance(Locale.US).format(totalValue)
        } else ""
        
        val formattedPercent = "${if (aggregateChangePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", aggregateChangePercent)}%"
        val trendColor = if (aggregateChangePercent >= 0) Color(0xFF00C853) else Color(0xFFFF1744)
        
        // 🌐 GLOBAL VISTA: Pull lastUpdated timestamp from UserConfig
        val lastSyncTimestamp = userConfig?.lastUpdated ?: 0L
        val syncTime = if (lastSyncTimestamp > 0) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(lastSyncTimestamp))
        } else "--:--"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val bgColor = try { Color(android.graphics.Color.parseColor(userConfig?.widgetBgColor ?: "#000416")) } catch (e: Exception) { Color(0xFF000416) }
        val bgTextColor = try { Color(android.graphics.Color.parseColor(userConfig?.widgetBgTextColor ?: "#FFFFFF")) } catch (e: Exception) { Color.White }
        val cardColor = try { Color(android.graphics.Color.parseColor(userConfig?.widgetCardColor ?: "#363636")) } catch (e: Exception) { Color(0xFF363636) }
        val cardTextColor = try { Color(android.graphics.Color.parseColor(userConfig?.widgetCardTextColor ?: "#C3C3C3")) } catch (e: Exception) { Color(0xFFC3C3C3) }

        provideContent {
            val size = LocalSize.current
            WidgetContent(
                vaultName,
                displayTotalValue, 
                formattedPercent, 
                trendColor, 
                syncTime, 
                filteredAssets, 
                assetHistoryMap,
                intent, 
                showTotal,
                size,
                bgColor,
                bgTextColor,
                cardColor,
                cardTextColor
            )
        }
    }

    @Composable
    private fun WidgetContent(
        vaultName: String,
        totalValue: String, 
        percentChange: String, 
        trendColor: Color, 
        syncTime: String, 
        assets: List<AssetEntity>,
        assetHistoryMap: Map<String, List<Double>>,
        intent: Intent,
        showTotal: Boolean,
        size: DpSize,
        bgColor: Color,
        bgTextColor: Color,
        cardColor: Color,
        cardTextColor: Color
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .padding(bottom = 4.dp)
                .clickable(actionStartActivity(intent))
        ) {
            // TOP ROW: VAULT IDENTITY
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = vaultName.uppercase(),
                    style = TextStyle(
                        color = ColorProvider(bgTextColor.copy(alpha = 0.8f)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Image(
                    provider = ImageProvider(android.R.drawable.ic_popup_sync),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .size(24.dp)
                        .clickable(actionRunCallback<RefreshCallback>())
                )
            }

            // HEADER: CENTERED TOTALS
            if (showTotal) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Text(
                        text = totalValue,
                        style = TextStyle(
                            color = ColorProvider(bgTextColor),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = "TREND: $percentChange",
                        style = TextStyle(
                            color = ColorProvider(trendColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // ASSET LIST
            val maxVisible = if (size.height > 200.dp) 10 else 5
            Column(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                assets.take(maxVisible).forEachIndexed { index, asset ->
                    AssetCard(asset, assetHistoryMap[asset.coinId] ?: emptyList(), cardColor, cardTextColor)
                    if (index < assets.take(maxVisible).size - 1) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
            
            Spacer(modifier = GlanceModifier.defaultWeight())

            // BOTTOM BAR: TIMESTAMP
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "Updated: $syncTime",
                    style = TextStyle(
                        color = ColorProvider(bgTextColor.copy(alpha = 0.4f)),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun AssetCard(asset: AssetEntity, history: List<Double>, cardColor: Color, textColor: Color) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(12.dp)
                .background(cardColor)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // ASSET ICON
            Box(
                modifier = GlanceModifier.size(32.dp).cornerRadius(16.dp).background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                val localPath = asset.localIconPath
                if (localPath != null && File(localPath).exists()) {
                    val bitmap = BitmapFactory.decodeFile(localPath)
                    if (bitmap != null) {
                        Image(
                            provider = ImageProvider(bitmap),
                            contentDescription = asset.symbol,
                            modifier = GlanceModifier.size(32.dp).cornerRadius(16.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        LetterFallback(asset.symbol)
                    }
                } else {
                    LetterFallback(asset.symbol)
                }
            }
            
            Spacer(modifier = GlanceModifier.width(10.dp))

            // NAME FIELD
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = asset.name,
                    style = TextStyle(color = ColorProvider(textColor), fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = asset.symbol.uppercase(),
                    style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.5f)), fontSize = 10.sp),
                    maxLines = 1
                )
            }

            // SPARKLINE
            if (history.size >= 2) {
                val lastPrice = history.last()
                val firstPrice = history.first()
                val sparkColor = if (lastPrice >= firstPrice) Color(0xFF00FF00) else Color(0xFFFF0000)
                
                val sparklineBitmap = SparklineDrawUtils.drawSparklineBitmap(history, sparkColor)
                Image(
                    provider = ImageProvider(sparklineBitmap),
                    contentDescription = "7d Trend",
                    modifier = GlanceModifier.width(120.dp).height(32.dp),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                Spacer(modifier = GlanceModifier.width(120.dp).height(32.dp))
            }

            Spacer(modifier = GlanceModifier.width(12.dp))
            
            // PRICE & TREND
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.US).format(asset.officialSpotPrice),
                    style = TextStyle(color = ColorProvider(textColor), fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                val rowTrendColor = if (asset.priceChange24h >= 0) Color(0xFF00FF00) else Color(0xFFFF0000)
                Text(
                    text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%",
                    style = TextStyle(color = ColorProvider(rowTrendColor), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    @Composable
    private fun LetterFallback(symbol: String) {
        val symbolLetter = symbol.take(1).uppercase()
        Text(
            text = symbolLetter,
            style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
    }
}

class RefreshCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PortfolioWidget.PortfolioWidgetEntryPoint::class.java
        )
        val repository = entryPoint.assetRepository()
        val themePrefs = entryPoint.themePreferences()
        
        val currentVaultId = themePrefs.currentVaultId.first()
        repository.refreshAssets(force = true, portfolioId = "MAIN")
        PortfolioWidget().updateAll(context)
    }
}
