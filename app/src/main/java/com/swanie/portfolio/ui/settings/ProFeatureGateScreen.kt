package com.swanie.portfolio.ui.settings

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.swanie.portfolio.R
import com.swanie.portfolio.billing.MonetizationPackage
import com.swanie.portfolio.ui.components.showPortfolioToast
import com.swanie.portfolio.ui.theme.ProLockBadge
import com.swanie.portfolio.ui.theme.ProPalette

@Composable
fun ProFeatureGateScreen(
    featureName: String,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundHex: String = "#000000",
    textHex: String = "#FFFFFF",
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val safeBg = ProPalette.Background
    val safeText = ProPalette.TextPrimary
    val accent = ProPalette.Accent
    var isRestoring by remember { mutableStateOf(false) }
    var selectedPackageId by remember { mutableStateOf<String?>(null) }
    var isPurchasing by remember { mutableStateOf(false) }
    var packagesLoadAttempted by remember { mutableStateOf(false) }
    val availablePackages by settingsViewModel.availableProPackages.collectAsState()
    val scroll = rememberScrollState()

    LaunchedEffect(Unit) {
        settingsViewModel.refreshProPackages {
            packagesLoadAttempted = true
        }
    }

    LaunchedEffect(availablePackages) {
        if (selectedPackageId == null && availablePackages.isNotEmpty()) {
            selectedPackageId = availablePackages.firstOrNull { pkg ->
                val id = pkg.identifier.lowercase()
                "annual" in id || "yearly" in id
            }?.identifier ?: availablePackages.first().identifier
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(safeBg)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProLockBadge(
            label = stringResource(R.string.analytics_premium_badge),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.pro_gate_title),
            color = safeText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.pro_gate_subtitle, featureName),
            color = safeText.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.pro_gate_value_props),
            color = accent.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.pro_gate_choose_plan),
            color = safeText.copy(alpha = 0.62f),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(22.dp))

        if (availablePackages.isNotEmpty()) {
            availablePackages.forEach { proPackage ->
                ProPlanOptionCard(
                    proPackage = proPackage,
                    isSelected = selectedPackageId == proPackage.identifier,
                    onSelect = { selectedPackageId = proPackage.identifier },
                    accent = accent,
                    safeText = safeText,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        } else if (packagesLoadAttempted) {
            Text(
                text = stringResource(R.string.pro_gate_no_packages),
                color = safeText.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    settingsViewModel.refreshProPackages {
                        packagesLoadAttempted = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(ProPalette.ButtonRadius),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText),
            ) {
                Text(
                    text = stringResource(R.string.pro_gate_retry_packages),
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            onClick = {
                val targetPackage = selectedPackageId
                if (activity == null || targetPackage == null || isPurchasing) return@Button
                isPurchasing = true
                settingsViewModel.purchaseProPackage(activity, targetPackage) { success ->
                    isPurchasing = false
                    val msgRes = if (success) {
                        R.string.pro_gate_purchase_success
                    } else {
                        R.string.pro_gate_purchase_failed
                    }
                    context.showPortfolioToast(context.getString(msgRes))
                }
            },
            enabled = activity != null && selectedPackageId != null && availablePackages.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                disabledContainerColor = accent.copy(alpha = 0.28f),
                disabledContentColor = ProPalette.AccentOn.copy(alpha = 0.45f),
            ),
        ) {
            Text(
                text = if (isPurchasing) {
                    stringResource(R.string.pro_gate_purchase_working)
                } else {
                    stringResource(R.string.pro_gate_upgrade_button)
                },
                color = ProPalette.AccentOn,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText),
            border = BorderStroke(1.dp, ProPalette.NeutralBorder),
        ) {
            Text(
                text = stringResource(R.string.pro_gate_go_back),
                fontWeight = FontWeight.Black,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                val packageName = context.packageName
                val uri = "https://play.google.com/store/account/subscriptions?package=$packageName".toUri()
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }.onFailure {
                    context.showPortfolioToast(
                        context.getString(R.string.pro_gate_manage_subscription_failed),
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText.copy(alpha = 0.85f)),
            border = BorderStroke(1.dp, ProPalette.NeutralBorder),
        ) {
            Text(
                text = stringResource(R.string.pro_gate_manage_subscription),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                if (isRestoring) return@OutlinedButton
                isRestoring = true
                settingsViewModel.restorePurchases { result ->
                    isRestoring = false
                    val msgRes = when (result) {
                        RestorePurchasesResult.ALREADY_ACTIVE ->
                            R.string.settings_restore_purchases_already_active
                        RestorePurchasesResult.RESTORED ->
                            R.string.settings_restore_purchases_success
                        RestorePurchasesResult.NO_ENTITLEMENT_FOUND ->
                            R.string.settings_restore_purchases_no_entitlement
                        RestorePurchasesResult.FAILED ->
                            R.string.settings_restore_purchases_failed
                    }
                    context.showPortfolioToast(context.getString(msgRes))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText.copy(alpha = 0.85f)),
            border = BorderStroke(1.dp, ProPalette.NeutralBorder),
        ) {
            Text(
                text = if (isRestoring) {
                    stringResource(R.string.settings_restore_purchases_working)
                } else {
                    stringResource(R.string.settings_restore_purchases_button)
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProPlanOptionCard(
    proPackage: MonetizationPackage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    accent: Color,
    safeText: Color,
) {
    val planKind = resolveProPlanKind(proPackage.identifier)
    val showBestValue = planKind == ProPlanKind.YEARLY
    val borderColor = if (isSelected) accent else ProPalette.NeutralBorder
    val surfaceColor = if (isSelected) {
        accent.copy(alpha = 0.14f)
    } else {
        ProPalette.SurfaceElevated
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ProPalette.CardRadius))
                .background(surfaceColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(ProPalette.CardRadius),
                )
                .clickable(onClick = onSelect)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            if (showBestValue) {
                Text(
                    text = stringResource(R.string.pro_plan_best_value),
                    color = accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pro_gate_brand_line),
                        color = safeText.copy(alpha = if (isSelected) 0.92f else 0.72f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.pro_plan_line_format,
                            planKindLabel(planKind),
                            proPackage.priceText,
                        ),
                        color = if (isSelected) accent else safeText,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accent else Color.Transparent,
                        )
                        .border(
                            width = if (isSelected) 0.dp else 1.5.dp,
                            color = if (isSelected) Color.Transparent else safeText.copy(alpha = 0.35f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ProPalette.AccentOn,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

private enum class ProPlanKind {
    MONTHLY,
    YEARLY,
    LIFETIME,
    OTHER,
}

@Composable
private fun planKindLabel(kind: ProPlanKind): String = when (kind) {
    ProPlanKind.MONTHLY -> stringResource(R.string.pro_plan_monthly)
    ProPlanKind.YEARLY -> stringResource(R.string.pro_plan_yearly)
    ProPlanKind.LIFETIME -> stringResource(R.string.pro_plan_lifetime)
    ProPlanKind.OTHER -> stringResource(R.string.pro_plan_lifetime)
}

private fun resolveProPlanKind(identifier: String): ProPlanKind {
    val id = identifier.lowercase()
    return when {
        "monthly" in id -> ProPlanKind.MONTHLY
        "annual" in id || "yearly" in id -> ProPlanKind.YEARLY
        "lifetime" in id -> ProPlanKind.LIFETIME
        else -> ProPlanKind.OTHER
    }
}
