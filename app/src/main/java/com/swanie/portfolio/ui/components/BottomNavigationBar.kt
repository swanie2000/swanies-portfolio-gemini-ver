package com.swanie.portfolio.ui.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", color = Color.White) },
            selected = false,
            onClick = { navController.navigate(Routes.HOME) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Holdings") },
            label = { Text("Holdings", color = Color.White) },
            selected = false,
            onClick = { navController.navigate(Routes.HOLDINGS) }
        )
    }
}