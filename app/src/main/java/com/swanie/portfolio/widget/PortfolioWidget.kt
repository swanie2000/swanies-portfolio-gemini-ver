package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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

class PortfolioWidget : GlanceAppWidget() {

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
        val filteredAssets = if (selectedIds.isEmpty()) {
            assets.take(3)
        } else {
            assets.filter { selectedIds.contains(it.coinId) }.take(3)
        }

        var totalValue = 0.0
        var totalChangeWeighted = 0.0
        var lastSyncTimestamp = 0L

        // Calculate based on ALL assets in portfolio for accurate "Pulse"
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

        // Pull timestamp from the actual filtered list for "Top 3" accuracy
        filteredAssets.forEach { if (it.lastUpdated > lastSyncTimestamp) lastSyncTimestamp = it.lastUpdated }

        val aggregateChangePercent = if (totalValue > 0) totalChangeWeighted / totalValue else 0.0
        val displayTotalValue = if (userConfig?.showWidgetTotal == true) {
            NumberFormat.getCurrencyInstance(Locale.US).format(totalValue)
        } else {
            "••••••••"
        }
        
        val formattedPercent = "${if (aggregateChangePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", aggregateChangePercent)}%"
        val trendColor = if (aggregateChangePercent >= 0) Color(0xFF00C853) else Color(0xFFFF1744)
        
        val syncTime = if (lastSyncTimestamp > 0) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastSyncTimestamp))
        } else "--:--"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        provideContent {
            WidgetContent(displayTotalValue, formattedPercent, trendColor, syncTime, filteredAssets, intent)
        }
    }

    @Composable
    private fun WidgetContent(
        totalValue: String, 
        percentChange: String, 
        trendColor: Color, 
        syncTime: String, 
        topAssets: List<AssetEntity>,
        intent: Intent
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(com.swanie.portfolio.R.drawable.widget_background))
                .clickable(actionStartActivity(intent))
        ) {
            // TOP ROW: BRANDING & REFRESH
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "SWANIE'S PORTFOLIO PULSE",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.4f)),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Image(
                    provider = ImageProvider(android.R.drawable.ic_popup_sync),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .size(16.dp)
                        .clickable(actionRunCallback<RefreshAction>())
                )
            }

            // CENTER: PORTFOLIO PULSE
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = totalValue,
                        style = TextStyle(
                            color = ColorProvider(Color.Yellow),
                            fontSize = 20.sp,
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
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // ASSET LIST: TOP 3 SELECTED
            Column(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                topAssets.forEach { asset ->
                    AssetRow(asset)
                }
            }
            
            Spacer(modifier = GlanceModifier.defaultWeight())

            // BOTTOM: TIMESTAMP & PULSE BAR
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Last Sync: $syncTime",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.3f)),
                        fontSize = 8.sp
                    )
                )
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorProvider(trendColor))
            ) {}
        }
    }

    @Composable
    private fun AssetRow(asset: AssetEntity) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = asset.symbol.uppercase(),
                style = TextStyle(color = ColorProvider(Color.White), fontSize = 10.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.width(40.dp)
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.US).format(asset.officialSpotPrice),
                style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.8f)), fontSize = 10.sp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            val color = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFFF1744)
            Text(
                text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.1f", asset.priceChange24h)}%",
                style = TextStyle(color = ColorProvider(color), fontSize = 10.sp, fontWeight = FontWeight.Medium)
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
        
        // REAL SYNC: Trigger force-refresh
        repository.refreshAssets(force = true, portfolioId = "MAIN")
        
        // REFRESH UI
        PortfolioWidget().updateAll(context)
    }
}
