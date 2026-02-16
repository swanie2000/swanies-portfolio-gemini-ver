package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AssetPickerScreen(onAssetSelected: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 1. Seed List for testing
    val assets = listOf("XRP", "BTC", "ETH", "Gold", "Silver", "Platinum", "Palladium")

    // 2. Filter Logic
    val filteredAssets = if (searchQuery.isBlank()) {
        assets
    } else {
        assets.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Near-black background
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("SEARCH...", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search
            ),
            // 4. Keyboard Action to select the first result
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (filteredAssets.isNotEmpty()) {
                        val firstAsset = filteredAssets.first()
                        // Use a placeholder coinId for navigation
                        val coinId = firstAsset.lowercase(Locale.ROOT)
                        onAssetSelected("$coinId|$firstAsset|$firstAsset")
                        keyboardController?.hide()
                    }
                }
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.Cyan,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            items(filteredAssets) { asset ->
                // 3. Click Action
                TextButton(
                    onClick = {
                        val coinId = asset.lowercase(Locale.ROOT)
                        onAssetSelected("$coinId|$asset|$asset")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = asset,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
            }
        }
    }
}
