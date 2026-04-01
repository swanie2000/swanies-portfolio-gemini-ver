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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlin.math.absoluteValue

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
    val siteBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())

    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")
    val isViewModelRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var assetBeingEdited by remember { mutableStateOf<AssetEntity?>(null) }
    var expandedAssetId by remember { mutableStateOf<String?>(null) }
    var assetPendingDeletion by remember { mutableStateOf<AssetEntity?>(null) }
    val trashBoundsInRoot = remember { mutableStateOf<Rect?>(null) }
    val isDraggingActive = remember { mutableStateOf(false) }
    val isOverTrash = remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    // --- 🛡️ CURTAIN LOGIC ---
    var isUnlocked by remember { mutableStateOf(false) }
    val curtainAlpha = remember { Animatable(1f) }

    LaunchedEffect(activeVault.id) {
        isUnlocked = false
        curtainAlpha.snapTo(1f)
        delay(300)
        isUnlocked = true
        curtainAlpha.animateTo(0f, tween(800))
    }

    // --- PAGER LOGIC ---
    val pagerState = rememberPagerState(
        initialPage = allVaults.indexOfFirst { it.id == activeVault.id }.coerceAtLeast(0)
    ) { allVaults.size.coerceAtLeast(1) }

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
        newList.add(to.index, newList.removeAt(from.index))
        localHoldings = newList
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isDark = ColorUtils.calculateLuminance(siteBg.toArgb()) < 0.5
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
    }

    Box(modifier = Modifier.fillMaxSize().background(siteBg).pointerInput(Unit) {
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
                // Header
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(100.dp).zIndex(10f)) {
                    Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.Center))
                    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (!isViewModelRefreshing) viewModel.refreshAssets() }) {
                            Icon(Icons.Default.Refresh, null, tint = textColor)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { mainViewModel.toggleCompactView() }) {
                            Icon(if (isCompactViewEnabled) Icons.Default.ViewModule else Icons.AutoMirrored.Filled.ViewList, null, tint = textColor)
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
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    val contentAlpha = (1f - (pageOffset * 1.2f).coerceIn(0f, 1f)).pow(2f)

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Portfolio Header
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().clickable { isExiting = true; navController.navigate(Routes.PORTFOLIO_MANAGER) }.padding(vertical = 8.dp).graphicsLayer { alpha = contentAlpha },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = vaultForPage.name.uppercase(), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            if (isCurrentPage && isUnlocked) {
                                Text(text = totalValueFormatted, color = textColor.copy(alpha = 0.8f), fontSize = 32.sp, fontWeight = FontWeight.Black, modifier = Modifier.graphicsLayer { alpha = (1f - curtainAlpha.value).coerceIn(0f, 1f) })
                            } else {
                                Spacer(modifier = Modifier.height(38.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                            if (isCurrentPage && isUnlocked) {
                                Column(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = (1f - curtainAlpha.value).coerceIn(0f, 1f) * contentAlpha }) {

                                    // --- 🛡️ THE ORIGINAL TABS + SHIELD LAYOUT ---
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TabRow(
                                            selectedTabIndex = selectedTab,
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            containerColor = Color.Transparent,
                                            indicator = { },
                                            divider = { }
                                        ) {
                                            tabs.forEachIndexed { index, title ->
                                                val isSelected = selectedTab == index
                                                // Shrink METAL tab to make room for the Shield
                                                val tabWeight = if (index == 2 && isSelected) 0.6f else 1f

                                                Row(
                                                    modifier = Modifier
                                                        .weight(tabWeight)
                                                        .padding(horizontal = 4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) textColor.copy(0.15f) else Color.Transparent)
                                                        .border(1.dp, if (isSelected) Color.Transparent else textColor.copy(0.15f), CircleShape)
                                                        .clickable { selectedTab = index }
                                                        .padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) textColor else textColor.copy(0.5f))
                                                }

                                                // THE ORIGINAL GOLD SHIELD
                                                if (index == 2 && isSelected) {
                                                    IconButton(
                                                        onClick = { isExiting = true; navController.navigate(Routes.METALS_AUDIT) },
                                                        modifier = Modifier
                                                            .padding(start = 8.dp)
                                                            .size(34.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Yellow) // Original Solid Gold color
                                                    ) {
                                                        Icon(Icons.Default.Shield, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (isViewModelRefreshing) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp).padding(horizontal = 24.dp), color = textColor, trackColor = textColor.copy(0.1f))
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
                                                val dragModifier = Modifier.longPressDraggableHandle(
                                                    onDragStarted = { isDraggingActive.value = true; haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                                    onDragStopped = {
                                                        if (isOverTrash.value) { assetPendingDeletion = asset }
                                                        isDraggingActive.value = false
                                                        scope.launch { viewModel.updateAssetOrder(localHoldings) }
                                                    }
                                                )
                                                if (isCompactViewEnabled && !isExpanded) CompactAssetCard(asset, isDragging, cardBg, cardText, activeVault.baseCurrency, { expandedAssetId = if (expandedAssetId == asset.coinId) null else asset.coinId }, dragModifier)
                                                else FullAssetCard(asset, isExpanded, false, isDragging, isExpanded, cardBg, cardText, activeVault.baseCurrency, { expandedAssetId = if (expandedAssetId == asset.coinId) null else asset.coinId }, { assetBeingEdited = asset }, { _, _, _, _, _ -> }, modifier = dragModifier)
                                            }
                                        }
                                    }
                                }
                            }

                            if (isCurrentPage && (!isUnlocked || curtainAlpha.value > 0.01f)) {
                                Box(modifier = Modifier.fillMaxSize().zIndex(20f).background(siteBg).graphicsLayer { alpha = curtainAlpha.value })
                            }
                        }
                    }
                }
                BottomNavigationBar(navController = navController, onNavigate = { isExiting = true })
            }
        }

        // Action Overlays (Trash, Delete Dialog, etc.)
        AnimatedVisibility(
            visible = isDraggingActive.value && !isExiting,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 120.dp).zIndex(100f).onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                trashBoundsInRoot.value = Rect(pos.x, pos.y, pos.x + coords.size.width, pos.y + coords.size.height)
            }
        ) {
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

fun Float.pow(x: Float) = Math.pow(this.toDouble(), x.toDouble()).toFloat()