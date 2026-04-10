package com.swanie.portfolio.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.swanie.portfolio.MainActivity
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.repository.AssetRepository
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.data.local.PriceHistoryDao
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.AssetCategory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.ContentScale

class PortfolioWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    companion object {
        val VAULT_ID_KEY = intPreferencesKey("bound_vault_id")
        val SELECTED_ASSETS_KEY = stringPreferencesKey("selected_widget_assets")
        val LAST_UPDATED_KEY = stringPreferencesKey("last_updated_time")
        val WIDGET_BG_COLOR_KEY = stringPreferencesKey("widget_bg_color")
        val WIDGET_BG_TEXT_COLOR_KEY = stringPreferencesKey("widget_bg_text_color")
        val WIDGET_CARD_COLOR_KEY = stringPreferencesKey("widget_card_color")
        val WIDGET_CARD_TEXT_COLOR_KEY = stringPreferencesKey("widget_card_text_color")
        val FORCE_UPDATE_KEY = longPreferencesKey("force_update_time")
        val STATIC_VAULT_NAME_KEY = stringPreferencesKey("static_vault_name")
        val STATIC_TOTAL_BALANCE_KEY = stringPreferencesKey("static_total_balance")
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PortfolioWidgetEntryPoint {
        fun assetRepository(): AssetRepository
        fun userConfigDao(): UserConfigDao
        fun priceHistoryDao(): PriceHistoryDao
        fun assetDao(): AssetDao
        fun vaultDao(): VaultDao
        fun themePreferences(): ThemePreferences
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, PortfolioWidgetEntryPoint::class.java)

        val manager = GlanceAppWidgetManager(context)
        val appWidgetId = manager.getAppWidgetId(id)

        // 🚀 INSTANT IDENTITY: Read directly from AppWidget Options first to bypass DataStore delay
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        
        val boundIdFromOptions = options.getInt("vault_id", 0)
        val staticNameFromOptions = options.getString("static_vault_name")
        val staticBalanceFromOptions = options.getString("static_total_balance")

        // ⚡ SAFETY: If we have snapshot data in Options, provide it IMMEDIATELY to kill the "Android Delay"
        if (boundIdFromOptions != 0 && staticNameFromOptions != null) {
            provideContent {
                WidgetContent(
                    totalValue = staticBalanceFromOptions ?: "",
                    lastUpdated = "Linking...",
                    assets = emptyList(),
                    assetHistoryMap = emptyMap(),
                    showTotal = true,
                    bgColor = Color(0xFF000416),
                    bgTextColor = Color.White,
                    cardColor = Color(0xFF1C1C1E),
                    cardTextColor = Color.White,
                    vaultName = staticNameFromOptions
                )
            }
        }

        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val boundId = boundIdFromOptions.takeIf { it != 0 } ?: prefs[VAULT_ID_KEY] ?: 0
        
        if (boundId == 0) {
            provideContent { UnlinkedContent(appWidgetId) }
            return
        }

        val activeVault = entryPoint.vaultDao().getVaultById(boundId)
        if (activeVault == null) {
            provideContent { UnlinkedContent(appWidgetId) }
            return
        }

        val lastUpdatedTime = prefs[LAST_UPDATED_KEY] ?: "--:--"
        val userConfig = entryPoint.userConfigDao().getUserConfig().first()
        
        // ⚡ SNAPSHOT ARCHITECTURE: Check for pre-calculated static data in Options (System Intent) then fall back to DataStore
        val staticName = staticNameFromOptions ?: prefs[STATIC_VAULT_NAME_KEY]
        val staticBalance = staticBalanceFromOptions ?: prefs[STATIC_TOTAL_BALANCE_KEY]

        val allVaultAssets = entryPoint.assetDao().getAssetsByVault(boundId).first()

        val selectedIds = activeVault.selectedWidgetAssets.split(",").filter { it.isNotBlank() }

        val filteredAssets = if (selectedIds.isEmpty()) allVaultAssets.take(5)
        else selectedIds.mapNotNull { aid -> allVaultAssets.find { it.coinId == aid } }.take(5)

        val assetHistoryMap = filteredAssets.associate { asset ->
            asset.coinId to entryPoint.priceHistoryDao().getRecentHistory(asset.coinId).map { it.price }.reversed()
        }

        var totalValue = 0.0
        allVaultAssets.forEach { asset ->
            totalValue += (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
        }

        val displayTotalValue = if (userConfig?.showWidgetTotal == true) {
            staticBalance ?: NumberFormat.getCurrencyInstance(Locale.US).format(totalValue)
        } else ""

        val bgColor = Color(android.graphics.Color.parseColor(activeVault.widgetBgColor))
        val bgTextColor = Color(android.graphics.Color.parseColor(activeVault.widgetBgTextColor))
        val cardColor = Color(android.graphics.Color.parseColor(activeVault.widgetCardColor))
        val cardTextColor = Color(android.graphics.Color.parseColor(activeVault.widgetCardTextColor))

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
                cardTextColor = cardTextColor,
                vaultName = staticName ?: activeVault.name
            )
        }
    }
}

@Composable
fun UnlinkedContent(appWidgetId: Int) {
    val context = LocalContext.current
    val intent = Intent(context, WidgetConfigActivity::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A)) 
            .padding(16.dp)
            .clickable(actionStartActivity(intent)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.swan_launcher_icon),
            contentDescription = "Swan Logo",
            modifier = GlanceModifier.size(64.dp)
        )
        Spacer(modifier = GlanceModifier.height(16.dp))
        Text(
            text = "Unlinked Widget",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Tap to link this widget to a portfolio",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        )
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
    cardTextColor: Color,
    vaultName: String
) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(8.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        Box(modifier = GlanceModifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = vaultName,
                style = TextStyle(
                    color = ColorProvider(bgTextColor.copy(alpha = 0.6f)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
            Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Image(
                    provider = ImageProvider(android.R.drawable.ic_popup_sync),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier.size(20.dp).clickable(actionRunCallback<RefreshCallback>())
                )
            }
        }

        if (showTotal && totalValue.isNotEmpty() && assets.isNotEmpty()) {
            Text(
                text = totalValue,
                style = TextStyle(
                    color = ColorProvider(bgTextColor),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        if (assets.isEmpty()) {
            Box(modifier = GlanceModifier.fillMaxSize().defaultWeight(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Portfolio Linked.",
                        style = TextStyle(
                            color = ColorProvider(bgTextColor),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Go to the App's Widget Manager to select assets.",
                        style = TextStyle(
                            color = ColorProvider(bgTextColor.copy(alpha = 0.6f)),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = GlanceModifier.padding(horizontal = 8.dp)
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
                    if (index < assets.size - 1) Spacer(modifier = GlanceModifier.height(6.dp))
                }
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = "Updated: $lastUpdated",
            style = TextStyle(fontSize = 8.sp, color = ColorProvider(bgTextColor.copy(alpha = 0.4f)), textAlign = TextAlign.End),
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
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        val isMetal = asset.category == AssetCategory.METAL || asset.isMetal || asset.portfolioId == "1"
        val isGold = asset.displayName.contains("Gold", true) || asset.name.contains("Gold", true) || asset.symbol.contains("XAU", true)

        val iconBgColor = if (isMetal) {
            if (isGold) Color(0xFFFFD700) else Color(0xFFC0C0C0)
        } else {
            Color.White.copy(alpha = 0.1f)
        }

        val isBar = asset.physicalForm.equals("Bar", ignoreCase = true)
        val cornerRadius = if (isBar) 4.dp else 16.dp

        Box(
            modifier = GlanceModifier.size(32.dp).cornerRadius(cornerRadius).background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            StampFallback(asset, isMetal)
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(text = asset.displayName.ifEmpty { asset.name }, style = TextStyle(color = ColorProvider(textColor), fontSize = 13.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            Text(text = asset.symbol.uppercase(), style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.5f)), fontSize = 9.sp), maxLines = 1)
        }

        if (history.size >= 2) {
            val sparkColor = if (history.last() >= history.first()) Color(0xFF00FF00) else Color(0xFFFF0000)
            val sparklineBitmap = com.swanie.portfolio.widget.SparklineDrawUtils.drawSparklineBitmap(history, sparkColor, cardColor.toArgb())
            Image(
                provider = ImageProvider(sparklineBitmap),
                contentDescription = "Trend",
                modifier = GlanceModifier.width(70.dp).height(24.dp),
                contentScale = ContentScale.FillBounds
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            val assetValue = asset.officialSpotPrice * asset.weight * asset.amountHeld
            Text(text = NumberFormat.getCurrencyInstance(Locale.US).format(assetValue), style = TextStyle(color = ColorProvider(textColor), fontSize = 12.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00FF00) else Color(0xFFFF0000)
            Text(text = "${if(asset.priceChange24h >= 0) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%", style = TextStyle(color = ColorProvider(trendColor), fontSize = 9.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun StampFallback(asset: AssetEntity, isMetal: Boolean) {
    if (!isMetal) {
        Text(text = asset.symbol.take(1).uppercase(), style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold))
    } else {
        val weightStr = when (asset.weightUnit.uppercase()) {
            "GRAM" -> "1g"
            "KILO" -> "1k"
            "OZ" -> {
                when {
                    asset.weight == 0.1 -> "1/10"
                    asset.weight == 100.0 -> "100"
                    asset.weight == 10.0 -> "10"
                    asset.weight == 1.0 -> "1"
                    else -> if (asset.weight < 1.0) asset.weight.toString().replace("0.", ".") else asset.weight.toInt().toString()
                }
            }
            else -> "1"
        }
        Text(text = weightStr, style = TextStyle(color = ColorProvider(Color.Black.copy(alpha = 0.8f)), fontSize = 10.sp, fontWeight = FontWeight.Bold))
    }
}

class RefreshCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, PortfolioWidget.PortfolioWidgetEntryPoint::class.java)

        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        val boundId = prefs[PortfolioWidget.VAULT_ID_KEY] ?: 0
        if (boundId == 0) return

        entryPoint.assetRepository().refreshAssets(force = true, portfolioId = boundId.toString())

        val newTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { p ->
            p.toMutablePreferences().apply {
                this[PortfolioWidget.LAST_UPDATED_KEY] = newTime
            }.toPreferences()
        }
        PortfolioWidget().update(context, glanceId)
    }
}
