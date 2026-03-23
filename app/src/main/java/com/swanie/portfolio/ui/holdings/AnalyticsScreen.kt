package com.swanie.portfolio.ui.holdings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.settings.ThemeViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalyticsScreen(navController: NavController) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = emptyList())
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    var isExiting by remember { mutableStateOf(false) }

    val neonPalette = listOf(
        Color(0xFF00E5FF), Color(0xFFFFD600), Color(0xFFFF4081),
        Color(0xFF00C853), Color(0xFF6200EA), Color(0xFFFF6D00),
        Color(0xFF2979FF), Color(0xFFEEFF41), Color(0xFFB2FF59)
    )

    val totalValue = holdings.sumOf { it.officialSpotPrice * (it.weight * it.amountHeld) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    var selectedAssetId by remember { mutableStateOf<String?>(null) }

    val assetSegments = holdings.mapIndexed { index, asset ->
        val assetValue = asset.officialSpotPrice * (asset.weight * asset.amountHeld)
        AssetSegment(
            asset = asset,
            value = assetValue,
            ratio = if (totalValue > 0) (assetValue / totalValue).toFloat() else 0f,
            color = neonPalette[index % neonPalette.size]
        )
    }.sortedByDescending { it.value }

    val focusedSegment = assetSegments.find { it.asset.coinId == selectedAssetId }
    val pagerState = rememberPagerState(pageCount = { 3 })

    // GRADIENT SYMMETRY: Set background to Transparent to allow NavGraph gradient to show
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController, onNavigate = { isExiting = true }) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (!isExiting) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.swanie_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Text(
                    text = "PORTFOLIO INTELLIGENCE",
                    color = safeText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(440.dp),
                    color = Color(0xFF060606),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.12f))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            Modifier.height(40.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            repeat(3) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(0.2f)
                                Box(modifier = Modifier.padding(4.dp).clip(CircleShape).background(color).size(6.dp))
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    when (page) {
                                        0 -> PopOutPieChart(assetSegments, selectedAssetId) { id -> selectedAssetId = if(selectedAssetId == id) null else id }
                                        1 -> InteractiveDonutChart(assetSegments, selectedAssetId, safeText) { id -> selectedAssetId = if(selectedAssetId == id) null else id }
                                        2 -> InteractiveBarChart(assetSegments, selectedAssetId) { id -> selectedAssetId = if(selectedAssetId == id) null else id }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(0.04f))
                                .height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(targetState = focusedSegment, label = "footerFade") { segment ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (segment != null) {
                                        Text(
                                            text = segment.asset.name.replace("\n", " ").uppercase(),
                                            color = segment.color,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = currencyFormatter.format(segment.value),
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "${String.format("%.1f", segment.ratio * 100)}% OF TOTAL",
                                            color = segment.color.copy(alpha = 0.8f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        Text(text = "TOTAL PORTFOLIO VALUE", color = Color.White.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        Text(text = currencyFormatter.format(totalValue), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                                        Text(text = " ", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "HOLDINGS KEY",
                    color = safeText.copy(0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)
                )

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    assetSegments.forEach { segment ->
                        val isSelected = selectedAssetId == segment.asset.coinId
                        AssetLegendRow(segment, safeText, isSelected) { selectedAssetId = if(isSelected) null else segment.asset.coinId }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = safeText.copy(0.05f))
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// --- SUB COMPONENTS ---

@Composable
fun PopOutPieChart(segments: List<AssetSegment>, selectedId: String?, onSelect: (String) -> Unit) {
    val animateSweep = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animateSweep.animateTo(1f, tween(1000, easing = FastOutSlowInEasing)) }
    Canvas(modifier = Modifier.size(200.dp).pointerInput(segments) {
        detectTapGestures { offset ->
            val center = Offset(size.width / 2f, size.height / 2f)
            val angle = (atan2(offset.y - center.y, offset.x - center.x) * (180 / PI).toFloat() + 450) % 360
            var currentAngle = 0f
            segments.forEach { segment ->
                val sweep = segment.ratio * 360f
                if (angle >= currentAngle && angle <= currentAngle + sweep) { onSelect(segment.asset.coinId) }
                currentAngle += sweep
            }
        }
    }) {
        var currentStartAngle = -90f
        segments.forEach { segment ->
            val sweepAngle = (segment.ratio * 360f) * animateSweep.value
            val isSelected = selectedId == segment.asset.coinId
            val offsetAmt = if (isSelected) 12.dp.toPx() else 0f
            val angleInRad = (currentStartAngle + sweepAngle / 2f) * (PI / 180f).toFloat()
            val offsetX = cos(angleInRad) * offsetAmt
            val offsetY = sin(angleInRad) * offsetAmt
            drawArc(color = if (selectedId == null || isSelected) segment.color else segment.color.copy(0.15f), startAngle = currentStartAngle, sweepAngle = sweepAngle, useCenter = true, topLeft = Offset(offsetX, offsetY), size = Size(size.width, size.height))
            currentStartAngle += (segment.ratio * 360f)
        }
    }
}

@Composable
fun InteractiveDonutChart(segments: List<AssetSegment>, selectedId: String?, baseColor: Color, onSelect: (String) -> Unit) {
    val focusedSegment = segments.find { it.asset.coinId == selectedId }
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(190.dp).pointerInput(segments) {
            detectTapGestures { offset ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val angle = (atan2(offset.y - center.y, offset.x - center.x) * (180 / PI).toFloat() + 450) % 360
                var currentAngle = 0f
                segments.forEach { segment ->
                    val sweep = segment.ratio * 360f
                    if (angle >= currentAngle && angle <= currentAngle + sweep) { onSelect(segment.asset.coinId) }
                    currentAngle += sweep
                }
            }
        }) {
            val strokeWidth = 32.dp.toPx()
            var currentStartAngle = -90f
            segments.forEach { segment ->
                val sweepAngle = segment.ratio * 360f
                val isSelected = selectedId == segment.asset.coinId
                drawArc(color = if (selectedId == null || isSelected) segment.color else segment.color.copy(0.15f), startAngle = currentStartAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = if(isSelected) 42.dp.toPx() else strokeWidth, cap = StrokeCap.Round))
                currentStartAngle += sweepAngle
            }
        }
        Text(text = focusedSegment?.asset?.symbol?.uppercase() ?: "TOTAL", color = Color.White.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun InteractiveBarChart(segments: List<AssetSegment>, selectedId: String?, onSelect: (String) -> Unit) {
    val displaySegments = segments.take(6)
    val maxRatio = displaySegments.maxByOrNull { it.ratio }?.ratio ?: 1f
    val animateHeight = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animateHeight.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }

    Row(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        displaySegments.forEach { segment ->
            val isSelected = selectedId == segment.asset.coinId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(segment.asset.coinId) }
            ) {
                Box(modifier = Modifier.fillMaxHeight(0.8f).width(22.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(0.05f)), contentAlignment = Alignment.BottomCenter) {
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight((segment.ratio / maxRatio) * animateHeight.value).background(brush = Brush.verticalGradient(colors = if (selectedId == null || isSelected) listOf(segment.color, segment.color.copy(alpha = 0.5f)) else listOf(segment.color.copy(0.15f), segment.color.copy(alpha = 0.05f)))))
                }
                Spacer(Modifier.height(8.dp))
                Text(text = segment.asset.symbol.uppercase(), color = if (selectedId == null || isSelected) Color.White else Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun AssetLegendRow(segment: AssetSegment, textColor: Color, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(if (isSelected) segment.color.copy(0.12f) else Color.Transparent, label = "")
    val borderColor by animateColorAsState(if (isSelected) segment.color.copy(0.5f) else Color.Transparent, label = "")

    val symbol = segment.asset.symbol.uppercase()
    val cleanName = segment.asset.name.replace("\n", " ").trim().uppercase()
    val displayTitle = if (symbol == cleanName) symbol else "$symbol • $cleanName"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(0.6f), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                if (segment.asset.category == AssetCategory.METAL) {
                    MetalIcon(name = segment.asset.name, weight = segment.asset.weight, size = 30, category = segment.asset.category)
                } else {
                    AsyncImage(
                        model = segment.asset.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp).clip(CircleShape)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = displayTitle,
                    color = if(isSelected) segment.color else textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segment.color)
                )
            }
        }

        Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
            Text(NumberFormat.getCurrencyInstance(Locale.US).format(segment.value), color = textColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text("${String.format("%.1f", segment.ratio * 100)}%", color = segment.color, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

data class AssetSegment(val asset: AssetEntity, val value: Double, val ratio: Float, val color: Color)