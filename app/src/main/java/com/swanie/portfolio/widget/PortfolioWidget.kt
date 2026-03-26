package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
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
import com.swanie.portfolio.widget.SparklineDrawUtils
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.NumberFormat
import java.util.Locale
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

        // --- V8.0.0 THEME CACHE KEYS ---
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
        fun vaultDao(): VaultDao
        fun assetDao(): AssetDao
        fun themePreferences(): ThemePreferences
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, PortfolioWidgetEntryPoint::class.java)

        val userConfigDao = entryPoint.userConfigDao()
        val priceHistoryDao = entryPoint.priceHistoryDao()
        val vaultDao = entryPoint.vaultDao()
        val assetDao = entryPoint.assetDao()
        val themePrefs = entryPoint.themePreferences()

        val prefs = getAppWidgetState<Preferences>(context, id)
        val directIdsString = prefs[SELECTED_ASSETS_KEY]
        val lastUpdatedTime = prefs[LAST_UPDATED_KEY] ?: "--:--"

        val userConfig = userConfigDao.getUserConfig().first()
        val currentVaultId = themePrefs.currentVaultId.first()
        val vault = vaultDao.getVaultById(currentVaultId)
        val vaultName = vault?.name ?: "SWANIE"

        val allVaultAssets = assetDao.getAssetsByVault(currentVaultId).first()

        val selectedIds = if (!directIdsString.isNullOrBlank()) {
            directIdsString.split(",").filter { it.isNotBlank() }
        } else {
            userConfig?.selectedWidgetAssets?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        }

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
            totalValue += (assetBaseValue + asset.premium)
            totalChangeWeighted += (assetBaseValue * asset.priceChange24h)
        }

        val aggregateChangePercent = if (totalValue > 0) totalChangeWeighted / totalValue else 0.0
        val displayTotalValue = if (userConfig?.showWidgetTotal == true) NumberFormat.getCurrencyInstance(Locale.US).format(totalValue) else ""
        val formattedPercent = "${if (aggregateChangePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", aggregateChangePercent)}%"
        val trendColor = if (aggregateChangePercent >= 0) Color(0xFF00C853) else Color(0xFFFF1744)

        val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

        // --- ATOMIC THEME RESOLUTION (Prioritize Direct Command Cache) ---
        val rawBg = prefs[WIDGET_BG_COLOR_KEY] ?: userConfig?.widgetBgColor ?: "#000416"
        val rawBgTxt = prefs[WIDGET_BG_TEXT_COLOR_KEY] ?: userConfig?.widgetBgTextColor ?: "#FFFFFF"
        val rawCrd = prefs[WIDGET_CARD_COLOR_KEY] ?: userConfig?.widgetCardColor ?: "#363636"
        val rawCrdTxt = prefs[WIDGET_CARD_TEXT_COLOR_KEY] ?: userConfig?.widgetCardTextColor ?: "#C3C3C3"

        val bgColor = try { Color(android.graphics.Color.parseColor(rawBg)) } catch (e: Exception) { Color(0xFF000416) }
        val bgTextColor = try { Color(android.graphics.Color.parseColor(rawBgTxt)) } catch (e: Exception) { Color.White }
        val cardColor = try { Color(android.graphics.Color.parseColor(rawCrd)) } catch (e: Exception) { Color(0xFF363636) }
        val cardTextColor = try { Color(android.graphics.Color.parseColor(rawCrdTxt)) } catch (e: Exception) { Color(0xFFC3C3C3) }

        provideContent {
            WidgetContent(
                vaultName = vaultName,
                totalValue = displayTotalValue,
                percentChange = formattedPercent,
                trendColor = trendColor,
                lastUpdated = lastUpdatedTime,
                assets = filteredAssets,
                assetHistoryMap = assetHistoryMap,
                intent = intent,
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
    vaultName: String,
    totalValue: String,
    percentChange: String,
    trendColor: Color,
    lastUpdated: String,
    assets: List<AssetEntity>,
    assetHistoryMap: Map<String, List<Double>>,
    intent: Intent,
    showTotal: Boolean,
    bgColor: Color,
    bgTextColor: Color,
    cardColor: Color,
    cardTextColor: Color
) {
    val size = LocalSize.current
    Column(
        modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(bottom = 4.dp)
            .clickable(actionStartActivity(intent))
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(text = vaultName.uppercase(), style = TextStyle(color = ColorProvider(bgTextColor.copy(alpha = 0.8f)), fontSize = 12.sp, fontWeight = FontWeight.Bold))
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(android.R.drawable.ic_popup_sync),
                contentDescription = "Refresh",
                modifier = GlanceModifier.size(24.dp).clickable(actionRunCallback<RefreshCallback>())
            )
        }

        if (showTotal) {
            Column(
                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(text = totalValue, style = TextStyle(color = ColorProvider(bgTextColor), fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
                Text(text = "TREND: " + percentChange, style = TextStyle(color = ColorProvider(trendColor), fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center))
            }
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        val maxVisible = if (size.height > 200.dp) 10 else 5
        Column(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            assets.take(maxVisible).forEachIndexed { index, asset ->
                AssetCard(asset, assetHistoryMap[asset.coinId] ?: emptyList(), cardColor, cardTextColor)
                if (index < assets.take(maxVisible).size - 1) Spacer(modifier = GlanceModifier.height(8.dp))
            }
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(text = "Updated: $lastUpdated", style = TextStyle(fontSize = 10.sp, color = ColorProvider(bgTextColor.copy(alpha = 0.5f))))
        }
    }
}

@Composable
fun AssetCard(asset: AssetEntity, history: List<Double>, cardColor: Color, textColor: Color) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().cornerRadius(12.dp).background(cardColor).padding(horizontal = 12.dp, vertical = 10.dp),
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
            Text(text = asset.name, style = TextStyle(color = ColorProvider(textColor), fontSize = 14.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            Text(text = asset.symbol.uppercase(), style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.5f)), fontSize = 10.sp), maxLines = 1)
        }
        if (history.size >= 2) {
            val lastPrice = history.last()
            val firstPrice = history.first()
            val sparkColor = if (lastPrice >= firstPrice) Color(0xFF00FF00) else Color(0xFFFF0000)
            val sparklineBitmap = SparklineDrawUtils.drawSparklineBitmap(history, sparkColor)
            Image(provider = ImageProvider(sparklineBitmap), contentDescription = "7d Trend", modifier = GlanceModifier.width(120.dp).height(32.dp), contentScale = ContentScale.FillBounds)
        } else { Spacer(modifier = GlanceModifier.width(120.dp).height(32.dp)) }
        Spacer(modifier = GlanceModifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = NumberFormat.getCurrencyInstance(Locale.US).format(asset.officialSpotPrice), style = TextStyle(color = ColorProvider(textColor), fontSize = 13.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            val rowTrendColor = if (asset.priceChange24h >= 0) Color(0xFF00FF00) else Color(0xFFFF0000)
            val sign = if (asset.priceChange24h >= 0) "+" else ""
            val formattedChange = String.format(Locale.US, "%.2f", asset.priceChange24h)
            Text(text = "$sign$formattedChange%", style = TextStyle(color = ColorProvider(rowTrendColor), fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun LetterFallback(symbol: String) {
    val symbolLetter = symbol.take(1).uppercase()
    Text(text = symbolLetter, style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold))
}

class RefreshCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, PortfolioWidget.PortfolioWidgetEntryPoint::class.java)
        entryPoint.assetRepository().refreshAssets(force = true, portfolioId = "MAIN")
        PortfolioWidget().updateAll(context)
    }
}