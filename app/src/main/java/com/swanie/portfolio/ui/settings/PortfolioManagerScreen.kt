package com.swanie.portfolio.ui.settings

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.ui.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioManagerScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val allVaults by mainViewModel.allVaults.collectAsStateWithLifecycle()
    val activeVault by mainViewModel.activeVault.collectAsStateWithLifecycle()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val safeThemeText = try { Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt()) } catch (e: Exception) { Color.White }

    var isCreating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var editingId by remember { mutableIntStateOf(-1) }
    var editName by remember { mutableStateOf("") }
    var isExiting by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MANAGE PORTFOLIOS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = safeThemeText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = safeThemeText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (!isExiting) {
                BottomNavigationBar(
                    navController = navController,
                    onNavigate = { isExiting = true }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Premium Incentive Header (Placeholder for later Tier logic)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Yellow.copy(0.1f))
                    .border(1.dp, Color.Yellow.copy(0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color.Yellow, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "PREMIUM FEATURE: UNLIMITED VAULTS",
                        color = Color.Yellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allVaults) { vault ->
                    val isSelected = vault.id == activeVault.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) safeThemeText.copy(0.1f) else Color.White.copy(0.02f))
                            .border(
                                1.dp,
                                if (isSelected) safeThemeText.copy(0.4f) else Color.White.copy(0.1f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { mainViewModel.selectVault(vault.id) }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (editingId == vault.id) {
                                TextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        cursorColor = Color.Yellow
                                    )
                                )
                                IconButton(onClick = {
                                    mainViewModel.updateVaultName(vault.id, editName)
                                    editingId = -1
                                }) {
                                    Icon(Icons.Default.Check, null, tint = Color.Green)
                                }
                            } else {
                                Text(
                                    vault.name.uppercase(),
                                    color = if (isSelected) Color.White else Color.White.copy(0.6f),
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp
                                )
                                IconButton(onClick = {
                                    editingId = vault.id
                                    editName = vault.name
                                }) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("USD", "EUR", "GBP").forEach { code ->
                                val isCurrent = vault.baseCurrency == code
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isCurrent) Color.Yellow else Color.White.copy(0.05f))
                                        .border(1.dp, if (isCurrent) Color.Transparent else Color.White.copy(0.1f), CircleShape)
                                        .clickable { mainViewModel.updateVaultCurrency(vault.id, code) }
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = code,
                                        color = if (isCurrent) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Create Section at bottom
            if (isCreating) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TextField(
                            value = newName,
                            onValueChange = { newName = it },
                            placeholder = { Text("Vault Name...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isCreating = false }) { Text("CANCEL", color = Color.White) }
                            Button(
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        mainViewModel.createNewVault(newName)
                                        isCreating = false
                                        newName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)
                            ) {
                                Text("CREATE", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Button(
                    onClick = { isCreating = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("ADD NEW PORTFOLIO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}