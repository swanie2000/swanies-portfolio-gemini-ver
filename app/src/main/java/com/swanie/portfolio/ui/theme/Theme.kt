package com.swanie.portfolio.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.swanie.portfolio.ui.Typography

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
    content: @Composable () -> Unit
) {
    val (lightScheme, darkScheme) = generateColorSchemeFromHex(seedColorHex)

    val colorScheme = if (darkTheme) {
        darkScheme
    } else {
        lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
