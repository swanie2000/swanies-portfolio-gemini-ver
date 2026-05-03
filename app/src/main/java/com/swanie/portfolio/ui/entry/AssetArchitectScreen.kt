package com.swanie.portfolio.ui.entry

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.res.Resources
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetValuation
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.holdings.ArchitectIconSelectionStep
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.FunnelGrid
import com.swanie.portfolio.ui.holdings.MetalIcon
import com.swanie.portfolio.ui.holdings.formatCurrency

/**
 * 🛠️ V7.2.8 MISSION: ARCHITECT PRECISION & LAYOUT TIGHTENING
 * Refined Two-Stage Architect with abbreviated units and clean whole-number formatting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetArchitectScreen(
    initialSymbol: String = "GOLD",
    initialPrice: Double = 0.0,
    initialSource: String = "Manual",
    /** When set (e.g. pencil edit from holdings), starts at step 1 with this row as the draft. */
    existingAsset: AssetEntity? = null,
    onSave: (AssetEntity) -> Unit,
    onCancel: () -> Unit
) {
    val viewModel: AssetViewModel = hiltViewModel()
    val res = LocalContext.current.resources
    val draftKey = existingAsset?.coinId ?: "create_${initialSymbol}_${initialPrice}"
    var architectStage by remember(draftKey) { mutableStateOf(ArchitectStage.BLUEPRINT) }
    var activeEditor by remember { mutableStateOf<ArchitectEditorField?>(null) }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val imeVisible = imeBottomPx > 0

    LaunchedEffect(architectStage) {
        when (architectStage) {
            ArchitectStage.BLUEPRINT -> activeEditor = null
            ArchitectStage.ICON_PICK -> {
                activeEditor = null
                focusManager.clearFocus()
            }
            ArchitectStage.LIVE_CARD -> Unit
        }
    }

    val defaultTicker = when (initialSymbol.uppercase()) {
        "XAU", "GOLD", "CUSTOM" -> "XAU"
        "XAG", "SILVER" -> "XAG"
        "XPT", "PLATINUM" -> "XPT"
        "XPD", "PALLADIUM" -> "XPD"
        else -> "XAU"
    }
    val defaultShape = "Bar"
    val defaultName = buildLocalizedMetalDisplayName(res, defaultTicker, defaultShape)

    // Step 1: new row uses defaults; edit (holdings pencil) hydrates from [existingAsset].
    var draftAsset by remember(draftKey) {
        mutableStateOf(
            if (existingAsset != null) {
                existingAsset.copy(
                    displayName = existingAsset.displayName.ifBlank { existingAsset.name },
                    name = existingAsset.name,
                )
            } else {
                AssetEntity(
                    coinId = "CUSTOM_${System.currentTimeMillis()}",
                    symbol = defaultTicker,
                    name = defaultName,
                    displayName = defaultName,
                    category = AssetCategory.METAL,
                    officialSpotPrice = initialPrice,
                    priceSource = initialSource,
                    baseSymbol = defaultTicker,
                    apiId = defaultTicker,
                    weight = 1.0,
                    weightUnit = "OZ",
                    physicalForm = defaultShape,
                    isMetal = true,
                    amountHeld = 1.0,
                    premium = 0.0
                )
            }
        )
    }

    // UI-only: how premium is entered; `draftAsset.premium` stays total $ for DB + rest of app.
    var premiumInputMode by remember { mutableStateOf(PremiumInputMode.PER_UNIT) }

    LaunchedEffect(draftAsset.symbol, draftKey) {
        if (existingAsset != null &&
            draftAsset.symbol == existingAsset.symbol &&
            draftAsset.officialSpotPrice > 0.0
        ) {
            return@LaunchedEffect
        }
        val spot = viewModel.fetchMarketPriceData(draftAsset.symbol).officialSpotPrice
        if (spot > 0.0) {
            draftAsset = draftAsset.copy(
                officialSpotPrice = spot,
                priceSource = "YahooFinance",
                baseSymbol = draftAsset.symbol,
                apiId = draftAsset.symbol,
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        when (architectStage) {
                            ArchitectStage.BLUEPRINT -> stringResource(R.string.architect_step_1_of_3)
                            ArchitectStage.LIVE_CARD -> stringResource(R.string.architect_step_2_of_3)
                            ArchitectStage.ICON_PICK -> stringResource(R.string.architect_step_3_of_3)
                        },
                        style = TextStyle(fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
                    ) 
                },
                navigationIcon = { 
                    IconButton(onClick = onCancel) { 
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel), tint = Color.White)
                    } 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF000416), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF000416)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (architectStage) {
            ArchitectStage.BLUEPRINT -> {
                // Step 1: compact choices + pinned primary action (step 2 is the live card).
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Spacer(Modifier.height(4.dp))

                        Column {
                            Text(
                                stringResource(R.string.architect_select_metal),
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Spacer(Modifier.height(4.dp))
                            FunnelGrid(
                                options = listOf("XAU", "XAG", "XPT", "XPD"),
                                selected = architectMetalSelectionKey(draftAsset.symbol, draftAsset.name),
                                compact = true,
                                labelForOption = { ticker ->
                                    when (ticker) {
                                        "XAU" -> res.getString(R.string.architect_metal_gold)
                                        "XAG" -> res.getString(R.string.architect_metal_silver)
                                        "XPT" -> res.getString(R.string.architect_metal_platinum)
                                        else -> res.getString(R.string.architect_metal_palladium)
                                    }
                                },
                            ) { ticker ->
                                val displayName = buildLocalizedMetalDisplayName(res, ticker, draftAsset.physicalForm)
                                draftAsset = draftAsset.copy(
                                    symbol = ticker,
                                    apiId = ticker,
                                    baseSymbol = ticker,
                                    name = displayName,
                                    displayName = displayName,
                                )
                            }
                        }

                        Column {
                            Text(
                                stringResource(R.string.architect_select_shape),
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Spacer(Modifier.height(4.dp))
                            FunnelGrid(
                                options = listOf("Bar", "Coin", "Round"),
                                selected = draftAsset.physicalForm,
                                compact = true,
                                labelForOption = { form ->
                                    when (form) {
                                        "Coin" -> res.getString(R.string.architect_shape_coin)
                                        "Round" -> res.getString(R.string.architect_shape_round)
                                        else -> res.getString(R.string.architect_shape_bar)
                                    }
                                },
                            ) { form ->
                                val displayName = buildLocalizedMetalDisplayName(res, draftAsset.symbol, form)
                                draftAsset = draftAsset.copy(physicalForm = form, name = displayName, displayName = displayName)
                            }
                        }

                        Column {
                            Text(
                                stringResource(R.string.architect_select_unit),
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Spacer(Modifier.height(4.dp))
                            FunnelGrid(
                                options = listOf("OZ", "KILO", "GRAM"),
                                selected = draftAsset.weightUnit,
                                compact = true,
                                labelForOption = { unit ->
                                    when (unit) {
                                        "KILO" -> res.getString(R.string.architect_unit_choice_kilo)
                                        "GRAM" -> res.getString(R.string.architect_unit_choice_gram)
                                        else -> res.getString(R.string.architect_unit_choice_oz)
                                    }
                                },
                            ) { draftAsset = draftAsset.copy(weightUnit = it) }
                        }

                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { architectStage = ArchitectStage.LIVE_CARD },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.architect_next_to_step, 2),
                            style = TextStyle(
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center,
                            ),
                            maxLines = 2,
                        )
                    }
                }
            }
            ArchitectStage.LIVE_CARD -> {
                // 🖼️ STAGE 2: THE LIVE CARD (scroll) + SAVE strip above IME when editing name
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                    Spacer(Modifier.height(20.dp))

                    if (activeEditor != null && imeVisible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                onClick = {
                                    when (activeEditor) {
                                        ArchitectEditorField.QUANTITY -> {
                                            draftAsset = draftAsset.copy(amountHeld = 0.0)
                                        }
                                        ArchitectEditorField.NAME -> {
                                            val fallback = buildLocalizedMetalDisplayName(res, draftAsset.symbol, draftAsset.physicalForm)
                                            draftAsset = draftAsset.copy(displayName = fallback, name = fallback)
                                        }
                                        ArchitectEditorField.WEIGHT -> {
                                            draftAsset = draftAsset.copy(weight = 0.0)
                                        }
                                        ArchitectEditorField.PREMIUM -> {
                                            draftAsset = draftAsset.copy(premium = 0.0)
                                        }
                                        null -> Unit
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    stringResource(R.string.action_clear),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                )
                            }
                            TextButton(
                                onClick = {
                                    activeEditor = null
                                    focusManager.clearFocus()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    stringResource(R.string.action_save),
                                    color = Color.Yellow,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                // ICON AREA
                                Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    MetalIcon(
                                        name = draftAsset.name,
                                        weight = draftAsset.weight,
                                        unit = draftAsset.weightUnit,
                                        physicalForm = draftAsset.physicalForm,
                                        category = AssetCategory.METAL
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    // INLINE WEIGHT INPUT (Abbreviated Unit)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SmartNumericField(
                                            value = draftAsset.weight,
                                            onValueChange = { draftAsset = draftAsset.copy(weight = it) },
                                            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center),
                                            modifier = Modifier
                                                .width(45.dp)
                                                .onFocusChanged {
                                                    activeEditor = when {
                                                        it.isFocused -> ArchitectEditorField.WEIGHT
                                                        activeEditor == ArchitectEditorField.WEIGHT -> null
                                                        else -> activeEditor
                                                    }
                                                }
                                        )
                                        Text(
                                            text = formatUnitAbbreviation(res, draftAsset.weightUnit),
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // IDENTITY AREA
                                Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.architect_quantity), color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    SmartNumericField(
                                        value = draftAsset.amountHeld,
                                        onValueChange = { draftAsset = draftAsset.copy(amountHeld = it) },
                                        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged {
                                                activeEditor = when {
                                                    it.isFocused -> ArchitectEditorField.QUANTITY
                                                    activeEditor == ArchitectEditorField.QUANTITY -> null
                                                    else -> activeEditor
                                                }
                                            }
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    // INLINE NAME INPUT
                                    BasicTextField(
                                        value = draftAsset.displayName,
                                        onValueChange = { next ->
                                            val normalized = next
                                                .replace("\r", "")
                                                .lines()
                                                .take(2)
                                                .joinToString("\n")
                                            draftAsset = draftAsset.copy(displayName = normalized, name = normalized)
                                        },
                                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                        cursorBrush = SolidColor(Color.Yellow),
                                        singleLine = false,
                                        minLines = 2,
                                        maxLines = 2,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged {
                                                activeEditor = when {
                                                    it.isFocused -> ArchitectEditorField.NAME
                                                    activeEditor == ArchitectEditorField.NAME -> null
                                                    else -> activeEditor
                                                }
                                            },
                                    )
                                }

                                // SPOT PRICE (premium controls moved to full-width row below)
                                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                                    Text(stringResource(R.string.architect_spot_price), color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(text = formatCurrency(draftAsset.officialSpotPrice), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FilterChip(
                                    selected = premiumInputMode == PremiumInputMode.PER_UNIT,
                                    onClick = { premiumInputMode = PremiumInputMode.PER_UNIT },
                                    label = { Text(stringResource(R.string.architect_premium_mode_per), fontSize = 9.sp, fontWeight = FontWeight.Black) },
                                    modifier = Modifier.height(30.dp),
                                )
                                FilterChip(
                                    selected = premiumInputMode == PremiumInputMode.ONCE,
                                    onClick = { premiumInputMode = PremiumInputMode.ONCE },
                                    label = { Text(stringResource(R.string.architect_premium_mode_once), fontSize = 9.sp, fontWeight = FontWeight.Black) },
                                    modifier = Modifier.height(30.dp),
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = when (premiumInputMode) {
                                    PremiumInputMode.PER_UNIT -> stringResource(R.string.architect_premium_per_unit)
                                    PremiumInputMode.ONCE -> stringResource(R.string.architect_premium_once)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White.copy(0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                            )
                            val premiumDisplay = when (premiumInputMode) {
                                PremiumInputMode.PER_UNIT ->
                                    if (draftAsset.amountHeld > 0.0) draftAsset.premium / draftAsset.amountHeld else 0.0
                                PremiumInputMode.ONCE -> draftAsset.premium
                            }
                            SmartNumericField(
                                value = premiumDisplay,
                                onValueChange = { v ->
                                    draftAsset = when (premiumInputMode) {
                                        PremiumInputMode.PER_UNIT ->
                                            if (draftAsset.amountHeld > 0.0) {
                                                draftAsset.copy(premium = v * draftAsset.amountHeld)
                                            } else {
                                                draftAsset.copy(premium = v)
                                            }
                                        PremiumInputMode.ONCE -> draftAsset.copy(premium = v)
                                    }
                                },
                                textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp, textAlign = TextAlign.End),
                                ghostPlaceholder = stringResource(R.string.architect_premium_placeholder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged {
                                        activeEditor = when {
                                            it.isFocused -> ArchitectEditorField.PREMIUM
                                            activeEditor == ArchitectEditorField.PREMIUM -> null
                                            else -> activeEditor
                                        }
                                    }
                            )

                            Spacer(Modifier.height(12.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(Modifier.height(12.dp))

                            // TOTAL VALUE PREVIEW
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.architect_estimated_value), color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                val total = AssetValuation.holdingValueUsd(draftAsset)
                                Text(text = formatCurrency(total), color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    if (!imeVisible) {
                        // Step 3: icon selection (hidden while IME is up so layout stays clean).
                        Button(
                            onClick = { architectStage = ArchitectStage.ICON_PICK },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.architect_next_to_step, 3), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }

                        TextButton(onClick = { architectStage = ArchitectStage.BLUEPRINT }, modifier = Modifier.padding(top = 8.dp)) {
                            Text(stringResource(R.string.action_back), color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    }

                }
            }
            ArchitectStage.ICON_PICK -> {
                ArchitectIconSelectionStep(
                    displayName = draftAsset.displayName.ifBlank { draftAsset.name },
                    weight = draftAsset.weight,
                    weightUnit = draftAsset.weightUnit,
                    physicalForm = draftAsset.physicalForm,
                    coinId = draftAsset.coinId,
                    existingLocalIconPath = draftAsset.localIconPath,
                    imageUrl = draftAsset.imageUrl,
                    isEditingExisting = existingAsset != null,
                    onBack = { architectStage = ArchitectStage.LIVE_CARD },
                    persistCustomIcon = { id, uri -> viewModel.persistCustomIconFromUri(id, uri) },
                    deleteCustomIcon = { id -> viewModel.deleteCustomAssetIcon(id) },
                    onFinished = { path ->
                        val fallback = buildLocalizedMetalDisplayName(res, draftAsset.symbol, draftAsset.physicalForm)
                        val label = draftAsset.displayName.trim()
                            .ifBlank { draftAsset.name.trim() }
                            .ifBlank { fallback }
                        onSave(
                            draftAsset.copy(
                                displayName = label,
                                name = label,
                                localIconPath = path,
                            ),
                        )
                    },
                )
            }
            }
        }
    }
}

/**
 * 🛠️ SMART NUMERIC FIELD
 * Features: Ghost numbers (alpha), Auto-clear on focus, and clean whole-number formatting.
 */
@Composable
fun SmartNumericField(
    value: Double,
    onValueChange: (Double) -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    ghostPlaceholder: String? = null,
) {
    // 🛠️ V7.2.8: Whole Number Fix - Use toString().removeSuffix(".0") for display
    var textState by remember(value) { 
        mutableStateOf(if (value == 0.0) "" else value.toString().removeSuffix(".0")) 
    }
    var focused by remember { mutableStateOf(false) }
    val isGhost = textState.isEmpty() || value == 0.0

    Box {
        if (ghostPlaceholder != null && isGhost && !focused) {
            Text(
                text = ghostPlaceholder,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(
                        when (textStyle.textAlign) {
                            TextAlign.Center -> Alignment.Center
                            TextAlign.Start, TextAlign.Left -> Alignment.CenterStart
                            else -> Alignment.CenterEnd
                        }
                    ),
                style = textStyle.copy(color = textStyle.color.copy(alpha = 0.35f)),
                textAlign = textStyle.textAlign,
                maxLines = 1,
            )
        }
        BasicTextField(
            value = textState,
            onValueChange = {
                if (it.length <= 10) {
                    textState = it
                    onValueChange(it.toDoubleOrNull() ?: 0.0)
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .then(
                    Modifier.onFocusChanged { focusState ->
                        focused = focusState.isFocused
                        if (focusState.isFocused) {
                            if (value == 0.0 || value == 1.0) {
                                textState = ""
                            }
                        }
                    }
                ),
            textStyle = textStyle.copy(color = if (isGhost) textStyle.color.copy(alpha = 0.3f) else textStyle.color),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(Color.Yellow),
            singleLine = true
        )
    }
}

private fun architectMetalSelectionKey(symbol: String, assetName: String): String {
    val s = symbol.uppercase()
    val n = assetName
    return when {
        s == "XAU" || s.contains("XAU") || s == "GOLD" || n.contains("gold", ignoreCase = true) -> "XAU"
        s == "XAG" || s.contains("XAG") || s == "SILVER" || n.contains("silver", ignoreCase = true) -> "XAG"
        s == "XPD" || s.contains("XPD") || s == "PALLADIUM" || n.contains("pallad", ignoreCase = true) -> "XPD"
        s == "XPT" || s.contains("XPT") || s == "PLATINUM" || n.contains("plat", ignoreCase = true) -> "XPT"
        else -> "XAU"
    }
}

private fun buildLocalizedMetalDisplayName(res: Resources, ticker: String, physicalForm: String): String {
    val t = ticker.uppercase()
    val metalWord = when {
        t.contains("XAG") || t == "SILVER" -> res.getString(R.string.architect_metal_silver)
        t.contains("XPT") || t == "PLATINUM" -> res.getString(R.string.architect_metal_platinum)
        t.contains("XPD") || t == "PALLADIUM" -> res.getString(R.string.architect_metal_palladium)
        else -> res.getString(R.string.architect_metal_gold)
    }
    val formSuffix = when (physicalForm.uppercase()) {
        "COIN", "COINS" -> res.getString(R.string.architect_form_suffix_coins)
        "ROUND", "ROUNDS" -> res.getString(R.string.architect_form_suffix_rounds)
        else -> res.getString(R.string.architect_form_suffix_bar)
    }
    return res.getString(
        R.string.architect_asset_display_name_template,
        metalWord.uppercase(),
        formSuffix.uppercase(),
    )
}

private fun formatUnitAbbreviation(res: Resources, unit: String): String = when (unit.uppercase()) {
    "GRAM" -> res.getString(R.string.architect_unit_abbr_gram)
    "KILO" -> res.getString(R.string.architect_unit_abbr_kilo)
    else -> res.getString(R.string.architect_unit_abbr_oz)
}

private enum class ArchitectStage {
    BLUEPRINT,
    LIVE_CARD,
    ICON_PICK,
}

private enum class ArchitectEditorField {
    QUANTITY,
    NAME,
    WEIGHT,
    PREMIUM,
}

private enum class PremiumInputMode {
    /** User enters $ per coin/unit; stored `premium` = value × quantity. */
    PER_UNIT,
    /** User enters one total $ adjustment for the line. */
    ONCE,
}
