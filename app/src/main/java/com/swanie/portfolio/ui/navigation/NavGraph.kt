package com.swanie.portfolio.ui.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.ui.features.CreateAccountScreen
import com.swanie.portfolio.ui.features.HomeScreen
import com.swanie.portfolio.ui.holdings.AmountEntryScreen
import com.swanie.portfolio.ui.holdings.AnalyticsScreen
import com.swanie.portfolio.ui.holdings.AssetPickerScreen
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.MyHoldingsScreen
import com.swanie.portfolio.ui.metals.MetalsAuditScreen
import com.swanie.portfolio.ui.settings.SettingsScreen
import com.swanie.portfolio.ui.settings.SettingsViewModel
import com.swanie.portfolio.ui.settings.ThemeStudioScreen
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavGraph(navController: NavHostController, mainViewModel: MainViewModel) {
    val assetViewModel: AssetViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

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
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    navController = navController,
                    settingsViewModel = settingsViewModel
                )
            }

            composable(Routes.THEME_STUDIO) {
                ThemeStudioScreen(navController)
            }

            composable(Routes.HOLDINGS) {
                MyHoldingsScreen(
                    mainViewModel = mainViewModel,
                    navController = navController
                )
            }

            composable(Routes.ANALYTICS) {
                AnalyticsScreen(navController = navController)
            }

            composable(Routes.METALS_AUDIT) {
                MetalsAuditScreen(navController = navController)
            }

            composable(Routes.ASSET_PICKER) {
                AssetPickerScreen(
                    navController = navController,
                    onAssetSelected = { asset ->
                        scope.launch {
                            // METADATA HEALING: Heal via CoinGecko but keep the unique provider coinId
                            val healedAsset = assetViewModel.healMetadata(asset)
                            
                            if (healedAsset.category == AssetCategory.METAL) {
                                Log.d("ADD_TRACE", "STEP 2: NAV_PASSING (METAL): ID=${healedAsset.coinId}")
                                assetViewModel.performSurgicalAdd(healedAsset) {
                                    navController.popBackStack()
                                }
                            } else {
                                val encodedThumb = URLEncoder.encode(healedAsset.iconUrl ?: healedAsset.imageUrl ?: "", "UTF-8")
                                val encodedSource = URLEncoder.encode(healedAsset.priceSource, "UTF-8")
                                
                                Log.d("ADD_TRACE", "STEP 2: NAV_PASSING (CRYPTO): coinId=${healedAsset.coinId}, apiId=${healedAsset.apiId}")
                                
                                // RESTORED UNIQUENESS: Passing both coinId AND apiId
                                navController.navigate("amount_entry/${healedAsset.coinId}/${healedAsset.symbol}/${healedAsset.apiId}/$encodedThumb/${healedAsset.category.name}/${healedAsset.officialSpotPrice}/$encodedSource")
                            }
                        }
                    }
                )
            }

            composable(
                route = "amount_entry/{coinId}/{symbol}/{apiId}/{iconUrl}/{category}/{price}/{priceSource}",
                arguments = listOf(
                    navArgument("coinId") { type = NavType.StringType },
                    navArgument("symbol") { type = NavType.StringType },
                    navArgument("apiId") { type = NavType.StringType },
                    navArgument("iconUrl") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("price") { type = NavType.StringType },
                    navArgument("priceSource") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val coinId = backStackEntry.arguments?.getString("coinId") ?: ""
                val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                val apiId = backStackEntry.arguments?.getString("apiId") ?: ""
                val iconUrl = backStackEntry.arguments?.getString("iconUrl") ?: ""
                val priceSource = backStackEntry.arguments?.getString("priceSource") ?: "CoinGecko"
                val decodedThumb = URLDecoder.decode(iconUrl, "UTF-8")

                val categoryString = backStackEntry.arguments?.getString("category") ?: "CRYPTO"
                val category = AssetCategory.valueOf(categoryString)
                val price = backStackEntry.arguments?.getString("price")?.toDoubleOrNull() ?: 0.0

                AmountEntryScreen(
                    coinId = coinId,
                    apiId = apiId, // Aligned with unique PK vs fetch ID
                    symbol = symbol,
                    name = symbol,
                    imageUrl = decodedThumb,
                    category = category,
                    officialSpotPrice = price,
                    priceSource = priceSource,
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