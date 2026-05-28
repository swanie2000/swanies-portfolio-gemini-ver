package com.swanie.portfolio.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.BuildConfig

/**
 * Small build stamp for auth / onboarding screens (e.g. v1.0.14 — matches Play versionName).
 * Pin with [Modifier.align][androidx.compose.ui.Alignment.TopEnd]
 * on a root [androidx.compose.foundation.layout.Box] that does **not** use [imePadding].
 */
@Composable
fun BuildVersionLabel(
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White.copy(alpha = 0.45f),
) {
    Text(
        text = "v${BuildConfig.VERSION_NAME}",
        color = contentColor,
        fontSize = 9.sp,
        fontWeight = FontWeight.Normal,
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 2.dp, end = 10.dp),
    )
}
