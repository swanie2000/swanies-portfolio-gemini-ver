package com.swanie.portfolio.ui.holdings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.AsyncImage
import com.swanie.portfolio.data.local.AppDatabase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyHoldingsScreen(
    onAddNewAsset: () -> Unit,
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: AssetViewModel = viewModel(
        factory = AssetViewModelFactory(db.assetDao())
    )
    val holdings by viewModel.holdings.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isRefreshEnabled by viewModel.isRefreshEnabled.collectAsState()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()

    val totalPortfolioValue = holdings.sumOf { it.amountHeld * it.currentPrice }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Total Portfolio Value",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = currencyFormat.format(totalPortfolioValue),
                    color = Color.Cyan,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (isRefreshEnabled) {
                        viewModel.refreshAllPrices()
                    } else {
                        Toast.makeText(context, "Please wait before refreshing again.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = isRefreshEnabled && !isRefreshing
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Prices",
                    tint = if (isRefreshEnabled && !isRefreshing) Color.Cyan else Color.DarkGray
                )
            }
            Column {
                Text(
                    text = if (isRefreshing) "Refreshing..." else if (!isRefreshEnabled) "Ready in 60s" else "Ready to refresh",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                )
                lastSyncTimestamp?.let {
                    val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                    Text(
                        text = "Last Sync: ${sdf.format(Date(it))}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.DarkGray)

        LazyColumn {
            items(holdings) { asset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = asset.imageUrl,
                        contentDescription = "${asset.name} icon",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = asset.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${NumberFormat.getInstance().format(asset.amountHeld)} ${asset.symbol.uppercase()})",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currencyFormat.format(asset.amountHeld * asset.currentPrice),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (asset.currentPrice == 0.0) {
                            Text(
                                text = "---",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "@ ${currencyFormat.format(asset.currentPrice)}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}