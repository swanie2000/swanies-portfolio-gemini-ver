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
    val scope = rememberCoroutineScope()

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    var localHoldings by remember { mutableStateOf<List<AssetEntity>>(emptyList()) }
    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle(initialValue = false)

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()

    val bgColor = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val textColor = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val cardBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val cardText = Color(cardTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val isDarkTheme = ColorUtils.calculateLuminance(bgColor.toArgb()) < 0.5

    val lazyListState = rememberLazyListState()
    val isDraggingActive = remember { mutableStateOf(false) }
    val isSavingOrder = remember { mutableStateOf(false) }
    val isOverTrash = remember { mutableStateOf(false) }
    val trashBoundsInRoot = remember { mutableStateOf<Rect?>(null) }
    var assetBeingEdited by remember { mutableStateOf<AssetEntity?>(null) }
    var expandedAssetId by remember { mutableStateOf<String?>(null) }
    var showEditButtonId by remember { mutableStateOf<String?>(null) }
    var assetPendingDeletion by remember { mutableStateOf<AssetEntity?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")

    val totalValueFormatted by remember(holdings, selectedTab) {
        derivedStateOf {
            val filteredForTotal = when (selectedTab) {
                1 -> holdings.filter { it.category == AssetCategory.CRYPTO }
                2 -> holdings.filter { it.category == AssetCategory.METAL }
                else -> holdings
            }
            val total = filteredForTotal.sumOf {
                val multiplier = when {
                    it.name.contains("KILO", ignoreCase = true) || it.name.contains("KG", ignoreCase = true) -> 32.1507
                    it.name.contains("GRAM", ignoreCase = true) || it.name.contains(" G", ignoreCase = true) -> 0.0321507
                    it.name.contains("DWT", ignoreCase = true) -> 0.0514426
                    else -> 1.0
                }
                it.currentPrice * multiplier * it.weight * it.amountHeld
            }
            formatCurrency(total, 2)
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    var refreshProgress by remember { mutableFloatStateOf(0f) }
    var showScanFlash by remember { mutableStateOf(false) }
    val scanOffset = remember { Animatable(-1f) }

    LaunchedEffect(holdings) {
        if (!isDraggingActive.value && !isSavingOrder.value && assetBeingEdited == null) {
            localHoldings = holdings
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            showScanFlash = true
            scanOffset.snapTo(-1f)
            scope.launch {
                scanOffset.animateTo(2f, tween(1000, easing = LinearOutSlowInEasing))
                showScanFlash = false
            }
            val startTime = System.currentTimeMillis()
            val duration = 30000L
            while (isRefreshing && (System.currentTimeMillis() - startTime) < duration) {
                refreshProgress = (System.currentTimeMillis() - startTime).toFloat() / duration
                delay(100)
            }
            refreshProgress = 1f; delay(500); isRefreshing = false; refreshProgress = 0f
        }
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val filtered = if (selectedTab == 0) localHoldings
        else localHoldings.filter { it.category == if(selectedTab == 1) AssetCategory.CRYPTO else AssetCategory.METAL }
        val newList = localHoldings.toMutableList()
        val fromIdx = newList.indexOfFirst { it.coinId == filtered[from.index].coinId }
        val toIdx = newList.indexOfFirst { it.coinId == filtered[to.index].coinId }
        if (fromIdx != -1 && toIdx != -1) { newList.add(toIdx, newList.removeAt(fromIdx)); localHoldings = newList }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor).pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: continue
                trashBoundsInRoot.value?.let { bounds -> isOverTrash.value = isDraggingActive.value && bounds.contains(change.position) }
            }
        }
    }) {
        if (showScanFlash) {
            Box(modifier = Modifier.fillMaxSize().zIndex(999f).background(Brush.verticalGradient(0f to Color.Transparent, 0.45f to Color.White.copy(0.15f), 0.5f to Color.White.copy(0.35f), 0.55f to Color.White.copy(0.15f), 1f to Color.Transparent, startY = scanOffset.value * 2000f, endY = (scanOffset.value * 2000f) + 600f)))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().background(bgColor).statusBarsPadding()) {
                IconButton(onClick = { if (!isRefreshing) isRefreshing = true; viewModel.refreshAssets() }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                    Icon(Icons.Default.Refresh, null, tint = if(isRefreshing) textColor.copy(0.2f) else textColor)
                }
                Column(modifier = Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(120.dp))
                    Box(modifier = Modifier.height(12.dp).offset(y = (-40).dp)) {
                        if (isRefreshing) {
                            LinearProgressIndicator(progress = { refreshProgress }, modifier = Modifier.width(140.dp).height(6.dp).clip(CircleShape), color = textColor.copy(0.7f), trackColor = textColor.copy(0.05f))
                        }
                    }
                    Text(text = totalValueFormatted, color = textColor, fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(y = (-25).dp).clickable { navController.navigate(Routes.ANALYTICS) })
                }
                IconButton(onClick = { navController.navigate(Routes.ASSET_PICKER) }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).clip(CircleShape).background(Color.Yellow).size(44.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height((-85).dp))

            TabRow(selectedTabIndex = selectedTab, modifier = Modifier.height(44.dp).padding(horizontal = 20.dp), containerColor = Color.Transparent, indicator = { }, divider = { }) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(selected = isSelected, onClick = { selectedTab = index }, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp).clip(CircleShape).background(if (isSelected) textColor.copy(0.15f) else Color.Transparent).border(1.dp, if (isSelected) Color.Transparent else textColor.copy(0.15f), CircleShape)) {
                        Text(text = title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) textColor else textColor.copy(0.5f), modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }

            LazyColumn(state = lazyListState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val filtered = if (selectedTab == 0) localHoldings else localHoldings.filter { it.category == if(selectedTab == 1) AssetCategory.CRYPTO else AssetCategory.METAL }
                items(items = filtered, key = { it.coinId }) { asset ->
                    ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
                        val isExpanded = expandedAssetId == asset.coinId
                        val isEditButtonVisible = showEditButtonId == asset.coinId
                        val dragModifier = Modifier.longPressDraggableHandle(
                            onDragStarted = { isDraggingActive.value = true },
                            onDragStopped = {
                                isDraggingActive.value = false
                                if (isOverTrash.value) { assetPendingDeletion = asset }
                                else { scope.launch { isDraggingActive.value = false; isSavingOrder.value = true; viewModel.updateAssetOrder(localHoldings); delay(500); isSavingOrder.value = false } }
                            }
                        )

                        if (isCompactViewEnabled && !isExpanded) {
                            CompactAssetCard(asset, isDragging, cardBg, cardText, { expandedAssetId = asset.coinId; showEditButtonId = null }, dragModifier)
                        } else {
                            FullAssetCard(asset, isExpanded, false, isDragging, isEditButtonVisible, cardBg, cardText, { if (isCompactViewEnabled) { if (expandedAssetId == asset.coinId) { if (isEditButtonVisible) { expandedAssetId = null; showEditButtonId = null } else { showEditButtonId = asset.coinId } } else { expandedAssetId = asset.coinId; showEditButtonId = null } } else { showEditButtonId = if (isEditButtonVisible) null else asset.coinId } }, { assetBeingEdited = asset }, { _, _, _, _ -> }, modifier = dragModifier)
                        }
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth().height(40.dp).background(bgColor)) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val icons = listOf(Icons.Default.Home to Routes.HOME, Icons.AutoMirrored.Filled.FormatListBulleted to Routes.HOLDINGS, Icons.Default.PieChart to Routes.ANALYTICS, Icons.Default.Settings to Routes.SETTINGS)
                    icons.forEach { (icon, route) -> IconButton(onClick = { navController.navigate(route) }) { Icon(icon, null, tint = if(currentRoute == route) textColor else textColor.copy(0.3f)) } }
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars).background(bgColor))
        }

        AnimatedVisibility(visible = assetBeingEdited != null, enter = fadeIn() + slideInVertically { -it }, exit = fadeOut() + slideOutVertically { -it }, modifier = Modifier.zIndex(1000f)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)).statusBarsPadding()) {
                assetBeingEdited?.let { asset ->
                    FullAssetCard(
                        asset = asset, isExpanded = true, isEditing = true, isDragging = false, showEditButton = true,
                        cardBg = cardBg, cardText = cardText, onExpandToggle = { }, onEditRequest = { },
                        onSave = { updatedName, amount, weight, decimals ->
                            scope.launch {
                                isSavingOrder.value = true
                                localHoldings = localHoldings.map { if (it.coinId == asset.coinId) it.copy(name = updatedName, amountHeld = amount, weight = weight, decimalPreference = decimals) else it }
                                viewModel.updateAsset(asset, updatedName, amount, weight, decimals)
                                viewModel.updateAssetOrder(localHoldings)
                                delay(500); isSavingOrder.value = false; assetBeingEdited = null; expandedAssetId = null; showEditButtonId = null
                            }
                        },
                        onCancel = { assetBeingEdited = null },
                        modifier = Modifier.padding(16.dp).align(Alignment.TopCenter)
                    )
                }
            }
        }

        AnimatedVisibility(visible = isDraggingActive.value, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 100.dp).onGloballyPositioned { coords -> val pos = coords.positionInRoot(); trashBoundsInRoot.value = Rect(pos.x, pos.y, pos.x + coords.size.width, pos.y + coords.size.height) }) {
            Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(if (isOverTrash.value) Color.Red else Color.DarkGray.copy(0.9f)).border(3.dp, if (isOverTrash.value) Color.White else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(40.dp)) }
        }

        if (assetPendingDeletion != null) {
            AlertDialog(onDismissRequest = { assetPendingDeletion = null }, containerColor = bgColor, title = { Text("Are you sure?", color = textColor, fontWeight = FontWeight.Black) }, text = { Text("Remove ${assetPendingDeletion?.name}?", color = textColor.copy(0.7f)) }, confirmButton = { TextButton(onClick = { val assetToDelete = assetPendingDeletion; if (assetToDelete != null) { viewModel.deleteAsset(assetToDelete) }; assetPendingDeletion = null }) { Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { assetPendingDeletion = null }) { Text("CANCEL", color = textColor) } })
        }
    }
}

@Composable
fun FullAssetCard(asset: AssetEntity, isExpanded: Boolean, isEditing: Boolean, isDragging: Boolean, showEditButton: Boolean, cardBg: Color, cardText: Color, onExpandToggle: () -> Unit, onEditRequest: () -> Unit, onSave: (String, Double, Double, Int) -> Unit, onCancel: () -> Unit = {}, modifier: Modifier = Modifier) {
    val trendColor = if (asset.priceChange24h >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "grabScale")
    val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "grabElevation")

    Card(
        modifier = modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = elevation.toPx(); clip = true; shape = RoundedCornerShape(16.dp) }.clickable(enabled = !isEditing) { onExpandToggle() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (asset.category == AssetCategory.METAL) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(0.9f).height(85.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        MetalIcon(asset.name)
                        Spacer(Modifier.height(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy((-2).dp)) {
                            Text(text = asset.weight.toString(), color = if(isEditing) Color.Yellow else cardText, fontWeight = FontWeight.Black, fontSize = 11.sp, lineHeight = 11.sp)
                            val displayUnit = when {
                                asset.name.contains("KILO", ignoreCase = true) -> "KILO"
                                asset.name.contains("GRAM", ignoreCase = true) -> "GRAM"
                                asset.name.contains("DWT", ignoreCase = true) -> "DWT"
                                else -> "OZ"
                            }
                            Text(text = displayUnit, color = if(isEditing) Color.Yellow.copy(0.6f) else cardText.copy(alpha = 0.6f), fontWeight = FontWeight.Black, fontSize = 9.sp, lineHeight = 9.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUANTITY", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(formatAmount(asset.amountHeld), color = if(isEditing) Color.Yellow else cardText, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Box(modifier = Modifier.height(44.dp), contentAlignment = Alignment.Center) {
                            val forcedName = if (asset.name.contains(" ") && !asset.name.contains("\n")) asset.name.replaceFirst(" ", "\n") else asset.name
                            Text(text = forcedName.uppercase(), color = if(isEditing) Color.Yellow else cardText, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, lineHeight = 13.sp, maxLines = 2)
                        }
                    }
                    Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                        if (showEditButton && !isEditing) IconButton(onClick = { onEditRequest() }, modifier = Modifier.size(40.dp).background(Color.Yellow, CircleShape)) { Icon(Icons.Default.Edit, null, tint = Color.Black) }
                        else {
                            SparklineChart(asset.sparklineData, if(isEditing) Color.Gray.copy(0.3f) else trendColor, Modifier.width(75.dp).height(32.dp))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if(isEditing) Color.Gray.copy(0.05f) else trendColor.copy(alpha = 0.15f)).padding(horizontal = 4.dp)) {
                                Icon(if (asset.priceChange24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = if(isEditing) Color.Gray else trendColor, modifier = Modifier.size(20.dp))
                                Text(text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = if(isEditing) Color.Gray else trendColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) { AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).then(if(isEditing) Modifier.graphicsLayer { alpha = 0.3f } else Modifier)); Spacer(Modifier.height(4.dp)); Text(asset.symbol.uppercase(), color = if(isEditing) cardText.copy(0.2f) else cardText.copy(0.7f), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                    Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUANTITY", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(formatAmount(asset.amountHeld), color = if(isEditing) Color.Yellow else cardText, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Text(text = asset.name.uppercase(), color = if(isEditing) Color.Yellow else cardText, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, lineHeight = 12.sp, maxLines = 2, modifier = Modifier.fillMaxWidth())
                    }
                    Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                        if (showEditButton && !isEditing) IconButton(onClick = { onEditRequest() }, modifier = Modifier.size(40.dp).background(Color.Yellow, CircleShape)) { Icon(Icons.Default.Edit, null, tint = Color.Black) }
                        else {
                            SparklineChart(asset.sparklineData, if(isEditing) Color.Gray.copy(0.3f) else trendColor, Modifier.width(75.dp).height(32.dp))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if(isEditing) Color.Gray.copy(0.05f) else trendColor.copy(alpha = 0.15f)).padding(horizontal = 4.dp)) {
                                Icon(if (asset.priceChange24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = if(isEditing) Color.Gray else trendColor, modifier = Modifier.size(20.dp))
                                Text(text = "${if (asset.priceChange24h >= 0) "+" else ""}${String.format(Locale.US, "%.2f", asset.priceChange24h)}%", color = if(isEditing) Color.Gray else trendColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp)); HorizontalDivider(color = cardText.copy(alpha = 0.05f)); Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val multiplier = when {
                    asset.name.contains("KILO", ignoreCase = true) || asset.name.contains("KG", ignoreCase = true) -> 32.1507
                    asset.name.contains("GRAM", ignoreCase = true) || asset.name.contains(" G", ignoreCase = true) -> 0.0321507
                    asset.name.contains("DWT", ignoreCase = true) -> 0.0514426
                    else -> 1.0
                }
                Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PRICE", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    AutoResizingText(text = formatCurrency(asset.currentPrice, asset.decimalPreference), style = TextStyle(color = if(isEditing) cardText.copy(0.2f) else cardText, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                }
                Column(modifier = Modifier.weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL VALUE", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                    AutoResizingText(text = formatCurrency(asset.currentPrice * multiplier * asset.weight * asset.amountHeld, 2), style = TextStyle(color = if(isEditing) cardText.copy(0.2f) else cardText, fontWeight = FontWeight.Black, fontSize = 17.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                }
            }

            AnimatedVisibility(visible = isEditing) {
                var editAmount by remember(asset) { mutableStateOf(asset.amountHeld.toString()) }
                var editWeight by remember(asset) { mutableStateOf(asset.weight.toString()) }
                var editDecimals by remember(asset) { mutableFloatStateOf(asset.decimalPreference.toFloat()) }
                var editNameState by remember(asset) { mutableStateOf(asset.name) }

                Column(modifier = Modifier.padding(top = 12.dp)) {

                    // --- Logical Variables (Quantity/Weight) now at TOP of edit block ---
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HOLDING QUANTITY:", color = cardText.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(value = editAmount, onValueChange = { editAmount = it }, textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().background(cardText.copy(0.05f), RoundedCornerShape(4.dp)).padding(8.dp))
                        }
                        if (asset.category == AssetCategory.METAL) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("UNIT WEIGHT:", color = cardText.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                BasicTextField(value = editWeight, onValueChange = { editWeight = it }, textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().background(cardText.copy(0.05f), RoundedCornerShape(4.dp)).padding(8.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // --- Descriptive Variables (Name/Precision) now at BOTTOM ---
                    Text("EDIT NAME:", color = cardText.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    BasicTextField(value = editNameState, onValueChange = { editNameState = it }, textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth().background(cardText.copy(0.05f), RoundedCornerShape(4.dp)).padding(8.dp))

                    Spacer(Modifier.height(12.dp))

                    Text("PRICE DECIMAL PREFERENCE: ${editDecimals.toInt()}", color = cardText.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(value = editDecimals, onValueChange = { editDecimals = it }, valueRange = 0f..8f, steps = 7, colors = SliderDefaults.colors(thumbColor = Color.Yellow, activeTrackColor = Color.Yellow, inactiveTrackColor = cardText.copy(0.1f)))

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { onCancel() }, modifier = Modifier.size(36.dp).background(Color.Red, CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        Spacer(Modifier.width(16.dp))
                        IconButton(onClick = { onSave(editNameState.trim(), editAmount.toDoubleOrNull() ?: asset.amountHeld, editWeight.toDoubleOrNull() ?: asset.weight, editDecimals.toInt()) }, modifier = Modifier.size(36.dp).background(Color.Yellow, CircleShape)) { Icon(Icons.Default.Check, null, tint = Color.Black) }
                    }
                }
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
            Box(modifier = Modifier.weight(0.3f)) { if (asset.category == AssetCategory.METAL) MetalIcon(asset.name, size = 32) else AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))) }
            Column(modifier = Modifier.weight(1f)) {
                AutoResizingText(asset.symbol.uppercase(), TextStyle(color = cardText, fontWeight = FontWeight.Black, fontSize = 14.sp));
                val multiplier = when {
                    asset.name.contains("KILO", ignoreCase = true) -> 32.1507
                    asset.name.contains("GRAM", ignoreCase = true) -> 0.03215
                    else -> 1.0
                }
                AutoResizingText(formatCurrency(asset.currentPrice * multiplier * asset.weight * asset.amountHeld, 2), TextStyle(color = cardText.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold))
            }
            SparklineChart(asset.sparklineData, trendColor, Modifier.weight(0.7f).height(24.dp))
        }
    }
}

@Composable
fun MetalIcon(name: String, size: Int = 44) {
    val metalColors = if (name.contains("Gold", true)) listOf(Color(0xFFFFD700), Color(0xFFB8860B))
    else if (name.contains("Silver", true)) listOf(Color(0xFFE0E0E0), Color(0xFF757575))
    else if (name.contains("Platinum", true)) listOf(Color(0xFFF5F5F5), Color(0xFFBDBDBD))
    else if (name.contains("Palladium", true)) listOf(Color(0xFFCED4DA), Color(0xFF8E9196))
    else listOf(Color(0xFFCED4DA), Color(0xFF495057))
    val isBar = name.contains("Bar", ignoreCase = true) || name.contains("Ingot", ignoreCase = true) || name.contains("KILO", ignoreCase = true) || name.contains("GRAM", ignoreCase = true)
    Box(modifier = Modifier.size(size.dp).clip(if (isBar) RoundedCornerShape(4.dp) else CircleShape).background(Brush.radialGradient(metalColors, center = Offset.Zero)).border(1.dp, Color.White.copy(0.2f), if (isBar) RoundedCornerShape(4.dp) else CircleShape), contentAlignment = Alignment.Center) {
        Icon(imageVector = if (isBar) Icons.Default.ViewAgenda else Icons.Default.Toll, contentDescription = null, tint = Color.Black.copy(0.2f), modifier = Modifier.size((size * 0.6).dp))
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
    var fontSizeValue by remember { mutableStateOf(style.fontSize) }
    var readyToDraw by remember { mutableStateOf(false) }
    Text(
        text = text, style = style.copy(fontSize = fontSizeValue),
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        maxLines = maxLines, softWrap = false, overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && fontSizeValue > 8.sp) { fontSizeValue *= 0.95f }
            else { readyToDraw = true }
        }
    )
}

fun formatCurrency(v: Double, d: Int = 2): String {
    val df = DecimalFormat("$#,##0.00")
    if (d != 2) {
        df.minimumFractionDigits = 0
        df.maximumFractionDigits = d
    }
    return df.format(v)
}
fun formatAmount(v: Double): String = DecimalFormat("#,###.########").format(v)