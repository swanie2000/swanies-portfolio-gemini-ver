package com.swanie.portfolio.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
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
import com.swanie.portfolio.widget.PortfolioWidget
import com.swanie.portfolio.widget.PortfolioWidgetReceiver
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
    val context = LocalContext.current
    val userConfig by settingsViewModel.userConfig.collectAsStateWithLifecycle(null)
    val assets by assetViewModel.holdings.collectAsStateWithLifecycle(emptyList())
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    Log.d("SWANIE_TRACE", "1. Save Button Clicked")
                    val idsString = draftSelectedIds.joinToString(",")
                    settingsViewModel.saveWidgetConfiguration(draftSelectedIds) {
                        Log.d("SWANIE_TRACE", "2. ViewModel Save Callback Received")
                        scope.launch {
                            val manager = GlanceAppWidgetManager(context)
                            val glanceIds = manager.getGlanceIds(PortfolioWidget::class.java)
                            Log.d("SWANIE_TRACE", "3. Found ${glanceIds.size} Glance IDs")

                            glanceIds.forEach { glanceId ->
                                Log.d("SWANIE_TRACE", "4. Sending Direct Command to ID: $glanceId")
                                updateAppWidgetState(context, glanceId) { prefs ->
                                    prefs[PortfolioWidget.SELECTED_ASSETS_KEY] = idsString
                                    prefs[PortfolioWidget.FORCE_UPDATE_KEY] = System.currentTimeMillis()
                                }
                                PortfolioWidget().update(context, glanceId)

                                // 🛡️ SURGERY: Direct cast to satisfy compiler instead of buggy toAppWidgetId extension
                                val rawId = (glanceId as? AppWidgetId)?.appWidgetId
                                if (rawId != null) {
                                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                                        component = ComponentName(context, PortfolioWidgetReceiver::class.java)
                                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(rawId))
                                        addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            addFlags(Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS)
                                        }
                                    }
                                    context.sendBroadcast(intent)
                                }
                            }

                            Toast.makeText(context, "Widget Updated!", Toast.LENGTH_SHORT).show()
                            snackbarHostState.showSnackbar("Settings Saved! Pulse Updated.", duration = SnackbarDuration.Short)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.9f).height(48.dp),
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
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TextButton(
                    onClick = { scope.launch { settingsViewModel.clearWidgetSelection(); draftSelectedIds = emptyList() } },
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
                Text("PRIVACY MODE", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { settingsViewModel.updateShowWidgetTotal(!(userConfig?.showWidgetTotal ?: false)) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hide Totals", color = safeText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Hide totals on the widget", color = safeText.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                    Checkbox(
                        checked = !(userConfig?.showWidgetTotal ?: false),
                        onCheckedChange = { settingsViewModel.updateShowWidgetTotal(!it) },
                        colors = CheckboxDefaults.colors(checkedColor = safeText, checkmarkColor = Color.Black)
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("OPEN WIDGET STUDIO", color = safeText, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, null, tint = safeText)
                    }
                }
            }
            items(assets) { asset ->
                WidgetAssetSelectItem(
                    asset = asset,
                    isSelected = draftSelectedIds.contains(asset.coinId),
                    orderIndex = if (draftSelectedIds.contains(asset.coinId)) draftSelectedIds.indexOf(asset.coinId) + 1 else null,
                    onToggle = {
                        draftSelectedIds = if (draftSelectedIds.contains(asset.coinId)) draftSelectedIds.filter { it != asset.coinId }
                        else if (draftSelectedIds.size < 10) draftSelectedIds + asset.coinId else draftSelectedIds
                    },
                    themeColor = safeText
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun WidgetAssetSelectItem(asset: AssetEntity, isSelected: Boolean, orderIndex: Int?, onToggle: () -> Unit, themeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) themeColor.copy(0.1f) else Color.Transparent)
            .border(1.dp, if (isSelected) themeColor.copy(0.3f) else themeColor.copy(0.1f), RoundedCornerShape(16.dp))
            .clickable { onToggle() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            if (asset.category == AssetCategory.METAL) {
                // 🛡️ CAST FIX: Explicit .name for category String requirement
                MetalIcon(asset.name, asset.weight, 32, asset.category.name)
            } else {
                AsyncImage(model = asset.imageUrl, null, modifier = Modifier.size(32.dp).clip(CircleShape))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.symbol.uppercase(), color = themeColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(asset.name.replace("\n", " "), color = themeColor.copy(0.6f), fontSize = 12.sp)
        }
        if (isSelected && orderIndex != null) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Yellow), contentAlignment = Alignment.Center) {
                Text(orderIndex.toString(), color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        } else { Box(modifier = Modifier.size(32.dp).clip(CircleShape).border(1.dp, themeColor.copy(0.2f), CircleShape)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetStudioScreen(navController: NavHostController, viewModel: SettingsViewModel = hiltViewModel()) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val userConfig by viewModel.userConfig.collectAsStateWithLifecycle(null)

    val targets = listOf("WIDGET\nBACKGROUND", "WIDGET\nBG TEXT", "WIDGET\nCARDS", "WIDGET\nCARD TEXT")
    var activeTarget by remember { mutableIntStateOf(0) }
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    fun Color.toHexString(): String = String.format("#%06X", 0xFFFFFF and this.toArgb())
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(1f, if (hasUnsavedChanges) 1.05f else 1f, infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")

    val livePreviewColor = remember(hexInput) { try { Color(android.graphics.Color.parseColor("#$hexInput")) } catch (e: Exception) { Color.hsv(hue, saturation, value) } }

    fun applyColor() {
        if (hexInput.length != 6) { errorMessage = "6 CHARACTERS REQUIRED"; showError = true; scope.launch { delay(2500); showError = false }; return }
        try {
            val finalHex = "#$hexInput"
            when (activeTarget) { 0 -> viewModel.updateWidgetBgColor(finalHex); 1 -> viewModel.updateWidgetBgTextColor(finalHex); 2 -> viewModel.updateWidgetCardColor(finalHex); 3 -> viewModel.updateWidgetCardTextColor(finalHex) }
            isFlashing = true; hasUnsavedChanges = false; keyboardController?.hide(); focusManager.clearFocus(); scope.launch { delay(300); isFlashing = false }
        } catch (e: Exception) { errorMessage = "SAVE ERROR"; showError = true; scope.launch { delay(2500); showError = false } }
    }

    Scaffold(containerColor = Color(0xFF1C1C1E)) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(28.dp).clickable { navController.popBackStack() })
                    Column(modifier = Modifier.clickable { showResetDialog = true }) {
                        Text("DEFAULT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Image(painter = painterResource(R.drawable.swanie_foreground), null, modifier = Modifier.size(120.dp))
            }
            Row(modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(livePreviewColor, RoundedCornerShape(8.dp)).border(2.dp, Color.White, RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Black, RoundedCornerShape(8.dp)).border(1.dp, Color.Gray, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("# ", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        BasicTextField(value = hexInput, onValueChange = { if (it.length <= 6) { hexInput = it.uppercase(); hasUnsavedChanges = true } }, textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black), cursorBrush = SolidColor(Color.Yellow), singleLine = true, keyboardActions = KeyboardActions(onDone = { applyColor() }))
                    }
                }
            }
            StudioSaturationBox(hue, saturation, value, Modifier.fillMaxWidth().weight(1f)) { s, v -> saturation = s; value = v; hasUnsavedChanges = true; hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, s, v).toArgb()) }
            StudioHueSlider(hue, Modifier.fillMaxWidth().height(36.dp)) { h -> hue = h; hasUnsavedChanges = true; hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(h, saturation, value).toArgb()) }
            Button(onClick = { applyColor() }, modifier = Modifier.fillMaxWidth().height(48.dp).scale(pulseScale), colors = ButtonDefaults.buttonColors(containerColor = if (isFlashing) Color.White else Color.Yellow), shape = RoundedCornerShape(12.dp)) {
                Text(targets[activeTarget].replace("\n", " ").uppercase(), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun StudioSaturationBox(hue: Float, saturation: Float, value: Float, modifier: Modifier, onSatValChanged: (Float, Float) -> Unit) {
    BoxWithConstraints(modifier = modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, Color.Gray, RoundedCornerShape(12.dp))) {
        val width = constraints.maxWidth.toFloat(); val height = constraints.maxHeight.toFloat()
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f)))))
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> onSatValChanged((change.position.x / width).coerceIn(0f, 1f), 1f - (change.position.y / height).coerceIn(0f, 1f)) } })
    }
}

@Composable
private fun StudioHueSlider(hue: Float, modifier: Modifier, onHueChanged: (Float) -> Unit) {
    val hueColors = remember { (0..360).map { Color.hsv(it.toFloat(), 1f, 1f) } }
    BoxWithConstraints(modifier = modifier.clip(CircleShape).border(1.dp, Color.Gray, CircleShape)) {
        val width = constraints.maxWidth.toFloat()
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(hueColors)))
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> onHueChanged((change.position.x / width * 360f).coerceIn(0f, 360f)) } })
    }
}