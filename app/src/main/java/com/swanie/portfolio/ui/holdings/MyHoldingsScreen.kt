package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHoldingsScreen(
    mainViewModel: MainViewModel,
    onAddNewAsset: () -> Unit,
    onAddCustomAsset: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: AssetViewModel = hiltViewModel()

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle(initialValue = false)
    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle(initialValue = false)
    val isUserDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
    val isLightTextEnabled by mainViewModel.isLightTextEnabled.collectAsStateWithLifecycle(initialValue = true)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")

    val filteredHoldings = remember(selectedTab, holdings) {
        when (selectedTab) {
            1 -> holdings.filter { it.category == AssetCategory.CRYPTO }
            2 -> holdings.filter { it.category == AssetCategory.METAL }
            else -> holdings
        }
    }

    val totalPortfolioValue = filteredHoldings.sumOf { asset ->
        if (asset.isCustom) {
            asset.currentPrice * (asset.weight * asset.amountHeld)
        } else {
            asset.currentPrice * asset.amountHeld
        }
    }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    val headerContentColor = Color.White

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedAssetForSheet by remember { mutableStateOf<AssetEntity?>(null) }

    LaunchedEffect(Unit) { viewModel.refreshAssets() }

    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { sheetState.hide() }.invokeOnCompletion { selectedAssetForSheet = null } },
            sheetState = sheetState
        ) {
            selectedAssetForSheet?.let { asset ->
                Box(Modifier.padding(16.dp)) { 
                    FullAssetCard(
                        asset = asset, 
                        isUserDarkMode = isUserDarkMode, 
                        isLightText = isLightTextEnabled
                    ) 
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    onClick = onAddNewAsset,
                    icon = { Icon(Icons.Default.Add, "Add new asset from the web") },
                    text = { Text("Add from Web") },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ExtendedFloatingActionButton(
                    onClick = onAddCustomAsset,
                    icon = { Icon(Icons.Default.Add, "Add a custom metal holding") },
                    text = { Text("Add Custom Metal") }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshAssets() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
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
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { /* Spacer */ }
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
                            CompactAssetCard(
                                asset = asset,
                                isUserDarkMode = isUserDarkMode,
                                isLightText = isLightTextEnabled,
                                onClick = { selectedAssetForSheet = asset; scope.launch { sheetState.show() } }
                            )
                        } else {
                            FullAssetCard(
                                asset = asset,
                                isUserDarkMode = isUserDarkMode,
                                isLightText = isLightTextEnabled
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullAssetCard(asset: AssetEntity, isUserDarkMode: Boolean, isLightText: Boolean) {
    val numberFormat = NumberFormat.getNumberInstance()
    val cardBgColor = if (isUserDarkMode) Color(0xFF636366) else Color(0xFFB0B0B3)
    val cardContentTextColor = if (isLightText) Color.White else Color(0xFF1C1C1E)
    val borderColor = if (isUserDarkMode)
        Color.White.copy(alpha = 0.12f)
    else
        Color.Black.copy(alpha = 0.15f)
    val subLabelColor = cardContentTextColor.copy(alpha = 0.6f)

    val isPositive = asset.priceChange24h >= 0
    val trendColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)

    val priceFormatter = if (asset.currentPrice < 0.10) DecimalFormat("$#,##0.0000") else DecimalFormat("$#,##0.00")
    val totalValue = if (asset.isCustom) {
        asset.currentPrice * (asset.weight * asset.amountHeld)
    } else {
        asset.currentPrice * asset.amountHeld
    }
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
                    when (asset.category) {
                        AssetCategory.METAL -> {
                            val color = when (asset.symbol) {
                                "XAU" -> Color(0xFFFFD700) // Gold
                                "XAG" -> Color(0xFFC0C0C0) // Silver
                                "XPT" -> Color(0xFFE5E4E2) // Platinum
                                "XPD" -> Color(0xFFE5E4E2) // Palladium
                                else -> Color.Gray
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                            )
                        }
                        AssetCategory.CRYPTO -> {
                            AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
                        }
                    }
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
fun CompactAssetCard(
    asset: AssetEntity, 
    isUserDarkMode: Boolean,
    isLightText: Boolean,
    onClick: () -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance()
    val cardBgColor = if (isUserDarkMode) Color(0xFF636366) else Color(0xFFB0B0B3)
    val cardContentTextColor = if (isLightText) Color.White else Color(0xFF1C1C1E)
    val borderColor = if (isUserDarkMode)
        Color.White.copy(alpha = 0.12f)
    else
        Color.Black.copy(alpha = 0.15f)

    val isPositive = asset.priceChange24h >= 0
    val trendColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)

    val priceFormatter = if (asset.currentPrice < 0.10) DecimalFormat("$#,##0.0000") else DecimalFormat("$#,##0.00")
    val assetPriceString = priceFormatter.format(asset.currentPrice)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1.2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (asset.category) {
                    AssetCategory.METAL -> {
                        val color = when (asset.symbol) {
                            "XAU" -> Color(0xFFFFD700) // Gold
                            "XAG" -> Color(0xFFC0C0C0) // Silver
                            "XPT" -> Color(0xFFE5E4E2) // Platinum
                            "XPD" -> Color(0xFFE5E4E2) // Palladium
                            else -> Color.Gray
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(color)
                        )
                    }
                    AssetCategory.CRYPTO -> {
                        AsyncImage(
                            model = asset.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = asset.symbol.uppercase(),
                    color = cardContentTextColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .padding(horizontal = 8.dp)
            ) {
                SparklineChart(
                    sparklineData = asset.sparklineData,
                    changeColor = trendColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.weight(1.3f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = assetPriceString,
                    color = cardContentTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (assetPriceString.length > 10) 13.sp else 15.sp,
                    maxLines = 1
                )
                Text(
                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%",
                    color = trendColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
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