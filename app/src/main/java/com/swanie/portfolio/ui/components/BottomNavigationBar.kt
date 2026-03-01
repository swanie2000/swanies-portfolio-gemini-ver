package com.swanie.portfolio.ui.components

import androidx.compose.foundation.background
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
import com.swanie.portfolio.ui.settings.ThemeViewModel

@Composable
fun BottomNavigationBar(navController: NavController) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val bgColorInt = siteBgColor.ifBlank { "#000416" }.toColorInt()
    val textColorInt = siteTextColor.ifBlank { "#FFFFFF" }.toColorInt()

    // Match MyHoldingsScreen structure exactly to prevent jumping
    Column(modifier = Modifier.background(Color(bgColorInt))) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(bgColorInt))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().background(Color(bgColorInt)),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val baseTextColor = Color(textColorInt)

                IconButton(onClick = { if (currentRoute != Routes.HOME) navController.navigate(Routes.HOME) }) {
                    Icon(Icons.Default.Home, null, tint = if(currentRoute == Routes.HOME) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                }
                IconButton(onClick = { if (currentRoute != Routes.HOLDINGS) navController.navigate(Routes.HOLDINGS) }) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, null, tint = if(currentRoute == Routes.HOLDINGS) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                }
                IconButton(onClick = { if (currentRoute != Routes.ANALYTICS) navController.navigate(Routes.ANALYTICS) }) {
                    Icon(Icons.Default.PieChart, null, tint = if(currentRoute == Routes.ANALYTICS) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                }
                IconButton(onClick = { if (currentRoute != Routes.SETTINGS) navController.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Default.Settings, null, tint = if(currentRoute == Routes.SETTINGS) baseTextColor else baseTextColor.copy(alpha = 0.3f))
                }
            }
        }
        // Identical spacer to the holdings screen
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(Color(bgColorInt))
        )
    }
}