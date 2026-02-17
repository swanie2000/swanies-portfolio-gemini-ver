package com.swanie.portfolio.ui.holdings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.swanie.portfolio.data.local.AssetEntity
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

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")

    val filteredHoldings = when (tabs[selectedTab]) {
        "CRYPTO" -> holdings.filter { it.category == "crypto" }
        "METAL" -> holdings.filter { it.category == "metal" }
        else -> holdings
    }

    val totalPortfolioValue = filteredHoldings.sumOf { it.amountHeld * it.currentPrice }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                .padding(horizontal = 16.dp),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Black,
            contentColor = Color.Cyan
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(filteredHoldings) { asset ->
                AssetRow(asset)
            }
        }
    }
}

@Composable
fun AssetRow(asset: AssetEntity) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val numberFormat = NumberFormat.getNumberInstance()
    val changeColor = if (asset.change24h >= 0) Color(0xFF00C853) else Color.Red

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Section: Icon and Name/Symbol
        AsyncImage(
            model = asset.imageUrl,
            contentDescription = "${asset.name} icon",
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1.5f)
        ) {
            Text(
                text = asset.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = asset.symbol.uppercase(),
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        // Middle Section: Sparkline and 24h Change
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for Sparkline
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(80.dp)
                    .background(Color.DarkGray.copy(alpha = 0.5f))
            ) {
                Text("Sparkline", color = Color.Gray, fontSize = 8.sp, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(Modifier.height(4.dp))
            Row {
                Icon(
                    imageVector = if (asset.change24h >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "24h change",
                    tint = changeColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${String.format("%.2f", asset.change24h)}%",
                    color = changeColor,
                    fontSize = 12.sp
                )
            }
        }

        // End Section: Amount Held and Value
        Column(
            modifier = Modifier.weight(1.5f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = currencyFormat.format(asset.amountHeld * asset.currentPrice),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "${numberFormat.format(asset.amountHeld)} ${asset.symbol.uppercase()}",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
}
