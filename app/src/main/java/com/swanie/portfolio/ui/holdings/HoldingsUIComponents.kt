package com.swanie.portfolio.ui.holdings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.yalantis.ucrop.UCrop
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.onboarding.HoldingsWalkthroughController
import com.swanie.portfolio.ui.onboarding.HoldingsWalkthroughStep
import com.swanie.portfolio.ui.onboarding.WalkthroughAnchor
import com.swanie.portfolio.ui.onboarding.walkthroughAnchor
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetValuation
import java.io.File
import java.text.DecimalFormat
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlinx.coroutines.launch

private const val CROP_TAG = "ICON_CROP"
private const val MAX_CROP_SOURCE_BYTES = 8 * 1024 * 1024

private fun Context.findComponentActivity(): ComponentActivity? {
    var ctx: Context? = this
    while (ctx != null) {
        if (ctx is ComponentActivity) return ctx
        ctx = (ctx as? ContextWrapper)?.baseContext
    }
    return null
}

private fun newCropDestinationUri(context: Context): Uri {
    val dir = File(context.cacheDir, "icon_crop")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "crop_${UUID.randomUUID()}.png")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

/**
 * Copy gallery/picker [Uri] into app cache and expose a [FileProvider] uri.
 * uCrop and cross-app [content] URIs often need this + [Intent] URI flags to open reliably.
 */
private fun copyPickerUriToCacheForCrop(context: Context, source: Uri): Uri? {
    val importDir = File(context.cacheDir, "icon_crop/imports")
    if (!importDir.exists()) importDir.mkdirs()
    val outFile = File(importDir, "in_${UUID.randomUUID()}.png")
    return try {
        var total = 0L
        context.contentResolver.openInputStream(source)?.use { ins ->
            java.io.FileOutputStream(outFile).use { outs ->
                val buf = ByteArray(8192)
                while (true) {
                    val n = ins.read(buf)
                    if (n == -1) break
                    total += n
                    if (total > MAX_CROP_SOURCE_BYTES) {
                        outFile.delete()
                        Log.w(CROP_TAG, "Picked image too large, skipping local copy")
                        return null
                    }
                    outs.write(buf, 0, n)
                }
            }
        } ?: return null
        if (!outFile.exists() || outFile.length() == 0L) {
            outFile.delete()
            return null
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    } catch (e: Exception) {
        Log.e(CROP_TAG, "copyPickerUriToCacheForCrop failed: ${e.message}", e)
        outFile.delete()
        null
    }
}

private fun buildIconCropIntent(context: Context, source: Uri, destination: Uri? = null): Intent {
    val dest = destination ?: newCropDestinationUri(context)
    val surface = ContextCompat.getColor(context, R.color.ucrop_surface)
    val toolbar = ContextCompat.getColor(context, R.color.ucrop_toolbar)
    val status = ContextCompat.getColor(context, R.color.ucrop_status_bar)
    val accent = AndroidColor.parseColor("#FFEB3B")
    val opts = UCrop.Options().apply {
        setCompressionQuality(92)
        setToolbarColor(toolbar)
        setStatusBarColor(status)
        setActiveControlsWidgetColor(accent)
        setToolbarWidgetColor(accent)
        setRootViewBackgroundColor(surface)
        setDimmedLayerColor(AndroidColor.parseColor("#E6161616"))
        setShowCropFrame(true)
        setShowCropGrid(true)
    }
    return UCrop.of(source, dest)
        .withOptions(opts)
        .withAspectRatio(1f, 1f)
        .withMaxResultSize(1024, 1024)
        .getIntent(context)
        .apply {
            // Let uCrop (same app) read the picker/source URI and write the target FileProvider URI.
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
}

/**
 * Center text for built-in (non-photo) metal icons on the Glance widget — matches [MetalIcon] weight tokens.
 */
fun metalWidgetCenterLabel(asset: AssetEntity): String {
    val weight = asset.weight
    val unit = asset.weightUnit
    return when (unit.uppercase(Locale.US)) {
        "GRAM" -> "1g"
        "KILO" -> "1k"
        "OZ" -> {
            when {
                abs(weight - 0.1) < 0.001 -> "1/10"
                abs(weight - 100.0) < 0.001 -> "100"
                abs(weight - 10.0) < 0.001 -> "10"
                abs(weight - 1.0) < 0.001 -> "1"
                weight < 1.0 -> weight.toString().replace("0.", ".").trimEnd('0').trimEnd('.')
                else -> weight.toInt().toString()
            }
        }
        else -> weight.toString()
    }
}

private fun resolveCustomIconFile(context: Context, coinId: String, localPath: String?): File? {
    if (localPath.isNullOrBlank()) return null
    val stored = File(localPath)
    if (stored.exists()) return stored
    if (coinId.isBlank()) return null
    val safeId = coinId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val onDisk = File(File(context.filesDir, "custom_icons"), "$safeId.png")
    return onDisk.takeIf { it.exists() }
}

@Composable
fun MetalIcon(
    name: String,
    weight: Double,
    unit: String = "OZ",
    physicalForm: String = "Coin",
    size: Int = 44,
    imageUrl: String = "",
    coinId: String = "",
    localPath: String? = null,
    category: AssetCategory = AssetCategory.METAL,
    /** Incremented by parent after custom icon save so the row reloads even when [localPath] string is unchanged. */
    localIconReloadNonce: Int = 0,
) {
    val context = LocalContext.current
    // Must reset when switching default/remote ↔ custom file; stale true skips the file branch.
    var isError by remember(coinId, localPath, localIconReloadNonce, imageUrl) { mutableStateOf(false) }
    val customFile = remember(coinId, localPath, localIconReloadNonce) {
        resolveCustomIconFile(context, coinId, localPath)
    }
    val customIconPath = customFile?.absolutePath

    LaunchedEffect(coinId, localPath, localIconReloadNonce, imageUrl) {
        isError = false
    }

    if (customFile != null && !isError) {
        var diskKey by remember(customIconPath, localIconReloadNonce) { mutableStateOf<String?>(null) }
        LaunchedEffect(customIconPath, localIconReloadNonce) {
            diskKey = "${customFile.lastModified()}_${customFile.length()}"
        }
        val resolvedDiskKey = diskKey
        if (resolvedDiskKey != null) {
            val imageModel = remember(customIconPath, resolvedDiskKey, localIconReloadNonce) {
                ImageRequest.Builder(context)
                    .data(customFile)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCacheKey("${customFile.absolutePath}#$resolvedDiskKey#$localIconReloadNonce")
                    .build()
            }
            key(coinId, customIconPath, resolvedDiskKey, localIconReloadNonce) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size.dp).clip(CircleShape),
                    onError = { isError = true }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
            )
        }
    } else if (imageUrl == "SW_DEFAULT") {
        Box(modifier = Modifier.size((size * 1.2).dp).clip(CircleShape).background(Color.White.copy(0.1f)), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.fillMaxSize().scale(1.5f))
        }
    } else if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
        AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size.dp).clip(CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape))
    } else if (imageUrl.isNotEmpty() && !isError) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size.dp).clip(CircleShape),
            onError = { isError = true }
        )
    } else {
        if (category == AssetCategory.CRYPTO) {
            val firstLetter = name.take(1).uppercase()
            Box(modifier = Modifier.size(size.dp).clip(CircleShape).background(Color.Yellow.copy(0.2f)).border(1.dp, Color.Yellow.copy(0.4f), CircleShape), contentAlignment = Alignment.Center) {
                Text(text = firstLetter, color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = (size * 0.5).sp)
            }
        } else {
            val isGold = name.contains("Gold", true) || name.contains("XAU", true)
            val isSilver = name.contains("Silver", true) || name.contains("XAG", true)
            val isPlat = name.contains("Plat", true) || name.contains("XPT", true)

            val metalColors = when {
                isGold -> listOf(Color(0xFFFFD700), Color(0xFFFDB931), Color(0xFFB8860B))
                isSilver -> listOf(Color(0xFFE0E0E0), Color(0xFFAAAAAA), Color(0xFF757575))
                isPlat -> listOf(Color(0xFFE5E4E2), Color(0xFFB4B1B0), Color(0xFF888581))
                else -> listOf(Color(0xFFCED4DA), Color(0xFFADB5BD), Color(0xFF495057))
            }

            val isBar = physicalForm.equals("Bar", ignoreCase = true)

            Box(modifier = Modifier.size(size.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (isBar) {
                        val barWidth = size.dp.toPx()
                        val barHeight = size.dp.toPx() * 0.7f
                        drawRoundRect(
                            brush = Brush.linearGradient(colors = metalColors, start = Offset.Zero, end = Offset(barWidth, barHeight)),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx()),
                            topLeft = Offset(0f, (barWidth - barHeight) / 2)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.3f),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx()),
                            topLeft = Offset(0f, (barWidth - barHeight) / 2),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    } else {
                        drawCircle(brush = Brush.radialGradient(colors = metalColors, center = center, radius = size.dp.toPx() / 2))
                        drawCircle(color = Color.White.copy(alpha = 0.2f), radius = (size.dp.toPx() / 2) - 2.dp.toPx(), style = Stroke(width = 1.5.dp.toPx()))
                    }
                }

                val weightStr = when (unit.uppercase()) {
                    "GRAM" -> "1g"
                    "KILO" -> "1k"
                    "OZ" -> {
                        when {
                            weight == 0.1 -> "1/10"
                            weight == 100.0 -> "100"
                            weight == 10.0 -> "10"
                            weight == 1.0 -> "1"
                            else -> if (weight < 1.0) weight.toString().replace("0.", ".") else weight.toInt().toString()
                        }
                    }
                    else -> weight.toString()
                }

                Text(
                    text = weightStr,
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = if (weightStr.length > 2) (size * 0.28).sp else (size * 0.35).sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun SparklineChart(
    historyData: List<Double>,
    modifier: Modifier = Modifier,
    lineColorOverride: Color? = null
) {
    if (historyData.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.sparkline_gathering_data), color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Canvas(Modifier.fillMaxSize()) {
                drawLine(color = Color.White.copy(alpha = 0.1f), start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 1.dp.toPx())
            }
        }
        return
    }
    val lastPrice = historyData.last()
    val firstPrice = historyData.first()
    val trendColor = lineColorOverride ?: if (lastPrice >= firstPrice) Color(0xFF00FF00) else Color(0xFFFF0000)
    val min = historyData.minOrNull() ?: 0.0
    val max = historyData.maxOrNull() ?: 1.0
    val range = if ((max - min) > 0) max - min else 1.0
    Canvas(modifier) {
        val points = historyData.mapIndexed { i, p ->
            Offset(
                i.toFloat() / (historyData.size - 1) * size.width,
                size.height - (((p - min) / range) * (size.height - 8f) + 4f).toFloat()
            )
        }
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        }
        drawPath(path, trendColor, style = Stroke(2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun WatermarkBadge(source: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = source.uppercase(),
        color = color.copy(alpha = 0.3f),
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun FunnelGrid(
    options: List<String>,
    selected: String?,
    compact: Boolean = false,
    labelForOption: (String) -> String = { it },
    onSelect: (String) -> Unit,
) {
    val rowGap = if (compact) 6.dp else 10.dp
    val colGap = if (compact) 6.dp else 10.dp
    val cellHeight = if (compact) 42.dp else 55.dp
    val fontSize = if (compact) 10.sp else 11.sp
    Column(verticalArrangement = Arrangement.spacedBy(rowGap)) {
        options.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(colGap)) {
                rowItems.forEach { option ->
                    val isSelected = selected != null && option.equals(selected, ignoreCase = true)
                    val label = labelForOption(option)
                    Box(modifier = Modifier.weight(1f).height(cellHeight).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Color.Yellow else Color.White.copy(0.05f)).clickable { onSelect(option) }.border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text(text = label.uppercase(), color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Black, fontSize = fontSize)
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun measureSingleLineWidthPx(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    fontSizeSp: Float,
    maxLines: Int,
    softWrap: Boolean,
    overflow: TextOverflow,
    layoutDirection: LayoutDirection,
    density: Density,
): Float {
    val layout = textMeasurer.measure(
        text = AnnotatedString(text),
        style = style.copy(fontSize = fontSizeSp.sp),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        constraints = Constraints(maxWidth = Constraints.Infinity),
        layoutDirection = layoutDirection,
        density = density,
    )
    return layout.size.width.toFloat()
}

private fun computeAutoResizeFontSizeSp(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    maxWidthPx: Float,
    maxSp: Float,
    minSp: Float,
    maxLines: Int,
    softWrap: Boolean,
    overflow: TextOverflow,
    layoutDirection: LayoutDirection,
    density: Density,
): Float {
    if (text.isEmpty() || !maxWidthPx.isFinite() || maxWidthPx <= 0f) return maxSp
    val loBound = minOf(maxSp, minSp)
    val hiBound = maxOf(maxSp, minSp)

    fun widthAt(sp: Float): Float =
        measureSingleLineWidthPx(textMeasurer, text, style, sp, maxLines, softWrap, overflow, layoutDirection, density)

    val limit = maxWidthPx.coerceAtLeast(1f)
    if (widthAt(hiBound) <= limit) return hiBound
    if (widthAt(loBound) > limit) return loBound

    var lo = loBound
    var hi = hiBound
    repeat(24) {
        val mid = (lo + hi) / 2f
        if (widthAt(mid) <= limit) lo = mid else hi = mid
    }
    return lo
}

@Composable
fun AutoResizingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    softWrap: Boolean = false,
    overflow: TextOverflow = TextOverflow.Clip,
    maxFontSize: TextUnit = style.fontSize,
    minFontSize: TextUnit = 10.sp,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val resolvedMax = when {
        maxFontSize != TextUnit.Unspecified -> maxFontSize
        style.fontSize != TextUnit.Unspecified -> style.fontSize
        else -> 16.sp
    }
    val resolvedMin = minFontSize

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val fontSp = remember(
            text,
            maxWidthPx,
            resolvedMax,
            resolvedMin,
            style,
            maxLines,
            softWrap,
            overflow,
            textMeasurer,
            layoutDirection,
            density,
        ) {
            computeAutoResizeFontSizeSp(
                textMeasurer = textMeasurer,
                text = text,
                style = style,
                maxWidthPx = maxWidthPx,
                maxSp = resolvedMax.value,
                minSp = resolvedMin.value,
                maxLines = maxLines,
                softWrap = softWrap,
                overflow = overflow,
                layoutDirection = layoutDirection,
                density = density,
            )
        }

        Text(
            text = text,
            style = style.copy(fontSize = fontSp.sp),
            maxLines = maxLines,
            softWrap = softWrap,
            overflow = overflow,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

fun getCurrencySymbol(code: String): String = when (code.uppercase()) {
    "EUR" -> "€"
    "GBP" -> "£"
    else -> "$"
}

fun formatBoutiquePrice(price: Double, currencyCode: String = "USD"): String {
    val symbol = getCurrencySymbol(currencyCode)
    return when {
        price >= 0.10 -> String.format(Locale.US, "%s%,.2f", symbol, price)
        price >= 0.0001 -> String.format(Locale.US, "%s%,.5f", symbol, price)
        else -> String.format(Locale.US, "%s%,.8f", symbol, price)
    }
}

fun formatCurrency(v: Double, d: Int = 2, currencyCode: String = "USD"): String {
    val multiplier = when (currencyCode.uppercase()) {
        "EUR" -> 0.92
        "GBP" -> 0.78
        else -> 1.0
    }
    val convertedValue = v * multiplier
    val symbol = getCurrencySymbol(currencyCode)
    val df = DecimalFormat("$symbol#,##0.00")
    if (d != 2) { df.minimumFractionDigits = 0; df.maximumFractionDigits = d }
    return df.format(convertedValue)
}

fun formatAmount(v: Double): String = DecimalFormat("#,###.########").format(v)

/** Under-icon label: architect / vault metals use display name; crypto stays on symbol. */
private fun underIconTickerText(asset: AssetEntity): String =
    when (asset.category) {
        AssetCategory.METAL -> asset.displayName.ifBlank { asset.name }.ifBlank { asset.symbol }
        AssetCategory.CRYPTO -> asset.symbol
    }

/** User-facing title for metal cards: prefer funnel / vault text over bare symbol. */
internal fun metalCardPrimaryLabel(asset: AssetEntity): String =
    asset.displayName.trim().ifBlank { asset.name.trim() }.ifBlank { asset.symbol.trim() }

/** Second-line spot ticker (e.g. XAG) when it is not already spelled out in the user title. */
internal fun metalShouldShowSymbolSubtitle(asset: AssetEntity, primary: String): Boolean {
    if (asset.category != AssetCategory.METAL) return false
    val sym = asset.symbol.trim()
    if (sym.isEmpty()) return false
    val p = primary.trim()
    if (p.isEmpty()) return false
    if (p.equals(sym, ignoreCase = true)) return false
    if (p.contains(sym, ignoreCase = true)) return false
    return true
}

private val METAL_HEADLINE_CAMEL_BOUNDARY = Regex("([a-z])([A-Z])")
private val METAL_HEADLINE_RUNON_PREFIX =
    Regex("^(?i)(platinum|palladium|silver|gold)(.+)$")

/** Spaces, underscores, and camelCase so stored titles like `SilverBar` or `SILVER_BAR` split cleanly. */
private fun normalizeMetalPrimaryForSplit(raw: String): String {
    val t = raw.trim()
    if (t.isEmpty()) return t
    var s = t.replace('_', ' ')
    s = METAL_HEADLINE_CAMEL_BOUNDARY.replace(s) { "${it.groupValues[1]} ${it.groupValues[2]}" }
    return s
}

/**
 * Two-line metal title for widget + collapsed/expanded cards: first token / remainder.
 * Handles spaces, [normalizeMetalPrimaryForSplit], and run‑together bullion names (`SILVERBAR` → SILVER / BAR).
 */
internal fun metalWidgetHeadlinePair(primary: String): Pair<String, String?> {
    val raw = primary.trim()
    if (raw.isEmpty()) return "" to null
    val spaced = normalizeMetalPrimaryForSplit(raw)
    val idx = spaced.indexOf(' ')
    if (idx >= 0) {
        val first = spaced.substring(0, idx).trim().uppercase(Locale.US)
        val rest = spaced.substring(idx + 1).trim().uppercase(Locale.US)
        return if (rest.isEmpty()) first to null else first to rest
    }
    val runon = METAL_HEADLINE_RUNON_PREFIX.find(spaced)
    if (runon != null) {
        val head = runon.groupValues[1].trim().uppercase(Locale.US)
        val tail = runon.groupValues[2].trim().uppercase(Locale.US)
        if (tail.isNotEmpty()) return head to tail
    }
    return spaced.uppercase(Locale.US) to null
}

/** MetalIcon tint logic: keep symbol in the string so gradients match even if the user title omits \"Silver\". */
private fun metalIconLookupName(asset: AssetEntity): String =
    when (asset.category) {
        AssetCategory.METAL -> {
            val seen = mutableSetOf<String>()
            val parts = mutableListOf<String>()
            for (s in listOf(asset.symbol, asset.displayName, asset.name)) {
                val t = s.trim()
                if (t.isEmpty()) continue
                val key = t.lowercase(Locale.US)
                if (seen.add(key)) parts.add(t)
            }
            parts.joinToString(" ").ifBlank { asset.symbol }
        }
        AssetCategory.CRYPTO -> asset.symbol
    }

@Composable
private fun AssetUnderIconNameBlock(
    asset: AssetEntity,
    cardText: Color,
    symSize: TextUnit,
    lineHeight: TextUnit,
    platform: PlatformTextStyle,
) {
    if (asset.category != AssetCategory.METAL) {
        Text(
            text = underIconTickerText(asset).uppercase(Locale.US),
            modifier = Modifier.fillMaxWidth(),
            style = LocalTextStyle.current.merge(
                TextStyle(
                    color = cardText,
                    fontWeight = FontWeight.Black,
                    fontSize = symSize,
                    textAlign = TextAlign.Center,
                    lineHeight = lineHeight,
                    platformStyle = platform,
                )
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        return
    }
    val primary = metalCardPrimaryLabel(asset)
    val (headLine1, headLine2) = metalWidgetHeadlinePair(primary)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = headLine1,
            modifier = Modifier.fillMaxWidth(),
            style = LocalTextStyle.current.merge(
                TextStyle(
                    color = cardText,
                    fontWeight = FontWeight.Black,
                    fontSize = symSize,
                    textAlign = TextAlign.Center,
                    lineHeight = lineHeight,
                    platformStyle = platform,
                )
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        if (headLine2 != null) {
            Text(
                text = headLine2,
                modifier = Modifier.fillMaxWidth(),
                style = LocalTextStyle.current.merge(
                    TextStyle(
                        color = cardText,
                        fontWeight = FontWeight.Black,
                        fontSize = symSize,
                        textAlign = TextAlign.Center,
                        lineHeight = lineHeight,
                        platformStyle = platform,
                    )
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (metalShouldShowSymbolSubtitle(asset, primary)) {
            Text(
                text = asset.symbol.trim().uppercase(Locale.US),
                modifier = Modifier.fillMaxWidth(),
                style = LocalTextStyle.current.merge(
                    TextStyle(
                        color = cardText.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Black,
                        fontSize = symSize * 0.82f,
                        textAlign = TextAlign.Center,
                        lineHeight = lineHeight,
                        platformStyle = platform,
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

data class CryptoEditSave(
    val amountHeld: Double,
    val decimalPreference: Int,
    val localIconPath: String?,
    val iconChanged: Boolean,
)

@Composable
fun CryptoEditFunnel(
    asset: AssetEntity,
    onDismiss: () -> Unit,
    onSave: (CryptoEditSave) -> Unit,
    persistCustomIcon: suspend (String, Uri) -> String?,
    deleteCustomIcon: suspend (String) -> Unit
) {
    var amt by remember(asset.coinId) { mutableStateOf(asset.amountHeld.toString()) }
    var dec by remember(asset.coinId) { mutableFloatStateOf(asset.decimalPreference.toFloat()) }
    var pendingIconUri by remember(asset.coinId) { mutableStateOf<Uri?>(null) }
    var userClearedCustom by remember(asset.coinId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    val scroll = rememberScrollState()
    val context = LocalContext.current

    val cropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            UCrop.getOutput(result.data!!)?.let { out ->
                pendingIconUri = out
                userClearedCustom = false
            }
        }
    }

    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val act = context.findComponentActivity()
        if (act == null) {
            Log.w(CROP_TAG, "No ComponentActivity; using uncropped image")
            pendingIconUri = uri
            userClearedCustom = false
            return@rememberLauncherForActivityResult
        }
        try {
            val sourceForCrop = copyPickerUriToCacheForCrop(act, uri) ?: uri
            cropLauncher.launch(buildIconCropIntent(act, sourceForCrop))
        } catch (e: Exception) {
            Log.e(CROP_TAG, "Failed to start crop: ${e.message}", e)
            pendingIconUri = uri
            userClearedCustom = false
        }
    }

    val previewModel: Any? = when {
        userClearedCustom -> asset.imageUrl.takeIf { it.isNotBlank() }
        pendingIconUri != null -> pendingIconUri
        else -> asset.localIconPath?.let { path -> File(path).takeIf { it.exists() } }
            ?: asset.imageUrl.takeIf { it.isNotBlank() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(1.dp, Color.White.copy(0.1f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scroll)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                Text(stringResource(R.string.crypto_settings_title, asset.symbol.uppercase()), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.asset_custom_icon_section), color = Color.White.copy(0.6f), fontSize = 10.sp)
                Text(stringResource(R.string.asset_custom_icon_hint), color = Color.White.copy(0.35f), fontSize = 9.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewModel != null) {
                        AsyncImage(
                            model = previewModel,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = asset.symbol.trim().take(1).ifBlank { "?" }.uppercase(Locale.US),
                            color = Color.Yellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Yellow),
                        border = BorderStroke(1.dp, Color.Yellow.copy(0.5f))
                    ) {
                        Text(stringResource(R.string.asset_custom_icon_choose), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                val canRecrop = pendingIconUri != null && !userClearedCustom
                OutlinedButton(
                    onClick = {
                        val act = context.findComponentActivity() ?: return@OutlinedButton
                        val src = pendingIconUri ?: return@OutlinedButton
                        try {
                            cropLauncher.launch(buildIconCropIntent(act, src))
                        } catch (e: Exception) {
                            Log.e(CROP_TAG, "Re-crop failed: ${e.message}")
                        }
                    },
                    enabled = canRecrop,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(0.85f),
                        disabledContentColor = Color.Gray.copy(0.35f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(if (canRecrop) 0.25f else 0.08f))
                ) {
                    Text(stringResource(R.string.asset_custom_icon_recrop), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                val canRemoveCustom = asset.localIconPath != null || pendingIconUri != null
                TextButton(
                    onClick = {
                        pendingIconUri = null
                        userClearedCustom = true
                    },
                    enabled = canRemoveCustom
                ) {
                    Text(
                        stringResource(R.string.asset_custom_icon_remove),
                        color = if (canRemoveCustom) Color.White.copy(0.7f) else Color.Gray.copy(0.4f),
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.crypto_quantity_held), color = Color.White.copy(0.6f), fontSize = 10.sp)
                BasicTextField(value = amt, onValueChange = { amt = it }, textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 28.sp, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().focusRequester(focus))
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.crypto_price_decimals, dec.toInt()), color = Color.White.copy(0.6f), fontSize = 10.sp)
                Slider(value = dec, onValueChange = { dec = it }, valueRange = 0f..8f, steps = 7, colors = SliderDefaults.colors(thumbColor = Color.Yellow, activeTrackColor = Color.Yellow))
                Button(
                    onClick = {
                        scope.launch {
                            val finalLocal = when {
                                userClearedCustom -> {
                                    deleteCustomIcon(asset.coinId)
                                    null
                                }
                                pendingIconUri != null -> {
                                    persistCustomIcon(asset.coinId, pendingIconUri!!) ?: asset.localIconPath
                                }
                                else -> asset.localIconPath
                            }
                            val iconChanged = userClearedCustom || pendingIconUri != null
                            onSave(
                                CryptoEditSave(
                                    amountHeld = amt.toDoubleOrNull() ?: asset.amountHeld,
                                    decimalPreference = dec.toInt(),
                                    localIconPath = finalLocal,
                                    iconChanged = iconChanged,
                                )
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.action_save_changes), fontWeight = FontWeight.Black)
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = Color.Gray) }
                }
            }
        }
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
}

/**
 * Step 3 — same pick → crop → preview flow as [CryptoEditFunnel] icon section, plus explicit default icon.
 */
@Composable
fun ArchitectIconSelectionStep(
    displayName: String,
    weight: Double,
    weightUnit: String,
    physicalForm: String,
    coinId: String,
    existingLocalIconPath: String?,
    imageUrl: String,
    isEditingExisting: Boolean,
    onBack: () -> Unit,
    persistCustomIcon: suspend (String, Uri) -> String?,
    deleteCustomIcon: suspend (String) -> Unit,
    onFinished: (localIconPath: String?) -> Unit,
    walkthroughController: HoldingsWalkthroughController? = null,
    walkthroughStep: HoldingsWalkthroughStep = HoldingsWalkthroughStep.INACTIVE,
    onTourIconSaving: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingIconUri by remember(coinId) { mutableStateOf<Uri?>(null) }
    var userClearedCustom by remember(coinId) { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val overlayInteraction = remember { MutableInteractionSource() }

    val cropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            UCrop.getOutput(result.data!!)?.let { out ->
                pendingIconUri = out
                userClearedCustom = false
            }
        }
    }

    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val act = context.findComponentActivity()
        if (act == null) {
            Log.w(CROP_TAG, "No ComponentActivity; using uncropped image")
            pendingIconUri = uri
            userClearedCustom = false
            return@rememberLauncherForActivityResult
        }
        try {
            val sourceForCrop = copyPickerUriToCacheForCrop(act, uri) ?: uri
            cropLauncher.launch(buildIconCropIntent(act, sourceForCrop))
        } catch (e: Exception) {
            Log.e(CROP_TAG, "Failed to start crop: ${e.message}", e)
            pendingIconUri = uri
            userClearedCustom = false
        }
    }

    val previewModel: Any? = when {
        pendingIconUri != null -> pendingIconUri
        userClearedCustom -> null
        else -> resolveCustomIconFile(context, coinId, existingLocalIconPath)
            ?: imageUrl.takeIf { it.isNotBlank() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll, enabled = !isSaving)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.asset_custom_icon_section), color = Color.White.copy(0.6f), fontSize = 10.sp)
        Text(
            stringResource(R.string.asset_custom_icon_hint),
            color = Color.White.copy(0.35f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.06f)),
            contentAlignment = Alignment.Center,
        ) {
            if (previewModel != null) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                MetalIcon(
                    name = displayName,
                    weight = weight,
                    unit = weightUnit,
                    physicalForm = physicalForm,
                    category = AssetCategory.METAL,
                    size = 72,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Yellow),
                border = BorderStroke(1.dp, Color.Yellow.copy(0.5f)),
            ) {
                Text(stringResource(R.string.asset_custom_icon_choose), fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
        val canRecrop = pendingIconUri != null && !userClearedCustom
        OutlinedButton(
            onClick = {
                val act = context.findComponentActivity() ?: return@OutlinedButton
                val src = pendingIconUri ?: return@OutlinedButton
                try {
                    cropLauncher.launch(buildIconCropIntent(act, src))
                } catch (e: Exception) {
                    Log.e(CROP_TAG, "Re-crop failed: ${e.message}")
                }
            },
            enabled = canRecrop,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White.copy(0.85f),
                disabledContentColor = Color.Gray.copy(0.35f),
            ),
            border = BorderStroke(1.dp, Color.White.copy(if (canRecrop) 0.25f else 0.08f)),
        ) {
            Text(stringResource(R.string.asset_custom_icon_recrop), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        val canUseDefault = existingLocalIconPath != null || pendingIconUri != null
        OutlinedButton(
            onClick = {
                scope.launch {
                    deleteCustomIcon(coinId)
                    pendingIconUri = null
                    userClearedCustom = true
                }
            },
            enabled = canUseDefault,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White,
                disabledContentColor = Color.Gray.copy(0.4f),
            ),
            border = BorderStroke(1.dp, Color.White.copy(if (canUseDefault) 0.35f else 0.12f)),
        ) {
            Text(stringResource(R.string.asset_custom_icon_remove), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onBack, enabled = !isSaving) {
            Text(
                stringResource(R.string.action_back),
                color = if (isSaving) Color.Gray.copy(0.35f) else Color.Gray,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                onTourIconSaving?.invoke()
                scope.launch {
                    isSaving = true
                    try {
                        val finalLocal = when {
                            userClearedCustom -> {
                                deleteCustomIcon(coinId)
                                null
                            }
                            pendingIconUri != null ->
                                persistCustomIcon(coinId, pendingIconUri!!) ?: existingLocalIconPath
                            else -> existingLocalIconPath
                        }
                        onFinished(finalLocal)
                    } catch (e: Exception) {
                        Log.e(CROP_TAG, "Architect icon save failed: ${e.message}", e)
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .then(
                    if (walkthroughController != null) {
                        Modifier.walkthroughAnchor(
                            anchor = WalkthroughAnchor.METAL_ARCHITECT_ICON_ADD,
                            controller = walkthroughController,
                            enabled = walkthroughStep == HoldingsWalkthroughStep.METAL_ARCHITECT_ICON_PICK,
                        )
                    } else {
                        Modifier
                    },
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                stringResource(
                    if (isEditingExisting) R.string.architect_icon_cta_update
                    else R.string.architect_icon_cta_add
                ),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
        if (isSaving) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = overlayInteraction,
                        onClick = {},
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp),
                        color = Color.Yellow,
                        trackColor = Color.White.copy(alpha = 0.18f),
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        stringResource(R.string.architect_saving_in_progress),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun FullAssetCard(
    asset: AssetEntity,
    isExpanded: Boolean,
    isEditing: Boolean,
    isDragging: Boolean,
    showEditButton: Boolean,
    cardBg: Color,
    cardText: Color,
    modifier: Modifier = Modifier,
    baseCurrency: String = "USD",
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit,
    onSave: (newName: String, newAmount: Double, newWeight: Double, weightUnit: String, decimals: Int) -> Unit,
    onCancel: () -> Unit = {},
    isHighVisibilityMode: Boolean = false,
    localIconReloadNonce: Int = 0,
) {
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    val scale by animateFloatAsState(if (isDragging) 1.03f else 1f, label = "grabScale")
    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "grabElevation")
    val alpha by animateFloatAsState(if (isDragging) 0.9f else 1f, label = "grabAlpha")

    AssetCardFontScaleScope {
        val hi = isHighVisibilityMode
        val symSize = if (hi) 14.sp else 12.sp
        val nameLabelSize = if (hi) 13.sp else 10.sp
        val amountSize = if (hi) 36.sp else 32.sp
        val metalDetailSize = if (hi) 11.sp else 9.sp
        val changePctSize = if (hi) 10.sp else 8.sp
        val statLabelSize = if (hi) 11.sp else 9.sp
        val priceValSize = if (hi) 18.sp else 15.sp
        val totalValSize = if (hi) 22.sp else 17.sp
        val platformCluster =
            if (hi) PlatformTextStyle(includeFontPadding = false)
            else PlatformTextStyle(includeFontPadding = true)
        val lineTight = if (hi) 1.em else 1.35.em
        val cardPaddingH = if (hi) 10.dp else 14.dp
        val iconSymbolGap = if (hi) 4.dp else 7.dp
        val sectionGapV = if (hi) 6.dp else 10.dp

        Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    shadowElevation = elevation.toPx()
                    this.alpha = alpha
                    clip = true
                    shape = RoundedCornerShape(16.dp)
                }
                .clickable(enabled = !isEditing) { onExpandToggle() },
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(horizontal = cardPaddingH, vertical = cardPaddingH)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    // Anchor Left: Icon Slot (80dp); height grows for two-line metal names.
                    Column(
                        modifier = Modifier.width(80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        key(asset.coinId, localIconReloadNonce, asset.localIconPath) {
                            MetalIcon(
                                name = metalIconLookupName(asset),
                                weight = asset.weight,
                                unit = asset.weightUnit,
                                physicalForm = asset.physicalForm,
                                coinId = asset.coinId,
                                imageUrl = asset.imageUrl,
                                localPath = asset.localIconPath,
                                category = asset.category,
                                size = 44, // Master Icon Scale
                                localIconReloadNonce = localIconReloadNonce,
                            )
                        }
                        Spacer(Modifier.height(iconSymbolGap))
                        AssetUnderIconNameBlock(
                            asset = asset,
                            cardText = cardText,
                            symSize = symSize,
                            lineHeight = lineTight,
                            platform = platformCluster,
                        )
                    }

                    // Middle Slot: The Real Estate (weight 1f)
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        val nameToUse = asset.displayName.ifEmpty { asset.name }

                        if (asset.category != AssetCategory.METAL) {
                            Text(
                                text = nameToUse.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                style = LocalTextStyle.current.merge(
                                    TextStyle(
                                        color = cardText.copy(alpha = 0.5f),
                                        fontSize = nameLabelSize,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                        lineHeight = lineTight,
                                        platformStyle = platformCluster,
                                    )
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        // Amount held: measure wall = 85% of middle column; shrink floor 12.sp.
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            AutoResizingText(
                                text = formatAmount(asset.amountHeld),
                                style = LocalTextStyle.current.merge(
                                    TextStyle(
                                        color = cardText,
                                        fontWeight = FontWeight.Black,
                                        fontSize = amountSize,
                                        textAlign = TextAlign.Center,
                                        lineHeight = lineTight,
                                        platformStyle = platformCluster,
                                    )
                                ),
                                modifier = Modifier
                                    .width(maxWidth * 0.85f)
                                    .align(Alignment.Center),
                                maxFontSize = amountSize,
                                minFontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                        if (asset.category == AssetCategory.METAL) {
                            Text(
                                text = "${formatAmount(asset.weight)} ${asset.weightUnit}".uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                style = LocalTextStyle.current.merge(
                                    TextStyle(
                                        color = cardText.copy(alpha = 0.4f),
                                        fontSize = metalDetailSize,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = lineTight,
                                        platformStyle = platformCluster,
                                    )
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Anchor Right: Action Slot (64dp)
                    Column(modifier = Modifier.width(64.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                        if (showEditButton && !isEditing) {
                            IconButton(
                                onClick = { onEditRequest() },
                                modifier = Modifier.size(40.dp).background(Color.Yellow, CircleShape)
                            ) {
                                Icon(Icons.Default.Edit, null, tint = Color.Black)
                            }
                        } else {
                            SparklineChart(asset.sparklineData, Modifier.width(64.dp).height(30.dp).padding(top = 8.dp))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(horizontal = 4.dp)) {
                                Icon(if (asset.priceChange24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = trendColor, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.1f", asset.priceChange24h)}%",
                                    style = LocalTextStyle.current.merge(
                                        TextStyle(
                                            color = trendColor,
                                            fontSize = changePctSize,
                                            fontWeight = FontWeight.Black,
                                            lineHeight = lineTight,
                                            platformStyle = platformCluster,
                                        )
                                    ),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(sectionGapV))
                HorizontalDivider(color = cardText.copy(alpha = 0.05f))
                Spacer(Modifier.height(sectionGapV))
                Row(modifier = Modifier.fillMaxWidth()) {
                    val priceLabel = if (asset.baseSymbol == "CUSTOM") stringResource(R.string.asset_price_label_value) else stringResource(R.string.asset_price_label_price)
                    Column(modifier = Modifier.weight(0.35f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            priceLabel,
                            style = LocalTextStyle.current.merge(
                                TextStyle(
                                    color = cardText.copy(0.6f),
                                    fontSize = statLabelSize,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = lineTight,
                                    platformStyle = platformCluster,
                                )
                            ),
                        )
                        AutoResizingText(
                            text = formatBoutiquePrice(AssetValuation.cardPriceRowUsd(asset), baseCurrency),
                            style = TextStyle(
                                color = cardText,
                                fontWeight = FontWeight.Bold,
                                fontSize = priceValSize,
                                textAlign = TextAlign.Center,
                                lineHeight = lineTight,
                                platformStyle = platformCluster,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            maxFontSize = priceValSize,
                            minFontSize = 10.sp,
                        )
                    }
                    Column(modifier = Modifier.weight(0.65f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.asset_total_value_label),
                            style = LocalTextStyle.current.merge(
                                TextStyle(
                                    color = cardText.copy(0.6f),
                                    fontSize = statLabelSize,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = lineTight,
                                    platformStyle = platformCluster,
                                )
                            ),
                        )
                        AutoResizingText(
                            text = formatCurrency(AssetValuation.spotMassHoldingsUsd(asset), 2, baseCurrency),
                            style = TextStyle(
                                color = cardText,
                                fontWeight = FontWeight.Black,
                                fontSize = totalValSize,
                                textAlign = TextAlign.Center,
                                lineHeight = lineTight,
                                platformStyle = platformCluster,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            maxFontSize = totalValSize,
                            minFontSize = 10.sp,
                        )
                    }
                }
            }
        }
        WatermarkBadge(
            source = asset.priceSource,
            color = cardText,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 20.dp, top = 0.dp).offset(y = (-4).dp).zIndex(1f)
        )
        }
    }
}

@Composable
fun PolishedAssetCard(
    asset: AssetEntity,
    isDragging: Boolean,
    cardBg: Color,
    cardText: Color,
    modifier: Modifier = Modifier,
    baseCurrency: String = "USD",
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit = {},
    isExpanded: Boolean = false,
    showEditButton: Boolean = false,
    localIconReloadNonce: Int = 0,
) {
    CompactAssetCard(
        asset = asset,
        isDragging = isDragging,
        cardBg = cardBg,
        cardText = cardText,
        baseCurrency = baseCurrency,
        onExpandToggle = onExpandToggle,
        onEditRequest = onEditRequest,
        modifier = modifier,
        isExpanded = isExpanded,
        showEditButton = showEditButton,
        isHighVisibilityMode = false,
        variant = CompactCardVariant.Polished,
        localIconReloadNonce = localIconReloadNonce,
    )
}

@Composable
fun HighDensityAssetCard(
    asset: AssetEntity,
    isDragging: Boolean,
    cardBg: Color,
    cardText: Color,
    modifier: Modifier = Modifier,
    baseCurrency: String = "USD",
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit = {},
    isExpanded: Boolean = false,
    showEditButton: Boolean = false,
    localIconReloadNonce: Int = 0,
) {
    CompactAssetCard(
        asset = asset,
        isDragging = isDragging,
        cardBg = cardBg,
        cardText = cardText,
        baseCurrency = baseCurrency,
        onExpandToggle = onExpandToggle,
        onEditRequest = onEditRequest,
        modifier = modifier,
        isExpanded = isExpanded,
        showEditButton = showEditButton,
        isHighVisibilityMode = true,
        variant = CompactCardVariant.HighDensity,
        localIconReloadNonce = localIconReloadNonce,
    )
}

enum class CompactCardVariant {
    Polished,
    HighDensity,
}

@Composable
fun CompactAssetCard(
    asset: AssetEntity,
    isDragging: Boolean,
    cardBg: Color,
    cardText: Color,
    modifier: Modifier = Modifier,
    baseCurrency: String = "USD",
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit = {},
    isExpanded: Boolean = false,
    showEditButton: Boolean = false,
    /** Dense dashboard typography vs airy boutique row. */
    isHighVisibilityMode: Boolean = false,
    variant: CompactCardVariant? = null,
    localIconReloadNonce: Int = 0,
) {
    val scale by animateFloatAsState(if (isDragging) 1.03f else 1f, label = "compactGrabScale")
    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "compactGrabElevation")
    val alpha by animateFloatAsState(if (isDragging) 0.9f else 1f, label = "compactGrabAlpha")
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)

    AssetCardFontScaleScope {
        val activeVariant = variant ?: if (isHighVisibilityMode) CompactCardVariant.HighDensity else CompactCardVariant.Polished
        val hi = activeVariant == CompactCardVariant.HighDensity
        val showCollapsedSparkline = true
        val symSize = if (hi) 14.sp else 12.sp
        val nameLabelSize = if (hi) 13.sp else 10.sp
        val amountSize = if (hi) 36.sp else 32.sp
        val metalDetailSize = if (hi) 11.sp else 9.sp
        val changePctSize = if (hi) 10.sp else 8.sp
        val statLabelSize = if (hi) 11.sp else 9.sp
        val priceValSize = if (hi) 18.sp else 15.sp
        val totalValSize = if (hi) 22.sp else 17.sp
        val expandedLine = if (hi) 1.em else 1.35.em
        val expandedPlatform =
            if (hi) PlatformTextStyle(includeFontPadding = false)
            else PlatformTextStyle(includeFontPadding = true)
        val collapsedPadH = if (hi) 10.dp else 14.dp
        val iconTextGapW = if (hi) 4.dp else 8.dp
        val sparklineStartPad = if (hi) 4.dp else 8.dp
        val expandedPad = if (hi) 10.dp else 14.dp
        // Room for two-line user title plus optional spot ticker (e.g. XAG) under the icon.
        val expandedMinHeight = if (hi) 180.dp else 202.dp
        val expandedDividerGapV = if (hi) 6.dp else 10.dp
        val expandedIconSymbolGap = if (hi) 4.dp else 8.dp

        Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    shadowElevation = elevation.toPx()
                    this.alpha = alpha
                    clip = true
                    shape = RoundedCornerShape(12.dp)
                }
                .clickable { onExpandToggle() },
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))
        ) {
            Column {
                // V37.1 Single-Truth Fix: Collapsed header ONLY visible if !isExpanded
                if (!isExpanded) {
                    val tickerSize = if (hi) 18.sp else 16.sp
                    val priceSize = if (hi) 14.sp else 14.sp
                    val totalSize = if (hi) 24.sp else 20.sp
                    val lineCluster = if (hi) 1.em else 1.32.em
                    val platformCluster =
                        if (hi) PlatformTextStyle(includeFontPadding = false)
                        else PlatformTextStyle(includeFontPadding = true)
                    val rowHeight = if (hi) 64.dp else 78.dp
                    val sparklineOuterH = if (hi) 36.dp else 30.dp
                    val sparklineInnerH = if (hi) 34.dp else 28.dp

                    BoxWithConstraints(
                        modifier = Modifier
                            .padding(horizontal = collapsedPadH)
                            .heightIn(min = 64.dp)
                            .height(rowHeight)
                            .fillMaxWidth()
                    ) {
                        val titleText = if (asset.category == AssetCategory.METAL) {
                            metalCardPrimaryLabel(asset)
                        } else {
                            asset.symbol
                        }
                        val leftClusterCap = if (hi) maxWidth * 0.32f else maxWidth * 0.40f
                        val rightColumnCap = if (hi) maxWidth * 0.65f else maxWidth * 0.28f
                        val iconAndGap = 38.dp + iconTextGapW
                        val subPriceColumnMax = (leftClusterCap - iconAndGap).coerceAtLeast(48.dp)

                        Row(
                            modifier = Modifier.width(maxWidth),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left cluster: icon + ticker + sub-price (bounded width so sub-price can auto-shrink)
                            Row(
                                modifier = Modifier
                                    .widthIn(max = leftClusterCap.coerceAtLeast(iconAndGap + 48.dp))
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(cardText.copy(alpha = 0.08f))
                                        .padding(3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    key(asset.coinId, localIconReloadNonce, asset.localIconPath) {
                                        MetalIcon(
                                            name = metalIconLookupName(asset),
                                            weight = asset.weight,
                                            unit = asset.weightUnit,
                                            physicalForm = asset.physicalForm,
                                            coinId = asset.coinId,
                                            size = 30,
                                            imageUrl = asset.imageUrl,
                                            localPath = asset.localIconPath,
                                            category = asset.category,
                                            localIconReloadNonce = localIconReloadNonce,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(iconTextGapW))
                                Column(
                                    modifier = Modifier
                                        .widthIn(max = subPriceColumnMax)
                                        .fillMaxHeight(),
                                    verticalArrangement = if (hi) {
                                        Arrangement.Center
                                    } else {
                                        Arrangement.SpaceEvenly
                                    },
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    if (asset.category == AssetCategory.METAL) {
                                        // Explicit two lines (same logic as widget): avoids soft-wrap breaking mid-word (SILVE / R CO…).
                                        val (h1, h2) = metalWidgetHeadlinePair(titleText)
                                        Text(
                                            text = h1,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = LocalTextStyle.current.copy(
                                                color = cardText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = tickerSize,
                                                lineHeight = lineCluster,
                                                platformStyle = platformCluster,
                                                textAlign = TextAlign.Start,
                                            ),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (h2 != null) {
                                            Text(
                                                text = h2,
                                                modifier = Modifier.fillMaxWidth(),
                                                style = LocalTextStyle.current.copy(
                                                    color = cardText,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = tickerSize,
                                                    lineHeight = lineCluster,
                                                    platformStyle = platformCluster,
                                                    textAlign = TextAlign.Start,
                                                ),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    } else {
                                        AutoResizingText(
                                            text = titleText.uppercase(Locale.US),
                                            style = LocalTextStyle.current.copy(
                                                color = cardText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = tickerSize,
                                                lineHeight = lineCluster,
                                                platformStyle = platformCluster,
                                                textAlign = TextAlign.Start,
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            maxFontSize = tickerSize,
                                            minFontSize = 12.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    AutoResizingText(
                                        text = formatBoutiquePrice(AssetValuation.cardPriceRowUsd(asset), baseCurrency),
                                        style = LocalTextStyle.current.copy(
                                            color = cardText.copy(alpha = 0.75f),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = priceSize,
                                            lineHeight = lineCluster,
                                            platformStyle = platformCluster,
                                            textAlign = TextAlign.Start,
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        maxFontSize = priceSize,
                                        minFontSize = 10.sp,
                                    )
                                }
                            }

                        // Sparkline: immediately after price, height matches two-line text block
                        if (showCollapsedSparkline) {
                            Box(
                                modifier = Modifier
                                    .padding(
                                        start = if (hi) 4.dp else sparklineStartPad,
                                        end = if (hi) 4.dp else 0.dp
                                    )
                                    .width(76.dp)
                                    .height(sparklineOuterH),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                SparklineChart(
                                    historyData = asset.sparklineData,
                                    modifier = Modifier
                                        .width(72.dp)
                                        .height(sparklineInnerH),
                                    lineColorOverride = trendColor,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Right cluster: mirror typography, snapped vertical pair
                        Column(
                            modifier = Modifier
                                .widthIn(max = rightColumnCap.coerceAtLeast(64.dp))
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = if (hi) {
                                Arrangement.Center
                            } else {
                                Arrangement.SpaceEvenly
                            },
                        ) {
                            if (showEditButton) {
                                IconButton(
                                    onClick = { onEditRequest() },
                                    modifier = Modifier.size(36.dp).background(Color.Yellow, CircleShape)
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                }
                            } else {
                                val collapsedHoldingValue =
                                    AssetValuation.spotMassHoldingsUsd(asset) +
                                        if (asset.category == AssetCategory.CRYPTO) asset.premium else 0.0
                                AutoResizingText(
                                    text = formatCurrency(collapsedHoldingValue, 2, baseCurrency),
                                    style = LocalTextStyle.current.merge(
                                        TextStyle(
                                            color = cardText,
                                            fontWeight = if (hi) FontWeight.Black else FontWeight.ExtraBold,
                                            fontSize = totalSize,
                                            lineHeight = lineCluster,
                                            textAlign = TextAlign.End,
                                            platformStyle = platformCluster
                                        )
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    maxFontSize = totalSize,
                                    minFontSize = 12.sp,
                                )
                                Text(
                                    text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.1f", asset.priceChange24h)}%",
                                    style = LocalTextStyle.current.copy(
                                        color = trendColor.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = priceSize,
                                        lineHeight = lineCluster,
                                        textAlign = TextAlign.End,
                                        platformStyle = platformCluster
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    // V37.2 Dimensional Parity: Applied min height and master padding
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = expandedMinHeight)
                            .padding(expandedPad)
                    ) {
                        // The One-Card Header (Strict structural twin of FullAssetCard)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            // Anchor Left: Icon/Symbol Slot (80dp); height grows for two-line metal names.
                            Column(
                                modifier = Modifier.width(80.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                key(asset.coinId, localIconReloadNonce, asset.localIconPath) {
                                    MetalIcon(
                                        name = metalIconLookupName(asset),
                                        weight = asset.weight,
                                        unit = asset.weightUnit,
                                        physicalForm = asset.physicalForm,
                                        coinId = asset.coinId,
                                        size = 44, // Master Icon Scale Parity
                                        imageUrl = asset.imageUrl,
                                        localPath = asset.localIconPath,
                                        category = asset.category,
                                        localIconReloadNonce = localIconReloadNonce,
                                    )
                                }
                                Spacer(Modifier.height(expandedIconSymbolGap))
                                AssetUnderIconNameBlock(
                                    asset = asset,
                                    cardText = cardText,
                                    symSize = symSize,
                                    lineHeight = expandedLine,
                                    platform = expandedPlatform,
                                )
                            }

                            // Middle Slot: Holding & Name (weight 1f)
                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                val nameToUse = asset.displayName.ifEmpty { asset.name }
                                if (asset.category != AssetCategory.METAL) {
                                    Text(
                                        text = nameToUse.uppercase(),
                                        modifier = Modifier.fillMaxWidth(),
                                        style = LocalTextStyle.current.merge(
                                            TextStyle(
                                                color = cardText.copy(alpha = 0.5f),
                                                fontSize = nameLabelSize,
                                                fontWeight = FontWeight.Black,
                                                textAlign = TextAlign.Center,
                                                lineHeight = expandedLine,
                                                platformStyle = expandedPlatform,
                                            )
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    AutoResizingText(
                                        text = formatAmount(asset.amountHeld),
                                        style = LocalTextStyle.current.merge(
                                            TextStyle(
                                                color = cardText,
                                                fontWeight = FontWeight.Black,
                                                fontSize = amountSize,
                                                textAlign = TextAlign.Center,
                                                lineHeight = expandedLine,
                                                platformStyle = expandedPlatform,
                                            )
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        maxFontSize = amountSize,
                                        minFontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                }
                                if (asset.category == AssetCategory.METAL) {
                                    Text(
                                        text = "${formatAmount(asset.weight)} ${asset.weightUnit}".uppercase(),
                                        modifier = Modifier.fillMaxWidth(),
                                        style = LocalTextStyle.current.merge(
                                            TextStyle(
                                                color = cardText.copy(alpha = 0.4f),
                                                fontSize = metalDetailSize,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                lineHeight = expandedLine,
                                                platformStyle = expandedPlatform,
                                            )
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Anchor Right: Action/Trend Slot (64dp)
                            Column(
                                modifier = Modifier.width(64.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (showEditButton) {
                                    IconButton(
                                        onClick = { onEditRequest() },
                                        modifier = Modifier.size(40.dp).background(Color.Yellow, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = Color.Black)
                                    }
                                } else {
                                    SparklineChart(asset.sparklineData, Modifier.width(64.dp).height(30.dp).padding(top = 8.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(horizontal = 4.dp)
                                    ) {
                                        Icon(if (asset.priceChange24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = trendColor, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.1f", asset.priceChange24h)}%",
                                            style = LocalTextStyle.current.merge(
                                                TextStyle(
                                                    color = trendColor,
                                                    fontSize = changePctSize,
                                                    fontWeight = FontWeight.Black,
                                                    lineHeight = expandedLine,
                                                    platformStyle = expandedPlatform,
                                                )
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(expandedDividerGapV))
                        HorizontalDivider(color = cardText.copy(alpha = 0.05f))
                        Spacer(Modifier.height(expandedDividerGapV))

                        // Bottom Row: Price / Total Value parity
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val priceLabel = if (asset.baseSymbol == "CUSTOM") stringResource(R.string.asset_price_label_value) else stringResource(R.string.asset_price_label_price)
                            Column(modifier = Modifier.weight(0.3f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    priceLabel,
                                    style = LocalTextStyle.current.merge(
                                        TextStyle(
                                            color = cardText.copy(0.6f),
                                            fontSize = statLabelSize,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = expandedLine,
                                            platformStyle = expandedPlatform,
                                        )
                                    ),
                                )
                                AutoResizingText(
                                    text = formatBoutiquePrice(AssetValuation.cardPriceRowUsd(asset), baseCurrency),
                                    style = TextStyle(
                                        color = cardText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = priceValSize,
                                        textAlign = TextAlign.Center,
                                        lineHeight = expandedLine,
                                        platformStyle = expandedPlatform,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxFontSize = priceValSize,
                                    minFontSize = 10.sp,
                                )
                            }
                            Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(R.string.asset_total_value_label),
                                    style = LocalTextStyle.current.merge(
                                        TextStyle(
                                            color = cardText.copy(0.6f),
                                            fontSize = statLabelSize,
                                            fontWeight = FontWeight.Black,
                                            lineHeight = expandedLine,
                                            platformStyle = expandedPlatform,
                                        )
                                    ),
                                )
                                AutoResizingText(
                                    text = formatCurrency(AssetValuation.spotMassHoldingsUsd(asset), 2, baseCurrency),
                                    style = TextStyle(
                                        color = cardText,
                                        fontWeight = FontWeight.Black,
                                        fontSize = totalValSize,
                                        textAlign = TextAlign.Center,
                                        lineHeight = expandedLine,
                                        platformStyle = expandedPlatform,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxFontSize = totalValSize,
                                    minFontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
        if (isExpanded) {
            WatermarkBadge(
                source = asset.priceSource,
                color = cardText,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 20.dp, top = 0.dp).offset(y = (-4).dp).zIndex(1f)
            )
        }
        }
    }
}

@Composable
fun MetalMarketCard(
    name: String,
    symbol: String,
    officialSpotPrice: Double,
    changePercent: Double,
    dayHigh: Double,
    dayLow: Double,
    sparkline: List<Double>,
    isOwned: Boolean,
    cardBg: Color,
    cardText: Color,
    modifier: Modifier = Modifier,
    baseCurrency: String = "USD",
) {
    val trendColor = if (changePercent >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    AssetCardFontScaleScope {
    Card(
        modifier = modifier.fillMaxWidth().height(195.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.uppercase(),
                        fontWeight = FontWeight.Black,
                        color = cardText,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = symbol,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = cardText.copy(alpha = 0.4f),
                            maxLines = 1
                        )
                        if (isOwned) {
                            Text(text = stringResource(R.string.holdings_metal_card_owned_badge), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Yellow)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (officialSpotPrice > 0.0) {
                        Text(
                            text = formatCurrency(officialSpotPrice, 2, baseCurrency),
                            fontWeight = FontWeight.Black,
                            color = cardText,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                R.string.widget_percent_change,
                                if (changePercent >= 0) "+" else "",
                                changePercent,
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = trendColor,
                            maxLines = 1
                        )
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = Color.Yellow)
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
                if (sparkline.isNotEmpty()) SparklineChart(sparkline, Modifier.fillMaxSize())
            }
            Row(modifier = Modifier.fillMaxWidth().height(32.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val lowStr = if (dayLow <= 0.0) "${getCurrencySymbol(baseCurrency)} --.--" else formatCurrency(dayLow, 2, baseCurrency)
                    Text(
                        text = lowStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = cardText,
                        maxLines = 1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val highStr = if (dayHigh <= 0.0) "${getCurrencySymbol(baseCurrency)} --.--" else formatCurrency(dayHigh, 2, baseCurrency)
                    Text(
                        text = highStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = cardText,
                        maxLines = 1
                    )
                }
            }
        }
    }
    }
}