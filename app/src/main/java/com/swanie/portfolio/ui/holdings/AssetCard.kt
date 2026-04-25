package com.swanie.portfolio.ui.holdings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * **Font-scale lock** for adaptive asset cards: `sp` sizes follow app layout + [isHighVisibilityMode],
 * not Android accessibility font scale (which would blow up boutique rows).
 *
 * Equivalent to wrapping the card root in:
 * `CompositionLocalProvider(LocalDensity provides Density(density = LocalDensity.current.density, fontScale = 1f))`
 * while capturing the parent density *before* overriding (see [base]).
 *
 * Dynamic totals and unit prices use [AutoResizingText] in `HoldingsUIComponents.kt` (full/compact cards).
 */
@Composable
fun AssetCardFontScaleScope(content: @Composable () -> Unit) {
    val base = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density = base.density, fontScale = 1f),
    ) {
        content()
    }
}
