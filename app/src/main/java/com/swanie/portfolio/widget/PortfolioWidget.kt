package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
import androidx.glance.text.TextAlign
import com.swanie.portfolio.MainActivity
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.repository.AssetRepository
import com.swanie.portfolio.ui.holdings.metalCardPrimaryLabel
import com.swanie.portfolio.ui.holdings.metalWidgetCenterLabel
import com.swanie.portfolio.ui.holdings.metalWidgetHeadlinePair
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.glance.ColorFilter

@Composable
private fun WidgetPlaceholderSwanWhite(sizeDp: androidx.compose.ui.unit.Dp) {
    Image(
        provider = ImageProvider(R.drawable.swan_widget_icon_padded),
        contentDescription = null,
        modifier = GlanceModifier.size(sizeDp),
        colorFilter = ColorFilter.tint(ColorProvider(Color.White))
    )
}

/** Metal identity column: three lines at compact sizes so row height matches crypto (2 lines at 11/9 sp). */
private val WidgetMetalHeadlineFont = 9.sp
private val WidgetMetalPriceFont = 7.sp
private val WidgetMetalPriceFontCompact = 6.sp

/**
 * Uniform card height so weighted gap below each row is even. Must fit sparkline (40dp), holdings column,
 * and metal identity (3 compact lines). 52–61dp clipped the metal spot-price row; 62dp shows full price.
 */
private val WidgetAssetCardHeight = 62.dp

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
        val LAST_GOOD_ASSETS_DATA_KEY = stringPreferencesKey("last_good_assets_key")
        val IS_PRO_USER_KEY = booleanPreferencesKey("is_pro_user")

        /** Row count when using [widgetAssetLineKey] / [lastGoodWidgetAssetLineKey] (avoids single-string OEM limits). */
        val ASSET_ROW_COUNT_KEY = intPreferencesKey("widget_asset_row_n")
        fun widgetAssetLineKey(index: Int) = stringPreferencesKey("widget_asset_line_$index")
        val LAST_GOOD_ASSET_ROW_COUNT_KEY = intPreferencesKey("widget_asset_last_row_n")
        fun lastGoodWidgetAssetLineKey(index: Int) = stringPreferencesKey("widget_asset_last_line_$index")

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
            val assetsData = prefs.readWidgetPackedAssetData(lastGood = false)
            val lastGoodAssetsData = prefs.readWidgetPackedAssetData(lastGood = true)
            Log.d("SWANIE_PIPE", "Widget Received Suitcase for Vault $boundId: $assetsData")
            Log.d("SWANIE_WIDGET", "provideGlance id=$appWidgetId vault=$boundId payload=${assetsData.length} lastGood=${lastGoodAssetsData.length}")

            if (boundId == 0) {
                UnlinkedContent(context = context, appWidgetId = appWidgetId)
            } else {
                val vaultName = prefs[STATIC_VAULT_NAME_KEY] ?: context.getString(R.string.widget_portfolio_label)
                val totalValue = prefs[STATIC_TOTAL_BALANCE_KEY] ?: ""
                val showTotal = prefs[SHOW_TOTAL_KEY] ?: true

                val bgColorHex = prefs[WIDGET_BG_COLOR_KEY] ?: "#000416"
                val bgTextColorHex = prefs[WIDGET_BG_TEXT_COLOR_KEY] ?: "#FFFFFF"
                val cardColorHex = prefs[WIDGET_CARD_COLOR_KEY] ?: "#1C1C1E"
                val cardTextColorHex = prefs[WIDGET_CARD_TEXT_COLOR_KEY] ?: "#FFFFFF"

                val bgColor = try { Color(bgColorHex.toColorInt()) } catch (e: Exception) { Color.Black }
                val bgTextColor = try { Color(bgTextColorHex.toColorInt()) } catch (e: Exception) { Color.White }
                val cardColor = try { Color(cardColorHex.toColorInt()) } catch (e: Exception) { Color(0xFF1C1C1E) }
                val cardTextColor = try { Color(cardTextColorHex.toColorInt()) } catch (e: Exception) { Color.White }
                val isProUser = prefs[IS_PRO_USER_KEY] ?: false

                // Order must match the packed "||" sequence from the repo (UI top → bottom). Do not re-sort.
                val latestAssets = parseAssetsData(assetsData)
                val fallbackAssets = parseAssetsData(lastGoodAssetsData)
                val assets = if (latestAssets.isNotEmpty()) latestAssets else fallbackAssets

                if (assets.isEmpty()) {
                    if (assetsData.isEmpty()) {
                        if (totalValue.isNotBlank()) {
                            ZombieFallbackContent(
                                context = context,
                                appWidgetId = appWidgetId,
                                vaultName = vaultName,
                                totalValue = totalValue,
                                lastUpdated = prefs[LAST_UPDATED_KEY] ?: "--:--",
                                bgColor = bgColor,
                                textColor = bgTextColor
                            )
                        } else {
                            EmptyWidgetContent(context = context, bgColor = bgColor, textColor = bgTextColor, appWidgetId = appWidgetId)
                        }
                    } else {
                        SyncingContent(context = context, bgColor = bgColor, textColor = bgTextColor)
                    }
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
                        vaultName = vaultName,
                        isProUser = isProUser,
                        showUpgradeBanner = !isProUser
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
            parseSingleWidgetAssetEntry(entry.trim())
        }
    }
}

/**
 * Pack each asset line into its own preference entry so a single `pushed_assets_key` string is never
 * truncated by OEM/DataStore limits (which previously capped visible rows around ~5).
 */
internal fun MutablePreferences.writeWidgetPackedAssetRows(rowLines: List<String>) {
    val maxSlots = WidgetAssetLimits.PRO_MAX
    if (rowLines.isEmpty()) {
        for (i in 0 until maxSlots) {
            remove(PortfolioWidget.widgetAssetLineKey(i))
            remove(PortfolioWidget.lastGoodWidgetAssetLineKey(i))
        }
        this[PortfolioWidget.ASSET_ROW_COUNT_KEY] = 0
        this[PortfolioWidget.LAST_GOOD_ASSET_ROW_COUNT_KEY] = 0
        this[PortfolioWidget.ASSETS_DATA_KEY] = ""
        remove(PortfolioWidget.LAST_GOOD_ASSETS_DATA_KEY)
        return
    }
    rowLines.forEachIndexed { i, line ->
        this[PortfolioWidget.widgetAssetLineKey(i)] = line
        this[PortfolioWidget.lastGoodWidgetAssetLineKey(i)] = line
    }
    for (i in rowLines.size until maxSlots) {
        remove(PortfolioWidget.widgetAssetLineKey(i))
        remove(PortfolioWidget.lastGoodWidgetAssetLineKey(i))
    }
    this[PortfolioWidget.ASSET_ROW_COUNT_KEY] = rowLines.size
    this[PortfolioWidget.LAST_GOOD_ASSET_ROW_COUNT_KEY] = rowLines.size
    val joined = rowLines.joinToString("||")
    this[PortfolioWidget.ASSETS_DATA_KEY] = joined
    this[PortfolioWidget.LAST_GOOD_ASSETS_DATA_KEY] = joined
}

private fun Preferences.readWidgetPackedAssetData(lastGood: Boolean): String {
    val countKey =
        if (lastGood) PortfolioWidget.LAST_GOOD_ASSET_ROW_COUNT_KEY else PortfolioWidget.ASSET_ROW_COUNT_KEY
    val n = this[countKey] ?: 0
    if (n > 0) {
        return (0 until n).mapNotNull { i ->
            val key =
                if (lastGood) PortfolioWidget.lastGoodWidgetAssetLineKey(i) else PortfolioWidget.widgetAssetLineKey(i)
            this[key]?.takeIf { it.isNotBlank() }
        }.joinToString("||")
    }
    return if (lastGood) this[PortfolioWidget.LAST_GOOD_ASSETS_DATA_KEY] ?: "" else this[PortfolioWidget.ASSETS_DATA_KEY] ?: ""
}

/**
 * Packed widget rows omit category. [file:] icons are used for custom metal photos and (rarely) local crypto art;
 * infer metals from standard bullion tickers plus name/coinId hints so [metalCardPrimaryLabel] matches holdings.
 */
internal fun inferPackedRowIsMetal(
    coinId: String,
    symbol: String,
    displayName: String,
    iconSource: String,
): Boolean {
    if (iconSource.startsWith("res:") || iconSource == "__METAL_DEFAULT__") return true
    if (!iconSource.startsWith("file:")) return false
    val sym = symbol.trim().uppercase(Locale.US)
    val metalSymbols = setOf("XAU", "XAG", "XPT", "XPD")
    if (sym in metalSymbols) return true
    if (sym.endsWith("=F") && (sym.contains("GC") || sym.contains("SI") || sym.contains("PL") || sym.contains("PA"))) return true
    val hay = buildString {
        append(coinId.lowercase(Locale.US))
        append(' ')
        append(symbol.lowercase(Locale.US))
        append(' ')
        append(displayName.lowercase(Locale.US))
    }
    return hay.contains("gold") || hay.contains("silver") || hay.contains("platinum") || hay.contains("palladium") ||
        hay.contains("bullion") || hay.contains("xau") || hay.contains("xag") || hay.contains("xpt") || hay.contains("xpd") ||
        hay.contains("metal_") || hay.contains("metal-") || hay.contains(" kilo") || hay.contains(" gram") ||
        hay.contains("(1oz") || hay.contains("(100oz") || hay.contains("10oz") || hay.contains("1/10")
}

/**
 * One packed widget row: fixed tail (line price, 24h%, weight, amount, total, sparkline) so [iconSource]
 * may contain `|` (e.g. URLs) without breaking the parser.
 */
internal fun parseSingleWidgetAssetEntry(entry: String): Triple<AssetEntity, String, String>? {
    if (entry.isBlank()) return null
    val parts = entry.split('|')
    if (parts.isEmpty() || parts[0].isBlank()) return null
    return when {
        parts.size == 9 -> {
            val icon = parts[3]
            val isRowMetal = inferPackedRowIsMetal(parts[0], parts[1], parts[2], icon)
            val asset = AssetEntity(
                coinId = parts[0],
                symbol = parts[1],
                displayName = parts[2],
                name = parts[2],
                imageUrl = icon,
                officialSpotPrice = parts[4].toDoubleOrNull() ?: 0.0,
                category = if (isRowMetal) AssetCategory.METAL else AssetCategory.CRYPTO,
                isMetal = isRowMetal,
                priceChange24h = parts[5].toDoubleOrNull() ?: 0.0,
                weight = parts[6].toDoubleOrNull() ?: 1.0,
                amountHeld = parts[7].toDoubleOrNull() ?: 1.0,
                premium = parts[8].toDoubleOrNull() ?: 0.0,
                localIconPath = "none",
                widgetOrder = 0
            )
            Triple(asset, parts[4], parts[8])
        }
        parts.size >= 10 -> {
            val end = parts.size
            val iconSource = parts.subList(3, end - 6).joinToString("|")
            val linePriceStr = parts[end - 6]
            val priceChange = parts[end - 5].toDoubleOrNull() ?: 0.0
            val weight = parts[end - 4].toDoubleOrNull() ?: 1.0
            val amount = parts[end - 3].toDoubleOrNull() ?: 1.0
            val formattedTotal = parts[end - 2]
            val sparklinePath = parts[end - 1]
            val isRowMetal = inferPackedRowIsMetal(parts[0], parts[1], parts[2], iconSource)
            val asset = AssetEntity(
                coinId = parts[0],
                symbol = parts[1],
                displayName = parts[2],
                name = parts[2],
                imageUrl = iconSource,
                officialSpotPrice = linePriceStr.toDoubleOrNull() ?: 0.0,
                category = if (isRowMetal) AssetCategory.METAL else AssetCategory.CRYPTO,
                isMetal = isRowMetal,
                priceChange24h = priceChange,
                weight = weight,
                amountHeld = amount,
                premium = formattedTotal.toDoubleOrNull() ?: 0.0,
                localIconPath = sparklinePath,
                widgetOrder = 0
            )
            Triple(asset, linePriceStr, formattedTotal)
        }
        else -> null
    }
}

@Composable
fun ZombieFallbackContent(
    context: Context,
    appWidgetId: Int,
    vaultName: String,
    totalValue: String,
    lastUpdated: String,
    bgColor: Color,
    textColor: Color
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(12.dp).clickable(
            actionRunCallback<WidgetClickCallback>(
                actionParametersOf(PortfolioWidget.WIDGET_ID_KEY to appWidgetId)
            )
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = vaultName.uppercase(),
            style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.8f)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = totalValue,
            style = TextStyle(color = ColorProvider(textColor), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = context.getString(R.string.widget_loading),
            style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.7f)), fontSize = 10.sp)
        )
        Text(
            text = context.getString(R.string.widget_updated_at, lastUpdated),
            style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.5f)), fontSize = 9.sp)
        )
    }
}

@Composable
fun UnlinkedContent(context: Context, appWidgetId: Int) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(Color(0xFF000416)).padding(16.dp).clickable(actionRunCallback<WidgetClickCallback>(actionParametersOf(PortfolioWidget.WIDGET_ID_KEY to appWidgetId))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WidgetPlaceholderSwanWhite(48.dp)
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(text = context.getString(R.string.app_name), style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(text = context.getString(R.string.widget_manager_title), style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.6f)), fontSize = 12.sp))
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
    vaultName: String,
    isProUser: Boolean,
    showUpgradeBanner: Boolean
) {
    Column(modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(modifier = GlanceModifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = GlanceModifier.width(80.dp).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                Box(modifier = GlanceModifier.size(44.dp).clickable(actionStartActivity(Intent(context, MainActivity::class.java))), contentAlignment = Alignment.Center) {
                    Image(provider = ImageProvider(R.drawable.swan_widget_icon_padded), contentDescription = null, modifier = GlanceModifier.size(28.dp))
                }
            }
            Text(text = vaultName.uppercase(), style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = GlanceModifier.defaultWeight())
            Box(modifier = GlanceModifier.width(80.dp).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = GlanceModifier.size(44.dp).clickable(actionRunCallback<WidgetClickCallback>(actionParametersOf(PortfolioWidget.WIDGET_ID_KEY to appWidgetId))), contentAlignment = Alignment.Center) {
                        Image(provider = ImageProvider(android.R.drawable.ic_menu_edit), contentDescription = null, modifier = GlanceModifier.size(20.dp), colorFilter = ColorFilter.tint(ColorProvider(Color.Yellow)))
                    }
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Box(modifier = GlanceModifier.size(44.dp).clickable(actionRunCallback<RefreshCallback>()), contentAlignment = Alignment.Center) {
                        Image(provider = ImageProvider(android.R.drawable.ic_popup_sync), contentDescription = null, modifier = GlanceModifier.size(20.dp), colorFilter = ColorFilter.tint(ColorProvider(Color.White)))
                    }
                }
            }
        }

        if (showTotal && totalValue.isNotEmpty()) {
            val totalFontSize = if (totalValue.length > 13) 16.sp else 20.sp
            Text(text = totalValue, style = TextStyle(color = ColorProvider(bgTextColor), fontSize = totalFontSize, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp))
        }

        Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            val rowCap = if (isProUser) WidgetAssetLimits.PRO_MAX else WidgetAssetLimits.FREE_MAX
            val visible = assets.take(rowCap)
            visible.forEach { (asset, priceStr, totalStr) ->
                // RemoteViews (Glance → RV) allows only a small number of direct children per Column (~10).
                // Card + Spacer as siblings was 2×N children → capped around 5 rows. Nest so this Column has one child per asset.
                // Do not use fillMaxHeight() on each row — on many launchers every row expands to the full list height and only one card shows.
                Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    AssetCardOriginal(context, asset, priceStr, totalStr, cardColor, cardTextColor)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }
            }
        }
        if (showUpgradeBanner) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(8.dp)
                    .background(Color(0x22FFD54F))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = context.getString(R.string.widget_free_pro_banner),
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFFFFD54F)),
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.fillMaxWidth()
                )
            }
            Spacer(modifier = GlanceModifier.height(2.dp))
        }
        Text(
            text = context.getString(R.string.widget_updated_at, lastUpdated),
            style = TextStyle(fontSize = 8.sp, color = ColorProvider(bgTextColor.copy(alpha = 0.5f)), textAlign = TextAlign.End),
            modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp)
        )
    }
}

@Composable
private fun MetalWidgetWeightStamp(asset: AssetEntity) {
    val label = metalWidgetCenterLabel(asset)
    val fontSize = if (label.length > 2) 8.sp else 10.sp
    Text(
        text = label,
        style = TextStyle(
            color = ColorProvider(Color.Black.copy(alpha = 0.7f)),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        ),
        maxLines = 1,
    )
}

@Composable
fun AssetCardOriginal(context: Context, asset: AssetEntity, priceStr: String, totalStr: String, cardColor: Color, textColor: Color) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(WidgetAssetCardHeight)
            .cornerRadius(10.dp)
            .background(cardColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        val isMetalAsset = asset.category == AssetCategory.METAL || asset.isMetal
        val haystack = "${asset.displayName} ${asset.name} ${asset.symbol}".lowercase(Locale.getDefault())
        val customIconBitmap: Bitmap? =
            if (asset.imageUrl.startsWith("file:")) {
                val path = asset.imageUrl.substringAfter("file:")
                try {
                    BitmapFactory.decodeFile(path)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        // Custom photos: neutral plate (matches crypto); built-in metal stamp keeps gold/silver disk.
        val iconBgColor = when {
            customIconBitmap != null -> Color.White.copy(alpha = 0.1f)
            isMetalAsset -> if (haystack.contains("gold") || haystack.contains("xau")) Color(0xFFFFD700) else Color(0xFFC0C0C0)
            else -> Color.White.copy(alpha = 0.1f)
        }

        // 🎯 LANE 1: IDENTITY (Fixed 130dp for High-Precision)
        Row(modifier = GlanceModifier.width(130.dp), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Box(modifier = GlanceModifier.size(28.dp).cornerRadius(14.dp).background(iconBgColor), contentAlignment = Alignment.Center) {
                when {
                    asset.imageUrl.startsWith("file:") -> {
                        when {
                            customIconBitmap != null -> Image(
                                provider = ImageProvider(customIconBitmap),
                                contentDescription = null,
                                modifier = GlanceModifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            isMetalAsset -> MetalWidgetWeightStamp(asset)
                            else -> StampFallback(asset)
                        }
                    }
                    isMetalAsset && (asset.imageUrl == "__METAL_DEFAULT__" || asset.imageUrl.isBlank()) -> {
                        MetalWidgetWeightStamp(asset)
                    }
                    isMetalAsset && asset.imageUrl.startsWith("res:") -> {
                        val resName = asset.imageUrl.substringAfter("res:")
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        when {
                            resId != 0 -> Image(
                                provider = ImageProvider(resId),
                                contentDescription = null,
                                modifier = GlanceModifier.size(18.dp),
                            )
                            else -> MetalWidgetWeightStamp(asset)
                        }
                    }
                    asset.imageUrl.startsWith("res:") -> {
                        val resName = asset.imageUrl.substringAfter("res:")
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        if (resId != 0) {
                            Image(
                                provider = ImageProvider(resId),
                                contentDescription = null,
                                modifier = GlanceModifier.size(18.dp),
                            )
                        } else {
                            StampFallback(asset)
                        }
                    }
                    else -> StampFallback(asset)
                }
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(
                modifier = GlanceModifier.fillMaxHeight(),
                verticalAlignment = Alignment.Vertical.Top,
            ) {
                val isMetalRow = asset.category == AssetCategory.METAL || asset.isMetal
                if (isMetalRow) {
                    val (headLine1, headLine2) = metalWidgetHeadlinePair(metalCardPrimaryLabel(asset))
                    Text(
                        text = headLine1,
                        style = TextStyle(
                            color = ColorProvider(textColor),
                            fontSize = WidgetMetalHeadlineFont,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )
                    if (headLine2 != null) {
                        Text(
                            text = headLine2,
                            style = TextStyle(
                                color = ColorProvider(textColor),
                                fontSize = WidgetMetalHeadlineFont,
                                fontWeight = FontWeight.Bold,
                            ),
                            maxLines = 1,
                        )
                    }
                } else {
                    Text(
                        text = asset.symbol.uppercase(Locale.getDefault()),
                        style = TextStyle(color = ColorProvider(textColor), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                    )
                }

                // 🎯 DIRECT STRING DISPLAY: Bypasses CurrencyFormatter rounding
                val displayPrice = if (priceStr.isNotEmpty()) "$$priceStr"
                    else NumberFormat.getCurrencyInstance(Locale.US).format(asset.officialSpotPrice)

                val dynamicFontSize = when {
                    isMetalRow -> if (displayPrice.length > 12) WidgetMetalPriceFontCompact else WidgetMetalPriceFont
                    else -> if (displayPrice.length > 12) 7.sp else 9.sp
                }

                Text(
                    text = displayPrice,
                    style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.6f)), fontSize = dynamicFontSize, fontWeight = FontWeight.Medium),
                    maxLines = 1,
                )
            }
        }

        // 🎯 LANE 2: SPARKLINE (Fixed 80dp)
        Box(modifier = GlanceModifier.width(80.dp).height(40.dp), contentAlignment = Alignment.Center) {
            val path = asset.localIconPath
            val bitmap = if (path != null && path != "none") {
                try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
            } else {
                null
            }
            if (bitmap != null) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                val trendUp = asset.priceChange24h >= 0
                val trendColor = if (trendUp) Color(0xFF00FF00) else Color(0xFFFF4444)
                val trendGlyph = if (trendUp) "↗" else "↘"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trendGlyph,
                        style = TextStyle(
                            color = ColorProvider(trendColor),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "%s%.2f%%",
                            if (trendUp) "+" else "",
                            asset.priceChange24h
                        ),
                        style = TextStyle(
                            color = ColorProvider(trendColor.copy(alpha = 0.82f)),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        // 🎯 LANE 3: HOLDINGS (Flexible / Right-Aligned)
        Column(modifier = GlanceModifier.defaultWeight().fillMaxWidth(), horizontalAlignment = Alignment.End) {
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
            val changeText = context.getString(
                R.string.widget_percent_change,
                if (asset.priceChange24h >= 0) "+" else "",
                asset.priceChange24h,
            )
            Text(text = changeText, style = TextStyle(color = ColorProvider(trendColor), fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun SyncingContent(context: Context, bgColor: Color, textColor: Color) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WidgetPlaceholderSwanWhite(48.dp)
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = context.getString(R.string.widget_loading),
            style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.5f)), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun EmptyWidgetContent(context: Context, bgColor: Color, textColor: Color, appWidgetId: Int) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(bgColor).padding(16.dp).clickable(actionRunCallback<WidgetClickCallback>(actionParametersOf(PortfolioWidget.WIDGET_ID_KEY to appWidgetId))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WidgetPlaceholderSwanWhite(44.dp)
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = context.getString(R.string.widget_no_assets_available),
            style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.8f)), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = context.getString(R.string.widget_manager_title),
            style = TextStyle(color = ColorProvider(textColor.copy(alpha = 0.5f)), fontSize = 11.sp)
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
                
                val prefs = PortfolioWidget().getAppWidgetState<Preferences>(appContext, glanceId)
                val boundId = prefs[PortfolioWidget.VAULT_ID_KEY] ?: 1
                
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
