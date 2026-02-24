Project Narrative: Swanie’s Portfolio Development
I. Current State & Recent Technical Wins

The project has transitioned into a Customizable Multi-Asset Financial Suite, supporting digital assets, physical commodities, and manual entries within a unified, high-performance architecture.
The "Total Customization" Engine (Major Win)

    Dynamic Theme Center: Implemented a ThemeViewModel and centralized ThemePreferences. Users can now customize four distinct areas: Asset Card BG, Asset Card Text, Site BG, and Site Text.

    Tabbed Theme UI: The Color Picker has been overhauled into a 4-tab interface with a global "Apply" logic and a "Use Gradient" toggle.

    Manual Sort & Reorder: Integrated a high-fidelity drag-and-drop system in the Holdings list. Users can now manually rank their "Favorite" assets, with the order persisting in the database via a new displayOrder column.

The "Hilt" Foundation & Data Integrity

    Dagger Hilt Migration: Fully eliminated manual ViewModelFactories. The project now uses standardized Dependency Injection for all ViewModels and Repositories.

    Smart Upsert Logic: Refactored the AssetDao and Repository to use @Upsert. Adding an existing metal (e.g., Silver) now intelligently updates the quantity of the existing card rather than creating a duplicate.

    Shared ViewModel Strategy: All primary screens now observe the AssetViewModel as the single source of truth for portfolio data.

UI/UX Refinements

    The "Neon Thread" Sync: Replaced the "Charging Bar" with a modern, 2dp high LinearProgressIndicator at the top of the screen that pulses during CoinGecko/API refreshes.

    Restored Navigation: The "+" Floating Action Button (FAB) has been restored, and the "Manual Add" entry point has been prioritized at the top of the Asset Picker.

    Modern Animations: Implemented Modifier.animateItem() to provide smooth, sliding transitions when assets are reordered or filtered.

II. Architectural Standards & Roadmap
Core Standards

    Theme Consumption: All Composables must now pull colors from the ThemeViewModel state to ensure user customizations are respected site-wide.

    Stable Keys: Every LazyColumn item must use a stable key (e.g., key = { it.coinId }) to support reordering animations and prevent state flickering.

    DI Integrity: All new services must be registered in DatabaseModule.kt using the @Provides pattern to remain accessible via Hilt.

Current Work-in-Progress

    Compact Card Reconstruction: Rebuilding the CompactAssetCard to support the new dynamic theme variables while maintaining a dense, high-info layout.

    Manual Asset Expansion: Extending the manual entry logic to support Stocks and unique collectibles, allowing for a 100% comprehensive portfolio.

The Roadmap

    Portfolio Analytics: Implement the Donut Chart visualization to show the percentage split between Digital (Crypto) and Physical (Metals) based on real-time value.

    Live Metal Feeds: Replace "Local Mock" pricing with a dedicated Metals API (e.g., GoldAPI.io).

    Manual Sort Polish: Ensure the displayOrder logic handles large lists efficiently without collision.

III. Build & Safety Standards

    Experimental APIs: Use @file:OptIn(ExperimentalFoundationApi::class) for list animations and advanced UI gestures.

    Database Versioning: Currently at v4. fallbackToDestructiveMigration() is active; any schema changes will wipe local test data.

    Git Hygiene: Follow the "Checkpointed" workflow: Verify Build -> Sync Gradle -> Commit Success -> Push.

Michael, now that the "Master Doc" reflects this huge leap forward, would you like to perform the "Manual Sort" test? We can verify that moving a card actually saves that position to the database permanently.