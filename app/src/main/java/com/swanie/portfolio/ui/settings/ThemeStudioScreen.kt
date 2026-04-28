@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.swanie.portfolio.data.local.AssetEntity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.components.BoutiqueHeader
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.theme.ThemeDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ThemeStudioScreen(
    navController: NavHostController,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val assetViewModel: AssetViewModel = hiltViewModel()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // ViewModel State
    val cardBgColor by viewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by viewModel.cardTextColor.collectAsState()
    val siteBgColor by viewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by viewModel.siteTextColor.collectAsState()
    val liveHoldings by assetViewModel.holdings.collectAsState()
    val liveSampleAsset = liveHoldings?.firstOrNull()
    val liveTotalValue = remember(liveHoldings) {
        (liveHoldings ?: emptyList()).sumOf { (it.officialSpotPrice * it.amountHeld) + it.premium }
    }
    val liveTotalValueText = remember(liveTotalValue) {
        NumberFormat.getCurrencyInstance(Locale.US).format(liveTotalValue)
    }

    // Local UI State
    var activeTarget by remember { mutableIntStateOf(0) }
    val targets = listOf("APP Background", "App Text", "Card Background", "Card Text")
    var targetMenuExpanded by remember { mutableStateOf(false) }

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Helper for ThemeDefaults color to Hex String
    fun Color.toHexString(): String = String.format("#%06X", 0xFFFFFF and this.toArgb())

    val safeSiteTextColor = try { Color(siteTextColor.toColorInt()) } catch (e: Exception) { Color.White }
    val safeCardBgColor = try { Color(cardBgColor.toColorInt()) } catch (e: Exception) { Color(0xFF1C1C1E) }

    // Pulse & Glow Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val savePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "savePulse"
    )
    val currentTargetHex = when (activeTarget) {
        0 -> siteBgColor
        1 -> siteTextColor
        2 -> cardBgColor
        else -> cardTextColor
    }.replace("#", "").uppercase()
    val normalizedInput = hexInput.uppercase()
    val hasValidHex = normalizedInput.length == 6 && normalizedInput.all { it.isDigit() || it in 'A'..'F' }
    val hasUnsavedChanges = normalizedInput != currentTargetHex
    val saveButtonColor = when {
        !hasUnsavedChanges -> Color(0xFF6A6A6A)
        else -> Color(0xFFFFD54F).copy(alpha = savePulseAlpha)
    }

    val livePreviewColor = remember(hexInput, hue, saturation, value) {
        try {
            if (hexInput.length == 6) Color("#$hexInput".toColorInt())
            else Color.hsv(hue, saturation, value)
        } catch (e: Exception) { Color.hsv(hue, saturation, value) }
    }

    fun applyColor() {
        if (hexInput.length != 6) {
            errorMessage = "6 CHARACTERS REQUIRED"
            showError = true
            scope.launch { delay(2500); showError = false }
            return
        }
        val isValid = hexInput.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
        if (!isValid) {
            errorMessage = "INVALID HEX: 0-9 & A-F ONLY"
            showError = true
            scope.launch { delay(2500); showError = false }
            return
        }

        try {
            val finalHex = "#$hexInput"
            when (activeTarget) {
                0 -> viewModel.saveSiteBackgroundColor(finalHex)
                1 -> viewModel.saveSiteTextColor(finalHex)
                2 -> viewModel.saveCardBackgroundColor(finalHex)
                3 -> viewModel.saveCardTextColor(finalHex)
            }
            isFlashing = true
            keyboardController?.hide()
            focusManager.clearFocus()
            scope.launch { delay(300); isFlashing = false }
        } catch (e: Exception) {
            errorMessage = "SAVE ERROR"
            showError = true
            scope.launch { delay(2500); showError = false }
        }
    }

    LaunchedEffect(activeTarget) {
        val currentHex = when (activeTarget) {
            0 -> siteBgColor; 1 -> siteTextColor; 2 -> cardBgColor; else -> cardTextColor
        }
        try {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(Color(currentHex.toColorInt()).toArgb(), hsv)
            hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
            hexInput = currentHex.replace("#", "").uppercase()
        } catch (e: Exception) {}
    }
    fun resetPendingEditToCurrentTarget() {
        val currentHex = when (activeTarget) {
            0 -> siteBgColor
            1 -> siteTextColor
            2 -> cardBgColor
            else -> cardTextColor
        }
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(Color(currentHex.toColorInt()).toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        hexInput = currentHex.replace("#", "").uppercase()
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.theme_reset_title), fontWeight = FontWeight.Bold, color = safeSiteTextColor) },
            text = { Text(stringResource(R.string.theme_reset_body), color = safeSiteTextColor.copy(0.7f)) },
            confirmButton = {
                TextButton(onClick = {
                    val defAppBg = ThemeDefaults.APP_BG.toHexString()
                    val defAppText = ThemeDefaults.APP_TEXT.toHexString()
                    val defCardBg = ThemeDefaults.CARD_BG.toHexString()
                    val defCardText = ThemeDefaults.CARD_TEXT.toHexString()

                    viewModel.saveSiteBackgroundColor(defAppBg)
                    viewModel.saveSiteTextColor(defAppText)
                    viewModel.saveCardBackgroundColor(defCardBg)
                    viewModel.saveCardTextColor(defCardText)

                    hexInput = defAppBg.replace("#", "")
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(defAppBg.toColorInt(), hsv)
                    hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
                    showResetDialog = false
                }) { Text(stringResource(R.string.settings_reset_everything), color = Color.Red, fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.action_cancel), color = safeSiteTextColor) }
            },
            containerColor = safeCardBgColor
        )
    }

    // 🛡️ SURGERY: Root Box replaces Scaffold to respect MainActivity shell
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- 🦢 BOUTIQUE HEADER ---
            BoutiqueHeader(
                title = stringResource(R.string.theme_manager_title),
                onBack = { navController.popBackStack() },
                actionIcon = Icons.Default.Refresh,
                onAction = { showResetDialog = true },
                textColor = safeSiteTextColor
            )

            Column(modifier = Modifier.fillMaxWidth().height(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                AnimatedVisibility(visible = showError, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                    Box(modifier = Modifier.clip(CircleShape).background(Color.Red).padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(text = errorMessage, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasUnsavedChanges) {
                    Button(
                        onClick = {
                            resetPendingEditToCurrentTarget()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.action_cancel), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = targetMenuExpanded,
                        onExpandedChange = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            targetMenuExpanded = !targetMenuExpanded
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = targets[activeTarget],
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetMenuExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Yellow.copy(alpha = 0.9f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        )
                        ExposedDropdownMenu(
                            expanded = targetMenuExpanded,
                            onDismissRequest = { targetMenuExpanded = false },
                        ) {
                            targets.forEachIndexed { index, label ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 12.sp) },
                                    onClick = {
                                        activeTarget = index
                                        targetMenuExpanded = false
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    },
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = { applyColor() },
                    enabled = hasUnsavedChanges && hasValidHex,
                    modifier = Modifier.height(56.dp).width(106.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = saveButtonColor,
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF6A6A6A),
                        disabledContentColor = Color.White.copy(alpha = 0.7f),
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.action_save), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            ThemeStudioSamplePreview(
                sampleAsset = liveSampleAsset,
                totalValue = liveTotalValueText,
                appBgHex = siteBgColor,
                appTextHex = siteTextColor,
                cardBgHex = cardBgColor,
                cardTextHex = cardTextColor,
            )

            Row(modifier = Modifier.fillMaxWidth().height(54.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(livePreviewColor, RoundedCornerShape(8.dp)).border(2.dp, Color.White, RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Black, RoundedCornerShape(8.dp)).border(1.dp, Color.Gray, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("# ", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        BasicTextField(
                            value = hexInput,
                            onValueChange = { input ->
                                if (input.length <= 6) {
                                    hexInput = input.uppercase()
                                    if (input.length == 6 && input.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }) {
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV("#$input".toColorInt(), hsv)
                                        hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) hexInput = "" },
                            textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black),
                            cursorBrush = SolidColor(Color.Yellow),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { applyColor() })
                        )
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White,
                            modifier = Modifier.size(24.dp).background(Color.Red, CircleShape).padding(4.dp).clickable {
                                if (hexInput.isNotEmpty()) hexInput = "" else { focusManager.clearFocus(); keyboardController?.hide() }
                            }
                        )
                    }
                }
            }

            StudioSaturationBox(hue, saturation, value, modifier = Modifier.fillMaxWidth().height(88.dp)) { s, v ->
                saturation = s; value = v
                hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, s, v).toArgb())
            }

            StudioHueSlider(hue, modifier = Modifier.fillMaxWidth().height(18.dp)) { h ->
                hue = h
                hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(h, saturation, value).toArgb())
            }
        }
    }
}

@Composable
private fun ThemeStudioSamplePreview(
    sampleAsset: AssetEntity?,
    totalValue: String,
    appBgHex: String,
    appTextHex: String,
    cardBgHex: String,
    cardTextHex: String,
) {
    WidgetPreviewSlim(
        sampleAsset = sampleAsset,
        vaultName = "LIVE PREVIEW",
        totalValue = totalValue,
        bgHex = appBgHex,
        bgTxtHex = appTextHex,
        cardHex = cardBgHex,
        cardTxtHex = cardTextHex,
        showTotal = true,
        isHighVisibilityMode = false,
    )
}

@Composable
private fun StudioSaturationBox(hue: Float, saturation: Float, value: Float, modifier: Modifier, onSatValChanged: (Float, Float) -> Unit) {
    BoxWithConstraints(modifier = modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, Color.Gray, RoundedCornerShape(12.dp))) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f)))))
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))
        Box(Modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures { change, _ ->
                onSatValChanged((change.position.x / width).coerceIn(0f, 1f), 1f - (change.position.y / height).coerceIn(0f, 1f))
            }
        })
    }
}

@Composable
private fun StudioHueSlider(hue: Float, modifier: Modifier, onHueChanged: (Float) -> Unit) {
    val hueColors = remember { (0..360).map { Color.hsv(it.toFloat(), 1f, 1f) } }
    BoxWithConstraints(modifier = modifier.clip(CircleShape).border(1.dp, Color.Gray, CircleShape)) {
        val width = constraints.maxWidth.toFloat()
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(hueColors)))
        Box(Modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures { change, _ -> onHueChanged((change.position.x / width * 360f).coerceIn(0f, 360f)) }
        })
    }
}