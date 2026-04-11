@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.swanie.portfolio.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Changed to itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.VaultEntity
import kotlinx.coroutines.launch // Required for auto-scroll
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun PortfolioManagerScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val allVaults by mainViewModel.allVaults.collectAsStateWithLifecycle()
    val activeVault by mainViewModel.activeVault.collectAsStateWithLifecycle()
    val defaultVaultId by mainViewModel.defaultVaultId.collectAsStateWithLifecycle()

    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val safeThemeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    val scope = rememberCoroutineScope() // Scope for auto-scrolling
    var editingId by remember { mutableIntStateOf(-1) }
    var editName by remember { mutableStateOf("") }
    var vaultToDelete by remember { mutableStateOf<VaultEntity?>(null) }

    val haptic = LocalHapticFeedback.current
    val trashBoundsInRoot = remember { mutableStateOf<Rect?>(null) }
    val isDraggingActive = remember { mutableStateOf(false) }
    val isOverTrash = remember { mutableStateOf(false) }

    var localVaults by remember { mutableStateOf(allVaults) }
    LaunchedEffect(allVaults) {
        if (!isDraggingActive.value) { localVaults = allVaults }
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            localVaults = localVaults.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    val isImeVisible = WindowInsets.isImeVisible
    
    // 🚀 KEYBOARD RESET: Reset scroll or handle state when keyboard closes
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible && editingId == -1) {
            // Optional: Snap back or clear specific UI states
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // --- 🦢 HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(100.dp)
                    .zIndex(10f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.swanie_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).align(Alignment.Center)
                )

                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = safeThemeText)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { mainViewModel.createNewVault("NEW PORTFOLIO") },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Yellow)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.Black)
                    }
                }
            }

            // --- ⭐ LEGEND ⭐ ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = Color.Yellow, modifier = Modifier.size(30.dp))
                Text(
                    text = "Indicates Startup Portfolio",
                    color = safeThemeText.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Icon(Icons.Default.Star, null, tint = Color.Yellow, modifier = Modifier.size(30.dp))
            }

            // --- 🌐 REORDERABLE LIST ---
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f).imePadding(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                itemsIndexed(localVaults, key = { _, vault -> vault.id }) { index, vault ->
                    ReorderableItem(reorderableState, key = vault.id) { isDragging ->
                        val isSelected = vault.id == activeVault.id
                        val isDefault = vault.id == defaultVaultId

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .longPressDraggableHandle(
                                    onDragStarted = {
                                        isDraggingActive.value = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragStopped = {
                                        if (isOverTrash.value) { vaultToDelete = vault }
                                        isDraggingActive.value = false
                                        mainViewModel.updateVaultOrder(localVaults)
                                    }
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDragging) safeThemeText.copy(0.2f)
                                    else if (isSelected) safeThemeText.copy(0.1f)
                                    else Color.White.copy(0.02f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected || isDragging) safeThemeText.copy(0.4f) else safeThemeText.copy(0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { mainViewModel.selectVault(vault.id) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (editingId == vault.id) {
                                    TextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    // 🛡️ AUTO-CLEAR
                                                    if (editName.uppercase() == "NEW PORTFOLIO") { editName = "" }
                                                    // 🚀 AUTO-SCROLL: Jump to top of keyboard
                                                    scope.launch {
                                                        lazyListState.animateScrollToItem(index)
                                                    }
                                                }
                                            },
                                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = safeThemeText),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            cursorColor = safeThemeText
                                        )
                                    )
                                    IconButton(onClick = {
                                        mainViewModel.updateVaultName(vault.id, editName.ifBlank { "PORTFOLIO" })
                                        editingId = -1
                                    }) {
                                        Icon(Icons.Default.Check, null, tint = Color.Green)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                editingId = vault.id
                                                editName = vault.name
                                            }
                                    ) {
                                        Text(
                                            vault.name.uppercase(),
                                            color = if (isSelected) safeThemeText else safeThemeText.copy(0.6f),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            mainViewModel.setDefaultVault(vault.id)
                                            mainViewModel.setResetToDefault(true)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (isDefault) Icons.Default.Star else Icons.Default.StarBorder,
                                            null,
                                            tint = if (isDefault) Color.Yellow else safeThemeText.copy(0.2f),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 🛡️ TRASH ZONE ---
        AnimatedVisibility(
            visible = isDraggingActive.value,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 40.dp).zIndex(100f)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    trashBoundsInRoot.value = Rect(pos.x, pos.y, pos.x + coords.size.width, pos.y + coords.size.height)
                }
        ) {
            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape)
                    .background(if (isOverTrash.value) Color.Red else Color.DarkGray.copy(0.9f))
                    .border(3.dp, if (isOverTrash.value) Color.White else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
    }

    // --- DELETE DIALOG ---
    vaultToDelete?.let { vault ->
        AlertDialog(
            onDismissRequest = { vaultToDelete = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("DELETE PORTFOLIO?", color = Color.Red, fontWeight = FontWeight.Black) },
            text = { Text("Permanently wipe '${vault.name}'?", color = Color.White) },
            confirmButton = {
                if (allVaults.size > 1) {
                    TextButton(onClick = { mainViewModel.deleteVault(vault); vaultToDelete = null }) {
                        Text("DELETE", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { vaultToDelete = null }) {
                    Text("CANCEL", color = Color.White)
                }
            }
        )
    }
}