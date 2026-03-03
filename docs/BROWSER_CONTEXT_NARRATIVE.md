📄 BROWSER_CONTEXT_NARRATIVE.md
1. Project Overview

   App Name: Swanie's Portfolio

   Purpose: Crypto & Precious Metals tracking with a high-end, custom-themed UI.

   Current Branch: main (Working Tree Clean - Commit b977b0d)

   Tech Stack: Kotlin, Jetpack Compose, Hilt, Room, Retrofit, StateFlow, DataStore.

2. Architectural Status

   Status: TAB-AWARE TOTALS & PRECISION FORMATTING ENABLED

        Segmented Portfolio Totals: The main header total now dynamically switches based on the active tab (ALL, CRYPTO, or METAL), providing category-specific valuations.

        Isolated Decimal Precision: Implemented a split formatting logic where the Unit Price respects user-defined slider precision (essential for low-satoshi crypto), while Total Value and Portfolio Totals are hard-locked to standard 2-decimal currency ($#,##0.00).

        Integrated Entry & Edit Flow: The 5-step funnel handles all new entries, while the refined FullAssetCard handles edits, both feeding into the unit-aware math engine.

3. Feature Map & UI Status
   🟢 Completed & Locked

   Segmented Header Logic: Switching tabs now instantly updates the top portfolio value to reflect only the assets in view.

   Currency Standardization: Fixed the "Too Many Decimals" bug in the balance displays. Your net worth now always displays as clean, standard currency.

   Edit Overlay Logic: Swapped the field order for better flow—Quantity/Weight (Logical variables) are now on top, while Name/Precision (Descriptive variables) are on the bottom.

   Unit-Aware Math Engine: Fully handles KILO, GRAM, and DWT conversions relative to troy ounce spot prices.

🟡 In Progress / Work-in-Progress

    Edit Screen Layout (Keyboard Obstruction): In the current edit overlay, the software keyboard covers the "Save/Cancel" buttons on smaller screens or when many fields are present.

    Focus Management: Ensuring the Quantity field in the Edit Overlay auto-clears and focuses as reliably as it does in the Add Funnel.

🔴 Upcoming Features

    IME Inset Padding: Implementing Modifier.imePadding() or WindowInsets.ime to ensure the Edit Overlay slides up when the keyboard is active.

    Interactive Donut Deep-Dive: Legend clicks on the Analytics screen triggering the asset detail overlay.

    Premium Logic Toggles: Adding a switch to enter Premium as "Total Amount" vs "Per Ounce."

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. ISOLATED PRECISION FORMATTING
// Price uses slider pref; Total Value uses hard-coded 2 decimals
Text("PRICE: " + formatCurrency(asset.currentPrice, asset.decimalPreference))
Text("TOTAL: " + formatCurrency(calculatedTotal, 2))

// 2. DYNAMIC TAB-BASED TOTALS
val totalValueFormatted by remember(holdings, selectedTab) {
derivedStateOf {
val filtered = when (selectedTab) {
1 -> holdings.filter { it.category == AssetCategory.CRYPTO }
2 -> holdings.filter { it.category == AssetCategory.METAL }
else -> holdings
}
val sum = filtered.sumOf { it.currentPrice * getUnitMultiplier(it) * it.weight * it.amountHeld }
formatCurrency(sum, 2)
}
}

// 3. VISUAL HIERARCHY (EDIT MODE)
// Dimming non-editable elements to highlight the Yellow input fields
val activeTint = if(isEditing) Color.Yellow else cardText
val backgroundAlpha = if(isEditing) 0.3f else 1.0f

🛡️ Narrative Synchronized
The portfolio now behaves like a professional financial tool—precise where it needs to be (unit price) and standard where it counts (account balance).

🎯 Next Steps (Session Kickoff)

    Keyboard Fix: Apply Inset handling to the MyHoldingsScreen edit overlay so the "Save" button is always reachable.

    State Persistence: Verify that the "Weight" field updates correctly when editing existing assets vs. new funnel entries.