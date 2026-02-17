package com.swanie.portfolio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swanie.portfolio.ui.features.HomeScreen
import com.swanie.portfolio.ui.features.SettingsScreen
import com.swanie.portfolio.ui.holdings.MyHoldingsScreen
import com.swanie.portfolio.ui.holdings.AssetPickerScreen
import com.swanie.portfolio.ui.holdings.AmountEntryScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(Routes.HOLDINGS) {
            MyHoldingsScreen(onAddNewAsset = { navController.navigate(Routes.ASSET_PICKER) })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }

        composable(Routes.ASSET_PICKER) {
            AssetPickerScreen(onAssetSelected = { id, sym, name, url ->
                val encodedUrl = java.net.URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                navController.navigate("amount_entry/$id/$sym/$name/$encodedUrl")
            })
        }

        composable(
            route = Routes.AMOUNT_ENTRY,
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
                        popUpTo(Routes.HOLDINGS) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
    }
}