package com.swanie.portfolio

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.ui.navigation.NavGraph
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen MUST be before super.onCreate
        installSplashScreen().setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            ).apply {
                interpolator = AccelerateInterpolator()
                duration = 500L
            }
            fadeOut.doOnEnd { splashScreenView.remove() }
            fadeOut.start()
        }
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        // Create an instance of our DataStore manager
        val themePreferences = ThemePreferences(this)

        setContent {
            // Collect the theme settings as state
            val themeIndex by themePreferences.themeIndex.collectAsState(initial = 0)
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)

            SwaniesPortfolioTheme(
                themeIndex = themeIndex,
                darkTheme = isDarkMode
            ) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
