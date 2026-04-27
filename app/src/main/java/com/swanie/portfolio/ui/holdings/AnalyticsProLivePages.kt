package com.swanie.portfolio.ui.holdings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.theme.ProPalette
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private data class ContributionRow(
    val symbol: String,
    val contribution: Double,
    val momentum: Double,
    val category: AssetCategory
)

@Composable
fun RiskExposureLiveScreen(
    textColor: Color,
    segments: List<AssetSegment>,
    surfaceColor: Color,
    accentColor: Color,
    borderColor: Color
) {
    val topShare = ((segments.firstOrNull()?.ratio ?: 0f) * 100f).coerceAtLeast(0f)
    val diversification = (100f - topShare).coerceIn(0f, 100f)
    val hhi = segments.sumOf { (it.ratio * it.ratio).toDouble() } * 100.0
    val volatilityPressure = segments.sumOf { abs(it.asset.priceChange24h) * it.ratio.toDouble() }.coerceAtMost(100.0)
    val cryptoShareRatio = segments.filter { it.asset.category == AssetCategory.CRYPTO }.sumOf { it.ratio.toDouble() }
    val metalShareRatio = 1.0 - cryptoShareRatio
    val categoryImbalance = abs(cryptoShareRatio - metalShareRatio) * 100.0
    val score = (hhi.toFloat() * 0.5f + volatilityPressure.toFloat() * 0.3f + categoryImbalance.toFloat() * 0.2f).coerceAtMost(100f).toInt()
    val riskTone = when {
        score >= 70 -> Color(0xFFFF6B6B)
        score >= 40 -> Color(0xFFFFD54F)
        else -> Color(0xFF66BB6A)
    }
    val riskRegime = when {
        score >= 70 -> stringResource(R.string.analytics_live_risk_regime_defensive)
        score >= 40 -> stringResource(R.string.analytics_live_risk_regime_monitor)
        else -> stringResource(R.string.analytics_live_risk_regime_balanced)
    }
    val animatedScore by animateFloatAsState(score.toFloat(), tween(650, easing = FastOutSlowInEasing), label = "riskScoreAnim")
    val cryptoShare = cryptoShareRatio * 100.0
    val metalShare = 100.0 - cryptoShare

    PremiumHeroStripe(
        stringResource(R.string.analytics_live_risk_hero_title),
        stringResource(R.string.analytics_live_risk_hero_subtitle),
        textColor,
        accentColor,
        borderColor
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_risk_engine_title), textColor, surfaceColor, accentColor, borderColor) {
        Text(stringResource(R.string.analytics_live_risk_engine_description), color = textColor.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModelChip(stringResource(R.string.analytics_live_model_hhi_vol_mix), textColor, accentColor)
            MetricPill(stringResource(R.string.analytics_live_metric_top), "${String.format("%.1f", topShare)}%", textColor, accentColor)
            MetricPill(stringResource(R.string.analytics_live_metric_diversified), "${String.format("%.1f", diversification)}%", textColor, accentColor)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_concentration_score_title), textColor, surfaceColor, accentColor, borderColor) {
        LinearProgressIndicator(
            progress = { animatedScore / 100f },
            modifier = Modifier.fillMaxWidth().height(9.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp)),
            color = riskTone,
            trackColor = Color.White.copy(alpha = 0.08f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text("${animatedScore.toInt()} / 100", color = riskTone, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text(stringResource(R.string.analytics_live_risk_regime_label, riskRegime), color = riskTone, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.analytics_live_risk_top_position_share, String.format("%.1f", topShare)), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Text(stringResource(R.string.analytics_live_risk_hhi_concentration, String.format("%.1f", hhi)), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Text(stringResource(R.string.analytics_live_risk_volatility_pressure, String.format("%.1f", volatilityPressure)), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Text(stringResource(R.string.analytics_live_risk_category_imbalance, String.format("%.1f", categoryImbalance)), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Text(stringResource(R.string.analytics_live_risk_diversification_reserve, String.format("%.1f", diversification)), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_category_balance_title), textColor, surfaceColor, accentColor, borderColor) {
        Text(stringResource(R.string.analytics_live_category_balance_line, String.format("%.1f", cryptoShare), String.format("%.1f", metalShare)), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (cryptoShare / 100.0).toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp)),
            color = Color(0xFF64B5F6),
            trackColor = Color(0xFFFFD54F).copy(alpha = 0.45f)
        )
    }
}

@Composable
fun AttributionLiveScreen(textColor: Color, segments: List<AssetSegment>, surfaceColor: Color, accentColor: Color, borderColor: Color) {
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    val contributions = segments.map {
        ContributionRow(
            symbol = it.asset.symbol.uppercase(),
            contribution = it.value * (it.asset.priceChange24h / 100.0),
            momentum = it.asset.priceChange24h,
            category = it.asset.category
        )
    }
    val top = contributions.sortedByDescending { it.contribution }.take(3)
    val drag = contributions.sortedBy { it.contribution }.take(2)
    val netContribution = contributions.sumOf { it.contribution }
    val netColor = if (netContribution >= 0.0) Color(0xFF66BB6A) else Color(0xFFFF6B6B)
    val animatedNetContribution by animateFloatAsState(netContribution.toFloat(), tween(700, easing = FastOutSlowInEasing), label = "netContributionAnim")
    val positiveCount = contributions.count { it.momentum > 0 }
    val negativeCount = contributions.count { it.momentum < 0 }
    val signalConfidence = contributions.sumOf { abs(it.momentum) }.div(contributions.size.coerceAtLeast(1)).coerceAtMost(100.0)
    val signalLabel = when {
        signalConfidence >= 6.0 -> stringResource(R.string.analytics_live_signal_high)
        signalConfidence >= 3.0 -> stringResource(R.string.analytics_live_signal_medium)
        else -> stringResource(R.string.analytics_live_signal_low)
    }
    val cryptoContribution = contributions.filter { it.category == AssetCategory.CRYPTO }.sumOf { it.contribution }
    val metalContribution = contributions.filter { it.category == AssetCategory.METAL }.sumOf { it.contribution }

    PremiumHeroStripe(
        stringResource(R.string.analytics_live_attribution_hero_title),
        stringResource(R.string.analytics_live_attribution_hero_subtitle),
        textColor,
        accentColor,
        borderColor
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_attribution_engine_title), textColor, surfaceColor, accentColor, borderColor) {
        Text(stringResource(R.string.analytics_live_attribution_engine_description), color = textColor.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.analytics_live_portfolio_24h_contribution, currency.format(animatedNetContribution.toDouble())), color = netColor, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text(stringResource(R.string.analytics_live_momentum_breadth, positiveCount, negativeCount), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Text(stringResource(R.string.analytics_live_signal_confidence, String.format("%.1f", signalConfidence), signalLabel), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModelChip(stringResource(R.string.analytics_live_model_weighted_24h), textColor, accentColor)
            MetricPill(stringResource(R.string.analytics_live_metric_positive), positiveCount.toString(), textColor, Color(0xFF66BB6A))
            MetricPill(stringResource(R.string.analytics_live_metric_negative), negativeCount.toString(), textColor, Color(0xFFFF8A80))
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_category_contribution_title), textColor, surfaceColor, accentColor, borderColor) {
        Text(stringResource(R.string.analytics_live_category_crypto_contribution, currency.format(cryptoContribution)), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.analytics_live_category_metal_contribution, currency.format(metalContribution)), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_top_drivers_title), textColor, surfaceColor, accentColor, borderColor) {
        top.forEach { row ->
            Text("${row.symbol}  ${currency.format(row.contribution)}  (${String.format("%.2f", row.momentum)}%)", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_drag_positions_title), textColor, surfaceColor, accentColor, borderColor) {
        drag.forEach { row ->
            Text("${row.symbol}  ${currency.format(row.contribution)}  (${String.format("%.2f", row.momentum)}%)", color = if (row.contribution < 0) Color(0xFFFF8A80) else textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun RebalanceLiveScreen(textColor: Color, segments: List<AssetSegment>, totalValue: Double, surfaceColor: Color, accentColor: Color, borderColor: Color) {
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    val workingSet = segments.take(6)
    val hasBothCategories = workingSet.any { it.asset.category == AssetCategory.CRYPTO } && workingSet.any { it.asset.category == AssetCategory.METAL }
    val targetMode = if (hasBothCategories) {
        stringResource(R.string.analytics_live_target_mode_category_balanced)
    } else {
        stringResource(R.string.analytics_live_target_mode_equal_weight)
    }
    val targetBySymbol: Map<String, Float> = if (hasBothCategories) {
        val cryptoSet = workingSet.filter { it.asset.category == AssetCategory.CRYPTO }
        val metalSet = workingSet.filter { it.asset.category == AssetCategory.METAL }
        buildMap {
            cryptoSet.forEach { put(it.asset.symbol.uppercase(), (0.5f / cryptoSet.size.coerceAtLeast(1))) }
            metalSet.forEach { put(it.asset.symbol.uppercase(), (0.5f / metalSet.size.coerceAtLeast(1))) }
        }
    } else {
        val equalTarget = if (workingSet.isNotEmpty()) 1f / workingSet.size else 0f
        workingSet.associate { it.asset.symbol.uppercase() to equalTarget }
    }
    val driftRows = workingSet.map {
        val symbol = it.asset.symbol.uppercase()
        val target = targetBySymbol[symbol] ?: 0f
        val driftRatio = it.ratio - target
        Triple(symbol, driftRatio, driftRatio * totalValue)
    }.sortedByDescending { abs(it.second) }
    val threshold = 0.04f
    val outOfBandCount = driftRows.count { abs(it.second) >= threshold }
    val estimatedTurnover = driftRows.sumOf { abs(it.third) } / 2.0
    val animatedOutOfBandCount by animateIntAsState(outOfBandCount, tween(550, easing = FastOutSlowInEasing), label = "outOfBandAnim")

    PremiumHeroStripe(
        stringResource(R.string.analytics_live_rebalance_hero_title),
        stringResource(R.string.analytics_live_rebalance_hero_subtitle),
        textColor,
        accentColor,
        borderColor
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_rebalance_engine_title), textColor, surfaceColor, accentColor, borderColor) {
        Text(stringResource(R.string.analytics_live_rebalance_model_active, targetMode, workingSet.size.coerceAtLeast(1)), color = textColor.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.analytics_live_rebalance_primary_target_band), color = ProPalette.Accent, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text(stringResource(R.string.analytics_live_rebalance_band_threshold, String.format("%.1f", threshold * 100f), animatedOutOfBandCount), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Text(stringResource(R.string.analytics_live_rebalance_estimated_turnover, currency.format(estimatedTurnover)), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        ModelChip(stringResource(R.string.analytics_live_model_dynamic, targetMode), textColor, accentColor)
        Spacer(modifier = Modifier.height(6.dp))
        MetricPill(stringResource(R.string.analytics_live_metric_out_of_band), animatedOutOfBandCount.toString(), textColor, if (animatedOutOfBandCount > 0) Color(0xFFFFD54F) else accentColor)
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_action_queue_title), textColor, surfaceColor, accentColor, borderColor) {
        driftRows.take(4).forEach { row ->
            val action = if (row.second > 0) stringResource(R.string.analytics_live_action_trim) else stringResource(R.string.analytics_live_action_add)
            val bandTone = if (abs(row.second) >= threshold) Color(0xFFFFD54F) else textColor
            Text("$action ${row.first}  ${currency.format(abs(row.third))}  (${String.format("%.1f", abs(row.second) * 100f)}%)", color = bandTone, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun ScenariosLiveScreen(textColor: Color, segments: List<AssetSegment>, totalValue: Double, surfaceColor: Color, accentColor: Color, borderColor: Color) {
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    val marketShock = stringResource(R.string.analytics_live_scenario_market_shock)
    val cryptoWinter = stringResource(R.string.analytics_live_scenario_crypto_winter)
    val rateSpike = stringResource(R.string.analytics_live_scenario_rate_spike)
    var selectedScenario by remember { mutableStateOf(marketShock) }
    val scenarioDropPct = when (selectedScenario) {
        marketShock -> 0.15
        cryptoWinter -> 0.22
        rateSpike -> 0.10
        else -> 0.15
    }
    val scenarioRallyPct = when (selectedScenario) {
        marketShock -> 0.12
        cryptoWinter -> 0.05
        rateSpike -> 0.08
        else -> 0.12
    }
    val scenarioInflationPct = when (selectedScenario) {
        marketShock -> 0.08
        cryptoWinter -> 0.11
        rateSpike -> 0.14
        else -> 0.08
    }
    val cryptoValue = segments.filter { it.asset.category == AssetCategory.CRYPTO }.sumOf { it.value }
    val metalValue = segments.filter { it.asset.category == AssetCategory.METAL }.sumOf { it.value }
    val marketShockImpact = -(totalValue * scenarioDropPct)
    val cryptoRallyImpact = cryptoValue * scenarioRallyPct
    val inflationHedgeImpact = metalValue * scenarioInflationPct
    val worstCase = min(marketShockImpact, min(cryptoRallyImpact, inflationHedgeImpact))
    val bestCase = max(marketShockImpact, max(cryptoRallyImpact, inflationHedgeImpact))
    val midCase = (marketShockImpact + cryptoRallyImpact + inflationHedgeImpact) / 3.0
    val animatedWorst by animateFloatAsState(worstCase.toFloat(), tween(650, easing = FastOutSlowInEasing), label = "scenarioWorstAnim")
    val animatedBest by animateFloatAsState(bestCase.toFloat(), tween(650, easing = FastOutSlowInEasing), label = "scenarioBestAnim")
    val animatedMid by animateFloatAsState(midCase.toFloat(), tween(650, easing = FastOutSlowInEasing), label = "scenarioMidAnim")
    val stressScore = (abs(worstCase) / totalValue.coerceAtLeast(1.0) * 100.0).coerceIn(0.0, 100.0)
    val animatedStressScore by animateFloatAsState(stressScore.toFloat(), tween(700, easing = FastOutSlowInEasing), label = "scenarioStressAnim")

    PremiumHeroStripe(
        stringResource(R.string.analytics_live_scenarios_hero_title),
        stringResource(R.string.analytics_live_scenarios_hero_subtitle),
        textColor,
        accentColor,
        borderColor
    )
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_scenario_preset_title), textColor, surfaceColor, accentColor, borderColor) {
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(marketShock, cryptoWinter, rateSpike).forEach { option ->
                FilterChip(
                    selected = selectedScenario == option,
                    onClick = { selectedScenario = option },
                    label = { Text(option, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = textColor.copy(alpha = 0.06f),
                        labelColor = textColor.copy(alpha = 0.8f),
                        selectedContainerColor = accentColor.copy(alpha = 0.2f),
                        selectedLabelColor = textColor
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedScenario == option,
                        borderColor = borderColor,
                        selectedBorderColor = accentColor.copy(alpha = 0.4f)
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.analytics_live_active_preset, selectedScenario), color = textColor.copy(alpha = 0.78f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_scenario_model_title), textColor, surfaceColor, accentColor, borderColor) {
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModelChip(stringResource(R.string.analytics_live_model_shock_matrix), textColor, accentColor)
            MetricPill(stringResource(R.string.analytics_live_metric_base), currency.format(totalValue), textColor, accentColor)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(stringResource(R.string.analytics_live_stress_score, animatedStressScore.toInt()), color = if (animatedStressScore >= 35f) Color(0xFFFF8A80) else accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedStressScore / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp)),
            color = if (animatedStressScore >= 35f) Color(0xFFFF8A80) else Color(0xFF66BB6A),
            trackColor = textColor.copy(alpha = 0.12f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(stringResource(R.string.analytics_live_worst_case, currency.format(animatedWorst.toDouble())), color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.analytics_live_base_case, currency.format(animatedMid.toDouble())), color = textColor.copy(alpha = 0.78f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.analytics_live_best_case, currency.format(animatedBest.toDouble())), color = Color(0xFF66BB6A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProInsightMiniCard(stringResource(R.string.analytics_live_preset_events_title), textColor, surfaceColor, accentColor, borderColor) {
        Text(stringResource(R.string.analytics_live_preset_event_market_shock, String.format("%.0f", scenarioDropPct * 100)), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.analytics_live_preset_event_crypto_rally, String.format("%.0f", scenarioRallyPct * 100)), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.analytics_live_preset_event_inflation_hedge, String.format("%.0f", scenarioInflationPct * 100)), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
