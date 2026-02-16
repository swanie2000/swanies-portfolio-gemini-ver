package com.swanie.portfolio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swanie.portfolio.ui.holdings.AmountEntryScreen
import com.swanie.portfolio.ui.holdings.AssetPickerScreen
import com.swanie.portfolio.ui.holdings.MyHoldingsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PortfolioNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOLDINGS,
        modifier = modifier
    ) {

        composable(Routes.HOLDINGS) {
            MyHoldingsScreen(
                onAddNewAsset = { navController.navigate(Routes.ASSET_PICKER) }
            )
        }

        composable(Routes.ASSET_PICKER) {
            AssetPickerScreen(
                onAssetSelected = { coinId, symbol, name, imageUrl ->
                    // Encode the URL to handle special characters safely
                    val encodedUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString())
                    navController.navigate("${Routes.AMOUNT_ENTRY}/$coinId/$symbol/$name/$encodedUrl")
                }
            )
        }

        composable(
            route = "${Routes.AMOUNT_ENTRY}/{coinId}/{symbol}/{name}/{imageUrl}",
            arguments = listOf(
                navArgument("coinId") { type = NavType.StringType },
                navArgument("symbol") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("imageUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId") ?: ""
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            val decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8.toString())

            AmountEntryScreen(
                coinId = coinId,
                symbol = symbol,
                name = name,
                imageUrl = decodedUrl,
                onSave = {
                    navController.navigate(Routes.HOLDINGS) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                // THE FIX: New cancel handler
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
