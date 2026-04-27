package com.swanie.portfolio.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swanie.portfolio.R

@Composable
fun UpgradeToProScreen(
    settingsViewModel: SettingsViewModel,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val siteBgColorHex by themeViewModel.siteBackgroundColor.collectAsStateWithLifecycle(initialValue = "#000416")
    val siteTextColorHex by themeViewModel.siteTextColor.collectAsStateWithLifecycle(initialValue = "#FFFFFF")
    ProFeatureGateScreen(
        featureName = stringResource(R.string.pro_feature_upgrade_now),
        settingsViewModel = settingsViewModel,
        onBack = onBack,
        backgroundHex = siteBgColorHex,
        textHex = siteTextColorHex
    )
}

@Composable
fun RevenueCatTestInfoScreen(
    settingsViewModel: SettingsViewModel,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val siteBgColorHex by themeViewModel.siteBackgroundColor.collectAsStateWithLifecycle(initialValue = "#000416")
    val siteTextColorHex by themeViewModel.siteTextColor.collectAsStateWithLifecycle(initialValue = "#FFFFFF")
    val isProUser by settingsViewModel.isProUser.collectAsStateWithLifecycle()
    val proPackages by settingsViewModel.availableProPackages.collectAsStateWithLifecycle()
    val isRevenueCatConfigured = settingsViewModel.isRevenueCatConfigured
    val entitlementId = settingsViewModel.revenueCatEntitlementId
    val offeringId = settingsViewModel.revenueCatOfferingId
    val safeBg = Color(runCatching { siteBgColorHex.toColorInt() }.getOrDefault("#000416".toColorInt()))
    val safeText = Color(runCatching { siteTextColorHex.toColorInt() }.getOrDefault("#FFFFFF".toColorInt()))
    var isRestoring by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(safeBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.settings_test_info_title),
            color = safeText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.settings_pro_status_label),
            color = safeText.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isProUser) {
                stringResource(R.string.settings_pro_status_active)
            } else {
                stringResource(R.string.settings_pro_status_inactive)
            },
            color = if (isProUser) Color(0xFF4CAF50) else safeText.copy(alpha = 0.8f),
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.settings_revenuecat_status_label),
            color = safeText.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isRevenueCatConfigured) {
                stringResource(R.string.settings_revenuecat_status_configured)
            } else {
                stringResource(R.string.settings_revenuecat_status_missing)
            },
            color = if (isRevenueCatConfigured) Color(0xFF4CAF50) else Color(0xFFFFA000),
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.settings_revenuecat_packages_label),
            color = safeText.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = proPackages.size.toString(),
            color = safeText.copy(alpha = 0.85f),
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.settings_revenuecat_checklist_line_1, entitlementId),
            color = safeText.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
        Text(
            text = stringResource(R.string.settings_revenuecat_checklist_line_2, offeringId),
            color = safeText.copy(alpha = 0.7f),
            fontSize = 13.sp
        )

        if (isRevenueCatConfigured && proPackages.isEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.settings_revenuecat_warning_no_packages),
                color = Color(0xFFFFA000),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                val checklist = context.getString(
                    R.string.settings_revenuecat_copy_payload,
                    entitlementId,
                    offeringId
                )
                clipboard?.setPrimaryClip(ClipData.newPlainText("RevenueCat checklist", checklist))
                Toast.makeText(
                    context,
                    context.getString(R.string.settings_revenuecat_copied),
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText)
        ) {
            Text(
                text = stringResource(R.string.settings_revenuecat_copy_button),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
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
            colors = ButtonDefaults.outlinedButtonColors(contentColor = safeText)
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

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.settings_test_plan_title),
            color = safeText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_test_plan_step_1),
            color = safeText.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
        Text(
            text = stringResource(R.string.settings_test_plan_step_2),
            color = safeText.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
        Text(
            text = stringResource(R.string.settings_test_plan_step_3),
            color = safeText.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
        Text(
            text = stringResource(R.string.settings_test_plan_step_4),
            color = safeText.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
        Text(
            text = stringResource(R.string.settings_test_plan_step_5),
            color = safeText.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
        Text(
            text = stringResource(R.string.settings_test_plan_step_6),
            color = safeText.copy(alpha = 0.85f),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.12f))
        ) {
            Text(
                text = stringResource(R.string.pro_gate_go_back),
                color = safeText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
