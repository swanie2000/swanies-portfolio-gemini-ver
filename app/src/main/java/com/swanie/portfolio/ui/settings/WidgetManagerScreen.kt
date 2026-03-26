package com.swanie.portfolio.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.MetalIcon
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.theme.ThemeDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetManagerScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    assetViewModel: AssetViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val userConfig by settingsViewModel.userConfig.collectAsStateWithLifecycle(null)
    val assets by assetViewModel.holdings.collectAsStateWithLifecycle(emptyList())
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // DRAFT STATE: Handle selection locally before saving
    var draftSelectedIds by remember(userConfig?.selectedWidgetAssets, assets.isEmpty()) {
        mutableStateOf(
            if (assets.isEmpty()) emptyList()
            else userConfig?.selectedWidgetAssets?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WIDGET MANAGER", fontWeight = FontWeight.Black, fontSize = 20.sp, color = safeText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = safeText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        floatingActionButton = {
            Button(
                onClick = {
                    settingsViewModel.saveWidgetConfiguration(draftSelectedIds) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Settings Saved! Pulse Updated.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text("SAVE WIDGET SETTINGS", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 🛡️ SURGICAL RESET: ONLY clears widget selection string to fix "7-10" numbering issue.
                // REMOVED: settingsViewModel.clearAllAssets() call to prevent accidental data loss.
                TextButton(
                    onClick = {
                        scope.launch {
                            settingsViewModel.clearWidgetSelection()
                            draftSelectedIds = emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("CLEAR ALL SELECTIONS", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            item {
                Text(
                    "PRIVACY MODE",
                    color = safeText.copy(0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { settingsViewModel.updateShowWidgetTotal(!(userConfig?.showWidgetTotal ?: false)) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hide Totals", color = safeText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Hide totals on the home screen widget", color = safeText.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                    Checkbox(
                        checked = !(userConfig?.showWidgetTotal ?: false),
                        onCheckedChange = { settingsViewModel.updateShowWidgetTotal(!it) },
                        colors = CheckboxDefaults.colors(checkedColor = safeText, uncheckedColor = safeText.copy(alpha = 0.3f), checkmarkColor = Color.Black)
                    )
                }
            }

            item {
                Button(
                    onClick = { navController.navigate(Routes.WIDGET_STUDIO) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("OPEN WIDGET STUDIO", color = safeText, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = safeText)
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "DISPLAY ASSETS",
                        color = safeText.copy(0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("(${draftSelectedIds.size}/10)", color = if(draftSelectedIds.size >= 10) Color.Red else safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            items(assets) { asset ->
                val isSelected = draftSelectedIds.contains(asset.coinId)
                val orderIndex = if (isSelected) draftSelectedIds.indexOf(asset.coinId) + 1 else null

                WidgetAssetSelectItem(
                    asset = asset,
                    isSelected = isSelected,
                    orderIndex = orderIndex,
                    onToggle = {
                        val newList = if (isSelected) {
                            draftSelectedIds.filter { it != asset.coinId }
                        } else {
                            if (draftSelectedIds.size < 10) draftSelectedIds + asset.coinId else draftSelectedIds
                        }
                        draftSelectedIds = newList
                    },
                    themeColor = safeText
                )
            }

            item { Spacer(Modifier.height(80.dp)) } // Extra padding for FAB
        }
    }
}

@Composable
fun WidgetAssetSelectItem(
    asset: AssetEntity,
    isSelected: Boolean,
    orderIndex: Int?,
    onToggle: () -> Unit,
    themeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) themeColor.copy(0.1f) else Color.Transparent)
            .border(1.dp, if (isSelected) themeColor.copy(0.3f) else themeColor.copy(0.1f), RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            if (asset.category == AssetCategory.METAL) {
                MetalIcon(name = asset.name, weight = asset.weight, size = 32, category = asset.category)
            } else {
                AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape))
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.symbol.uppercase(), color = themeColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(asset.name.replace("\n", " "), color = themeColor.copy(0.6f), fontSize = 12.sp)
        }

        if (isSelected && orderIndex != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = orderIndex.toString(),
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, themeColor.copy(0.2f), CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetStudioScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val userConfig by viewModel.userConfig.collectAsStateWithLifecycle(null)
    
    val widgetBgColor = userConfig?.widgetBgColor ?: "#000000"
    val widgetBgTextColor = userConfig?.widgetBgTextColor ?: "#FFFFFF"
    val widgetCardColor = userConfig?.widgetCardColor ?: "#1A1C1E"
    val widgetCardTextColor = userConfig?.widgetCardTextColor ?: "#FFFFFF"

    var activeTarget by remember { mutableIntStateOf(0) }
    val targets = listOf(
        "WIDGET\nBACKGROUND",
        "WIDGET\nBG TEXT",
        "WIDGET\nCARDS",
        "WIDGET\nCARD TEXT"
    )

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Helper for ThemeDefaults color to Hex String
    fun Color.toHexString(): String = String.format("#%06X", 0xFFFFFF and this.toArgb())

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
        if (!hexInput.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }) {
            errorMessage = "INVALID HEX: 0-9 & A-F ONLY"
            showError = true
            scope.launch { delay(2500); showError = false }
            return
        }

        try {
            val finalHex = "#$hexInput"
            when (activeTarget) {
                0 -> viewModel.updateWidgetBgColor(finalHex)
                1 -> viewModel.updateWidgetBgTextColor(finalHex)
                2 -> viewModel.updateWidgetCardColor(finalHex)
                3 -> viewModel.updateWidgetCardTextColor(finalHex)
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

    LaunchedEffect(activeTarget, userConfig) {
        val currentHex = when (activeTarget) {
            0 -> widgetBgColor
            1 -> widgetBgTextColor
            2 -> widgetCardColor
            else -> widgetCardTextColor
        }
        try {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(Color(currentHex.toColorInt()).toArgb(), hsv)
            hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
            hexInput = currentHex.replace("#", "").uppercase()
            hasUnsavedChanges = false
        } catch (e: Exception) {}
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Default?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text("This will override your custom HEX selection.", color = Color.White.copy(0.7f)) },
            confirmButton = {
                TextButton(onClick = {
                    val defAppBg = ThemeDefaults.APP_BG.toHexString()
                    val defAppText = ThemeDefaults.APP_TEXT.toHexString()
                    val defCardBg = ThemeDefaults.CARD_BG.toHexString()
                    val defCardText = ThemeDefaults.CARD_TEXT.toHexString()

                    viewModel.updateWidgetBgColor(defAppBg)
                    viewModel.updateWidgetBgTextColor(defAppText)
                    viewModel.updateWidgetCardColor(defCardBg)
                    viewModel.updateWidgetCardTextColor(defCardText)

                    hasUnsavedChanges = false
                    hexInput = defAppBg.replace("#", "")
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(defAppBg), hsv)
                    hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
                    showResetDialog = false
                }) { Text("RESET", color = Color.Red, fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("CANCEL", color = Color.White) }
            },
            containerColor = Color(0xFF1C1C1E)
        )
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
                        modifier = Modifier.clickable { showResetDialog = true }
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

            AnimatedVisibility(visible = showError) {
                Box(modifier = Modifier.clip(CircleShape).background(Color.Red).padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(text = errorMessage, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().height(54.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(livePreviewColor, RoundedCornerShape(8.dp)).border(2.dp, Color.White, RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Black, RoundedCornerShape(8.dp)).border(1.dp, Color.Gray, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("# ", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        BasicTextField(
                            value = hexInput,
                            onValueChange = { if (it.length <= 6) { hexInput = it.uppercase(); hasUnsavedChanges = true } },
                            modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) hexInput = "" },
                            textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black),
                            cursorBrush = SolidColor(Color.Yellow),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { applyColor() })
                        )
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp).background(Color.Red, CircleShape).padding(4.dp).clickable { hexInput = "" })
                    }
                }
            }

            StudioSaturationBox(hue, saturation, value, modifier = Modifier.fillMaxWidth().weight(1f)) { s, v ->
                saturation = s; value = v; hasUnsavedChanges = true
                hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, s, v).toArgb())
            }

            StudioHueSlider(hue, modifier = Modifier.fillMaxWidth().height(36.dp)) { h ->
                hue = h; hasUnsavedChanges = true
                hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(h, saturation, value).toArgb())
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // LEFT COLUMN TOP: Widget Background
                    StudioTargetItem(targets[0], widgetBgColor, widgetBgTextColor, activeTarget == 0, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 0 }
                    // RIGHT COLUMN TOP: Widget Cards
                    StudioTargetItem(targets[2], widgetCardColor, widgetCardTextColor, activeTarget == 2, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 2 }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // LEFT COLUMN BOTTOM: Widget BG Text
                    StudioTargetItem(targets[1], widgetBgColor, widgetBgTextColor, activeTarget == 1, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 1 }
                    // RIGHT COLUMN BOTTOM: Widget Card Text
                    StudioTargetItem(targets[3], widgetCardColor, widgetCardTextColor, activeTarget == 3, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 3 }
                }
            }

            Button(
                onClick = { applyColor() },
                modifier = Modifier.fillMaxWidth().height(48.dp).scale(pulseScale),
                colors = ButtonDefaults.buttonColors(containerColor = if (isFlashing) Color.White else Color.Yellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = targets[activeTarget].replace("\n", " ").uppercase(),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StudioTargetItem(
    label: String,
    bg: String,
    txt: String,
    isSelected: Boolean,
    hasChanges: Boolean,
    alpha: Float,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val borderThickness = if (isSelected && hasChanges) 4.dp else if (isSelected) 2.dp else 1.dp
    val borderColor = if (isSelected && hasChanges) Color.White.copy(alpha = alpha) else if (isSelected) Color.White else Color.Gray

    Box(
        modifier = modifier
            .height(54.dp)
            .background(Color(bg.toColorInt()), RoundedCornerShape(8.dp))
            .border(borderThickness, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(txt.toColorInt()),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            lineHeight = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
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
