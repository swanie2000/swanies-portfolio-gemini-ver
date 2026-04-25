package com.swanie.portfolio.ui.navigation

object Routes {
    const val PRIMARY_VAULT_ID = 1
    const val HOME = "home"
    const val CREATE_ACCOUNT = "create_account"
    const val UNLOCK_VAULT = "unlock_vault" // 🔒 Cleaned up name
    const val RESTORE_VAULT = "restore_vault" // 🛰️ Added for Sovereign Vault Recovery
    const val HOLDINGS = "my_holdings"
    const val HOLDINGS_WITH_VAULT = "my_holdings/{vaultId}"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val THEME_STUDIO = "theme_studio"
    const val WIDGET_MANAGER = "widget_manager"
    const val ASSET_PICKER = "add_asset/{vaultId}"
    const val AMOUNT_ENTRY = "amount_entry/{coinId}/{symbol}/{apiId}/{iconUrl}/{category}/{price}/{priceSource}/{vaultId}"
    const val MANUAL_ASSET_ENTRY = "manual_asset_entry"
    const val METALS_AUDIT = "metals_audit"
    const val ASSET_ARCHITECT = "asset_architect/{symbol}/{price}/{source}/{vaultId}"
    const val PORTFOLIO_MANAGER = "portfolio_manager"
    const val TERMS_CONDITIONS = "terms_conditions" // ⚖️ New Route for Privacy Pledge

    fun addAssetRoute(vaultId: Int): String = "add_asset/$vaultId"
    fun holdingsRoute(vaultId: Int): String = "my_holdings/$vaultId"
}