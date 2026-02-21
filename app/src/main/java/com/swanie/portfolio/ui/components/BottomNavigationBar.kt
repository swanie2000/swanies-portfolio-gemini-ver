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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.swanie.portfolio.ui.navigation.Routes

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedIconColor = MaterialTheme.colorScheme.onSurface,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
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
            alwaysShowLabel = false,
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
