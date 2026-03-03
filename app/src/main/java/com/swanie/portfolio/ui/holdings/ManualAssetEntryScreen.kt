@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.holdings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
    val cardBgHex by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextHex by themeViewModel.cardTextColor.collectAsState()

    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))
    val cardBg = Color(android.graphics.Color.parseColor(cardBgHex.ifBlank { "#121212" }))
    val cardText = Color(android.graphics.Color.parseColor(cardTextHex.ifBlank { "#FFFFFF" }))

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var selectedMetalType by remember { mutableStateOf("Gold") }
    var menuExpanded by remember { mutableStateOf(false) }
    var line1 by remember { mutableStateOf("") }
    var line2 by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("1.0") }
    var premium by remember { mutableStateOf("0.0") }
    var quantity by remember { mutableStateOf("1") }

    val metalOptions = listOf("Gold", "Silver", "Platinum", "Palladium", "Custom")
    val charLimit = 12

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(80.dp).padding(horizontal = 16.dp)) {
                IconButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor, modifier = Modifier.size(28.dp))
                }
                Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.align(Alignment.Center).size(85.dp))
                IconButton(
                    onClick = {
                        val baseSymbol = when (selectedMetalType) {
                            "Gold" -> "XAU"; "Silver" -> "XAG"; "Platinum" -> "XPT"; "Palladium" -> "XPD"; else -> "CUSTOM"
                        }
                        val spotPrice = MetalsProvider.preciousMetals.find { it.symbol == baseSymbol }?.currentPrice ?: 0.0
                        val weightVal = weight.toDoubleOrNull() ?: 1.0
                        val premiumVal = premium.toDoubleOrNull() ?: 0.0

                        // SYMBOL FIX: No more "CUST" unless it's actually custom
                        val displaySymbol = if (selectedMetalType == "Custom") line1.take(4).ifBlank { "CUST" }.uppercase()
                        else when (selectedMetalType) { "Gold" -> "GOLD"; "Silver" -> "SILV"; "Platinum" -> "PLAT"; "Palladium" -> "PALL"; else -> "CUST" }

                        val newAsset = AssetEntity(
                            coinId = "custom_${UUID.randomUUID()}", symbol = displaySymbol,
                            name = "${line1.ifBlank { "SILVER" }.trim()}\n${line2.ifBlank { "BARS" }.trim()}",
                            amountHeld = quantity.toDoubleOrNull() ?: 1.0, currentPrice = spotPrice + (if(weightVal > 0) premiumVal/weightVal else premiumVal),
                            change24h = 0.0, displayOrder = 0, lastUpdated = System.currentTimeMillis(), imageUrl = "",
                            category = AssetCategory.METAL, weight = weightVal, premium = premiumVal, isCustom = true, baseSymbol = baseSymbol, decimalPreference = 2
                        )
                        onSave(newAsset)
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).clip(CircleShape).background(Color.Yellow).size(44.dp)
                ) { Icon(Icons.Default.Add, null, tint = Color.Black) }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ADD ASSET HOLDING", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, cardText.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // TAPPABLE ICON
                        Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)).background(Color.Yellow.copy(alpha = glowAlpha)).border(1.dp, Color.Yellow.copy(0.2f), RoundedCornerShape(10.dp)).clickable { menuExpanded = true }, contentAlignment = Alignment.Center) {
                                MetalIcon(name = selectedMetalType, size = 34) // Borrows from MyHoldingsScreen.kt
                                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(cardBg).border(1.dp, textColor.copy(0.1f), RoundedCornerShape(8.dp))) {
                                    metalOptions.forEach { metal -> DropdownMenuItem(text = { Text(metal, color = if(selectedMetalType == metal) Color.Yellow else textColor, fontWeight = FontWeight.Bold) }, onClick = { selectedMetalType = metal; menuExpanded = false }) }
                                }
                            }
                            Text("TYPE", color = cardText.copy(0.4f), fontWeight = FontWeight.Bold, fontSize = 8.sp, modifier = Modifier.padding(top = 4.dp))
                        }

                        // QUANTITY & NAME (AUTO-CLEAR LOGIC)
                        Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("QUANTITY", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(
                                value = quantity, onValueChange = { quantity = it },
                                modifier = Modifier.width(80.dp).background(Color.Yellow.copy(alpha = glowAlpha), RoundedCornerShape(4.dp)).onFocusChanged { if(it.isFocused && (quantity == "1" || quantity == "0")) quantity = "" },
                                textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                cursorBrush = SolidColor(Color.Yellow),
                                decorationBox = { inner -> Box(contentAlignment = Alignment.Center) { if(quantity.isEmpty()) Text("1", color = Color.Yellow.copy(0.3f), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold); inner() } }
                            )
                            Spacer(Modifier.height(8.dp))
                            // Line 1
                            BasicTextField(
                                value = line1, onValueChange = { if(it.length <= charLimit) line1 = it },
                                modifier = Modifier.fillMaxWidth().height(30.dp).background(cardText.copy(alpha = glowAlpha), RoundedCornerShape(4.dp)).onFocusChanged { if(it.isFocused && line1.isEmpty()) line1 = "" },
                                textStyle = TextStyle(color = cardText, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center),
                                cursorBrush = SolidColor(Color.Yellow),
                                decorationBox = { inner -> Box(contentAlignment = Alignment.Center) { if(line1.isEmpty()) Text("SILVER", color = cardText.copy(0.2f), fontSize = 13.sp, fontWeight = FontWeight.Bold); inner() } }
                            )
                            Spacer(Modifier.height(4.dp))
                            // Line 2
                            BasicTextField(
                                value = line2, onValueChange = { if(it.length <= charLimit) line2 = it },
                                modifier = Modifier.fillMaxWidth().height(30.dp).background(cardText.copy(alpha = glowAlpha), RoundedCornerShape(4.dp)).onFocusChanged { if(it.isFocused && line2.isEmpty()) line2 = "" },
                                textStyle = TextStyle(color = cardText, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center),
                                cursorBrush = SolidColor(Color.Yellow),
                                decorationBox = { inner -> Box(contentAlignment = Alignment.Center) { if(line2.isEmpty()) Text("BARS", color = cardText.copy(0.2f), fontSize = 13.sp, fontWeight = FontWeight.Bold); inner() } }
                            )
                        }

                        // SPARKLINE (PLACEHOLDER)
                        Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                            Box(Modifier.width(75.dp).height(32.dp).background(cardText.copy(0.05f), RoundedCornerShape(4.dp)))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.Gray.copy(alpha = 0.15f)).padding(horizontal = 4.dp)) { Icon(painterResource(R.drawable.swan_launcher_icon), null, tint = Color.Gray, modifier = Modifier.size(14.dp)); Text("0.00%", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black) }
                        }
                    }

                    Spacer(Modifier.height(10.dp)); HorizontalDivider(color = cardText.copy(alpha = 0.05f))

                    // BOTTOM ROW (WEIGHT AUTO-CLEAR)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text("PRICE", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("$---", color = cardText.copy(0.3f), fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UNIT WEIGHT", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(
                                value = weight, onValueChange = { weight = it },
                                modifier = Modifier.width(70.dp).background(cardText.copy(alpha = glowAlpha), RoundedCornerShape(4.dp)).onFocusChanged { if(it.isFocused && (weight == "1.0" || weight == "1")) weight = "" },
                                textStyle = TextStyle(color = cardText, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                cursorBrush = SolidColor(Color.Yellow),
                                decorationBox = { inner -> Box(contentAlignment = Alignment.Center) { if(weight.isEmpty()) Text("1.0", color = cardText.copy(0.3f), fontSize = 15.sp, fontWeight = FontWeight.Bold); inner() } }
                            )
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text("TOTAL VALUE", color = cardText.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black); Text("$---", color = cardText.copy(0.3f), fontWeight = FontWeight.Black, fontSize = 17.sp) }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            // PREMIUM (AUTO-CLEAR)
            OutlinedTextField(
                value = premium, onValueChange = { premium = it },
                label = { Text("Premium per oz (USD)") }, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = Color.Yellow, focusedLabelColor = Color.Yellow, cursorColor = Color.Yellow),
                modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused && (premium == "0.0" || premium == "0")) premium = "" },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true
            )
            Spacer(Modifier.height(400.dp))
        }
    }
}