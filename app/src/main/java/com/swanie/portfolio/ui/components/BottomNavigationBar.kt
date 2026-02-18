package com.swanie.portfolio.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.swanie.portfolio.ui.navigation.Routes

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        containerColor = Color(0xFF000416),
        contentColor = Color.White
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            unselectedIconColor = Color.White,
            selectedIconColor = Color.White
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("") },
            selected = false,
            onClick = { navController.navigate(Routes.HOME) },
            alwaysShowLabel = false,
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Holdings") },
            label = { Text("") },
            selected = false,
            onClick = { navController.navigate(Routes.HOLDINGS) },
            alwaysShowLabel = false,
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("") },
            selected = false,
            onClick = { /* TODO */ },
            alwaysShowLabel = false,
            colors = itemColors
        )
    }
}