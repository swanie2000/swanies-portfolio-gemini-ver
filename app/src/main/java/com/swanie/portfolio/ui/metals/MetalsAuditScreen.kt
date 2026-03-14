package com.swanie.portfolio.ui.metals

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.repository.MarketPriceData
import com.swanie.portfolio.ui.components.MetalMarketCard
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun MetalsAuditScreen(navController: NavController) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())

    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()
    val cardBgHex by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextHex by themeViewModel.cardTextColor.collectAsState()

    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))
    val cardBg = Color(android.graphics.Color.parseColor(cardBgHex.ifBlank { "#121212" }))
    val cardText = Color(android.graphics.Color.parseColor(cardTextHex.ifBlank { "#FFFFFF" }))

    var metalsOrder by remember { mutableStateOf(listOf("Gold" to "XAU", "Silver" to "XAG", "Platinum" to "XPT", "Palladium" to "XPD")) }
    val marketDataMap = remember { mutableStateMapOf<String, MarketPriceData>() }

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    // ONE-TIME-FETCH: Now decoupled and calling refreshMarketWatch()
    LaunchedEffect(Unit) {
        // 1. Restore saved order
        val savedOrder = viewModel.getMetalDisplayOrder()
        if (savedOrder != null) {
            val defaultList = listOf("Gold" to "XAU", "Silver" to "XAG", "Platinum" to "XPT", "Palladium" to "XPD")
            metalsOrder = savedOrder.mapNotNull { sym -> defaultList.find { it.second == sym } }
        }

        // 2. Perform decoupled refresh for the "Big 4"
        viewModel.refreshMarketWatch()

        // 3. Populate UI map from the result of individual fetches
        metalsOrder.forEach { (_, sym) ->
            launch {
                val data = viewModel.fetchMarketPriceData(sym)
                if (data.current > 0.0) {
                    marketDataMap[sym] = data
                }
            }
        }

        // 4. Scroll to top
        delay(100)
        lazyListState.scrollToItem(0)
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        metalsOrder = metalsOrder.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 4.dp)) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                }
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(70.dp), contentScale = ContentScale.Fit)
                    Text(text = "METALS MARKET WATCH", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.offset(y = (-6).dp))
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(metalsOrder, key = { it.second }) { item ->
                    val (name, sym) = item
                    ReorderableItem(reorderableLazyListState, key = sym) { isDragging ->
                        val isOwned = holdings.any { it.baseSymbol == sym && it.category == AssetCategory.METAL }
                        val marketData = marketDataMap[sym]

                        val scale by animateFloatAsState(if (isDragging) 1.1f else 1f, label = "scale")
                        val elevation by animateDpAsState(if (isDragging) 15.dp else 2.dp, label = "elevation")

                        val dragModifier = Modifier.longPressDraggableHandle(
                            onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                            onDragStopped = { viewModel.saveMetalDisplayOrder(metalsOrder.map { it.second }) }
                        )

                        MetalMarketCard(
                            name = name,
                            symbol = sym,
                            currentPrice = marketData?.current ?: 0.0,
                            changePercent = marketData?.changePercent ?: 0.0,
                            dayHigh = marketData?.dayHigh ?: 0.0,
                            dayLow = marketData?.dayLow ?: 0.0,
                            sparkline = marketData?.sparkline ?: emptyList(),
                            isOwned = isOwned,
                            cardBg = cardBg,
                            cardText = cardText,
                            modifier = Modifier
                                .graphicsLayer { 
                                    scaleX = scale
                                    scaleY = scale
                                    shadowElevation = elevation.toPx() 
                                }
                                .zIndex(if (isDragging) 1f else 0f)
                                .then(dragModifier)
                        )
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth().height(64.dp), color = bgColor) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val icons = listOf(Icons.Default.Home to Routes.HOME, Icons.Default.FormatListBulleted to Routes.HOLDINGS, Icons.Default.PieChart to Routes.ANALYTICS, Icons.Default.Settings to Routes.SETTINGS)
                    icons.forEach { (icon, route) ->
                        IconButton(onClick = { navController.navigate(route) }, modifier = Modifier.size(48.dp)) {
                            Icon(icon, null, modifier = Modifier.size(28.dp), tint = if(currentRoute == route) textColor else textColor.copy(0.3f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars).background(bgColor))
        }
    }
}
