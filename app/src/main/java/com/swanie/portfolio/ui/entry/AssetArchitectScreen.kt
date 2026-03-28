package com.swanie.portfolio.ui.entry

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import com.swanie.portfolio.ui.holdings.formatAmount
import com.swanie.portfolio.ui.holdings.formatCurrency
import java.util.Locale

/**
 * 🛠️ V7.2.6 MISSION: DIRECT-CARD EDITING
 * A high-fidelity WYSIWYG screen where the card itself is the input form.
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
                title = { Text("ASSET ARCHITECT", style = TextStyle(fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)) },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White) } },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // 🖼️ THE DIRECT-EDIT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // ICON AREA (V19 Logic)
                        Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                            MetalIcon(
                                name = draftAsset.name,
                                weight = draftAsset.weight,
                                unit = draftAsset.weightUnit,
                                physicalForm = draftAsset.physicalForm,
                                imageUrl = draftAsset.imageUrl,
                                localPath = draftAsset.localIconPath,
                                category = draftAsset.category
                            )
                            Spacer(Modifier.height(8.dp))
                            // INLINE WEIGHT INPUT
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BasicTextField(
                                    value = if (draftAsset.weight == 0.0) "" else draftAsset.weight.toString(),
                                    onValueChange = { draftAsset = draftAsset.copy(weight = it.toDoubleOrNull() ?: 0.0) },
                                    textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    cursorBrush = SolidColor(Color.Yellow),
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(text = draftAsset.weightUnit, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Black, fontSize = 10.sp)
                            }
                        }

                        // IDENTITY AREA
                        Column(modifier = Modifier.weight(1.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("QUANTITY", color = Color.White.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(
                                value = draftAsset.amountHeld.toString(),
                                onValueChange = { draftAsset = draftAsset.copy(amountHeld = it.toDoubleOrNull() ?: 0.0) },
                                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                cursorBrush = SolidColor(Color.Yellow),
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
                            BasicTextField(
                                value = draftAsset.premium.toString(),
                                onValueChange = { draftAsset = draftAsset.copy(premium = it.toDoubleOrNull() ?: 0.0) },
                                textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 14.sp, textAlign = TextAlign.End),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                cursorBrush = SolidColor(Color.Yellow),
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

            Spacer(Modifier.height(24.dp))

            // 🛠️ THE COMPACT TOGGLE BAR (Anchored below Card)
            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // MATERIAL TOGGLE
                Column {
                    Text("MATERIAL", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    FunnelGrid(
                        options = listOf("Gold", "Silver", "Platinum", "Other"),
                        selected = when {
                            draftAsset.name.contains("Gold", true) -> "Gold"
                            draftAsset.name.contains("Silver", true) -> "Silver"
                            draftAsset.name.contains("Plat", true) -> "Platinum"
                            else -> "Other"
                        }
                    ) { material ->
                        val currentBase = draftAsset.name
                            .replace("Gold", "", true)
                            .replace("Silver", "", true)
                            .replace("Platinum", "", true).trim()
                        val newName = if (material == "Other") currentBase else "$material $currentBase".trim()
                        draftAsset = draftAsset.copy(name = newName, displayName = newName)
                    }
                }

                // SHAPE TOGGLE
                Column {
                    Text("SHAPE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    FunnelGrid(
                        options = listOf("Bar", "Coin", "Round"),
                        selected = draftAsset.physicalForm
                    ) { draftAsset = draftAsset.copy(physicalForm = it) }
                }

                // UNIT TOGGLE
                Column {
                    Text("UNIT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    FunnelGrid(
                        options = listOf("OZ", "KILO", "GRAM"),
                        selected = draftAsset.weightUnit
                    ) { draftAsset = draftAsset.copy(weightUnit = it) }
                }
            }

            Spacer(Modifier.weight(1f))

            // 💾 FINALIZE BUTTON
            Button(
                onClick = { onSave(draftAsset) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("FINALIZE & VAULT", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}
