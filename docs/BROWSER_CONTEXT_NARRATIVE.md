Markdown

# BROWSER_CONTEXT_NARRATIVE.md

## 1. Project Overview
**App Name:** Swanie's Portfolio  
**Purpose:** Crypto & Precious Metals tracking with a high-end, custom-themed UI.  
**Current Branch:** main (Merged and Locked)  
**Tech Stack:** Kotlin, Jetpack Compose, Hilt (Dependency Injection), Room, Retrofit, StateFlow.

## 2. Architectural Status (The "Hilt" Restoration)
**Status: COMPLETED** Today marked a major milestone: the successful migration from a failing manual ViewModel factory system to a clean, production-ready **Hilt-injected architecture**.

* **ThemeViewModel:** Acts as the single source of truth for UI colors, persisting hex strings and allowing real-time theme updates across all screens without "ghost instances."
* **AssetViewModel:** Fully integrated with Hilt to manage asset data, search logic, and database persistence.
* **Application Setup:** Hilt entry points and the single `PortfolioApplication` class are now correctly configured and stable.

## 3. Feature Map & UI Status

### 🟢 Completed & Locked
* **Hilt Integration:** The entire dependency tree is now stabilized.
* **Home Screen:** Splash-to-App transition preserved; dynamic "Radial Burst" logic implemented.
* **Theme Studio:** Full interactive control over background and text colors with "Reset to Default" logic.
* **Asset Picker:** "Ghost Swan" empty state implemented with keyboard-aware positioning.
* **Git Foundation:** The project is merged into `main` with a "Working Tree Clean" status on GitHub.

### 🟡 In Progress / Work-in-Progress (Current Focus)
* **Holdings Reorder Logic (DRAG & DROP):** * **Status:** Extremely volatile. Compiles and runs, but custom manual drag math is violently fighting Jetpack Compose's internal `LazyColumn` layout engine.
  * **The Problem:** We are experiencing 1-frame latency jitters, "glass ceiling" scroll fighting at the top index, and `animateItem` crossfade flickering. We have mitigated Z-index bleed-through using hardware layers (`shadowElevation`), but the core math is still unstable.
  * **🚨 CRITICAL DIRECTIVE FOR NEXT AI 🚨:** DO NOT ATTEMPT TO WRITE MORE CUSTOM PIXEL-MATH FOR THIS DRAG EVENT. The manual translation math over a `LazyColumn` is a failed architectural approach.
  * **NEXT STEPS:** The next AI must research how top-tier open-source libraries (specifically **`compose-reorderable`** by Calintz) handle this. You must either:
    1. Implement a true "God Listener" `pointerInput(Unit)` that relies strictly on `LazyListState` layout lookups for offset translation rather than manual drag delta accumulators.
    2. Suggest integrating a battle-tested third-party reorder library to handle the state desync and 1-frame latency issues natively.
    3. Suggest pivoting the UX to an "Edit Mode" with simple up/down arrows to completely bypass the GPU physics war.

### 🔴 Upcoming Features
* **Portfolio Analytics:** Donut chart visualization for Metals vs. Crypto breakdown.
* **Trash Zone Refinement:** Fine-tuning collision detection for the floating delete button during asset drags.
* **Price Refresh Polling:** Real-time background updates for asset values.
* **Manual Asset Entry:** UI exists; logic for custom price/weight inputs for non-API assets is pending final validation.

## 4. Key Logic Snippets (The Build-Saver & Render Fixes)
To stop the build-crash loop and fix rendering glitches, we learned that Jetpack Compose requires atomic state updates and strict modifier ordering to prevent UI flickering during rapid list swaps:

```kotlin
// 1. ATOMIC STATE UPDATES: Prevents the 2-step visual flash during a swap
var localHoldings by remember { mutableStateOf<List<AssetEntity>>(emptyList()) }

// 2. DISABLING CROSSFADES: Prevents cards from becoming "see-through" during animateItem swaps
animateItem(
    fadeInSpec = null,
    fadeOutSpec = null,
    // Snap active item to finger, Spring neighbor items out of the way
    placementSpec = if (isDraggingThisItem) snap() else spring(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )
)

// 3. HARDWARE LAYER LOCKING: Prevents bottom cards from Z-index bleeding through the dragged card
.graphicsLayer {
    scaleX = scale; scaleY = scale
    shadowElevation = if (isDraggingThisItem) 30f else 0f // Forces a dedicated GPU layer
}