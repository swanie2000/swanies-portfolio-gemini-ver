@file:OptIn(ExperimentalAnimationApi::class)

package com.swanie.portfolio.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.swanie.portfolio.ui.features.AuthViewModel
import com.swanie.portfolio.ui.holdings.*
import com.swanie.portfolio.ui.metals.MetalsAuditScreen // ✅ Fixed: Added missing import
import com.swanie.portfolio.ui.settings.*
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    startDestination: String = Routes.HOME
) {
    val activity = LocalContext.current as androidx.fragment.app.FragmentActivity
    val assetViewModel: AssetViewModel = hiltViewModel()
    val amountEntryViewModel: AmountEntryViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel(activity)
    val scope = rememberCoroutineScope()
    val authState = authViewModel.authState.collectAsStateWithLifecycle().value
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldForceUnlock = authState !is AuthViewModel.AuthState.Authenticated &&
        currentRoute != Routes.UNLOCK_VAULT &&
        currentRoute != Routes.CREATE_ACCOUNT

    Box(modifier = Modifier.fillMaxSize().background(brush = LocalBackgroundBrush.current)) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) },
            popEnterTransition = { fadeIn(animationSpec = tween(400)) },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable(Routes.HOME) {
                HomeScreen(navController, mainViewModel)
            }

            composable(Routes.CREATE_ACCOUNT) {
                CreateAccountScreen(
                    navController = navController,
                    mainViewModel = mainViewModel
                )
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

            composable(Routes.PORTFOLIO_MANAGER) {
                PortfolioManagerScreen(
                    navController = navController,
                    mainViewModel = mainViewModel
                )
            }

            composable(Routes.HOLDINGS) {
                if (shouldForceUnlock) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.UNLOCK_VAULT) {
                            popUpTo(Routes.HOLDINGS) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    MyHoldingsScreen(
                        mainViewModel = mainViewModel,
                        navController = navController
                    )
                }
            }
            composable(
                route = Routes.HOLDINGS_WITH_VAULT,
                arguments = listOf(
                    navArgument("vaultId") {
                        type = NavType.IntType
                        defaultValue = Routes.PRIMARY_VAULT_ID
                    }
                )
            ) { backStackEntry ->
                val targetVaultId = backStackEntry.arguments?.getInt("vaultId") ?: Routes.PRIMARY_VAULT_ID
                if (shouldForceUnlock) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.UNLOCK_VAULT) {
                            popUpTo(Routes.HOLDINGS_WITH_VAULT) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    MyHoldingsScreen(
                        mainViewModel = mainViewModel,
                        navController = navController,
                        requestedVaultId = targetVaultId
                    )
                }
            }

            composable(Routes.ANALYTICS) {
                AnalyticsScreen(navController = navController)
            }

            composable(Routes.METALS_AUDIT) {
                MetalsAuditScreen(navController = navController)
            }

            composable(
                route = Routes.ASSET_PICKER,
                arguments = listOf(
                    navArgument("vaultId") {
                        type = NavType.IntType
                        defaultValue = Routes.PRIMARY_VAULT_ID
                    }
                )
            ) { backStackEntry ->
                val vaultId = backStackEntry.arguments?.getInt("vaultId") ?: Routes.PRIMARY_VAULT_ID
                AssetPickerScreen(
                    navController = navController,
                    onAssetSelected = { asset ->
                        scope.launch {
                            val healedAsset = assetViewModel.healMetadata(asset)
                            val encodedThumb = URLEncoder.encode(healedAsset.iconUrl ?: healedAsset.imageUrl ?: "NONE", "UTF-8")
                            val encodedSource = URLEncoder.encode(healedAsset.priceSource, "UTF-8")
                            navController.navigate("amount_entry/${healedAsset.coinId}/${healedAsset.symbol}/${healedAsset.apiId}/$encodedThumb/${healedAsset.category.name}/${healedAsset.officialSpotPrice}/$encodedSource/$vaultId")
                        }
                    }
                )
            }

            composable(
                route = Routes.AMOUNT_ENTRY,
                arguments = listOf(
                    navArgument("coinId") { type = NavType.StringType },
                    navArgument("symbol") { type = NavType.StringType },
                    navArgument("apiId") { type = NavType.StringType },
                    navArgument("iconUrl") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("price") { type = NavType.StringType },
                    navArgument("priceSource") { type = NavType.StringType },
                    navArgument("vaultId") {
                        type = NavType.IntType
                        defaultValue = Routes.PRIMARY_VAULT_ID
                    }
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
                val vaultId = backStackEntry.arguments?.getInt("vaultId") ?: Routes.PRIMARY_VAULT_ID

                AmountEntryScreen(
                    coinId = coinId,
                    apiId = apiId,
                    symbol = symbol,
                    name = symbol,
                    imageUrl = decodedThumb,
                    category = category,
                    officialSpotPrice = price,
                    priceSource = priceSource,
                    onSave = { targetVaultId ->
                        navController.navigate(Routes.holdingsRoute(targetVaultId)) {
                            popUpTo(Routes.HOLDINGS) { inclusive = false }
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onNavigateToArchitect = { sym: String, p: Double, src: String ->
                        val encodedSrc = URLEncoder.encode(src, "UTF-8")
                        navController.navigate("asset_architect/$sym/$p/$encodedSrc/$vaultId")
                    }
                )
            }

            composable(
                route = Routes.ASSET_ARCHITECT,
                arguments = listOf(
                    navArgument("symbol") { type = NavType.StringType },
                    navArgument("price") { type = NavType.FloatType },
                    navArgument("source") { type = NavType.StringType },
                    navArgument("vaultId") {
                        type = NavType.IntType
                        defaultValue = Routes.PRIMARY_VAULT_ID
                    }
                )
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: "GOLD"
                val price = backStackEntry.arguments?.getFloat("price")?.toDouble() ?: 0.0
                val source = URLDecoder.decode(backStackEntry.arguments?.getString("source") ?: "Manual", "UTF-8")
                val vaultId = backStackEntry.arguments?.getInt("vaultId") ?: Routes.PRIMARY_VAULT_ID

                AssetArchitectScreen(
                    initialSymbol = symbol,
                    initialPrice = price,
                    initialSource = source,
                    onSave = { entity: AssetEntity ->
                        amountEntryViewModel.performSurgicalAdd(entity) {
                            navController.navigate(Routes.holdingsRoute(vaultId)) {
                                popUpTo(Routes.HOLDINGS) { inclusive = false }
                            }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}