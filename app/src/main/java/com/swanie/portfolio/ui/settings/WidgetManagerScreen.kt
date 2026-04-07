@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.MetalIcon
import com.swanie.portfolio.widget.PortfolioWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WidgetManagerScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    assetViewModel: AssetViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userConfig by settingsViewModel.userConfig.collectAsStateWithLifecycle(null)
    val assets by assetViewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    val vaults by settingsViewModel.allVaults.collectAsStateWithLifecycle(initialValue = emptyList())
    
    // 🛡️ VAULT STRIP STATE: Manage which vault's widget we are editing
    val currentAppVaultId by mainViewModel.currentVaultId.collectAsStateWithLifecycle()
    var selectedVaultId by rememberSaveable { mutableIntStateOf(-1) }

    // Sync initial selection with active app vault
    LaunchedEffect(currentAppVaultId) {
        if (selectedVaultId == -1) {
            selectedVaultId = currentAppVaultId
        }
    }

    val selectedVault = remember(selectedVaultId, vaults) {
        vaults.find { it.id == selectedVaultId } ?: vaults.firstOrNull()
    }

    val siteTextColor by themeViewModel.siteTextColor.collectAsStateWithLifecycle(initialValue = "#FFFFFF")

    // Draft states for colors, reactive to selected vault changes
    var draftBg by rememberSaveable { mutableStateOf("#1C1C1E") }
    var draftBgTxt by rememberSaveable { mutableStateOf("#FFFFFF") }
    var draftCrd by rememberSaveable { mutableStateOf("#2C2C2E") }
    var draftCrdTxt by rememberSaveable { mutableStateOf("#FFFFFF") }

    var draftSelectedIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    // Re-initialize selection and colors when vault changes in the strip
    LaunchedEffect(selectedVault?.id) {
        selectedVault?.let {
            draftSelectedIds = it.selectedWidgetAssets.split(",").filter { it.isNotBlank() }
            draftBg = it.widgetBgColor
            draftBgTxt = it.widgetBgTextColor
            draftCrd = it.widgetCardColor
            draftCrdTxt = it.widgetCardTextColor
        }
    }

    var draftHideTotals by remember(userConfig) { mutableStateOf(!(userConfig?.showWidgetTotal ?: false)) }
    var appearanceExpanded by rememberSaveable { mutableStateOf(false) }
    var privacyExpanded by rememberSaveable { mutableStateOf(false) }
    var assetsExpanded by rememberSaveable { mutableStateOf(true) }

    val safeThemeText = try { Color((siteTextColor ?: "#FFFFFF").toColorInt()) } catch(e: Exception) { Color.White }

    val sharedPrefs = remember { context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val lastSave = sharedPrefs.getLong("last_widget_save_time", 0L)
        val elapsed = (System.currentTimeMillis() - lastSave) / 1000
        val remaining = (180 - elapsed).toInt()
        cooldownSeconds = if (remaining > 0) remaining else 0
    }
    LaunchedEffect(cooldownSeconds) { if (cooldownSeconds > 0) { delay(1000L); cooldownSeconds -= 1 } }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // --- CUSTOM HEADER ---
            Box(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp)) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = safeThemeText)
                }
                Text(text = "WIDGET MANAGER", fontWeight = FontWeight.Black, fontSize = 16.sp, color = safeThemeText, modifier = Modifier.align(Alignment.Center))
            }

            // 🛡️ VAULT STRIP: Swapping between portfolios
            VaultStrip(
                vaults = vaults,
                selectedVaultId = selectedVaultId,
                onVaultSelected = { selectedVaultId = it },
                themeColor = safeThemeText
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 0.dp)
            ) {
                item {
                    WidgetPreviewSlim(bgHex = draftBg, bgTxtHex = draftBgTxt, cardHex = draftCrd, cardTxtHex = draftCrdTxt, showTotal = !draftHideTotals)
                }

                item {
                    SectionHeaderSmall("APPEARANCE", appearanceExpanded, safeThemeText) { appearanceExpanded = !appearanceExpanded }
                    AnimatedVisibility(visible = appearanceExpanded) {
                        WidgetStudioInlineCompact(draftBg, draftBgTxt, draftCrd, draftCrdTxt) { target, newHex ->
                            when(target) {
                                0 -> draftBg = newHex
                                1 -> draftBgTxt = newHex
                                2 -> draftCrd = newHex
                                3 -> draftCrdTxt = newHex
                            }
                        }
                    }
                    HorizontalDivider(color = safeThemeText.copy(0.1f), thickness = 1.dp)
                }

                item {
                    SectionHeaderSmall("PRIVACY", privacyExpanded, safeThemeText) { privacyExpanded = !privacyExpanded }
                    AnimatedVisibility(visible = privacyExpanded) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { draftHideTotals = !draftHideTotals }, verticalAlignment = Alignment.CenterVertically) {
                            Text("Hide Totals", color = safeThemeText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Checkbox(checked = draftHideTotals, onCheckedChange = { draftHideTotals = it }, colors = CheckboxDefaults.colors(checkedColor = safeThemeText))
                        }
                    }
                    HorizontalDivider(color = safeThemeText.copy(0.1f), thickness = 1.dp)
                }

                item {
                    val countText = "${draftSelectedIds.size}/5 SELECTED"
                    SectionHeaderSmall("ASSETS ($countText)", assetsExpanded, safeThemeText) { assetsExpanded = !assetsExpanded }
                }

                if (assetsExpanded) {
                    if (assets.isEmpty()) {
                        item { Text("No assets in portfolio", color = safeThemeText.copy(0.4f), modifier = Modifier.padding(16.dp)) }
                    } else {
                        itemsIndexed(assets) { _, asset ->
                            val isSelected = draftSelectedIds.contains(asset.coinId)
                            val orderIndex = if (isSelected) draftSelectedIds.indexOf(asset.coinId) + 1 else null

                            WidgetAssetSelectItem(
                                asset = asset,
                                isSelected = isSelected,
                                orderIndex = orderIndex,
                                onToggle = {
                                    if (isSelected) draftSelectedIds = draftSelectedIds.filter { it != asset.coinId }
                                    else if (draftSelectedIds.size < 5) draftSelectedIds = draftSelectedIds + asset.coinId
                                    else Toast.makeText(context, "Max 5 assets", Toast.LENGTH_SHORT).show()
                                },
                                themeColor = safeThemeText
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    val timeDisplay = String.format("%d:%02d", cooldownSeconds / 60, cooldownSeconds % 60)
                    Button(
                        onClick = {
                            if (cooldownSeconds == 0 && selectedVault != null) {
                                scope.launch {
                                    // 1. Update privacy global config
                                    settingsViewModel.updateShowWidgetTotal(!draftHideTotals)
                                    
                                    // 2. Save vault-specific asset selection
                                    settingsViewModel.saveWidgetConfiguration(selectedVault.id, draftSelectedIds) {
                                        scope.launch {
                                            // 3. Save vault-specific colors
                                            settingsViewModel.saveWidgetAppearance(
                                                selectedVault.id,
                                                draftBg,
                                                draftBgTxt,
                                                draftCrd,
                                                draftCrdTxt
                                            )

                                            // 4. Force immediate Glance broadcast with specific data
                                            val manager = GlanceAppWidgetManager(context)
                                            val glanceIds = manager.getGlanceIds(PortfolioWidget::class.java)
                                            glanceIds.forEach { id ->
                                                updateAppWidgetState(context, id) { p ->
                                                    p[PortfolioWidget.SELECTED_ASSETS_KEY] = draftSelectedIds.joinToString(",")
                                                    p[androidx.datastore.preferences.core.stringPreferencesKey("widget_bg_color")] = draftBg
                                                    p[androidx.datastore.preferences.core.stringPreferencesKey("widget_bg_text_color")] = draftBgTxt
                                                    p[androidx.datastore.preferences.core.stringPreferencesKey("widget_card_color")] = draftCrd
                                                    p[androidx.datastore.preferences.core.stringPreferencesKey("widget_card_text_color")] = draftCrdTxt
                                                    p[PortfolioWidget.FORCE_UPDATE_KEY] = System.currentTimeMillis()
                                                }
                                                PortfolioWidget().update(context, id)
                                            }

                                            sharedPrefs.edit().putLong("last_widget_save_time", System.currentTimeMillis()).apply()
                                            cooldownSeconds = 180
                                            Toast.makeText(context, "Widget Synced!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cooldownSeconds > 0) Color.Gray else Color.Yellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = cooldownSeconds == 0
                    ) {
                        Text(text = if (cooldownSeconds > 0) "WAIT $timeDisplay" else "SAVE & SYNC WIDGET", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun VaultStrip(
    vaults: List<VaultEntity>,
    selectedVaultId: Int,
    onVaultSelected: (Int) -> Unit,
    themeColor: Color
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(vaults) { vault ->
            val isSelected = vault.id == selectedVaultId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) themeColor.copy(alpha = 0.1f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color.Yellow else themeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onVaultSelected(vault.id) }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(try { Color(vault.vaultColor.toColorInt()) } catch(e: Exception) { themeColor.copy(0.2f) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = vault.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vault.name.uppercase(),
                    color = if (isSelected) Color.Yellow else themeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WidgetPreviewSlim(bgHex: String, bgTxtHex: String, cardHex: String, cardTxtHex: String, showTotal: Boolean) {
    val bgColor = try { Color(bgHex.toColorInt()) } catch(e: Exception) { Color.Black }
    val bgTextColor = try { Color(bgTxtHex.toColorInt()) } catch(e: Exception) { Color.White }
    val cardColor = try { Color(cardHex.toColorInt()) } catch(e: Exception) { Color.DarkGray }
    val cardTextColor = try { Color(cardTxtHex.toColorInt()) } catch(e: Exception) { Color.White }

    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)).padding(8.dp)) {
        if (showTotal) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("$42,069.00", color = bgTextColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(8.dp))
                Text("+5.2%", color = Color(0xFF00FF00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
        }
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(cardColor).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(16.dp).background(Color.White.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Text("₿", color = Color.White, fontSize = 9.sp) }
            Spacer(Modifier.width(6.dp))
            Text("BITCOIN", color = cardTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("$98,245.00", color = cardTextColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SectionHeaderSmall(title: String, isExpanded: Boolean, themeColor: Color, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = themeColor, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun WidgetStudioInlineCompact(
    draftBg: String, draftBgTxt: String, draftCrd: String, draftCrdTxt: String,
    onColorChanged: (Int, String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var activeTarget by rememberSaveable { mutableIntStateOf(0) }
    val targets = listOf("Widget BG", "BG Text", "Card BG", "Card Text")
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }

    val liveColor = remember(hexInput, hue, saturation, value) {
        try { if (hexInput.length == 6) Color(android.graphics.Color.parseColor("#$hexInput")) else Color.hsv(hue, saturation, value) }
        catch (e: Exception) { Color.hsv(hue, saturation, value) }
    }

    LaunchedEffect(activeTarget) {
        val cur = when (activeTarget) { 0 -> draftBg; 1 -> draftBgTxt; 2 -> draftCrd; else -> draftCrdTxt }
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(try { Color(cur.toColorInt()).toArgb() } catch(e: Exception) { 0 }, hsv)
        hue = hsv[0]; saturation = hsv[1]; value = hsv[2]; hexInput = cur.replace("#", "").uppercase()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            targets.forEachIndexed { i, label ->
                val isSel = activeTarget == i
                Box(Modifier.weight(1f).height(32.dp).background(if(isSel) Color.White.copy(0.1f) else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if(isSel) Color.Yellow else Color.Gray.copy(0.3f), RoundedCornerShape(4.dp)).clickable { activeTarget = i }, contentAlignment = Alignment.Center) {
                    Text(label, color = if(isSel) Color.Yellow else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(0.5f).fillMaxHeight().background(liveColor, RoundedCornerShape(4.dp)).border(1.dp, Color.White, RoundedCornerShape(4.dp)))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f).fillMaxHeight().background(Color.Black, RoundedCornerShape(4.dp)).border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                BasicTextField(value = hexInput, onValueChange = { if (it.length <= 6) hexInput = it.uppercase() }, textStyle = TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = SolidColor(Color.Yellow), singleLine = true)
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color.Gray, RoundedCornerShape(8.dp))) {
            val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f)))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))))
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> saturation = (change.position.x / w).coerceIn(0f, 1f); value = 1f - (change.position.y / h).coerceIn(0f, 1f); hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, saturation, value).toArgb()) } })
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(24.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape)) {
            val w = constraints.maxWidth.toFloat(); val colors = (0..360).map { Color.hsv(it.toFloat(), 1f, 1f) }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(colors)))
            Box(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> hue = (change.position.x / w * 360f).coerceIn(0f, 360f); hexInput = String.format("%06X", 0xFFFFFF and Color.hsv(hue, saturation, value).toArgb()) } })
        }
        Button(onClick = {
            onColorChanged(activeTarget, "#$hexInput")
            isFlashing = true; scope.launch { delay(200); isFlashing = false }
            keyboardController?.hide(); focusManager.clearFocus()
        }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isFlashing) Color.White else Color.Yellow)) {
            Text("SET DRAFT ${targets[activeTarget].uppercase()}", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun WidgetAssetSelectItem(asset: AssetEntity, isSelected: Boolean, orderIndex: Int?, onToggle: () -> Unit, themeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) themeColor.copy(0.05f) else Color.Transparent)
            .border(1.dp, if (isSelected) themeColor.copy(0.2f) else themeColor.copy(0.05f), RoundedCornerShape(8.dp))
            .clickable { onToggle() }.padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (asset.category == AssetCategory.METAL) {
                MetalIcon(
                    name = asset.symbol,
                    weight = asset.weight,
                    unit = asset.weightUnit,
                    physicalForm = asset.physicalForm,
                    size = 20
                )
            } else {
                AsyncImage(model = asset.imageUrl, contentDescription = asset.name, modifier = Modifier.size(20.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = asset.symbol.uppercase(), color = themeColor, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(text = asset.name.replace("\n", " "), color = themeColor.copy(0.6f), fontSize = 11.sp)
        }
        Spacer(Modifier.width(16.dp))
        if (isSelected && orderIndex != null) {
            Box(modifier = Modifier.size(26.dp).clip(CircleShape).background(Color.Yellow), contentAlignment = Alignment.Center) {
                Text(
                    text = orderIndex.toString(), color = Color.Black,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 14.sp, platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        } else {
            Box(modifier = Modifier.size(26.dp).clip(CircleShape).border(1.5.dp, themeColor.copy(0.15f), CircleShape))
        }
    }
}
