package com.swanie.portfolio

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.features.AuthViewModel
import com.swanie.portfolio.ui.navigation.NavGraph
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import com.swanie.portfolio.widget.PortfolioWidget
import dagger.hilt.android.AndroidEntryPoint
import android.os.SystemClock
import java.io.File
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var lastBackgroundedAtMs: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val iconDir = File(filesDir, "icons")
        if (!iconDir.exists()) iconDir.mkdirs()

        // 🌊 SPLASH TRANSITION: Keep splash until data is ready
        splashScreen.setKeepOnScreenCondition {
            viewModel.isDataReady.value == false
        }

        enableEdgeToEdge()

        setContent {
            val siteBgColor by viewModel.siteBackgroundColor.collectAsStateWithLifecycle()
            val useGradient by viewModel.useGradient.collectAsStateWithLifecycle()
            val gradientAmount by viewModel.gradientAmount.collectAsStateWithLifecycle()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            // 🌊 NAVY FADE COORDINATION: Ensuring the system bars and background are ready for the entry animation
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

                val hideBottomBarRoutes = listOf(
                    Routes.HOME,
                    Routes.CREATE_ACCOUNT,
                    Routes.UNLOCK_VAULT,
                    Routes.RESTORE_VAULT,
                    Routes.TERMS_CONDITIONS
                )
                val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        NavGraph(
                            navController = navController,
                            mainViewModel = viewModel,
                            startDestination = Routes.HOME
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        viewModel.cancelEmergencyDataReadyFallback()
        lifecycleScope.launch {
            try {
                PortfolioWidget().updateAll(applicationContext)
            } catch (_: Exception) {
                // Best-effort heartbeat when app is backgrounded.
            }
        }
        super.onStop()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Stamp only when user actually leaves the app, avoiding false timeout triggers
        // during system overlays (e.g., biometric prompt transitions).
        lastBackgroundedAtMs = SystemClock.elapsedRealtime()
    }

    override fun onResume() {
        super.onResume()
        val backgroundedAt = lastBackgroundedAtMs
        val isAuthenticated = authViewModel.authState.value is AuthViewModel.AuthState.Authenticated
        val timeoutSeconds = viewModel.loginResumeTimeoutSeconds.value

        if (backgroundedAt != null && isAuthenticated && timeoutSeconds >= 0) {
            val elapsedMs = SystemClock.elapsedRealtime() - backgroundedAt
            val timeoutMs = timeoutSeconds * 1000L
            if (elapsedMs > timeoutMs) {
                authViewModel.setLocked()
            }
        }
        lastBackgroundedAtMs = null
    }
}
