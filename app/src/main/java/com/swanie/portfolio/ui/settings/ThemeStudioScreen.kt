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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStudioScreen(
    navController: NavHostController,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // ViewModel State (Source of Truth)
    val cardBgColor by viewModel.cardBackgroundColor.collectAsState()
    val cardTextColor by viewModel.cardTextColor.collectAsState()
    val siteBgColor by viewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by viewModel.siteTextColor.collectAsState()

    // Local UI State
    var activeTarget by remember { mutableIntStateOf(0) }
    val targets = listOf("Card Background", "Card Text", "App Background", "App Text")

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Pulse & Glow Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (hasUnsavedChanges) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (hasUnsavedChanges) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    val livePreviewColor = remember(hexInput, hue, saturation, value) {
        try {
            if (hexInput.length == 6) Color(android.graphics.Color.parseColor("#$hexInput"))
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
                0 -> viewModel.saveCardBackgroundColor(finalHex)
                1 -> viewModel.saveCardTextColor(finalHex)
                2 -> viewModel.saveSiteBackgroundColor(finalHex)
                3 -> viewModel.saveSiteTextColor(finalHex)
            }
            isFlashing = true
            hasUnsavedChanges = false
            keyboardController?.hide()
            focusManager.clearFocus()
            scope.launch { delay(300); isFlashing = false }
        } catch (e: Exception) {
            errorMessage = "SAVE ERROR"
            showError = true
            scope.launch { delay(2500); showError = false }
        }
    }

    // Sync Local UI when Target Changes
    LaunchedEffect(activeTarget) {
        val currentHex = when (activeTarget) {
            0 -> cardBgColor; 1 -> cardTextColor; 2 -> siteBgColor; else -> siteTextColor
        }
        try {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(Color(currentHex.toColorInt()).toArgb(), hsv)
            hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
            hexInput = currentHex.replace("#", "").uppercase()
            hasUnsavedChanges = false
        } catch (e: Exception) {}
    }

    Scaffold(containerColor = Color(0xFF1C1C1E)) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- HEADER ---
            Box(modifier = Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp).clickable { navController.popBackStack() }
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy((-2).dp),
                        modifier = Modifier.clickable {
                            viewModel.saveCardBackgroundColor("#000416")
                            viewModel.saveCardTextColor("#FFFFFF")
                            viewModel.saveSiteBackgroundColor("#000416")
                            viewModel.saveSiteTextColor("#FFFFFF")
                            hasUnsavedChanges = false
                            hexInput = "000416"
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(android.graphics.Color.parseColor("#000416"), hsv)
                            hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
                        }
                    ) {
                        Text("DEFAULT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 10.sp)
                        Text("COLOR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 10.sp)
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.swanie_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
            }

            // Error Message Pill
            Column(modifier = Modifier.fillMaxWidth().height(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                AnimatedVisibility(visible = showError, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                    Box(modifier = Modifier.clip(CircleShape).background(Color.Red).padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(text = errorMessage, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Preview & Hex Input Row
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
                                    hasUnsavedChanges = true
                                    if (input.length == 6 && input.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }) {
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(android.graphics.Color.parseColor("#$input"), hsv)
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

            // Saturation/Value Box
            StudioSaturationBox(hue, saturation, value, modifier = Modifier.fillMaxWidth().weight(1f)) { s, v ->
                saturation = s; value = v; hasUnsavedChanges = true
                hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, s, v).toArgb())
            }

            // Hue Slider
            StudioHueSlider(hue, modifier = Modifier.fillMaxWidth().height(36.dp)) { h ->
                hue = h; hasUnsavedChanges = true
                hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(h, saturation, value).toArgb())
            }

            // Selection Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudioTargetItem(targets[0], cardBgColor, cardTextColor, activeTarget == 0, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 0 }
                    StudioTargetItem(targets[2], siteBgColor, siteTextColor, activeTarget == 2, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 2 }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudioTargetItem(targets[1], cardBgColor, cardTextColor, activeTarget == 1, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 1 }
                    StudioTargetItem(targets[3], siteBgColor, siteTextColor, activeTarget == 3, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 3 }
                }
            }

            // Pulsing Apply Button
            Button(
                onClick = { applyColor() },
                modifier = Modifier.fillMaxWidth().height(50.dp).scale(pulseScale),
                colors = ButtonDefaults.buttonColors(containerColor = if (isFlashing) Color.White else Color.Yellow),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("APPLY TO ${targets[activeTarget].uppercase()}", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// --- HELPERS ---

@Composable
private fun StudioTargetItem(label: String, bg: String, txt: String, isSelected: Boolean, hasChanges: Boolean, alpha: Float, modifier: Modifier, onClick: () -> Unit) {
    val borderThickness = if (isSelected && hasChanges) 4.dp else if (isSelected) 2.dp else 1.dp
    val borderColor = if (isSelected && hasChanges) Color.White.copy(alpha = alpha) else if (isSelected) Color.White else Color.Gray

    Box(
        modifier = modifier.height(50.dp).background(Color(bg.toColorInt()), RoundedCornerShape(8.dp))
            .border(borderThickness, borderColor, RoundedCornerShape(8.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color(txt.toColorInt()), fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
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