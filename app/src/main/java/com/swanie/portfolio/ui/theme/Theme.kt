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

/**
 * A CompositionLocal to hold the background brush.
 * This allows us to switch between a solid color and a gradient background.
 */
val LocalBackgroundBrush = staticCompositionLocalOf<Brush> {
    error("No background brush provided")
}

/**
 * Converts a Compose [Color] to its HSV (Hue, Saturation, Value) representation.
 * Accessible within the :app module.
 */
internal fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv
}

/**
 * Generates a light and dark color scheme from a given HEX color string.
 * @param seedColorHex The primary color to base the theme on, e.g., "#0056D2".
 */
fun generateColorSchemeFromHex(seedColorHex: String): Pair<ColorScheme, ColorScheme> {
    val seed = try {
        Color(seedColorHex.toColorInt())
    } catch (e: IllegalArgumentException) {
        // Fallback to a default if the hex is invalid
        Color(0xFF000416)
    }

    // For light theme, use a very light tint of the seed color for the background.
    val lightScheme = lightColorScheme(
        primary = seed,
        background = seed.copy(alpha = 0.05f), // Faint tonal surface (95% white)
        surface = seed.copy(alpha = 0.05f),
        onBackground = Color(0xFF1A1A1A), // Use a dark grey instead of pure black
        onSurface = Color(0xFF1A1A1A)
    )

    // For dark theme, the seed color itself becomes the background.
    val darkScheme = darkColorScheme(
        primary = seed,
        background = seed,
        surface = seed,
        onBackground = Color.White,
        onSurface = Color.White
    )

    return Pair(lightScheme, darkScheme)
}

@Composable
fun SwaniesPortfolioTheme(
    seedColorHex: String = "#000416", // Default to Swanie Navy
    darkTheme: Boolean = true,
    isGradientEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val (lightScheme, darkScheme) = generateColorSchemeFromHex(seedColorHex)

    val colorScheme = if (darkTheme) {
        darkScheme
    } else {
        lightScheme
    }

    val backgroundBrush = if (isGradientEnabled) {
        val hsv = colorScheme.background.toHsv()
        val hue = hsv[0]
        val saturation = hsv[1]
        val value = hsv[2]

        val topColor = Color.hsv(
            hue,
            (saturation * 0.8f).coerceIn(0f, 1f), // Decrease saturation for the highlight effect
            (value * 1.40f).coerceIn(0f, 1f)  // Increase value for a brighter top color
        )

        val bottomColor = Color.hsv(
            hue,
            (saturation * 1.1f).coerceIn(0f, 1f), // Increase saturation for a richer shadow
            (value * 0.60f).coerceIn(0f, 1f)   // Decrease value for a darker bottom color
        )

        Brush.linearGradient(
            colors = listOf(topColor, bottomColor),
            start = Offset(0f, 0f),       // Top-Left
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) // Bottom-Right
        )
    } else {
        SolidColor(colorScheme.background)
    }

    CompositionLocalProvider(LocalBackgroundBrush provides backgroundBrush) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
