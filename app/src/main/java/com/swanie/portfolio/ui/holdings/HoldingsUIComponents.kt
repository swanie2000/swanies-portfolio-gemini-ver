package com.swanie.portfolio.ui.holdings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import java.io.File
import java.text.DecimalFormat
import java.util.Locale

@Composable
fun MetalIcon(
    name: String,
    weight: Double,
    unit: String = "OZ",
    physicalForm: String = "Coin",
    size: Int = 44,
    imageUrl: String = "",
    localPath: String? = null,
    category: AssetCategory = AssetCategory.METAL
) {
    var isError by remember { mutableStateOf(false) }
    val localFile = localPath?.let { File(it) }

    if (localFile != null && localFile.exists() && !isError) {
        AsyncImage(
            model = localFile,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size.dp).clip(CircleShape),
            onError = { isError = true }
        )
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
fun FunnelGrid(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { option ->
                    val isSelected = option.equals(selected, ignoreCase = true)
                    Box(modifier = Modifier.weight(1f).height(55.dp).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Color.Yellow else Color.White.copy(0.05f)).clickable { onSelect(option) }.border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text(text = option.uppercase(), color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
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

@Composable
fun MetalSelectionFunnel(
    initialMetal: String, initialForm: String, initialWeight: Double, initialQty: String, initialPrem: String, initialManualPrice: String,
    onDismiss: () -> Unit,
    onConfirmed: (String, String, Double, String, String, String, String?, Boolean, String) -> Unit,
    onNavigateToArchitect: (() -> Unit)? = null
) {
    var step by remember { mutableIntStateOf(1) }
    val startMetal = when {
        initialMetal.contains("XAU", true) -> "Gold"
        initialMetal.contains("XAG", true) -> "Silver"
        initialMetal.contains("XPT", true) -> "Platinum"
        initialMetal.contains("XPD", true) -> "Palladium"
        else -> initialMetal
    }
    var selectedMetal by remember { mutableStateOf(startMetal) }
    var l1 by remember { mutableStateOf(initialForm) }
    var l2 by remember { mutableStateOf("") }
    var selectedWeight by remember { mutableDoubleStateOf(initialWeight) }
    var selectedUnit by remember { mutableStateOf("OZ") }
    var qtyInput by remember { mutableStateOf(initialQty) }
    var premInput by remember { mutableStateOf(initialPrem) }
    var manualPriceInput by remember { mutableStateOf(initialManualPrice) }
    var customIconUri by remember { mutableStateOf<String?>(null) }
    var isTrueManualFlag by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> customIconUri = uri?.toString() }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = when(step) {
                    1 -> stringResource(R.string.funnel_select_type)
                    10 -> stringResource(R.string.funnel_name)
                    11 -> stringResource(R.string.funnel_icon)
                    12 -> stringResource(R.string.funnel_labels)
                    13 -> stringResource(R.string.funnel_value)
                    4 -> stringResource(R.string.funnel_quantity)
                    else -> stringResource(R.string.funnel_premium)
                }, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(20.dp))
                when (step) {
                    1 -> FunnelGrid(listOf("Gold", "Silver", "Platinum", "Palladium", "Custom"), selectedMetal) {
                        if (it == "Custom") {
                            if (onNavigateToArchitect != null) {
                                onNavigateToArchitect()
                            } else {
                                isTrueManualFlag = true; selectedMetal = ""; step = 10
                            }
                        }
                        else { isTrueManualFlag = false; selectedMetal = it; step = 2 }
                    }
                    10 -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        Text(stringResource(R.string.funnel_label_under_icon), color = Color.White.copy(0.5f), fontSize = 10.sp)
                        OutlinedTextField(value = selectedMetal, onValueChange = { if(it.length <= 8) selectedMetal = it }, placeholder = { Text(stringResource(R.string.funnel_enter_name), color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { if(selectedMetal.isNotBlank()) step = 11 } , modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.action_next)) }
                    }
                    11 -> {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(0.05f)).clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
                            if (customIconUri != null) {
                                AsyncImage(model = customIconUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.fillMaxSize().scale(1.5f))
                            }
                        }
                        Text(stringResource(R.string.funnel_tap_photo), color = Color.White.copy(0.5f), fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp)); Button(onClick = { step = 12 }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.action_next)) }
                    }
                    12 -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        Text(stringResource(R.string.funnel_description_lines), color = Color.White.copy(0.5f), fontSize = 10.sp)
                        OutlinedTextField(value = l1, onValueChange = { l1 = it }, placeholder = { Text(stringResource(R.string.funnel_line_1), color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Spacer(Modifier.height(8.dp)); OutlinedTextField(value = l2, onValueChange = { l2 = it }, placeholder = { Text(stringResource(R.string.funnel_line_2), color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { step = 4 }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.action_next)) }
                    }
                    13 -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        Text(stringResource(R.string.funnel_unit_value), color = Color.White.copy(0.5f), fontSize = 10.sp); OutlinedTextField(value = manualPriceInput, onValueChange = { manualPriceInput = it }, placeholder = { Text("0.00", color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { onConfirmed(selectedMetal, "$l1\n$l2".trim(), 1.0, "OZ", qtyInput, "0.0", customIconUri, true, manualPriceInput) }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.action_finalize)) }
                    }
                    2 -> FunnelGrid(listOf("Bars", "Coins", "Rounds", "Custom"), l1) { l1 = it; step = 3 }
                    3 -> FunnelGrid(listOf("1/10 OZ", "1 OZ", "10 OZ", "100 OZ", "1 KILO", "1 GRAM", "Custom"), "") { label ->
                        val common = mapOf(
                            "1/10 OZ" to (0.1 to "OZ"),
                            "1 OZ" to (1.0 to "OZ"),
                            "10 OZ" to (10.0 to "OZ"),
                            "100 OZ" to (100.0 to "OZ"),
                            "1 KILO" to (32.1507 to "KILO"),
                            "1 GRAM" to (0.0321507 to "GRAM")
                        )
                        val data = common[label] ?: (1.0 to "OZ")
                        selectedWeight = data.first
                        selectedUnit = data.second
                        step = 4
                    }
                    4 -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        OutlinedTextField(value = qtyInput, onValueChange = { qtyInput = it }, placeholder = { Text(stringResource(R.string.funnel_enter_quantity), color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { if(qtyInput.isNotBlank()) { if(isTrueManualFlag) step = 13 else step = 5 } }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.action_next)) }
                    }
                    else -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        OutlinedTextField(value = premInput, onValueChange = { premInput = it }, placeholder = { Text("0.00", color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { onConfirmed(selectedMetal, l1, selectedWeight, selectedUnit, qtyInput, premInput.ifBlank { "0.0" }, null, false, "0.0") }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.action_finalize)) }
                    }
                }
                if (step > 1) { val prev = when(step) { 10 -> 1; 11 -> 10; 12 -> 11; 13 -> 4; else -> step - 1 }; TextButton(onClick = { step = prev }) { Text(stringResource(R.string.action_back), color = Color.Gray) } }
            }
        }
    }
}

@Composable
fun CryptoEditFunnel(asset: AssetEntity, onDismiss: () -> Unit, onSave: (String, Double, Int) -> Unit) {
    var amt by remember { mutableStateOf(asset.amountHeld.toString()) }
    var dec by remember { mutableFloatStateOf(asset.decimalPreference.toFloat()) }
    val focus = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.crypto_settings_title, asset.symbol.uppercase()), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp); Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.crypto_quantity_held), color = Color.White.copy(0.6f), fontSize = 10.sp); BasicTextField(value = amt, onValueChange = { amt = it }, textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 28.sp, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().focusRequester(focus))
                Spacer(Modifier.height(20.dp)); Text(stringResource(R.string.crypto_price_decimals, dec.toInt()), color = Color.White.copy(0.6f), fontSize = 10.sp); Slider(value = dec, onValueChange = { dec = it }, valueRange = 0f..8f, steps = 7, colors = SliderDefaults.colors(thumbColor = Color.Yellow, activeTrackColor = Color.Yellow))
                Button(onClick = { onSave(asset.name, amt.toDoubleOrNull() ?: asset.amountHeld, dec.toInt()) }, modifier = Modifier.fillMaxWidth().padding(top = 30.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.action_save_changes), fontWeight = FontWeight.Black) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = Color.Gray) }
            }
        }
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
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
    baseCurrency: String = "USD",
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit,
    onSave: (newName: String, newAmount: Double, newWeight: Double, weightUnit: String, decimals: Int) -> Unit,
    onCancel: () -> Unit = {},
    isHighVisibilityMode: Boolean = false,
    modifier: Modifier = Modifier
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
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // Anchor Left: Icon Slot (80dp)
                    Column(
                        modifier = Modifier
                            .width(80.dp)
                            .height(95.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        MetalIcon(
                            name = asset.symbol,
                            weight = asset.weight,
                            unit = asset.weightUnit,
                            physicalForm = asset.physicalForm,
                            imageUrl = asset.imageUrl,
                            localPath = asset.localIconPath,
                            category = asset.category,
                            size = 44 // Master Icon Scale
                        )
                        Spacer(Modifier.height(iconSymbolGap))
                        Text(
                            text = asset.symbol.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            style = LocalTextStyle.current.merge(
                                TextStyle(
                                    color = cardText,
                                    fontWeight = FontWeight.Black,
                                    fontSize = symSize,
                                    textAlign = TextAlign.Center,
                                    lineHeight = lineTight,
                                    platformStyle = platformCluster,
                                )
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Middle Slot: The Real Estate (weight 1f)
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val nameToUse = asset.displayName.ifEmpty { asset.name }

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
                    val priceLabel = if (asset.baseSymbol == "CUSTOM") "VALUE" else "PRICE"
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
                            text = formatBoutiquePrice(asset.officialSpotPrice, baseCurrency),
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
                            "TOTAL VALUE",
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
                            text = formatCurrency(asset.officialSpotPrice * asset.weight * asset.amountHeld, 2, baseCurrency),
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
    baseCurrency: String = "USD",
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    showEditButton: Boolean = false
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
    )
}

@Composable
fun HighDensityAssetCard(
    asset: AssetEntity,
    isDragging: Boolean,
    cardBg: Color,
    cardText: Color,
    baseCurrency: String = "USD",
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    showEditButton: Boolean = false
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
    baseCurrency: String = "USD",
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    showEditButton: Boolean = false,
    /** Dense dashboard typography vs airy boutique row. */
    isHighVisibilityMode: Boolean = false,
    variant: CompactCardVariant? = null,
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
        val expandedMinHeight = if (hi) 152.dp else 172.dp
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
                            asset.displayName.ifEmpty { asset.name }
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
                                    MetalIcon(
                                        name = asset.symbol,
                                        weight = asset.weight,
                                        unit = asset.weightUnit,
                                        physicalForm = asset.physicalForm,
                                        size = 30,
                                        imageUrl = asset.imageUrl,
                                        localPath = asset.localIconPath,
                                        category = asset.category
                                    )
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
                                    AutoResizingText(
                                        text = titleText.uppercase(),
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
                                    )
                                    AutoResizingText(
                                        text = formatBoutiquePrice(asset.officialSpotPrice, baseCurrency),
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
                                AutoResizingText(
                                    text = formatCurrency(
                                        (asset.officialSpotPrice * asset.amountHeld) + asset.premium,
                                        2,
                                        baseCurrency
                                    ),
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
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            // Anchor Left: Icon/Symbol Slot (80dp)
                            Column(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(95.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                MetalIcon(
                                    name = asset.symbol,
                                    weight = asset.weight,
                                    unit = asset.weightUnit,
                                    physicalForm = asset.physicalForm,
                                    size = 44, // Master Icon Scale Parity
                                    imageUrl = asset.imageUrl,
                                    localPath = asset.localIconPath,
                                    category = asset.category
                                )
                                Spacer(Modifier.height(expandedIconSymbolGap))
                                Text(
                                    text = asset.symbol.uppercase(),
                                    modifier = Modifier.fillMaxWidth(),
                                    style = LocalTextStyle.current.merge(
                                        TextStyle(
                                            color = cardText,
                                            fontWeight = FontWeight.Black,
                                            fontSize = symSize,
                                            textAlign = TextAlign.Center,
                                            lineHeight = expandedLine,
                                            platformStyle = expandedPlatform,
                                        )
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Middle Slot: Holding & Name (weight 1f)
                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val nameToUse = asset.displayName.ifEmpty { asset.name }
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
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
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
                            val priceLabel = if (asset.baseSymbol == "CUSTOM") "VALUE" else "PRICE"
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
                                    text = formatBoutiquePrice(asset.officialSpotPrice, baseCurrency),
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
                                    "TOTAL VALUE",
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
                                    text = formatCurrency(asset.officialSpotPrice * asset.weight * asset.amountHeld, 2, baseCurrency),
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
    baseCurrency: String = "USD",
    modifier: Modifier = Modifier
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
                            Text(text = "    \"Holding\"", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Yellow)
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
                            text = "${if (changePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", changePercent)}%",
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