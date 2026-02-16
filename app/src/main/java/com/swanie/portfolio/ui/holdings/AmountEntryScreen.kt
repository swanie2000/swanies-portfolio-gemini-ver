package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.local.AssetEntity

@Composable
fun AmountEntryScreen(
    coinId: String,
    symbol: String,
    name: String,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: AmountEntryViewModel = viewModel(
        factory = AmountEntryViewModelFactory(db.assetDao())
    )

    var amount by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter Amount for $symbol", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.Cyan,
                    focusedBorderColor = Color.Cyan,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Button(
                onClick = {
                    val amountHeld = amount.toDoubleOrNull() ?: 0.0
                    if (amountHeld > 0) {
                        // For testing, let's use a placeholder price.
                        val placeholderPrice = when(symbol) {
                            "XRP" -> 0.52
                            "BTC" -> 68000.0
                            "ETH" -> 3400.0
                            "Gold" -> 2300.0
                            else -> 1.0
                        }
                        val asset = AssetEntity(
                            coinId = coinId,
                            symbol = symbol,
                            name = name,
                            amountHeld = amountHeld,
                            currentPrice = placeholderPrice,
                            change24h = 0.0,    // Placeholder
                            displayOrder = 0,     // Placeholder
                            lastUpdated = System.currentTimeMillis() // Add current timestamp
                        )
                        viewModel.saveAsset(asset)
                        onSave()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
            ) {
                Text("Save Asset", color = Color.Black)
            }
        }
    }
}
