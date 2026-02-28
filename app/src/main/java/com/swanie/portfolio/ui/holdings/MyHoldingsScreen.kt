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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
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

    val lazyListState = rememberLazyListState()
    val isDraggingActive = remember { mutableStateOf(false) }
    val isSavingOrder = remember { mutableStateOf(false) }
    val isOverTrash = remember { mutableStateOf(false) }
    val trashBoundsInRoot = remember { mutableStateOf<Rect?>(null) }

    var assetBeingEdited by remember { mutableStateOf<AssetEntity?>(null) }
    var expandedAssetId by remember { mutableStateOf<String?>(null) }
    var showEditButtonId by remember { mutableStateOf<String?>(null) }

    // TAB STATE LOGIC
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")

    LaunchedEffect(holdings) {
        if (!isDraggingActive.value && !isSavingOrder.value && assetBeingEdited == null) {
            localHoldings = holdings
        }
    }

    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle(initialValue = false)
    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val bgColorInt = siteBgColor.ifBlank { "#000416" }.toColorInt()
    val textColorInt = siteTextColor.ifBlank { "#FFFFFF" }.toColorInt()
    val isDarkTheme = ColorUtils.calculateLuminance(bgColorInt) < 0.5

    var isRefreshing by remember { mutableStateOf(false) }
    var refreshProgress by remember { mutableFloatStateOf(0f) }
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

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localHoldings = localHoldings.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(bgColorInt)).pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: continue
                val bounds = trashBoundsInRoot.value
                isOverTrash.value = isDraggingActive.value && bounds != null && bounds.contains(change.position)
            }
        }
    }) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().background(Color(bgColorInt)).statusBarsPadding()) {
                Box(modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp).width(70.dp).height(120.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { if (!isRefreshing) { scope.launch { isRefreshing = true; viewModel.refreshAssets() } } }) {
                        Icon(Icons.Default.Refresh, null, tint = if (isRefreshing) Color(textColorInt).copy(0.2f) else Color(textColorInt), modifier = Modifier.size(28.dp))
                    }
                }
                Column(modifier = Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(120.dp))
                    Box(modifier = Modifier.height(12.dp).offset(y = (-40).dp)) {
                        if (isRefreshing) LinearProgressIndicator(progress = { refreshProgress }, modifier = Modifier.width(140.dp).height(6.dp).clip(CircleShape), color = Color(textColorInt).copy(0.7f), trackColor = Color(textColorInt).copy(0.05f))
                    }
                    Text(text = currencyFormat.format(totalPortfolioValue), color = Color(textColorInt), fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(y = (-25).dp).clickable { navController.navigate(Routes.HOME) })
                }
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp).width(70.dp).height(120.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { navController.navigate(Routes.ASSET_PICKER) }, modifier = Modifier.clip(CircleShape).background(Color.Yellow).size(44.dp)) {
                        Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height((-85).dp))

            // FIXED: TABS NOW UPDATE STATE
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.height(44.dp).padding(horizontal = 20.dp),
                containerColor = Color.Transparent, indicator = { }, divider = { }
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index }, // FIX: Update index on click
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp).clip(CircleShape).background(if (isSelected) Color(textColorInt).copy(0.15f) else Color.Transparent).border(width = 1.dp, color = if (isSelected) Color.Transparent else Color(textColorInt).copy(0.15f), shape = CircleShape)
                    ) {
                        Text(text = title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) Color(textColorInt) else Color(textColorInt).copy(0.5f), modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- ASSET LIST ---
            LazyColumn(state = lazyListState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // FIXED: FILTERING LOGIC
                val filteredHoldings = when(selectedTab) {
                    1 -> localHoldings.filter { it.category == AssetCategory.CRYPTO }
                    2 -> localHoldings.filter { it.category == AssetCategory.METAL }
                    else -> localHoldings
                }

                items(items = filteredHoldings, key = { it.coinId }) { asset ->
                    ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
                        val isExpanded = expandedAssetId == asset.coinId
                        val isEditButtonVisible = showEditButtonId == asset.coinId

                        if (isCompactViewEnabled && !isExpanded) {
                            CompactAssetCard(
                                asset = asset,
                                isDragging = isDragging,
                                onExpandToggle = { expandedAssetId = asset.coinId; showEditButtonId = null },
                                modifier = Modifier.longPressDraggableHandle(
                                    onDragStarted = { isDraggingActive.value = true },
                                    onDragStopped = {
                                        isDraggingActive.value = false
                                        scope.launch { isSavingOrder.value = true; viewModel.updateAssetOrder(localHoldings); delay(500); isSavingOrder.value = false }
                                    }
                                )
                            )
                        } else {
                            FullAssetCard(
                                asset = asset,
                                isExpanded = isExpanded,
                                isEditing = false,
                                isDragging = isDragging,
                                showEditButton = isEditButtonVisible,
                                onExpandToggle = {
                                    if (isCompactViewEnabled) {
                                        if (expandedAssetId == asset.coinId) {
                                            if (isEditButtonVisible) { expandedAssetId = null; showEditButtonId = null }
                                            else { showEditButtonId = asset.coinId }
                                        } else { expandedAssetId = asset.coinId; showEditButtonId = null }
                                    } else {
                                        showEditButtonId = if (isEditButtonVisible) null else asset.coinId
                                    }
                                },
                                onEditRequest = { assetBeingEdited = asset },
                                onSave = { _, _, _ -> },
                                modifier = Modifier.longPressDraggableHandle(
                                    onDragStarted = { isDraggingActive.value = true },
                                    onDragStopped = {
                                        isDraggingActive.value = false
                                        scope.launch { isSavingOrder.value = true; viewModel.updateAssetOrder(localHoldings); delay(500); isSavingOrder.value = false }
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // BOTTOM NAVIGATION: Synchronized with siteTextColor
            Surface(modifier = Modifier.fillMaxWidth().height(40.dp).background(Color(bgColorInt))) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val baseTextColor = Color(textColorInt)

                    IconButton(onClick = { navController.navigate(Routes.HOME) }) {
                        Icon(Icons.Default.Home, null, tint = if(currentRoute == Routes.HOME) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                    }
                    IconButton(onClick = { navController.navigate(Routes.HOLDINGS) }) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null, tint = if(currentRoute == Routes.HOLDINGS) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                    }
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, null, tint = if(currentRoute == Routes.SETTINGS) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                    }
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars).background(Color(bgColorInt)))
        }

        // --- FLOATING EDIT OVERLAY ---
        AnimatedVisibility(visible = assetBeingEdited != null, enter = fadeIn() + slideInVertically { -it }, exit = fadeOut() + slideOutVertically { -it }, modifier = Modifier.zIndex(1000f)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)).statusBarsPadding()) {
                assetBeingEdited?.let { asset ->
                    FullAssetCard(
                        asset = asset, isExpanded = true, isEditing = true, isDragging = false, showEditButton = true,
                        onExpandToggle = { }, onEditRequest = { },
                        onSave = { name, amount, decimals ->
                            scope.launch {
                                isSavingOrder.value = true
                                localHoldings = localHoldings.map { if (it.coinId == asset.coinId) it.copy(name = name, amountHeld = amount, decimalPreference = decimals) else it }
                                viewModel.updateAsset(asset, name, amount, decimals)
                                viewModel.updateAssetOrder(localHoldings)
                                delay(500)
                                isSavingOrder.value = false; assetBeingEdited = null; expandedAssetId = null; showEditButtonId = null
                            }
                        },
                        onCancel = { assetBeingEdited = null },
                        modifier = Modifier.padding(16.dp).align(Alignment.TopCenter)
                    )
                }
            }
        }

        AnimatedVisibility(visible = isDraggingActive.value, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 100.dp).onGloballyPositioned { coords -> val pos = coords.positionInRoot(); val size: IntSize = coords.size; trashBoundsInRoot.value = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height) }) {
            Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(if (isOverTrash.value) Color.Red else Color.DarkGray.copy(0.9f)).border(3.dp, if (isOverTrash.value) Color.White else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(40.dp)) }
        }
    }
}

// -------------------- COMPONENTS --------------------

@Composable
fun AutoResizingText(text: String, style: TextStyle, modifier: Modifier = Modifier, maxLines: Int = 1) {
    var fontSizeValue by remember { mutableStateOf(style.fontSize) }
    var readyToDraw by remember { mutableStateOf(false) }
    Text(
        text = text, style = style.copy(fontSize = fontSizeValue), modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        maxLines = maxLines, softWrap = false, overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult -> if (textLayoutResult.hasVisualOverflow && fontSizeValue > 8.sp) fontSizeValue *= 0.9f else readyToDraw = true }
    )
}

@Composable
fun FullAssetCard(
    asset: AssetEntity,
    isExpanded: Boolean,
    isEditing: Boolean,
    isDragging: Boolean,
    showEditButton: Boolean,
    onExpandToggle: () -> Unit,
    onEditRequest: () -> Unit,
    onSave: (String, Double, Int) -> Unit,
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var editName by remember(asset) { mutableStateOf(asset.name) }
    var editAmount by remember(asset) { mutableStateOf(asset.amountHeld.toString()) }
    var editDecimals by remember(asset) { mutableFloatStateOf(asset.decimalPreference.toFloat()) }
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()
    val safeCardBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val safeCardText = Color(cardTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    val elevation by animateDpAsState(if (isDragging) 30.dp else 0.dp)
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f)

    Card(
        modifier = modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = elevation.toPx(); shape = RoundedCornerShape(16.dp); clip = true }.animateContentSize().clickable(enabled = !isEditing) { onExpandToggle() },
        colors = CardDefaults.cardColors(containerColor = safeCardBg), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, safeCardText.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (asset.category == AssetCategory.METAL) MetalIcon(asset.name) else AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.height(4.dp)); Text(asset.symbol.uppercase(), color = safeCardText.copy(0.7f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
                Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YOUR HOLDING", color = safeCardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    if (isEditing) BasicTextField(value = editAmount, onValueChange = { editAmount = it }, textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.background(safeCardText.copy(0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp))
                    else Text(formatAmount(asset.amountHeld), color = safeCardText, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                        if (isEditing) BasicTextField(value = editName, onValueChange = { editName = it }, textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center), modifier = Modifier.background(safeCardText.copy(0.1f), RoundedCornerShape(4.dp)).padding(4.dp))
                        else Text(asset.name.uppercase(), color = safeCardText, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, softWrap = true)
                    }
                }
                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                    if (showEditButton && !isEditing) {
                        IconButton(onClick = { onEditRequest() }, modifier = Modifier.size(40.dp).background(Color.Yellow, CircleShape)) { Icon(Icons.Default.Edit, contentDescription = "Edit Asset", tint = Color.Black, modifier = Modifier.size(22.dp)) }
                    } else {
                        SparklineChart(asset.sparklineData, trendColor, Modifier.width(75.dp).height(32.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(trendColor.copy(alpha = 0.15f)).padding(end = 4.dp)) {
                        Icon(if (asset.priceChange24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = trendColor, modifier = Modifier.size(24.dp))
                        Text(text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.height(2.dp)); HorizontalDivider(color = safeCardText.copy(alpha = 0.05f))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text("ASSET PRICE", color = safeCardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(formatCurrency(asset.currentPrice, asset.decimalPreference), color = safeCardText, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text("TOTAL VALUE", color = safeCardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); val totalValue = if (asset.isCustom) asset.currentPrice * (asset.weight * asset.amountHeld) else asset.currentPrice * asset.amountHeld; Text(formatCurrency(totalValue, asset.decimalPreference), color = safeCardText, fontWeight = FontWeight.Black, fontSize = 17.sp) }
            }
            AnimatedVisibility(visible = isEditing) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text("DECIMAL PREFERENCE: ${editDecimals.toInt()}", color = safeCardText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(value = editDecimals, onValueChange = { editDecimals = it }, valueRange = 0f..8f, steps = 7, colors = SliderDefaults.colors(thumbColor = Color.Yellow, activeTrackColor = Color.Yellow))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { IconButton(onClick = { onCancel() }, modifier = Modifier.size(36.dp).background(Color.Red, CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp)) }; Spacer(Modifier.width(16.dp)); IconButton(onClick = { onSave(editName, editAmount.toDoubleOrNull() ?: asset.amountHeld, editDecimals.toInt()) }, modifier = Modifier.size(36.dp).background(Color.Yellow, CircleShape)) { Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(20.dp)) } }
                }
            }
        }
    }
}

@Composable
fun CompactAssetCard(asset: AssetEntity, isDragging: Boolean, onExpandToggle: () -> Unit, modifier: Modifier = Modifier) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()
    val safeCardBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val safeCardText = Color(cardTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    val elevation by animateDpAsState(if (isDragging) 30.dp else 0.dp)
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f)

    Card(modifier = modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = elevation.toPx(); shape = RoundedCornerShape(12.dp); clip = true }.clickable { onExpandToggle() }, colors = CardDefaults.cardColors(containerColor = safeCardBg), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, safeCardText.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) { if (asset.category == AssetCategory.METAL) MetalIcon(asset.name, size = 32) else AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))) }
                Spacer(Modifier.width(8.dp)); Column(modifier = Modifier.width(80.dp)) { AutoResizingText(text = asset.symbol.uppercase(), style = TextStyle(color = safeCardText, fontWeight = FontWeight.Black, fontSize = 14.sp)); val totalValue = if (asset.isCustom) asset.currentPrice * (asset.weight * asset.amountHeld) else asset.currentPrice * asset.amountHeld; AutoResizingText(text = formatCurrency(totalValue, asset.decimalPreference), style = TextStyle(color = safeCardText.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)) }
            }
            Box(modifier = Modifier.weight(0.7f).height(24.dp).padding(horizontal = 4.dp)) { SparklineChart(asset.sparklineData, trendColor, Modifier.fillMaxSize()) }
            Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) { AutoResizingText(text = formatCurrency(asset.currentPrice, asset.decimalPreference), style = TextStyle(color = safeCardText, fontWeight = FontWeight.Bold, fontSize = 13.sp)); Row(verticalAlignment = Alignment.CenterVertically) { Text(text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = trendColor, fontSize = 10.sp, fontWeight = FontWeight.Black); Icon(if (asset.priceChange24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = trendColor, modifier = Modifier.size(24.dp).offset(x = 4.dp)) } }
        }
    }
}

// -------------------- UTILS --------------------

fun formatCurrency(value: Double, decimalPreference: Int = 2): String {
    val df = DecimalFormat("$#,##0"); df.minimumFractionDigits = 0; df.maximumFractionDigits = decimalPreference
    if (value > 0 && value < 0.01 && decimalPreference < 4) df.maximumFractionDigits = 6
    return df.format(value)
}
fun formatAmount(value: Double): String = DecimalFormat("#,###.########").format(value)
@Composable
fun MetalIcon(name: String, size: Int = 44) {
    val metalColors = if (name.contains("Gold", true)) listOf(Color(0xFFFFD700), Color(0xFFB8860B)) else if (name.contains("Silver", true)) listOf(Color(0xFFE0E0E0), Color(0xFF757575)) else if (name.contains("Platinum", true)) listOf(Color(0xFFF5F5F5), Color(0xFFBDBDBD)) else listOf(Color(0xFFCED4DA), Color(0xFF495057))
    Box(modifier = Modifier.size(size.dp).clip(RoundedCornerShape(8.dp)).background(Brush.radialGradient(metalColors, center = Offset.Zero)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Toll, contentDescription = null, tint = Color.Black.copy(0.2f), modifier = Modifier.size((size * 0.6).dp)); Canvas(modifier = Modifier.fillMaxSize()) { drawRect(brush = Brush.linearGradient(0.0f to Color.White.copy(alpha = 0.3f), 0.5f to Color.Transparent, 1.0f to Color.Black.copy(alpha = 0.1f))) } }
}
@Composable
fun SparklineChart(sparklineData: List<Double>, changeColor: Color, modifier: Modifier = Modifier) {
    if (sparklineData.size < 2) { Box(modifier); return }
    val minPrice = sparklineData.minOrNull() ?: 0.0; val maxPrice = sparklineData.maxOrNull() ?: 0.0; val priceRange = if ((maxPrice - minPrice) > 0) maxPrice - minPrice else 1.0
    Canvas(modifier) { val points = sparklineData.mapIndexed { index, price -> Offset(index.toFloat() / (sparklineData.size - 1) * size.width, size.height - ((price - minPrice) / priceRange * size.height).toFloat()) }; val linePath = Path().apply { moveTo(points[0].x, points[0].y); for (i in 1 until points.size) { val prev = points[i - 1]; val curr = points[i]; val cp1x = prev.x + (curr.x - prev.x) / 2f; cubicTo(cp1x, prev.y, cp1x, curr.y, curr.x, curr.y) } }; drawPath(path = linePath, color = changeColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)); drawPath(path = linePath.apply { lineTo(size.width, size.height); lineTo(0f, size.height); close() }, brush = Brush.verticalGradient(listOf(changeColor.copy(alpha = 0.2f), Color.Transparent))) }
}