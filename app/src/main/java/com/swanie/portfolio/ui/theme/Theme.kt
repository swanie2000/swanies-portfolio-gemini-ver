package com.swanie.portfolio.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import com.swanie.portfolio.ui.Typography

val LocalBackgroundBrush = staticCompositionLocalOf<Brush> {
    error("No background brush provided")
}

internal fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv
}

fun generateColorSchemeFromHex(seedColor: Color): Pair<ColorScheme, ColorScheme> {
    val lightScheme = lightColorScheme(
        primary = seedColor,
        background = Color.Transparent, // Decoupled from theme
        surface = Color.Transparent,    // Decoupled from theme
        onBackground = Color(0xFF1A1A1A), // Dark text for light backgrounds
        onSurface = Color(0xFF1A1A1A)
    )

    val darkScheme = darkColorScheme(
        primary = seedColor,
        background = Color.Transparent, // Decoupled from theme
        surface = Color.Transparent,    // Decoupled from theme
        onBackground = Color.White,         // Light text for dark backgrounds
        onSurface = Color.White
    )

    return Pair(lightScheme, darkScheme)
}

@Composable
fun SwaniesPortfolioTheme(
    seedColorHex: String = "#000416",
    darkTheme: Boolean = true,
    isGradientEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val seedColor = try {
        Color(seedColorHex.toColorInt())
    } catch (e: IllegalArgumentException) {
        Color(0xFF000416) // Fallback to Swanie Navy
    }

    val (lightScheme, darkScheme) = generateColorSchemeFromHex(seedColor)

    val colorScheme = if (darkTheme) darkScheme else lightScheme

    val backgroundBrush = if (isGradientEnabled) {
        val hsv = seedColor.toHsv()
        val hue = hsv[0]
        val saturation = hsv[1]
        val value = hsv[2]

        val topColor = Color.hsv(
            hue,
            (saturation * 0.8f).coerceIn(0f, 1f),
            (value * 1.40f).coerceIn(0f, 1f)
        )

        val bottomColor = Color.hsv(
            hue,
            (saturation * 1.1f).coerceIn(0f, 1f),
            (value * 0.60f).coerceIn(0f, 1f)
        )

        Brush.linearGradient(
            colors = listOf(topColor, bottomColor),
            start = Offset.Zero,
            end = Offset.Infinite
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
