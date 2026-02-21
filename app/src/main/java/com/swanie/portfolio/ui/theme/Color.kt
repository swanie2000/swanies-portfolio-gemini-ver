package com.swanie.portfolio.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Brand Colors
val NavyBackground = Color(0xFF000416)
val NavyPrimary = Color(0xFF0056D2)

val EmeraldBackground = Color(0xFF062C22)
val EmeraldPrimary = Color(0xFF00C853)

val RoyalBackground = Color(0xFF001A3D)
val RoyalPrimary = Color(0xFF1976D2)

val CharcoalBackground = Color(0xFF121212)
val CharcoalPrimary = Color(0xFFBB86FC) // A typical Material purple

val BurgundyBackground = Color(0xFF2D0B0B)
val BurgundyPrimary = Color(0xFFD32F2F)

// Color Schemes
val NavyColorScheme = darkColorScheme(
    primary = NavyPrimary,
    background = NavyBackground,
    surface = NavyBackground
)

val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    background = EmeraldBackground,
    surface = EmeraldBackground
)

val RoyalColorScheme = darkColorScheme(
    primary = RoyalPrimary,
    background = RoyalBackground,
    surface = RoyalBackground
)

val CharcoalColorScheme = darkColorScheme(
    primary = CharcoalPrimary,
    background = CharcoalBackground,
    surface = CharcoalBackground
)

val BurgundyColorScheme = darkColorScheme(
    primary = BurgundyPrimary,
    background = BurgundyBackground,
    surface = BurgundyBackground
)

val colorSchemes = listOf(
    NavyColorScheme,
    EmeraldColorScheme,
    RoyalColorScheme,
    CharcoalColorScheme,
    BurgundyColorScheme
)
