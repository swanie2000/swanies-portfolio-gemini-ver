package com.swanie.portfolio.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.swanie.portfolio.ui.navigation.Routes

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = 0.dp, // Remove M3 elevation tint
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onBackground,
            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            selectedTextColor = MaterialTheme.colorScheme.onBackground,
            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            indicatorColor = Color.Transparent // No pill indicator
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Routes.HOME,
            onClick = { navController.navigate(Routes.HOME) },
            alwaysShowLabel = false,
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Holdings") },
            label = { Text("Holdings") },
            selected = currentRoute == Routes.HOLDINGS,
            onClick = { navController.navigate(Routes.HOLDINGS) },
            alwaysShowLabel = true, // Main screen should always have label
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Routes.SETTINGS,
            onClick = { navController.navigate(Routes.SETTINGS) },
            alwaysShowLabel = false,
            colors = itemColors
        )
    }
}
