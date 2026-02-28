@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.holdings

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
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
    var localHoldings by remember { mutableStateOf<List<AssetEntity>>(emptyList()) }

    LaunchedEffect(holdings) {
        if (localHoldings.isEmpty() && holdings.isNotEmpty()) {
            localHoldings = holdings
        }
    }

    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle(initialValue = false)

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()

    val safeBgHex = siteBgColor.ifBlank { "#000416" }
    val safeTextHex = siteTextColor.ifBlank { "#FFFFFF" }
    val safeCardBgHex = cardBgColor.ifBlank { "#121212" }
    val safeCardTextHex = cardTextColor.ifBlank { "#FFFFFF" }

    val bgColorInt = safeBgHex.toColorInt()
    val textColorInt = safeTextHex.toColorInt()
    val isDarkTheme = ColorUtils.calculateLuminance(bgColorInt) < 0.5

    var isRefreshing by remember { mutableStateOf(false) }
    var refreshProgress by remember { mutableFloatStateOf(0f) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            val startTime = System.currentTimeMillis()
            val duration = 30000L
            while (System.currentTimeMillis() - startTime < duration) {
                refreshProgress = (System.currentTimeMillis() - startTime).toFloat() / duration
                delay(50)
            }
            isRefreshing = false
            refreshProgress = 0f
        }
    }

    val lazyListState = rememberLazyListState()
    val draggedItemId = remember { mutableStateOf<String?>(null) }
    val isDraggingActive = remember { mutableStateOf(false) }
    val isOverTrash = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val assetToDelete = remember { mutableStateOf<AssetEntity?>(null) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")
    val isReorderEnabled = selectedTab == 0

    val filteredHoldings = remember(selectedTab, localHoldings) {
        when (selectedTab) {
            1 -> localHoldings.filter { it.category == AssetCategory.CRYPTO }
            2 -> localHoldings.filter { it.category == AssetCategory.METAL }
            else -> localHoldings
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
    }

    val totalPortfolioValue = holdings.sumOf { asset ->
        if (asset.isCustom) asset.currentPrice * (asset.weight * asset.amountHeld)
        else asset.currentPrice * asset.amountHeld
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val trashBoundsInRoot = remember { mutableStateOf<Rect?>(null) }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (!isReorderEnabled) return@rememberReorderableLazyListState
        localHoldings = localHoldings.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = Color(safeCardBgHex.toColorInt()),
            titleContentColor = Color(safeCardTextHex.toColorInt()),
            textContentColor = Color(safeCardTextHex.toColorInt()),
            title = { Text("DATA REFRESH", fontWeight = FontWeight.Black) },
            text = { Text("Market data refresh has a 30-second cooldown. Note: Metals are currently static in this version.") },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("GOT IT", fontWeight = FontWeight.Bold, color = Color(safeCardTextHex.toColorInt()))
                }
            }
        )
    }

    if (showDeleteDialog.value && assetToDelete.value != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false; assetToDelete.value = null },
            containerColor = Color(safeCardBgHex.toColorInt()),
            titleContentColor = Color(safeCardTextHex.toColorInt()),
            textContentColor = Color(safeCardTextHex.toColorInt()),
            title = { Text("DELETE ASSET?", fontWeight = FontWeight.Black) },
            text = { Text("Remove ${assetToDelete.value?.name} from your holdings?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    onClick = {
                        assetToDelete.value?.let { asset ->
                            viewModel.deleteAsset(asset)
                            localHoldings = localHoldings.filterNot { it.coinId == asset.coinId }
                        }
                        showDeleteDialog.value = false
                        assetToDelete.value = null
                    }
                ) { Text("DELETE", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false; assetToDelete.value = null }) {
                    Text("CANCEL", color = Color(safeCardTextHex.toColorInt()))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(bgColorInt))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull() ?: continue
                        val bounds = trashBoundsInRoot.value
                        isOverTrash.value = isDraggingActive.value && bounds != null && bounds.contains(change.position)
                    }
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color(bgColorInt))
                    .statusBarsPadding()
                    .padding(bottom = 0.dp)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp).width(70.dp).height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (!isRefreshing) { scope.launch { isRefreshing = true; viewModel.refreshAssets() } } }) {
                            Icon(Icons.Default.Refresh, null, tint = if (isRefreshing) Color(textColorInt).copy(0.2f) else Color(textColorInt), modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = { showInfoDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Info, null, tint = Color(textColorInt).copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Column(modifier = Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(120.dp))

                    Box(modifier = Modifier.height(12.dp).offset(y = (-40).dp), contentAlignment = Alignment.TopCenter) {
                        if (isRefreshing) {
                            LinearProgressIndicator(
                                progress = { refreshProgress },
                                modifier = Modifier.width(140.dp).height(6.dp).clip(CircleShape).border(1.dp, Color(textColorInt).copy(0.1f), CircleShape),
                                color = Color(textColorInt).copy(alpha = 0.7f),
                                trackColor = Color(textColorInt).copy(alpha = 0.05f)
                            )
                        }
                    }

                    // FIXED: Lowered portfolio value numbers down 10 pixels to fill space
                    Text(
                        text = currencyFormat.format(totalPortfolioValue),
                        color = Color(textColorInt),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.offset(y = (-25).dp)
                    )
                }

                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp).width(70.dp).height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { navController.navigate(Routes.ASSET_PICKER) }, modifier = Modifier.clip(CircleShape).background(Color.Yellow).size(44.dp)) {
                        Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                }
            }

            // FINAL DENSITY LOCK: Physical spacer pulls the rest of UI up to close gaps
            Spacer(modifier = Modifier.height((-85).dp))

            // --- TAB ROW ---
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.height(44.dp).padding(horizontal = 20.dp),
                containerColor = Color.Transparent, indicator = { }, divider = { }
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected, onClick = { selectedTab = index },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp).clip(CircleShape).background(if (isSelected) Color(safeCardBgHex.toColorInt()).copy(0.9f) else Color.Transparent).border(width = 1.dp, color = if (isSelected) Color.Transparent else Color(textColorInt).copy(0.15f), shape = CircleShape)
                    ) {
                        Text(text = title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) Color(safeCardTextHex.toColorInt()) else Color(textColorInt).copy(0.5f), modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }

            // --- ASSET LIST AREA ---
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = filteredHoldings, key = { it.coinId }) { asset ->
                        if (isReorderEnabled) {
                            ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
                                val scale by animateFloatAsState(targetValue = if (isDragging) 1.05f else 1f, label = "scale")
                                val elevation by animateFloatAsState(targetValue = if (isDragging) 30f else 0f, label = "elevation")
                                Box(modifier = Modifier.fillMaxWidth().zIndex(if (isDragging) 100f else 0f)
                                    .longPressDraggableHandle(
                                        onDragStarted = { draggedItemId.value = asset.coinId; isDraggingActive.value = true },
                                        onDragStopped = {
                                            val shouldDelete = isOverTrash.value
                                            isDraggingActive.value = false; isOverTrash.value = false
                                            if (shouldDelete) {
                                                assetToDelete.value = localHoldings.find { it.coinId == asset.coinId }
                                                showDeleteDialog.value = true
                                            } else {
                                                viewModel.updateAssetOrder(localHoldings)
                                            }
                                            draggedItemId.value = null
                                        }
                                    )
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        shadowElevation = elevation
                                        shape = RoundedCornerShape(16.dp)
                                        clip = true
                                    }
                                ) {
                                    if (isCompactViewEnabled) CompactAssetCard(asset = asset, modifier = Modifier)
                                    else FullAssetCard(asset = asset, modifier = Modifier)
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (isCompactViewEnabled) CompactAssetCard(asset = asset, modifier = Modifier)
                                else FullAssetCard(asset = asset, modifier = Modifier)
                            }
                        }
                    }
                }
            }

            // --- BOTTOM NAVIGATION ---
            Surface(modifier = Modifier.fillMaxWidth().height(40.dp), color = Color(bgColorInt)) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val iconColor = Color(textColorInt)
                    val itemColors = NavigationBarItemDefaults.colors(selectedIconColor = iconColor, unselectedIconColor = iconColor, indicatorColor = Color.Transparent)
                    NavigationBarItem(icon = { Icon(Icons.Default.Home, null, tint = iconColor) }, selected = currentRoute == Routes.HOME, onClick = { navController.navigate(Routes.HOME) }, colors = itemColors)
                    NavigationBarItem(icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null, tint = iconColor) }, selected = currentRoute == Routes.HOLDINGS, onClick = { navController.navigate(Routes.HOLDINGS) }, colors = itemColors)
                    NavigationBarItem(icon = { Icon(Icons.Default.Settings, null, tint = iconColor) }, selected = currentRoute == Routes.SETTINGS, onClick = { navController.navigate(Routes.SETTINGS) }, colors = itemColors)
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars).background(Color(bgColorInt)))
        }

        AnimatedVisibility(
            visible = isDraggingActive.value && isReorderEnabled,
            enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 100.dp)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size: IntSize = coords.size
                    trashBoundsInRoot.value = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                }
        ) {
            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape).background(if (isOverTrash.value) Color.Red else Color.DarkGray.copy(0.9f)).border(3.dp, if (isOverTrash.value) Color.White else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
    }
}

// -------------------- COMPONENTS --------------------

@Composable
fun MetalIcon(name: String, size: Int = 44) {
    val metalColors = when {
        name.contains("Gold", ignoreCase = true) -> listOf(Color(0xFFFFD700), Color(0xFFB8860B))
        name.contains("Silver", ignoreCase = true) -> listOf(Color(0xFFE0E0E0), Color(0xFF757575))
        name.contains("Platinum", ignoreCase = true) -> listOf(Color(0xFFF5F5F5), Color(0xFFBDBDBD))
        name.contains("Palladium", ignoreCase = true) -> listOf(Color(0xFFCED4DA), Color(0xFF495057))
        else -> listOf(Color(0xFF8A8D8F), Color(0xFF343a40))
    }
    Box(modifier = Modifier.size(size.dp).clip(RoundedCornerShape(8.dp)).background(Brush.radialGradient(metalColors, center = Offset.Zero)), contentAlignment = Alignment.Center) {
        Icon(imageVector = if (name.contains("Bar", ignoreCase = true)) Icons.Default.ViewCarousel else Icons.Default.Toll, contentDescription = null, tint = Color.Black.copy(0.2f), modifier = Modifier.size((size * 0.6).dp))
        Canvas(modifier = Modifier.fillMaxSize()) { drawRect(brush = Brush.linearGradient(0.0f to Color.White.copy(alpha = 0.3f), 0.5f to Color.Transparent, 1.0f to Color.Black.copy(alpha = 0.1f))) }
    }
}

@Composable
fun FullAssetCard(asset: AssetEntity, modifier: Modifier) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()
    val safeCardBg = cardBgColor.ifBlank { "#121212" }; val safeCardText = cardTextColor.ifBlank { "#FFFFFF" }
    val isPositive = asset.priceChange24h > 0; val isNeutral = asset.priceChange24h == 0.0
    val trendColor = when { isPositive -> Color(0xFF00C853); isNeutral -> Color.Gray; else -> Color(0xFFD32F2F) }
    val trendIcon = when { isPositive -> Icons.Filled.ArrowDropUp; isNeutral -> Icons.Filled.FiberManualRecord; else -> Icons.Filled.ArrowDropDown }
    val totalValue = if (asset.isCustom) asset.currentPrice * (asset.weight * asset.amountHeld) else asset.currentPrice * asset.amountHeld
    val totalValueString = if (totalValue >= 1000000) DecimalFormat("$#,##0").format(totalValue) else DecimalFormat("$#,##0.00").format(totalValue)
    val displaySymbol = when { asset.isCustom && asset.name.contains("Gold", ignoreCase = true) -> "GOLD"; asset.isCustom && asset.name.contains("Silver", ignoreCase = true) -> "SILVER"; asset.isCustom && asset.name.contains("Platinum", ignoreCase = true) -> "PLAT"; asset.isCustom && asset.name.contains("Palladium", ignoreCase = true) -> "PALL"; else -> asset.symbol.uppercase() }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(safeCardBg.toColorInt())),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(safeCardText.toColorInt()).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (asset.category == AssetCategory.METAL) MetalIcon(asset.name) else AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.height(4.dp)); Text(displaySymbol, color = Color(safeCardText.toColorInt()).copy(0.7f), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.Center)
                }
                Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YOUR HOLDING", color = Color(safeCardText.toColorInt()).copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(NumberFormat.getNumberInstance().format(asset.amountHeld), color = Color(safeCardText.toColorInt()), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = asset.name.uppercase(),
                            color = Color(safeCardText.toColorInt()),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Bold,
                            softWrap = true
                        )
                    }
                }
                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                    SparklineChart(asset.sparklineData, trendColor, Modifier.width(75.dp).height(32.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(end = 4.dp)) {
                        Icon(trendIcon, null, tint = trendColor, modifier = Modifier.size(24.dp))
                        Text(text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                    }
                }
            }
            // CENTER-CUT LOCK: Spacer remains at 2.dp to maintain card density
            Spacer(Modifier.height(2.dp)); HorizontalDivider(color = Color(safeCardText.toColorInt()).copy(alpha = 0.05f))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text("ASSET PRICE", color = Color(safeCardText.toColorInt()).copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(DecimalFormat("$#,##0.00").format(asset.currentPrice), color = Color(safeCardText.toColorInt()), fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text("TOTAL VALUE", color = Color(safeCardText.toColorInt()).copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(totalValueString, color = Color(safeCardText.toColorInt()), fontWeight = FontWeight.Black, fontSize = 17.sp) }
            }
        }
    }
}

@Composable
fun CompactAssetCard(asset: AssetEntity, modifier: Modifier) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()
    val safeCardBg = cardBgColor.ifBlank { "#121212" }; val safeCardText = cardTextColor.ifBlank { "#FFFFFF" }
    val isPositive = asset.priceChange24h > 0; val isNeutral = asset.priceChange24h == 0.0
    val trendColor = when { isPositive -> Color(0xFF00C853); isNeutral -> Color.Gray; else -> Color(0xFFD32F2F) }
    val trendIcon = when { isPositive -> Icons.Filled.ArrowDropUp; isNeutral -> Icons.Filled.FiberManualRecord; else -> Icons.Filled.ArrowDropDown }
    val totalValue = if (asset.isCustom) asset.currentPrice * (asset.weight * asset.amountHeld) else asset.currentPrice * asset.amountHeld
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(safeCardBg.toColorInt())), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(safeCardText.toColorInt()).copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1.1f), verticalAlignment = Alignment.CenterVertically) {
                if (asset.category == AssetCategory.METAL) MetalIcon(asset.name, size = 32) else AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(asset.symbol.uppercase(), color = Color(safeCardText.toColorInt()), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    val compactValue = if (totalValue >= 1000000) DecimalFormat("$#,##0").format(totalValue) else DecimalFormat("$#,##0.00").format(totalValue)
                    Text(compactValue, color = Color(safeCardText.toColorInt()).copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(modifier = Modifier.weight(0.7f).height(24.dp)) { SparklineChart(asset.sparklineData, trendColor, Modifier.fillMaxSize()) }
            Row(modifier = Modifier.weight(1.4f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) { Text(DecimalFormat("$#,##0.00").format(asset.currentPrice), color = Color(safeCardText.toColorInt()), fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false) }
                Icon(trendIcon, null, tint = trendColor, modifier = Modifier.size(32.dp).offset(x = 4.dp))
            }
        }
    }
}

@Composable
fun SparklineChart(sparklineData: List<Double>, changeColor: Color, modifier: Modifier = Modifier) {
    if (sparklineData.size < 2) { Box(modifier); return }
    val minPrice = sparklineData.minOrNull() ?: 0.0; val maxPrice = sparklineData.maxOrNull() ?: 0.0; val priceRange = if ((maxPrice - minPrice) > 0) maxPrice - minPrice else 1.0
    Canvas(modifier) {
        val points = sparklineData.mapIndexed { index, price -> Offset(index.toFloat() / (sparklineData.size - 1) * size.width, size.height - ((price - minPrice) / priceRange * size.height).toFloat()) }
        val linePath = Path().apply { moveTo(points[0].x, points[0].y); for (i in 1 until points.size) { val prev = points[i - 1]; val curr = points[i]; val cp1x = prev.x + (curr.x - prev.x) / 2f; cubicTo(cp1x, prev.y, cp1x, curr.y, curr.x, curr.y) } }
        drawPath(path = linePath, color = changeColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path = linePath.apply { lineTo(size.width, size.height); lineTo(0f, size.height); close() }, brush = Brush.verticalGradient(listOf(changeColor.copy(alpha = 0.2f), Color.Transparent)))
    }
}