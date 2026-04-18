package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
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
            val assetsData = prefs[ASSETS_DATA_KEY] ?: ""
            Log.d("SWANIE_PIPE", "Widget Received Suitcase for Vault $boundId: $assetsData")

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
                Log.d("SWANIE_SYNC", "Widget Reading: $assetsData")
                
                val assets = parseAssetsData(assetsData)

                if (assets.isEmpty()) {
                    SyncingContent(bgColor, bgTextColor)
                } else {
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
    }

    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(this.javaClass)
        ids.forEach { update(context, it) }
    }

    private fun parseAssetsData(data: String): List<Triple<AssetEntity, String, String>> {
        if (data.isBlank()) return emptyList()
        return data.split("||").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 10) {
                val asset = AssetEntity(
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
                    premium = parts[8].toDoubleOrNull() ?: 0.0, // Used for calculatedTotal in part 9
                    localIconPath = parts[9] // Part 10 is the Sparkline Path
                )
                Triple(asset, parts[4], parts[8])
            } else if (parts.size == 9) {
                val asset = AssetEntity(
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
                    premium = parts[8].toDoubleOrNull() ?: 0.0, // Used for calculatedTotal
                    localIconPath = "none"
                )
                Triple(asset, parts[4], parts[8])
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
    assets: List<Triple<AssetEntity, String, String>>,
    showTotal: Boolean,
    bgColor: Color,
    bgTextColor: Color,
    cardColor: Color,
    cardTextColor: Color,
    vaultName: String
) {
    Column(modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(modifier = GlanceModifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            // 🎯 BOUTIQUE CENTERED HEADER: Left/Right fixed at 80dp
            Box(modifier = GlanceModifier.width(80.dp).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                Box(modifier = GlanceModifier.size(44.dp).clickable(actionStartActivity(Intent(context, MainActivity::class.java))), contentAlignment = Alignment.Center) {
                    Image(provider = ImageProvider(R.drawable.swan_launcher_icon), contentDescription = "Home", modifier = GlanceModifier.size(28.dp))
                }
            }
            Text(text = vaultName.uppercase(), style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = GlanceModifier.defaultWeight())
            Box(modifier = GlanceModifier.width(80.dp).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = GlanceModifier.size(44.dp).clickable(actionRunCallback<WidgetClickCallback>(actionParametersOf(PortfolioWidget.WIDGET_ID_KEY to appWidgetId))), contentAlignment = Alignment.Center) {
                        Image(provider = ImageProvider(android.R.drawable.ic_menu_edit), contentDescription = "Edit", modifier = GlanceModifier.size(20.dp), colorFilter = ColorFilter.tint(ColorProvider(Color.Yellow)))
                    }
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Box(modifier = GlanceModifier.size(44.dp).clickable(actionRunCallback<RefreshCallback>()), contentAlignment = Alignment.Center) {
                        Image(provider = ImageProvider(android.R.drawable.ic_popup_sync), contentDescription = "Refresh", modifier = GlanceModifier.size(20.dp), colorFilter = ColorFilter.tint(ColorProvider(Color.White)))
                    }
                }
            }
        }

        if (showTotal && totalValue.isNotEmpty()) {
            Text(text = totalValue, style = TextStyle(color = ColorProvider(bgTextColor), fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp))
        }

        Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            assets.take(5).forEach { (asset, priceStr, totalStr) ->
                AssetCardOriginal(context, asset, priceStr, totalStr, cardColor, cardTextColor)
                Spacer(modifier = GlanceModifier.defaultWeight())
            }
        }
        Text(text = "Updated: $lastUpdated", style = TextStyle(fontSize = 8.sp, color = ColorProvider(bgTextColor.copy(alpha = 0.5f)), textAlign = TextAlign.End), modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp))
    }
}

@Composable
fun AssetCardOriginal(context: Context, asset: AssetEntity, priceStr: String, totalStr: String, cardColor: Color, textColor: Color) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().cornerRadius(10.dp).background(cardColor).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        val isMetal = asset.imageUrl.startsWith("res:")
        val iconBgColor = if (isMetal) { if (asset.imageUrl.contains("gold", true)) Color(0xFFFFD700) else Color(0xFFC0C0C0) } else Color.White.copy(alpha = 0.1f)

        // 🎯 LANE 1: IDENTITY (Fixed 130dp for High-Precision)
        Row(modifier = GlanceModifier.width(130.dp), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Box(modifier = GlanceModifier.size(28.dp).cornerRadius(14.dp).background(iconBgColor), contentAlignment = Alignment.Center) {
                if (isMetal) {
                    val resName = asset.imageUrl.substringAfter("res:")
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                    if (resId != 0) Image(provider = ImageProvider(resId), contentDescription = null, modifier = GlanceModifier.size(18.dp))
                } else if (asset.imageUrl.startsWith("file:")) {
                    val path = asset.imageUrl.substringAfter("file:")
                    val bitmap = try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
                    if (bitmap != null) Image(provider = ImageProvider(bitmap), contentDescription = null, modifier = GlanceModifier.size(20.dp)) else StampFallback(asset)
                } else StampFallback(asset)
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column {
                Text(text = asset.symbol.uppercase(), style = TextStyle(color = ColorProvider(textColor), fontSize = 11.sp, fontWeight = FontWeight.Bold))
                
                // 🎯 DIRECT STRING DISPLAY: Bypasses CurrencyFormatter rounding
                val displayPrice = if (priceStr.isNotEmpty()) "$$priceStr" 
                                   else NumberFormat.getCurrencyInstance(Locale.US).format(asset.officialSpotPrice)
                
                Text(
                    text = displayPrice, 
                    style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.6f)), fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
            }
        }

        // 🎯 LANE 2: SPARKLINE (Fixed 80dp)
        Box(modifier = GlanceModifier.width(80.dp).height(40.dp), contentAlignment = Alignment.Center) {
            val path = asset.localIconPath
            if (path != null && path != "none") {
                val bitmap = try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = "Trend",
                        modifier = GlanceModifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }

        // 🎯 LANE 3: HOLDINGS (Flexible / Right-Aligned)
        Column(modifier = GlanceModifier.defaultWeight().fillMaxWidth(), horizontalAlignment = Alignment.End) {
            // 🎯 DIRECT STRING DISPLAY: Bypasses CurrencyFormatter rounding
            val displayTotal = if (totalStr.isNotEmpty()) {
                val formatted = try { 
                    val value = totalStr.toDouble()
                    NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 2 }.format(value)
                } catch (e: Exception) { totalStr }
                formatted
            } else NumberFormat.getCurrencyInstance(Locale.US).format(asset.premium)

            Text(
                text = displayTotal, 
                style = TextStyle(color = ColorProvider(textColor), fontSize = 12.sp, fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00FF00) else Color(0xFFFF4444)
            Text(text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%", style = TextStyle(color = ColorProvider(trendColor), fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun SyncingContent(bgColor: Color, textColor: Color) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.swan_launcher_icon),
            contentDescription = null,
            modifier = GlanceModifier.size(48.dp),
            colorFilter = ColorFilter.tint(ColorProvider(textColor.copy(alpha = 0.5f)))
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = "Syncing Assets...",
            style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.5f)), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun StampFallback(asset: AssetEntity) {
    Text(text = asset.symbol.take(1).uppercase(), style = TextStyle(color = ColorProvider(Color.White), fontSize = 13.sp, fontWeight = FontWeight.Bold))
}

class RefreshCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val appContext = context.applicationContext
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(appContext, PortfolioWidget.PortfolioWidgetEntryPoint::class.java)
                val repo = entryPoint.assetRepository()
                
                // 🛡️ THE SECRET SAUCE: Use the extension on PortfolioWidget to get latest state
                val prefs = PortfolioWidget().getAppWidgetState<Preferences>(appContext, glanceId)
                val boundId = prefs[PortfolioWidget.VAULT_ID_KEY] ?: 1 // Default to 1 if null
                
                try { repo.javaClass.getMethod("invalidatePriceCache").invoke(repo) } catch (e: Exception) {}
                repo.refreshAssets(force = true, portfolioId = boundId.toString())

                val newTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                updateAppWidgetState(appContext, PreferencesGlanceStateDefinition, glanceId) { p ->
                    p.toMutablePreferences().apply {
                        this[PortfolioWidget.LAST_UPDATED_KEY] = newTime
                        this[PortfolioWidget.FORCE_UPDATE_KEY] = System.currentTimeMillis()
                    }.toPreferences()
                }
                PortfolioWidget().update(appContext, glanceId)
            } catch (e: Exception) {
                Log.e("PortfolioWidget", "Refresh failed", e)
            }
        }
    }
}
