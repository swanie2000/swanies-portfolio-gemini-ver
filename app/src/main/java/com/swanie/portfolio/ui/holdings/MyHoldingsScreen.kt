@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.holdings

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerEventPass
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
    val confirmDeleteSetting by mainViewModel.confirmDelete.collectAsStateWithLifecycle(initialValue = true)

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

    // Filtered list used for display and reordering logic
    val filteredHoldings by remember(localHoldings, selectedTab) {
        derivedStateOf {
            when (selectedTab) {
                1 -> localHoldings.filter { it.category == AssetCategory.CRYPTO }
                2 -> localHoldings.filter { it.category == AssetCategory.METAL }
                else -> localHoldings
            }
        }
    }

    val totalValueFormatted by remember(holdings, selectedTab) {
        derivedStateOf {
            val filtered = when (selectedTab) {
                1 -> holdings.filter { it.category == AssetCategory.CRYPTO }
                2 -> holdings.filter { it.category == AssetCategory.METAL }
                else -> holdings
            }
            val total = filtered.sumOf { asset ->
                val multiplier = when {
                    asset.name.contains("KILO", ignoreCase = true) -> 32.1507
                    asset.name.contains("GRAM", ignoreCase = true) -> 0.0321507
                    else -> 1.0
                }
                (asset.currentPrice * multiplier * asset.weight * asset.amountHeld) + asset.premium
            }
            NumberFormat.getCurrencyInstance(Locale.US).format(total)
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    var refreshProgress by remember { mutableFloatStateOf(0f) }
    var showScanFlash by remember { mutableStateOf(false) }
    val scanOffset = remember { Animatable(-1f) }

    LaunchedEffect(holdings) {
        // Only sync from DB if we are NOT in the middle of a drag or a save operation
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
            while (isRefreshing && (System.currentTimeMillis() - startTime) < 30000L) {
                refreshProgress = (System.currentTimeMillis() - startTime).toFloat() / 30000L
                delay(100)
            }
            refreshProgress = 1f; delay(500); isRefreshing = false; refreshProgress = 0f
        }
    }

    // FIXED: Corrected reorderable logic to handle filtered list mapping
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val currentFiltered = filteredHoldings
        val itemFrom = currentFiltered[from.index]
        val itemTo = currentFiltered[to.index]

        val newList = localHoldings.toMutableList()
        val indexFrom = newList.indexOfFirst { it.coinId == itemFrom.coinId }
        val indexTo = newList.indexOfFirst { it.coinId == itemTo.coinId }

        if (indexFrom != -1 && indexTo != -1) {
            newList.add(indexTo, newList.removeAt(indexFrom))
            localHoldings = newList
        }
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

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                TabRow(selectedTabIndex = selectedTab, modifier = Modifier.weight(1f).height(48.dp), containerColor = Color.Transparent, indicator = { }, divider = { }) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Tab(selected = isSelected, onClick = { selectedTab = index }, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp).clip(CircleShape).background(if (isSelected) textColor.copy(0.15f) else Color.Transparent).border(1.dp, if (isSelected) Color.Transparent else textColor.copy(0.15f), CircleShape)) {
                            Text(text = title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) textColor else textColor.copy(0.5f), modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }

                AnimatedVisibility(visible = selectedTab == 2) {
                    IconButton(onClick = { navController.navigate(Routes.METALS_AUDIT) }) {
                        Icon(Icons.Default.Shield, contentDescription = "Audit Info", tint = Color.Yellow)
                    }
                }
            }

            LazyColumn(state = lazyListState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = filteredHoldings, key = { it.coinId }) { asset ->
                    ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
                        val isExpanded = expandedAssetId == asset.coinId
                        val isEditButtonVisible = showEditButtonId == asset.coinId
                        val dragModifier = Modifier.longPressDraggableHandle(
                            onDragStarted = { isDraggingActive.value = true },
                            onDragStopped = {
                                isDraggingActive.value = false
                                if (isOverTrash.value) {
                                    if (confirmDeleteSetting) assetPendingDeletion = asset else viewModel.deleteAsset(asset)
                                } else {
                                    // FIXED: Robust save logic with extended suppression of DB sync
                                    scope.launch {
                                        isSavingOrder.value = true
                                        viewModel.updateAssetOrder(localHoldings)
                                        delay(800) // Give the DB and LazyColumn time to settle
                                        isSavingOrder.value = false
                                    }
                                }
                            }
                        )

                        if (isCompactViewEnabled && !isExpanded) {
                            CompactAssetCard(asset, isDragging, cardBg, cardText, { expandedAssetId = asset.coinId; showEditButtonId = null }, dragModifier)
                        } else {
                            FullAssetCard(asset, isExpanded, false, isDragging, isEditButtonVisible, cardBg, cardText, {
                                if (isEditButtonVisible) { showEditButtonId = null } else { showEditButtonId = asset.coinId }
                                expandedAssetId = if (isExpanded) null else asset.coinId
                            }, { assetBeingEdited = asset }, { _, _, _, _ -> }, modifier = dragModifier)
                        }
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth().height(56.dp).background(bgColor)) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val icons = listOf(Icons.Default.Home to Routes.HOME, Icons.AutoMirrored.Filled.FormatListBulleted to Routes.HOLDINGS, Icons.Default.PieChart to Routes.ANALYTICS, Icons.Default.Settings to Routes.SETTINGS)
                    icons.forEach { (icon, route) ->
                        IconButton(onClick = { navController.navigate(route) }) {
                            Icon(icon, null, tint = if(currentRoute == route) textColor else textColor.copy(0.3f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars).background(bgColor))
        }

        AnimatedVisibility(visible = isDraggingActive.value, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 120.dp).onGloballyPositioned { coords -> val pos = coords.positionInRoot(); trashBoundsInRoot.value = Rect(pos.x, pos.y, pos.x + coords.size.width, pos.y + coords.size.height) }) {
            Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(if (isOverTrash.value) Color.Red else Color.DarkGray.copy(0.9f)).border(3.dp, if (isOverTrash.value) Color.White else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }

        if (assetBeingEdited != null) {
            val asset = assetBeingEdited!!
            if (asset.category == AssetCategory.METAL) {
                MetalSelectionFunnel(
                    initialMetal = if(asset.baseSymbol == "CUSTOM") asset.symbol else (asset.name.split("\n").getOrNull(0) ?: "Silver"),
                    initialForm = asset.name, initialWeight = asset.weight, initialQty = asset.amountHeld.toString(),
                    initialPrem = asset.premium.toString(), initialManualPrice = if(asset.baseSymbol == "CUSTOM") asset.currentPrice.toString() else "0.0",
                    onDismiss = { assetBeingEdited = null },
                    onConfirmed = { fMetal, fForm, fWeight, isKilo, fQty, fPrem, iconUri, isTrueCust, manualPrice ->
                        scope.launch {
                            val finalName = if(isTrueCust) fForm else "${fMetal.uppercase()}\n${if (isKilo) "KILO" else fForm.uppercase()}"
                            viewModel.updateAsset(asset, finalName, fQty.toDoubleOrNull() ?: asset.amountHeld, fWeight, asset.decimalPreference)
                            assetBeingEdited = null; expandedAssetId = null; showEditButtonId = null
                        }
                    }
                )
            } else {
                CryptoEditFunnel(asset = asset, onDismiss = { assetBeingEdited = null }, onSave = { updatedName, amount, decimals ->
                    scope.launch { viewModel.updateAsset(asset, updatedName, amount, asset.weight, decimals); assetBeingEdited = null; expandedAssetId = null; showEditButtonId = null }
                })
            }
        }

        if (assetPendingDeletion != null) {
            AlertDialog(onDismissRequest = { assetPendingDeletion = null }, containerColor = cardBg, titleContentColor = cardText, textContentColor = cardText.copy(alpha = 0.7f), title = { Text("Are you sure?", fontWeight = FontWeight.Black) }, text = { Text("Remove ${assetPendingDeletion?.name?.replace("\n", " ")}?") }, confirmButton = { TextButton(onClick = { assetPendingDeletion?.let { viewModel.deleteAsset(it) }; assetPendingDeletion = null }) { Text("DELETE", color = Color.Red, fontWeight = FontWeight.Black) } }, dismissButton = { TextButton(onClick = { assetPendingDeletion = null }) { Text("CANCEL", color = cardText.copy(alpha = 0.5f)) } })
        }
    }
}