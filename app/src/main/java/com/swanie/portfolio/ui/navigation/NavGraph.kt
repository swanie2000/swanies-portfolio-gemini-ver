@file:OptIn(ExperimentalAnimationApi::class)

package com.swanie.portfolio.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.entry.AssetArchitectScreen
import com.swanie.portfolio.ui.features.CreateAccountScreen
import com.swanie.portfolio.ui.features.HomeScreen
import com.swanie.portfolio.ui.features.RestoreVaultScreen
import com.swanie.portfolio.ui.features.UnlockVaultScreen
import com.swanie.portfolio.ui.features.TermsAndConditionsScreen
import com.swanie.portfolio.ui.holdings.*
import com.swanie.portfolio.ui.metals.MetalsAuditScreen // ✅ Fixed: Added missing import
import com.swanie.portfolio.ui.settings.*
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavGraph(navController: NavHostController, mainViewModel: MainViewModel) {
    val assetViewModel: AssetViewModel = hiltViewModel()
    val amountEntryViewModel: AmountEntryViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(brush = LocalBackgroundBrush.current)) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) },
            popEnterTransition = { fadeIn(animationSpec = tween(400)) },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable(Routes.HOME) {
                HomeScreen(navController, mainViewModel)
            }

            composable(Routes.CREATE_ACCOUNT) {
                CreateAccountScreen(navController = navController)
            }

            composable(Routes.UNLOCK_VAULT) {
                UnlockVaultScreen(navController = navController)
            }

            composable(Routes.RESTORE_VAULT) {
                RestoreVaultScreen(navController = navController)
            }

            composable(Routes.TERMS_CONDITIONS) {
                TermsAndConditionsScreen(navController = navController)
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

            composable(Routes.WIDGET_MANAGER) {
                WidgetManagerScreen(navController)
            }

            composable(Routes.PORTFOLIO_MANAGER) {
                PortfolioManagerScreen(
                    navController = navController,
                    mainViewModel = mainViewModel
                )
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
                            val healedAsset = assetViewModel.healMetadata(asset)
                            val encodedThumb = URLEncoder.encode(healedAsset.iconUrl ?: healedAsset.imageUrl ?: "NONE", "UTF-8")
                            val encodedSource = URLEncoder.encode(healedAsset.priceSource, "UTF-8")
                            navController.navigate("amount_entry/${healedAsset.coinId}/${healedAsset.symbol}/${healedAsset.apiId}/$encodedThumb/${healedAsset.category.name}/${healedAsset.officialSpotPrice}/$encodedSource")
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
                val iconUrl = backStackEntry.arguments?.getString("iconUrl") ?: "NONE"
                val priceSource = backStackEntry.arguments?.getString("priceSource") ?: "CoinGecko"
                val decodedThumb = if (iconUrl == "NONE") "" else URLDecoder.decode(iconUrl, "UTF-8")

                val categoryString = backStackEntry.arguments?.getString("category") ?: "CRYPTO"
                val category = AssetCategory.valueOf(categoryString)
                val price = backStackEntry.arguments?.getString("price")?.toDoubleOrNull() ?: 0.0

                AmountEntryScreen(
                    coinId = coinId,
                    apiId = apiId,
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
                    onCancel = { navController.popBackStack() },
                    onNavigateToArchitect = { sym: String, p: Double, src: String ->
                        val encodedSrc = URLEncoder.encode(src, "UTF-8")
                        navController.navigate("asset_architect/$sym/$p/$encodedSrc")
                    }
                )
            }

            composable(
                route = Routes.ASSET_ARCHITECT,
                arguments = listOf(
                    navArgument("symbol") { type = NavType.StringType },
                    navArgument("price") { type = NavType.FloatType },
                    navArgument("source") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: "GOLD"
                val price = backStackEntry.arguments?.getFloat("price")?.toDouble() ?: 0.0
                val source = URLDecoder.decode(backStackEntry.arguments?.getString("source") ?: "Manual", "UTF-8")

                AssetArchitectScreen(
                    initialSymbol = symbol,
                    initialPrice = price,
                    initialSource = source,
                    onSave = { entity: AssetEntity ->
                        amountEntryViewModel.performSurgicalAdd(entity) {
                            navController.navigate(Routes.HOLDINGS) {
                                popUpTo(Routes.HOLDINGS) { inclusive = true }
                            }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}