package com.swanie.portfolio.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swanie.portfolio.R
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

    var activeTarget by remember { mutableIntStateOf(0) }
    val targets = listOf("Card Background", "Card Text", "App Background", "App Text")

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var validationErrorMessage by remember { mutableStateOf<String?>(null) }
    var isFlashing by remember { mutableStateOf(false) }

    val localColor = Color.hsv(hue, saturation, value)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(activeTarget) {
        val hex = when (activeTarget) {
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

    LaunchedEffect(isFlashing) {
        if (isFlashing) {
            delay(100)
            isFlashing = false
        }
    }

    LaunchedEffect(validationErrorMessage) {
        if (validationErrorMessage != null) {
            delay(3000)
            validationErrorMessage = null
        }
    }

    fun validateAndApplyHex(input: String) {
        if (isValidHex(input)) {
            validationErrorMessage = null
            try {
                val hsv = Color("#$input".toColorInt()).toHsv()
                hue = hsv[0]
                saturation = hsv[1]
                value = hsv[2]
                keyboardController?.hide()
                focusManager.clearFocus()
            } catch (e: IllegalArgumentException) {
                validationErrorMessage = "INVALID HEX"
            }
        } else {
            validationErrorMessage = "INVALID HEX"
        }
    }

    Scaffold(containerColor = Color(0xFF1C1C1E)) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50))
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(50))
                            .clickable { navController.popBackStack() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("BACK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Image(
                        painter = painterResource(id = R.drawable.swanie_foreground),
                        contentDescription = "logo",
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopCenter)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50))
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(50))
                            .clickable {
                                val defaultBg = "#000416"
                                val defaultText = "#FFFFFF"
                                viewModel.saveCardBackgroundColor(defaultBg)
                                viewModel.saveSiteBackgroundColor(defaultBg)
                                viewModel.saveCardTextColor(defaultText)
                                viewModel.saveSiteTextColor(defaultText)
                                val hsv = Color(defaultBg.toColorInt()).toHsv()
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("DEFAULT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                AnimatedVisibility(visible = validationErrorMessage != null) {
                     Text(
                        text = validationErrorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Text(
                    text = "THEME STUDIO",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Preview & Hex
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .height(50.dp)
                            .background(localColor, shape = RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp))
                    )
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { hexInput = it.take(6).uppercase() },
                        modifier = Modifier
                            .weight(0.65f)
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp))
                            .onFocusChanged {
                                if (it.isFocused) {
                                    hexInput = ""
                                }
                                isEditingHex = it.isFocused
                             },
                        textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { validateAndApplyHex(hexInput) }),
                        trailingIcon = {
                            if (hexInput.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .clickable {
                                            hexInput = ""
                                            validationErrorMessage = null
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
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = Color.White)
                    )
                }

                // Color Picker
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
                        .aspectRatio(1.8f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp))
                )
                Box(modifier = Modifier.padding(vertical = 8.dp)) {
                    HueSlider(
                        hue = hue,
                        onHueChanged = { newHue -> hue = newHue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp))
                    )
                }

                // Selection Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val index0 = 0
                        val index2 = 2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(cardBgColor.toColorInt()), shape = RoundedCornerShape(8.dp))
                                .border(
                                    width = if (activeTarget == index0) 3.dp else 1.dp,
                                    color = if (activeTarget == index0) Color.White else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { activeTarget = index0 }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(targets[index0], color = Color(cardTextColor.toColorInt()), fontSize = 10.sp, maxLines = 2, textAlign = TextAlign.Center)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(siteBgColor.toColorInt()), shape = RoundedCornerShape(8.dp))
                                .border(
                                    width = if (activeTarget == index2) 3.dp else 1.dp,
                                    color = if (activeTarget == index2) Color.White else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { activeTarget = index2 }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(targets[index2], color = Color(siteTextColor.toColorInt()), fontSize = 10.sp, maxLines = 2, textAlign = TextAlign.Center)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val index1 = 1
                        val index3 = 3
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(cardBgColor.toColorInt()), shape = RoundedCornerShape(8.dp))
                                .border(
                                    width = if (activeTarget == index1) 3.dp else 1.dp,
                                    color = if (activeTarget == index1) Color.White else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { activeTarget = index1 }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(targets[index1], color = Color(cardTextColor.toColorInt()), fontSize = 10.sp, maxLines = 2, textAlign = TextAlign.Center)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(siteBgColor.toColorInt()), shape = RoundedCornerShape(8.dp))
                                .border(
                                    width = if (activeTarget == index3) 3.dp else 1.dp,
                                    color = if (activeTarget == index3) Color.White else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { activeTarget = index3 }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(targets[index3], color = Color(siteTextColor.toColorInt()), fontSize = 10.sp, maxLines = 2, textAlign = TextAlign.Center)
                        }
                    }
                }

                // Apply Button
                Button(
                    onClick = {
                        val hexToSave = "#$hexInput"
                        when (activeTarget) {
                            0 -> viewModel.saveCardBackgroundColor(hexToSave)
                            1 -> viewModel.saveCardTextColor(hexToSave)
                            2 -> viewModel.saveSiteBackgroundColor(hexToSave)
                            3 -> viewModel.saveSiteTextColor(hexToSave)
                        }
                        isFlashing = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isFlashing) Color.White else Color.Yellow)
                ) {
                    Text(
                        text = "APPLY ${targets[activeTarget].uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
