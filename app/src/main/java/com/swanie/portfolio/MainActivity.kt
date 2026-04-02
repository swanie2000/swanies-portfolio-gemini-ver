package com.swanie.portfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.navigation.NavGraph
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 🌐 GLOBAL VISTA: Initialize Icon Vault directory
        val iconDir = File(filesDir, "icons")
        if (!iconDir.exists()) {
            iconDir.mkdirs()
        }

        splashScreen.setKeepOnScreenCondition {
            viewModel.isDataReady.value == false
        }

        enableEdgeToEdge()

        setContent {
            val siteBgColor by viewModel.siteBackgroundColor.collectAsStateWithLifecycle()
            val useGradient by viewModel.useGradient.collectAsStateWithLifecycle()
            val gradientAmount by viewModel.gradientAmount.collectAsStateWithLifecycle()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            LaunchedEffect(isDarkMode) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkMode
                insetsController.isAppearanceLightNavigationBars = !isDarkMode
            }

            SwaniesPortfolioTheme(
                seedColorHex = siteBgColor,
                isGradientEnabled = useGradient,
                gradientAmount = gradientAmount
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 🛡️ SECURITY FIX: Do not show BottomBar on Auth Screens
                val hideBottomBarRoutes = listOf(
                    Routes.HOME,
                    Routes.CREATE_ACCOUNT,
                    Routes.UNLOCK_VAULT,
                    Routes.TERMS_CONDITIONS
                )
                val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

                Scaffold(
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        mainViewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
