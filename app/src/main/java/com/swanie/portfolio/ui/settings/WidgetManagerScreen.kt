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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.swanie.portfolio.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.ui.components.BoutiqueHeader
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.AssetCardFontScaleScope
import com.swanie.portfolio.ui.holdings.CompactAssetCard
import com.swanie.portfolio.ui.holdings.MetalIcon
import com.swanie.portfolio.ui.holdings.SparklineChart
import com.swanie.portfolio.ui.holdings.formatBoutiquePrice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.NumberFormat
import java.util.Locale

/** Reorder / “slide to slot” feel: heavy, deliberate motion */
private val WidgetReorderItemAnimationSpec = tween<IntSize>(
    durationMillis = 600,
    easing = LinearOutSlowInEasing
)

/** Wait until drag visuals settle before persisting order to the vault */
private const val WIDGET_DRAG_SETTLE_DELAY_MS = 380L

/**
 * LazyColumn index of the first asset [ReorderableItem] on the ASSETS sub-page:
 * 0 top spacer, 1 sticky ASSETS header, 2 landing spacer.
 */
private const val WIDGET_FLAT_LIST_FIRST_ASSET_INDEX = 3

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
    val isHighVisibilityMode by settingsViewModel.isHighVisibilityMode.collectAsStateWithLifecycle()
    val selectedVault by settingsViewModel.targetVault.collectAsStateWithLifecycle()
    val targetAssets by settingsViewModel.targetVaultAssets.collectAsStateWithLifecycle()
    val allVaults by settingsViewModel.allVaults.collectAsStateWithLifecycle()
    /** Portfolio row the screen is editing (widget manager target), not the global theme vault. */
    val displayedPortfolioVaultId =
        remember(targetVaultId) { if (targetVaultId > 0) targetVaultId else 0 }
    val widgetSelectedIds by assetViewModel.widgetSelectedAssetIds.collectAsStateWithLifecycle()
    var dragOrderedIds by remember { mutableStateOf<List<String>?>(null) }
    /** Full row order after finger lifts; keeps UI on this order until [persistWidgetSelectionOrderForCurrentVault] succeeds. */
    var postDragOrderLock by remember { mutableStateOf<List<String>?>(null) }
    // Stable vault membership for ordering only (price ticks must not reshuffle unchecked rows).
    val targetVaultCoinIdsFingerprint = targetAssets.joinToString("\u0001") { it.coinId }
    val targetVaultCoinIdKey = remember(targetVaultId, targetVaultCoinIdsFingerprint) {
        targetVaultCoinIdsFingerprint
    }
    val orderedWidgetAssetIds by remember(
        widgetSelectedIds,
        dragOrderedIds,
        postDragOrderLock,
        targetVaultCoinIdKey,
    ) {
        derivedStateOf {
            val idsAll = targetVaultCoinIdKey.split('\u0001').filter { it.isNotBlank() }
            val idSet = idsAll.toSet()
            when {
                dragOrderedIds != null -> {
                    val drag = dragOrderedIds!!.filter { it in idSet }
                    val tail = idsAll.filter { it !in drag.toSet() }
                    drag + tail
                }
                postDragOrderLock != null -> {
                    val locked = postDragOrderLock!!.filter { it in idSet }
                    val tail = idsAll.filter { it !in locked.toSet() }
                    locked + tail
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
            dragOrderedIds = null
            postDragOrderLock = null
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

    var currentPage by remember { mutableIntStateOf(0) } // 0=SETUP, 1=ASSETS, 2=STYLE, 3=PREVIEW

    val safeThemeText = try { Color((siteTextColor ?: "#FFFFFF").toColorInt()) } catch(e: Exception) { Color.White }
    val safeSiteBg = try { Color((siteBgColor ?: "#000416").toColorInt()) } catch(e: Exception) { Color(0xFF000416) }
    val safeCardBg = try { Color((cardBgColor ?: "#121212").toColorInt()) } catch(e: Exception) { Color(0xFF121212) }
    val safeCardText = try { Color((cardTextColor ?: "#FFFFFF").toColorInt()) } catch(e: Exception) { Color.White }

    val lazyListStateSetup = rememberLazyListState()
    val lazyListStateAssets = rememberLazyListState()
    val lazyListStateStyle = rememberLazyListState()
    val lazyListStatePreview = rememberLazyListState()
    val reorderableAssetsState = rememberReorderableLazyListState(
        lazyListState = lazyListStateAssets,
        onMove = { from, to ->
            if (currentPage != 1) return@rememberReorderableLazyListState
            val ids = dragOrderedIds ?: postDragOrderLock ?: orderedWidgetAssetIds
            if (ids.isEmpty()) return@rememberReorderableLazyListState
            val fromA = from.index - WIDGET_FLAT_LIST_FIRST_ASSET_INDEX
            val toA = to.index - WIDGET_FLAT_LIST_FIRST_ASSET_INDEX
            if (fromA !in ids.indices || toA !in ids.indices) return@rememberReorderableLazyListState
            val working = ids.toMutableList()
            working.add(toA, working.removeAt(fromA))
            dragOrderedIds = working
        }
    )

    LaunchedEffect(currentPage) {
        if (currentPage != 1) {
            dragOrderedIds = null
            postDragOrderLock = null
        }
    }

    LaunchedEffect(targetVaultId) {
        postDragOrderLock = null
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

                val showPortfolioSelector =
                    targetVaultId != -1 &&
                        (!isConfigMode || configAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)

                val runSaveAction: () -> Unit = {
                    if (cooldownSeconds == 0 && selectedVault != null) {
                        scope.launch {
                            val selectedSet = widgetSelectedIds.toSet()
                            val reorderedCheckedIds =
                                orderedWidgetAssets.map { it.coinId }.filter { it in selectedSet }
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
                                    dragOrderedIds = null
                                    postDragOrderLock = null
                                }
                            }
                        }
                    }
                }

                // --- 🦢 BOUTIQUE HEADER (pinned top on every page) ---
                BoutiqueHeader(
                    title = "WIDGET MANAGER",
                    onBack = { onBack() },
                    actionIcon = if (canOfferSaveAndExit) Icons.Default.Save else Icons.Default.Undo,
                    actionLabel = if (canOfferSaveAndExit) "SAVE & EXIT" else null,
                    textColor = safeThemeText,
                    belowBrandingContent = {
                        WidgetSubPageTabRow(
                            current = currentPage,
                            onSelect = { currentPage = it },
                            themeColor = safeThemeText,
                        )
                    },
                    onAction = {
                        if (canOfferSaveAndExit) {
                            runSaveAction()
                        } else {
                            revertChanges()
                        }
                    },
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
                    when (currentPage) {
                        0 -> {
                            LazyColumn(
                                state = lazyListStateSetup,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                            ) {
                                item("wm_setup_title") {
                                    Text(
                                        "SETUP",
                                        color = safeThemeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.1.sp,
                                    )
                                }
                                item("wm_setup_dropdown") {
                                    if (showPortfolioSelector) {
                                        Box(Modifier.fillMaxWidth(0.95f)) {
                                            PortfolioSelectorDropdown(
                                                vaults = allVaults,
                                                selectedVaultId = displayedPortfolioVaultId,
                                                themeColor = safeThemeText,
                                                isWidgetInstanceContext =
                                                    isConfigMode && configAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID,
                                                compact = true,
                                                onVaultSelected = { vaultId ->
                                                    if (vaultId == displayedPortfolioVaultId) return@PortfolioSelectorDropdown
                                                    scope.launch {
                                                        dragOrderedIds = null
                                                        postDragOrderLock = null
                                                        if (isConfigMode && configAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                                            settingsViewModel.bindWidgetInstanceToVault(configAppWidgetId, vaultId)
                                                        }
                                                        settingsViewModel.setTargetVaultId(vaultId)
                                                        settingsViewModel.triggerWidgetUpdate(vaultId)
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                                item("wm_setup_divider") {
                                    HorizontalDivider(color = safeThemeText.copy(alpha = 0.12f))
                                }
                                item("wm_setup_show_totals") {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { draftHideTotals = !draftHideTotals }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Checkbox(
                                            checked = !draftHideTotals,
                                            onCheckedChange = { draftHideTotals = !it },
                                            colors = CheckboxDefaults.colors(checkedColor = safeThemeText),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Show Portfolio Total Amount",
                                            color = safeThemeText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            LazyColumn(
                                state = lazyListStateAssets,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .graphicsLayer { clip = false },
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                contentPadding = PaddingValues(top = 12.dp, bottom = 0.dp),
                            ) {
                                item(key = "wm_top_spacer") {
                                    Spacer(Modifier.height(8.dp))
                                }
                                stickyHeader(key = "wm_assets_sticky") {
                                    val selectedCount = widgetSelectedIds.size
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(safeSiteBg)
                                            .padding(top = 4.dp, bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.Start,
                                        ) {
                                            Text(
                                                "SELECTED ASSETS",
                                                color = safeThemeText.copy(alpha = 0.6f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                "CAN BE REORDERED",
                                                color = safeThemeText.copy(alpha = 0.6f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = safeThemeText.copy(alpha = 0.12f),
                                        ) {
                                            Text(
                                                "$selectedCount/5",
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                color = safeThemeText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                            )
                                        }
                                    }
                                }
                                item(key = "wm_assets_landing_spacer") {
                                    HorizontalDivider(color = safeThemeText.copy(alpha = 0.12f))
                                    Spacer(Modifier.height(8.dp))
                                }
                                if (orderedWidgetAssets.isEmpty()) {
                                    item(key = "wm_assets_empty") {
                                        Text(
                                            "No assets in this portfolio",
                                            color = safeThemeText.copy(0.4f),
                                            modifier = Modifier.padding(20.dp),
                                        )
                                    }
                                } else {
                                    items(orderedWidgetAssets, key = { it.coinId }) { asset ->
                                        ReorderableItem(
                                            state = reorderableAssetsState,
                                            key = asset.coinId,
                                            animateItemModifier = Modifier.animateItem(
                                                placementSpec = WidgetFlatListPlacementSpec,
                                            ),
                                        ) { isDragging ->
                                            val isChecked = widgetSelectedIds.contains(asset.coinId)
                                            WidgetReorderVisibilityItem(
                                                asset = asset,
                                                isChecked = isChecked,
                                                isDragging = isDragging,
                                                cardBg = safeCardBg,
                                                cardText = safeCardText,
                                                baseCurrency = selectedVault?.baseCurrency ?: "USD",
                                                isHighVisibilityMode = isHighVisibilityMode,
                                                animatePlacement = isChecked || isDragging,
                                                onToggleChecked = { checked ->
                                                    if (checked) {
                                                        if (widgetSelectedIds.size >= 5 && !isChecked) {
                                                            Toast.makeText(
                                                                context,
                                                                "Max 5 assets allowed on widget.",
                                                                Toast.LENGTH_SHORT,
                                                            ).show()
                                                            return@WidgetReorderVisibilityItem
                                                        }
                                                    }
                                                    assetViewModel.toggleAssetInWidgetSelection(asset)
                                                },
                                                modifier = Modifier.longPressDraggableHandle(
                                                    onDragStarted = {
                                                        postDragOrderLock = null
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    },
                                                    onDragStopped = {
                                                        val snapshot = dragOrderedIds?.toList()
                                                            ?: return@longPressDraggableHandle
                                                        postDragOrderLock = snapshot
                                                        dragOrderedIds = null
                                                        scope.launch {
                                                            delay(WIDGET_DRAG_SETTLE_DELAY_MS)
                                                            val selectedSet = widgetSelectedIds.toSet()
                                                            val reorderedCheckedIds =
                                                                snapshot.filter { it in selectedSet }
                                                            val ok =
                                                                assetViewModel.persistWidgetSelectionOrderForCurrentVault(
                                                                    reorderedCheckedIds,
                                                                )
                                                            if (ok) {
                                                                postDragOrderLock = null
                                                            }
                                                        }
                                                    },
                                                ),
                                            )
                                        }
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = safeThemeText.copy(alpha = 0.08f),
                                        )
                                    }
                                }
                                item(key = "wm_bottom_spacer_assets") {
                                    Spacer(modifier = Modifier.height(100.dp))
                                }
                            }
                        }
                        2 -> {
                            val styleSampleAsset = remember(orderedWidgetAssets, widgetSelectedIds, targetAssets) {
                                orderedWidgetAssets.firstOrNull { it.coinId in widgetSelectedIds }
                                    ?: orderedWidgetAssets.firstOrNull()
                                    ?: targetAssets.firstOrNull()
                            }
                            val styleHeaderData = remember(orderedWidgetAssets, widgetSelectedIds, selectedVault?.name) {
                                runCatching {
                                    val selectedTotalValue = orderedWidgetAssets
                                        .filter { it.coinId in widgetSelectedIds }
                                        .sumOf { (it.officialSpotPrice * it.amountHeld) + it.premium }
                                    val selectedTotalText =
                                        NumberFormat.getCurrencyInstance(Locale.US).format(selectedTotalValue)
                                    val cleanName = (selectedVault?.name ?: "Portfolio")
                                        .lowercase()
                                        .split(" ")
                                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                        .uppercase()
                                    cleanName to selectedTotalText
                                }.getOrDefault("PORTFOLIO" to NumberFormat.getCurrencyInstance(Locale.US).format(0.0))
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp),
                            ) {
                                WidgetPreviewSlim(
                                    sampleAsset = styleSampleAsset,
                                    vaultName = styleHeaderData.first,
                                    totalValue = styleHeaderData.second,
                                    bgHex = draftBg,
                                    bgTxtHex = draftBgTxt,
                                    cardHex = draftCrd,
                                    cardTxtHex = draftCrdTxt,
                                    showTotal = !draftHideTotals,
                                    isHighVisibilityMode = isHighVisibilityMode,
                                )
                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = safeThemeText.copy(alpha = 0.12f))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(top = 12.dp, bottom = 16.dp),
                                ) {
                                    WidgetStudioInlineCompact(
                                        draftBg,
                                        draftBgTxt,
                                        draftCrd,
                                        draftCrdTxt,
                                    ) { target, newHex ->
                                        when (target) {
                                            0 -> draftBg = newHex
                                            1 -> draftBgTxt = newHex
                                            2 -> draftCrd = newHex
                                            3 -> draftCrdTxt = newHex
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            val previewData = runCatching {
                                val immutableList = orderedWidgetAssets.toList()
                                val finalAssets = immutableList.filter { it.coinId in widgetSelectedIds }.take(5)
                                val selectedTotalValue = immutableList
                                    .filter { it.coinId in widgetSelectedIds }
                                    .sumOf { (it.officialSpotPrice * it.amountHeld) + it.premium }
                                val selectedTotalText =
                                    NumberFormat.getCurrencyInstance(Locale.US).format(selectedTotalValue)
                                val cleanName = (selectedVault?.name ?: "Portfolio")
                                    .lowercase()
                                    .split(" ")
                                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                Triple(cleanName, finalAssets, selectedTotalText)
                            }.getOrNull()
                            if (previewData != null) {
                                val cleanName = previewData.first
                                val finalAssets = previewData.second
                                val selectedTotalText = previewData.third
                                LazyColumn(
                                    state = lazyListStatePreview,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                                ) {
                                    item("wm_preview_hero") {
                                        if (finalAssets.isNotEmpty()) {
                                            HeroWidgetPreview(
                                                vaultName = cleanName.uppercase(),
                                                totalValue = if (!draftHideTotals) selectedTotalText else "",
                                                orderedAssets = finalAssets,
                                                selectedIds = widgetSelectedIds,
                                                bgHex = draftBg,
                                                bgTxtHex = draftBgTxt,
                                                cardHex = draftCrd,
                                                cardTxtHex = draftCrdTxt,
                                                showTotal = !draftHideTotals,
                                                isHighVisibilityMode = isHighVisibilityMode,
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight()
                                                    .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                                                    .padding(vertical = 18.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = safeThemeText)
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(stringResource(R.string.widget_loading_preview), color = safeThemeText.copy(alpha = 0.75f), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                    item("wm_preview_finish") {
                                        if (canOfferSaveAndExit) {
                                            Button(
                                                onClick = runSaveAction,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                                                shape = RoundedCornerShape(999.dp),
                                            ) {
                                                Text(
                                                    stringResource(R.string.widget_finish_save),
                                                    color = Color.Black,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 0.8.sp,
                                                )
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { currentPage = 0 },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(999.dp),
                                            ) {
                                                Text(stringResource(R.string.widget_back_to_setup), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    stringResource(R.string.widget_preview_error),
                                    color = safeThemeText,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetSubPageTabRow(
    current: Int,
    onSelect: (Int) -> Unit,
    themeColor: Color,
) {
    val pages = listOf(0, 1, 2, 3)
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = themeColor.copy(alpha = 0.12f),
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            pages.forEach { page ->
                val selected = page == current
                val label = when (page) {
                    0 -> stringResource(R.string.widget_tab_setup)
                    1 -> stringResource(R.string.widget_tab_assets)
                    2 -> stringResource(R.string.widget_tab_style)
                    else -> stringResource(R.string.widget_tab_preview)
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) themeColor.copy(alpha = 0.2f) else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (selected) themeColor.copy(alpha = 0.45f) else themeColor.copy(alpha = 0.16f),
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onSelect(page) },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            color = themeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioSelectorDropdown(
    vaults: List<VaultEntity>,
    selectedVaultId: Int,
    themeColor: Color,
    isWidgetInstanceContext: Boolean,
    compact: Boolean = false,
    onVaultSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = toDisplayTitleCase(vaults.find { it.id == selectedVaultId }?.name ?: "—")
    val valueSp = if (compact) 11.sp else 14.sp
    val labelSp = if (compact) 7.5.sp else 10.sp
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            textStyle = TextStyle(
                color = themeColor,
                fontSize = valueSp,
                fontWeight = FontWeight.SemiBold,
            ),
            label = {
                Text(
                    if (isWidgetInstanceContext) "PORTFOLIO FOR THIS WIDGET" else "PORTFOLIO",
                    fontSize = labelSp,
                    fontWeight = FontWeight.Black,
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
                .then(if (compact) Modifier.padding(vertical = 2.dp) else Modifier.padding(horizontal = 16.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = themeColor,
                unfocusedTextColor = themeColor,
                focusedBorderColor = themeColor.copy(alpha = 0.45f),
                unfocusedBorderColor = themeColor.copy(alpha = 0.25f),
                focusedLabelColor = themeColor.copy(alpha = 0.75f),
                unfocusedLabelColor = themeColor.copy(alpha = 0.75f),
                cursorColor = themeColor,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            vaults.forEach { v ->
                DropdownMenuItem(
                    text = { Text(toDisplayTitleCase(v.name)) },
                    onClick = {
                        expanded = false
                        onVaultSelected(v.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun HeroWidgetPreview(
    vaultName: String,
    totalValue: String,
    orderedAssets: List<AssetEntity>?,
    selectedIds: List<String>,
    bgHex: String,
    bgTxtHex: String,
    cardHex: String,
    cardTxtHex: String,
    showTotal: Boolean,
    isHighVisibilityMode: Boolean = false,
) {
    val bgColor = try { Color(bgHex.toColorInt()) } catch (e: Exception) { Color(0xFF000416) }
    val bgTextColor = try { Color(bgTxtHex.toColorInt()) } catch (e: Exception) { Color.White }
    val cardColor = try { Color(cardHex.toColorInt()) } catch (e: Exception) { Color(0xFF1C1C1E) }
    val cardTextColor = try { Color(cardTxtHex.toColorInt()) } catch (e: Exception) { Color.White }
    val shownAssets = remember(orderedAssets, selectedIds) {
        (orderedAssets ?: emptyList()).filter { it.coinId in selectedIds }.take(5)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = vaultName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (showTotal && totalValue.isNotBlank()) {
                    Text(
                        text = totalValue,
                        color = bgTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (orderedAssets.isNullOrEmpty() || shownAssets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .background(cardColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Loading...", color = cardTextColor.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        for (asset in shownAssets) {
                            key(asset.coinId) {
                                SimulatedAssetRow(
                                    asset = asset,
                                    cardColor = cardColor,
                                    cardTextColor = cardTextColor,
                                    isHighVisibilityMode = isHighVisibilityMode,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun toDisplayTitleCase(raw: String): String {
    if (raw.isBlank()) return raw
    return raw.lowercase().split(' ').joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
    }
}

@Composable
private fun SimulatedAssetRow(
    asset: AssetEntity,
    cardColor: Color,
    cardTextColor: Color,
    isHighVisibilityMode: Boolean = false,
) {
    val hi = isHighVisibilityMode
    val tickerSize = if (hi) 18.sp else 14.sp
    val subSize = if (hi) 14.sp else 12.sp
    val lineCluster = if (hi) 1.em else 1.32.em
    val platformCluster =
        if (hi) PlatformTextStyle(includeFontPadding = false)
        else PlatformTextStyle(includeFontPadding = true)
    val rowHeight = if (hi) 64.dp else 68.dp
    val textStackGap = if (hi) (-2).dp else 2.dp
    val sparklineH = if (hi) 20.dp else 16.dp
    val sparklineStartPad = if (hi) 6.dp else 10.dp
    val displayPrice = runCatching {
        val safePrice = asset.officialSpotPrice
        if (!safePrice.isFinite()) error("invalid price")
        formatBoutiquePrice(safePrice, "USD")
    }.getOrDefault("N/A")
    val totalValue = runCatching {
        val safePrice = asset.officialSpotPrice
        if (!safePrice.isFinite()) error("invalid price")
        NumberFormat.getCurrencyInstance(Locale.US).format((safePrice * asset.amountHeld) + asset.premium)
    }.getOrDefault("N/A")
    val changeText = runCatching {
        String.format(Locale.US, "%.1f", asset.priceChange24h)
    }.getOrDefault("N/A")
    val trendColor = when {
        asset.priceChange24h > 0 -> Color(0xFF00C853)
        asset.priceChange24h < 0 -> Color(0xFFD32F2F)
        else -> cardTextColor.copy(alpha = 0.7f)
    }
    AssetCardFontScaleScope {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(8.dp))
            .padding(horizontal = if (hi) 10.dp else 14.dp),
    ) {
        Row(
            modifier = Modifier.height(rowHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Lane 1 (left): fixed 100dp for icon + text.
            Row(
                modifier = Modifier
                    .width(100.dp)
                    .padding(start = 2.dp)
                    .align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(cardTextColor.copy(alpha = 0.08f))
                        .padding(3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MetalIcon(
                        name = asset.symbol,
                        weight = asset.weight,
                        unit = asset.weightUnit,
                        physicalForm = asset.physicalForm,
                        size = 24,
                        imageUrl = asset.imageUrl,
                        localPath = asset.localIconPath,
                        category = asset.category,
                    )
                }
                Spacer(Modifier.width(if (hi) 4.dp else 6.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(textStackGap, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.Start,
                ) {
                    val titleText = if (asset.category == AssetCategory.METAL) {
                        asset.displayName.ifEmpty { asset.name }
                    } else {
                        asset.symbol
                    }
                    Text(
                        text = titleText.uppercase(),
                        style = LocalTextStyle.current.merge(
                            TextStyle(
                                color = cardTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = tickerSize,
                                lineHeight = lineCluster,
                                platformStyle = platformCluster,
                            )
                        ),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = displayPrice,
                        style = LocalTextStyle.current.merge(
                            TextStyle(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = subSize,
                                fontWeight = FontWeight.Bold,
                                lineHeight = lineCluster,
                                platformStyle = platformCluster,
                            )
                        ),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Lane 2 (center): flexible center gutter with long-thin sparkline.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = sparklineStartPad)
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center,
            ) {
                SparklineChart(
                    historyData = asset.sparklineData,
                    modifier = Modifier
                        .width(75.dp)
                        .height(sparklineH),
                    lineColorOverride = trendColor,
                )
            }

            // Lane 3 (right): fixed 100dp for total/change.
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(textStackGap, Alignment.CenterVertically),
            ) {
                Text(
                    text = totalValue,
                    style = LocalTextStyle.current.merge(
                        TextStyle(
                            color = cardTextColor,
                            fontSize = tickerSize,
                            fontWeight = FontWeight.Bold,
                            lineHeight = lineCluster,
                            platformStyle = platformCluster,
                        )
                    ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (changeText == "N/A") "N/A" else "${if (asset.priceChange24h > 0) "+" else ""}$changeText%",
                    style = LocalTextStyle.current.merge(
                        TextStyle(
                            color = trendColor.copy(alpha = 0.9f),
                            fontSize = subSize,
                            fontWeight = FontWeight.Bold,
                            lineHeight = lineCluster,
                            platformStyle = platformCluster,
                        )
                    ),
                    maxLines = 1,
                )
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
    isHighVisibilityMode: Boolean = false,
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
            isHighVisibilityMode = isHighVisibilityMode,
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
fun WidgetPreviewSlim(
    sampleAsset: AssetEntity?,
    vaultName: String,
    totalValue: String,
    bgHex: String,
    bgTxtHex: String,
    cardHex: String,
    cardTxtHex: String,
    showTotal: Boolean,
    isHighVisibilityMode: Boolean = false,
) {
    val bgColor = try { Color(bgHex.toColorInt()) } catch(e: Exception) { Color.Black }
    val bgTextColor = try { Color(bgTxtHex.toColorInt()) } catch(e: Exception) { Color.White }
    val cardColor = try { Color(cardHex.toColorInt()) } catch(e: Exception) { Color.DarkGray }
    val cardTextColor = try { Color(cardTxtHex.toColorInt()) } catch(e: Exception) { Color.White }

    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)).padding(8.dp)) {
        Text(
            text = vaultName,
            color = bgTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showTotal) {
            Text(
                text = totalValue,
                color = bgTextColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
        }
        if (sampleAsset != null) {
            SimulatedAssetRow(
                asset = sampleAsset,
                cardColor = cardColor,
                cardTextColor = cardTextColor,
                isHighVisibilityMode = isHighVisibilityMode,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardColor)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.widget_no_assets_available), color = cardTextColor.copy(alpha = 0.7f), fontSize = 10.sp)
            }
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

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            targets.forEachIndexed { i, label ->
                val isSel = activeTarget == i
                Box(Modifier.weight(1f).height(64.dp).background(if(isSel) Color.White.copy(0.08f) else Color.Transparent, RoundedCornerShape(12.dp)).border(1.dp, if(isSel) Color.White.copy(0.45f) else Color.Gray.copy(0.3f), RoundedCornerShape(12.dp)).clickable { activeTarget = i }, contentAlignment = Alignment.Center) {
                    Text(label, color = if(isSel) Color.Yellow else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(0.72f).fillMaxHeight().background(liveColor, RoundedCornerShape(4.dp)).border(1.dp, Color.White, RoundedCornerShape(4.dp)))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1.28f).fillMaxHeight().background(Color.Black, RoundedCornerShape(4.dp)).border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                BasicTextField(value = hexInput, onValueChange = { if (it.length <= 6) hexInput = it.uppercase() }, textStyle = TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = SolidColor(Color.Yellow), singleLine = true)
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(88.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color.Gray, RoundedCornerShape(8.dp))) {
            val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f)))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> saturation = (change.position.x / w).coerceIn(0f, 1f); value = 1f - (change.position.y / h).coerceIn(0f, 1f); hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, saturation, value).toArgb()) } })
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(18.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape)) {
            val w = constraints.maxWidth.toFloat(); val colors = (0..360).map { Color.hsv(it.toFloat(), 1f, 1f) }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(colors)))
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> hue = (change.position.x / w * 360f).coerceIn(0f, 360f); hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, saturation, value).toArgb()) } })
        }
        Button(onClick = {
            onColorChanged(activeTarget, "#$hexInput")
            isFlashing = true; scope.launch { delay(200); isFlashing = false }
            keyboardController?.hide(); focusManager.clearFocus()
        }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isFlashing) Color.White else Color.Yellow)) {
            Text(stringResource(R.string.widget_set_draft_target, targets[activeTarget].uppercase()), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

