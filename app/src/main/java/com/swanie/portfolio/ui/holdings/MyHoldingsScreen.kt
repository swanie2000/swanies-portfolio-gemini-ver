package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MyHoldingsScreen(
    onAddNewAsset: () -> Unit,
) {
    // Placeholder data - will be replaced by ViewModel logic later
    val holdings = listOf("XRP", "BTC", "ETH")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Portfolio",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddNewAsset,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Asset", tint = Color.Black)
                Text("Add New Asset", color = Color.Black, modifier = Modifier.padding(start = 4.dp))
            }
        }

        // Placeholder List
        LazyColumn(modifier = Modifier.padding(top = 24.dp)) {
            items(holdings) { holding ->
                Text(text = holding, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
