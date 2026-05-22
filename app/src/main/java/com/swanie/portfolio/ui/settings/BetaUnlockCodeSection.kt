package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.components.showPortfolioToast
import com.swanie.portfolio.ui.theme.ProPalette

@Composable
fun BetaUnlockCodeSection(
    settingsViewModel: SettingsViewModel,
    safeText: Color,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    if (!settingsViewModel.isBetaUnlockConfigured) return

    val context = LocalContext.current
    var codeInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.beta_unlock_section_title),
            color = safeText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.beta_unlock_section_hint),
            color = safeText.copy(alpha = 0.72f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it.trim().uppercase() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.beta_unlock_code_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = safeText,
                unfocusedTextColor = safeText,
                focusedBorderColor = accent,
                unfocusedBorderColor = ProPalette.NeutralBorder,
                focusedLabelColor = accent,
                unfocusedLabelColor = safeText.copy(alpha = 0.6f),
            ),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                if (isSubmitting || codeInput.isBlank()) return@OutlinedButton
                isSubmitting = true
                settingsViewModel.redeemBetaUnlockCode(codeInput) { result ->
                    isSubmitting = false
                    val msgRes = when (result) {
                        BetaUnlockRedeemResult.SUCCESS -> R.string.beta_unlock_success
                        BetaUnlockRedeemResult.NOT_CONFIGURED ->
                            R.string.beta_unlock_error_not_configured
                        BetaUnlockRedeemResult.PROGRAM_ENDED ->
                            R.string.beta_unlock_error_program_ended
                        BetaUnlockRedeemResult.MALFORMED ->
                            R.string.beta_unlock_error_invalid
                        BetaUnlockRedeemResult.WRONG_EMAIL ->
                            R.string.beta_unlock_error_email
                        BetaUnlockRedeemResult.EXPIRED ->
                            R.string.beta_unlock_error_expired
                        BetaUnlockRedeemResult.INVALID ->
                            R.string.beta_unlock_error_invalid
                    }
                    context.showPortfolioToast(context.getString(msgRes))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(ProPalette.ButtonRadius),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = accent),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
        ) {
            Text(
                text = if (isSubmitting) {
                    stringResource(R.string.pro_gate_purchase_working)
                } else {
                    stringResource(R.string.beta_unlock_submit)
                },
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
            )
        }
    }
}
