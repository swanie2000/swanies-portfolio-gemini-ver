package com.swanie.portfolio.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.theme.ProLockBadge
import com.swanie.portfolio.ui.theme.ProPalette
import androidx.compose.foundation.shape.RoundedCornerShape

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

    LaunchedEffect(Unit) {
        settingsViewModel.refreshProPackages {
            packagesLoadAttempted = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(safeBg)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ProLockBadge(
            label = stringResource(R.string.analytics_premium_badge),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.pro_gate_title),
            color = safeText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.pro_gate_subtitle, featureName),
            color = safeText.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.pro_gate_upgrade_coming),
            color = safeText.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (availablePackages.isNotEmpty()) {
            availablePackages.forEach { proPackage ->
                OutlinedButton(
                    onClick = { selectedPackageId = proPackage.identifier },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(ProPalette.ButtonRadius),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selectedPackageId == proPackage.identifier) {
                            accent
                        } else {
                            safeText
                        }
                    )
                ) {
                    Text(
                        text = "${proPackage.title} - ${proPackage.priceText}",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else if (packagesLoadAttempted) {
            Text(
                text = stringResource(R.string.pro_gate_no_packages),
                color = safeText.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
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
                colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText)
            ) {
                Text(
                    text = stringResource(R.string.pro_gate_retry_packages),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
                    Toast.makeText(context, context.getString(msgRes), Toast.LENGTH_SHORT).show()
                }
            },
            enabled = activity != null && selectedPackageId != null && availablePackages.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text(
                text = if (isPurchasing) {
                    stringResource(R.string.pro_gate_purchase_working)
                } else {
                    stringResource(R.string.pro_gate_upgrade_button)
                },
                color = ProPalette.AccentOn,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(ProPalette.SectionSpacing))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText),
            border = androidx.compose.foundation.BorderStroke(1.dp, ProPalette.NeutralBorder)
        ) {
            Text(
                text = stringResource(R.string.pro_gate_go_back),
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(ProPalette.SectionSpacing))

        OutlinedButton(
            onClick = {
                val packageName = context.packageName
                val uri = "https://play.google.com/store/account/subscriptions?package=$packageName".toUri()
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }.onFailure {
                    Toast.makeText(
                        context,
                        context.getString(R.string.pro_gate_manage_subscription_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText),
            border = androidx.compose.foundation.BorderStroke(1.dp, ProPalette.NeutralBorder)
        ) {
            Text(
                text = stringResource(R.string.pro_gate_manage_subscription),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(ProPalette.SectionSpacing))

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
                    Toast.makeText(context, context.getString(msgRes), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText),
            border = androidx.compose.foundation.BorderStroke(1.dp, ProPalette.NeutralBorder)
        ) {
            Text(
                text = if (isRestoring) {
                    stringResource(R.string.settings_restore_purchases_working)
                } else {
                    stringResource(R.string.settings_restore_purchases_button)
                },
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(ProPalette.SectionSpacing))
    }
}

