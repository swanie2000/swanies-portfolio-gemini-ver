@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.holdings

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.components.*
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun MyHoldingsScreen(
    mainViewModel: MainViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    var localHoldings by remember { mutableStateOf<List<AssetEntity>>(emptyList()) }
    val allVaults by mainViewModel.allVaults.collectAsStateWithLifecycle()
    val activeVault by mainViewModel.activeVault.collectAsStateWithLifecycle()

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()

    val textColor = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val cardBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val cardText = Color(cardTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle()
    val confirmDeleteSetting by mainViewModel.confirmDelete.collectAsStateWithLifecycle(initialValue = true)

    val lazyListState = rememberLazyListState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")
    val isViewModelRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var assetBeingEdited by remember { mutableStateOf<AssetEntity?>(null) }
    var expandedAssetId by remember { mutableStateOf<String?>(null) }
    var showEditButtonId by remember { mutableStateOf<String?>(null) }
    var assetPendingDeletion by remember { mutableStateOf<AssetEntity?>(null) }
    val trashBoundsInRoot = remember { mutableStateOf<Rect?>(null) }

    val isDraggingActive = remember { mutableStateOf(false) }
    val isOverTrash = remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    // --- FINITE PAGER LOGIC ---
    val pagerState = rememberPagerState(
        initialPage = allVaults.indexOfFirst { it.id == activeVault.id }.coerceAtLeast(0)
    ) { allVaults.size.coerceAtLeast(1) }

    LaunchedEffect(activeVault.id) {
        val targetIndex = allVaults.indexOfFirst { it.id == activeVault.id }
        if (targetIndex != -1 && targetIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress && allVaults.isNotEmpty()) {
            val targetVault = allVaults[pagerState.currentPage]
            if (targetVault.id != activeVault.id) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                mainViewModel.selectVault(targetVault.id)
            }
        }
    }

    val totalValueFormatted by remember(holdings, activeVault.baseCurrency) {
        derivedStateOf {
            val total = holdings.sumOf { (it.officialSpotPrice * it.weight * it.amountHeld) + it.premium }
            formatCurrency(total, 2, activeVault.baseCurrency)
        }
    }

    LaunchedEffect(holdings) {
        if (!isDraggingActive.value && assetBeingEdited == null) localHoldings = holdings
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val newList = localHoldings.toMutableList()
        val indexFrom = newList.indexOfFirst { it.coinId == localHoldings.getOrNull(from.index)?.coinId }
        val indexTo = newList.indexOfFirst { it.coinId == localHoldings.getOrNull(to.index)?.coinId }
        if (indexFrom != -1 && indexTo != -1) {
            newList.add(indexTo, newList.removeAt(indexFrom))
            localHoldings = newList
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isDark = ColorUtils.calculateLuminance(Color(siteBgColor.ifBlank { "#000416" }.toColorInt()).toArgb()) < 0.5
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: continue
                trashBoundsInRoot.value?.let { bounds ->
                    isOverTrash.value = isDraggingActive.value && bounds.contains(change.position)
                }
            }
        }
    }) {
        if (!isExiting) {
            Column(modifier = Modifier.fillMaxSize()) {
                // RESTORED HEADER WITH COMPACT TOGGLE
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(100.dp)) {
                    Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.Center))
                    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (!isViewModelRefreshing) viewModel.refreshAssets() }) {
                            Icon(Icons.Default.Refresh, null, tint = textColor)
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        // 🛠️ RESTORED ICON: Compact/Full Toggle
                        IconButton(onClick = { mainViewModel.toggleCompactView() }) {
                            Icon(
                                if (isCompactViewEnabled) Icons.Default.ViewModule else Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = "Toggle View",
                                tint = textColor
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { isExiting = true; navController.navigate(Routes.ASSET_PICKER) }, modifier = Modifier.clip(CircleShape).background(Color.Yellow).size(44.dp)) {
                            Icon(Icons.Default.Add, null, tint = Color.Black)
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    userScrollEnabled = !isDraggingActive.value
                ) { page ->
                    val vaultForPage = allVaults.getOrNull(page) ?: activeVault
                    val isCurrentPage = vaultForPage.id == activeVault.id

                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).border(1.dp, cardText.copy(0.1f), RoundedCornerShape(16.dp))
                                .clickable { isExiting = true; navController.navigate(Routes.PORTFOLIO_MANAGER) }.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = vaultForPage.name.uppercase(), color = cardText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text(text = if (isCurrentPage) totalValueFormatted else "---", color = cardText.copy(alpha = 0.7f), fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isCurrentPage) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                                TabRow(selectedTabIndex = selectedTab, modifier = Modifier.weight(1f).height(40.dp), containerColor = Color.Transparent, indicator = { }, divider = { }) {
                                    tabs.forEachIndexed { index, title ->
                                        val isSelected = selectedTab == index
                                        Tab(selected = isSelected, onClick = { selectedTab = index }, modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(if (isSelected) textColor.copy(0.15f) else Color.Transparent).border(1.dp, if (isSelected) Color.Transparent else textColor.copy(0.15f), CircleShape)) {
                                            Text(text = title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) textColor else textColor.copy(0.5f), modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }

                            LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val currentHoldings = when (selectedTab) {
                                    1 -> localHoldings.filter { it.category == AssetCategory.CRYPTO }
                                    2 -> localHoldings.filter { it.category == AssetCategory.METAL }
                                    else -> localHoldings
                                }
                                items(items = currentHoldings, key = { it.coinId }) { asset ->
                                    ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
                                        val isExpanded = expandedAssetId == asset.coinId
                                        val isEditButtonVisible = showEditButtonId == asset.coinId

                                        val handleExpandToggle = {
                                            if (isCompactViewEnabled) {
                                                if (expandedAssetId != asset.coinId) { expandedAssetId = asset.coinId; showEditButtonId = null }
                                                else if (showEditButtonId != asset.coinId) { showEditButtonId = asset.coinId }
                                                else { expandedAssetId = null; showEditButtonId = null }
                                            } else {
                                                if (showEditButtonId != asset.coinId) { showEditButtonId = asset.coinId; expandedAssetId = asset.coinId }
                                                else { showEditButtonId = null; expandedAssetId = null }
                                            }
                                        }

                                        val dragModifier = Modifier.longPressDraggableHandle(
                                            onDragStarted = { isDraggingActive.value = true; haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                            onDragStopped = {
                                                isDraggingActive.value = false
                                                if (isOverTrash.value) { if (confirmDeleteSetting) assetPendingDeletion = asset else viewModel.deleteAsset(asset) }
                                                else { scope.launch { viewModel.updateAssetOrder(localHoldings) } }
                                            }
                                        )

                                        if (isCompactViewEnabled && !isExpanded) {
                                            CompactAssetCard(asset, isDragging, cardBg, cardText, activeVault.baseCurrency, handleExpandToggle, dragModifier)
                                        } else {
                                            FullAssetCard(
                                                asset = asset,
                                                isExpanded = isExpanded,
                                                isEditing = false,
                                                isDragging = isDragging,
                                                showEditButton = isEditButtonVisible,
                                                cardBg = cardBg,
                                                cardText = cardText,
                                                baseCurrency = activeVault.baseCurrency,
                                                onExpandToggle = handleExpandToggle,
                                                onEditRequest = { assetBeingEdited = asset },
                                                onSave = { _, _, _, _, _ -> },
                                                modifier = dragModifier
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = textColor.copy(0.1f))
                            }
                        }
                    }
                }
                BottomNavigationBar(navController = navController, onNavigate = { isExiting = true })
            }
        }

        AnimatedVisibility(visible = isDraggingActive.value && !isExiting, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 120.dp).onGloballyPositioned { coords -> val pos = coords.positionInRoot(); trashBoundsInRoot.value = Rect(pos.x, pos.y, pos.x + coords.size.width, pos.y + coords.size.height) }) {
            Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(if (isOverTrash.value) Color.Red else Color.DarkGray.copy(0.9f)).border(3.dp, if (isOverTrash.value) Color.White else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }

        assetBeingEdited?.let { asset ->
            if (asset.category == AssetCategory.CRYPTO) {
                CryptoEditFunnel(asset = asset, onDismiss = { assetBeingEdited = null }, onSave = { newName, newAmount, newDecimals -> viewModel.updateAsset(asset, newName, newAmount, asset.weight, asset.weightUnit, newDecimals); assetBeingEdited = null })
            } else {
                MetalSelectionFunnel(initialMetal = asset.symbol, initialForm = asset.name, initialWeight = asset.weight, initialQty = asset.amountHeld.toString(), initialPrem = asset.premium.toString(), initialManualPrice = asset.officialSpotPrice.toString(), onDismiss = { assetBeingEdited = null }, onConfirmed = { type, desc, weight, unit, qty, prem, icon, isManual, manualPrice -> viewModel.updateAssetEntity(asset.copy(symbol = type, name = desc, weight = weight, weightUnit = unit, amountHeld = qty.toDoubleOrNull() ?: 0.0, premium = prem.toDoubleOrNull() ?: 0.0, imageUrl = icon ?: asset.imageUrl, officialSpotPrice = if(isManual) (manualPrice.toDoubleOrNull() ?: 0.0) else asset.officialSpotPrice)); assetBeingEdited = null })
            }
        }

        assetPendingDeletion?.let { asset ->
            AlertDialog(onDismissRequest = { assetPendingDeletion = null }, containerColor = Color(0xFF1A1A1A), title = { Text("PURGE ASSET?", color = Color.Red, fontWeight = FontWeight.Black) }, text = { Text("Permanently remove ${asset.displayName.ifEmpty { asset.name }}?") }, confirmButton = { TextButton(onClick = { viewModel.deleteAsset(asset); assetPendingDeletion = null }) { Text("DELETE", color = Color.Red) } }, dismissButton = { TextButton(onClick = { assetPendingDeletion = null }) { Text("CANCEL", color = Color.White) } })
        }
    }
}