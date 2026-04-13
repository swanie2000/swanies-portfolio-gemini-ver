package com.swanie.portfolio.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.glance.action.actionParametersOf
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
import android.graphics.BitmapFactory
import java.io.File

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
        val SHOW_TOTAL_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("show_total")
        val ASSETS_DATA_KEY = stringPreferencesKey("pushed_assets_key")
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
        provideContent {
            val prefs = currentState<Preferences>()
            val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

            val boundId = prefs[VAULT_ID_KEY] ?: 0
            if (boundId == 0) {
                UnlinkedContent(appWidgetId)
                return@provideContent
            }

            val vaultName = prefs[STATIC_VAULT_NAME_KEY] ?: "Loading..."
            val totalValue = prefs[STATIC_TOTAL_BALANCE_KEY] ?: ""
            val showTotal = prefs[SHOW_TOTAL_KEY] ?: true
            val bgColorHex = prefs[WIDGET_BG_COLOR_KEY] ?: "#000416"
            val bgTextColorHex = prefs[WIDGET_BG_TEXT_COLOR_KEY] ?: "#FFFFFF"
            val cardColorHex = prefs[WIDGET_CARD_COLOR_KEY] ?: "#1C1C1E"
            val cardTextColorHex = prefs[WIDGET_CARD_TEXT_COLOR_KEY] ?: "#FFFFFF"

            val bgColor = Color(android.graphics.Color.parseColor(bgColorHex))
            val bgTextColor = Color(android.graphics.Color.parseColor(bgTextColorHex))
            val cardColor = Color(android.graphics.Color.parseColor(cardColorHex))
            val cardTextColor = Color(android.graphics.Color.parseColor(cardTextColorHex))

            val assetsData = prefs[ASSETS_DATA_KEY] ?: ""
            val assets = parseAssetsData(assetsData)

            WidgetContent(
                appWidgetId = appWidgetId,
                totalValue = totalValue,
                lastUpdated = prefs[LAST_UPDATED_KEY] ?: "--:--",
                assets = assets,
                assetHistoryMap = emptyMap(),
                showTotal = showTotal,
                bgColor = bgColor,
                bgTextColor = bgTextColor,
                cardColor = cardColor,
                cardTextColor = cardTextColor,
                vaultName = vaultName
            )
        }
    }

    private fun parseAssetsData(data: String): List<AssetEntity> {
        if (data.isBlank()) return emptyList()
        return data.split("||").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 6) {
                AssetEntity(
                    coinId = parts[0],
                    symbol = parts[1],
                    displayName = parts[2],
                    name = parts[2],
                    imageUrl = parts[3], 
                    officialSpotPrice = parts[4].toDoubleOrNull() ?: 0.0,
                    category = AssetCategory.CRYPTO,
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
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A)) 
            .padding(16.dp)
            .clickable(actionRunCallback<WidgetClickCallback>(actionParametersOf(WidgetClickCallback.WIDGET_ID_KEY to appWidgetId))),
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
    appWidgetId: Int,
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
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(8.dp)
            .clickable(actionRunCallback<WidgetClickCallback>(actionParametersOf(WidgetClickCallback.WIDGET_ID_KEY to appWidgetId)))
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
                // 🚀 REFRESH HITBOX: Preserving 48.dp target area (KEEP ALL)
                Box(
                    modifier = GlanceModifier.size(48.dp).clickable(actionRunCallback<RefreshCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_popup_sync),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier.size(24.dp)
                    )
                }
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
                        text = "Tap to select your assets.",
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
            // 🚀 THE SPRING SHIELD: Soft Stretch Spacing between assets
            Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                assets.forEachIndexed { index, asset ->
                    AssetCardOriginal(
                        asset = asset,
                        history = assetHistoryMap[asset.coinId] ?: emptyList(),
                        cardColor = cardColor,
                        textColor = cardTextColor
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }
            }
        }

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
        Row(
            modifier = GlanceModifier.wrapContentWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            val isMetal = asset.imageUrl.startsWith("res:")
            val iconBgColor = if (isMetal) {
                val isGold = asset.imageUrl.contains("gold", true)
                if (isGold) Color(0xFFFFD700) else Color(0xFFC0C0C0)
            } else {
                Color.White.copy(alpha = 0.1f)
            }

            Box(
                modifier = GlanceModifier.size(32.dp).cornerRadius(16.dp).background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (isMetal) {
                    val resName = asset.imageUrl.substringAfter("res:")
                    val context = LocalContext.current
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                    if (resId != 0) {
                        Image(
                            provider = ImageProvider(resId),
                            contentDescription = null,
                            modifier = GlanceModifier.size(20.dp)
                        )
                    }
                } else if (asset.imageUrl.startsWith("file:")) {
                    val path = asset.imageUrl.substringAfter("file:")
                    val file = java.io.File(path)
                    if (file.exists()) {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            Image(
                                provider = ImageProvider(bitmap),
                                contentDescription = null,
                                modifier = GlanceModifier.size(24.dp).cornerRadius(12.dp)
                            )
                        } else {
                            StampFallback(asset, false)
                        }
                    } else {
                        StampFallback(asset, false)
                    }
                } else {
                    StampFallback(asset, false)
                }
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            Column {
                Text(
                    text = asset.symbol.uppercase(),
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
        }

        Box(
            modifier = GlanceModifier.defaultWeight(),
            contentAlignment = Alignment.Center
        ) {
            asset.localIconPath?.let { path ->
                if (path != "none") {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        val bitmap = try {
                            android.graphics.BitmapFactory.decodeFile(path)
                        } catch (e: Exception) {
                            null
                        }

                        if (bitmap != null) {
                            Image(
                                provider = ImageProvider(bitmap),
                                contentDescription = "Trend",
                                modifier = GlanceModifier.height(30.dp).fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = GlanceModifier.wrapContentWidth(),
            horizontalAlignment = Alignment.End
        ) {
            val assetValue = asset.officialSpotPrice * asset.weight * asset.amountHeld
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.US).format(assetValue),
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00FF00) else Color(0xFFFF0000)
            Text(
                text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%",
                style = TextStyle(
                    color = ColorProvider(trendColor),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
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
