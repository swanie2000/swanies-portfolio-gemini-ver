package com.swanie.portfolio.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.features.HomeScreen
import com.swanie.portfolio.ui.holdings.AssetPickerScreen
import com.swanie.portfolio.ui.holdings.MyHoldingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(Routes.HOLDINGS) {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController = navController) }
            ) { innerPadding ->
                MyHoldingsScreen(
                    onAddNewAsset = { navController.navigate(Routes.ASSET_PICKER) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable(Routes.ASSET_PICKER) {
            AssetPickerScreen(onAssetSelected = { coinId, symbol, name, imageUrl ->
                navController.popBackStack()
            })
        }
    }
}