package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.local.AssetEntity
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AmountEntryScreen(
    coinId: String,
    symbol: String,
    name: String,
    imageUrl: String,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: AssetViewModel = viewModel(
        factory = AssetViewModelFactory(db.assetDao())
    )

    var amount by remember { mutableStateOf("") }
    var price by remember { mutableDoubleStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(coinId) {
        price = viewModel.getSingleCoinPrice(coinId)
        isLoading = false
        focusRequester.requestFocus()
    }

    fun onSaveHolding() {
        val amountHeld = amount.toDoubleOrNull() ?: 0.0
        if (amountHeld > 0) {
            val asset = AssetEntity(
                coinId = coinId,
                symbol = symbol,
                name = name,
                amountHeld = amountHeld,
                currentPrice = price, // Use the fetched price
                change24h = 0.0,
                displayOrder = 0,
                lastUpdated = System.currentTimeMillis(),
                imageUrl = imageUrl
            )
            viewModel.saveNewAsset(asset, onSaveComplete = onSave)
        }
    }

    Scaffold(containerColor = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            Spacer(Modifier.height(32.dp))

            AsyncImage(
                model = imageUrl,
                contentDescription = "$name Icon",
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            } else {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.US).format(price),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Cyan
                )
            }
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("Enter Amount for $symbol", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSaveHolding() }
                ),
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

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onSaveHolding() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
            ) {
                Text("Save Asset", color = Color.Black)
            }
        }
    }
}
