package com.swanie.portfolio.ui.holdings

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.local.AssetEntity
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun MyHoldingsScreen(
    onAddNewAsset: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: AssetViewModel = viewModel(
        factory = AssetViewModelFactory(db.assetDao())
    )
    val holdings by viewModel.holdings.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("ALL", "CRYPTO", "METAL")

    val filteredHoldings = when (tabs[selectedTab]) {
        "CRYPTO" -> holdings.filter { it.category == "crypto" }
        "METAL" -> holdings.filter { it.category == "metal" }
        else -> holdings
    }

    val totalPortfolioValue = filteredHoldings.sumOf { it.amountHeld * it.currentPrice }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    var countdown by remember { mutableStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(true) }

    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(countdown, isTimerRunning) {
        if (isTimerRunning && countdown > 0) {
            delay(1000)
            countdown--
        } else if (countdown == 0) {
            isTimerRunning = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f)) // Spacer to balance the add icon

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.swanie_foreground),
                        contentDescription = "Swan Logo",
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        text = currencyFormat.format(totalPortfolioValue),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = onAddNewAsset,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add New Asset",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }


            // Refresh Bar Section
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmoothProgressBar(
                        progress = (30 - countdown) / 30f,
                        modifier = Modifier.weight(1f),
                        trackColor = surfaceVariantColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = {
                            if (countdown == 0 && !isRefreshing) {
                                viewModel.refreshAllPrices()
                                countdown = 30 // Reset timer
                                isTimerRunning = true
                            } else {
                                val message =
                                    if (isRefreshing) "Sync in progress." else "Please wait for the timer."
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = countdown == 0 && !isRefreshing
                    ) {
                        val refreshColor = if (countdown == 0 && !isRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Prices",
                            tint = refreshColor
                        )
                    }
                }
                Text(
                    text = "LIVE MARKET",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                divider = { }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(filteredHoldings) { asset ->
                    HoldingItemCard(asset)
                }
            }
        }
    }
}

@Composable
fun SmoothProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 2.dp,
    trackColor: Color
) {
    Canvas(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
    ) {
        val activeColor = when {
            progress >= (21f / 30f) -> Color(0xFF00C853) // Green
            progress >= (11f / 30f) -> Color(0xFFFFD600) // Yellow
            else -> Color(0xFFD32F2F) // Red
        }

        // Draw the background track
        drawRect(
            color = trackColor,
            size = size
        )

        // Draw the active progress portion
        drawRect(
            color = activeColor,
            size = Size(width = size.width * progress, height = size.height)
        )
    }
}

@Composable
fun HoldingItemCard(asset: AssetEntity) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val numberFormat = NumberFormat.getNumberInstance()
    val changeColor = if (asset.change24h >= 0) Color(0xFF00C853) else Color.Red

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Identity
        AsyncImage(
            model = asset.imageUrl,
            contentDescription = "${asset.name} icon",
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.width(16.dp))
        if (asset.name.equals(asset.symbol, ignoreCase = true)) {
            Text(
                text = asset.symbol.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = asset.symbol.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Middle Column: Visuals
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for Sparkline
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    "Sparkline",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 8.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
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
                    text = "${String.format(Locale.US, "%.2f", asset.change24h)}%",
                    color = changeColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right Column: Financials
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = currencyFormat.format(asset.amountHeld * asset.currentPrice),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${numberFormat.format(asset.amountHeld)} ${asset.symbol.uppercase()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
}
