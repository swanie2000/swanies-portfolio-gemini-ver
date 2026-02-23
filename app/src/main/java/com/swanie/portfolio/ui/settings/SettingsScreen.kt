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
import androidx.compose.material3.MaterialTheme
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

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                .padding(16.dp)
        ) {
            Text("Appearance", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use Light Text", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Checkbox(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.saveIsDarkMode(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        checkmarkColor = if (isDarkMode) Color.Black else Color.White
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gradient Background", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Checkbox(
                    checked = isGradientEnabled,
                    onCheckedChange = { viewModel.saveIsGradientEnabled(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        checkmarkColor = if (isDarkMode) Color.Black else Color.White
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Compact Holdings View", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Checkbox(
                    checked = isCompactViewEnabled,
                    onCheckedChange = { viewModel.saveIsCompactViewEnabled(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        checkmarkColor = if (isDarkMode) Color.Black else Color.White
                    )
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.THEME_STUDIO) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Adjust Theme Color", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
