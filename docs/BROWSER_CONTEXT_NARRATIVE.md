BROWSER_CONTEXT_NARRATIVE.md
1. Project Overview

App Name: Swanie's Portfolio

Purpose: Crypto & Precious Metals tracking with a high-end, custom-themed UI.

Current Branch: main (Working Tree Clean - Commit ff0b684)

Tech Stack: Kotlin, Jetpack Compose, Hilt (Dependency Injection), Room, Retrofit, StateFlow.
2. Architectural Status

Status: STABLE & BRANDED

    Hilt Integration: Completed. ViewModels are properly injected via Hilt, ensuring single-instance integrity for Theme and Asset data.

    Theme Hardening: DataStore fallbacks are hardcoded to Swanie Navy (#000416) and White (#FFFFFF). This eliminates the "White Flash" on fresh installs; the app is brand-consistent from the very first frame.

    Navigation: Stabilized with consistent icon shading reactive to the dynamic theme.

3. Feature Map & UI Status
   🟢 Completed & Locked

   High-Density Holdings UI: Reclaimed ~85dp of vertical space by tightening the header cluster and performing a 10dp "Center-Cut" shrink on asset cards. Third card visibility is achieved.

   Holdings Reorder Logic: implemented sh.calvin.reorderable for frame-perfect, GPU-accelerated dragging.

        Safety: Reordering is restricted to the "ALL" tab to prevent index corruption.

        UX: Integrated hardware layer locking (shadowElevation) and touch-aligned trash-zone detection.

   Branded Manual Asset Entry: Supports custom metals with two-line descriptions (e.g., "SILVER" over "EAGLES"). Cards use a 40dp name container with softWrap to ensure perfect stacking.

   Theme Studio: Full interactive control over background and text colors with "Reset to Default" logic.

🟡 In Progress / Work-in-Progress (Current Focus)

    Portfolio Analytics (Donut Charts): * Goal: Make the Portfolio Value row clickable to launch a dedicated analytics screen.

        Logic: Aggregating value breakdown (Metals vs. Crypto) and top specific holdings.

    Big Refactor: Breaking down the massive MyHoldingsScreen.kt into modular components (Header, TabRow, AssetList) for better maintainability.

🔴 Upcoming Features

    Compact Card Mode: Connecting the Settings switch to toggle between the Full and Compact card layouts.

    Welcome Experience: Implementing a one-time welcome popup for new users that does not "ghost flash" after the first asset entry.

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. BRAND-HARDENED FALLBACKS: Preventing the "White Flash"
val siteBackgroundColor: Flow<String> = context.dataStore.data.map {
it[PreferencesKeys.SITE_BACKGROUND_COLOR] ?: "#000416" // Default to Navy
}

// 2. CENTER-CUT CARD SHRINK: Shaving 10dp while preserving 2-line name stacking
Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
Text(text = asset.name.uppercase(), softWrap = true, maxLines = 2)
}
Spacer(modifier = Modifier.height(2.dp)) // Precision center-cut
HorizontalDivider()

// 3. VERTICAL SPACE RECLAMATION: Using negative spacers to pull UI up safely
Spacer(modifier = Modifier.height((-85).dp)) // Reclaims space above Tabs/List

Project status updated, Swanie. Rest easy—we'll start tomorrow by cleaning up that big holdings file.