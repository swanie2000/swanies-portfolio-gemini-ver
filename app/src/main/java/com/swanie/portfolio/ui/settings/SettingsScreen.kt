package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.navigation.Routes

@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    mainViewModel: MainViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isCompactMode by settingsViewModel.isCompactViewEnabled.collectAsState()
    val confirmDelete by mainViewModel.confirmDelete.collectAsState(initial = true)
    
    // GRADIENT STATE
    val useGradient by mainViewModel.useGradient.collectAsState()
    val gradientAmount by mainViewModel.gradientAmount.collectAsState()

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    // GRADIENT SYMMETRY: Set containerColor to Transparent to allow NavGraph gradient to show
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(text = "SETTINGS", color = safeText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                
                // --- VISUAL SETTINGS ---
                Text("INTERFACE", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                
                SettingsToggleItem(
                    title = "Use Compact Cards",
                    subtitle = "Shrink asset cards to show more on screen",
                    checked = isCompactMode,
                    onCheckedChange = { settingsViewModel.saveIsCompactViewEnabled(it) },
                    themeColor = safeText
                )

                SettingsToggleItem(
                    title = "Confirm Deletion",
                    subtitle = "Show 'Are you sure?' before removing an asset",
                    checked = confirmDelete,
                    onCheckedChange = { mainViewModel.setConfirmDelete(it) },
                    themeColor = safeText
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- THEME & GRADIENT ---
                Text("THEME & DEPTH", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                
                SettingsToggleItem(
                    title = "Background Gradient",
                    subtitle = "Enable dynamic color shifting for the vault",
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

                Button(
                    onClick = { navController.navigate(Routes.THEME_STUDIO) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OPEN THEME STUDIO", color = safeText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, themeColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { onCheckedChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = themeColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = themeColor.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(checkedColor = themeColor, uncheckedColor = themeColor.copy(alpha = 0.3f), checkmarkColor = Color.Black))
    }
}