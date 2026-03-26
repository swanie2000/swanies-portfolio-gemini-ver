package com.swanie.portfolio.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.swanie.portfolio.widget.PortfolioWidget
import com.swanie.portfolio.widget.PortfolioWidgetReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetManagerScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    assetViewModel: AssetViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userConfig by settingsViewModel.userConfig.collectAsStateWithLifecycle(null)
    val assets by assetViewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    val siteTextColor by themeViewModel.siteTextColor.collectAsStateWithLifecycle("#FFFFFF")
    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    // Live Source of Truth for Widget Colors
    val widgetBg by themeViewModel.widgetBgColor.collectAsStateWithLifecycle("#1C1C1E")
    val widgetBgTxt by themeViewModel.widgetBgTextColor.collectAsStateWithLifecycle("#FFFFFF")
    val widgetCrd by themeViewModel.widgetCardColor.collectAsStateWithLifecycle("#2C2C2E")
    val widgetCrdTxt by themeViewModel.widgetCardTextColor.collectAsStateWithLifecycle("#FFFFFF")

    var draftSelectedIds by remember(userConfig?.selectedWidgetAssets) {
        mutableStateOf(userConfig?.selectedWidgetAssets?.split(",")?.filter { it.isNotBlank() } ?: emptyList())
    }
    var draftHideTotals by remember(userConfig?.showWidgetTotal) {
        mutableStateOf(!(userConfig?.showWidgetTotal ?: false))
    }

    val sharedPrefs = remember { context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit, cooldownSeconds) {
        val lastSave = sharedPrefs.getLong("last_widget_save_time", 0L)
        val elapsed = (System.currentTimeMillis() - lastSave) / 1000
        val remaining = (180 - elapsed).toInt()
        cooldownSeconds = if (remaining > 0) remaining else 0
        if (cooldownSeconds > 0) { delay(1000L); cooldownSeconds -= 1 }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WIDGET MANAGER", fontWeight = FontWeight.Black, color = safeText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = safeText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            val timeDisplay = String.format("%d:%02d", cooldownSeconds / 60, cooldownSeconds % 60)
            Button(
                onClick = {
                    if (cooldownSeconds == 0) {
                        scope.launch {
                            settingsViewModel.updateShowWidgetTotal(!draftHideTotals)
                            settingsViewModel.saveWidgetConfiguration(draftSelectedIds) {
                                scope.launch {
                                    val manager = GlanceAppWidgetManager(context)
                                    val glanceIds = manager.getGlanceIds(PortfolioWidget::class.java)
                                    val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

                                    glanceIds.forEach { glanceId ->
                                        updateAppWidgetState(context, glanceId) { prefs ->
                                            prefs[PortfolioWidget.SELECTED_ASSETS_KEY] = draftSelectedIds.joinToString(",")

                                            // INJECT LIVE COLORS INTO WIDGET PREFS
                                            prefs[androidx.datastore.preferences.core.stringPreferencesKey("widget_bg_color")] = widgetBg
                                            prefs[androidx.datastore.preferences.core.stringPreferencesKey("widget_bg_text_color")] = widgetBgTxt
                                            prefs[androidx.datastore.preferences.core.stringPreferencesKey("widget_card_color")] = widgetCrd
                                            prefs[androidx.datastore.preferences.core.stringPreferencesKey("widget_card_text_color")] = widgetCrdTxt

                                            prefs[PortfolioWidget.FORCE_UPDATE_KEY] = System.currentTimeMillis()
                                            prefs[PortfolioWidget.LAST_UPDATED_KEY] = currentTime
                                        }
                                        PortfolioWidget().update(context, glanceId)

                                        val rawId = (glanceId as? AppWidgetId)?.appWidgetId
                                        if (rawId != null) {
                                            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                                                component = ComponentName(context, PortfolioWidgetReceiver::class.java)
                                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(rawId))
                                                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                                            }
                                            context.sendBroadcast(intent)
                                        }
                                    }
                                    sharedPrefs.edit().putLong("last_widget_save_time", System.currentTimeMillis()).apply()
                                    cooldownSeconds = 180
                                    Toast.makeText(context, "Widget Updated & Synced!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.85f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (cooldownSeconds > 0) Color.Gray else Color.Yellow, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = cooldownSeconds == 0
            ) {
                Text(if (cooldownSeconds > 0) "SYNC COOLDOWN $timeDisplay" else "SAVE & SYNC WIDGET", fontWeight = FontWeight.Black)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                WidgetStudioInline(themeViewModel)
                Divider(color = safeText.copy(0.1f), thickness = 1.dp, modifier = Modifier.padding(top = 24.dp))
            }

            item {
                Text("PRIVACY SETTINGS", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Black)
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { draftHideTotals = !draftHideTotals }, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Hide Totals", color = safeText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Only individual prices show on home screen", color = safeText.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                    Checkbox(checked = draftHideTotals, onCheckedChange = { draftHideTotals = it }, colors = CheckboxDefaults.colors(checkedColor = safeText))
                }
                Divider(color = safeText.copy(0.1f), thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))
            }

            item {
                Text("ASSET SELECTION (MAX 10)", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Black)
            }

            itemsIndexed(assets) { _, asset ->
                WidgetAssetSelectItem(asset, draftSelectedIds.contains(asset.coinId), if (draftSelectedIds.contains(asset.coinId)) draftSelectedIds.indexOf(asset.coinId) + 1 else null, {
                    draftSelectedIds = if (draftSelectedIds.contains(asset.coinId)) draftSelectedIds.filter { it != asset.coinId }
                    else if (draftSelectedIds.size < 10) draftSelectedIds + asset.coinId else draftSelectedIds
                }, safeText)
            }
        }
    }
}

@Composable
fun WidgetStudioInline(viewModel: ThemeViewModel) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val bg by viewModel.widgetBgColor.collectAsStateWithLifecycle("#1C1C1E")
    val bgTxt by viewModel.widgetBgTextColor.collectAsStateWithLifecycle("#FFFFFF")
    val crd by viewModel.widgetCardColor.collectAsStateWithLifecycle("#2C2C2E")
    val crdTxt by viewModel.widgetCardTextColor.collectAsStateWithLifecycle("#FFFFFF")

    var activeTarget by rememberSaveable { mutableIntStateOf(0) }
    val targets = listOf("Widget BG", "BG Text", "Card BG", "Card Text")

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = if (hasUnsavedChanges) 1.03f else 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "s")
    val borderAlpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = if (hasUnsavedChanges) 1f else 0.3f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "a")

    val livePreviewColor = remember(hexInput, hue, saturation, value) {
        try { if (hexInput.length == 6) Color(android.graphics.Color.parseColor("#$hexInput")) else Color.hsv(hue, saturation, value) }
        catch (e: Exception) { Color.hsv(hue, saturation, value) }
    }

    fun applyColor() {
        if (hexInput.length != 6) { showError = true; scope.launch { delay(2000); showError = false }; return }
        val finalHex = "#$hexInput"
        when (activeTarget) {
            0 -> viewModel.updateWidgetBgColor(finalHex)
            1 -> viewModel.updateWidgetBgTextColor(finalHex)
            2 -> viewModel.updateWidgetCardColor(finalHex)
            3 -> viewModel.updateWidgetCardTextColor(finalHex)
        }
        isFlashing = true; hasUnsavedChanges = false; focusManager.clearFocus(); keyboardController?.hide()
        scope.launch { delay(300); isFlashing = false }
    }

    LaunchedEffect(activeTarget) {
        val cur = when (activeTarget) { 0 -> bg; 1 -> bgTxt; 2 -> crd; else -> crdTxt }
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(Color(cur.toColorInt()).toArgb(), hsv)
        hue = hsv[0]; saturation = hsv[1]; value = hsv[2]
        hexInput = cur.replace("#", "").uppercase()
        hasUnsavedChanges = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("WIDGET THEME DRAFT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text("DEFAULT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                viewModel.updateWidgetBgColor("#1C1C1E"); viewModel.updateWidgetBgTextColor("#FFFFFF")
                viewModel.updateWidgetCardColor("#2C2C2E"); viewModel.updateWidgetCardTextColor("#FFFFFF")
            })
        }

        // 2x2 Grid from Theme Studio
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioTargetItem(targets[0], bg, bgTxt, activeTarget == 0, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 0 }
                StudioTargetItem(targets[2], crd, crdTxt, activeTarget == 2, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 2 }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioTargetItem(targets[1], bg, bgTxt, activeTarget == 1, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 1 }
                StudioTargetItem(targets[3], crd, crdTxt, activeTarget == 3, hasUnsavedChanges, borderAlpha, Modifier.weight(1f)) { activeTarget = 3 }
            }
        }

        // Preview Row
        Row(Modifier.fillMaxWidth().height(54.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).fillMaxHeight().background(livePreviewColor, RoundedCornerShape(8.dp)).border(2.dp, Color.White, RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1.2f).fillMaxHeight().background(Color.Black, RoundedCornerShape(8.dp)).border(1.dp, if (showError) Color.Red else Color.Gray, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("# ", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    BasicTextField(
                        value = hexInput,
                        onValueChange = { if (it.length <= 6) { hexInput = it.uppercase(); hasUnsavedChanges = true } },
                        modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) hexInput = "" },
                        textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black),
                        cursorBrush = SolidColor(Color.Yellow), singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { applyColor() })
                    )
                    if (hexInput.isNotEmpty()) Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp).background(Color.Red, CircleShape).clickable { hexInput = "" })
                }
            }
        }

        // sliders
        BoxWithConstraints(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.Gray, RoundedCornerShape(12.dp))) {
            val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f)))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ ->
                saturation = (change.position.x / w).coerceIn(0f, 1f); value = 1f - (change.position.y / h).coerceIn(0f, 1f)
                hasUnsavedChanges = true; hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, saturation, value).toArgb())
            } })
        }

        BoxWithConstraints(Modifier.fillMaxWidth().height(36.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape)) {
            val w = constraints.maxWidth.toFloat(); val colors = remember { (0..360).map { Color.hsv(it.toFloat(), 1f, 1f) } }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(colors)))
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ ->
                hue = (change.position.x / w * 360f).coerceIn(0f, 360f); hasUnsavedChanges = true
                hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, saturation, value).toArgb())
            } })
        }

        Button(onClick = { applyColor() }, modifier = Modifier.fillMaxWidth().height(50.dp).scale(pulseScale),
            colors = ButtonDefaults.buttonColors(containerColor = if (isFlashing) Color.White else Color.Yellow, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("APPLY TO ${targets[activeTarget].uppercase()}", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun StudioTargetItem(label: String, bg: String, txt: String, isSelected: Boolean, hasChanges: Boolean, alpha: Float, modifier: Modifier, onClick: () -> Unit) {
    val borderThickness = if (isSelected && hasChanges) 4.dp else if (isSelected) 2.dp else 1.dp
    val borderColor = if (isSelected && hasChanges) Color.White.copy(alpha = alpha) else if (isSelected) Color.White else Color.Gray
    Box(modifier = modifier.height(50.dp).background(Color(bg.toColorInt()), RoundedCornerShape(8.dp)).border(borderThickness, borderColor, RoundedCornerShape(8.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(label, color = Color(txt.toColorInt()), fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WidgetAssetSelectItem(asset: AssetEntity, isSelected: Boolean, orderIndex: Int?, onToggle: () -> Unit, themeColor: Color) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (isSelected) themeColor.copy(0.1f) else Color.Transparent).border(1.dp, if (isSelected) themeColor.copy(0.3f) else themeColor.copy(0.1f), RoundedCornerShape(16.dp)).clickable { onToggle() }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            if (asset.category == AssetCategory.METAL) MetalIcon(asset.name, asset.weight, 32, asset.category.name)
            else AsyncImage(model = asset.imageUrl, null, modifier = Modifier.size(32.dp).clip(CircleShape))
        }
        Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) {
        Text(asset.symbol.uppercase(), color = themeColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(asset.name.replace("\n", " "), color = themeColor.copy(0.6f), fontSize = 12.sp)
    }
        if (isSelected && orderIndex != null) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(Color.Yellow), contentAlignment = Alignment.Center) {
                Text(orderIndex.toString(), color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        } else { Box(Modifier.size(32.dp).clip(CircleShape).border(1.dp, themeColor.copy(0.2f), CircleShape)) }
    }
}