package com.swanie.portfolio.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel

@Composable
fun BottomNavigationBar(navController: NavHostController, modifier: Modifier = Modifier) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    // Safety check for empty color strings
    val safeTextColor = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier.height(44.dp),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = safeTextColor,
            unselectedIconColor = safeTextColor.copy(alpha = 0.4f),
            indicatorColor = Color.Transparent
        )

        val iconModifier = Modifier.size(22.dp)

        // HOME
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Home, null, modifier = iconModifier) },
            selected = currentRoute == Routes.HOME,
            onClick = { if (currentRoute != Routes.HOME) navController.navigate(Routes.HOME) { launchSingleTop = true } },
            colors = itemColors
        )

        // HOLDINGS
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.FormatListBulleted, null, modifier = iconModifier) },
            selected = currentRoute == Routes.HOLDINGS,
            onClick = { if (currentRoute != Routes.HOLDINGS) navController.navigate(Routes.HOLDINGS) { launchSingleTop = true } },
            colors = itemColors
        )

        // ANALYTICS (NEW)
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.PieChart, null, modifier = iconModifier) },
            selected = currentRoute == Routes.ANALYTICS,
            onClick = { if (currentRoute != Routes.ANALYTICS) navController.navigate(Routes.ANALYTICS) { launchSingleTop = true } },
            colors = itemColors
        )

        // SETTINGS
        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Settings, null, modifier = iconModifier) },
            selected = currentRoute == Routes.SETTINGS,
            onClick = { if (currentRoute != Routes.SETTINGS) navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
            colors = itemColors
        )
    }
}