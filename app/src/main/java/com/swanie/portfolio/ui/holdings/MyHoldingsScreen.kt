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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.holdings.formatAmount
import com.swanie.portfolio.ui.holdings.formatCurrency
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
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
    val haptic = LocalHapticFeedback.current

    val allVaults by viewModel.allVaults.collectAsStateWithLifecycle(initialValue = null)
    val activeVault by mainViewModel.activeVault.collectAsStateWithLifecycle()

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by themeViewModel.cardTextColor.collectAsState()

    val textColor = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val siteBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val nightVaultColor = lerp(siteBg, Color.Black, 0.25f)

    val isCompactViewEnabled by mainViewModel.isCompactViewEnabled.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")

    val isViewModelRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle(initialValue = false)
    var assetBeingEdited by remember { mutableStateOf<AssetEntity?>(null) }
    var expandedAssetId by remember { mutableStateOf<String?>(null) }
    var editingAssetId by remember { mutableStateOf<String?>(null) }
    val trashBoundsInRoot = remember { mutableStateOf<Rect?>(null) }
    val isDraggingActive = remember { mutableStateOf(false) }
    val isOverTrash = remember { mutableStateOf(false) }
    var draggingVaultId by remember { mutableIntStateOf(-1) }
    var isExiting by remember { mutableStateOf(false) }

    // 🛡️ DELETION SHIELD STATE
    val confirmDeleteEnabled by mainViewModel.confirmDelete.collectAsStateWithLifecycle()
    val assetToDelete by viewModel.confirmDelete.collectAsStateWithLifecycle()

    val resolvedVaults = allVaults ?: emptyList()
    val pagerState = rememberPagerState(
        initialPage = resolvedVaults.indexOfFirst { it.id == activeVault.id }.coerceAtLeast(0)
    ) { resolvedVaults.size.coerceAtLeast(1) }

    LaunchedEffect(activeVault.id) {
        val targetIndex = resolvedVaults.indexOfFirst { it.id == activeVault.id }
        if (targetIndex != -1 && targetIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress && resolvedVaults.isNotEmpty()) {
            val targetVault = resolvedVaults[pagerState.currentPage]
            if (targetVault.id != activeVault.id) mainViewModel.selectVault(targetVault.id)
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isDark = ColorUtils.calculateLuminance(siteBg.toArgb()) < 0.5
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
    }

    val curtainAlpha by animateFloatAsState(
        targetValue = if (allVaults != null) 0f else 1f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "curtain"
    )

    Box(modifier = Modifier.fillMaxSize().background(siteBg).pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: continue
                trashBoundsInRoot.value?.let { b -> isOverTrash.value = isDraggingActive.value && b.contains(change.position) }
            }
        }
    }) {
        Box(modifier = Modifier.fillMaxSize()) {
        if (!isExiting) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Fixed Header Swan Logo
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(80.dp).zIndex(10f)) {
                    Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(70.dp).align(Alignment.Center))
                    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (!isViewModelRefreshing) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.refreshAssets()
                                }
                            },
                        ) { Icon(Icons.Default.Refresh, "Refresh", tint = textColor) }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { mainViewModel.toggleCompactView() }) { Icon(if (isCompactViewEnabled) Icons.Default.ViewModule else Icons.AutoMirrored.Filled.ViewList, null, tint = textColor) }
                        IconButton(onClick = { isExiting = true; navController.navigate(Routes.ASSET_PICKER) }, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Yellow)) { Icon(Icons.Default.Add, null, tint = Color.Black) }
                    }
                }

                // Unified pager body: each page owns identity + list for its vault.
                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    if (allVaults != null && resolvedVaults.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Welcome to Swanies Portfolio",
                                    color = textColor,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        isExiting = true
                                        navController.navigate(Routes.PORTFOLIO_MANAGER)
                                    }
                                ) {
                                    Text("Create Your First Portfolio")
                                }
                            }
                        }
                    } else if (resolvedVaults.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            beyondViewportPageCount = 1,
                            userScrollEnabled = !isDraggingActive.value,
                            key = { page -> resolvedVaults[page].id }
                        ) { page ->
                            val vaultForPage = resolvedVaults[page]
                            val pageOffset = pagerState.getOffsetDistanceInPages(page)
                            val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                            val rawAlpha = (1f - absOffset) * (1f - absOffset)
                            val pageAlpha = 0.1f + (rawAlpha * 0.9f)
                            val deckScale = 0.85f + (1f - 0.85f) * (1f - absOffset)
                            val holdingsForPage by viewModel.getHoldingsForVault(vaultForPage.id)
                                .collectAsStateWithLifecycle(initialValue = emptyList())
                            var localHoldingsForPage by remember(vaultForPage.id) { mutableStateOf(emptyList<AssetEntity>()) }
                            val pageLazyListState = rememberLazyListState()

                            LaunchedEffect(holdingsForPage, vaultForPage.id, assetBeingEdited) {
                                val isDraggingThisPage = isDraggingActive.value && draggingVaultId == vaultForPage.id
                                if (!isDraggingThisPage && assetBeingEdited == null) {
                                    localHoldingsForPage = holdingsForPage
                                }
                            }

                            val totalValueFormatted = remember(localHoldingsForPage, vaultForPage.baseCurrency) {
                                val total = localHoldingsForPage.sumOf {
                                    (it.officialSpotPrice * it.weight * it.amountHeld) + it.premium
                                }
                                formatCurrency(total, 2, vaultForPage.baseCurrency)
                            }

                            val filteredHoldingsForPage = remember(localHoldingsForPage, selectedTab) {
                                when (selectedTab) {
                                    1 -> localHoldingsForPage.filter { it.category == AssetCategory.CRYPTO }
                                    2 -> localHoldingsForPage.filter { it.category == AssetCategory.METAL }
                                    else -> localHoldingsForPage
                                }
                            }

                            val reorderableLazyListState = rememberReorderableLazyListState(
                                lazyListState = pageLazyListState,
                                onMove = { from, to ->
                                    val fromItem = filteredHoldingsForPage[from.index]
                                    val toItem = filteredHoldingsForPage[to.index]
                                    localHoldingsForPage = localHoldingsForPage.toMutableList().apply {
                                        val fromIdx = indexOfFirst { it.coinId == fromItem.coinId }
                                        val toIdx = indexOfFirst { it.coinId == toItem.coinId }
                                        if (fromIdx != -1 && toIdx != -1) add(toIdx, removeAt(fromIdx))
                                    }
                                }
                            )

                            Column(modifier = Modifier.fillMaxSize().alpha(pageAlpha)) {
                                Box(modifier = Modifier.fillMaxWidth().height(140.dp).zIndex(5f).padding(horizontal = 24.dp)) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(110.dp).align(Alignment.Center).graphicsLayer {
                                            scaleX = deckScale
                                            scaleY = deckScale
                                        }.clip(RoundedCornerShape(12.dp)).background(nightVaultColor).border(1.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).clickable { isExiting = true; navController.navigate(Routes.PORTFOLIO_MANAGER) }
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                                                Text(
                                                    text = vaultForPage.name.uppercase(),
                                                    color = textColor,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 2.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                                                AutoResizingText(
                                                    text = totalValueFormatted,
                                                    style = TextStyle(
                                                        color = textColor.copy(0.7f),
                                                        fontSize = with(LocalDensity.current) { (20.sp.toPx() / fontScale.coerceAtMost(1.15f)).toSp() },
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    tabs.forEachIndexed { i, t ->
                                        val sel = selectedTab == i
                                        Box(modifier = Modifier.weight(1f).height(40.dp).padding(horizontal = 4.dp).clip(CircleShape).background(if (sel) textColor.copy(0.15f) else Color.Transparent).border(1.dp, textColor.copy(if (sel) 0.3f else 0.1f), CircleShape).clickable { selectedTab = i }, contentAlignment = Alignment.Center) {
                                            Text(t, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (sel) textColor else textColor.copy(0.5f))
                                        }
                                        if (t == "METAL" && sel) { IconButton(onClick = { isExiting = true; navController.navigate(Routes.METALS_AUDIT) }) { Icon(Icons.Default.Security, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp)) } }
                                    }
                                }

                                if (isViewModelRefreshing && vaultForPage.id == activeVault.id) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(2.dp),
                                        color = Color.Yellow,
                                        trackColor = Color.Transparent
                                    )
                                }

                                LazyColumn(
                                    state = pageLazyListState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(items = filteredHoldingsForPage, key = { it.coinId }) { asset ->
                                        ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
                                            val hndl = Modifier.longPressDraggableHandle(
                                                onDragStarted = {
                                                    draggingVaultId = vaultForPage.id
                                                    isDraggingActive.value = true
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                },
                                                onDragStopped = {
                                                    if (isOverTrash.value) {
                                                        if (confirmDeleteEnabled) {
                                                            viewModel.requestDeleteConfirmation(asset)
                                                        } else {
                                                            viewModel.deleteAsset(asset)
                                                        }
                                                    }
                                                    isDraggingActive.value = false
                                                    draggingVaultId = -1
                                                    viewModel.updateAssetOrder(localHoldingsForPage)
                                                }
                                            )
                                            val cardBgCol = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
                                            val cardTextCol = Color(cardTextColor.ifBlank { "#FFFFFF" }.toColorInt())

                                            val isExpanded = expandedAssetId == asset.coinId
                                            val showEdit = editingAssetId == asset.coinId

                                            if (isCompactViewEnabled) {
                                                CompactAssetCard(
                                                    asset = asset, isDragging = isDragging, cardBg = cardBgCol, cardText = cardTextCol, baseCurrency = vaultForPage.baseCurrency,
                                                    onExpandToggle = {
                                                        if (expandedAssetId != asset.coinId) { expandedAssetId = asset.coinId; editingAssetId = null }
                                                        else if (editingAssetId != asset.coinId) { editingAssetId = asset.coinId }
                                                        else { expandedAssetId = null; editingAssetId = null }
                                                    },
                                                    onEditRequest = { assetBeingEdited = asset },
                                                    modifier = hndl, isExpanded = isExpanded, showEditButton = showEdit
                                                )
                                            } else {
                                                FullAssetCard(
                                                    asset = asset, isExpanded = true, isEditing = false, isDragging = isDragging, cardBg = cardBgCol, cardText = cardTextCol, baseCurrency = vaultForPage.baseCurrency,
                                                    onExpandToggle = {
                                                        if (editingAssetId != asset.coinId) editingAssetId = asset.coinId else editingAssetId = null
                                                    },
                                                    onEditRequest = { assetBeingEdited = asset },
                                                    onSave = { newName, newAmount, newWeight, weightUnit, decimals ->
                                                        viewModel.updateAsset(asset, newName, newAmount, newWeight, weightUnit, decimals)
                                                    },
                                                    showEditButton = showEdit, modifier = hndl
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

            if (!isExiting && curtainAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(20f)
                        .background(nightVaultColor)
                        .alpha(curtainAlpha)
                )
            }
        }

        // Selection Funnels
        assetBeingEdited?.let { asset ->
            if (asset.category == AssetCategory.METAL) {
                MetalSelectionFunnel(
                    initialMetal = asset.name, initialForm = asset.physicalForm, initialWeight = asset.weight, initialQty = asset.amountHeld.toString(), initialPrem = asset.premium.toString(), initialManualPrice = asset.officialSpotPrice.toString(),
                    onDismiss = { assetBeingEdited = null },
                    onConfirmed = { name, form, w, unit, qty, prem, icon, manual, price ->
                        viewModel.updateAssetEntity(asset.copy(name = name, physicalForm = form, weight = w, weightUnit = unit, amountHeld = qty.toDoubleOrNull() ?: 0.0, premium = prem.toDoubleOrNull() ?: 0.0, imageUrl = icon ?: asset.imageUrl, officialSpotPrice = price.toDoubleOrNull() ?: asset.officialSpotPrice))
                        assetBeingEdited = null
                        editingAssetId = null
                    }
                )
            } else {
                CryptoEditFunnel(
                    asset = asset, onDismiss = { assetBeingEdited = null },
                    onSave = { name, amt, dec ->
                        viewModel.updateAssetEntity(asset.copy(amountHeld = amt, decimalPreference = dec))
                        assetBeingEdited = null
                        editingAssetId = null
                    }
                )
            }
        }

        // --- 🛡️ V38.1 ASSET DELETION DIALOG ---
        assetToDelete?.let { asset ->
            AlertDialog(
                onDismissRequest = { viewModel.clearDeleteConfirmation() },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("REMOVE ASSET?", color = Color.Red, fontWeight = FontWeight.Black) },
                text = { Text("Are you sure you want to remove ${asset.symbol} from your vault?", color = Color.White) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteAsset(asset)
                        viewModel.clearDeleteConfirmation()
                    }) {
                        Text("REMOVE", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.clearDeleteConfirmation() }) {
                        Text("CANCEL", color = Color.White)
                    }
                }
            )
        }

        // Trash Zone
        AnimatedVisibility(visible = isDraggingActive.value, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.BottomEnd).padding(30.dp).onGloballyPositioned { c -> val p = c.positionInRoot(); trashBoundsInRoot.value = Rect(p.x, p.y, p.x + c.size.width, p.y + c.size.height) }) {
            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(if (isOverTrash.value) Color.Red else Color.DarkGray).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, null, tint = Color.White) }
        }
    }
}

fun Float.pow(x: Float) = Math.pow(this.toDouble(), x.toDouble()).toFloat()
