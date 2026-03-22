package com.swanie.portfolio.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import com.swanie.portfolio.ui.Typography

// CHANGE: Switched to compositionLocalOf for reactive updates when gradient toggles
val LocalBackgroundBrush = compositionLocalOf<Brush> {
    SolidColor(Color(0xFF000416))
}

internal fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv
}

@Composable
fun SwaniesPortfolioTheme(
    seedColorHex: String = "#000416",
    isGradientEnabled: Boolean = false,
    gradientAmount: Float = 0.5f,
    content: @Composable () -> Unit
) {
    val seedColor = try {
        Color(seedColorHex.toColorInt())
    } catch (e: Exception) {
        Color(0xFF000416)
    }

    val colorScheme = darkColorScheme(primary = seedColor)

    val backgroundBrush = if (isGradientEnabled) {
        val hsv = seedColor.toHsv()
        val hue = hsv[0]
        val saturation = hsv[1]
        val value = hsv[2]

        val topColor = Color.hsv(
            hue,
            (saturation * (1f - (gradientAmount * 0.4f))).coerceIn(0f, 1f),
            (value * (1f + (gradientAmount * 0.8f))).coerceIn(0f, 1f)
        )

        val bottomColor = Color.hsv(
            hue,
            (saturation * (1f + (gradientAmount * 0.2f))).coerceIn(0f, 1f),
            (value * (1f - (gradientAmount * 0.6f))).coerceIn(0f, 1f)
        )

        Brush.verticalGradient(
            colors = listOf(topColor, bottomColor)
        )
    } else {
        SolidColor(seedColor)
    }

    CompositionLocalProvider(LocalBackgroundBrush provides backgroundBrush) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
