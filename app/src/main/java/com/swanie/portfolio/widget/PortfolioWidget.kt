package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.swanie.portfolio.MainActivity
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.glance.ColorFilter

class PortfolioWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    companion object {
        val VAULT_ID_KEY = intPreferencesKey("bound_vault_id")
        val LAST_UPDATED_KEY = stringPreferencesKey("last_updated_time")
        val WIDGET_BG_COLOR_KEY = stringPreferencesKey("widget_bg_color")
        val WIDGET_BG_TEXT_COLOR_KEY = stringPreferencesKey("widget_bg_text_color")
        val WIDGET_CARD_COLOR_KEY = stringPreferencesKey("widget_card_color")
        val WIDGET_CARD_TEXT_COLOR_KEY = stringPreferencesKey("widget_card_text_color")
        val FORCE_UPDATE_KEY = longPreferencesKey("force_update_time")
        val STATIC_VAULT_NAME_KEY = stringPreferencesKey("static_vault_name")
        val STATIC_TOTAL_BALANCE_KEY = stringPreferencesKey("static_total_balance")
        val SHOW_TOTAL_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("show_total")
        val ASSETS_DATA_KEY = stringPreferencesKey("pushed_assets_key")

        val WIDGET_ID_KEY = ActionParameters.Key<Int>("widgetId")
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PortfolioWidgetEntryPoint {
        fun assetRepository(): AssetRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

            val boundId = prefs[VAULT_ID_KEY] ?: 0

            if (boundId == 0) {
                UnlinkedContent(appWidgetId)
            } else {
                val vaultName = prefs[STATIC_VAULT_NAME_KEY] ?: "PORTFOLIO"
                val totalValue = prefs[STATIC_TOTAL_BALANCE_KEY] ?: ""
                val showTotal = prefs[SHOW_TOTAL_KEY] ?: true

                val bgColorHex = prefs[WIDGET_BG_COLOR_KEY] ?: "#000416"
                val bgTextColorHex = prefs[WIDGET_BG_TEXT_COLOR_KEY] ?: "#FFFFFF"
                val cardColorHex = prefs[WIDGET_CARD_COLOR_KEY] ?: "#1C1C1E"
                val cardTextColorHex = prefs[WIDGET_CARD_TEXT_COLOR_KEY] ?: "#FFFFFF"

                val bgColor = try { Color(android.graphics.Color.parseColor(bgColorHex)) } catch (e: Exception) { Color.Black }
                val bgTextColor = try { Color(android.graphics.Color.parseColor(bgTextColorHex)) } catch (e: Exception) { Color.White }
                val cardColor = try { Color(android.graphics.Color.parseColor(cardColorHex)) } catch (e: Exception) { Color(0xFF1C1C1E) }
                val cardTextColor = try { Color(android.graphics.Color.parseColor(cardTextColorHex)) } catch (e: Exception) { Color.White }

                val assetsData = prefs[ASSETS_DATA_KEY] ?: ""
                val assets = parseAssetsData(assetsData)

                WidgetContent(
                    context = context,
                    appWidgetId = appWidgetId,
                    totalValue = totalValue,
                    lastUpdated = prefs[LAST_UPDATED_KEY] ?: "--:--",
                    assets = assets,
                    showTotal = showTotal,
                    bgColor = bgColor,
                    bgTextColor = bgTextColor,
                    cardColor = cardColor,
                    cardTextColor = cardTextColor,
                    vaultName = vaultName
                )
            }
        }
    }

    private fun parseAssetsData(data: String): List<AssetEntity> {
        if (data.isBlank()) return emptyList()
        return data.split("||").mapNotNull { entry ->
            val parts = entry.split("|")
            // 🛡️ SYNC PROTECTION: We must have at least 8 parts for holding math to work
            if (parts.size >= 8) {
                AssetEntity(
                    coinId = parts[0],
                    symbol = parts[1],
                    displayName = parts[2],
                    name = parts[2],
                    imageUrl = parts[3],
                    officialSpotPrice = parts[4].toDoubleOrNull() ?: 0.0,
                    category = if (parts[3].startsWith("res:")) AssetCategory.METAL else AssetCategory.CRYPTO,
                    priceChange24h = parts[5].toDoubleOrNull() ?: 0.0,
                    weight = parts[6].toDoubleOrNull() ?: 1.0,
                    amountHeld = parts[7].toDoubleOrNull() ?: 1.0,
                    localIconPath = if (parts.size >= 9) parts[8] else null
                )
            } else if (parts.size >= 6) {
                AssetEntity(
                    coinId = parts[0],
                    symbol = parts[1],
                    displayName = parts[2],
                    name = parts[2],
                    imageUrl = parts[3],
                    officialSpotPrice = parts[4].toDoubleOrNull() ?: 0.0,
                    category = if (parts[3].startsWith("res:")) AssetCategory.METAL else AssetCategory.CRYPTO,
                    weight = 1.0,
                    amountHeld = 1.0,
                    priceChange24h = parts[5].toDoubleOrNull() ?: 0.0,
                    localIconPath = if (parts.size >= 7) parts[6] else null
                )
            } else null
        }
    }
}

@Composable
fun UnlinkedContent(appWidgetId: Int) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(Color(0xFF000416)).padding(16.dp).clickable(actionRunCallback<WidgetClickCallback>(actionParametersOf(PortfolioWidget.WIDGET_ID_KEY to appWidgetId))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(provider = ImageProvider(R.drawable.swan_launcher_icon), contentDescription = null, modifier = GlanceModifier.size(48.dp))
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(text = "Swanie's Portfolio", style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(text = "Tap to setup widget", style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.6f)), fontSize = 12.sp))
    }
}

@Composable
fun WidgetContent(
    context: Context,
    appWidgetId: Int,
    totalValue: String,
    lastUpdated: String,
    assets: List<AssetEntity>,
    showTotal: Boolean,
    bgColor: Color,
    bgTextColor: Color,
    cardColor: Color,
    cardTextColor: Color,
    vaultName: String
) {
    Column(modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(horizontal = 8.dp, vertical = 4.dp)) {
        // --- 🦢 HEADER ---
        Row(modifier = GlanceModifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = GlanceModifier.width(60.dp), contentAlignment = Alignment.CenterStart) {
                Box(modifier = GlanceModifier.size(40.dp).clickable(actionStartActivity(Intent(context, MainActivity::class.java))), contentAlignment = Alignment.Center) {
                    Image(provider = ImageProvider(R.drawable.swan_launcher_icon), contentDescription = "Home", modifier = GlanceModifier.size(26.dp))
                }
            }
            Text(text = vaultName.uppercase(), style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = GlanceModifier.defaultWeight())
            Box(modifier = GlanceModifier.width(80.dp), contentAlignment = Alignment.CenterEnd) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = GlanceModifier.size(36.dp).clickable(actionRunCallback<WidgetClickCallback>(actionParametersOf(PortfolioWidget.WIDGET_ID_KEY to appWidgetId))), contentAlignment = Alignment.Center) {
                        Image(provider = ImageProvider(android.R.drawable.ic_menu_edit), contentDescription = "Edit", modifier = GlanceModifier.size(18.dp), colorFilter = ColorFilter.tint(ColorProvider(Color.Yellow)))
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Box(modifier = GlanceModifier.size(36.dp).clickable(actionRunCallback<RefreshCallback>()), contentAlignment = Alignment.Center) {
                        Image(provider = ImageProvider(android.R.drawable.ic_popup_sync), contentDescription = "Refresh", modifier = GlanceModifier.size(18.dp))
                    }
                }
            }
        }

        if (showTotal && totalValue.isNotEmpty()) {
            Text(text = totalValue, style = TextStyle(color = ColorProvider(bgTextColor), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = GlanceModifier.fillMaxWidth().padding(bottom = 2.dp))
        }

        Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            assets.take(5).forEach { asset ->
                AssetCardOriginal(context, asset, cardColor, cardTextColor)
                Spacer(modifier = GlanceModifier.defaultWeight())
            }
        }

        Text(text = "Updated: $lastUpdated", style = TextStyle(fontSize = 7.sp, color = ColorProvider(bgTextColor.copy(alpha = 0.4f)), textAlign = TextAlign.End), modifier = GlanceModifier.fillMaxWidth())
    }
}

@Composable
fun AssetCardOriginal(context: Context, asset: AssetEntity, cardColor: Color, textColor: Color) {
    Row(modifier = GlanceModifier.fillMaxWidth().cornerRadius(8.dp).background(cardColor).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.Vertical.CenterVertically) {

        // --- LEFT COLUMN: Icon + [Symbol over Spot Price] ---
        Row(modifier = GlanceModifier.wrapContentWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
            val isMetal = asset.imageUrl.startsWith("res:")
            val iconBgColor = if (isMetal) { if (asset.imageUrl.contains("gold", true)) Color(0xFFFFD700) else Color(0xFFC0C0C0) } else Color.White.copy(alpha = 0.1f)
            Box(modifier = GlanceModifier.size(28.dp).cornerRadius(14.dp).background(iconBgColor), contentAlignment = Alignment.Center) {
                if (isMetal) {
                    val resName = asset.imageUrl.substringAfter("res:")
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                    if (resId != 0) Image(provider = ImageProvider(resId), contentDescription = null, modifier = GlanceModifier.size(18.dp))
                } else if (asset.imageUrl.startsWith("file:")) {
                    val path = asset.imageUrl.substringAfter("file:")
                    val bitmap = BitmapFactory.decodeFile(path)
                    if (bitmap != null) Image(provider = ImageProvider(bitmap), contentDescription = null, modifier = GlanceModifier.size(20.dp)) else StampFallback(asset)
                } else StampFallback(asset)
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column {
                Text(text = asset.symbol.uppercase(), style = TextStyle(color = ColorProvider(textColor), fontSize = 11.sp, fontWeight = FontWeight.Bold))

                // 💎 RAW SPOT PRICE (Left side)
                val spotPrice = NumberFormat.getCurrencyInstance(Locale.US).format(asset.officialSpotPrice)
                Text(text = spotPrice, style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.6f)), fontSize = 9.sp, fontWeight = FontWeight.Medium))
            }
        }

        Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = Alignment.Center) {
            asset.localIconPath?.let { path ->
                if (path != "none") {
                    val bitmap = BitmapFactory.decodeFile(path)
                    if (bitmap != null) Image(provider = ImageProvider(bitmap), contentDescription = "Trend", modifier = GlanceModifier.height(24.dp).fillMaxWidth(), contentScale = ContentScale.Fit)
                }
            }
        }

        // --- RIGHT COLUMN: [Bag Total over Trend] ---
        Column(modifier = GlanceModifier.wrapContentWidth(), horizontalAlignment = Alignment.End) {

            // 💰 BAG TOTAL (Price * Weight * Amount)
            // If weight and amountHeld are pulled correctly from the 8-part string, this will be different from the left side.
            val totalHoldingValue = asset.officialSpotPrice * asset.weight * asset.amountHeld

            Text(text = NumberFormat.getCurrencyInstance(Locale.US).format(totalHoldingValue), style = TextStyle(color = ColorProvider(textColor), fontSize = 11.sp, fontWeight = FontWeight.Bold))
            val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00FF00) else Color(0xFFFF4444)
            Text(text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%", style = TextStyle(color = ColorProvider(trendColor), fontSize = 9.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun StampFallback(asset: AssetEntity) {
    Text(text = asset.symbol.take(1).uppercase(), style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp, fontWeight = FontWeight.Bold))
}

class RefreshCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, PortfolioWidget.PortfolioWidgetEntryPoint::class.java)
        val repo = entryPoint.assetRepository()
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        val boundId = prefs[PortfolioWidget.VAULT_ID_KEY] ?: 0
        if (boundId == 0) return
        try { repo.javaClass.getMethod("invalidatePriceCache").invoke(repo) } catch (e: Exception) {}
        repo.refreshAssets(force = true, portfolioId = boundId.toString())

        // 🚀 FORCE SYNC: Repack the suitcase on refresh
        repo.pushAssetsToWidget(context, boundId.toString())

        val newTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { p ->
            p.toMutablePreferences().apply {
                this[PortfolioWidget.LAST_UPDATED_KEY] = newTime
                this[PortfolioWidget.FORCE_UPDATE_KEY] = System.currentTimeMillis()
            }.toPreferences()
        }
        PortfolioWidget().update(context, glanceId)
    }
}