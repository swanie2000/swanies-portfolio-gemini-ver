package com.swanie.portfolio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swanie.portfolio.ui.holdings.AssetPickerScreen
import com.swanie.portfolio.ui.holdings.AmountEntryScreen

@Composable
fun PortfolioNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.ASSET_PICKER,
        modifier = modifier
    ) {
        // 1. Asset Picker Screen
        composable(Routes.ASSET_PICKER) {
            AssetPickerScreen(
                onAssetSelected = { assetInfo ->
                    // assetInfo is expected to be a string like "coinId|symbol|name"
                    navController.navigate("${Routes.AMOUNT_ENTRY}/$assetInfo")
                }
            )
        }

        // 2. Amount Entry Screen
        composable("${Routes.AMOUNT_ENTRY}/{assetInfo}") { backStackEntry ->
            val assetInfo = backStackEntry.arguments?.getString("assetInfo") ?: "||"
            val (coinId, symbol, name) = assetInfo.split('|')
            AmountEntryScreen(
                coinId = coinId,
                symbol = symbol,
                name = name,
                onSave = {
                    navController.navigate(Routes.HOLDINGS)
                }
            )
        }

        // 3. Holdings Dashboard Placeholder
        composable(Routes.HOLDINGS) {
            // Future home of MyHoldingsScreen
        }
    }
}