package com.swanie.portfolio.ui.metals

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.repository.MarketPriceData
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.SparklineChart
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.NumberFormat
import java.util.*

/**
 * Metals Market Watch - Final Production Edition.
 * Live sparklines, optimized spacing, and "Holding" badge logic.
 */
@Composable
fun MetalsAuditScreen(navController: NavController) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())

    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()
    val cardBgHex by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextHex by themeViewModel.cardTextColor.collectAsState()

    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))
    val cardBg = Color(android.graphics.Color.parseColor(cardBgHex.ifBlank { "#121212" }))
    val cardText = Color(android.graphics.Color.parseColor(cardTextHex.ifBlank { "#FFFFFF" }))

    var metalsOrder by remember { mutableStateOf(listOf("Gold" to "XAU", "Silver" to "XAG", "Platinum" to "XPT", "Palladium" to "XPD")) }

    LaunchedEffect(Unit) {
        val savedOrder = viewModel.getMetalDisplayOrder()
        if (savedOrder != null) {
            val defaultList = listOf("Gold" to "XAU", "Silver" to "XAG", "Platinum" to "XPT", "Palladium" to "XPD")
            metalsOrder = savedOrder.mapNotNull { sym -> defaultList.find { it.second == sym } }
        }
    }

    val marketDataMap = remember { mutableStateMapOf<String, MarketPriceData>() }

    LaunchedEffect(metalsOrder) {
        metalsOrder.forEach { (_, sym) ->
            scope.launch {
                val data = viewModel.fetchMarketPriceData(sym)
                if (data.current > 0.0) {
                    marketDataMap[sym] = data
                }
            }
        }
    }

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        metalsOrder = metalsOrder.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 4.dp)) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                }
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(70.dp), contentScale = ContentScale.Fit)
                    Text(text = "METALS MARKET WATCH", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.offset(y = (-6).dp))
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(metalsOrder, key = { it.second }) { item ->
                    val (name, sym) = item
                    ReorderableItem(reorderableLazyListState, key = sym) { isDragging ->
                        val isOwned = holdings.any { it.baseSymbol == sym && it.category == AssetCategory.METAL }
                        val marketData = marketDataMap[sym]

                        val currentPrice = marketData?.current ?: 0.0
                        val changePct = marketData?.changePercent ?: 0.0
                        val liveSparkline = marketData?.sparkline ?: emptyList()
                        val high = marketData?.high ?: 0.0
                        val low = marketData?.low ?: 0.0

                        val scale by animateFloatAsState(if (isDragging) 1.1f else 1f, label = "scale")
                        val elevation by animateDpAsState(if (isDragging) 15.dp else 2.dp, label = "elevation")

                        val dragModifier = Modifier.longPressDraggableHandle(
                            onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                            onDragStopped = { viewModel.saveMetalDisplayOrder(metalsOrder.map { it.second }) }
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(195.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = elevation.toPx() }
                                .zIndex(if (isDragging) 1f else 0f)
                                .then(dragModifier),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp).fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // --- HEADER ---
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name.uppercase(), fontWeight = FontWeight.Black, color = cardText, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(sym, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = cardText.copy(alpha = 0.4f))
                                            if (isOwned) {
                                                Text("    \"Holding\"", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Yellow)
                                            }
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (currentPrice > 0.0) {
                                            Text(NumberFormat.getCurrencyInstance(Locale.US).format(currentPrice), fontWeight = FontWeight.Black, color = cardText, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${if(changePct >= 0) "+" else ""}${String.format("%.2f", changePct)}%", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (changePct >= 0) Color(0xFF00C853) else Color(0xFFD32F2F))
                                        } else {
                                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.5.dp, color = Color.Yellow)
                                        }
                                    }
                                }

                                // --- SPARKLINE AREA (Clean Manual Draw) ---
                                Box(modifier = Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
                                    if (liveSparkline.isNotEmpty()) {
                                        val lineColor = if (changePct >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val width = size.width
                                            val height = size.height
                                            val maxVal = liveSparkline.maxOrNull() ?: 1.0
                                            val minVal = liveSparkline.minOrNull() ?: 0.0
                                            val range = (maxVal - minVal).coerceAtLeast(1.0)

                                            val path = Path().apply {
                                                liveSparkline.forEachIndexed { index, value ->
                                                    val x = index * (width / (liveSparkline.size - 1))
                                                    val y = height - ((value - minVal) / range * height).toFloat()
                                                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                                                }
                                            }
                                            drawPath(path = path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
                                        }
                                    } else if (currentPrice > 0.0) {
                                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp, color = Color.White.copy(0.2f))
                                    }
                                }

                                // --- FOOTER ---
                                Row(modifier = Modifier.fillMaxWidth().height(32.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("DAY", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Red, lineHeight = 7.sp)
                                            Text("LOW", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Red, lineHeight = 8.sp)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        val lowStr = if (currentPrice <= 0.0 || low <= 0.0) "$ --.--" else NumberFormat.getCurrencyInstance(Locale.US).format(low)
                                        Text(lowStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cardText)
                                    }

                                    Spacer(Modifier.weight(1f))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("DAY", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Green, lineHeight = 7.sp)
                                            Text("HIGH", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Green, lineHeight = 8.sp)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        val highStr = if (currentPrice <= 0.0 || high <= 0.0) "$ --.--" else NumberFormat.getCurrencyInstance(Locale.US).format(high)
                                        Text(highStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cardText)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth().height(64.dp), color = bgColor) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val icons = listOf(Icons.Default.Home to Routes.HOME, Icons.Default.FormatListBulleted to Routes.HOLDINGS, Icons.Default.PieChart to Routes.ANALYTICS, Icons.Default.Settings to Routes.SETTINGS)
                    icons.forEach { (icon, route) ->
                        IconButton(onClick = { navController.navigate(route) }, modifier = Modifier.size(48.dp)) {
                            Icon(icon, null, modifier = Modifier.size(28.dp), tint = if(currentRoute == route) textColor else textColor.copy(0.3f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars).background(bgColor))
        }
    }
}