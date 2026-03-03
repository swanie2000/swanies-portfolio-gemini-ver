package com.swanie.portfolio.ui.holdings

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.MetalsProvider
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import java.util.UUID

@Composable
fun AssetPickerScreen(
    navController: NavController,
    onAssetSelected: (AssetEntity) -> Unit // Updated signature to handle funnel data
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))

    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val viewModel: AssetViewModel = hiltViewModel()
    val searchResults by viewModel.searchResults.collectAsState()

    // Funnel State
    var showFunnel by remember { mutableStateOf(false) }
    var funnelInitialMetal by remember { mutableStateOf("Silver") }

    LaunchedEffect(Unit) {
        viewModel.searchCoins("")
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(bgColor)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; viewModel.searchCoins(it) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            placeholder = { Text("Search (e.g., Bitcoin, Gold)", color = textColor.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor, unfocusedTextColor = textColor, cursorColor = textColor,
                focusedBorderColor = textColor, unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
            )
        )

        // MANUAL ADD TRIGGER: Launches the 5-step funnel
        AnimatedVisibility(visible = searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.Center) {
                TextButton(onClick = {
                    funnelInitialMetal = "Silver"
                    showFunnel = true
                }) {
                    Text("Manual Add Asset", color = textColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        if (searchResults.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.weight(0.1f))
                Image(painter = painterResource(id = R.drawable.swan_launcher_icon), contentDescription = null, modifier = Modifier.size(220.dp).alpha(0.08f), colorFilter = ColorFilter.tint(textColor))
                Spacer(modifier = Modifier.weight(3.0f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                items(searchResults) { asset ->
                    CoinItem(asset = asset, textColor = textColor, onAssetSelected = {
                        if (it.category == AssetCategory.METAL) {
                            funnelInitialMetal = it.name.split(" ").first()
                            showFunnel = true
                        } else {
                            onAssetSelected(it)
                        }
                    })
                    HorizontalDivider(color = textColor.copy(alpha = 0.1f), thickness = 1.dp)
                }
            }
        }
    }

    if (showFunnel) {
        MetalSelectionFunnel(
            initialMetal = funnelInitialMetal,
            onDismiss = { showFunnel = false },
            onConfirmed = { funnelMetal, funnelForm, funnelWeight, isKilo, funnelQty, funnelPrem ->
                val baseSymbol = when (funnelMetal) {
                    "Gold" -> "XAU"; "Silver" -> "XAG"; "Platinum" -> "XPT"; "Palladium" -> "XPD"; else -> "CUSTOM"
                }
                val spotPrice = MetalsProvider.preciousMetals.find { it.symbol == baseSymbol }?.currentPrice ?: 0.0

                val displaySymbol = when (funnelMetal) {
                    "Gold" -> "GOLD"; "Silver" -> "SILV"; "Platinum" -> "PLAT"; "Palladium" -> "PALL"; else -> "CUST"
                }

                // Format the name for the MyHoldings 2-line display
                val finalName = "${funnelMetal.uppercase()}\n${if (isKilo) "KILO" else funnelForm.uppercase()}"

                val newAsset = AssetEntity(
                    coinId = "custom_${UUID.randomUUID()}", symbol = displaySymbol,
                    name = finalName,
                    amountHeld = funnelQty.toDoubleOrNull() ?: 1.0, currentPrice = spotPrice,
                    change24h = 0.0, displayOrder = 0, lastUpdated = System.currentTimeMillis(),
                    imageUrl = "", category = AssetCategory.METAL, weight = funnelWeight,
                    premium = funnelPrem.toDoubleOrNull() ?: 0.0, isCustom = true,
                    baseSymbol = baseSymbol, decimalPreference = 2
                )
                onAssetSelected(newAsset)
                showFunnel = false
            }
        )
    }
}

@Composable
fun MetalSelectionFunnel(
    initialMetal: String,
    onDismiss: () -> Unit,
    onConfirmed: (String, String, Double, Boolean, String, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedMetal by remember { mutableStateOf(initialMetal) }
    var selectedForm by remember { mutableStateOf("Bars") }
    var selectedWeight by remember { mutableDoubleStateOf(1.0) }
    var isKiloSelected by remember { mutableStateOf(false) }

    var qtyInput by remember { mutableStateOf("") }
    var premInput by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    val metals = listOf("Gold", "Silver", "Platinum", "Palladium", "Custom")
    val forms = listOf("Bars", "Coins", "Rounds", "Custom")
    val commonWeights = mapOf(
        "1/10 OZ" to (0.1 to false), "1 OZ" to (1.0 to false), "10 OZ" to (10.0 to false),
        "100 OZ" to (100.0 to false), "1 KILO" to (1.0 to true), "1 GRAM" to (1.0 to false),
        "10 GRAM" to (10.0 to false), "100 GRAM" to (100.0 to false), "5 DWT" to (5.0 to false), "Custom" to (-1.0 to false)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            border = BorderStroke(1.dp, Color.White.copy(0.1f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when(step) { 1 -> "SELECT METAL"; 2 -> "SELECT FORM"; 3 -> "SELECT WEIGHT"; 4 -> "QUANTITY HELD"; else -> "PREMIUM PER UNIT" },
                    color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp
                )
                Spacer(Modifier.height(20.dp))

                when (step) {
                    1 -> FunnelGrid(metals, selectedMetal) { selectedMetal = it; step = 2 }
                    2 -> FunnelGrid(forms, selectedForm) { selectedForm = it; step = 3 }
                    3 -> FunnelGrid(commonWeights.keys.toList(), "") { label ->
                        val data = commonWeights[label] ?: (1.0 to false)
                        selectedWeight = data.first
                        isKiloSelected = data.second
                        step = 4
                    }
                    4 -> {
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        Column {
                            OutlinedTextField(
                                value = qtyInput,
                                onValueChange = { qtyInput = it },
                                placeholder = { Text("0", color = Color.White.copy(0.2f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                textStyle = TextStyle(color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { if(qtyInput.isNotBlank()) step = 5 }),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow, unfocusedBorderColor = Color.White.copy(0.2f))
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = { if (qtyInput.isNotBlank()) step = 5 }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("NEXT STEP", fontWeight = FontWeight.Bold) }
                        }
                    }
                    5 -> {
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        Column {
                            OutlinedTextField(
                                value = premInput,
                                onValueChange = { premInput = it },
                                placeholder = { Text("$0.00", color = Color.White.copy(0.2f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                textStyle = TextStyle(color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { onConfirmed(selectedMetal, selectedForm, selectedWeight, isKiloSelected, qtyInput, premInput.ifBlank { "0.0" }) }),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow, unfocusedBorderColor = Color.White.copy(0.2f))
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = { onConfirmed(selectedMetal, selectedForm, selectedWeight, isKiloSelected, qtyInput, premInput.ifBlank { "0.0" }) }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("FINALIZE & SAVE", fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                if (step > 1) {
                    TextButton(onClick = { step-- }, modifier = Modifier.padding(top = 16.dp)) { Text("BACK", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun FunnelGrid(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { option ->
                    val isSelected = option == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.Yellow else Color.White.copy(0.05f))
                            .clickable { onSelect(option) }
                            .border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = option.uppercase(), color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CoinItem(asset: AssetEntity, textColor: Color, onAssetSelected: (AssetEntity) -> Unit) {
    TextButton(onClick = { onAssetSelected(asset) }, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (asset.category == AssetCategory.METAL) {
                val color = when (asset.symbol) { "XAU" -> Color(0xFFFFD700); "XAG" -> Color(0xFFC0C0C0); else -> Color.Gray }
                Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(color))
            } else {
                AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(text = "${asset.name} (${asset.symbol.uppercase()})", color = textColor, fontWeight = FontWeight.Medium)
        }
    }
}