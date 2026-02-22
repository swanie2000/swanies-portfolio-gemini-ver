package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush
import com.swanie.portfolio.ui.theme.toHsv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(themePreferences)
    )

    val savedHex by viewModel.themeColorHex.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isGradientEnabled by viewModel.isGradientEnabled.collectAsState()

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(savedHex) {
        try {
            val hsv = Color(savedHex.toColorInt()).toHsv()
            hue = hsv[0]
            saturation = hsv[1]
            value = hsv[2]
        } catch (e: IllegalArgumentException) {
            val hsv = Color(android.graphics.Color.parseColor("#000416")).toHsv()
            hue = hsv[0]
            saturation = hsv[1]
            value = hsv[2]
        }
    }

    val localColor = Color.hsv(hue, saturation, value)

    LaunchedEffect(localColor) {
        hexInput = String.format("%06X", 0xFFFFFF and localColor.toArgb())
    }

    val keyboardController = LocalSoftwareKeyboardController.current

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
        }
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
                Text("Dark Mode", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Switch(checked = isDarkMode, onCheckedChange = { viewModel.saveIsDarkMode(it) })
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gradient Background", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Switch(checked = isGradientEnabled, onCheckedChange = { viewModel.saveIsGradientEnabled(it) })
            }

            Spacer(Modifier.height(32.dp))

            Text("Theme Color", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))

            SaturationValueBox(
                hue = hue,
                saturation = saturation,
                value = value,
                onSatValChanged = { newSat, newVal ->
                    saturation = newSat
                    value = newVal
                },
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Spacer(Modifier.height(16.dp))
            HueSlider(
                hue = hue,
                onHueChanged = { newHue -> hue = newHue },
                modifier = Modifier.fillMaxWidth().height(24.dp)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = {
                        val filtered = it.filter { char -> char.isLetterOrDigit() }.take(6).uppercase()
                        hexInput = filtered
                        if (filtered.length == 6) {
                            try {
                                val hsv = Color(android.graphics.Color.parseColor("#$filtered")).toHsv()
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                            } catch (e: IllegalArgumentException) { /* Ignore invalid hex */ }
                        }
                    },
                    label = { Text("Hex") },
                    prefix = { Text("#") },
                    modifier = Modifier.width(140.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )

                Box(
                    modifier = Modifier
                        .heightIn(min = 56.dp) // Match OutlinedTextField height
                        .weight(1f)
                        .background(localColor, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(8.dp))
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        val hsv = Color(android.graphics.Color.parseColor("#000416")).toHsv()
                        hue = hsv[0]
                        saturation = hsv[1]
                        value = hsv[2]
                        viewModel.saveDefaultTheme()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("Default")
                }
                Button(
                    onClick = {
                        viewModel.saveThemeColorHex("#$hexInput")
                    },
                    modifier = Modifier.weight(1f),
                    enabled = hexInput.length == 6
                ) {
                    Text("Apply Theme")
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}
