@file:OptIn(ExperimentalFoundationApi::class)

package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHoldingsScreen(
    mainViewModel: MainViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()

    val sortOrder by viewModel.sortOrder.collectAsState()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle(initialValue = false)
    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle(initialValue = false)

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val useGradient by themeViewModel.useGradient.collectAsState()

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

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedAssetForSheet by remember { mutableStateOf<AssetEntity?>(null) }

    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshAssets() }

    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { sheetState.hide() }.invokeOnCompletion { selectedAssetForSheet = null } },
            sheetState = sheetState
        ) {
            selectedAssetForSheet?.let { asset ->
                Box(Modifier.padding(16.dp)) {
                    FullAssetCard(asset = asset, modifier = Modifier)
                }
            }
        }
    }

    val backgroundBrush = if (useGradient) {
        Brush.verticalGradient(
            colors = listOf(Color(siteBgColor.toColorInt()), Color(siteBgColor.toColorInt()).copy(alpha = 0.7f))
        )
    } else {
        SolidColor(Color(siteBgColor.toColorInt()))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(paddingValues)
        ) {
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = Color.Cyan
                )
            }
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshAssets() },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(color = Color(cardBgColor.toColorInt()), shape = RoundedCornerShape(50))
                                    .clickable { showSortMenu = true }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "SORT",
                                    color = Color(siteTextColor.toColorInt()),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                DropdownMenuItem(text = { Text("Value") }, onClick = { viewModel.setSortOrder(SortOrder.VALUE); showSortMenu = false })
                                DropdownMenuItem(text = { Text("Name") }, onClick = { viewModel.setSortOrder(SortOrder.NAME); showSortMenu = false })
                                DropdownMenuItem(text = { Text("Category") }, onClick = { viewModel.setSortOrder(SortOrder.CATEGORY); showSortMenu = false })
                                DropdownMenuItem(text = { Text("Manual") }, onClick = { viewModel.setSortOrder(SortOrder.MANUAL); showSortMenu = false })
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.swanie_foreground),
                                contentDescription = "Swan Logo",
                                modifier = Modifier.size(120.dp)
                            )
                            Text(
                                text = currencyFormat.format(totalPortfolioValue),
                                color = Color(siteTextColor.toColorInt()),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(color = Color(cardBgColor.toColorInt()), shape = CircleShape)
                                    .clickable { navController.navigate(Routes.ASSET_PICKER) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = Color(siteTextColor.toColorInt())
                                )
                            }
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), height = 3.dp, color = Color(siteTextColor.toColorInt())) },
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
                                        color = if (selectedTab == index) Color(siteTextColor.toColorInt()) else Color(siteTextColor.toColorInt()).copy(alpha = 0.5f)
                                    )
                                }
                            )
                        }
                    }

                    var draggedItem by remember { mutableStateOf<AssetEntity?>(null) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredHoldings, key = { it.coinId }) { asset ->
                            val isManualSort = sortOrder == SortOrder.MANUAL
                            val dragModifier = if (isManualSort) {
                                Modifier.pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { draggedItem = asset },
                                        onDragEnd = { 
                                            draggedItem?.let { 
                                                val fromIndex = filteredHoldings.indexOf(it)
                                                val toIndex = filteredHoldings.indexOf(asset)
                                                if (fromIndex != -1 && toIndex != -1) {
                                                    val reordered = filteredHoldings.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
                                                    viewModel.updateAssetOrder(reordered)
                                                }
                                            }
                                            draggedItem = null 
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                    }
                                }
                            } else {
                                Modifier
                            }

                            if (isCompactViewEnabled) {
                                CompactAssetCard(
                                    asset = asset,
                                    onClick = { selectedAssetForSheet = asset; scope.launch { sheetState.show() } },
                                    modifier = Modifier.animateItem().then(dragModifier)
                                )
                            } else {
                                FullAssetCard(
                                    asset = asset,
                                    modifier = Modifier.animateItem().then(dragModifier)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullAssetCard(asset: AssetEntity, modifier: Modifier) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()

    val numberFormat = NumberFormat.getNumberInstance()
    val subLabelColor = Color(cardTextColor.toColorInt()).copy(alpha = 0.6f)

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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(cardBgColor.toColorInt())),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(cardTextColor.toColorInt()).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    when (asset.category) {
                        AssetCategory.METAL -> {
                            val color = when (asset.symbol) {
                                "XAU" -> Color(0xFFFFD700)
                                "XAG" -> Color(0xFFC0C0C0)
                                "XPT" -> Color(0xFFE5E4E2)
                                "XPD" -> Color(0xFFE5E4E2)
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
                    Text(text = asset.symbol.uppercase(), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1)
                }

                Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "YOUR HOLDING", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = holdingAmountString,
                        color = Color(cardTextColor.toColorInt()),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (holdingAmountString.length > 12) 11.sp else if (holdingAmountString.length > 8) 14.sp else 17.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(text = asset.name.uppercase(), color = Color(cardTextColor.toColorInt()).copy(alpha = 0.6f), fontSize = if (asset.name.length > 12) 8.sp else 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1)
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
            HorizontalDivider(color = Color(cardTextColor.toColorInt()).copy(alpha = 0.05f))
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "ASSET PRICE", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text(text = "TOTAL VALUE", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = assetPriceString,
                        color = Color(cardTextColor.toColorInt()),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (assetPriceString.length > 12) 11.sp else if (assetPriceString.length > 9) 13.sp else 17.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = totalValueString,
                        color = Color(cardTextColor.toColorInt()),
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
    onClick: () -> Unit,
    modifier: Modifier
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()

    val isPositive = asset.priceChange24h >= 0
    val trendColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)

    val priceFormatter = if (asset.currentPrice < 0.10) DecimalFormat("$#,##0.0000") else DecimalFormat("$#,##0.00")
    val assetPriceString = priceFormatter.format(asset.currentPrice)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(cardBgColor.toColorInt())),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(cardTextColor.toColorInt()).copy(alpha = 0.15f))
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
                            "XAU" -> Color(0xFFFFD700)
                            "XAG" -> Color(0xFFC0C0C0)
                            "XPT" -> Color(0xFFE5E4E2)
                            "XPD" -> Color(0xFFE5E4E2)
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
                    color = Color(cardTextColor.toColorInt()),
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
                    color = Color(cardTextColor.toColorInt()),
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