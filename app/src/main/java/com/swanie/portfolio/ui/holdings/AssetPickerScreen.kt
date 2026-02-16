package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swanie.portfolio.ui.components.AlphaKeyboard

@Composable
fun AssetPickerScreen(onAssetSelected: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    // Placeholder list - will be replaced by API logic later
    val allAssets = listOf("XRP", "BTC", "ETH", "DOGE", "SOL", "ADA")
    val filteredAssets = allAssets.filter { it.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        // Search Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            color = Color.Black
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
                Text(
                    text = if (searchQuery.isEmpty()) "SEARCH..." else searchQuery,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.Cyan
                )
            }
        }

        // Results List
        LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
            items(filteredAssets) { asset ->
                TextButton(
                    onClick = {
                        // Passing a placeholder coinId, the symbol, and the name
                        onAssetSelected("ripple|${asset}|${asset}")
                     },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = asset,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                HorizontalDivider(color = Color.DarkGray)
            }
        }

        // Custom Keyboard
        AlphaKeyboard(
            onKeyClick = { searchQuery += it },
            onBackSpace = { if (searchQuery.isNotEmpty()) searchQuery = searchQuery.dropLast(1) },
            onClear = { searchQuery = "" }
        )
    }
}