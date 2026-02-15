package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swanie.portfolio.ui.components.AlphaKeyboard

@Composable
fun AssetPickerScreen() {
    var searchQuery by remember { mutableStateOf("") }

    // Using a dark background to match your keyboard's neon/dark aesthetic
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {

        // Display what we are typing
        Surface(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            color = Color.Black.copy(alpha = 0.8f)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text(
                    text = if (searchQuery.isEmpty()) "SEARCH ASSETS..." else searchQuery,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Cyan
                )
            }
        }

        // Placeholder for results list
        Box(modifier = Modifier.weight(1f)) {
            // We will add the API search results here soon
        }

        // Our Custom Keyboard at the bottom
        AlphaKeyboard(
            onKeyClick = { searchQuery += it },
            onBackSpace = { if (searchQuery.isNotEmpty()) searchQuery = searchQuery.dropLast(1) },
            onClear = { searchQuery = "" }
        )
    }
}