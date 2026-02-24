package com.swanie.portfolio.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swanie.portfolio.ui.components.CustomToast
import com.swanie.portfolio.ui.theme.toHsv
import kotlinx.coroutines.delay

private fun isValidHex(hex: String): Boolean {
    return hex.matches(Regex("^[A-Fa-f0-9]{6}$"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStudioScreen(navController: NavController) {
    val viewModel: ThemeViewModel = hiltViewModel()

    val cardBgColor by viewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by viewModel.cardTextColor.collectAsState()
    val siteBgColor by viewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by viewModel.siteTextColor.collectAsState()
    val useGradient by viewModel.useGradient.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Card BG", "Card Text", "Site BG", "Site Text")

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var customToastMessage by remember { mutableStateOf<String?>(null) }

    val localColor = Color.hsv(hue, saturation, value)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(selectedTab) {
        val hex = when (selectedTab) {
            0 -> cardBgColor
            1 -> cardTextColor
            2 -> siteBgColor
            else -> siteTextColor
        }
        try {
            val hsv = Color(hex.toColorInt()).toHsv()
            hue = hsv[0]
            saturation = hsv[1]
            value = hsv[2]
        } catch (e: IllegalArgumentException) {
            val hsv = Color.Black.toHsv()
            hue = hsv[0]
            saturation = hsv[1]
            value = hsv[2]
        }
    }

    var isEditingHex by remember { mutableStateOf(false) }
    LaunchedEffect(localColor) {
        if (!isEditingHex) {
            hexInput = String.format("%06X", 0xFFFFFF and localColor.toArgb())
        }
    }

    LaunchedEffect(customToastMessage) {
        if (customToastMessage != null) {
            delay(3000)
            customToastMessage = null
        }
    }

    fun validateAndApplyHex(input: String) {
        if (isValidHex(input)) {
            try {
                val hsv = Color("#$input".toColorInt()).toHsv()
                hue = hsv[0]
                saturation = hsv[1]
                value = hsv[2]
                keyboardController?.hide()
                focusManager.clearFocus()
            } catch (e: IllegalArgumentException) {
                customToastMessage = "Invalid Hex Code"
            }
        } else {
            customToastMessage = "Invalid Hex Code"
            hexInput = ""
        }
    }

    Scaffold(
        containerColor = Color(0xFF1C1C1E)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel", color = Color.White)
                    }
                    Button(
                        onClick = {
                            validateAndApplyHex(hexInput)
                            val hexToSave = "#$hexInput"
                            when (selectedTab) {
                                0 -> viewModel.saveCardBackgroundColor(hexToSave)
                                1 -> viewModel.saveCardTextColor(hexToSave)
                                2 -> viewModel.saveSiteBackgroundColor(hexToSave)
                                else -> viewModel.saveSiteTextColor(hexToSave)
                            }
                            keyboardController?.hide()
                            navController.popBackStack()
                        },
                        enabled = hexInput.length == 6
                    ) {
                        Text("Apply")
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                            Text(title, modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(localColor, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { hexInput = it.take(6).uppercase() },
                    label = { Text("Hex Code", color = Color.White.copy(alpha = 0.7f)) },
                    prefix = { Text("#", color = Color.White.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        if (hexInput.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .clickable {
                                        hexInput = ""
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Text",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            isEditingHex = it.isFocused
                            if (it.isFocused) {
                                hexInput = "" // Auto-clear on focus
                            }
                        },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { validateAndApplyHex(hexInput) })
                )

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useGradient, onCheckedChange = { viewModel.saveUseGradient(it) })
                    Text("Use Gradient", color = Color.White)
                }

                Spacer(Modifier.height(24.dp))

                SaturationValueBox(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onSatValChanged = { newSat, newVal ->
                        saturation = newSat
                        value = newVal
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(24.dp))
                HueSlider(
                    hue = hue,
                    onHueChanged = { newHue -> hue = newHue },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
            }

            AnimatedVisibility(
                visible = customToastMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 80.dp)
            ) {
                customToastMessage?.let { CustomToast(message = it) }
            }
        }
    }
}
