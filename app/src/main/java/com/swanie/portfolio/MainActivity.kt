package com.swanie.portfolio

import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.ui.navigation.NavGraph
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(ThemePreferences(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Set up a pre-draw listener to wait for the theme to be ready.
        // This is the modern replacement for setKeepOnScreenCondition.
        val content: android.view.View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // The first frame is blocked until the theme is ready.
                    return if (viewModel.isThemeReady.value) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )

        // Asynchronously update the window background as soon as the color is loaded.
        lifecycleScope.launch {
            val hex = viewModel.themeColorHex.first()
            try {
                val color = hex.toColorInt()
                Log.d("ThemeSync", "Setting window background to: $hex")
                window.setBackgroundDrawable(color.toDrawable())
            } catch (e: IllegalArgumentException) {
                Log.e("ThemeSync", "Invalid hex color: $hex, falling back to default.")
                window.setBackgroundDrawable("#000416".toColorInt().toDrawable())
            }
        }

        enableEdgeToEdge()

        setContent {
            val seedColorHex by viewModel.themeColorHex.collectAsStateWithLifecycle()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val isGradientEnabled by viewModel.isGradientEnabled.collectAsStateWithLifecycle()

            // This effect synchronizes the system bar icon colors with the current theme.
            LaunchedEffect(isDarkMode) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkMode
                insetsController.isAppearanceLightNavigationBars = !isDarkMode
            }

            SwaniesPortfolioTheme(
                seedColorHex = seedColorHex,
                darkTheme = isDarkMode,
                isGradientEnabled = isGradientEnabled
            ) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
