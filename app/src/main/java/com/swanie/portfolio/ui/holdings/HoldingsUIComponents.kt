package com.swanie.portfolio.ui.holdings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

            val isBar = name.contains("Bar", true) || name.contains("Ingot", true) || name.contains("KILO", true) || weight >= 10.0

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

                val weightStr = when {
                    weight > 0.03 && weight < 0.033 -> "1g"
                    weight > 32.1 && weight < 32.2 -> "1k"
                    weight == 100.0 -> "100"
                    weight == 10.0 -> "10"
                    weight >= 1.0 -> weight.toInt().toString()
                    else -> weight.toString().replace("0.", ".")
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
fun SparklineChart(historyData: List<Double>, modifier: Modifier = Modifier) {
    if (historyData.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Gathering Data...", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Canvas(Modifier.fillMaxSize()) {
                drawLine(color = Color.White.copy(alpha = 0.1f), start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 1.dp.toPx())
            }
        }
        return
    }
    val lastPrice = historyData.last()
    val firstPrice = historyData.first()
    val trendColor = if (lastPrice >= firstPrice) Color(0xFF00FF00) else Color(0xFFFF0000)
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
        drawPath(path, trendColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
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

@Composable
fun AutoResizingText(text: String, style: TextStyle, modifier: Modifier = Modifier, maxLines: Int = 1) {
    var fs by remember { mutableStateOf(style.fontSize) }
    var rd by remember { mutableStateOf(false) }
    Text(text = text, style = style.copy(fontSize = fs), modifier = modifier.drawWithContent { if (rd) drawContent() }, maxLines = maxLines, softWrap = false, overflow = TextOverflow.Clip, onTextLayout = { if (it.hasVisualOverflow && fs > 8.sp) { fs *= 0.95f } else { rd = true } })
}

fun getCurrencySymbol(code: String): String = when (code.uppercase()) {
    "EUR" -> "€"
    "GBP" -> "£"
    else -> "$"
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
    onDismiss: () -> Unit, onConfirmed: (String, String, Double, Boolean, String, String, String?, Boolean, String) -> Unit
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
    var isKiloSelected by remember { mutableStateOf(initialWeight >= 32.0) }
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
                Text(text = when(step) { 1 -> "SELECT TYPE"; 10 -> "NAME"; 11 -> "ICON"; 12 -> "LABELS"; 13 -> "VALUE"; 4 -> "QUANTITY"; else -> "PREMIUM" }, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(20.dp))
                when (step) {
                    1 -> FunnelGrid(listOf("Gold", "Silver", "Platinum", "Palladium", "Custom"), selectedMetal) {
                        if (it == "Custom") { isTrueManualFlag = true; selectedMetal = ""; step = 10 }
                        else { isTrueManualFlag = false; selectedMetal = it; step = 2 }
                    }
                    10 -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        Text("LABEL UNDER ICON", color = Color.White.copy(0.5f), fontSize = 10.sp)
                        OutlinedTextField(value = selectedMetal, onValueChange = { if(it.length <= 8) selectedMetal = it }, placeholder = { Text("Enter Name...", color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { if(selectedMetal.isNotBlank()) step = 11 } , modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("NEXT") }
                    }
                    11 -> {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(0.05f)).clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center) { if (customIconUri != null) AsyncImage(model = customIconUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) else Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.fillMaxSize().scale(1.5f)) }
                        Text("TAP PHOTO (OR SWAN)", color = Color.White.copy(0.5f), fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp)); Button(onClick = { step = 12 }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("NEXT") }
                    }
                    12 -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        Text("DESCRIPTION LINES", color = Color.White.copy(0.5f), fontSize = 10.sp)
                        OutlinedTextField(value = l1, onValueChange = { l1 = it }, placeholder = { Text("Line 1...", color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Spacer(Modifier.height(8.dp)); OutlinedTextField(value = l2, onValueChange = { l2 = it }, placeholder = { Text("Line 2...", color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { step = 4 }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("NEXT") }
                    }
                    13 -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        Text("UNIT VALUE", color = Color.White.copy(0.5f), fontSize = 10.sp); OutlinedTextField(value = manualPriceInput, onValueChange = { manualPriceInput = it }, placeholder = { Text("0.00", color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { onConfirmed(selectedMetal, "$l1\n$l2".trim(), 1.0, false, qtyInput, "0.0", customIconUri, true, manualPriceInput) }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("FINALIZE") }
                    }
                    2 -> FunnelGrid(listOf("Bars", "Coins", "Rounds", "Custom"), l1) { l1 = it; step = 3 }
                    3 -> FunnelGrid(listOf("1/10 OZ", "1 OZ", "10 OZ", "100 OZ", "1 KILO", "1 GRAM", "Custom"), "") { label ->
                        val common = mapOf(
                            "1/10 OZ" to (0.1 to false),
                            "1 OZ" to (1.0 to false),
                            "10 OZ" to (10.0 to false),
                            "100 OZ" to (100.0 to false),
                            "1 KILO" to (32.1507 to true),
                            "1 GRAM" to (0.0321507 to false)
                        )
                        val data = common[label] ?: (1.0 to false)
                        selectedWeight = data.first
                        isKiloSelected = data.second
                        step = 4
                    }
                    4 -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        OutlinedTextField(value = qtyInput, onValueChange = { qtyInput = it }, placeholder = { Text("Enter Quantity...", color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { if(qtyInput.isNotBlank()) { if(isTrueManualFlag) step = 13 else step = 5 } }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("NEXT") }
                    }
                    else -> {
                        LaunchedEffect(Unit) { focus.requestFocus() }
                        OutlinedTextField(value = premInput, onValueChange = { premInput = it }, placeholder = { Text("0.00", color = Color.White.copy(0.4f)) }, modifier = Modifier.fillMaxWidth().focusRequester(focus), textStyle = TextStyle(color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow))
                        Button(onClick = { onConfirmed(selectedMetal, l1, selectedWeight, isKiloSelected, qtyInput, premInput.ifBlank { "0.0" }, null, false, "0.0") }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("FINALIZE") }
                    }
                }
                if (step > 1) { val prev = when(step) { 10 -> 1; 11 -> 10; 12 -> 11; 13 -> 4; else -> step - 1 }; TextButton(onClick = { step = prev }) { Text("BACK", color = Color.Gray) } }
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
                Text("${asset.symbol.uppercase()} SETTINGS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp); Spacer(Modifier.height(24.dp))
                Text("QUANTITY HELD", color = Color.White.copy(0.6f), fontSize = 10.sp); BasicTextField(value = amt, onValueChange = { amt = it }, textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 28.sp, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().focusRequester(focus))
                Spacer(Modifier.height(20.dp)); Text("PRICE DECIMALS: ${dec.toInt()}", color = Color.White.copy(0.6f), fontSize = 10.sp); Slider(value = dec, onValueChange = { dec = it }, valueRange = 0f..8f, steps = 7, colors = SliderDefaults.colors(thumbColor = Color.Yellow, activeTrackColor = Color.Yellow))
                Button(onClick = { onSave(asset.name, amt.toDoubleOrNull() ?: asset.amountHeld, dec.toInt()) }, modifier = Modifier.fillMaxWidth().padding(top = 30.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("SAVE CHANGES", fontWeight = FontWeight.Black) }
                TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
            }
        }
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
}

@Composable
fun FullAssetCard(asset: AssetEntity, isExpanded: Boolean, isEditing: Boolean, isDragging: Boolean, showEditButton: Boolean, cardBg: Color, cardText: Color, baseCurrency: String = "USD", onExpandToggle: () -> Unit, onEditRequest: () -> Unit, onSave: (String, Double, Double, Int) -> Unit, onCancel: () -> Unit = {}, modifier: Modifier = Modifier) {
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "grabScale")
    val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "grabElevation")
    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = elevation.toPx(); clip = true; shape = RoundedCornerShape(16.dp) }
                .clickable(enabled = !isEditing) { onExpandToggle() },
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(0.9f).height(85.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        MetalIcon(name = asset.symbol, weight = asset.weight, imageUrl = asset.imageUrl, localPath = asset.localIconPath, category = asset.category)
                        if (asset.baseSymbol != "CUSTOM") {
                            Spacer(Modifier.height(6.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = asset.weight.toString(), color = cardText, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                val unit = when {
                                    asset.displayName.contains("KILO", true) || asset.name.contains("KILO", true) -> "KILO"
                                    asset.displayName.contains("GRAM", true) || asset.name.contains("GRAM", true) -> "GRAM"
                                    else -> "OZ"
                                }
                                Text(text = unit, color = cardText.copy(alpha = 0.6f), fontWeight = FontWeight.Black, fontSize = 9.sp)
                            }
                        } else {
                            Text(asset.symbol, color = cardText, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUANTITY", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(formatAmount(asset.amountHeld), color = cardText, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Box(modifier = Modifier.height(55.dp), contentAlignment = Alignment.Center) {
                            val nameToUse = asset.displayName.ifEmpty { asset.name }
                            val forced = if (nameToUse.contains(" ") && !nameToUse.contains("\n")) nameToUse.replaceFirst(" ", "\n") else nameToUse
                            Text(text = forced, color = cardText, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, lineHeight = 13.sp, maxLines = 3)
                        }
                    }
                    Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                        if (showEditButton && !isEditing) {
                            IconButton(onClick = { onEditRequest() }, modifier = Modifier.size(40.dp).background(Color.Yellow, CircleShape)) { Icon(Icons.Default.Edit, null, tint = Color.Black) }
                        } else {
                            SparklineChart(asset.sparklineData, Modifier.width(75.dp).height(32.dp).padding(top = 12.dp))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(horizontal = 4.dp)) { Icon(if (asset.priceChange24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = trendColor, modifier = Modifier.size(20.dp)); Text(text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 9.sp, fontWeight = FontWeight.Black) }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp)); HorizontalDivider(color = cardText.copy(alpha = 0.05f)); Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    val priceLabel = if (asset.baseSymbol == "CUSTOM") "VALUE" else "PRICE"
                    Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(priceLabel, color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        AutoResizingText(text = formatCurrency(asset.officialSpotPrice, asset.decimalPreference, baseCurrency), style = TextStyle(color = cardText, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                    }
                    Column(modifier = Modifier.weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL VALUE", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        AutoResizingText(text = formatCurrency(asset.officialSpotPrice * asset.weight * asset.amountHeld, 2, baseCurrency), style = TextStyle(color = cardText, fontWeight = FontWeight.Black, fontSize = 17.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
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

@Composable
fun CompactAssetCard(asset: AssetEntity, isDragging: Boolean, cardBg: Color, cardText: Color, baseCurrency: String = "USD", onExpandToggle: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(if (isDragging) 1.04f else 1f, label = "compactGrabScale")
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    Box(modifier = modifier.fillMaxWidth()) {
        Card(modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale; clip = true; shape = RoundedCornerShape(12.dp) }.clickable { onExpandToggle() }, colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(0.3f)) { MetalIcon(name = asset.symbol, weight = asset.weight, size = 32, imageUrl = asset.imageUrl, localPath = asset.localIconPath, category = asset.category) }
                Column(modifier = Modifier.weight(1f)) {
                    val titleText = if (asset.category == AssetCategory.METAL) {
                        asset.displayName.ifEmpty { asset.name }
                    } else {
                        asset.symbol
                    }
                    AutoResizingText(titleText, TextStyle(color = cardText, fontWeight = FontWeight.Black, fontSize = 11.sp))
                    AutoResizingText(formatCurrency(asset.officialSpotPrice * asset.weight * asset.amountHeld, 2, baseCurrency), TextStyle(color = cardText.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold))
                }
                SparklineChart(asset.sparklineData, Modifier.weight(0.7f).height(24.dp).padding(top = 12.dp))
            }
        }
        WatermarkBadge(
            source = asset.priceSource,
            color = cardText,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 20.dp, top = 0.dp).offset(y = (-4).dp).zIndex(1f)
        )
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
                    Text(text = name.uppercase(), fontWeight = FontWeight.Black, color = cardText, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = symbol, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = cardText.copy(alpha = 0.4f))
                        if (isOwned) {
                            Text(text = "    \"Holding\"", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Yellow)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (officialSpotPrice > 0.0) {
                        Text(text = formatCurrency(officialSpotPrice, 2, baseCurrency), fontWeight = FontWeight.Black, color = cardText, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "${if (changePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", changePercent)}%", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = trendColor)
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
                    Text(lowStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cardText)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val highStr = if (dayHigh <= 0.0) "${getCurrencySymbol(baseCurrency)} --.--" else formatCurrency(dayHigh, 2, baseCurrency)
                    Text(highStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cardText)
                }
            }
        }
    }
}