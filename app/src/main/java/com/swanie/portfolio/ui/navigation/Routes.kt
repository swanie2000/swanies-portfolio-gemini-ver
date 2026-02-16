package com.swanie.portfolio.ui.navigation

object Routes {
    const val ASSET_PICKER = "asset_picker"
    const val AMOUNT_ENTRY = "amount_entry/{assetId}"
    const val HOLDINGS = "holdings"

    // Helper function to build the path with an actual ID
    fun amountEntryWithId(assetId: String) = "amount_entry/$assetId"
}