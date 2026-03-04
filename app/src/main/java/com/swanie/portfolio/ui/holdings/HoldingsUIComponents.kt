package com.swanie.portfolio.ui.holdings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.Offset
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
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import java.text.DecimalFormat
import java.util.Locale

@Composable
fun MetalIcon(name: String, size: Int = 44, imageUrl: String = "") {
    if (imageUrl == "SWAN_DEFAULT") {
        Box(modifier = Modifier.size((size * 1.2).dp).clip(CircleShape).background(Color.White.copy(0.1f)), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.fillMaxSize().scale(1.5f))
        }
    } else if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
        AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size.dp).clip(CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape))
    } else if (imageUrl.isNotEmpty()) {
        AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.size(size.dp).clip(RoundedCornerShape(8.dp)))
    } else {
        val metalColors = if (name.contains("Gold", true)) listOf(Color(0xFFFFD700), Color(0xFFB8860B))
        else if (name.contains("Silver", true)) listOf(Color(0xFFE0E0E0), Color(0xFF757575))
        else listOf(Color(0xFFCED4DA), Color(0xFF495057))
        val isBar = name.contains("Bar", true) || name.contains("Ingot", true) || name.contains("KILO", true) || name.contains("GRAM", true)
        Box(modifier = Modifier.size(size.dp).clip(if (isBar) RoundedCornerShape(4.dp) else CircleShape).background(Brush.radialGradient(metalColors, center = Offset.Zero)).border(1.dp, Color.White.copy(0.2f), if (isBar) RoundedCornerShape(4.dp) else CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = if (isBar) Icons.Default.ViewAgenda else Icons.Default.Toll, contentDescription = null, tint = Color.Black.copy(0.2f), modifier = Modifier.size((size * 0.6).dp))
        }
    }
}

@Composable
fun SparklineChart(sparklineData: List<Double>, changeColor: Color, modifier: Modifier = Modifier) {
    if (sparklineData.size < 2) {
        Canvas(modifier) { drawLine(color = Color.White.copy(alpha = 0.2f), start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 1.dp.toPx()) }
        return
    }
    val min = sparklineData.minOrNull() ?: 0.0; val max = sparklineData.maxOrNull() ?: 0.0; val range = if ((max - min) > 0) max - min else 1.0
    Canvas(modifier) {
        val points = sparklineData.mapIndexed { i, p -> Offset(i.toFloat() / (sparklineData.size - 1) * size.width, size.height - ((p - min) / range * size.height).toFloat()) }
        val path = Path().apply { moveTo(points[0].x, points[0].y); for (i in 1 until points.size) lineTo(points[i].x, points[i].y) }
        drawPath(path, changeColor, style = Stroke(2.dp.toPx()))
    }
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

fun formatCurrency(v: Double, d: Int = 2): String {
    val df = DecimalFormat("$#,##0.00")
    if (d != 2) { df.minimumFractionDigits = 0; df.maximumFractionDigits = d }
    return df.format(v)
}

fun formatAmount(v: Double): String = DecimalFormat("#,###.########").format(v)

@Composable
fun MetalSelectionFunnel(
    initialMetal: String, initialForm: String, initialWeight: Double, initialQty: String, initialPrem: String, initialManualPrice: String,
    onDismiss: () -> Unit, onConfirmed: (String, String, Double, Boolean, String, String, String?, Boolean, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedMetal by remember { mutableStateOf("") }
    var l1 by remember { mutableStateOf("") }
    var l2 by remember { mutableStateOf("") }
    var selectedWeight by remember { mutableDoubleStateOf(initialWeight) }
    var isKiloSelected by remember { mutableStateOf(false) }
    var qtyInput by remember { mutableStateOf("") }
    var premInput by remember { mutableStateOf("") }
    var manualPriceInput by remember { mutableStateOf("") }
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
                        Button(onClick = { if(selectedMetal.isNotBlank()) step = 11 }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("NEXT") }
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
                        val common = mapOf("1/10 OZ" to (0.1 to false), "1 OZ" to (1.0 to false), "10 OZ" to (10.0 to false), "100 OZ" to (100.0 to false), "1 KILO" to (1.0 to true), "1 GRAM" to (1.0 to false))
                        val data = common[label] ?: (1.0 to false); selectedWeight = data.first; isKiloSelected = data.second; step = 4
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
fun FullAssetCard(asset: AssetEntity, isExpanded: Boolean, isEditing: Boolean, isDragging: Boolean, showEditButton: Boolean, cardBg: Color, cardText: Color, onExpandToggle: () -> Unit, onEditRequest: () -> Unit, onSave: (String, Double, Double, Int) -> Unit, onCancel: () -> Unit = {}, modifier: Modifier = Modifier) {
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "grabScale")
    val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "grabElevation")
    Card(modifier = modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = elevation.toPx(); clip = true; shape = RoundedCornerShape(16.dp) }.clickable(enabled = !isEditing) { onExpandToggle() }, colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(0.9f).height(85.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    MetalIcon(asset.name, imageUrl = asset.imageUrl)
                    if (asset.baseSymbol != "CUSTOM") {
                        Spacer(Modifier.height(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(text = asset.weight.toString(), color = cardText, fontWeight = FontWeight.Black, fontSize = 11.sp); val unit = when { asset.name.contains("KILO", true) -> "KILO"; asset.name.contains("GRAM", true) -> "GRAM"; else -> "OZ" }; Text(text = unit, color = cardText.copy(alpha = 0.6f), fontWeight = FontWeight.Black, fontSize = 9.sp) }
                    } else {
                        Text(asset.symbol, color = cardText, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("QUANTITY", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold);
                    Text(formatAmount(asset.amountHeld), color = cardText, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp);
                    Box(modifier = Modifier.height(55.dp), contentAlignment = Alignment.Center) {
                        val forced = if (asset.name.contains(" ") && !asset.name.contains("\n")) asset.name.replaceFirst(" ", "\n") else asset.name;
                        Text(text = forced.uppercase(), color = cardText, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, lineHeight = 13.sp, maxLines = 3)
                    }
                }
                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) { if (showEditButton && !isEditing) IconButton(onClick = { onEditRequest() }, modifier = Modifier.size(40.dp).background(Color.Yellow, CircleShape)) { Icon(Icons.Default.Edit, null, tint = Color.Black) } else { SparklineChart(asset.sparklineData, trendColor, Modifier.width(75.dp).height(32.dp)); Spacer(Modifier.height(4.dp)); Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(horizontal = 4.dp)) { Icon(if (asset.priceChange24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = trendColor, modifier = Modifier.size(20.dp)); Text(text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 9.sp, fontWeight = FontWeight.Black) } } }
            }
            Spacer(Modifier.height(8.dp)); HorizontalDivider(color = cardText.copy(alpha = 0.05f)); Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                val priceLabel = if (asset.baseSymbol == "CUSTOM") "VALUE" else "PRICE"
                val mult = when { asset.name.contains("KILO", true) -> 32.1507; asset.name.contains("GRAM", true) -> 0.03215; else -> 1.0 }
                Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.CenterHorizontally) { Text(priceLabel, color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); AutoResizingText(text = formatCurrency(asset.currentPrice, asset.decimalPreference), style = TextStyle(color = cardText, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth()) }
                Column(modifier = Modifier.weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally) { Text("TOTAL VALUE", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black); AutoResizingText(text = formatCurrency(asset.currentPrice * mult * asset.weight * asset.amountHeld, 2), style = TextStyle(color = cardText, fontWeight = FontWeight.Black, fontSize = 17.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable
fun CompactAssetCard(asset: AssetEntity, isDragging: Boolean, cardBg: Color, cardText: Color, onExpandToggle: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(if (isDragging) 1.04f else 1f, label = "compactGrabScale")
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    Card(modifier = modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale; clip = true; shape = RoundedCornerShape(12.dp) }.clickable { onExpandToggle() }, colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(0.3f)) { MetalIcon(asset.name, size = 32, imageUrl = asset.imageUrl) }
            Column(modifier = Modifier.weight(1f)) { AutoResizingText(asset.symbol.uppercase(), TextStyle(color = cardText, fontWeight = FontWeight.Black, fontSize = 14.sp)); val mult = when { asset.name.contains("KILO", true) -> 32.1507; asset.name.contains("GRAM", true) -> 0.03215; else -> 1.0 }; AutoResizingText(formatCurrency(asset.currentPrice * mult * asset.weight * asset.amountHeld, 2), TextStyle(color = cardText.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)) }
            SparklineChart(asset.sparklineData, trendColor, Modifier.weight(0.7f).height(24.dp))
        }
    }
}
