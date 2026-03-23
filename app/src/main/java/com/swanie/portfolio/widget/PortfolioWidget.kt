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
        val filteredAssets = if (selectedIds.isEmpty()) assets else assets.filter { selectedIds.contains(it.coinId) }

        var totalValue = 0.0
        var totalChangeWeighted = 0.0
        var lastSyncTimestamp = 0L

        filteredAssets.forEach { asset ->
            val multiplier = when {
                asset.name.contains("KILO", ignoreCase = true) -> 32.1507
                asset.name.contains("GRAM", ignoreCase = true) -> 0.0321507
                else -> 1.0
            }
            val assetBaseValue = asset.officialSpotPrice * multiplier * asset.weight * asset.amountHeld
            val assetTotalValue = assetBaseValue + asset.premium
            
            totalValue += assetTotalValue
            totalChangeWeighted += (assetBaseValue * asset.priceChange24h)
            if (asset.lastUpdated > lastSyncTimestamp) {
                lastSyncTimestamp = asset.lastUpdated
            }
        }

        val aggregateChangePercent = if (totalValue > 0) totalChangeWeighted / totalValue else 0.0
        
        // PRIVACY LOCK: Check userConfig for permission to show total
        val displayValue = if (userConfig?.showWidgetTotal == true) {
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
            WidgetContent(displayValue, formattedPercent, trendColor, syncTime, intent)
        }
    }

    @Composable
    private fun WidgetContent(totalValue: String, percentChange: String, trendColor: Color, syncTime: String, intent: Intent) {
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
                        fontSize = 8.sp, // Slightly smaller for longer name
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

            // MIDDLE: VALUE & TREND
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = totalValue,
                    style = TextStyle(
                        color = ColorProvider(Color.Yellow),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = percentChange,
                    style = TextStyle(
                        color = ColorProvider(trendColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            // BOTTOM: TIMESTAMP & PULSE BAR
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Last Sync: $syncTime",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.3f)),
                        fontSize = 9.sp
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
