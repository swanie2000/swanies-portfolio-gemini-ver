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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.ui.features.AuthViewModel
import com.swanie.portfolio.ui.navigation.Routes
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
    val activity = LocalContext.current as androidx.fragment.app.FragmentActivity
    val authViewModel: AuthViewModel = hiltViewModel(activity)
    val allVaults by mainViewModel.allVaults.collectAsStateWithLifecycle()
    val activeVault by mainViewModel.activeVault.collectAsStateWithLifecycle()
    val starredVaultId by mainViewModel.starredVaultId.collectAsStateWithLifecycle()

    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val safeThemeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val dialogBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val goldPrimary = Color.Yellow
    val nightVaultColor = Color(0xFF121212)

    val scope = rememberCoroutineScope() // Scope for async tasks
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedVaultForEdit by remember { mutableStateOf<VaultEntity?>(null) }
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
        if (!isImeVisible && !showEditDialog) {
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
                    .padding(top = 8.dp, bottom = 10.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = safeThemeText)
                }
                Column(
                    modifier = Modifier.align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.swanie_foreground),
                        contentDescription = null,
                        modifier = Modifier.height(80.dp)
                    )
                    Text(
                        text = stringResource(R.string.portfolio_manager_title),
                        color = safeThemeText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(goldPrimary)
                        .clickable { mainViewModel.createNewVault(activity.getString(R.string.portfolio_new_default_name)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
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
                    text = stringResource(R.string.portfolio_startup_indicator),
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
                        val isDefault = vault.id == starredVaultId

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
                                .clickable { 
                                    if (vault.id != activeVault.id) {
                                        authViewModel.setLocked()
                                        mainViewModel.selectVault(vault.id) 
                                        navController.navigate(Routes.UNLOCK_VAULT)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedVaultForEdit = vault
                                            editName = vault.name
                                            showEditDialog = true
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
            containerColor = dialogBg,
            title = { Text(stringResource(R.string.portfolio_delete_title), color = Color.Red, fontWeight = FontWeight.Black) },
            text = { Text(stringResource(R.string.portfolio_delete_body, vault.name), color = safeThemeText) },
            confirmButton = {
                if (allVaults.size > 1) {
                    TextButton(onClick = { mainViewModel.deleteVault(vault); vaultToDelete = null }) {
                        Text(stringResource(R.string.action_delete), color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { vaultToDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = safeThemeText)
                }
            }
        )
    }

    if (showEditDialog && selectedVaultForEdit != null) {
        BasicAlertDialog(
            onDismissRequest = {
                showEditDialog = false
                selectedVaultForEdit = null
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(dialogBg)
                    .border(1.dp, safeThemeText.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.portfolio_edit_title),
                    color = safeThemeText,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                TextField(
                    value = editName,
                    onValueChange = { editName = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        color = safeThemeText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = safeThemeText
                    )
                )
                Button(
                    onClick = {
                        selectedVaultForEdit?.let { target ->
                            mainViewModel.updateVaultName(
                                target.id,
                                editName.ifBlank { activity.getString(R.string.portfolio_fallback_name) }
                            )
                        }
                        showEditDialog = false
                        selectedVaultForEdit = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = goldPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}