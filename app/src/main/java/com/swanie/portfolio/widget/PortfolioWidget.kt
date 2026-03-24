package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
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
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.swanie.portfolio.MainActivity
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.glance.appwidget.cornerRadius

class PortfolioWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PortfolioWidgetEntryPoint {
        fun assetRepository(): AssetRepository
        fun userConfigDao(): UserConfigDao
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            PortfolioWidgetEntryPoint::class.java
        )
        val repository = entryPoint.assetRepository()
        val userConfigDao = entryPoint.userConfigDao()

        val userConfig = userConfigDao.getUserConfig().first()
        val assets = repository.getAssetsForPortfolio("MAIN").first()
        
        val selectedIds = userConfig?.selectedWidgetAssets?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        
        // Dynamic List & Exact Selection Order
        val filteredAssets = if (selectedIds.isEmpty()) {
            assets.take(10)
        } else {
            selectedIds.mapNotNull { id -> assets.find { it.coinId == id } }.take(10)
        }

        var totalValue = 0.0
        var totalChangeWeighted = 0.0
        var lastSyncTimestamp = 0L

        assets.forEach { asset ->
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

        filteredAssets.forEach { if (it.lastUpdated > lastSyncTimestamp) lastSyncTimestamp = it.lastUpdated }

        val aggregateChangePercent = if (totalValue > 0) totalChangeWeighted / totalValue else 0.0
        
        val showTotal = userConfig?.showWidgetTotal == true
        val displayTotalValue = if (showTotal) {
            NumberFormat.getCurrencyInstance(Locale.US).format(totalValue)
        } else "" // Zero-Mask Stealth
        
        val formattedPercent = "${if (aggregateChangePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", aggregateChangePercent)}%"
        val trendColor = if (aggregateChangePercent >= 0) Color(0xFF00C853) else Color(0xFFFF1744)
        
        val syncTime = if (lastSyncTimestamp > 0) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastSyncTimestamp))
        } else "--:--"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // 4-Tier Color System
        val bgColor = try { Color(android.graphics.Color.parseColor(userConfig?.widgetBgColor ?: "#000000")) } catch (e: Exception) { Color.Black }
        val bgTextColor = try { Color(android.graphics.Color.parseColor(userConfig?.widgetBgTextColor ?: "#FFFFFF")) } catch (e: Exception) { Color.White }
        val cardColor = try { Color(android.graphics.Color.parseColor(userConfig?.widgetCardColor ?: "#1A1C1E")) } catch (e: Exception) { Color(0xFF1A1C1E) }
        val cardTextColor = try { Color(android.graphics.Color.parseColor(userConfig?.widgetCardTextColor ?: "#FFFFFF")) } catch (e: Exception) { Color.White }

        provideContent {
            val size = LocalSize.current
            WidgetContent(
                displayTotalValue, 
                formattedPercent, 
                trendColor, 
                syncTime, 
                filteredAssets, 
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
        totalValue: String, 
        percentChange: String, 
        trendColor: Color, 
        syncTime: String, 
        assets: List<AssetEntity>,
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
                .clickable(actionStartActivity(intent))
        ) {
            // TOP ROW: BRANDING
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                if (showTotal) {
                    Text(
                        text = "SWANIE'S PORTFOLIO PULSE",
                        style = TextStyle(
                            color = ColorProvider(bgTextColor.copy(alpha = 0.4f)),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.defaultWeight())
                Image(
                    provider = ImageProvider(android.R.drawable.ic_popup_sync),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .size(16.dp)
                        .clickable(actionRunCallback<RefreshAction>())
                )
            }

            // HEADER: TOTAL OR CENTERED STEALTH TITLE
            Box(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = if (showTotal) Alignment.CenterStart else Alignment.Center
            ) {
                if (showTotal) {
                    Column {
                        Text(
                            text = totalValue,
                            style = TextStyle(
                                color = ColorProvider(bgTextColor),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "AGGREGATE TREND: $percentChange",
                            style = TextStyle(
                                color = ColorProvider(trendColor),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    Text(
                        text = "SWANIE'S PORTFOLIO PULSE",
                        style = TextStyle(
                            color = ColorProvider(bgTextColor),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
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
                assets.take(maxVisible).forEach { asset ->
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .cornerRadius(12.dp)
                            .background(cardColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        AssetRow(asset, cardTextColor)
                    }
                }
            }
            
            Spacer(modifier = GlanceModifier.defaultWeight())

            // BOTTOM BAR
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Sync: $syncTime",
                    style = TextStyle(
                        color = ColorProvider(bgTextColor.copy(alpha = 0.3f)),
                        fontSize = 8.sp
                    )
                )
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(ColorProvider(trendColor))
            ) {}
        }
    }

    @Composable
    private fun AssetRow(asset: AssetEntity, textColor: Color) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = asset.symbol.uppercase(),
                style = TextStyle(color = ColorProvider(textColor), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.width(45.dp)
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.US).format(asset.officialSpotPrice),
                style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.9f)), fontSize = 11.sp)
            )
            Spacer(modifier = GlanceModifier.width(10.dp))
            val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFFF1744)
            Text(
                text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.1f", asset.priceChange24h)}%",
                style = TextStyle(color = ColorProvider(trendColor), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            )
        }
    }
}

class RefreshAction : ActionCallback {
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
        
        repository.refreshAssets(force = true, portfolioId = "MAIN")
        PortfolioWidget().updateAll(context)
    }
}
