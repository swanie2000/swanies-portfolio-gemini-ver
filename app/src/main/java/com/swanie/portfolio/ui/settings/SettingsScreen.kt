package com.swanie.portfolio.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    mainViewModel: MainViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isCompactMode by settingsViewModel.isCompactViewEnabled.collectAsState()
    val isHighVisibilityMode by settingsViewModel.isHighVisibilityMode.collectAsState()
    val confirmDelete by mainViewModel.confirmDelete.collectAsState(initial = true)
    val currentVaultId by mainViewModel.currentVaultId.collectAsState(initial = 1)
    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()

    val useGradient by mainViewModel.useGradient.collectAsState()
    val gradientAmount by mainViewModel.gradientAmount.collectAsState()

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    var isExiting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showFactoryResetDialog by remember { mutableStateOf(false) }

    if (showFactoryResetDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            title = {
                Text(
                    text = "RESET VAULT TO FACTORY?",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "This will completely wipe the current vault, including all assets and history. This action is sovereign and permanent.",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            settingsViewModel.factoryResetAllData()
                            showFactoryResetDialog = false
                            isExiting = true
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) {
                    Text("RESET EVERYTHING", color = Color.Red, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetDialog = false }) {
                    Text("CANCEL", color = safeText.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF1C1C1E)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (!isExiting) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp)) {
                    Text(text = "SETTINGS", color = safeText, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {

                    // --- SECURITY ---
                    Text("SECURITY", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    SettingsCheckboxItem(
                        title = "LOGIN OPTION",
                        subtitle = "Require biometric authentication on startup.",
                        checked = isBiometricEnabled,
                        onCheckedChange = { enabled ->
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                settingsViewModel.setBiometricEnabled(activity, enabled) { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        themeColor = safeText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- INTERFACE ---
                    Text("INTERFACE", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    SettingsToggleItem(
                        title = "Use Compact Cards",
                        subtitle = "Shrink asset cards to show more on screen",
                        checked = isCompactMode,
                        onCheckedChange = { settingsViewModel.saveIsCompactViewEnabled(it) },
                        themeColor = safeText
                    )

                    // Syncs to ThemePreferences + MainViewModel; holdings/widget previews read the same flow.
                    SettingsToggleItem(
                        title = "High-Visibility Compact Cards",
                        subtitle = "18/14 dashboard rows vs 14/12 boutique; cards use a fixed font-scale lock so system text size does not override layout.",
                        checked = isHighVisibilityMode,
                        onCheckedChange = { settingsViewModel.saveIsHighVisibilityMode(it) },
                        themeColor = safeText
                    )

                    SettingsToggleItem(
                        title = "Confirm Deletion",
                        subtitle = "Show confirmation before removing an asset",
                        checked = confirmDelete,
                        onCheckedChange = { mainViewModel.setConfirmDelete(it) },
                        themeColor = safeText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- THEME & DEPTH ---
                    Text("THEME & DEPTH", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    SettingsToggleItem(
                        title = "Background Gradient",
                        subtitle = "Enable dynamic color shifting",
                        checked = useGradient,
                        onCheckedChange = { mainViewModel.setUseGradient(it) },
                        themeColor = safeText
                    )

                    if (useGradient) {
                        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
                            Text(text = "Gradient Intensity", color = safeText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = gradientAmount,
                                onValueChange = { mainViewModel.setGradientAmount(it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Yellow,
                                    activeTrackColor = Color.Yellow,
                                    inactiveTrackColor = safeText.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- MANAGEMENT ---
                    Text("MANAGEMENT", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Button(
                        onClick = {
                            isExiting = true
                            navController.navigate(Routes.PORTFOLIO_MANAGER)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("PORTFOLIO MANAGER", color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isExiting = true
                            navController.navigate(Routes.THEME_STUDIO)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("THEME MANAGER", color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // --- SYSTEM ACTIONS ---
                    Text("SYSTEM ACTIONS", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Button(
                        onClick = { showFactoryResetDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("FACTORY DEFAULT", color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, themeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = themeColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = themeColor.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Yellow,
                checkmarkColor = Color.Black
            )
        )
    }
}

@Composable
fun SettingsCheckboxItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = themeColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = themeColor.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Yellow,
                checkmarkColor = Color.Black
            )
        )
    }
}
