package com.swanie.portfolio.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.BuildConfig

/**
 * Small build stamp for auth / onboarding screens so testers can confirm Play version
 * before creating an account or signing in.
 */
@Composable
fun BuildVersionLabel(
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White.copy(alpha = 0.45f),
) {
    Text(
        text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        color = contentColor,
        fontSize = 9.sp,
        fontWeight = FontWeight.Normal,
        modifier = modifier
            .navigationBarsPadding()
            .padding(end = 10.dp, bottom = 0.dp),
    )
}
