package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.swanie.portfolio.ui.components.BottomNavigationBar
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

    val primaryTextColor = Color(siteTextColor.toColorInt())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(siteBgColor.toColorInt()))
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryTextColor,
                fontWeight = FontWeight.Bold
            )
        }

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
                onCheckedChange = { settingsViewModel.saveIsCompactViewEnabled(it) },
                textColor = primaryTextColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate(Routes.THEME_STUDIO) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryTextColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Open Theme Studio", color = primaryTextColor)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(siteBgColor.toColorInt()),
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
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = textColor.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = textColor,
                checkedTrackColor = textColor.copy(alpha = 0.5f),
                uncheckedThumbColor = textColor.copy(alpha = 0.5f),
                uncheckedTrackColor = textColor.copy(alpha = 0.2f)
            )
        )
    }
}
