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
import androidx.compose.ui.focus.onFocusChanged
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
    val defaultMetalName = metalNameFromTicker(defaultTicker)
    val defaultShape = "Bar"
    val defaultName = buildMetalDisplayName(defaultMetalName, defaultShape)

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
                                options = listOf("Gold", "Silver", "Platinum", "Palladium"),
                                selected = when {
                                    draftAsset.symbol.contains("XAU", true) || draftAsset.name.contains("Gold", true) -> "Gold"
                                    draftAsset.symbol.contains("XAG", true) || draftAsset.name.contains("Silver", true) -> "Silver"
                                    draftAsset.symbol.contains("XPT", true) || draftAsset.name.contains("Plat", true) -> "Platinum"
                                    else -> "Palladium"
                                },
                                compact = true,
                            ) { metal ->
                                val ticker = when (metal) {
                                    "Gold" -> "XAU"
                                    "Silver" -> "XAG"
                                    "Platinum" -> "XPT"
                                    else -> "XPD"
                                }
                                val displayName = buildMetalDisplayName(metal, draftAsset.physicalForm)
                                draftAsset = draftAsset.copy(symbol = ticker, name = displayName, displayName = displayName)
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
                            ) { form ->
                                val metal = metalNameFromTicker(draftAsset.symbol)
                                val displayName = buildMetalDisplayName(metal, form)
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
                                            val metal = metalNameFromTicker(draftAsset.symbol)
                                            val fallback = buildMetalDisplayName(metal, draftAsset.physicalForm)
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
                                            text = formatUnitAbbreviation(draftAsset.weightUnit), 
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
                                val total = (draftAsset.officialSpotPrice * draftAsset.weight * draftAsset.amountHeld) + draftAsset.premium
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
                        val metal = metalNameFromTicker(draftAsset.symbol)
                        val fallback = buildMetalDisplayName(metal, draftAsset.physicalForm)
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

/**
 * 🛠️ UNIT ABBREVIATIONS
 * Maps OZ -> oz, KILO -> kg, GRAM -> g for professional stamping look.
 */
private fun formatUnitAbbreviation(unit: String): String = when (unit.uppercase()) {
    "GRAM" -> "g"
    "KILO" -> "kg"
    "OZ" -> "oz"
    else -> unit.lowercase()
}

private fun metalNameFromTicker(symbol: String): String = when (symbol.uppercase()) {
    "XAG", "SILVER" -> "Silver"
    "XPT", "PLATINUM" -> "Platinum"
    "XPD", "PALLADIUM" -> "Palladium"
    else -> "Gold"
}

private fun buildMetalDisplayName(metal: String, form: String): String {
    val formLabel = when (form.uppercase()) {
        "COIN", "COINS" -> "COINS"
        "ROUND", "ROUNDS" -> "ROUNDS"
        else -> "BAR"
    }
    return "${metal.uppercase()} $formLabel"
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
