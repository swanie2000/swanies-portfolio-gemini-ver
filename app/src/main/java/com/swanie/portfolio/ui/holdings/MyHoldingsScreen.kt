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

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = null)
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

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha_pulse"
    )

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
                mainViewModel.selectVault(targetVault.id)
            }
        }
    }

    val totalValueFormatted by remember(holdings, activeVault.baseCurrency) {
        derivedStateOf {
            val h = holdings ?: emptyList()
            val total = h.sumOf { (it.officialSpotPrice * it.weight * it.amountHeld) + it.premium }
            formatCurrency(total, 2, activeVault.baseCurrency)
        }
    }

    LaunchedEffect(holdings) {
        if (!isDraggingActive.value && assetBeingEdited == null) localHoldings = holdings ?: emptyList()
    }

    val filteredHoldings by remember(localHoldings, selectedTab) {
        derivedStateOf {
            when (selectedTab) {
                1 -> localHoldings.filter { it.category == AssetCategory.CRYPTO }
                2 -> localHoldings.filter { it.category == AssetCategory.METAL }
                else -> localHoldings
            }
        }
    }

    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val fromItem = filteredHoldings[from.index]
            val toItem = filteredHoldings[to.index]
            localHoldings = localHoldings.toMutableList().apply {
                val fromIdx = indexOfFirst { it.coinId == fromItem.coinId }
                val toIdx = indexOfFirst { it.coinId == toItem.coinId }
                if (fromIdx != -1 && toIdx != -1) {
                    add(toIdx, removeAt(fromIdx))
                }
            }
        }
    )

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isDark = ColorUtils.calculateLuminance(siteBg.toArgb()) < 0.5
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(siteBg)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull() ?: continue
                    trashBoundsInRoot.value?.let { bounds ->
                        isOverTrash.value = isDraggingActive.value && bounds.contains(change.position)
                    }
                }
            }
        }
    ) {
        if (!isExiting) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Persistent Logo Branding
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(100.dp).zIndex(10f)) {
                    Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.Center))
                    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (!isViewModelRefreshing) viewModel.refreshAssets()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Assets",
                                tint = if (isViewModelRefreshing) textColor.copy(alpha = 0.3f) else textColor
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { mainViewModel.toggleCompactView() }) {
                            Icon(imageVector = if (isCompactViewEnabled) Icons.Default.ViewModule else Icons.AutoMirrored.Filled.ViewList, contentDescription = null, tint = textColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { isExiting = true; navController.navigate(Routes.ASSET_PICKER) }, modifier = Modifier.clip(CircleShape).background(Color.Yellow).size(44.dp)) {
                            Icon(Icons.Default.Add, null, tint = Color.Black)
                        }
                    }
                }

                // Header Control Deck
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    userScrollEnabled = !isDraggingActive.value,
                    key = { page -> allVaults.getOrNull(page)?.id ?: page }
                ) { page ->
                    val vaultForPage = allVaults.getOrNull(page) ?: activeVault
                    val isCurrentPage = vaultForPage.id == activeVault.id
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

                    val contentAlpha = if (pagerState.isScrollInProgress) {
                        (1f - (pageOffset * 1.2f).coerceIn(0f, 1f)).pow(2f)
                    } else if (isCurrentPage) {
                        1f
                    } else {
                        0f
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                isExiting = true; navController.navigate(Routes.PORTFOLIO_MANAGER)
                            }
                            .graphicsLayer {
                                alpha = contentAlpha
                                val scale = if (pagerState.isScrollInProgress) {
                                    lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                } else 1f
                                scaleX = if (isCurrentPage) scale else 0.85f
                                scaleY = if (isCurrentPage) scale else 0.85f
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = vaultForPage.id,
                            transitionSpec = {
                                fadeIn(tween(300)) + scaleIn(initialScale = 0.95f) togetherWith fadeOut(tween(300))
                            },
                            label = "header_morph"
                        ) { _ ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = vaultForPage.name.uppercase(),
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )

                                AnimatedContent(
                                    targetState = isCurrentPage && holdings != null,
                                    transitionSpec = {
                                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                                    }, label = "value_fade"
                                ) { loaded ->
                                    if (loaded) {
                                        Text(
                                            text = totalValueFormatted,
                                            color = textColor.copy(alpha = 0.8f),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    } else {
                                        Box(modifier = Modifier.width(160.dp).height(38.dp).padding(vertical = 8.dp).clip(RoundedCornerShape(8.dp)).background(textColor.copy(alpha = 0.1f)))
                                    }
                                }
                            }
                        }

                        if (allVaults.size > 1) {
                            val swipeLabel = when {
                                page == 0 -> ">"
                                page == allVaults.size - 1 -> "<"
                                else -> "<  SWIPE  >"
                            }
                            Text(
                                text = swipeLabel,
                                color = textColor.copy(alpha = if (isCurrentPage) breathingAlpha else 0.2f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Vault Floor (Static Area)
                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    AnimatedContent(
                        targetState = activeVault.id,
                        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                        modifier = Modifier.fillMaxSize(),
                        label = "vault_floor_transition"
                    ) { _ ->
                        Crossfade(targetState = holdings == null, label = "ghost_handshake") { isLoading ->
                            if (isLoading) {
                                SkeletonAssetList()
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Categories Tabs
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(44.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        tabs.forEachIndexed { index, title ->
                                            val isSelected = selectedTab == index
                                            val isMetalTab = title == "METAL"
                                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp).clip(CircleShape).background(if (isSelected) textColor.copy(0.15f) else Color.Transparent).border(1.dp, if (isSelected) textColor.copy(0.3f) else textColor.copy(0.1f), CircleShape).clickable { selectedTab = index },
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) textColor else textColor.copy(0.5f))
                                                }
                                                if (isMetalTab && isSelected) {
                                                    IconButton(onClick = { isExiting = true; navController.navigate(Routes.METALS_AUDIT) }, modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp).size(28.dp)) {
                                                        Icon(Icons.Default.Security, null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Box(modifier = Modifier.fillMaxWidth().height(12.dp).padding(horizontal = 24.dp)) {
                                        if (isViewModelRefreshing) {
                                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.Center), color = Color.Yellow, trackColor = textColor.copy(0.1f))
                                        }
                                    }

                                    LazyColumn(
                                        state = lazyListState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(items = filteredHoldings, key = { asset: AssetEntity -> asset.coinId }) { asset ->
                                            ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
                                                val isExpanded = expandedAssetId == asset.coinId
                                                val dragModifier = Modifier.longPressDraggableHandle(
                                                    onDragStarted = { isDraggingActive.value = true; haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                                    onDragStopped = {
                                                        if (isOverTrash.value) { assetPendingDeletion = asset }
                                                        isDraggingActive.value = false
                                                        viewModel.updateAssetOrder(localHoldings)
                                                    }
                                                )
                                                if (isCompactViewEnabled && !isExpanded) {
                                                    CompactAssetCard(asset, isDragging, cardBg, cardText, activeVault.baseCurrency, { expandedAssetId = if (expandedAssetId == asset.coinId) null else asset.coinId }, dragModifier)
                                                } else {
                                                    FullAssetCard(asset, isExpanded, false, isDragging, isExpanded, cardBg, cardText, activeVault.baseCurrency, { expandedAssetId = if (expandedAssetId == asset.coinId) null else asset.coinId }, { assetBeingEdited = asset }, { _, _, _, _, _ -> }, modifier = dragModifier)
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
        }

        AnimatedVisibility(visible = isDraggingActive.value && !isExiting, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 40.dp).zIndex(100f).onGloballyPositioned { coords ->
            val pos = coords.positionInRoot()
            trashBoundsInRoot.value = Rect(pos.x, pos.y, pos.x + coords.size.width, pos.y + coords.size.height)
        }) {
            Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(if (isOverTrash.value) Color.Red else Color.DarkGray.copy(0.9f)).border(3.dp, if (isOverTrash.value) Color.White else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }

        assetBeingEdited?.let { asset ->
            if (asset.category == AssetCategory.CRYPTO) {
                CryptoEditFunnel(asset = asset, onDismiss = { assetBeingEdited = null }, onSave = { n, a, d -> viewModel.updateAsset(asset, n, a, asset.weight, asset.weightUnit, d); assetBeingEdited = null })
            } else {
                MetalSelectionFunnel(initialMetal = asset.symbol, initialForm = asset.name, initialWeight = asset.weight, initialQty = asset.amountHeld.toString(), initialPrem = asset.premium.toString(), initialManualPrice = asset.officialSpotPrice.toString(), onDismiss = { assetBeingEdited = null }, onConfirmed = { t, ds, w, u, q, p, i, m, mp -> viewModel.updateAssetEntity(asset.copy(symbol = t, name = ds, weight = w, weightUnit = u, amountHeld = q.toDoubleOrNull() ?: 0.0, premium = p.toDoubleOrNull() ?: 0.0, imageUrl = i ?: asset.imageUrl, officialSpotPrice = if(m) (mp.toDoubleOrNull() ?: 0.0) else asset.officialSpotPrice)); assetBeingEdited = null })
            }
        }

        assetPendingDeletion?.let { asset ->
            AlertDialog(onDismissRequest = { assetPendingDeletion = null }, containerColor = Color(0xFF1A1A1A), title = { Text("PURGE ASSET?", color = Color.Red, fontWeight = FontWeight.Black) }, text = { Text("Permanently remove ${asset.displayName.ifEmpty { asset.name }}?") }, confirmButton = { TextButton(onClick = { viewModel.deleteAsset(asset); assetPendingDeletion = null }) { Text("DELETE", color = Color.Red) } }, dismissButton = { TextButton(onClick = { assetPendingDeletion = null }) { Text("CANCEL", color = Color.White) } })
        }
    }
}

fun Float.pow(x: Float) = Math.pow(this.toDouble(), x.toDouble()).toFloat()

fun lerp(start: Float, stop: Float, fraction: Float): Float = (1 - fraction) * start + fraction * stop