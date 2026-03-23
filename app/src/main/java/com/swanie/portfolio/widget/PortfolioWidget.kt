package com.swanie.portfolio.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale

class PortfolioWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PortfolioWidgetEntryPoint {
        fun assetRepository(): AssetRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            PortfolioWidgetEntryPoint::class.java
        )
        val repository = entryPoint.assetRepository()

        // V8 INTEGRATION: Fetch "MAIN" portfolio assets
        val assets = repository.getAssetsForPortfolio("MAIN").first()
        
        var totalValue = 0.0
        var totalChangeWeighted = 0.0

        assets.forEach { asset ->
            val multiplier = when {
                asset.name.contains("KILO", ignoreCase = true) -> 32.1507
                asset.name.contains("GRAM", ignoreCase = true) -> 0.0321507
                else -> 1.0
            }
            // Value = (Price * Weight * Qty) + Premium
            val assetBaseValue = asset.officialSpotPrice * multiplier * asset.weight * asset.amountHeld
            val assetTotalValue = assetBaseValue + asset.premium
            
            totalValue += assetTotalValue
            // Aggregate weighted change: (AssetBaseValue * ChangePercent)
            totalChangeWeighted += (assetBaseValue * asset.priceChange24h)
        }

        val aggregateChangePercent = if (totalValue > 0) totalChangeWeighted / totalValue else 0.0
        val formattedValue = NumberFormat.getCurrencyInstance(Locale.US).format(totalValue)
        val formattedPercent = "${if (aggregateChangePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", aggregateChangePercent)}%"
        
        // Fortress Brand Colors
        val greenPulse = Color(0xFF00C853)
        val redPulse = Color(0xFFFF1744)
        val trendColor = if (aggregateChangePercent >= 0) greenPulse else redPulse

        provideContent {
            WidgetContent(formattedValue, formattedPercent, trendColor)
        }
    }

    @Composable
    private fun WidgetContent(totalValue: String, percentChange: String, trendColor: Color) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(com.swanie.portfolio.R.drawable.widget_background))
        ) {
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL PORTFOLIO",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = totalValue,
                    style = TextStyle(
                        color = ColorProvider(Color.Yellow),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = percentChange,
                    style = TextStyle(
                        color = ColorProvider(trendColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            // THE COLOR PULSE BAR: Represents the overall 24h trend
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorProvider(trendColor))
            ) {}
        }
    }
}
