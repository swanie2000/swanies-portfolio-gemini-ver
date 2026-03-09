📄 BROWSER_CONTEXT_NARRATIVE.md (Updated 2026-03-09)

1. Project Overview

   App Name: Swanie’s Portfolio

   Current Branch: main (Metals Stability Recovered | UI State Debugging)

2. Architectural Status

   Status: REINFORCED GRID-LOCKED ARCHITECTURE

   The Metals "Safe Harbor": Successfully reverted to a stable state where the Metals Market Watch independently fetches and renders live Yahoo Finance data. The logic is now "Hands-Off" to prevent regression.

   The State Synchronization Challenge: Currently investigating a "Deaf UI" issue where the database updates successfully (verified by navigation), but the Holdings screen remains frozen until a manual interaction (like tapping an edit field) forces a re-composition.

3. Feature Map & UI Status

🟢 Completed & Locked (Today’s Wins)

    Metals Logic Recovery: Yahoo Finance API integration is restored and stable.

    Manual Re-composition Proof: Confirmed that the data is correctly stored in Room; the issue is isolated to the UI "Bridge" failing to trigger a redraw on the second asset addition.

    NavGraph Scoping: Initial work on unified AssetViewModel scoping to ensure the Picker and List share a single source of truth.

🔴 Bug Tracker (Current Priority)

    Reactive Flow Lock: Solve the "Second Asset" bug where new prices only appear after navigating to Analytics/Settings and back, or after interacting with a card.

    UI "Wake-Up" Call: Implement a robust trigger to force the Holdings screen to redraw immediately upon landing or database emission.

📝 The Drawing Board (Post-Sync Phase)

    Pull-to-Refresh: Implement a standard "Swipe Down" manual refresh for the main Holdings list.

    The Leaky Bucket Sync: Develop a "leaky bucket" synchronization strategy to handle high-frequency price updates without overwhelming the UI thread.

    Expanded Search Engines: Integrate additional API endpoints to locate and track niche or "hard-to-find" crypto assets not currently indexed by the primary provider.

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// UI REACTIVITY BRIDGE (Pending Final Stabilization)
// Unconditional update intended to bypass "frozen" local states
LaunchedEffect(holdings) {
Log.d("SWAN_DEBUG", "UI Received ${holdings.size} assets. Forcing Redraw.")
localHoldings = holdings.toList()
}

// METALS API RECOVERY
// Direct-assignment logic that restored the Market Watch functionality
val result = viewModel.fetchMarketPriceData(sym)
if (result.current > 0.0) {
marketDataMap[sym] = result
}