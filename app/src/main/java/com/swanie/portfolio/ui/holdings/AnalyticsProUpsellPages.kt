package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.theme.ProLockBadge
import com.swanie.portfolio.ui.theme.ProPalette
import java.text.NumberFormat
import java.util.Locale

private data class PremiumInsightPreview(
    val title: String,
    val description: String,
    val highlights: List<String>
)

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
        Column(modifier = Modifier.padding(ProPalette.CardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProLockBadge(label = stringResource(R.string.analytics_premium_badge), modifier = Modifier.fillMaxWidth())
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onViewDetailsClick,
                    shape = RoundedCornerShape(ProPalette.ButtonRadius),
                    border = BorderStroke(1.dp, ProPalette.NeutralBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                    modifier = Modifier.weight(1f)
                ) { Text(text = stringResource(R.string.analytics_view_details_cta), fontWeight = FontWeight.ExtraBold) }
                OutlinedButton(
                    onClick = onUpgradeClick,
                    shape = RoundedCornerShape(ProPalette.ButtonRadius),
                    border = BorderStroke(1.dp, ProPalette.AccentBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ProPalette.Accent),
                    modifier = Modifier.weight(1f)
                ) { Text(text = stringResource(R.string.analytics_upgrade_cta), fontWeight = FontWeight.ExtraBold) }
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
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        color = ProPalette.Surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ProPalette.AccentBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProLockBadge(label = stringResource(R.string.analytics_premium_badge), modifier = Modifier.fillMaxWidth())
            Text(text = preview.title, color = ProPalette.Accent, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(text = preview.description, color = textColor.copy(alpha = 0.9f), fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            preview.highlights.forEach { Text(text = "• $it", color = textColor.copy(alpha = 0.82f), fontSize = 13.sp) }
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProPalette.Accent, contentColor = ProPalette.AccentOn),
                shape = RoundedCornerShape(ProPalette.ButtonRadius)
            ) {
                Text(text = stringResource(R.string.analytics_upgrade_cta), fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun RiskExposureProScreen(textColor: Color, segments: List<AssetSegment>, onUpgradeClick: () -> Unit) {
    val topShare = ((segments.firstOrNull()?.ratio ?: 0f) * 100f).coerceAtLeast(0f)
    val concentrationScore = (topShare * 1.6f).coerceAtMost(100f).toInt()
    val concentrationLabel = when {
        concentrationScore >= 70 -> "HIGH"
        concentrationScore >= 40 -> "MEDIUM"
        else -> "LOW"
    }
    LargeProAdPanel(
        preview = PremiumInsightPreview(
            title = stringResource(R.string.analytics_pro_risk_title),
            description = stringResource(R.string.analytics_pro_risk_description),
            highlights = listOf(
                stringResource(R.string.analytics_pro_risk_highlight_top_position, String.format("%.1f", topShare)),
                stringResource(R.string.analytics_pro_risk_highlight_score, concentrationScore, concentrationLabel),
                stringResource(R.string.analytics_pro_risk_highlight_unlock)
            )
        ),
        textColor = textColor,
        onUpgradeClick = onUpgradeClick
    )
    Spacer(modifier = Modifier.height(12.dp))
    ProInsightMiniCard(title = stringResource(R.string.analytics_pro_risk_snapshot_title), textColor = textColor) {
        val markerColor = when (concentrationLabel) {
            "HIGH" -> Color(0xFFFF6B6B)
            "MEDIUM" -> Color(0xFFFFD54F)
            else -> Color(0xFF66BB6A)
        }
        Text(text = stringResource(R.string.analytics_pro_risk_concentration_label), color = textColor.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.LinearProgressIndicator(
            progress = { concentrationScore / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)),
            color = markerColor,
            trackColor = Color.White.copy(alpha = 0.08f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "$concentrationScore / 100  •  $concentrationLabel", color = markerColor, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun AttributionProScreen(textColor: Color, segments: List<AssetSegment>, totalValue: Double, onUpgradeClick: () -> Unit) {
    val topThree = segments.take(3)
    val combinedShare = topThree.sumOf { it.ratio.toDouble() } * 100.0
    val leadName = topThree.firstOrNull()?.asset?.symbol?.uppercase() ?: "TOP ASSET"
    val leadValue = topThree.firstOrNull()?.value ?: 0.0
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    LargeProAdPanel(
        preview = PremiumInsightPreview(
            title = stringResource(R.string.analytics_pro_attribution_title),
            description = stringResource(R.string.analytics_pro_attribution_description),
            highlights = listOf(
                stringResource(R.string.analytics_pro_attribution_highlight_lead, leadName, currency.format(leadValue)),
                stringResource(R.string.analytics_pro_attribution_highlight_top3, String.format("%.1f", combinedShare)),
                stringResource(R.string.analytics_pro_attribution_highlight_unlock)
            )
        ),
        textColor = textColor,
        onUpgradeClick = onUpgradeClick
    )
    Spacer(modifier = Modifier.height(12.dp))
    ProInsightMiniCard(title = stringResource(R.string.analytics_pro_attribution_timeline_title), textColor = textColor) {
        Text(text = stringResource(R.string.analytics_pro_attribution_timeline_description), color = textColor.copy(alpha = 0.72f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.analytics_pro_attribution_base_value, currency.format(totalValue)), color = textColor.copy(alpha = 0.62f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RebalanceCoachProScreen(textColor: Color, segments: List<AssetSegment>, onUpgradeClick: () -> Unit) {
    val targetPerAsset = if (segments.isNotEmpty()) (100f / segments.take(4).size) else 25f
    val largest = segments.firstOrNull()?.ratio?.times(100f) ?: 0f
    val drift = (largest - targetPerAsset).coerceAtLeast(0f)
    val driftLabel = if (drift >= 8f) {
        stringResource(R.string.analytics_pro_rebalance_drift_action)
    } else {
        stringResource(R.string.analytics_pro_rebalance_drift_range)
    }
    LargeProAdPanel(
        preview = PremiumInsightPreview(
            title = stringResource(R.string.analytics_pro_rebalance_title),
            description = stringResource(R.string.analytics_pro_rebalance_description),
            highlights = listOf(
                stringResource(R.string.analytics_pro_rebalance_highlight_drift, String.format("%.1f", drift)),
                stringResource(R.string.analytics_pro_rebalance_highlight_status, driftLabel),
                stringResource(R.string.analytics_pro_rebalance_highlight_unlock)
            )
        ),
        textColor = textColor,
        onUpgradeClick = onUpgradeClick
    )
}

@Composable
fun ScenariosProScreen(textColor: Color, segments: List<AssetSegment>, totalValue: Double, onUpgradeClick: () -> Unit) {
    val topAsset = segments.firstOrNull()?.asset?.symbol?.uppercase() ?: "TOP"
    val shockPreview = (totalValue * 0.12).coerceAtLeast(0.0)
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    LargeProAdPanel(
        preview = PremiumInsightPreview(
            title = stringResource(R.string.analytics_pro_scenarios_title),
            description = stringResource(R.string.analytics_pro_scenarios_description),
            highlights = listOf(
                stringResource(R.string.analytics_pro_scenarios_highlight_run),
                stringResource(R.string.analytics_pro_scenarios_highlight_preview),
                stringResource(R.string.analytics_pro_scenarios_highlight_worst_case, currency.format(shockPreview))
            )
        ),
        textColor = textColor,
        onUpgradeClick = onUpgradeClick
    )
    Spacer(modifier = Modifier.height(12.dp))
    ProInsightMiniCard(title = stringResource(R.string.analytics_pro_scenarios_presets_title), textColor = textColor) {
        Text(text = stringResource(R.string.analytics_pro_scenarios_preset_crypto), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.analytics_pro_scenarios_preset_metals), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(R.string.analytics_pro_scenarios_preset_top_asset, topAsset), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ProLockBadge(label = stringResource(R.string.analytics_premium_badge), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = preview.title, color = textColor, textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = preview.description, color = textColor.copy(alpha = 0.8f), fontSize = 13.sp)
                preview.highlights.forEach { Text(text = "• $it", color = textColor.copy(alpha = 0.78f), fontSize = 12.sp) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.action_cancel), color = ProPalette.TextPrimary) }
        },
        confirmButton = {
            Button(onClick = onUpgradeClick, colors = ButtonDefaults.buttonColors(containerColor = ProPalette.Accent, contentColor = ProPalette.AccentOn)) {
                Text(text = stringResource(R.string.analytics_upgrade_cta))
            }
        }
    )
}
