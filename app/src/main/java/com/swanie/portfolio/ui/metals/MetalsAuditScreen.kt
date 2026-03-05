package com.swanie.portfolio.ui.metals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.repository.MarketPriceData
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.SparklineChart
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * Market Watch Screen (Rebuilt for Precious Metals).
 * Synchronized with the Room database to ensure 1:1 price parity with Holdings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetalsAuditScreen(navController: NavController) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    // Observe Database Holdings for 1:1 Parity
    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    
    // Observe Theme Preferences
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()
    val cardBgHex by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextHex by themeViewModel.cardTextColor.collectAsState()

    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))
    val cardBg = Color(android.graphics.Color.parseColor(cardBgHex.ifBlank { "#121212" }))
    val cardText = Color(android.graphics.Color.parseColor(cardTextHex.ifBlank { "#FFFFFF" }))

    // Local state for "Ghost" assets (metals not currently owned)
    val ghostMarketData = remember { mutableStateMapOf<String, MarketPriceData>() }
    val metals = listOf("Gold" to "XAU", "Silver" to "XAG", "Platinum" to "XPT", "Palladium" to "XPD")

    // Fetch live market data only for metals NOT in holdings
    LaunchedEffect(holdings) {
        metals.forEach { (_, sym) ->
            val isOwned = holdings.any { it.baseSymbol == sym && it.category == AssetCategory.METAL }
            if (!isOwned) {
                scope.launch {
                    ghostMarketData[sym] = viewModel.fetchMarketPriceData(sym)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market Watch", fontWeight = FontWeight.Black, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
            items(metals) { (name, sym) ->
                // Priority 1: Use Database Asset (Holdings) for exact parity
                val ownedAsset = holdings.find { it.baseSymbol == sym && it.category == AssetCategory.METAL }
                
                // Priority 2: Use "Ghost" fetch if not owned
                val ghostData = ghostMarketData[sym]
                
                val currentPrice = ownedAsset?.currentPrice ?: ghostData?.current ?: 0.0
                val changePct = ownedAsset?.priceChange24h ?: ghostData?.changePercent ?: 0.0
                val sparkline = ownedAsset?.sparklineData ?: ghostData?.sparkline ?: emptyList()
                val high = ghostData?.high ?: 0.0
                val low = ghostData?.low ?: 0.0

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = CardDefaults.outlinedCardBorder(enabled = true).copy(brush = SolidColor(cardText.copy(alpha = 0.1f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(name.uppercase(), fontWeight = FontWeight.ExtraBold, color = cardText, fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(sym, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cardText.copy(alpha = 0.4f))
                                    if (ownedAsset != null) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = Color.Yellow.copy(alpha = 0.1f),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "OWNED", 
                                                fontSize = 8.sp, 
                                                fontWeight = FontWeight.Black, 
                                                color = Color.Yellow,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // SPARKLINE CHART
                            if (sparkline.isNotEmpty()) {
                                Box(Modifier.width(90.dp).height(35.dp).padding(horizontal = 10.dp)) {
                                    SparklineChart(
                                        sparklineData = sparkline,
                                        changeColor = if (changePct >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (currentPrice > 0.0) {
                                    Text(
                                        NumberFormat.getCurrencyInstance(Locale.US).format(currentPrice), 
                                        fontWeight = FontWeight.Black, color = cardText, fontSize = 18.sp
                                    )
                                    Text(
                                        text = "${if(changePct >= 0) "+" else ""}${String.format(Locale.US, "%.2f", changePct)}%",
                                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                                        color = if (changePct >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
                                    )
                                } else {
                                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Yellow)
                                }
                            }
                        }

                        // HIGH/LOW RANGES
                        if (high > 0.0) {
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = cardText.copy(alpha = 0.05f))
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("DAY LOW", fontSize = 9.sp, fontWeight = FontWeight.Black, color = cardText.copy(alpha = 0.3f))
                                    Text(NumberFormat.getCurrencyInstance(Locale.US).format(low), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("DAY HIGH", fontSize = 9.sp, fontWeight = FontWeight.Black, color = cardText.copy(alpha = 0.3f))
                                    Text(NumberFormat.getCurrencyInstance(Locale.US).format(high), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
