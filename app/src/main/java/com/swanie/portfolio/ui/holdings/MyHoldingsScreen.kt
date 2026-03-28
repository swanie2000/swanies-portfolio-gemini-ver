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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.swanie.portfolio.data.local.VaultEntity
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
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")

    val remainingCooldown by viewModel.remainingCooldown.collectAsStateWithLifecycle()

    val isDraggingActive = remember { mutableStateOf(false) }
    val isSavingOrder = remember { mutableStateOf(false) }
    val isOverTrash = remember { mutableStateOf(false) }
    val trashBoundsInRoot = remember { mutableStateOf<Rect?>(null) }

    var assetBeingEdited by remember { mutableStateOf<AssetEntity?>(null) }
    var expandedAssetId by remember { mutableStateOf<String?>(null) }
    var showEditButtonId by remember { mutableStateOf<String?>(null) }
    var assetPendingDeletion by remember { mutableStateOf<AssetEntity?>(null) }

    val isViewModelRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var refreshProgress by remember { mutableFloatStateOf(0f) }
    var showScanFlash by remember { mutableStateOf(false) }
    val scanOffset = remember { Animatable(-1f) }

    var isExiting by remember { mutableStateOf(false) }

    val activeVault by mainViewModel.activeVault.collectAsStateWithLifecycle()
    val allVaults by mainViewModel.allVaults.collectAsStateWithLifecycle()
    var showVaultManager by remember { mutableStateOf(false) }

    val filteredHoldings by remember(localHoldings, selectedTab) {
        derivedStateOf {
            when (selectedTab) {
                1 -> localHoldings.filter { it.category == AssetCategory.CRYPTO }
                2 -> localHoldings.filter { it.category == AssetCategory.METAL }
                else -> localHoldings
            }
        }
    }

    val totalValueFormatted by remember(holdings, selectedTab, activeVault.baseCurrency) {
        derivedStateOf {
            val filtered = when (selectedTab) {
                1 -> holdings.filter { it.category == AssetCategory.CRYPTO }
                2 -> holdings.filter { it.category == AssetCategory.METAL }
                else -> holdings
            }
            val total = filtered.sumOf { asset ->
                (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
            }
            formatCurrency(total, 2, activeVault.baseCurrency)
        }
    }

    LaunchedEffect(holdings) {
        if (!isDraggingActive.value && !isSavingOrder.value && assetBeingEdited == null) {
            localHoldings = holdings
        }
    }

    LaunchedEffect(isViewModelRefreshing) {
        if (isViewModelRefreshing) {
            showScanFlash = true
            scope.launch {
                while(isViewModelRefreshing) {
                    scanOffset.animateTo(2f, tween(1200, easing = LinearOutSlowInEasing))
                    scanOffset.snapTo(-1f)
                }
                showScanFlash = false
            }
            scope.launch {
                var progress = 0.1f
                while(isViewModelRefreshing) {
                    refreshProgress = progress
                    delay(100)
                    progress = if (progress < 0.9f) progress + 0.05f else 0.85f
                }
                refreshProgress = 1.0f
                delay(500)
                refreshProgress = 0f
            }
        }
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val newList = localHoldings.toMutableList()
        val indexFrom = newList.indexOfFirst { it.coinId == filteredHoldings[from.index].coinId }
        val indexTo = newList.indexOfFirst { it.coinId == filteredHoldings[to.index].coinId }
        if (indexFrom != -1 && indexTo != -1) {
            newList.add(indexTo, newList.removeAt(indexFrom))
            localHoldings = newList
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDarkTheme
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: continue
                trashBoundsInRoot.value?.let { bounds -> isOverTrash.value = isDraggingActive.value && bounds.contains(change.position) }
            }
        }
    }) {
        if (showScanFlash && !isExiting) {
            Box(modifier = Modifier.fillMaxSize().zIndex(999f).background(Brush.verticalGradient(0f to Color.Transparent, 0.45f to Color.White.copy(0.15f), 0.5f to Color.White.copy(0.35f), 0.55f to Color.White.copy(0.15f), 1f to Color.Transparent, startY = scanOffset.value * 2000f, endY = (scanOffset.value * 2000f) + 600f)))
        }

        if (!isExiting) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(Color.Transparent).statusBarsPadding()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.swanie_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).align(Alignment.Center)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (!isViewModelRefreshing && remainingCooldown <= 0) viewModel.refreshAssets() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                if (remainingCooldown > 0 && !isViewModelRefreshing) {
                                    Text("${remainingCooldown}s", color = textColor.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Refresh, null, tint = if(isViewModelRefreshing || remainingCooldown > 0) textColor.copy(0.2f) else textColor)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = { mainViewModel.toggleCompactView() }, modifier = Modifier.size(44.dp)) {
                                Icon(if (isCompactViewEnabled) Icons.Default.ViewModule else Icons.AutoMirrored.Filled.ViewList, null, tint = textColor)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(onClick = { isExiting = true; navController.navigate(Routes.ASSET_PICKER) }, modifier = Modifier.clip(CircleShape).background(Color.Yellow).size(44.dp)) {
                                Icon(Icons.Default.Add, null, tint = Color.Black)
                            }
                        }

                        if (refreshProgress > 0f) {
                            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 2.dp), contentAlignment = Alignment.Center) {
                                LinearProgressIndicator(
                                    progress = { refreshProgress },
                                    modifier = Modifier.width(100.dp).height(4.dp).clip(CircleShape),
                                    color = textColor.copy(0.7f),
                                    trackColor = textColor.copy(0.05f)
                                )
                            }
                        }
                    }
                }

                // --- THE PORTFOLIO HANDLE BOX ---
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, cardText.copy(0.1f), RoundedCornerShape(16.dp))
                        .clickable { showVaultManager = true }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activeVault.name.uppercase(),
                            color = cardText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            null,
                            tint = cardText.copy(0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = totalValueFormatted,
                        color = cardText.copy(alpha = 0.7f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable { isExiting = true; navController.navigate(Routes.ANALYTICS) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    TabRow(selectedTabIndex = selectedTab, modifier = Modifier.weight(1f).height(40.dp), containerColor = Color.Transparent, indicator = { }, divider = { }) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Tab(selected = isSelected, onClick = { selectedTab = index }, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp).clip(CircleShape).background(if (isSelected) textColor.copy(0.15f) else Color.Transparent).border(1.dp, if (isSelected) Color.Transparent else textColor.copy(0.15f), CircleShape)) {
                                Text(text = title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) textColor else textColor.copy(0.5f), modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                    AnimatedVisibility(visible = selectedTab == 2) {
                        IconButton(onClick = { isExiting = true; navController.navigate(Routes.METALS_AUDIT) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Shield, null, tint = Color.Yellow, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                AnimatedContent(
                    targetState = isCompactViewEnabled,
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    label = "ViewToggle"
                ) { targetIsCompact ->
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = filteredHoldings, key = { it.coinId }) { asset ->
                            ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
                                val isExpanded = expandedAssetId == asset.coinId
                                val isEditButtonVisible = showEditButtonId == asset.coinId
                                val handleExpandToggle = {
                                    if (targetIsCompact) {
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
                                        else { scope.launch { isSavingOrder.value = true; viewModel.updateAssetOrder(localHoldings); delay(800); isSavingOrder.value = false } }
                                    }
                                )

                                if (targetIsCompact && !isExpanded) {
                                    CompactAssetCard(asset, isDragging, cardBg, cardText, activeVault.baseCurrency, handleExpandToggle, dragModifier)
                                } else {
                                    FullAssetCard(asset, isExpanded, false, isDragging, isEditButtonVisible, cardBg, cardText, activeVault.baseCurrency, handleExpandToggle, { assetBeingEdited = asset }, { _, _, _, _, _ -> }, modifier = dragModifier)
                                }
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
                CryptoEditFunnel(
                    asset = asset,
                    onDismiss = { assetBeingEdited = null },
                    onSave = { newName, newAmount, newDecimals ->
                        viewModel.updateAsset(asset, newName, newAmount, asset.weight, asset.weightUnit, newDecimals)
                        assetBeingEdited = null
                    }
                )
            } else {
                MetalSelectionFunnel(
                    initialMetal = asset.symbol,
                    initialForm = asset.name,
                    initialWeight = asset.weight,
                    initialQty = asset.amountHeld.toString(),
                    initialPrem = asset.premium.toString(),
                    initialManualPrice = asset.officialSpotPrice.toString(),
                    onDismiss = { assetBeingEdited = null },
                    onConfirmed = { type, desc, weight, unit, qty, prem, icon, isManual, manualPrice ->
                        viewModel.updateAssetEntity(asset.copy(
                            symbol = type,
                            name = desc,
                            weight = weight,
                            weightUnit = unit,
                            amountHeld = qty.toDoubleOrNull() ?: 0.0,
                            premium = prem.toDoubleOrNull() ?: 0.0,
                            imageUrl = icon ?: asset.imageUrl,
                            officialSpotPrice = if(isManual) (manualPrice.toDoubleOrNull() ?: 0.0) else asset.officialSpotPrice
                        ))
                        assetBeingEdited = null
                    }
                )
            }
        }

        assetPendingDeletion?.let { asset ->
            AlertDialog(
                onDismissRequest = { assetPendingDeletion = null },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("PURGE ASSET?", color = Color.Red, fontWeight = FontWeight.Black) },
                text = { Text("This will permanently remove ${asset.displayName.ifEmpty { asset.name }} from your vault. Proceed?", color = Color.White.copy(0.7f)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteAsset(asset)
                        assetPendingDeletion = null
                    }) { Text("DELETE", color = Color.Red, fontWeight = FontWeight.Black) }
                },
                dismissButton = {
                    TextButton(onClick = { assetPendingDeletion = null }) { Text("CANCEL", color = Color.White) }
                }
            )
        }

        if (showVaultManager) {
            VaultManagerDialog(
                allVaults = allVaults,
                activeVault = activeVault,
                onDismiss = { showVaultManager = false },
                onSelectVault = { vaultId: Int -> mainViewModel.selectVault(vaultId); showVaultManager = false },
                onRenameVault = { id: Int, name: String -> mainViewModel.updateVaultName(id, name) },
                onUpdateCurrency = { id: Int, code: String -> mainViewModel.updateVaultCurrency(id, code) },
                onCreateVault = { name: String -> mainViewModel.createNewVault(name) }
            )
        }
    }
}

@Composable
fun VaultManagerDialog(
    allVaults: List<VaultEntity>,
    activeVault: VaultEntity,
    onDismiss: () -> Unit,
    onSelectVault: (Int) -> Unit,
    onRenameVault: (Int, String) -> Unit,
    onUpdateCurrency: (Int, String) -> Unit,
    onCreateVault: (String) -> Unit
) {
    var isCreating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var editingId by remember { mutableIntStateOf(-1) }
    var editName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF000416))
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("VAULT MANAGER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                    items(allVaults) { vault ->
                        val isSelected = vault.id == activeVault.id
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color.White.copy(0.1f) else Color.Transparent)
                                .border(1.dp, if (isSelected) Color.White.copy(0.3f) else Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                                .clickable { onSelectVault(vault.id) }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (editingId == vault.id) {
                                    TextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        modifier = Modifier.weight(1f),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, cursorColor = Color.Yellow)
                                    )
                                    IconButton(onClick = { onRenameVault(vault.id, editName); editingId = -1 }) {
                                        Icon(Icons.Default.Check, null, tint = Color.Green)
                                    }
                                } else {
                                    Text(vault.name, color = if (isSelected) Color.White else Color.White.copy(0.6f), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { editingId = vault.id; editName = vault.name }) {
                                        Icon(Icons.Default.Edit, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            if (isSelected && editingId != vault.id) {
                                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("USD", "EUR", "GBP").forEach { code ->
                                        val isCurrent = vault.baseCurrency == code
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isCurrent) Color.Yellow else Color.White.copy(0.05f))
                                                .border(1.dp, if (isCurrent) Color.Transparent else Color.White.copy(0.1f), CircleShape)
                                                .clickable { onUpdateCurrency(vault.id, code) }
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = code, color = if (isCurrent) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isCreating) {
                    TextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Vault Name...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, cursorColor = Color.Yellow)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isCreating = false }) { Text("CANCEL", color = Color.White) }
                        Button(onClick = { onCreateVault(newName); isCreating = false; newName = "" }, colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) {
                            Text("CREATE", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { isCreating = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("CREATE NEW VAULT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}