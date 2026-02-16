package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.local.AssetEntity

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Enter Amount") }
            )
            Button(
                onClick = {
                    val asset = AssetEntity(
                        coinId = coinId,
                        symbol = symbol,
                        name = name,
                        amountHeld = amount.toDouble(),
                        currentPrice = 0.0, // Will be updated later
                        change24h = 0.0,    // Will be updated later
                        displayOrder = 0      // Will be updated later
                    )
                    viewModel.saveAsset(asset)
                    onSave()
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Save")
            }
        }
    }
}
