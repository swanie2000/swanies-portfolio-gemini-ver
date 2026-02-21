package com.swanie.portfolio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.swanie.portfolio.ui.Typography

@Composable
fun SwaniesPortfolioTheme(
    themeIndex: Int = 0, // Default to Navy
    darkTheme: Boolean = true, // We are only using dark themes
    content: @Composable () -> Unit
) {
    // Ensure the index is within the bounds of our color schemes list
    val safeIndex = themeIndex.coerceIn(colorSchemes.indices)
    val colorScheme = colorSchemes[safeIndex]

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
