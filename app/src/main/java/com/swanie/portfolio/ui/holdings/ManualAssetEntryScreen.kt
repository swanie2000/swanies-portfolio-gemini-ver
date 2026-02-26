package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.MetalsProvider
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.settings.ThemeViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
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
    var assetName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("1.0") }
    var premium by remember { mutableStateOf("0.0") }
    var quantity by remember { mutableStateOf("1") }

    val metalTypes = listOf("Gold", "Silver", "Platinum")

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor, titleContentColor = textColor),
                title = { Text("Add Custom Metal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = Color.Yellow,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    val baseSymbol = when (selectedMetalType) {
                        "Gold" -> "XAU"
                        "Silver" -> "XAG"
                        "Platinum" -> "XPT"
                        else -> ""
                    }
                    val spotPrice = MetalsProvider.preciousMetals.find { it.symbol == baseSymbol }?.currentPrice ?: 0.0
                    val weightVal = weight.toDoubleOrNull() ?: 0.0
                    val premiumVal = premium.toDoubleOrNull() ?: 0.0
                    val qtyVal = quantity.toDoubleOrNull() ?: 0.0

                    // FIXED: Added missing parameters to constructor
                    val newAsset = AssetEntity(
                        coinId = "custom_${UUID.randomUUID()}",
                        symbol = assetName.take(5).uppercase().ifBlank { selectedMetalType.take(3).uppercase() },
                        name = assetName,
                        amountHeld = qtyVal,
                        currentPrice = (spotPrice * weightVal) + premiumVal,
                        category = AssetCategory.METAL,
                        isCustom = true,
                        weight = weightVal,
                        premium = premiumVal,
                        baseSymbol = baseSymbol,
                        lastUpdated = System.currentTimeMillis(),
                        imageUrl = "",      // REQUIRED PARAMETER
                        change24h = 0.0,    // REQUIRED PARAMETER
                        displayOrder = 0,   // REQUIRED PARAMETER
                        priceChange24h = 0.0,
                        marketCapRank = 0,
                        sparklineData = emptyList()
                    )
                    onSave(newAsset)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Save", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BRANDING: Swan Logo
            Image(
                painter = painterResource(id = R.drawable.swan_launcher_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .alpha(0.2f)
                    .padding(vertical = 16.dp)
            )

            Text(
                "Select Metal Type",
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                metalTypes.forEach { metal ->
                    FilterChip(
                        selected = selectedMetalType == metal,
                        onClick = { selectedMetalType = metal },
                        label = { Text(metal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = textColor,
                            selectedLabelColor = bgColor,
                            labelColor = textColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Text Entry Components
            val commonModifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = textColor,
                unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                focusedLabelColor = textColor
            )

            // ASSET NAME
            OutlinedTextField(
                value = assetName,
                onValueChange = { assetName = it },
                label = { Text("Asset Name") },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = commonModifier.clickable { assetName = "" }, // Clear on touch
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // WEIGHT
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (oz)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f).clickable { weight = "" }, // Clear on touch
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true
                )
                // QUANTITY
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f).clickable { quantity = "" }, // Clear on touch
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true
                )
            }

            // PREMIUM
            OutlinedTextField(
                value = premium,
                onValueChange = { premium = it },
                label = { Text("Premium (USD)") },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = commonModifier.clickable { premium = "" }, // Clear on touch
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true
            )

            // Bottom Spacer to ensure the last field scrolls above keyboard
            Spacer(modifier = Modifier.height(300.dp))
        }
    }
}