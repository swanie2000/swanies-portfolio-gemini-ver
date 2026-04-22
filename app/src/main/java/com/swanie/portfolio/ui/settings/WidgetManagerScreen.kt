@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.settings

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.ui.components.BoutiqueHeader
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.MetalIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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

    val vaults by settingsViewModel.allVaults.collectAsStateWithLifecycle(initialValue = emptyList())
    
    // 🎯 DRAFT STATE: Hoisted to the top for scope visibility
    var draftBg by rememberSaveable { mutableStateOf("#1C1C1E") }
    var draftBgTxt by rememberSaveable { mutableStateOf("#FFFFFF") }
    var draftCrd by rememberSaveable { mutableStateOf("#2C2C2E") }
    var draftCrdTxt by rememberSaveable { mutableStateOf("#FFFFFF") }
    var draftSelectedIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var draftHideTotals by remember { mutableStateOf(false) }

    // 🎯 REGISTRY STATE: The target vault for configuration
    val targetVaultId by settingsViewModel.targetVaultId.collectAsStateWithLifecycle()
    val selectedVault by settingsViewModel.targetVault.collectAsStateWithLifecycle()
    val targetAssets by settingsViewModel.targetVaultAssets.collectAsStateWithLifecycle()
    val widgetSelectedIds by assetViewModel.widgetSelectedAssetIds.collectAsStateWithLifecycle()
    var orderedWidgetAssets by remember { mutableStateOf<List<AssetEntity>>(emptyList()) }

    // 🚀 CONFIG MODE AUTO-SELECT: Handled via ViewModel's forceVaultSwitch on entry
    // (Actual logic moved to Activity for Intent-level control, but kept here as fallback)
    LaunchedEffect(configAppWidgetId) {
        if (targetVaultId == -1 && configAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            settingsViewModel.forceVaultSwitch(configAppWidgetId, isAppWidgetId = true)
        } else if (targetVaultId == -1) {
            settingsViewModel.forceVaultSwitch(1)
        }
    }

    // 🎯 INITIALIZATION LOCK: Force draft states to match selected vault on every ID change
    LaunchedEffect(selectedVault?.id) {
        selectedVault?.let {
            draftBg = it.widgetBgColor.takeIf { c -> c.isNotEmpty() } ?: "#1C1C1E"
            draftBgTxt = it.widgetBgTextColor.takeIf { c -> c.isNotEmpty() } ?: "#FFFFFF"
            draftCrd = it.widgetCardColor.takeIf { c -> c.isNotEmpty() } ?: "#2C2C2E"
            draftCrdTxt = it.widgetCardTextColor.takeIf { c -> c.isNotEmpty() } ?: "#FFFFFF"
            draftSelectedIds = it.selectedWidgetAssets.split(",").filter { id -> id.isNotBlank() }
            draftHideTotals = !it.showWidgetTotal
        }
    }

    LaunchedEffect(selectedVault?.id, targetAssets, widgetSelectedIds) {
        if (selectedVault == null) {
            orderedWidgetAssets = emptyList()
            return@LaunchedEffect
        }

        val filteredIds = widgetSelectedIds.filter { id -> targetAssets.any { it.coinId == id } }
        val orderedFromSelection = filteredIds.mapNotNull { id -> targetAssets.find { it.coinId == id } }
        val missingAssets = targetAssets.filter { asset -> filteredIds.none { it == asset.coinId } }
        val resolvedOrder = orderedFromSelection + missingAssets
        val resolvedIds = resolvedOrder.map { it.coinId }

        orderedWidgetAssets = resolvedOrder
        draftSelectedIds = resolvedIds

        val shouldSync =
            resolvedIds.isNotEmpty() && resolvedIds != widgetSelectedIds ||
                (targetAssets.isNotEmpty() && widgetSelectedIds.isEmpty())
        if (shouldSync) {
            assetViewModel.updateWidgetSelectionForCurrentVault(resolvedIds)
        }
    }

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsStateWithLifecycle(initialValue = "#000416")
    val siteTextColor by themeViewModel.siteTextColor.collectAsStateWithLifecycle(initialValue = "#FFFFFF")

    val isDirty by remember(selectedVault, draftBg, draftBgTxt, draftCrd, draftCrdTxt, draftSelectedIds, draftHideTotals) {
        derivedStateOf {
            selectedVault?.let {
                val originalIds = it.selectedWidgetAssets.split(",").filter { id -> id.isNotBlank() }
                val originalHideTotals = !it.showWidgetTotal

                draftBg != it.widgetBgColor ||
                        draftBgTxt != it.widgetBgTextColor ||
                        draftCrd != it.widgetCardColor ||
                        draftCrdTxt != it.widgetCardTextColor ||
                        draftSelectedIds != originalIds ||
                        draftHideTotals != originalHideTotals
            } ?: false
        }
    }

    fun revertChanges() {
        selectedVault?.let {
            draftSelectedIds = it.selectedWidgetAssets.split(",").filter { it.isNotBlank() }
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

    val lazyListState = rememberLazyListState()
    val assetsListState = rememberLazyListState()
    val reorderableAssetsState = rememberReorderableLazyListState(
        lazyListState = assetsListState,
        onMove = { from, to ->
            orderedWidgetAssets = orderedWidgetAssets.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            draftSelectedIds = orderedWidgetAssets.map { it.coinId }
        }
    )
    
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

                // --- 🦢 BOUTIQUE HEADER ---
                BoutiqueHeader(
                    title = if (isConfigMode) "WIDGET CONFIG" else "WIDGET MANAGER",
                    onBack = { onBack() },
                    actionIcon = if (isDirty || (isConfigMode && selectedVault != null)) Icons.Default.Save else Icons.Default.Undo,
                    onAction = {
                        if (isDirty || (isConfigMode && selectedVault != null)) {
                            if (cooldownSeconds == 0 && selectedVault != null) {
                                scope.launch {
                                    val targetId = selectedVault!!.id
                                    val currentWidgetId = if (isConfigMode) configAppWidgetId else null

                                    // 🛡️ REGISTRATION LOCK: Links the hardware appWidgetId to the VaultEntity in Room.
                                    settingsViewModel.saveWidgetConfiguration(targetId, currentWidgetId, draftSelectedIds) {
                                        scope.launch {
                                            settingsViewModel.saveWidgetAppearance(
                                                targetId,
                                                draftBg,
                                                draftBgTxt,
                                                draftCrd,
                                                draftCrdTxt
                                            )
                                            // Privacy is now vault-specific
                                            settingsViewModel.updateShowWidgetTotal(targetId, !draftHideTotals)

                                            // Hard refresh to sync state
                                            settingsViewModel.getVaultById(targetId)

                                            // 🚀 DIRECT DRAW: Manually push RemoteViews for instant feedback
                                            if (currentWidgetId != null && currentWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                                settingsViewModel.forceImmediateRemoteViewsUpdate(targetId, currentWidgetId)
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

                // 🎯 PORTFOLIO REGISTRY SELECTOR (Always Visible Safety Valve)
                PortfolioSelectorDropdown(
                    vaults = vaults,
                    selectedVaultId = targetVaultId,
                    onVaultSelected = { id ->
                        settingsViewModel.forceVaultSwitch(id)
                    },
                    themeColor = safeThemeText
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
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(bottom = 0.dp)
                    ) {
                        item {
                            WidgetPreviewSlim(bgHex = draftBg, bgTxtHex = draftBgTxt, cardHex = draftCrd, cardTxtHex = draftCrdTxt, showTotal = !draftHideTotals)
                        }

                        item {
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

                        item {
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { draftHideTotals = !draftHideTotals }, verticalAlignment = Alignment.CenterVertically) {
                                Text("Hide Numbers", color = safeThemeText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Checkbox(checked = draftHideTotals, onCheckedChange = { draftHideTotals = it }, colors = CheckboxDefaults.colors(checkedColor = safeThemeText))
                            }
                            HorizontalDivider(color = safeThemeText.copy(0.1f), thickness = 1.dp)
                        }

                        item {
                            val countText = "${orderedWidgetAssets.size} ORDERED"
                            SectionHeaderSmall("ASSETS ($countText)", assetsExpanded, safeThemeText) { assetsExpanded = !assetsExpanded }
                        }

                        if (assetsExpanded) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = try { Color(draftCrd.toColorInt()).copy(alpha = 0.5f) } catch(e: Exception) { Color.DarkGray.copy(alpha = 0.5f) }),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, safeThemeText.copy(alpha = 0.1f))
                                ) {
                                    Column(Modifier.padding(8.dp)) {
                                        if (orderedWidgetAssets.isEmpty()) {
                                            Text("No assets in this portfolio", color = safeThemeText.copy(0.4f), modifier = Modifier.padding(16.dp))
                                        } else {
                                            LazyColumn(
                                                state = assetsListState,
                                                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                                userScrollEnabled = true
                                            ) {
                                                items(orderedWidgetAssets, key = { it.coinId }) { asset ->
                                                    ReorderableItem(reorderableAssetsState, key = asset.coinId) { isDragging ->
                                                        WidgetAssetReorderItem(
                                                            asset = asset,
                                                            orderIndex = orderedWidgetAssets.indexOfFirst { it.coinId == asset.coinId } + 1,
                                                            cardModifier = Modifier.longPressDraggableHandle(
                                                                onDragStarted = {
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                },
                                                                onDragStopped = {
                                                                    val reorderedIds = orderedWidgetAssets.map { it.coinId }
                                                                    draftSelectedIds = reorderedIds
                                                                    assetViewModel.updateWidgetSelectionForCurrentVault(reorderedIds)
                                                                }
                                                            ),
                                                            isDragging = isDragging,
                                                            themeColor = safeThemeText
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioSelectorDropdown(
    vaults: List<VaultEntity>,
    selectedVaultId: Int,
    onVaultSelected: (Int) -> Unit,
    themeColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedVault = vaults.find { it.id == selectedVaultId } ?: vaults.firstOrNull()

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Pick a Portfolio to edit", color = themeColor.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            TextField(
                value = selectedVault?.name?.uppercase() ?: "SELECT VAULT",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(0.05f),
                    focusedContainerColor = Color.White.copy(0.1f),
                    unfocusedTextColor = themeColor,
                    focusedTextColor = themeColor,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Yellow
                ),
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(fontWeight = FontWeight.Black, fontSize = 14.sp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1C1C1E))
            ) {
                vaults.forEach { vault ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                vault.name.uppercase(),
                                color = if (vault.id == selectedVaultId) Color.Yellow else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onClick = {
                            onVaultSelected(vault.id)
                            expanded = false
                        }
                    )
                }
            }
        }
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

@Composable
fun WidgetAssetReorderItem(
    asset: AssetEntity,
    orderIndex: Int,
    cardModifier: Modifier,
    isDragging: Boolean,
    themeColor: Color
) {
    val liftScale = animateFloatAsState(
        targetValue = if (isDragging) 1.02f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "widgetCardLiftScale"
    )

    Row(
        modifier = cardModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDragging) themeColor.copy(0.08f) else Color.Transparent)
            .border(1.dp, if (isDragging) themeColor.copy(0.3f) else themeColor.copy(0.05f), RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
            .graphicsLayer {
                scaleX = liftScale.value
                scaleY = liftScale.value
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (asset.category == AssetCategory.METAL) {
                MetalIcon(
                    name = asset.symbol,
                    weight = asset.weight,
                    unit = asset.weightUnit,
                    physicalForm = asset.physicalForm,
                    size = 20
                )
            } else {
                AsyncImage(model = asset.imageUrl, contentDescription = asset.name, modifier = Modifier.size(20.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = asset.symbol.uppercase(), color = themeColor, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(text = asset.name.replace("\n", " "), color = themeColor.copy(0.6f), fontSize = 11.sp)
        }
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.Yellow),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = orderIndex.toString(),
                color = Color.Black,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}
