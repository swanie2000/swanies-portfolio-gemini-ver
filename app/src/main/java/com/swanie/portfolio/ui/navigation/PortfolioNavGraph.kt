package com.swanie.portfolio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swanie.portfolio.ui.holdings.AmountEntryScreen
import com.swanie.portfolio.ui.holdings.AssetPickerScreen
import com.swanie.portfolio.ui.holdings.MyHoldingsScreen

@Composable
fun PortfolioNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        // Start at the new Holdings screen for a more natural user flow
        startDestination = Routes.HOLDINGS,
        modifier = modifier
    ) {

        // 1. Holdings Dashboard (New Start Destination)
        composable(Routes.HOLDINGS) {
            MyHoldingsScreen(
                onAddNewAsset = { navController.navigate(Routes.ASSET_PICKER) }
            )
        }

        // 2. Asset Picker Screen
        composable(Routes.ASSET_PICKER) {
            AssetPickerScreen(
                onAssetSelected = { assetInfo ->
                    navController.navigate("${Routes.AMOUNT_ENTRY}/$assetInfo")
                }
            )
        }

        // 3. Amount Entry Screen
        composable("${Routes.AMOUNT_ENTRY}/{assetInfo}") { backStackEntry ->
            val assetInfo = backStackEntry.arguments?.getString("assetInfo") ?: "||"
            val (coinId, symbol, name) = assetInfo.split('|')
            AmountEntryScreen(
                coinId = coinId,
                symbol = symbol,
                name = name,
                onSave = {
                    // Navigate to holdings and clear the backstack up to the graph's start destination
                    navController.navigate(Routes.HOLDINGS) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}