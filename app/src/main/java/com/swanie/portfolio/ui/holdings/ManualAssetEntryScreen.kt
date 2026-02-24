package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.swanie.portfolio.data.MetalsProvider
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAssetEntryScreen(
    onSave: (AssetEntity) -> Unit,
    onCancel: () -> Unit
) {
    var selectedMetalType by remember { mutableStateOf("Gold") }
    var assetName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("1.0") }
    var premium by remember { mutableStateOf("0.0") }
    var quantity by remember { mutableStateOf("1") }

    val metalTypes = listOf("Gold", "Silver", "Platinum")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Custom Metal") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val baseSymbol = when (selectedMetalType) {
                    "Gold" -> "XAU"
                    "Silver" -> "XAG"
                    "Platinum" -> "XPT"
                    else -> ""
                }

                val spotPrice = MetalsProvider.preciousMetals.find { it.symbol == baseSymbol }?.currentPrice ?: 0.0

                val weightValue = weight.toDoubleOrNull() ?: 1.0
                val premiumValue = premium.toDoubleOrNull() ?: 0.0
                val quantityValue = quantity.toDoubleOrNull() ?: 0.0

                val pricePerUnit = (spotPrice * weightValue) + premiumValue

                val newAsset = AssetEntity(
                    coinId = "custom_${UUID.randomUUID()}",
                    symbol = assetName.take(5).uppercase().ifBlank { selectedMetalType.take(3).uppercase() },
                    name = assetName,
                    amountHeld = quantityValue,
                    currentPrice = pricePerUnit,
                    change24h = 0.0,
                    displayOrder = 0,
                    lastUpdated = System.currentTimeMillis(),
                    imageUrl = "",
                    category = AssetCategory.METAL,
                    isCustom = true,
                    weight = weightValue,
                    premium = premiumValue,
                    baseSymbol = baseSymbol
                )
                onSave(newAsset)
            }) {
                Icon(Icons.Default.Check, contentDescription = "Save Asset")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select Metal Type", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                metalTypes.forEach { metal ->
                    FilterChip(
                        selected = selectedMetalType == metal,
                        onClick = { selectedMetalType = metal },
                        label = { Text(metal) }
                    )
                }
            }

            OutlinedTextField(
                value = assetName,
                onValueChange = { assetName = it },
                label = { Text("Asset Name (e.g., '''Silver Eagle''')") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)){
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (oz)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }


            OutlinedTextField(
                value = premium,
                onValueChange = { premium = it },
                label = { Text("Premium Per Unit (USD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Text("$") }
            )
        }
    }
}
