package com.swanie.portfolio.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.onboarding.HoldingsWalkthroughController
import com.swanie.portfolio.ui.onboarding.HoldingsWalkthroughStep
import com.swanie.portfolio.ui.onboarding.WalkthroughAnchor
import com.swanie.portfolio.ui.onboarding.walkthroughAnchor
import com.swanie.portfolio.ui.settings.ThemeViewModel

@Composable
fun BottomNavigationBar(
    navController: NavController,
    walkthroughController: HoldingsWalkthroughController? = null,
    onNavigate: () -> Unit = {}
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val tourActive = walkthroughController?.isActive() == true
    val tourStep = walkthroughController?.step?.collectAsState()?.value
        ?: HoldingsWalkthroughStep.INACTIVE
    val allowSettingsNav = tourStep == HoldingsWalkthroughStep.OPEN_SETTINGS

    val textColorInt = siteTextColor.ifBlank { "#FFFFFF" }.toColorInt()

    // Match MyHoldingsScreen structure exactly to prevent jumping
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val isHoldingsRoute = currentRoute == Routes.HOLDINGS || currentRoute == Routes.HOLDINGS_WITH_VAULT
                val baseTextColor = Color(textColorInt)

                IconButton(
                    onClick = {
                        if (tourActive) return@IconButton
                        if (currentRoute != Routes.HOME) {
                            onNavigate()
                            navController.navigate(Routes.HOME)
                        }
                    },
                    enabled = !tourActive,
                ) {
                    Icon(Icons.Default.Home, null, tint = if(currentRoute == Routes.HOME) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                }
                IconButton(
                    onClick = {
                        if (tourActive) return@IconButton
                        if (!isHoldingsRoute) {
                            onNavigate()
                            navController.navigate(Routes.HOLDINGS)
                        }
                    },
                    enabled = !tourActive,
                ) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null, tint = if(isHoldingsRoute) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                }
                IconButton(
                    onClick = {
                        if (tourActive) return@IconButton
                        if (currentRoute != Routes.ANALYTICS) {
                            onNavigate()
                            navController.navigate(Routes.ANALYTICS)
                        }
                    },
                    enabled = !tourActive,
                ) {
                    Icon(Icons.Default.PieChart, null, tint = if(currentRoute == Routes.ANALYTICS) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                }
                IconButton(
                    onClick = {
                        if (tourActive && !allowSettingsNav) return@IconButton
                        if (currentRoute != Routes.SETTINGS) {
                            onNavigate()
                            navController.navigate(Routes.SETTINGS)
                        }
                    },
                    enabled = !tourActive || allowSettingsNav,
                    modifier = if (walkthroughController != null) {
                        Modifier.walkthroughAnchor(
                            anchor = WalkthroughAnchor.SETTINGS_TAB,
                            controller = walkthroughController,
                        )
                    } else {
                        Modifier
                    },
                ) {
                    Icon(Icons.Default.Settings, null, tint = if(currentRoute == Routes.SETTINGS) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                }
            }
        }
        // Identical spacer to the holdings screen
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
        )
    }
}