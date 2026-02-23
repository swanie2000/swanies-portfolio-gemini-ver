package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHoldingsScreen(
    mainViewModel: MainViewModel,
    onAddNewAsset: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: MyHoldingsViewModel = viewModel(
        factory = MyHoldingsViewModelFactory(db.assetDao())
    )
    val holdings by viewModel.holdings.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")

    val filteredHoldings = when (tabs[selectedTab]) {
        "CRYPTO" -> holdings.filter { it.category == "crypto" }
        "METAL" -> holdings.filter { it.category == "metal" }
        else -> holdings
    }

    val totalPortfolioValue = filteredHoldings.sumOf { it.amountHeld * it.currentPrice }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    // Header/Tabs should always be white against the screen background
    val headerContentColor = Color.White

    var countdown by remember { mutableStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedAssetForSheet by remember { mutableStateOf<AssetEntity?>(null) }

    LaunchedEffect(Unit) { viewModel.refreshAssets() }

    LaunchedEffect(countdown, isTimerRunning) {
        if (isTimerRunning && countdown > 0) {
            delay(1000)
            countdown--
        } else if (countdown == 0) {
            isTimerRunning = false
        }
    }

    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { sheetState.hide() }.invokeOnCompletion { selectedAssetForSheet = null } },
            sheetState = sheetState
        ) {
            selectedAssetForSheet?.let { asset ->
                Box(Modifier.padding(16.dp)) { FullAssetCard(asset = asset) }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(brush = LocalBackgroundBrush.current)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.swanie_foreground),
                        contentDescription = "Swan Logo",
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        text = currencyFormat.format(totalPortfolioValue),
                        color = headerContentColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    IconButton(onClick = onAddNewAsset) {
                        Icon(Icons.Default.Add, "Add", tint = headerContentColor, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // Refresh Bar
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).border(1.dp, headerContentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(2.dp)) {
                        LinearProgressIndicator(
                            progress = { (30 - countdown) / 30f },
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(2.dp)),
                            color = when {
                                countdown <= 10 -> Color(0xFF00C853)
                                countdown <= 20 -> Color(0xFFFFD600)
                                else -> Color(0xFFD32F2F)
                            },
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(modifier = Modifier.size(24.dp), onClick = { if (countdown == 0 && !isRefreshing) { viewModel.refreshAssets(); countdown = 30; isTimerRunning = true } }, enabled = countdown == 0 && !isRefreshing) {
                        Icon(Icons.Default.Refresh, null, tint = headerContentColor)
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), height = 3.dp, color = headerContentColor) },
                divider = { }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (selectedTab == index) headerContentColor else headerContentColor.copy(alpha = 0.5f)
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredHoldings) { asset ->
                    if (isCompactViewEnabled) {
                        CompactAssetCard(asset = asset, onClick = { selectedAssetForSheet = asset; scope.launch { sheetState.show() } })
                    } else {
                        FullAssetCard(asset)
                    }
                }
            }
        }
    }
}

@Composable
fun FullAssetCard(asset: AssetEntity) {
    val numberFormat = NumberFormat.getNumberInstance()
    val isDark = isSystemInDarkTheme()

    // Card logic: Dark card for Dark mode, Light card for Light mode
    val cardContentTextColor = if (isDark) Color.White else Color(0xFF1C1C1E)
    val cardBgColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    val subLabelColor = cardContentTextColor.copy(alpha = 0.4f)

    val isPositive = asset.priceChange24h >= 0
    val trendColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)

    val priceFormatter = if (asset.currentPrice < 0.10) DecimalFormat("$#,##0.0000") else DecimalFormat("$#,##0.00")
    val totalValue = asset.amountHeld * asset.currentPrice
    val totalValueString = if (totalValue >= 1_000_000) DecimalFormat("$#,##0").format(totalValue) else DecimalFormat("$#,##0.00").format(totalValue)
    val assetPriceString = priceFormatter.format(asset.currentPrice)
    val holdingAmountString = numberFormat.format(asset.amountHeld)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.height(6.dp))
                    Text(text = asset.symbol.uppercase(), color = cardContentTextColor, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1)
                }

                Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "YOUR HOLDING", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = holdingAmountString,
                        color = cardContentTextColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (holdingAmountString.length > 12) 11.sp else if (holdingAmountString.length > 8) 14.sp else 17.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(text = asset.name.uppercase(), color = cardContentTextColor.copy(alpha = 0.6f), fontSize = if (asset.name.length > 12) 8.sp else 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1)
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    SparklineChart(sparklineData = asset.sparklineData, changeColor = trendColor, modifier = Modifier.width(75.dp).height(32.dp))
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(text = "${if(isPositive) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = cardContentTextColor.copy(alpha = 0.05f))
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "ASSET PRICE", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text(text = "TOTAL VALUE", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = assetPriceString,
                        color = cardContentTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (assetPriceString.length > 12) 11.sp else if (assetPriceString.length > 9) 13.sp else 17.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = totalValueString,
                        color = cardContentTextColor,
                        fontWeight = FontWeight.Black,
                        fontSize = if (totalValueString.length > 12) 12.sp else if (totalValueString.length > 9) 14.sp else 19.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun CompactAssetCard(asset: AssetEntity, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1C1C1E)
    val cardBgColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val isPositive = asset.priceChange24h >= 0
    val trendColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = asset.symbol.uppercase(), color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(DecimalFormat("$#,##0.00").format(asset.currentPrice), color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text("${if(isPositive) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SparklineChart(sparklineData: List<Double>, changeColor: Color, modifier: Modifier = Modifier) {
    if (sparklineData.size < 2) { Box(modifier); return }
    val minPrice = sparklineData.minOrNull() ?: 0.0
    val maxPrice = sparklineData.maxOrNull() ?: 0.0
    val priceRange = if ((maxPrice - minPrice) > 0) maxPrice - minPrice else 1.0
    Canvas(modifier) {
        val points = sparklineData.mapIndexed { index, price ->
            val x = index.toFloat() / (sparklineData.size - 1) * size.width
            val y = size.height - ((price - minPrice) / priceRange * size.height).toFloat()
            Offset(x, y)
        }
        val linePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]; val curr = points[i]
                val cp1x = prev.x + (curr.x - prev.x) / 2f
                cubicTo(cp1x, prev.y, cp1x, curr.y, curr.x, curr.y)
            }
        }
        drawPath(path = linePath, color = changeColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path = linePath.apply { lineTo(size.width, size.height); lineTo(0f, size.height); close() }, brush = Brush.verticalGradient(listOf(changeColor.copy(alpha = 0.2f), Color.Transparent)))
    }
}