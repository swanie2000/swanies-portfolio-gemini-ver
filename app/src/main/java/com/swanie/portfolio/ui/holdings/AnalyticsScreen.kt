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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
// 🛡️ NO EXTRA IMPORTS ADDED HERE
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import com.swanie.portfolio.ui.theme.ProLockBadge
import com.swanie.portfolio.ui.theme.ProPalette
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

    val holdings by viewModel.holdings.collectAsStateWithLifecycle(initialValue = null)
    val safeHoldings = holdings ?: emptyList()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    var isExiting by remember { mutableStateOf(false) }

    val neonPalette = listOf(
        Color(0xFF00E5FF), Color(0xFFFFD600), Color(0xFFFF4081),
        Color(0xFF00C853), Color(0xFF6200EA), Color(0xFFFF6D00),
        Color(0xFF2979FF), Color(0xFFEEFF41), Color(0xFFB2FF59)
    )

    val totalValue = safeHoldings.sumOf { it.officialSpotPrice * (it.weight * it.amountHeld) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    var selectedAssetId by remember { mutableStateOf<String?>(null) }

    val assetSegments = safeHoldings.mapIndexed { index, asset ->
        val assetValue = asset.officialSpotPrice * (asset.weight * asset.amountHeld)
        AssetSegment(
            asset = asset,
            value = assetValue,
            ratio = if (totalValue > 0) (assetValue / totalValue).toFloat() else 0f,
            color = neonPalette[index % neonPalette.size]
        )
    }.sortedByDescending { it.value }

    val focusedSegment = assetSegments.find { it.asset.coinId == selectedAssetId }
    var modeTabIndex by remember { mutableIntStateOf(0) }
    val pageTitles = listOf(
        "START",
        "PIE CHART",
        "DONUT CHART",
        "BAR CHART",
        "RISK EXPOSURE MAP",
        "ATTRIBUTION",
        "REBALANCE COACH"
    )
    val contentPagerState = rememberPagerState(pageCount = { pageTitles.size })

    LaunchedEffect(contentPagerState.currentPage) {
        if (modeTabIndex != contentPagerState.currentPage) {
            modeTabIndex = contentPagerState.currentPage
        }
    }

    // 🛡️ SURGERY: Replacing Scaffold/BottomBar with a simple Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (!isExiting) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pageTitles[modeTabIndex],
                        color = safeText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalPager(
                    state = contentPagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (page == 0) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.swanie_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(96.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "PORTFOLIO INTELLIGENCE",
                                    color = safeText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                text = "Swipe left or right to move between analytics pages.\n\nPIE, DONUT, and BAR are live chart views.\nTap any asset in the list to highlight it in the active chart.\n\nRISK, ATTRIBUTION, and REBALANCE are premium insight previews.",
                                    color = safeText.copy(alpha = 0.85f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        } else if (page in 1..3) {
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                color = Color(0xFF060606),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Color.White.copy(0.12f))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (page) {
                                            1 -> PopOutPieChart(assetSegments, selectedAssetId) { id -> selectedAssetId = if (selectedAssetId == id) null else id }
                                            2 -> InteractiveDonutChart(assetSegments, selectedAssetId, safeText) { id -> selectedAssetId = if (selectedAssetId == id) null else id }
                                            else -> InteractiveBarChart(assetSegments, selectedAssetId) { id -> selectedAssetId = if (selectedAssetId == id) null else id }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(0.04f))
                                            .height(84.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Crossfade(targetState = focusedSegment, label = "chartTabFooter") { segment ->
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                if (segment != null) {
                                                    Text(
                                                        text = segment.asset.name.replace("\n", " ").uppercase(),
                                                        color = segment.color,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = currencyFormatter.format(segment.value),
                                                        color = Color.White,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Black,
                                                        maxLines = 1
                                                    )
                                                } else {
                                                    Text(
                                                        text = "TOTAL PORTFOLIO VALUE",
                                                        color = Color.White.copy(0.45f),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = currencyFormatter.format(totalValue),
                                                        color = Color.White,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Black,
                                                        maxLines = 1
                                                    )
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
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 6.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                assetSegments.forEach { segment ->
                                    val isSelected = selectedAssetId == segment.asset.coinId
                                    AssetLegendRow(segment, safeText, isSelected) { selectedAssetId = if(isSelected) null else segment.asset.coinId }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 1.dp), color = safeText.copy(0.05f))
                                }
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                        } else {
                            val preview = when (page) {
                                4 -> PremiumInsightPreview(
                                    title = "RISK EXPOSURE MAP",
                                    description = "See concentration warnings, category heat, and volatility pressure.",
                                    highlights = listOf(
                                        "Concentration risk score by asset and category.",
                                        "Volatility buckets to spot unstable exposure.",
                                        "Early warning when one position dominates."
                                    )
                                )
                                5 -> PremiumInsightPreview(
                                    title = "PERFORMANCE ATTRIBUTION",
                                    description = "Break down portfolio gains by asset, category, and position size.",
                                    highlights = listOf(
                                        "Identify top gain drivers across the portfolio.",
                                        "Compare realized gains vs market movement.",
                                        "See drag positions that reduce momentum."
                                    )
                                )
                                else -> PremiumInsightPreview(
                                    title = "REBALANCE COACH",
                                    description = "Get target allocation drift alerts and one-tap rebalance suggestions.",
                                    highlights = listOf(
                                        "Set allocation targets and drift thresholds.",
                                        "Get alerts when your portfolio slips off target.",
                                        "Receive a guided rebalance action plan."
                                    )
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            ) {
                                LargeProAdPanel(
                                    preview = preview,
                                    textColor = safeText,
                                    onUpgradeClick = { navController.navigate(Routes.UPGRADE_TO_PRO) }
                                )
                            }
                            Spacer(Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- SUB COMPONENTS ---

@Composable
fun PopOutPieChart(segments: List<AssetSegment>, selectedId: String?, onSelect: (String) -> Unit) {
    val animateSweep = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animateSweep.animateTo(1f, tween(1000, easing = FastOutSlowInEasing)) }
    Canvas(modifier = Modifier.size(168.dp).pointerInput(segments) {
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
        Canvas(modifier = Modifier.size(158.dp).pointerInput(segments) {
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
            val strokeWidth = 24.dp.toPx()
            var currentStartAngle = -90f
            segments.forEach { segment ->
                val sweepAngle = segment.ratio * 360f
                val isSelected = selectedId == segment.asset.coinId
                drawArc(color = if (selectedId == null || isSelected) segment.color else segment.color.copy(0.15f), startAngle = currentStartAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = if(isSelected) 32.dp.toPx() else strokeWidth, cap = StrokeCap.Round))
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

    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        displaySegments.forEach { segment ->
            val isSelected = selectedId == segment.asset.coinId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(segment.asset.coinId) }
            ) {
                Box(modifier = Modifier.fillMaxHeight(0.78f).width(18.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(0.05f)), contentAlignment = Alignment.BottomCenter) {
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
    val density = LocalDensity.current

    val symbol = segment.asset.symbol.uppercase()
    val cleanName = segment.asset.name.replace("\n", " ").trim().uppercase()
    val displayTitle = if (symbol == cleanName) symbol else "$symbol - $cleanName"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(0.52f), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                if (segment.asset.category == AssetCategory.METAL) {
                    // This relies on MetalIcon being available in the package scope
                    MetalIcon(name = segment.asset.name, weight = segment.asset.weight, size = 26, category = segment.asset.category)
                } else {
                    AsyncImage(
                        model = segment.asset.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp).clip(CircleShape)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = displayTitle,
                    color = if(isSelected) segment.color else textColor,
                    fontSize = with(density) { (12.sp.toPx() / fontScale.coerceAtMost(1.1f)).toSp() },
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segment.color)
                )
            }
        }

        Column(modifier = Modifier.weight(0.48f), horizontalAlignment = Alignment.End) {
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.US).format(segment.value),
                color = textColor,
                fontSize = with(density) { (14.sp.toPx() / fontScale.coerceAtMost(1.1f)).toSp() },
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${String.format("%.1f", segment.ratio * 100)}%",
                color = segment.color,
                fontSize = with(density) { (10.sp.toPx() / fontScale.coerceAtMost(1.1f)).toSp() },
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

data class AssetSegment(val asset: AssetEntity, val value: Double, val ratio: Float, val color: Color)

private data class PremiumInsightPreview(
    val title: String,
    val description: String,
    val highlights: List<String>
)

@Composable
private fun PremiumInsightsSection(
    textColor: Color,
    onUpgradeClick: () -> Unit
) {
    var selectedPreview by remember { mutableStateOf<PremiumInsightPreview?>(null) }
    val previews = remember {
        listOf(
            PremiumInsightPreview(
                title = "RISK EXPOSURE MAP",
                description = "See concentration warnings, category heat, and volatility pressure.",
                highlights = listOf(
                    "Concentration risk score by asset and category.",
                    "Volatility buckets to spot unstable exposure.",
                    "Early warning when one position dominates."
                )
            ),
            PremiumInsightPreview(
                title = "PERFORMANCE ATTRIBUTION",
                description = "Break down portfolio gains by asset, category, and position size.",
                highlights = listOf(
                    "Identify top gain drivers across the portfolio.",
                    "Compare realized gains vs market movement.",
                    "See drag positions that reduce momentum."
                )
            ),
            PremiumInsightPreview(
                title = "REBALANCE COACH",
                description = "Get target allocation drift alerts and one-tap rebalance suggestions.",
                highlights = listOf(
                    "Set allocation targets and drift thresholds.",
                    "Get alerts when your portfolio slips off target.",
                    "Receive a guided rebalance action plan."
                )
            )
        )
    }

    Text(
        text = stringResource(R.string.analytics_premium_title),
        color = textColor.copy(alpha = 0.65f),
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
    )

    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(ProPalette.SectionSpacing)
    ) {
        previews.forEach { preview ->
            LockedInsightCard(
                preview = preview,
                textColor = textColor,
                onViewDetailsClick = { selectedPreview = preview },
                onUpgradeClick = onUpgradeClick
            )
        }
    }

    selectedPreview?.let { preview ->
        PremiumInsightDetailDialog(
            preview = preview,
            textColor = textColor,
            onDismiss = { selectedPreview = null },
            onUpgradeClick = {
                selectedPreview = null
                onUpgradeClick()
            }
        )
    }
}

@Composable
private fun LockedInsightCard(
    preview: PremiumInsightPreview,
    textColor: Color,
    onViewDetailsClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetailsClick() },
        color = ProPalette.Surface,
        shape = RoundedCornerShape(ProPalette.CardRadius),
        border = BorderStroke(1.dp, ProPalette.NeutralBorder)
    ) {
        Column(
            modifier = Modifier.padding(ProPalette.CardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProLockBadge(
                label = stringResource(R.string.analytics_premium_badge),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = preview.title,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = preview.description,
                color = textColor.copy(alpha = 0.72f),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewDetailsClick,
                    shape = RoundedCornerShape(ProPalette.ButtonRadius),
                    border = BorderStroke(1.dp, ProPalette.NeutralBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textColor
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.analytics_view_details_cta),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp
                    )
                }
                OutlinedButton(
                    onClick = onUpgradeClick,
                    shape = RoundedCornerShape(ProPalette.ButtonRadius),
                    border = BorderStroke(1.dp, ProPalette.AccentBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ProPalette.Accent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.analytics_upgrade_cta),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LargeProAdPanel(
    preview: PremiumInsightPreview,
    textColor: Color,
    onUpgradeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        color = ProPalette.Surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ProPalette.AccentBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProLockBadge(
                label = stringResource(R.string.analytics_premium_badge),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = preview.title,
                color = ProPalette.Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = preview.description,
                color = textColor.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            preview.highlights.forEach { line ->
                Text(
                    text = "• $line",
                    color = textColor.copy(alpha = 0.82f),
                    fontSize = 13.sp
                )
            }
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProPalette.Accent,
                    contentColor = ProPalette.AccentOn
                ),
                shape = RoundedCornerShape(ProPalette.ButtonRadius)
            ) {
                Text(
                    text = stringResource(R.string.analytics_upgrade_cta),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun PremiumInsightDetailDialog(
    preview: PremiumInsightPreview,
    textColor: Color,
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ProPalette.SurfaceElevated,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProLockBadge(
                    label = stringResource(R.string.analytics_premium_badge),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = preview.title,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = preview.description,
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                preview.highlights.forEach { line ->
                    Text(
                        text = "• $line",
                        color = textColor.copy(alpha = 0.78f),
                        fontSize = 12.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = ProPalette.TextPrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onUpgradeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProPalette.Accent,
                    contentColor = ProPalette.AccentOn
                )
            ) {
                Text(text = stringResource(R.string.analytics_upgrade_cta))
            }
        }
    )
}