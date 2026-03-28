package com.swanie.portfolio.ui.settings

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
import com.swanie.portfolio.data.local.VaultEntity
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
    val defaultVaultId by mainViewModel.defaultVaultId.collectAsStateWithLifecycle()
    val resetToDefault by mainViewModel.resetToDefaultOnStart.collectAsStateWithLifecycle()

    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val safeThemeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    var editingId by remember { mutableIntStateOf(-1) }
    var editName by remember { mutableStateOf("") }
    var vaultToDelete by remember { mutableStateOf<VaultEntity?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PORTFOLIO MANAGER", fontWeight = FontWeight.Black, fontSize = 16.sp, color = safeThemeText) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = safeThemeText) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController, onNavigate = {}) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {

            // Startup Strategy Toggle
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.05f)).clickable { mainViewModel.setResetToDefault(!resetToDefault) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("STARTUP BEHAVIOR", color = Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text(if (resetToDefault) "Always open Default Portfolio" else "Open last viewed portfolio", color = Color.White, fontSize = 13.sp)
                }
                Switch(checked = resetToDefault, onCheckedChange = { mainViewModel.setResetToDefault(it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.Yellow))
            }

            Spacer(Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(allVaults) { vault ->
                    val isSelected = vault.id == activeVault.id
                    val isDefault = vault.id == defaultVaultId

                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (isSelected) safeThemeText.copy(0.1f) else Color.White.copy(0.02f)).border(1.dp, if (isSelected) safeThemeText.copy(0.4f) else Color.White.copy(0.1f), RoundedCornerShape(16.dp)).clickable { mainViewModel.selectVault(vault.id) }.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (editingId == vault.id) {
                                TextField(value = editName, onValueChange = { editName = it }, modifier = Modifier.weight(1f))
                                IconButton(onClick = { mainViewModel.updateVaultName(vault.id, editName); editingId = -1 }) { Icon(Icons.Default.Check, null, tint = Color.Green) }
                            } else {
                                Text(vault.name.uppercase(), color = if (isSelected) Color.White else Color.White.copy(0.6f), fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))

                                // Star Button
                                IconButton(onClick = { mainViewModel.setDefaultVault(vault.id) }) {
                                    Icon(if (isDefault) Icons.Default.Star else Icons.Default.StarBorder, null, tint = if (isDefault) Color.Yellow else Color.White.copy(0.2f))
                                }

                                // Edit Button
                                IconButton(onClick = { editingId = vault.id; editName = vault.name }) { Icon(Icons.Default.Edit, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(18.dp)) }

                                // Delete Button (Safe-guarded)
                                if (allVaults.size > 1) {
                                    IconButton(onClick = { vaultToDelete = vault }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.6f), modifier = Modifier.size(18.dp)) }
                                }
                            }
                        }
                    }
                }
            }

            Button(onClick = { mainViewModel.createNewVault("NEW PORTFOLIO") }, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp).height(56.dp)) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("ADD NEW PORTFOLIO")
            }
        }
    }

    vaultToDelete?.let { vault ->
        AlertDialog(
            onDismissRequest = { vaultToDelete = null },
            title = { Text("DELETE PORTFOLIO?") },
            text = { Text("This will wipe all assets in '${vault.name}'. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { mainViewModel.deleteVault(vault); vaultToDelete = null }) { Text("DELETE", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { vaultToDelete = null }) { Text("CANCEL") } }
        )
    }
}