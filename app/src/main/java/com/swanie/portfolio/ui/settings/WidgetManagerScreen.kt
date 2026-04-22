@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.swanie.portfolio.ui.settings

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import com.swanie.portfolio.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.components.BoutiqueHeader
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.CompactAssetCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/** Reorder / “slide to slot” feel: heavy, deliberate motion */
private val WidgetReorderItemAnimationSpec = tween<IntSize>(
    durationMillis = 600,
    easing = LinearOutSlowInEasing
)

/** Wait until drag visuals settle before persisting order to the vault */
private const val WIDGET_DRAG_SETTLE_DELAY_MS = 380L

/**
 * LazyColumn index of the first asset [ReorderableItem] when the assets section is expanded:
 * 0 top spacer, 1 preview, 2 appearance, 3 hide totals, 4 sticky ASSETS, 5 landing spacer (16.dp).
 * Must stay in sync with the slot order in [WidgetManagerScreen]'s primary LazyColumn.
 */
private const val WIDGET_FLAT_LIST_FIRST_ASSET_INDEX = 6

/** Flat list: placement animation for rows (stable coordinates vs nested list). */
private val WidgetFlatListPlacementSpec = tween<IntOffset>(
    durationMillis = 600,
    easing = LinearOutSlowInEasing
)

/**
 * @param configAppWidgetId Host `appWidgetId` from the widget (pencil). [WidgetConfigActivity] supplies a
 *   valid id only; SAVE & EXIT binds the vault row to this id and refreshes this instance.
 */
@Composable
fun WidgetManagerScreen(
    navController: NavHostController? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    assetViewModel: AssetViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    isConfigMode: Boolean = false,
    configAppWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    onConfigComplete: () -> Unit = {},
    onBack: () -> Unit = { navController?.popBackStack() }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // 🎯 DRAFT STATE: Hoisted to the top for scope visibility
    var draftBg by rememberSaveable { mutableStateOf("#1C1C1E") }
    var draftBgTxt by rememberSaveable { mutableStateOf("#FFFFFF") }
    var draftCrd by rememberSaveable { mutableStateOf("#2C2C2E") }
    var draftCrdTxt by rememberSaveable { mutableStateOf("#FFFFFF") }
    var draftHideTotals by remember { mutableStateOf(false) }

    // 🎯 REGISTRY STATE: The target vault for configuration
    val targetVaultId by settingsViewModel.targetVaultId.collectAsStateWithLifecycle()
    val selectedVault by settingsViewModel.targetVault.collectAsStateWithLifecycle()
    val targetAssets by settingsViewModel.targetVaultAssets.collectAsStateWithLifecycle()
    /** Portfolio row the screen is editing (widget manager target), not the global theme vault. */
    val displayedPortfolioVaultId =
        remember(targetVaultId) { if (targetVaultId > 0) targetVaultId else 0 }
    val widgetSelectedIds by assetViewModel.widgetSelectedAssetIds.collectAsStateWithLifecycle()
    var dragOrderedIds by remember { mutableStateOf<List<String>?>(null) }
    // Stable vault membership for ordering only (price ticks must not reshuffle unchecked rows).
    val targetVaultCoinIdsFingerprint = targetAssets.joinToString("\u0001") { it.coinId }
    val targetVaultCoinIdKey = remember(targetVaultId, targetVaultCoinIdsFingerprint) {
        targetVaultCoinIdsFingerprint
    }
    val orderedWidgetAssetIds by remember(widgetSelectedIds, dragOrderedIds, targetVaultCoinIdKey) {
        derivedStateOf {
            val idsAll = targetVaultCoinIdKey.split('\u0001').filter { it.isNotBlank() }
            val idSet = idsAll.toSet()
            val liveDrag = dragOrderedIds
            when {
                liveDrag != null -> {
                    val drag = liveDrag.filter { it in idSet }
                    val tail = idsAll.filter { it !in drag.toSet() }
                    drag + tail
                }
                else -> {
                    // Selected-only "slide" block: widget order for checked rows, static tail for reading.
                    val selectedOrdered = widgetSelectedIds.filter { it in idSet }
                    val unselectedStatic = idsAll.filter { it !in selectedOrdered.toSet() }
                    selectedOrdered + unselectedStatic
                }
            }
        }
    }
    val orderedWidgetAssets = orderedWidgetAssetIds.mapNotNull { id ->
        targetAssets.find { it.coinId == id }
    }

    // Fallback if the activity’s async vault bind has not landed yet.
    LaunchedEffect(configAppWidgetId) {
        if (targetVaultId == -1 && configAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            settingsViewModel.forceVaultSwitch(configAppWidgetId, isAppWidgetId = true)
        }
    }

    // Keep AssetViewModel widget state bound to the vault being edited.
    LaunchedEffect(targetVaultId) {
        assetViewModel.setWidgetSelectionVaultId(targetVaultId.takeIf { it > 0 })
    }

    DisposableEffect(Unit) {
        onDispose {
            assetViewModel.setWidgetSelectionVaultId(null)
        }
    }

    // 🎯 INITIALIZATION LOCK: Force draft appearance states to match selected vault on ID change
    LaunchedEffect(selectedVault?.id) {
        selectedVault?.let {
            draftBg = it.widgetBgColor.takeIf { c -> c.isNotEmpty() } ?: "#1C1C1E"
            draftBgTxt = it.widgetBgTextColor.takeIf { c -> c.isNotEmpty() } ?: "#FFFFFF"
            draftCrd = it.widgetCardColor.takeIf { c -> c.isNotEmpty() } ?: "#2C2C2E"
            draftCrdTxt = it.widgetCardTextColor.takeIf { c -> c.isNotEmpty() } ?: "#FFFFFF"
            draftHideTotals = !it.showWidgetTotal
        }
    }

    // Local draft selection/order is the single source of truth while editing.
    LaunchedEffect(selectedVault?.id, targetAssets, widgetSelectedIds) {
        if (selectedVault == null) {
            return@LaunchedEffect
        }
    }

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsStateWithLifecycle(initialValue = "#000416")
    val siteTextColor by themeViewModel.siteTextColor.collectAsStateWithLifecycle(initialValue = "#FFFFFF")
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsStateWithLifecycle(initialValue = "#121212")
    val cardTextColor by themeViewModel.cardTextColor.collectAsStateWithLifecycle(initialValue = "#FFFFFF")

    val isDirty by remember(selectedVault, draftBg, draftBgTxt, draftCrd, draftCrdTxt, widgetSelectedIds, draftHideTotals) {
        derivedStateOf {
            selectedVault?.let {
                val originalIds = it.selectedWidgetAssets.split(",").filter { id -> id.isNotBlank() }
                val originalHideTotals = !it.showWidgetTotal

                draftBg != it.widgetBgColor ||
                        draftBgTxt != it.widgetBgTextColor ||
                        draftCrd != it.widgetCardColor ||
                        draftCrdTxt != it.widgetCardTextColor ||
                        widgetSelectedIds != originalIds ||
                        draftHideTotals != originalHideTotals
            } ?: false
        }
    }

    fun revertChanges() {
        selectedVault?.let {
            draftBg = it.widgetBgColor
            draftBgTxt = it.widgetBgTextColor
            draftCrd = it.widgetCardColor
            draftCrdTxt = it.widgetCardTextColor
            draftHideTotals = !it.showWidgetTotal
        }
    }
    var appearanceExpanded by rememberSaveable { mutableStateOf(false) }
    var assetsExpanded by rememberSaveable { mutableStateOf(true) }

    val safeThemeText = try { Color((siteTextColor ?: "#FFFFFF").toColorInt()) } catch(e: Exception) { Color.White }
    val safeSiteBg = try { Color((siteBgColor ?: "#000416").toColorInt()) } catch(e: Exception) { Color(0xFF000416) }
    val safeCardBg = try { Color((cardBgColor ?: "#121212").toColorInt()) } catch(e: Exception) { Color(0xFF121212) }
    val safeCardText = try { Color((cardTextColor ?: "#FFFFFF").toColorInt()) } catch(e: Exception) { Color.White }

    val lazyListState = rememberLazyListState()
    val reorderableAssetsState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            if (!assetsExpanded) return@rememberReorderableLazyListState
            val ids = dragOrderedIds ?: orderedWidgetAssetIds
            if (ids.isEmpty()) return@rememberReorderableLazyListState
            val fromA = from.index - WIDGET_FLAT_LIST_FIRST_ASSET_INDEX
            val toA = to.index - WIDGET_FLAT_LIST_FIRST_ASSET_INDEX
            if (fromA !in ids.indices || toA !in ids.indices) return@rememberReorderableLazyListState
            val working = ids.toMutableList()
            working.add(toA, working.removeAt(fromA))
            dragOrderedIds = working
        }
    )

    LaunchedEffect(assetsExpanded) {
        if (!assetsExpanded) {
            dragOrderedIds = null
        }
    }
    
    val sharedPrefs = remember { context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        cooldownSeconds = 0
    }
    LaunchedEffect(cooldownSeconds) { if (cooldownSeconds > 0) { delay(1000L); cooldownSeconds -= 1 } }

    Box(modifier = Modifier.fillMaxSize().background(if (isConfigMode) safeSiteBg else Color.Transparent)) {
        if (targetVaultId == -1) {
            // 🚀 THE STATE FLICKER: 10ms transient black hole to break Intent ghosting
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = safeThemeText, strokeWidth = 2.dp)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                val canOfferSaveAndExit =
                    displayedPortfolioVaultId > 0 &&
                        (isDirty || (isConfigMode && selectedVault != null))

                // --- 🦢 BOUTIQUE HEADER ---
                BoutiqueHeader(
                    title = "WIDGET MANAGER",
                    onBack = { onBack() },
                    actionIcon = if (canOfferSaveAndExit) Icons.Default.Save else Icons.Default.Undo,
                    actionLabel = if (canOfferSaveAndExit) "SAVE & EXIT" else null,
                    onAction = {
                        if (canOfferSaveAndExit) {
                            if (cooldownSeconds == 0 && selectedVault != null) {
                                scope.launch {
                                    val selectedSet = widgetSelectedIds.toSet()
                                    val reorderedCheckedIds = (dragOrderedIds ?: orderedWidgetAssetIds)
                                        .filter { it in selectedSet }
                                    val portfolioId = displayedPortfolioVaultId
                                    val currentWidgetId = if (isConfigMode) configAppWidgetId else null

                                    assetViewModel.saveWidgetConfiguration(
                                        portfolioId,
                                        currentWidgetId,
                                        reorderedCheckedIds,
                                    ) {
                                        scope.launch {
                                            settingsViewModel.saveWidgetAppearance(
                                                portfolioId,
                                                draftBg,
                                                draftBgTxt,
                                                draftCrd,
                                                draftCrdTxt
                                            )
                                            settingsViewModel.updateShowWidgetTotal(portfolioId, !draftHideTotals)
                                            settingsViewModel.getVaultById(portfolioId)

                                            if (currentWidgetId != null && currentWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                                settingsViewModel.forceImmediateRemoteViewsUpdate(portfolioId, currentWidgetId)
                                            }

                                            sharedPrefs.edit().putLong("last_widget_save_time", System.currentTimeMillis()).apply()

                                            if (isConfigMode) {
                                                onConfigComplete()
                                            } else {
                                                Toast.makeText(context, "Registry Synced!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            revertChanges()
                        }
                    },
                    textColor = safeThemeText
                )

                if (selectedVault == null) {
                    // 🛡️ THE SAFETY VALVE: Break the infinite spinner for new widgets
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = safeThemeText, strokeWidth = 2.dp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "LINK THIS WIDGET TO A PORTFOLIO",
                            color = safeThemeText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            // Relax layer clip so drag / placement motion reads cleanly in the flat list.
                            .graphicsLayer { clip = false },
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 0.dp)
                    ) {
                        item(key = "wm_top_spacer") {
                            Spacer(Modifier.height(8.dp))
                        }
                        item(key = "wm_preview") {
                            WidgetPreviewSlim(
                                bgHex = draftBg,
                                bgTxtHex = draftBgTxt,
                                cardHex = draftCrd,
                                cardTxtHex = draftCrdTxt,
                                showTotal = !draftHideTotals
                            )
                        }

                        item(key = "wm_appearance") {
                            SectionHeaderSmall("APPEARANCE", appearanceExpanded, safeThemeText) { appearanceExpanded = !appearanceExpanded }
                            AnimatedVisibility(visible = appearanceExpanded) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = try { Color(draftCrd.toColorInt()).copy(alpha = 0.5f) } catch(e: Exception) { Color.DarkGray.copy(alpha = 0.5f) }),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, safeThemeText.copy(alpha = 0.1f))
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        WidgetStudioInlineCompact(draftBg, draftBgTxt, draftCrd, draftCrdTxt) { target, newHex ->
                                            when(target) {
                                                0 -> draftBg = newHex
                                                1 -> draftBgTxt = newHex
                                                2 -> draftCrd = newHex
                                                3 -> draftCrdTxt = newHex
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = safeThemeText.copy(0.1f), thickness = 1.dp)
                        }

                        item(key = "wm_hide_totals") {
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { draftHideTotals = !draftHideTotals },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hide Numbers", color = safeThemeText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = draftHideTotals,
                                    onCheckedChange = { draftHideTotals = it },
                                    colors = CheckboxDefaults.colors(checkedColor = safeThemeText)
                                )
                            }
                            HorizontalDivider(color = safeThemeText.copy(0.1f), thickness = 1.dp)
                        }

                        stickyHeader(key = "wm_assets_sticky") {
                            val countText = "${orderedWidgetAssets.size} ORDERED"
                            val headerBg = try {
                                Color(draftCrd.toColorInt()).copy(alpha = 0.96f)
                            } catch (e: Exception) {
                                Color(0xFF2C2C2E).copy(alpha = 0.96f)
                            }
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .background(headerBg)
                                    .border(1.dp, safeThemeText.copy(alpha = 0.12f))
                            ) {
                                SectionHeaderSmall(
                                    title = "ASSETS ($countText)",
                                    isExpanded = assetsExpanded,
                                    themeColor = safeThemeText,
                                    onToggle = { assetsExpanded = !assetsExpanded }
                                )
                            }
                        }

                        if (assetsExpanded) {
                            item(key = "wm_assets_landing_spacer") {
                                Spacer(Modifier.height(16.dp))
                            }
                            if (orderedWidgetAssets.isEmpty()) {
                                item(key = "wm_assets_empty") {
                                    Text(
                                        "No assets in this portfolio",
                                        color = safeThemeText.copy(0.4f),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                items(orderedWidgetAssets, key = { it.coinId }) { asset ->
                                    ReorderableItem(
                                        state = reorderableAssetsState,
                                        key = asset.coinId,
                                        animateItemModifier = Modifier.animateItem(placementSpec = WidgetFlatListPlacementSpec)
                                    ) { isDragging ->
                                        val isChecked = widgetSelectedIds.contains(asset.coinId)
                                        WidgetReorderVisibilityItem(
                                            asset = asset,
                                            isChecked = isChecked,
                                            isDragging = isDragging,
                                            cardBg = safeCardBg,
                                            cardText = safeCardText,
                                            baseCurrency = selectedVault?.baseCurrency ?: "USD",
                                            animatePlacement = isChecked || isDragging,
                                            onToggleChecked = { checked ->
                                                if (checked) {
                                                    if (widgetSelectedIds.size >= 5 && !isChecked) {
                                                        Toast.makeText(context, "Max 5 assets allowed on widget.", Toast.LENGTH_SHORT).show()
                                                        return@WidgetReorderVisibilityItem
                                                    }
                                                }
                                                assetViewModel.toggleAssetInWidgetSelection(asset)
                                            },
                                            modifier = Modifier.longPressDraggableHandle(
                                                onDragStarted = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                },
                                                onDragStopped = {
                                                    scope.launch {
                                                        delay(WIDGET_DRAG_SETTLE_DELAY_MS)
                                                        val selectedSet = widgetSelectedIds.toSet()
                                                        val reorderedCheckedIds = (dragOrderedIds ?: orderedWidgetAssetIds)
                                                            .filter { it in selectedSet }
                                                        assetViewModel.updateWidgetSelectionForCurrentVault(reorderedCheckedIds)
                                                        dragOrderedIds = null
                                                    }
                                                }
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "wm_bottom_spacer") {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetReorderVisibilityItem(
    asset: AssetEntity,
    isChecked: Boolean,
    isDragging: Boolean,
    cardBg: Color,
    cardText: Color,
    baseCurrency: String,
    animatePlacement: Boolean = false,
    onToggleChecked: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val placementModifier = if (animatePlacement) {
        Modifier.animateContentSize(animationSpec = WidgetReorderItemAnimationSpec)
    } else {
        Modifier
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        CompactAssetCard(
            asset = asset,
            isDragging = isDragging,
            cardBg = cardBg,
            cardText = cardText,
            baseCurrency = baseCurrency,
            onExpandToggle = {},
            showEditButton = false,
            isExpanded = false,
            modifier = modifier
                .then(placementModifier)
                .alpha(if (isChecked) 1f else 0.45f)
        )
        Checkbox(
            checked = isChecked,
            onCheckedChange = onToggleChecked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Yellow,
                uncheckedColor = cardText.copy(alpha = 0.45f),
                checkmarkColor = Color.Black
            )
        )
    }
}

@Composable
fun WidgetPreviewSlim(bgHex: String, bgTxtHex: String, cardHex: String, cardTxtHex: String, showTotal: Boolean) {
    val bgColor = try { Color(bgHex.toColorInt()) } catch(e: Exception) { Color.Black }
    val bgTextColor = try { Color(bgTxtHex.toColorInt()) } catch(e: Exception) { Color.White }
    val cardColor = try { Color(cardHex.toColorInt()) } catch(e: Exception) { Color.DarkGray }
    val cardTextColor = try { Color(cardTxtHex.toColorInt()) } catch(e: Exception) { Color.White }

    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)).padding(8.dp)) {
        if (showTotal) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("$42,069.00", color = bgTextColor, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(8.dp))
                Text("+5.2%", color = Color(0xFF00FF00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
        }
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(cardColor).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(16.dp).background(Color.White.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Text("₿", color = Color.White, fontSize = 9.sp) }
            Spacer(Modifier.width(6.dp))
            Text("BITCOIN", color = cardTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("$98,245.00", color = cardTextColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SectionHeaderSmall(title: String, isExpanded: Boolean, themeColor: Color, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = themeColor, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun WidgetStudioInlineCompact(
    draftBg: String, draftBgTxt: String, draftCrd: String, draftCrdTxt: String,
    onColorChanged: (Int, String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var activeTarget by rememberSaveable { mutableIntStateOf(0) }
    val targets = listOf("Widget BG", "BG Text", "Card BG", "Card Text")
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }

    val liveColor = remember(hexInput, hue, saturation, value) {
        try { if (hexInput.length == 6) Color(android.graphics.Color.parseColor("#$hexInput")) else Color.hsv(hue, saturation, value) }
        catch (e: Exception) { Color.hsv(hue, saturation, value) }
    }

    LaunchedEffect(activeTarget) {
        val cur = when (activeTarget) { 0 -> draftBg; 1 -> draftBgTxt; 2 -> draftCrd; else -> draftCrdTxt }
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(try { Color(cur.toColorInt()).toArgb() } catch(e: Exception) { 0 }, hsv)
        hue = hsv[0]; saturation = hsv[1]; value = hsv[2]; hexInput = cur.replace("#", "").uppercase()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            targets.forEachIndexed { i, label ->
                val isSel = activeTarget == i
                Box(Modifier.weight(1f).height(48.dp).background(if(isSel) Color.White.copy(0.1f) else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if(isSel) Color.Yellow else Color.Gray.copy(0.3f), RoundedCornerShape(4.dp)).clickable { activeTarget = i }, contentAlignment = Alignment.Center) {
                    Text(label, color = if(isSel) Color.Yellow else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(0.5f).fillMaxHeight().background(liveColor, RoundedCornerShape(4.dp)).border(1.dp, Color.White, RoundedCornerShape(4.dp)))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f).fillMaxHeight().background(Color.Black, RoundedCornerShape(4.dp)).border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                BasicTextField(value = hexInput, onValueChange = { if (it.length <= 6) hexInput = it.uppercase() }, textStyle = TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = SolidColor(Color.Yellow), singleLine = true)
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color.Gray, RoundedCornerShape(8.dp))) {
            val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f)))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> saturation = (change.position.x / w).coerceIn(0f, 1f); value = 1f - (change.position.y / h).coerceIn(0f, 1f); hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, saturation, value).toArgb()) } })
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(24.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape)) {
            val w = constraints.maxWidth.toFloat(); val colors = (0..360).map { Color.hsv(it.toFloat(), 1f, 1f) }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(colors)))
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> hue = (change.position.x / w * 360f).coerceIn(0f, 360f); hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, saturation, value).toArgb()) } })
        }
        Button(onClick = {
            onColorChanged(activeTarget, "#$hexInput")
            isFlashing = true; scope.launch { delay(200); isFlashing = false }
            keyboardController?.hide(); focusManager.clearFocus()
        }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isFlashing) Color.White else Color.Yellow)) {
            Text("SET DRAFT ${targets[activeTarget].uppercase()}", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

