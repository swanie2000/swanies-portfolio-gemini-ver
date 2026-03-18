package com.swanie.portfolio.ui.components

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swanie.portfolio.R
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
fun FullAssetCard(
    asset: AssetEntity,
    isExpanded: Boolean,
    isEditing: Boolean,
    isDragging: Boolean,
    showEditButton: Boolean,
    cardBg: Color,
    cardText: Color,
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit,
    onSave: (String, Double, Double, Int) -> Unit,
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Log.d("UI_TRACE", "CARD_RENDER: Drawing full card for ${asset.symbol} with source: ${asset.priceSource}")
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "grabScale")
    val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "grabElevation")
    
    Card(
        modifier = modifier
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
                    // THE DATA CHAIN OF CUSTODY: Pulling iconUrl directly from database
                    MetalIcon(asset.name, imageUrl = asset.iconUrl ?: asset.imageUrl)
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
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                val priceLabel = if (asset.baseSymbol == "CUSTOM") "VALUE" else "PRICE"
                val mult = when { asset.name.contains("KILO", true) -> 32.1507; asset.name.contains("GRAM", true) -> 0.03215; else -> 1.0 }
                Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.CenterHorizontally) { 
                    Text(priceLabel, color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); 
                    AutoResizingText(text = formatCurrency(asset.currentPrice, asset.decimalPreference), style = TextStyle(color = cardText, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                    // THE INVISIBILITY FIX: Force Display Source Label
                    Text(
                        text = asset.priceSource.uppercase(), 
                        color = MaterialTheme.colorScheme.primary, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Black, 
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Column(modifier = Modifier.weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally) { Text("TOTAL VALUE", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black); AutoResizingText(text = formatCurrency(asset.currentPrice * mult * asset.weight * asset.amountHeld, 2), style = TextStyle(color = cardText, fontWeight = FontWeight.Black, fontSize = 17.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable
fun CompactAssetCard(
    asset: AssetEntity,
    isDragging: Boolean,
    cardBg: Color,
    cardText: Color,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("UI_TRACE", "CARD_RENDER: Drawing compact card for ${asset.symbol} with source: ${asset.priceSource}")
    val scale by animateFloatAsState(if (isDragging) 1.04f else 1f, label = "compactGrabScale")
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale; clip = true; shape = RoundedCornerShape(12.dp) }
            .clickable { onExpandToggle() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            // THE DATA CHAIN OF CUSTODY: Pulling iconUrl directly from database
            Box(modifier = Modifier.weight(0.3f)) { MetalIcon(asset.name, size = 32, imageUrl = asset.iconUrl ?: asset.imageUrl) }
            Column(modifier = Modifier.weight(1f)) { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AutoResizingText(asset.symbol.uppercase(), TextStyle(color = cardText, fontWeight = FontWeight.Black, fontSize = 14.sp))
                    // THE INVISIBILITY FIX: Force Display Source Label
                    Text(
                        text = " • ${asset.priceSource.uppercase()}", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                val mult = when { asset.name.contains("KILO", true) -> 32.1507; asset.name.contains("GRAM", true) -> 0.03215; else -> 1.0 }; 
                AutoResizingText(formatCurrency(asset.currentPrice * mult * asset.weight * asset.amountHeld, 2), TextStyle(color = cardText.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)) 
            }
            SparklineChart(asset.sparklineData, trendColor, Modifier.weight(0.7f).height(24.dp))
        }
    }
}

@Composable
fun MetalMarketCard(
    name: String,
    symbol: String,
    currentPrice: Double,
    changePercent: Double,
    dayHigh: Double,
    dayLow: Double,
    sparkline: List<Double>,
    isOwned: Boolean,
    cardBg: Color,
    cardText: Color,
    modifier: Modifier = Modifier
) {
    val trendColor = if (changePercent >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(195.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Row 1: Identity & Price
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
                            color = cardText.copy(alpha = 0.4f)
                        )
                        if (isOwned) {
                            Text(
                                text = "    \"Holding\"",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Yellow
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (currentPrice > 0.0) {
                        Text(
                            text = formatCurrency(currentPrice),
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
                            color = trendColor
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = Color.Yellow
                        )
                    }
                }
            }

            // Row 2: Full-width Sparkline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                contentAlignment = Alignment.Center
            ) {
                if (sparkline.isNotEmpty()) {
                    SparklineChart(sparkline, trendColor, Modifier.fillMaxSize())
                } else if (currentPrice > 0.0) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = cardText.copy(0.2f)
                    )
                }
            }

            // Row 3: Day High / Low
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DAY", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Red, lineHeight = 7.sp)
                        Text("LOW", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Red, lineHeight = 8.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    val lowStr = if (dayLow <= 0.0) "$ --.--" else formatCurrency(dayLow)
                    Text(lowStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cardText)
                }

                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DAY", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Green, lineHeight = 7.sp)
                        Text("HIGH", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Green, lineHeight = 8.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    val highStr = if (dayHigh <= 0.0) "$ --.--" else formatCurrency(dayHigh)
                    Text(highStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cardText)
                }
            }
        }
    }
}
