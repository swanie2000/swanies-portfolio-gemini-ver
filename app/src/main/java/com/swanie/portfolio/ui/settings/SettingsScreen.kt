package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // CRITICAL MISSING IMPORT
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush

@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel // Use the correct ViewModel type
) {
    // 1. Observe the correct state names from your SettingsViewModel
    val isCompactMode by settingsViewModel.isCompactViewEnabled.collectAsState()

    // THE SOVEREIGN COLUMN (Locked Layout)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LocalBackgroundBrush.current)
            .statusBarsPadding()
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // CONTENT AREA (Weight 1f creates the Hard Stop floor)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            SettingsToggleItem(
                title = "Use Compact Cards",
                subtitle = "Shrink asset cards to show more on screen",
                checked = isCompactMode,
                onCheckedChange = { settingsViewModel.saveIsCompactViewEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // THEME STUDIO SHORTCUT
            Button(
                onClick = { navController.navigate("theme_studio") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Open Theme Studio", color = Color.White)
            }
        }

        // THE HARD STOP NAVIGATION DOCK
        Surface(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            color = Color.Black.copy(alpha = 0.2f),
            tonalElevation = 0.dp
        ) {
            Column {
                BottomNavigationBar(navController = navController)
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.Gray.copy(alpha = 0.8f),
                uncheckedTrackColor = Color.Black.copy(alpha = 0.3f)
            )
        )
    }
}