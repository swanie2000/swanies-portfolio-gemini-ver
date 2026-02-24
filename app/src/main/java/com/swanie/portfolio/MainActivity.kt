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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Removed the manual Factory. Hilt will now handle the injection
    // of ThemePreferences and AssetRepository into the ViewModel.
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen visible until the theme is ready.
        splashScreen.setKeepOnScreenCondition { !viewModel.isThemeReady.value }

        enableEdgeToEdge()

        setContent {
            val seedColorHex by viewModel.themeColorHex.collectAsStateWithLifecycle()
            val isGradientEnabled by viewModel.isGradientEnabled.collectAsStateWithLifecycle()
            val isLightTextEnabled by viewModel.isLightTextEnabled.collectAsStateWithLifecycle()

            // This effect synchronizes the system bar icon colors with the current theme.
            LaunchedEffect(isLightTextEnabled) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isLightTextEnabled
                insetsController.isAppearanceLightNavigationBars = !isLightTextEnabled
            }

            SwaniesPortfolioTheme(
                seedColorHex = seedColorHex,
                isGradientEnabled = isGradientEnabled
            ) {
                val navController = rememberNavController()
                NavGraph(navController = navController, mainViewModel = viewModel)
            }
        }
    }
}