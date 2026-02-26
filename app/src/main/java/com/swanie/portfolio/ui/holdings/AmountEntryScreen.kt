package com.swanie.portfolio.ui.holdings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // FIXED: Added missing import
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.settings.ThemeViewModel

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
    // Inject Theme State
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    // Safely parse hex colors
    val bgColor = remember(siteBgHex) { Color(android.graphics.Color.parseColor(siteBgHex)) }
    val textColor = remember(siteTextHex) { Color(android.graphics.Color.parseColor(siteTextHex)) }

    val viewModel: AmountEntryViewModel = hiltViewModel()

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

        val asset = AssetEntity(
            coinId = coinId,
            symbol = symbol,
            name = name,
            amountHeld = amountValue,
            currentPrice = currentPrice,
            category = category,
            imageUrl = imageUrl,
            lastUpdated = System.currentTimeMillis(),
            change24h = 0.0,
            displayOrder = 0,
            priceChange24h = 0.0,
            marketCapRank = 0,
            sparklineData = emptyList()
        )

        viewModel.saveAsset(asset)
        onSave()
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = bgColor,
            title = { Text("Discard Asset?", color = textColor) },
            text = { Text("Are you sure you want to discard this new asset?", color = textColor.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text("Yes", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("No", color = textColor)
                }
            }
        )
    }

    BackHandler {
        showExitDialog = true
    }

    Scaffold(containerColor = bgColor) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { showExitDialog = true }, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
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
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = textColor.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol.take(1),
                        style = MaterialTheme.typography.displayMedium,
                        color = textColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.displaySmall,
                color = textColor
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
                label = { Text("Enter Amount for $symbol", color = textColor.copy(alpha = 0.6f)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { executeSave() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor,
                    focusedBorderColor = textColor,
                    unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                    focusedLabelColor = textColor.copy(alpha = 0.7f),
                    unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { executeSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Yellow,
                    contentColor = Color.Black
                ),
                enabled = amountText.isNotBlank()
            ) {
                Text("Save Asset", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}