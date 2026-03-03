📄 BROWSER_CONTEXT_NARRATIVE.md
1. Project Overview

   App Name: Swanie's Portfolio

   Purpose: Crypto & Precious Metals tracking with a high-end, custom-themed UI.

   Current Branch: main (Working Tree Clean - Commit 49cdc75)

   Tech Stack: Kotlin, Jetpack Compose, Hilt, Room, Retrofit, StateFlow, DataStore.

2. Architectural Status

   Status: MULTI-LEVEL CALCULATIONS ENABLED

        The "Heavy" Math Engine: Upgraded the Room database to Version 8. Assets now support a weight and premium field. The portfolio logic now calculates value as: ((SpotPrice×Weight)+Premium)×Quantity.

        State-Lock Reordering: Restored reorder stability by stripping out animateContentSize which caused UI "jitter." Implemented a cross-tab mapping logic that allows dragging on CRYPTO and METAL tabs to update the master list order correctly.

        UI Component Consolidation: Centralized shared components (FullAssetCard, MetalIcon, SparklineChart) within MyHoldingsScreen.kt to prevent "Double Vision" compiler errors across the package.

3. Feature Map & UI Status

🟢 Completed & Locked

    Tactile Grab Feedback: Restored the 1.05f scale-up and shadow elevation when long-pressing cards. The app feels "physical" again.

    The Intelligence Suite: Full-screen "Scan Flash" and the logo-centered "Charging Bar" are fully restored and linked to the manual refresh trigger.

    Universal Tab Reordering: Drag-to-sort and Drag-to-delete now function seamlessly across all three view filters (ALL, CRYPTO, METAL).

    Analytics Legend Update: The Holdings Key now features rectangular color-coded pills under each asset name, providing an immediate visual link to the chart slices.

🟡 In Progress / Work-in-Progress

    Ghost Card Entry (Manual Add): Implemented a "What You See Is What You Get" entry card. Fields use a pulsing "Breathing Glow" to invite interaction.

        Known Bug: Manual entry flow needs a final pass to ensure all fields persist correctly to the DB on the first click.

    Dynamic Identity Mapping: Manual assets now intelligently assign symbols (GOLD, SILV, PLAT) based on the selected metal type instead of a hardcoded "CUST" tag.

🔴 Upcoming Features

    Premium Logic Toggles: Adding a switch to enter Premium as "Total Amount" vs "Per Ounce."

    Interactive Donut Deep-Dive: Allowing legend clicks to trigger the "Full Asset Card" overlay directly from the Analytics screen.

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. THE WEIGHT-BASED ENGINE (Room v8)
// Portfolio Value = ((Price * Weight) + TotalPremium) * Quantity
val totalVal = asset.currentPrice * (asset.weight * asset.amountHeld)

// 2. UNIVERSAL REORDER MAPPING (Filtered Tab Support)
// Finds the actual database index even when viewing a filtered list
val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
val fromItem = filteredList[from.index]
val toItem = filteredList[to.index]
val newList = localHoldings.toMutableList()
val fromIdxMaster = newList.indexOfFirst { it.coinId == fromItem.coinId }
val toIdxMaster = newList.indexOfFirst { it.coinId == toItem.coinId }

    if (fromIdxMaster != -1 && toIdxMaster != -1) {
        newList.add(toIdxMaster, newList.removeAt(fromIdxMaster))
        localHoldings = newList
    }
}

// 3. AUTO-CLEAR FOCUS LOGIC
// Wipes sample text (1.0, 0.0) the moment the user taps the field
modifier = Modifier.onFocusChanged {
if (it.isFocused && (quantity == "1" || quantity == "0")) quantity = ""
}

🛡️ Narrative Synchronized
The record is straight, Michael. We ended the day with a codebase that is mathematically more powerful and visually more responsive.
🎯 Next Steps

I've noted the Manual Add Asset bugs for our next session. We’ll finish polishing those "Breathing" fields and ensure the save logic is 100% bulletproof.

Would you like me to generate a checklist of those specific manual entry bugs so we don't miss anything next time?


Here is the targeted punch-list for our next session to ensure the "Ghost Card" manual entry is as bulletproof as the rest of the app:
📝 Manual Entry Bug Checklist

    Field Persistence Check: Verify that the "Premium per oz" and "Unit Weight" values are correctly saved to the Room database on the initial "Add" click.

    Keyboard "ImeAction" Flow: Ensure that hitting "Next" on the soft keyboard moves the cursor logically through the card fields (Quantity → Line 1 → Line 2 → Weight → Premium) and "Done" hides the keyboard.

    Symbol Refinement: Confirm that the first-four-letters logic for "Custom" metals looks clean in the compact view (e.g., ensuring "BRONZE" becomes "BRON" without awkward clipping).

    Zero-State Validation: Prevent saving the asset if Line 1 is empty to avoid "Ghost Assets" in the main list.

    Focus Reset: Ensure that when the "Add" operation is cancelled or finished, all internal state variables (line1, line2, etc.) are wiped clean for the next entry.