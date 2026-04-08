@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
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

    var editingId by remember { mutableIntStateOf(-1) }
    var editName by remember { mutableStateOf("") }
    var vaultToDelete by remember { mutableStateOf<VaultEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // --- 🦢 HEADER (Holdings Blueprint) ---
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

                    // 🟡 THE ADD BUTTON
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

            // --- ⭐ CENTERED VISUAL LEGEND ⭐ ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center, // Center the legend
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Indicates Startup Portfolio",
                    color = safeThemeText.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(20.dp)
                )
            }

            // --- 🌐 THE SCROLLABLE LIST ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 400.dp)
            ) {
                items(allVaults) { vault ->
                    val isSelected = vault.id == activeVault.id
                    val isDefault = vault.id == defaultVaultId

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) safeThemeText.copy(0.1f) else Color.White.copy(0.02f))
                            .border(
                                1.dp,
                                if (isSelected) safeThemeText.copy(0.4f) else safeThemeText.copy(0.1f),
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
                                            if (focusState.isFocused && editName.uppercase() == "NEW PORTFOLIO") {
                                                editName = ""
                                            }
                                        },
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = safeThemeText),
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = safeThemeText,
                                        unfocusedTextColor = safeThemeText,
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
                                Text(
                                    vault.name.uppercase(),
                                    color = if (isSelected) safeThemeText else safeThemeText.copy(0.6f),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )

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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(onClick = { editingId = vault.id; editName = vault.name }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Edit, null, tint = safeThemeText.copy(0.4f), modifier = Modifier.size(16.dp))
                                }

                                if (allVaults.size > 1) {
                                    IconButton(onClick = { vaultToDelete = vault }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    vaultToDelete?.let { vault ->
        AlertDialog(
            onDismissRequest = { vaultToDelete = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("DELETE PORTFOLIO?", color = Color.Red, fontWeight = FontWeight.Black) },
            text = { Text("Permanently wipe '${vault.name}'?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = { mainViewModel.deleteVault(vault); vaultToDelete = null }) {
                    Text("DELETE", color = Color.Red)
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