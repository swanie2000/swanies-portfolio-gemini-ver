# BROWSER_CONTEXT_NARRATIVE.md

## 1. Project Overview
**App Name:** Swanie's Portfolio  
**Purpose:** Crypto & Precious Metals tracking with a high-end, custom-themed UI.  
**Current Branch:** main (Working Tree Clean)  
**Tech Stack:** Kotlin, Jetpack Compose, Hilt (Dependency Injection), Room, Retrofit, StateFlow.

## 2. Architectural Status
**Status: STABLE** * **Hilt Integration:** Completed. ViewModels are properly injected via Hilt, ensuring single-instance integrity for Theme and Asset data.
* **Navigation:** Stabilized with consistent icon shading reactive to the dynamic theme.

## 3. Feature Map & UI Status

### 🟢 Completed & Locked
* **Holdings Reorder Logic (STABILIZED):** Abandoned custom manual translation math. Implemented **`sh.calvin.reorderable`** for frame-perfect, GPU-accelerated dragging.
  * **Safety:** Reordering is strictly restricted to the "ALL" tab to prevent index corruption.
  * **UX:** Integrated hardware layer locking (`shadowElevation`) and trash-zone collision detection.
* **Theme Studio:** Full interactive control over background and text colors with "Reset to Default" logic.
* **Asset Picker:** "Ghost Swan" empty state implemented with keyboard-aware positioning.
* **Git Foundation:** Merged into `main`, GitHub repository is up to date.

### 🟡 In Progress / Work-in-Progress (Current Focus)
* **Portfolio Analytics (DONUT CHART):**
  * **Goal:** Create a custom Canvas-based visualization of the asset breakdown (Metals vs. Crypto).
  * **Logic:** Aggregating $ value based on `currentPrice * weight * amountHeld`.
  * **Risk:** Ensuring the UI doesn't stutter during data refreshes; aggregation must stay in the ViewModel.

### 🔴 Upcoming Features
* **Price Refresh Polling:** Real-time background updates for asset values (refining the current manual refresh).
* **Manual Asset Entry:** Logic for custom price/weight inputs for non-API assets (e.g., physical bullion with specific premiums).

## 4. Key Logic Snippets (The Build-Savers)

```kotlin
// 1. REORDERABLE IMPLEMENTATION: Using specialized library for stability
ReorderableItem(reorderableLazyListState, key = asset.coinId) { isDragging ->
    Box(
        modifier = Modifier
            .longPressDraggableHandle(
                onDragStarted = { /* state updates */ },
                onDragStopped = { /* persistence + delete checks */ }
            )
            .graphicsLayer {
                shadowElevation = if (isDragging) 30f else 0f // Hardware Layer Lock
            }
    )
}

// 2. POINTER SPY: Efficient trash-zone detection during active reorder
.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            // check bounds in root coordinates
        }
    }
}