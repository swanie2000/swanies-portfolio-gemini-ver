@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.holdings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.MetalsProvider
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.settings.ThemeViewModel
import java.util.UUID

@Composable
fun ManualAssetEntryScreen(
    onSave: (AssetEntity) -> Unit,
    onCancel: () -> Unit
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    val bgColor = remember(siteBgHex) { Color(android.graphics.Color.parseColor(siteBgHex)) }
    val textColor = remember(siteTextHex) { Color(android.graphics.Color.parseColor(siteTextHex)) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // State
    var selectedMetalType by remember { mutableStateOf("Gold") }
    var customCategoryName by remember { mutableStateOf("") }
    var line1 by remember { mutableStateOf("") }
    var line2 by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("1.0") }
    var premium by remember { mutableStateOf("0.0") }
    var quantity by remember { mutableStateOf("1") }

    val row1Metals = listOf("Gold", "Silver", "Platinum")
    val row2Metals = listOf("Palladium", "Custom")
    val charLimit = 12

    Scaffold(
        containerColor = bgColor,
        topBar = {
            // HEADER: Swan Center-Aligned, Utility Buttons Centered in lateral space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(130.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // BACK BUTTON
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // SWAN LOGO (Centered, 120dp)
                Image(
                    painter = painterResource(id = R.drawable.swanie_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(120.dp)
                )

                // SAVE BUTTON (Centered in right space)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val baseSymbol = when (selectedMetalType) {
                                "Gold" -> "XAU"; "Silver" -> "XAG"; "Platinum" -> "XPT"; "Palladium" -> "XPD"
                                else -> "CUSTOM"
                            }
                            val spotPrice = MetalsProvider.preciousMetals.find { it.symbol == baseSymbol }?.currentPrice ?: 0.0
                            val weightVal = weight.toDoubleOrNull() ?: 0.0
                            val premiumVal = premium.toDoubleOrNull() ?: 0.0
                            val qtyVal = quantity.toDoubleOrNull() ?: 0.0

                            val displaySymbol = if (selectedMetalType == "Custom") {
                                customCategoryName.take(4).uppercase().ifBlank { "CUST" }
                            } else {
                                when (selectedMetalType) {
                                    "Gold" -> "GOLD"; "Silver" -> "SILVER"; "Platinum" -> "PLAT"; "Palladium" -> "PALL"
                                    else -> "CUST"
                                }
                            }

                            val fullName = "${line1.trim()}\n${line2.trim()}"

                            val newAsset = AssetEntity(
                                coinId = "custom_${UUID.randomUUID()}",
                                symbol = displaySymbol,
                                name = fullName,
                                amountHeld = qtyVal,
                                currentPrice = (spotPrice * weightVal) + premiumVal,
                                category = AssetCategory.METAL,
                                isCustom = true,
                                weight = weightVal,
                                premium = premiumVal,
                                baseSymbol = baseSymbol,
                                lastUpdated = System.currentTimeMillis(),
                                imageUrl = "", change24h = 0.0, displayOrder = 0, priceChange24h = 0.0, marketCapRank = 0, sparklineData = emptyList()
                            )
                            onSave(newAsset)
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Yellow)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER TEXT
            Text(
                text = "Add Asset Card",
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                "Select Type",
                color = textColor.copy(0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // PILL BUTTONS ROW 1: Centered with optimized spacing for "Platinum"
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                row1Metals.forEach { metal ->
                    FilterChip(
                        selected = selectedMetalType == metal,
                        onClick = { selectedMetalType = metal },
                        label = { Text(metal, fontSize = 13.sp) }, // Slightly smaller font to prevent wrap
                        shape = CircleShape,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = textColor,
                            selectedLabelColor = bgColor,
                            labelColor = textColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedMetalType == metal,
                            borderColor = textColor.copy(alpha = 0.3f),
                            selectedBorderColor = textColor
                        )
                    )
                }
            }

            // PILL BUTTONS ROW 2: Centered
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                row2Metals.forEach { metal ->
                    FilterChip(
                        selected = selectedMetalType == metal,
                        onClick = { selectedMetalType = metal },
                        label = { Text(metal, fontSize = 13.sp) },
                        shape = CircleShape,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = textColor,
                            selectedLabelColor = bgColor,
                            labelColor = textColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedMetalType == metal,
                            borderColor = textColor.copy(alpha = 0.3f),
                            selectedBorderColor = textColor
                        )
                    )
                }
            }

            // CUSTOM NAME POPUP
            AnimatedVisibility(visible = selectedMetalType == "Custom") {
                OutlinedTextField(
                    value = customCategoryName,
                    onValueChange = { customCategoryName = it },
                    label = { Text("Custom Category Name") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = textColor, focusedLabelColor = textColor),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).onFocusChanged { if(it.isFocused) customCategoryName = "" },
                    singleLine = true
                )
            }

            Spacer(Modifier.height(24.dp))
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = textColor,
                unfocusedBorderColor = textColor.copy(0.3f),
                focusedLabelColor = textColor,
                cursorColor = textColor
            )

            // DUAL LINE INPUTS (Auto-clear on focus)
            OutlinedTextField(
                value = line1, onValueChange = { if (it.length <= charLimit) line1 = it },
                label = { Text("Card Line 1 (Max $charLimit)") },
                shape = RoundedCornerShape(12.dp), colors = fieldColors,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).onFocusChanged { if(it.isFocused) line1 = "" },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), singleLine = true
            )
            OutlinedTextField(
                value = line2, onValueChange = { if (it.length <= charLimit) line2 = it },
                label = { Text("Card Line 2 (Max $charLimit)") },
                shape = RoundedCornerShape(12.dp), colors = fieldColors,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).onFocusChanged { if(it.isFocused) line2 = "" },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), singleLine = true
            )

            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Weight (oz)") }, shape = RoundedCornerShape(12.dp), colors = fieldColors,
                    modifier = Modifier.weight(1f).onFocusChanged { if(it.isFocused) weight = "" },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next), singleLine = true
                )
                OutlinedTextField(
                    value = quantity, onValueChange = { quantity = it },
                    label = { Text("Quantity") }, shape = RoundedCornerShape(12.dp), colors = fieldColors,
                    modifier = Modifier.weight(1f).onFocusChanged { if(it.isFocused) quantity = "" },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next), singleLine = true
                )
            }
            OutlinedTextField(
                value = premium, onValueChange = { premium = it },
                label = { Text("Premium per oz (USD)") }, shape = RoundedCornerShape(12.dp), colors = fieldColors,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).onFocusChanged { if(it.isFocused) premium = "" },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }), singleLine = true
            )

            Spacer(modifier = Modifier.height(250.dp))
        }
    }
}