package com.swanie.portfolio.ui.holdings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush

@Composable
fun AmountEntryScreen(
    coinId: String,
    symbol: String,
    name: String,
    imageUrl: String,
    category: AssetCategory,
    currentPrice: Double,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: AssetViewModel = viewModel(
        factory = AssetViewModelFactory(db.assetDao())
    )

    var amountText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var showExitDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun executeSave() {
        val amountValue = amountText.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            errorMessage = "Please enter a valid amount."
            return
        }

        // FIXED: Added all missing parameters required by the AssetEntity constructor
        val asset = AssetEntity(
            coinId = coinId,
            symbol = symbol,
            name = name,
            amountHeld = amountValue,
            currentPrice = currentPrice,
            category = category,
            imageUrl = imageUrl,
            lastUpdated = System.currentTimeMillis(),
            change24h = 0.0,      // Provided default to fix build error
            displayOrder = 0,     // Provided default to fix build error
            priceChange24h = 0.0,
            marketCapRank = 0,
            sparklineData = emptyList()
        )

        viewModel.saveNewAsset(
            asset = asset,
            amount = amountValue,
            onSaveComplete = onSave
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Discard Asset?") },
            text = { Text("Are you sure you want to discard this new asset?") },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    BackHandler {
        showExitDialog = true
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = LocalBackgroundBrush.current)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { showExitDialog = true }, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "$name Icon",
                    modifier = Modifier.size(120.dp)
                )
            } else {
                // Placeholder for assets (like metals) without URLs
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol.take(1),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("Enter Amount for $symbol") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { executeSave() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { executeSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                enabled = amountText.isNotBlank()
            ) {
                Text("Save Asset")
            }
        }
    }
}