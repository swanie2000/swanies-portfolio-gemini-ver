package com.swanie.portfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.swanie.portfolio.ui.SwaniesPortfolioTheme
import com.swanie.portfolio.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen MUST be before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwaniesPortfolioTheme {
                val navController = rememberNavController()
                // Directly call NavGraph. We will add Scaffold inside the screens that need it.
                NavGraph(navController = navController)
            }
        }
    }
}