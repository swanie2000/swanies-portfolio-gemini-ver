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
import androidx.navigation.NavController
import com.swanie.portfolio.data.repository.MarketPriceData
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.SparklineChart
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * Market Watch Screen (Rebuilt for Precious Metals).
 * Fetches data for Gold, Silver, Platinum, and Palladium independently of database holdings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetalsAuditScreen(navController: NavController) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()

    // Observe Theme Preferences
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()
    val cardBgHex by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextHex by themeViewModel.cardTextColor.collectAsState()

    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))
    val cardBg = Color(android.graphics.Color.parseColor(cardBgHex.ifBlank { "#121212" }))
    val cardText = Color(android.graphics.Color.parseColor(cardTextHex.ifBlank { "#FFFFFF" }))

    // Local state for fetched market data
    val marketData = remember { mutableStateMapOf<String, MarketPriceData>() }
    val metals = listOf("Gold" to "XAU", "Silver" to "XAG", "Platinum" to "XPT", "Palladium" to "XPD")

    // Fetch live market data on launch
    LaunchedEffect(Unit) {
        metals.forEach { (_, sym) ->
            launch { 
                marketData[sym] = viewModel.fetchMarketPriceData(sym) 
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
                val data = marketData[sym]
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = CardDefaults.outlinedCardBorder(enabled = true).copy(brush = SolidColor(cardText.copy(alpha = 0.1f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(name.uppercase(), fontWeight = FontWeight.ExtraBold, color = cardText, fontSize = 16.sp)
                                Text(sym, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cardText.copy(alpha = 0.4f))
                            }

                            // SPARKLINE CHART (Independent of database)
                            if (data != null && data.sparkline.isNotEmpty()) {
                                Box(Modifier.width(90.dp).height(35.dp).padding(horizontal = 10.dp)) {
                                    SparklineChart(
                                        sparklineData = data.sparkline,
                                        changeColor = if (data.changePercent >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (data != null && data.current > 0.0) {
                                    Text(
                                        NumberFormat.getCurrencyInstance(Locale.US).format(data.current), 
                                        fontWeight = FontWeight.Black, color = cardText, fontSize = 18.sp
                                    )
                                    Text(
                                        text = "${if(data.changePercent >= 0) "+" else ""}${String.format("%.2f", data.changePercent)}%",
                                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                                        color = if (data.changePercent >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)
                                    )
                                } else {
                                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Yellow)
                                }
                            }
                        }

                        // HIGH/LOW RANGES
                        if (data != null && data.high > 0.0) {
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = cardText.copy(alpha = 0.05f))
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("DAY LOW", fontSize = 9.sp, fontWeight = FontWeight.Black, color = cardText.copy(alpha = 0.3f))
                                    Text(NumberFormat.getCurrencyInstance(Locale.US).format(data.low), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("DAY HIGH", fontSize = 9.sp, fontWeight = FontWeight.Black, color = cardText.copy(alpha = 0.3f))
                                    Text(NumberFormat.getCurrencyInstance(Locale.US).format(data.high), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
