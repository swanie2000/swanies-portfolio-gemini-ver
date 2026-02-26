BROWSER_CONTEXT_NARRATIVE.md
1. Project Overview

   App Name: Swanie's Portfolio

   Purpose: Crypto & Precious Metals tracking with a high-end, custom-themed UI.

   Current Branch: wip-hilt-fix

   Tech Stack: Kotlin, Jetpack Compose, Hilt (Dependency Injection), Room, Retrofit, StateFlow.

2. Architectural Status (The "Hilt" Restoration)

Today we successfully completed the migration from a broken manual ViewModel factory system to a clean Hilt-injected architecture.

    ThemeViewModel: Acts as the single source of truth for UI colors. It persists hex strings for siteBackgroundColor and siteTextColor.

    MainViewModel: Handles core app logic, currently observing themeColorHex for legacy compatibility where needed.

    Injection Pattern: Screens now use hiltViewModel<ThemeViewModel>() to observe theme changes in real-time without "ghost instances."

3. Feature Map & UI Status
   🟢 Completed & Themed

   Home Screen:

        Preserved the "Splash-to-App" transition.

        Layer 1 remains hardcoded Navy (#000416) to match the Android Splash Screen.

        Layer 2 (Radial Burst) and all text/buttons are now dynamic.

   Theme Studio: Full interactive control over site colors with real-time preview and "Reset to Default" logic.

   Asset Picker:

        Implemented "Ghost Swan" empty state that appears instantly.

        Swan is positioned at weight(0.1f) top / weight(3.0f) bottom to stay above the software keyboard.

   Holdings & Create Account: Full theme observation with high-contrast button logic.

🟡 In Progress / Refinement Needed

    Manual Asset Entry: UI exists but needs logic for custom price/weight inputs for non-API assets.

    Asset Logic: AssetViewModel is searching correctly, but the "Add to Database" flow needs a final verification.

🔴 Upcoming Features

    Swipe-to-Delete: Needs to be added to the Holdings LazyColumn.

    Portfolio Analytics: Donut chart visualization for the breakdown of Metals vs. Crypto.

4. Key Logic Snippets (Theming)

We use a remember block with android.graphics.Color.parseColor to safely convert persisted hex strings into Compose Color objects, ensuring the UI doesn't crash on malformed hex strings:
Kotlin

val bgColor = remember(siteBgHex) {
try { Color(android.graphics.Color.parseColor(siteBgHex)) }
catch (e: Exception) { Color(0xFF000416) }
}

5. Active Branch Instructions

   Git Commit History: Last commit 5bbd2f3 locked in the Home Screen and Asset Picker refinements.

   Build Status: Passing. All Unresolved reference errors regarding FocusRequester and CoinItem were resolved.

Next Session Goal: Start the Portfolio Analytics module or finalize Swipe-to-Delete on the Holdings screen.

Would you like me to help you brainstorm the layout for that Analytics Donut Chart when you start your next session?