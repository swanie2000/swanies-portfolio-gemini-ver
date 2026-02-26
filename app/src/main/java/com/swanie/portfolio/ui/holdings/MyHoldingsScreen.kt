@file:OptIn(ExperimentalFoundationApi::class)

package com.swanie.portfolio.ui.holdings

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

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
    var isRefreshing by remember { mutableStateOf(false) }
    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle(initialValue = false)

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()

    var timerProgress by remember { mutableFloatStateOf(0f) }

    val view = LocalView.current
    val context = LocalContext.current

    // Fixed luminance check
    val luminance = ColorUtils.calculateLuminance(siteBgColor.toColorInt())
    val isDarkTheme = luminance < 0.5

    SideEffect {
        val window = (context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }

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
        if (asset.isCustom) asset.currentPrice * (asset.weight * asset.amountHeld)
        else asset.currentPrice * asset.amountHeld
    }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var selectedAssetForSheet by remember { mutableStateOf<AssetEntity?>(null) }

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

    // MAIN LAYOUT STACK
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(siteBgColor.toColorInt()))
    ) {
        // 1. TOP CHARGING BAR
        val animatedProgress by animateFloatAsState(
            targetValue = if (isRefreshing) timerProgress else 0f,
            animationSpec = tween(durationMillis = 30000, easing = LinearEasing),
            label = "progressAnimation"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .statusBarsPadding(),
            color = Color(siteTextColor.toColorInt()),
            trackColor = Color.Transparent
        )

        // 2. HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(siteBgColor.toColorInt()))
        ) {
            IconButton(
                onClick = {
                    if (!isRefreshing) {
                        scope.launch {
                            isRefreshing = true
                            timerProgress = 1f
                            viewModel.refreshAssets()
                            delay(30000)
                            isRefreshing = false
                            timerProgress = 0f
                        }
                    }
                },
                enabled = !isRefreshing,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isRefreshing) Color(siteTextColor.toColorInt()).copy(alpha = 0.3f) else Color(siteTextColor.toColorInt())
                )
            }

            Image(
                painter = painterResource(id = R.drawable.swanie_foreground),
                contentDescription = "Swan Logo",
                modifier = Modifier.size(100.dp).align(Alignment.TopCenter)
            )

            Text(
                text = currencyFormat.format(totalPortfolioValue),
                color = Color(siteTextColor.toColorInt()),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-10).dp)
            )

            IconButton(
                onClick = { navController.navigate(Routes.ASSET_PICKER) },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Asset", tint = Color(siteTextColor.toColorInt()))
            }
        }

        // 3. TAB ROW
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.height(30.dp),
            containerColor = Color(siteBgColor.toColorInt()),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 2.dp,
                    color = Color(siteTextColor.toColorInt())
                )
            },
            divider = { }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(siteTextColor.toColorInt())) }
                )
            }
        }

        // 4. THE CONSTRAINED LIST
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .background(Color(siteBgColor.toColorInt()))
        ) {
            var draggedItem by remember { mutableStateOf<AssetEntity?>(null) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            ) { change, _ -> change.consume() }
                        }
                    } else Modifier

                    if (isCompactViewEnabled) {
                        CompactAssetCard(asset = asset, onClick = { selectedAssetForSheet = asset; scope.launch { sheetState.show() } }, modifier = Modifier.animateItem().then(dragModifier))
                    } else {
                        FullAssetCard(asset = asset, modifier = Modifier.animateItem().then(dragModifier))
                    }
                }
            }
        }

        // 5. NAV BAR DOCK
        Surface(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            color = Color(siteBgColor.toColorInt())
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(siteTextColor.toColorInt()),
                    unselectedIconColor = Color(siteTextColor.toColorInt()).copy(alpha = 0.5f),
                    indicatorColor = Color.Transparent
                )

                NavigationBarItem(icon = { Icon(Icons.Default.Home, null) }, selected = currentRoute == Routes.HOME, onClick = { navController.navigate(Routes.HOME) }, colors = itemColors)
                NavigationBarItem(icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null) }, selected = currentRoute == Routes.HOLDINGS, onClick = { navController.navigate(Routes.HOLDINGS) }, colors = itemColors)
                NavigationBarItem(icon = { Icon(Icons.Default.Settings, null) }, selected = currentRoute == Routes.SETTINGS, onClick = { navController.navigate(Routes.SETTINGS) }, colors = itemColors)
            }
        }

        // 6. SYSTEM BOTTOM BAR FLOOR
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(Color(siteBgColor.toColorInt()))
        )
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
    val totalValue = if (asset.isCustom) asset.currentPrice * (asset.weight * asset.amountHeld) else asset.currentPrice * asset.amountHeld
    val totalValueString = if (totalValue >= 1_000_000) DecimalFormat("$#,##0").format(totalValue) else DecimalFormat("$#,##0.00").format(totalValue)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(cardBgColor.toColorInt())),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(cardTextColor.toColorInt()).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    if (asset.category == AssetCategory.METAL) {
                        val color = when (asset.symbol) { "XAU" -> Color(0xFFFFD700); "XAG" -> Color(0xFFC0C0C0); else -> Color.Gray }
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(color))
                    } else {
                        AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(text = asset.symbol.uppercase(), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "YOUR HOLDING", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = numberFormat.format(asset.amountHeld), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, maxLines = 1)
                    Text(text = asset.name.uppercase(), color = subLabelColor, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    SparklineChart(asset.sparklineData, trendColor, Modifier.width(75.dp).height(32.dp))
                    Box(modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(text = "${if(isPositive) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(cardTextColor.toColorInt()).copy(alpha = 0.05f))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ASSET PRICE", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(priceFormatter.format(asset.currentPrice), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL VALUE", color = subLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(totalValueString, color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Black, fontSize = 17.sp)
                }
            }
        }
    }
}

@Composable
fun CompactAssetCard(asset: AssetEntity, onClick: () -> Unit, modifier: Modifier) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()
    val isPositive = asset.priceChange24h >= 0
    val trendColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(cardBgColor.toColorInt())),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(cardTextColor.toColorInt()).copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                if (asset.category == AssetCategory.METAL) {
                    val color = when (asset.symbol) { "XAU" -> Color(0xFFFFD700); "XAG" -> Color(0xFFC0C0C0); else -> Color.Gray }
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(color))
                } else {
                    AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)))
                }
                Spacer(Modifier.width(8.dp))
                Text(asset.symbol.uppercase(), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            Box(modifier = Modifier.weight(1f).height(24.dp)) { SparklineChart(asset.sparklineData, trendColor, Modifier.fillMaxSize()) }
            Column(modifier = Modifier.weight(1.3f), horizontalAlignment = Alignment.End) {
                Text(DecimalFormat("$#,##0.00").format(asset.currentPrice), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${if (isPositive) "+" else ""}${String.format("%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
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