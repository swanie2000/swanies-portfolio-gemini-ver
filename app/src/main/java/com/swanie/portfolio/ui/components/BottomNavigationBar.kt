package com.swanie.portfolio.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.swanie.portfolio.ui.navigation.Routes

@Composable
fun BottomNavigationBar(navController: NavHostController, modifier: Modifier = Modifier) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        // Increasing height slightly to 48.dp (Standard) to prevent pixel-smearing,
        // OR keep at 40.dp but we must force the icon size.
        modifier = modifier.height(44.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onBackground,
            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            indicatorColor = Color.Transparent
        )

        // Define a consistent icon modifier to force pixel alignment
        val iconModifier = Modifier.size(22.dp)

        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Home, null, modifier = iconModifier) },
            selected = currentRoute == Routes.HOME,
            onClick = { if (currentRoute != Routes.HOME) navController.navigate(Routes.HOME) { launchSingleTop = true } },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Rounded.FormatListBulleted, null, modifier = iconModifier) },
            selected = currentRoute == Routes.HOLDINGS,
            onClick = { if (currentRoute != Routes.HOLDINGS) navController.navigate(Routes.HOLDINGS) { launchSingleTop = true } },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Rounded.Settings, null, modifier = iconModifier) },
            selected = currentRoute == Routes.SETTINGS,
            onClick = { if (currentRoute != Routes.SETTINGS) navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
            colors = itemColors
        )
    }
}