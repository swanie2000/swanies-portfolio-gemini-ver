package com.swanie.portfolio

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.swanie.portfolio.ui.SwaniesPortfolioTheme
import com.swanie.portfolio.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen MUST be before super.onCreate
        installSplashScreen().setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view, // The view to animate
                View.ALPHA, // The property to animate
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