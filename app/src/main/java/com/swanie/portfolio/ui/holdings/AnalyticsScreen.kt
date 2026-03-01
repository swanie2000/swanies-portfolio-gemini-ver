package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.settings.ThemeViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(navController: NavHostController) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val safeBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    val totalValue = holdings.sumOf { it.currentPrice * it.amountHeld }
    val cryptoValue = holdings.filter { it.category == AssetCategory.CRYPTO }.sumOf { it.currentPrice * it.amountHeld }
    val metalValue = holdings.filter { it.category == AssetCategory.METAL }.sumOf { it.currentPrice * it.amountHeld }

    Scaffold(
        bottomBar = {
            // FIXED: Unified Nav Bar wrapper to match Holdings screen exactly
            Column(modifier = Modifier.background(safeBg)) {
                BottomNavigationBar(navController = navController)
                // This Spacer ensures the bar sits above the Android system nav
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        },
        containerColor = safeBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "PORTFOLIO BREAKDOWN",
                color = safeText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
            )

            // Visual Chart Area
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(320.dp)) {
                // Background Glow
                Canvas(modifier = Modifier.size(280.dp)) {
                    drawCircle(brush = Brush.radialGradient(listOf(safeText.copy(0.08f), Color.Transparent)))
                }

                PortfolioDonutChart(
                    cryptoValue = cryptoValue.toFloat(),
                    metalValue = metalValue.toFloat(),
                    baseColor = safeText
                )
            }

            // Data Cards
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                AnalyticsCard(
                    title = "CRYPTOCURRENCY",
                    value = cryptoValue,
                    percent = if(totalValue > 0) (cryptoValue / totalValue * 100).toInt() else 0,
                    color = safeText,
                    isMetal = false
                )
                Spacer(Modifier.height(16.dp))
                AnalyticsCard(
                    title = "PRECIOUS METALS",
                    value = metalValue,
                    percent = if(totalValue > 0) (metalValue / totalValue * 100).toInt() else 0,
                    color = safeText,
                    isMetal = true
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun PortfolioDonutChart(cryptoValue: Float, metalValue: Float, baseColor: Color) {
    val total = cryptoValue + metalValue
    val cryptoRatio = if (total > 0) cryptoValue / total else 0f
    val cryptoAngle = cryptoRatio * 360f

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            // Metal Track
            drawArc(
                color = baseColor.copy(alpha = 0.1f),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                style = Stroke(width = 36.dp.toPx(), cap = StrokeCap.Round)
            )
            // Crypto Segment
            if (total > 0) {
                drawArc(
                    color = baseColor,
                    startAngle = -90f, sweepAngle = cryptoAngle, useCenter = false,
                    style = Stroke(width = 36.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("CRYPTO", color = baseColor.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${(cryptoRatio * 100).toInt()}%", color = baseColor, fontSize = 40.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: Double, percent: Int, color: Color, isMetal: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(if(isMetal) 0.04f else 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(0.1f))
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if(isMetal) color.copy(0.3f) else color))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                Text("$percent% of your total wealth", color = color.copy(0.5f), fontSize = 11.sp)
            }
            Text(
                NumberFormat.getCurrencyInstance(Locale.US).format(value),
                color = color, fontSize = 18.sp, fontWeight = FontWeight.Black
            )
        }
    }
}