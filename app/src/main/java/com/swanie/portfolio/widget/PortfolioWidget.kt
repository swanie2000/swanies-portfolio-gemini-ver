package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.swanie.portfolio.MainActivity
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.repository.AssetRepository
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.data.local.PriceHistoryDao
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.ThemePreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.state.getAppWidgetState

class PortfolioWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    companion object {
        val SELECTED_ASSETS_KEY = stringPreferencesKey("selected_widget_assets")
        val FORCE_UPDATE_KEY = longPreferencesKey("force_update_time")
        val LAST_UPDATED_KEY = stringPreferencesKey("last_updated_time")

        val WIDGET_BG_COLOR_KEY = stringPreferencesKey("widget_bg_color")
        val WIDGET_BG_TEXT_COLOR_KEY = stringPreferencesKey("widget_bg_text_color")
        val WIDGET_CARD_COLOR_KEY = stringPreferencesKey("widget_card_color")
        val WIDGET_CARD_TEXT_COLOR_KEY = stringPreferencesKey("widget_card_text_color")
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PortfolioWidgetEntryPoint {
        fun assetRepository(): AssetRepository
        fun userConfigDao(): UserConfigDao
        fun priceHistoryDao(): PriceHistoryDao
        fun assetDao(): AssetDao
        fun themePreferences(): ThemePreferences
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, PortfolioWidgetEntryPoint::class.java)
        val prefs = getAppWidgetState<Preferences>(context, id)
        val lastUpdatedTime = prefs[LAST_UPDATED_KEY] ?: "--:--"

        val userConfig = entryPoint.userConfigDao().getUserConfig().first()
        val currentVaultId = entryPoint.themePreferences().currentVaultId.first()
        val allVaultAssets = entryPoint.assetDao().getAssetsByVault(currentVaultId).first()

        val selectedIds = prefs[SELECTED_ASSETS_KEY]?.split(",")?.filter { it.isNotBlank() }
            ?: userConfig?.selectedWidgetAssets?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

        val filteredAssets = if (selectedIds.isEmpty()) allVaultAssets.take(5)
        else selectedIds.mapNotNull { aid -> allVaultAssets.find { it.coinId == aid } }.take(5)

        val assetHistoryMap = filteredAssets.associate { asset ->
            asset.coinId to entryPoint.priceHistoryDao().getRecentHistory(asset.coinId).map { it.price }.reversed()
        }

        var totalValue = 0.0
        allVaultAssets.forEach { asset ->
            // 🛠️ V18 PRECISION: Weight is already troy-normalized. Multipliers are obsolete.
            totalValue += (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
        }

        val displayTotalValue = if (userConfig?.showWidgetTotal == true) NumberFormat.getCurrencyInstance(Locale.US).format(totalValue) else ""

        val rawBg = prefs[WIDGET_BG_COLOR_KEY] ?: userConfig?.widgetBgColor ?: "#000416"
        val rawBgTxt = prefs[WIDGET_BG_TEXT_COLOR_KEY] ?: userConfig?.widgetBgTextColor ?: "#FFFFFF"
        val rawCrd = prefs[WIDGET_CARD_COLOR_KEY] ?: userConfig?.widgetCardColor ?: "#363636"
        val rawCrdTxt = prefs[WIDGET_CARD_TEXT_COLOR_KEY] ?: userConfig?.widgetCardTextColor ?: "#C3C3C3"

        val bgColor = Color(android.graphics.Color.parseColor(rawBg))
        val bgTextColor = Color(android.graphics.Color.parseColor(rawBgTxt))
        val cardColor = Color(android.graphics.Color.parseColor(rawCrd))
        val cardTextColor = Color(android.graphics.Color.parseColor(rawCrdTxt))

        provideContent {
            WidgetContent(
                totalValue = displayTotalValue,
                lastUpdated = lastUpdatedTime,
                assets = filteredAssets,
                assetHistoryMap = assetHistoryMap,
                showTotal = userConfig?.showWidgetTotal == true,
                bgColor = bgColor,
                bgTextColor = bgTextColor,
                cardColor = cardColor,
                cardTextColor = cardTextColor
            )
        }
    }
}

@Composable
fun WidgetContent(
    totalValue: String,
    lastUpdated: String,
    assets: List<AssetEntity>,
    assetHistoryMap: Map<String, List<Double>>,
    showTotal: Boolean,
    bgColor: Color,
    bgTextColor: Color,
    cardColor: Color,
    cardTextColor: Color
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(8.dp)
            .clickable(actionStartActivity(Intent(LocalContext.current, MainActivity::class.java)))
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text(
                text = "PORTFOLIO",
                style = TextStyle(color = ColorProvider(bgTextColor.copy(alpha = 0.6f)), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = GlanceModifier.defaultWeight())

            Box(
                modifier = GlanceModifier
                    .padding(4.dp)
                    .clickable(actionRunCallback<RefreshCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(android.R.drawable.ic_popup_sync),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier.size(28.dp)
                )
            }
        }

        if (showTotal && totalValue.isNotEmpty()) {
            Text(
                text = totalValue,
                style = TextStyle(color = ColorProvider(bgTextColor), fontSize = 24.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (assets.isEmpty()) {
            // 🛠️ PHASE 3: Branded Empty State
            Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                    Image(
                        provider = ImageProvider(com.swanie.portfolio.R.drawable.swanie_foreground),
                        contentDescription = null,
                        modifier = GlanceModifier.size(48.dp).padding(bottom = 8.dp)
                    )
                    Text(
                        text = "EMPTY VAULT",
                        style = TextStyle(color = ColorProvider(bgTextColor.copy(alpha = 0.4f)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tap to sync assets",
                        style = TextStyle(color = ColorProvider(bgTextColor.copy(alpha = 0.3f)), fontSize = 10.sp)
                    )
                }
            }
        } else {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                assets.forEachIndexed { index, asset ->
                    AssetCardOriginal(
                        asset = asset,
                        history = assetHistoryMap[asset.coinId] ?: emptyList(),
                        cardColor = cardColor,
                        textColor = cardTextColor
                    )
                    if (index < assets.size - 1) Spacer(modifier = GlanceModifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = "Updated: $lastUpdated",
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(bgTextColor.copy(alpha = 0.5f)),
                textAlign = TextAlign.End
            ),
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}

@Composable
fun AssetCardOriginal(asset: AssetEntity, history: List<Double>, cardColor: Color, textColor: Color) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(12.dp)
            .background(cardColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier.size(32.dp).cornerRadius(16.dp).background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            val localPath = asset.localIconPath
            if (localPath != null && File(localPath).exists()) {
                val bitmap = BitmapFactory.decodeFile(localPath)
                if (bitmap != null) {
                    Image(provider = ImageProvider(bitmap), contentDescription = asset.symbol, modifier = GlanceModifier.size(32.dp).cornerRadius(16.dp), contentScale = ContentScale.Crop)
                } else { LetterFallback(asset.symbol) }
            } else { LetterFallback(asset.symbol) }
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            val nameToUse = if (asset.isMetal) asset.displayName.ifEmpty { asset.name } else asset.name
            Text(text = nameToUse, style = TextStyle(color = ColorProvider(textColor), fontSize = 14.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            Text(text = asset.symbol.uppercase(), style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.5f)), fontSize = 10.sp), maxLines = 1)
        }

        if (history.size >= 2) {
            val sparkColor = if (history.last() >= history.first()) Color(0xFF00FF00) else Color(0xFFFF0000)
            // 🚀 RGB_565 Blending: Pass the card background color so the sparkline looks native
            val sparklineBitmap = SparklineDrawUtils.drawSparklineBitmap(history, sparkColor, cardColor.toArgb())
            Image(provider = ImageProvider(sparklineBitmap), contentDescription = "Trend", modifier = GlanceModifier.width(120.dp).height(32.dp), contentScale = ContentScale.FillBounds)
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(text = NumberFormat.getCurrencyInstance(Locale.US).format(asset.officialSpotPrice), style = TextStyle(color = ColorProvider(textColor), fontSize = 13.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00FF00) else Color(0xFFFF0000)
            Text(text = "${if(asset.priceChange24h >= 0) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%", style = TextStyle(color = ColorProvider(trendColor), fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun LetterFallback(symbol: String) {
    Text(text = symbol.take(1).uppercase(), style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold))
}

class RefreshCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, PortfolioWidget.PortfolioWidgetEntryPoint::class.java)
        entryPoint.assetRepository().refreshAssets(force = true, portfolioId = "MAIN")

        val newTime = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[PortfolioWidget.LAST_UPDATED_KEY] = newTime
        }
        PortfolioWidget().update(context, glanceId)
    }
}
