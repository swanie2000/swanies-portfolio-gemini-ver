package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.swanie.portfolio.ui.navigation.Routes

@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isCompactMode by settingsViewModel.isCompactViewEnabled.collectAsState()
    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val safeBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(safeBg)
            .statusBarsPadding()
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "SETTINGS",
                color = safeText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        // --- CONTENT ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SettingsToggleItem(
                title = "Use Compact Cards",
                subtitle = "Shrink asset cards to show more on screen",
                checked = isCompactMode,
                onCheckedChange = { settingsViewModel.saveIsCompactViewEnabled(it) },
                themeColor = safeText // FIXED: Checkbox now follows theme
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate(Routes.THEME_STUDIO) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("OPEN THEME STUDIO", color = safeText, fontWeight = FontWeight.Bold)
            }
        }

        // --- BOTTOM NAVIGATION (DYNAMIC THEME) ---
        Surface(modifier = Modifier.fillMaxWidth().height(40.dp).background(safeBg)) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Active = 100% Opacity, Inactive = 30% Opacity
                IconButton(onClick = { navController.navigate(Routes.HOME) }) {
                    Icon(Icons.Default.Home, null, tint = if(currentRoute == Routes.HOME) safeText else safeText.copy(alpha = 0.3f))
                }
                IconButton(onClick = { navController.navigate(Routes.HOLDINGS) }) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null, tint = if(currentRoute == Routes.HOLDINGS) safeText else safeText.copy(alpha = 0.3f))
                }
                IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Default.Settings, null, tint = if(currentRoute == Routes.SETTINGS) safeText else safeText.copy(alpha = 0.3f))
                }
            }
        }
        Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars).background(safeBg))
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColor: Color
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
                checkedColor = themeColor, // THEMED: Matches site text
                uncheckedColor = themeColor.copy(alpha = 0.3f),
                checkmarkColor = Color.Black // Contrast check
            )
        )
    }
}