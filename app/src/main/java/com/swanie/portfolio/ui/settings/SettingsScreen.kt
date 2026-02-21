package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.ui.theme.SeedBurgundy
import com.swanie.portfolio.ui.theme.SeedCharcoal
import com.swanie.portfolio.ui.theme.SeedEmerald
import com.swanie.portfolio.ui.theme.SeedNavy
import com.swanie.portfolio.ui.theme.SeedRoyal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(themePreferences)
    )

    val currentHex by viewModel.themeColorHex.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var customHex by remember(currentHex) { mutableStateOf(currentHex) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val predefinedSeedColors = listOf(
        SeedNavy, SeedEmerald, SeedRoyal, SeedCharcoal, SeedBurgundy
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Section 1: Appearance
            Text("Appearance", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.saveIsDarkMode(it) }
                )
            }

            Spacer(Modifier.height(32.dp))

            // Section 2: Theme Color
            Text("Theme Color", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                predefinedSeedColors.forEach { color ->
                    val hex = "#" + Integer.toHexString(color.toArgb()).substring(2).uppercase()
                    val isSelected = currentHex.equals(hex, ignoreCase = true)
                    ColorBubble(color = color, isSelected = isSelected) {
                        viewModel.saveThemeColorHex(hex)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Section 3: Custom Color
            Text("Custom Color", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customHex,
                    onValueChange = { customHex = it },
                    label = { Text("HEX Color (e.g., #1976D2)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { viewModel.saveThemeColorHex(customHex) }) {
                    Text("Set")
                }
            }
        }
    }
}

@Composable
fun ColorBubble(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                shape = CircleShape
            )
    )
}
