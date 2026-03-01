📄 BROWSER_CONTEXT_NARRATIVE.md
1. Project Overview

   App Name: Swanie's Portfolio

   Purpose: Crypto & Precious Metals tracking with a high-end, custom-themed UI.

   Current Branch: main (Working Tree Clean - Commit 02b8d98)

   Tech Stack: Kotlin, Jetpack Compose, Hilt, Room, Retrofit, StateFlow, DataStore.

2. Architectural Status

   Status: OPTIMIZED & MONOLITHIC

        Performance Hardening: Implemented a Navigation Gate (400ms delay) that pauses data-syncing during screen transitions. This ensures navigation animations are buttery smooth (60 FPS) before the high-density list populates.

        State Hoisting: Moved theme color collection and currency formatters to the top level of the screen. This prevents "molasses" lag caused by 20+ asset cards performing redundant ViewModel lookups.

        Refactor Strategy: Settled on a "Master File" approach for MyHoldingsScreen.kt (~450 lines) to ensure the reorderable library and LazyColumn state remain 100% stable.

3. Feature Map & UI Status

🟢 Completed & Locked

    "Crisp" Entry Transition: Holdings now snap into place after the transition animation, eliminating the "ghostly/molasses" fading effect.

    4-Icon Unified Navigation: Persistent, theme-reactive bottom bar (Home, Holdings, Analytics, Settings) with zero vertical shifting.

    Drag-to-Delete (Toggleable): Interactive trash-can zone with collision detection. Protected by a themed confirmation dialog that can be disabled in Settings for Pro users.

    High-Density UI: 10dp "Center-Cut" shrink on asset cards preserved, maximizing vertical real estate while maintaining 2-line name stacking.

🟡 In Progress / Work-in-Progress

    Interactive Analytics: Expanding the Donut Chart logic to allow segment tapping for filtered holdings views.

    Metal Weight Logic: (Upcoming) Transitioning custom assets from "Quantity" to "Weight-Based" calculations (Price×Weight×Amount).

🔴 Upcoming Features

    Compact Card Mode: Finalizing the toggle logic to switch between the 40dp "Center-Cut" cards and ultra-compact list items.

    Asset Deep-Dive: Implementing a detail view for performance metrics (24h High/Low, Volume).

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. THE NAVIGATION GATE: Preventing "Molasses" transitions
var isScreenLoaded by remember { mutableStateOf(false) }
LaunchedEffect(Unit) {
delay(400) // Let the nav animation finish
isScreenLoaded = true
}

// 2. STABILIZED SYNC: No data-overwriting during drags or edits
LaunchedEffect(holdings, isScreenLoaded) {
if (isScreenLoaded && !isDraggingActive.value && assetBeingEdited == null) {
if (localHoldings != holdings) { localHoldings = holdings }
}
}

// 3. PERFORMANCE HOISTING: Derived state for zero-lag calculations
val totalValueFormatted by remember(holdings) {
derivedStateOf {
val total = holdings.sumOf { it.currentPrice * it.amountHeld }
currencyFormatter.format(total)
}
}

🛡️ Narrative Synchronized

The record is straight, Michael. We ended the day with a codebase that is faster, smaller, and strictly verified.