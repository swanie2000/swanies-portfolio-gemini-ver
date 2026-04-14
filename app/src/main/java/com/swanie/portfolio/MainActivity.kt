package com.swanie.portfolio

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import com.swanie.portfolio.security.SecurityManager
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.features.AuthViewModel
import com.swanie.portfolio.ui.navigation.NavGraph
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var securityManager: SecurityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val iconDir = File(filesDir, "icons")
        if (!iconDir.exists()) iconDir.mkdirs()

        splashScreen.setKeepOnScreenCondition {
            viewModel.isDataReady.value == false
        }

        enableEdgeToEdge()

        setContent {
            val siteBgColor by viewModel.siteBackgroundColor.collectAsStateWithLifecycle()
            val useGradient by viewModel.useGradient.collectAsStateWithLifecycle()
            val gradientAmount by viewModel.gradientAmount.collectAsStateWithLifecycle()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            
            val allVaults by viewModel.allVaults.collectAsStateWithLifecycle()

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
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        NavGraph(
                            navController = navController,
                            mainViewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockScreen(onUnlockClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF000416)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.swanie_foreground),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onUnlockClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("UNLOCK SOVEREIGN VAULT", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
