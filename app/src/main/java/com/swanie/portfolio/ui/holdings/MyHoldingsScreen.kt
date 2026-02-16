package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swanie.portfolio.data.local.AppDatabase
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MyHoldingsScreen(
    onAddNewAsset: () -> Unit,
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: MyHoldingsViewModel = viewModel(
        factory = MyHoldingsViewModelFactory(db.assetDao())
    )
    val holdings by viewModel.holdings.collectAsState()

    val totalPortfolioValue = holdings.sumOf { it.amountHeld * it.currentPrice }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

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
                Text("Add New", color = Color.Black, modifier = Modifier.padding(start = 4.dp))
            }
        }

        // Total Portfolio Value Display
        Text(
            text = currencyFormat.format(totalPortfolioValue),
            color = Color.Cyan,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Total Portfolio Value",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
        )

        // Total Assets Counter
        Text(
            text = "Total Assets: ${holdings.size}",
            color = Color.Cyan.copy(alpha = 0.8f), // Cyan, slightly transparent
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.DarkGray)

        // Display the List of Saved Assets
        LazyColumn {
            items(holdings) { asset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${NumberFormat.getInstance().format(asset.amountHeld)} ${asset.symbol}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Value: ${currencyFormat.format(asset.amountHeld * asset.currentPrice)}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
