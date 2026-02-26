@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.holdings

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MyHoldingsScreen(
    mainViewModel: MainViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    val localHoldings = remember { mutableStateListOf<AssetEntity>() }

    LaunchedEffect(holdings) {
        if (localHoldings.isEmpty() && holdings.isNotEmpty()) {
            localHoldings.addAll(holdings)
        }
    }

    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle(initialValue = false)
    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    var timerProgress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Proper delegated state for reassignment
    var draggedItemId by remember { mutableStateOf<String?>(null) }
    var draggingOffset by remember { mutableStateOf(Offset.Zero) }
    var isOverTrash by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var assetToDelete by remember { mutableStateOf<AssetEntity?>(null) }
    var reorderJob by remember { mutableStateOf<Job?>(null) }

    val listSpacing by animateDpAsState(
        targetValue = if (draggedItemId != null) 32.dp else 8.dp,
        animationSpec = spring(stiffness = 50f), // Slow expansion
        label = "listSpacing"
    )

    val bgColorInt = siteBgColor.toColorInt()
    val textColorInt = siteTextColor.toColorInt()
    val isDarkTheme = ColorUtils.calculateLuminance(bgColorInt) < 0.5

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")
    val filteredHoldings = remember(selectedTab, localHoldings.toList()) {
        when (selectedTab) {
            1 -> localHoldings.filter { it.category == AssetCategory.CRYPTO }
            2 -> localHoldings.filter { it.category == AssetCategory.METAL }
            else -> localHoldings.toList()
        }
    }

    val totalPortfolioValue = holdings.sumOf { asset ->
        if (asset.isCustom) asset.currentPrice * (asset.weight * asset.amountHeld)
        else asset.currentPrice * asset.amountHeld
    }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    if (showDeleteDialog && assetToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; assetToDelete = null },
            containerColor = Color(cardBgColor.toColorInt()),
            titleContentColor = Color(cardTextColor.toColorInt()),
            textContentColor = Color(cardTextColor.toColorInt()),
            title = { Text("DELETE ASSET?", fontWeight = FontWeight.Black) },
            text = { Text("Remove ${assetToDelete?.name} from your holdings?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    onClick = {
                        assetToDelete?.let {
                            viewModel.deleteAsset(it)
                            localHoldings.remove(it)
                        }
                        showDeleteDialog = false; assetToDelete = null
                    }
                ) { Text("DELETE", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; assetToDelete = null }) {
                    Text("CANCEL", color = Color(cardTextColor.toColorInt()))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(bgColorInt))) {
        Column(modifier = Modifier.fillMaxSize()) {

            val animatedProgress by animateFloatAsState(
                targetValue = if (isRefreshing) timerProgress else 0f,
                animationSpec = tween(durationMillis = 30000, easing = LinearEasing),
                label = "progressAnimation"
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(4.dp).statusBarsPadding(),
                color = Color(textColorInt),
                trackColor = Color.Transparent
            )

            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(bgColorInt))) {
                IconButton(
                    onClick = {
                        if (!isRefreshing) {
                            scope.launch {
                                isRefreshing = true; timerProgress = 1f
                                viewModel.refreshAssets()
                                delay(30000)
                                isRefreshing = false; timerProgress = 0f
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = if (isRefreshing) Color(textColorInt).copy(0.3f) else Color(textColorInt))
                }

                Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.TopCenter))

                Text(
                    text = currencyFormat.format(totalPortfolioValue),
                    color = Color(textColorInt),
                    fontSize = 24.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-10).dp)
                )

                IconButton(onClick = { navController.navigate(Routes.ASSET_PICKER) }, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color(textColorInt))
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.height(30.dp),
                containerColor = Color(bgColorInt),
                indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(it[selectedTab]), height = 2.dp, color = Color(textColorInt)) },
                divider = { }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(textColorInt)) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds().pointerInput(filteredHoldings) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val item = listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
                        item?.let { draggedItemId = filteredHoldings[it.index].coinId }
                    },
                    onDragEnd = {
                        reorderJob?.cancel()
                        if (isOverTrash) {
                            assetToDelete = localHoldings.find { it.coinId == draggedItemId }
                            showDeleteDialog = true
                        } else {
                            viewModel.updateAssetOrder(localHoldings.toList())
                        }
                        draggedItemId = null; draggingOffset = Offset.Zero; isOverTrash = false
                    },
                    onDragCancel = {
                        reorderJob?.cancel()
                        draggedItemId = null; draggingOffset = Offset.Zero; isOverTrash = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        draggingOffset += dragAmount

                        val globalPos = change.position
                        isOverTrash = globalPos.x > size.width * 0.65f && globalPos.y > size.height * 0.7f

                        if (reorderJob?.isActive != true) {
                            reorderJob = scope.launch {
                                val id = draggedItemId ?: return@launch
                                val fromIndex = localHoldings.indexOfFirst { it.coinId == id }
                                if (fromIndex == -1) return@launch

                                val itemSize = listState.layoutInfo.visibleItemsInfo.find { it.index == fromIndex }?.size ?: 300
                                val threshold = itemSize.toFloat() * 0.9f // Require significant drag to trigger move

                                val targetIndex = when {
                                    draggingOffset.y > threshold -> fromIndex + 1
                                    draggingOffset.y < -threshold -> fromIndex - 1
                                    else -> fromIndex
                                }

                                if (targetIndex in localHoldings.indices && targetIndex != fromIndex) {
                                    localHoldings.add(targetIndex, localHoldings.removeAt(fromIndex))
                                    draggingOffset = Offset(draggingOffset.x, draggingOffset.y + (if (targetIndex > fromIndex) -itemSize.toFloat() else itemSize.toFloat()))
                                }
                                delay(600) // Super slow reorder check to prevent jumpiness
                            }
                        }

                        if (globalPos.y < 120f) { scope.launch { listState.scrollBy(-30f) } }
                        if (globalPos.y > size.height - 120f) { scope.launch { listState.scrollBy(30f) } }
                    }
                )
            }) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(listSpacing)
                ) {
                    itemsIndexed(filteredHoldings, key = { _, item -> item.coinId }) { index, asset ->
                        val isDraggingThisItem = draggedItemId == asset.coinId
                        val scale by animateFloatAsState(if (isDraggingThisItem) 1.05f else 1f, label = "scale")

                        Box(
                            modifier = Modifier.fillMaxWidth().animateItem(
                                // ULTRA SLOW PHYSICS
                                placementSpec = spring(
                                    stiffness = 25f, // Underwater feel
                                    dampingRatio = Spring.DampingRatioHighBouncy
                                )
                            ).zIndex(if (isDraggingThisItem) 10f else 1f).graphicsLayer {
                                scaleX = scale; scaleY = scale
                                if (isDraggingThisItem) {
                                    translationX = draggingOffset.x
                                    translationY = draggingOffset.y
                                }
                            }
                        ) {
                            if (isCompactViewEnabled) {
                                CompactAssetCard(asset = asset, onClick = { }, modifier = Modifier)
                            } else {
                                FullAssetCard(asset = asset, modifier = Modifier)
                            }
                        }
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth().height(40.dp), color = Color(bgColorInt)) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val itemColors = NavigationBarItemDefaults.colors(selectedIconColor = Color(textColorInt), unselectedIconColor = Color(textColorInt).copy(0.5f), indicatorColor = Color.Transparent)

                    NavigationBarItem(icon = { Icon(Icons.Default.Home, null) }, selected = currentRoute == Routes.HOME, onClick = { navController.navigate(Routes.HOME) }, colors = itemColors)
                    NavigationBarItem(icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null) }, selected = currentRoute == Routes.HOLDINGS, onClick = { navController.navigate(Routes.HOLDINGS) }, colors = itemColors)
                    NavigationBarItem(icon = { Icon(Icons.Default.Settings, null) }, selected = currentRoute == Routes.SETTINGS, onClick = { navController.navigate(Routes.SETTINGS) }, colors = itemColors)
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars).background(Color(bgColorInt)))
        }

        AnimatedVisibility(
            visible = draggedItemId != null,
            enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 100.dp)
        ) {
            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape).background(if (isOverTrash) Color.Red else Color.DarkGray.copy(0.9f)).border(3.dp, if (isOverTrash) Color.White else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
    }
}

// FullAssetCard and other components remain exactly as previous working version...
@Composable
fun FullAssetCard(asset: AssetEntity, modifier: Modifier) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()
    val numberFormat = NumberFormat.getNumberInstance()
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
                        val color = when (asset.symbol) { "XAU" -> Color(0xFFFFD700); "XAG" -> Color(0xFFC0C0C0); "XPT" -> Color(0xFFE5E4E2); else -> Color.Gray }
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(color))
                    } else {
                        AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(text = asset.symbol.uppercase(), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YOUR HOLDING", color = Color(cardTextColor.toColorInt()).copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = numberFormat.format(asset.amountHeld), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Text(asset.name.uppercase(), color = Color(cardTextColor.toColorInt()).copy(0.6f), fontSize = 10.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    SparklineChart(asset.sparklineData, trendColor, Modifier.width(75.dp).height(32.dp))
                    Box(modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(text = "${if(isPositive) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(cardTextColor.toColorInt()).copy(alpha = 0.05f))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ASSET PRICE", color = Color(cardTextColor.toColorInt()).copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(priceFormatter.format(asset.currentPrice), color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL VALUE", color = Color(cardTextColor.toColorInt()).copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(totalValueString, color = Color(cardTextColor.toColorInt()), fontWeight = FontWeight.Black, fontSize = 17.sp)
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
                    val color = when (asset.symbol) { "XAU" -> Color(0xFFFFD700); "XAG" -> Color(0xFFC0C0C0); "XPT" -> Color(0xFFE5E4E2); else -> Color.Gray }
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
                Text("${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
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