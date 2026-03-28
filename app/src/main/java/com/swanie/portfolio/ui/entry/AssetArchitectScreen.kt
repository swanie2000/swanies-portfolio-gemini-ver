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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
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
    onSave: (AssetEntity) -> Unit,
    onCancel: () -> Unit
) {
    var isBlueprintStage by remember { mutableStateOf(true) }

    // 🛡️ V19 ARCHITECT STATE
    var draftAsset by remember {
        mutableStateOf(
            AssetEntity(
                coinId = "CUSTOM_${System.currentTimeMillis()}",
                symbol = initialSymbol,
                name = initialSymbol,
                displayName = initialSymbol,
                category = AssetCategory.METAL,
                officialSpotPrice = initialPrice,
                priceSource = initialSource,
                weight = 1.0,
                weightUnit = "OZ",
                physicalForm = "Coin",
                isMetal = true,
                amountHeld = 1.0,
                premium = 0.0
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (isBlueprintStage) "STAGE 1: BLUEPRINT" else "STAGE 2: LIVE CARD", 
                        style = TextStyle(fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
                    ) 
                },
                navigationIcon = { 
                    IconButton(onClick = onCancel) { 
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White) 
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
                .imePadding()
        ) {
            if (isBlueprintStage) {
                // 🏗️ STAGE 1: THE BLUEPRINT (Vertical Squish Applied)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // METAL SELECT
                    Column {
                        Text("SELECT METAL", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        FunnelGrid(
                            options = listOf("Gold", "Silver", "Platinum", "Palladium"),
                            selected = when {
                                draftAsset.symbol.contains("XAU", true) || draftAsset.name.contains("Gold", true) -> "Gold"
                                draftAsset.symbol.contains("XAG", true) || draftAsset.name.contains("Silver", true) -> "Silver"
                                draftAsset.symbol.contains("XPT", true) || draftAsset.name.contains("Plat", true) -> "Platinum"
                                else -> "Palladium"
                            }
                        ) { metal ->
                            val ticker = when (metal) {
                                "Gold" -> "XAU"
                                "Silver" -> "XAG"
                                "Platinum" -> "XPT"
                                else -> "XPD"
                            }
                            draftAsset = draftAsset.copy(symbol = ticker, name = metal, displayName = metal)
                        }
                    }

                    // SHAPE SELECT
                    Column {
                        Text("SELECT SHAPE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        FunnelGrid(
                            options = listOf("Bar", "Coin", "Round"),
                            selected = draftAsset.physicalForm
                        ) { draftAsset = draftAsset.copy(physicalForm = it) }
                    }

                    // UNIT SELECT
                    Column {
                        Text("SELECT UNIT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        FunnelGrid(
                            options = listOf("OZ", "KILO", "GRAM"),
                            selected = draftAsset.weightUnit
                        ) { draftAsset = draftAsset.copy(weightUnit = it) }
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = { isBlueprintStage = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("PROCEED TO CARD", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(20.dp))
                }
            } else {
                // 🖼️ STAGE 2: THE LIVE CARD
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(20.dp))

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
                                            modifier = Modifier.width(45.dp)
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
                                    Text("QUANTITY", color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    SmartNumericField(
                                        value = draftAsset.amountHeld,
                                        onValueChange = { draftAsset = draftAsset.copy(amountHeld = it) },
                                        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    // INLINE NAME INPUT
                                    BasicTextField(
                                        value = draftAsset.displayName,
                                        onValueChange = { draftAsset = draftAsset.copy(displayName = it, name = it) },
                                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                        cursorBrush = SolidColor(Color.Yellow),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // PRICE/PREMIUM AREA
                                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                                    Text("SPOT PRICE", color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(text = formatCurrency(draftAsset.officialSpotPrice), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("PREMIUM ($)", color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    SmartNumericField(
                                        value = draftAsset.premium,
                                        onValueChange = { draftAsset = draftAsset.copy(premium = it) },
                                        textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp, textAlign = TextAlign.End),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(Modifier.height(12.dp))

                            // TOTAL VALUE PREVIEW
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ESTIMATED VALUE", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                val total = (draftAsset.officialSpotPrice * draftAsset.weight * draftAsset.amountHeld) + draftAsset.premium
                                Text(text = formatCurrency(total), color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    // 💾 FINALIZE BUTTON
                    Button(
                        onClick = { onSave(draftAsset) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("FINALIZE & VAULT", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }

                    TextButton(onClick = { isBlueprintStage = true }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("BACK TO BLUEPRINT", color = Color.Gray, fontSize = 12.sp)
                    }
                    
                    Spacer(Modifier.height(24.dp))
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
    modifier: Modifier = Modifier
) {
    // 🛠️ V7.2.8: Whole Number Fix - Use toString().removeSuffix(".0") for display
    var textState by remember(value) { 
        mutableStateOf(if (value == 0.0) "" else value.toString().removeSuffix(".0")) 
    }
    val isGhost = textState.isEmpty() || value == 0.0

    BasicTextField(
        value = textState,
        onValueChange = {
            if (it.length <= 10) {
                textState = it
                onValueChange(it.toDoubleOrNull() ?: 0.0)
            }
        },
        modifier = modifier.onFocusChanged { focusState ->
            if (focusState.isFocused) {
                // Auto-clear logic: Clear if it's a default/ghost value
                if (value == 0.0 || value == 1.0) {
                    textState = ""
                }
            }
        },
        textStyle = textStyle.copy(color = if (isGhost) textStyle.color.copy(alpha = 0.3f) else textStyle.color),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        cursorBrush = SolidColor(Color.Yellow),
        singleLine = true
    )
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
