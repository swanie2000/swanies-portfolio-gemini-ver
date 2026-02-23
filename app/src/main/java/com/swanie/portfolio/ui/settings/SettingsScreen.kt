package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(themePreferences)
    )

    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isGradientEnabled by viewModel.isGradientEnabled.collectAsState()
    val isCompactViewEnabled by viewModel.isCompactViewEnabled.collectAsState()
    val isLightTextEnabled by viewModel.isLightTextEnabled.collectAsState()

    val textColor = if (isLightTextEnabled) Color.White else Color(0xFF1C1C1E)
    val checkmarkColor = if (isLightTextEnabled) Color(0xFF1C1C1E) else Color.White
    val settingsFontSize = 12.sp

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = LocalBackgroundBrush.current)
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = 4.dp) // Apply horizontal padding here
        ) {
            Text(
                text = "APPEARANCE",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp, // Reduced header size
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Asset Cards Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("ASSET CARDS", fontSize = settingsFontSize, color = textColor, softWrap = false)
                Spacer(Modifier.width(8.dp))
                Checkbox(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.saveIsDarkMode(true) },
                    colors = CheckboxDefaults.colors(checkedColor = textColor, checkmarkColor = checkmarkColor)
                )
                Spacer(Modifier.width(4.dp))
                Text("Dark", fontSize = settingsFontSize, color = textColor, softWrap = false)
                Spacer(Modifier.width(8.dp))
                Checkbox(
                    checked = !isDarkMode,
                    onCheckedChange = { viewModel.saveIsDarkMode(false) },
                    colors = CheckboxDefaults.colors(checkedColor = textColor, checkmarkColor = checkmarkColor)
                )
                Spacer(Modifier.width(4.dp))
                Text("Light", fontSize = settingsFontSize, color = textColor, softWrap = false)
            }

            // Asset Text Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("ASSET TEXT  ", fontSize = settingsFontSize, color = textColor, softWrap = false) // Extra space for alignment
                Spacer(Modifier.width(8.dp))
                Checkbox(
                    checked = !isLightTextEnabled,
                    onCheckedChange = { viewModel.saveIsLightTextEnabled(false) },
                    colors = CheckboxDefaults.colors(checkedColor = textColor, checkmarkColor = checkmarkColor)
                )
                Spacer(Modifier.width(4.dp))
                Text("Dark", fontSize = settingsFontSize, color = textColor, softWrap = false)
                Spacer(Modifier.width(8.dp))
                Checkbox(
                    checked = isLightTextEnabled,
                    onCheckedChange = { viewModel.saveIsLightTextEnabled(true) },
                    colors = CheckboxDefaults.colors(checkedColor = textColor, checkmarkColor = checkmarkColor)
                )
                Spacer(Modifier.width(4.dp))
                Text("Light", fontSize = settingsFontSize, color = textColor, softWrap = false)
            }

            // Gradient Background Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("Use gradient on background", fontSize = settingsFontSize, color = textColor, softWrap = false)
                Checkbox(
                    checked = isGradientEnabled,
                    onCheckedChange = { viewModel.saveIsGradientEnabled(it) },
                    colors = CheckboxDefaults.colors(checkedColor = textColor, checkmarkColor = checkmarkColor)
                )
            }

            // Compact View Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("Use compact asset cards", fontSize = settingsFontSize, color = textColor, softWrap = false)
                Checkbox(
                    checked = isCompactViewEnabled,
                    onCheckedChange = { viewModel.saveIsCompactViewEnabled(it) },
                    colors = CheckboxDefaults.colors(checkedColor = textColor, checkmarkColor = checkmarkColor)
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.THEME_STUDIO) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Adjust Theme Color", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor, softWrap = false)
            }
        }
    }
}
