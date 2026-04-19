package com.swanie.portfolio.ui.navigation

object Routes {
    const val HOME = "home"
    const val CREATE_ACCOUNT = "create_account"
    const val UNLOCK_VAULT = "unlock_vault" // 🔒 Cleaned up name
    const val RESTORE_VAULT = "restore_vault" // 🛰️ Added for Sovereign Vault Recovery
    const val HOLDINGS = "my_holdings"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val THEME_STUDIO = "theme_studio"
    const val WIDGET_MANAGER = "widget_manager"
    const val ASSET_PICKER = "asset_picker"
    const val AMOUNT_ENTRY = "amount_entry/{coinId}/{symbol}/{name}/{imageUrl}/{category}/{price}"
    const val MANUAL_ASSET_ENTRY = "manual_asset_entry"
    const val METALS_AUDIT = "metals_audit"
    const val ASSET_ARCHITECT = "asset_architect/{symbol}/{price}/{source}"
    const val PORTFOLIO_MANAGER = "portfolio_manager"
    const val TERMS_CONDITIONS = "terms_conditions" // ⚖️ New Route for Privacy Pledge
}