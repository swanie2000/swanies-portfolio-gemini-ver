package com.swanie.portfolio.ui.entry

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.holdings.FullAssetCard
import com.swanie.portfolio.ui.holdings.FunnelGrid
import kotlinx.coroutines.launch

/**
 * 🛠️ V7.2.5 MISSION: ASSET ARCHITECT
 * Finalized WYSIWYG screen with Touch-to-Edit.
 * Logic is passed back to NavGraph for V19 database persistence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetArchitectScreen(
    initialSymbol: String = "GOLD",
    initialPrice: Double = 0.0,
    initialSource: String = "Manual",
    onSave: (AssetEntity) -> Unit, // 🛠️ Fixed: Now passes the entity back
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // 🛡️ V19 ARCHITECT STATE (Reactive Draft)
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
                amountHeld = 1.0
            )
        )
    }

    val scrollState = rememberLazyListState()
    val nameFocusRequester = remember { FocusRequester() }
    val weightFocusRequester = remember { FocusRequester() }
    val quantityFocusRequester = remember { FocusRequester() }

    // Smooth Scroll & Focus logic for Touch-to-Edit
    val onCardPartTapped: (String) -> Unit = { part ->
        scope.launch {
            when (part) {
                "ICON" -> scrollState.animateScrollToItem(3) // Scroll to Shape picker
                "TITLE" -> {
                    scrollState.animateScrollToItem(2) // Scroll to Name field
                    nameFocusRequester.requestFocus()
                }
                "MASS" -> {
                    scrollState.animateScrollToItem(4) // Scroll to Weight field
                    weightFocusRequester.requestFocus()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ASSET ARCHITECT", style = TextStyle(fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 2.sp)) },
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
        ) {
            // 🖼️ THE INTERACTIVE PREVIEW (TOUCH-TO-EDIT)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White.copy(0.05f), RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    FullAssetCard(
                        asset = draftAsset,
                        isExpanded = true,
                        isEditing = false,
                        isDragging = false,
                        showEditButton = false,
                        cardBg = Color(0xFF1A1A1A),
                        cardText = Color.White,
                        onExpandToggle = {},
                        onEditRequest = {},
                        onSave = { _, _, _, _, _ -> },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 👆 OVERLAY SENSORS: Mapping taps to edit sections
                    Row(modifier = Modifier.matchParentSize()) {
                        // Icon Sensor
                        Box(modifier = Modifier.weight(0.9f).fillMaxHeight().clickable { onCardPartTapped("ICON") })
                        // Identity Sensor
                        Box(modifier = Modifier.weight(1.4f).fillMaxHeight().clickable { onCardPartTapped("TITLE") })
                        // Mass/Price Sensor
                        Box(modifier = Modifier.weight(1.1f).fillMaxHeight().clickable { onCardPartTapped("MASS") })
                    }
                }
            }

            // 🛠️ THE EDIT ZONE
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // 1. MATERIAL TYPE (V19 Real-time Tinting)
                item {
                    Column {
                        Text("MATERIAL TYPE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
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
                }

                // 2. IDENTITY (Text Entry)
                item {
                    Column {
                        Text("ASSET NAME", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = draftAsset.displayName,
                            onValueChange = { draftAsset = draftAsset.copy(displayName = it, name = it) },
                            modifier = Modifier.fillMaxWidth().focusRequester(nameFocusRequester),
                            textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Bold),
                            placeholder = { Text("e.g. Eagle, Maple, Bar", color = Color.White.copy(0.3f)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow, unfocusedBorderColor = Color.White.copy(0.2f))
                        )
                    }
                }

                // 3. PHYSICAL FORM (V19 Shape Logic)
                item {
                    Column {
                        Text("PHYSICAL FORM", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        FunnelGrid(
                            options = listOf("Bar", "Coin", "Round"),
                            selected = draftAsset.physicalForm
                        ) { form ->
                            draftAsset = draftAsset.copy(physicalForm = form)
                        }
                    }
                }

                // 4. MASS (Weight & V19 Units)
                item {
                    Column {
                        Text("WEIGHT & UNIT", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = if (draftAsset.weight == 0.0) "" else draftAsset.weight.toString(),
                                onValueChange = { val w = it.toDoubleOrNull() ?: 0.0; draftAsset = draftAsset.copy(weight = w) },
                                modifier = Modifier.weight(1f).focusRequester(weightFocusRequester),
                                label = { Text("Weight", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow)
                            )
                            Box(modifier = Modifier.weight(1.2f)) {
                                FunnelGrid(
                                    options = listOf("OZ", "KILO", "GRAM"),
                                    selected = draftAsset.weightUnit
                                ) { unit ->
                                    draftAsset = draftAsset.copy(weightUnit = unit)
                                }
                            }
                        }
                    }
                }

                // 5. QUANTITY
                item {
                    Column {
                        Text("QUANTITY HELD", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = if (draftAsset.amountHeld == 0.0) "" else draftAsset.amountHeld.toString(),
                            onValueChange = { val q = it.toDoubleOrNull() ?: 0.0; draftAsset = draftAsset.copy(amountHeld = q) },
                            modifier = Modifier.fillMaxWidth().focusRequester(quantityFocusRequester),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Yellow)
                        )
                    }
                }
            }

            // 💾 THE SAVE BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Button(
                    onClick = { onSave(draftAsset) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("FINALIZE & VAULT", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }
}
