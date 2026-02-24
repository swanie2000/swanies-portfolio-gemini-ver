package com.swanie.portfolio.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.features.CreateAccountScreen
import com.swanie.portfolio.ui.features.HomeScreen
import com.swanie.portfolio.ui.holdings.AmountEntryScreen
import com.swanie.portfolio.ui.holdings.AssetPickerScreen
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.ManualAssetEntryScreen
import com.swanie.portfolio.ui.holdings.MyHoldingsScreen
import com.swanie.portfolio.ui.settings.SettingsScreen
import com.swanie.portfolio.ui.settings.ThemeStudioScreen
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavGraph(navController: NavHostController, mainViewModel: MainViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(brush = LocalBackgroundBrush.current)) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
        ) {
            composable(Routes.HOME) {
                HomeScreen(navController, mainViewModel)
            }

            composable(Routes.CREATE_ACCOUNT) {
                CreateAccountScreen(navController, mainViewModel)
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(navController)
            }

            composable(Routes.THEME_STUDIO) {
                ThemeStudioScreen(navController)
            }

            composable(Routes.HOLDINGS) {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) { innerPadding ->
                    MyHoldingsScreen(
                        mainViewModel = mainViewModel,
                        onAddNewAsset = { navController.navigate(Routes.ASSET_PICKER) },
                        onAddCustomAsset = { navController.navigate(Routes.MANUAL_ASSET_ENTRY) },
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            composable(Routes.ASSET_PICKER) {
                AssetPickerScreen(onAssetSelected = { coinId, symbol, name, imageUrl, category, price ->
                    val encodedUrl = URLEncoder.encode(imageUrl, "UTF-8")
                    navController.navigate("amount_entry/$coinId/$symbol/$name/$encodedUrl/${category.name}/$price")
                })
            }

            composable(Routes.MANUAL_ASSET_ENTRY) {
                val viewModel: AssetViewModel = hiltViewModel()

                ManualAssetEntryScreen(
                    onSave = {
                        viewModel.saveNewAsset(it, it.amountHeld) { // amountHeld is now part of AssetEntity
                            navController.navigate(Routes.HOLDINGS) {
                                popUpTo(Routes.HOLDINGS) { inclusive = true }
                            }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.AMOUNT_ENTRY,
                arguments = listOf(
                    navArgument("coinId") { type = NavType.StringType },
                    navArgument("symbol") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType },
                    navArgument("imageUrl") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("price") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val coinId = backStackEntry.arguments?.getString("coinId") ?: ""
                val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                val decodedUrl = URLDecoder.decode(imageUrl, "UTF-8")

                val categoryString = backStackEntry.arguments?.getString("category") ?: "CRYPTO"
                val category = AssetCategory.valueOf(categoryString)
                val price = backStackEntry.arguments?.getString("price")?.toDoubleOrNull() ?: 0.0

                AmountEntryScreen(
                    coinId = coinId,
                    symbol = symbol,
                    name = name,
                    imageUrl = decodedUrl,
                    category = category,
                    currentPrice = price,
                    onSave = {
                        navController.navigate(Routes.HOLDINGS) {
                            popUpTo(Routes.HOLDINGS) { inclusive = true }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}