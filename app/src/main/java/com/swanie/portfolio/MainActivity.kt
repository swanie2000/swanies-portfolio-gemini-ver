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
import com.swanie.portfolio.ui.navigation.NavGraph
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // FIX: Changed from isThemeReady to isDataReady.
        // The splash screen will now hang until the Yahoo/CoinGecko audit finishes.
        splashScreen.setKeepOnScreenCondition {
            !viewModel.isDataReady.value
        }

        enableEdgeToEdge()

        setContent {
            val siteBgColor by viewModel.siteBackgroundColor.collectAsStateWithLifecycle()
            val useGradient by viewModel.useGradient.collectAsStateWithLifecycle()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            // Handle Status Bar / Nav Bar icons based on dark mode
            LaunchedEffect(isDarkMode) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkMode
                insetsController.isAppearanceLightNavigationBars = !isDarkMode
            }

            SwaniesPortfolioTheme(
                seedColorHex = siteBgColor,
                isGradientEnabled = useGradient
            ) {
                val navController = rememberNavController()
                NavGraph(navController = navController, mainViewModel = viewModel)
            }
        }
    }
}