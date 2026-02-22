package com.swanie.portfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.ui.navigation.NavGraph
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(ThemePreferences(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen visible until the theme is ready.
        splashScreen.setKeepOnScreenCondition { !viewModel.isThemeReady.value }

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
                NavGraph(navController = navController, mainViewModel = viewModel)
            }
        }
    }
}
