Architecture_Brief.md

    MANDATORY PROTOCOL: Read this entire brief before every task. Do not refactor multiple layers (Repository, ViewModel, UI) simultaneously. Implement changes atomically, one file at a time, to maintain build stability.

Project Intent

    Goal: A premium, personal portfolio tracker for Crypto (XRP focus) and Metals.

    Target: Personal use first, then Google Play Store (commercial).

    Aesthetic: Dark/Neon theme. Note: Use standard Android system keyboards; custom high-res keyboard assets have been deprecated.

Technical Stack

    Language: Kotlin / Jetpack Compose.

    Database: Room (Persistent local storage).

    API: Retrofit (CoinGecko integration).

    Navigation: Compose Navigation with URL-encoded argument passing.

Feature Requirements (Phase 1)

    Data Persistence: AssetEntity contains: coinId, symbol, name, imageUrl, amountHeld, currentPrice, change24h, displayOrder, and lastUpdated.

    The "Lean Search" Logic: - Asset Picker: Search results must remain Price-Blind to avoid API rate limits. It only provides id, name, symbol, and imageUrl.

        Handoff: Navigation must pass all 4 string arguments (URL-encoded) from Picker to Amount Entry.

    Amount Entry Screen: This screen is responsible for the Targeted Price Fetch.

        It must fetch the price for the specific coinId on launch.

        The UI should display the price/icon and allow the user to input quantity.

    Holdings Dashboard: Displays cards with drag-handle reordering and delete functionality.

Database & Schema Management

    Primary Key: coinId (Unique identifier).

    Conflict Strategy: OnConflictStrategy.REPLACE. Saving an entry updates the quantity for that asset.

    Migration: Development uses fallbackToDestructiveMigration(). Increment database version in AppDatabase.kt whenever AssetEntity fields change.

Performance & API Stability

    Rate Limiting: Strictly Forbidden to perform price lookups in the Search List or during onValueChange in the Picker.

    Debounce: Search queries must have a 500ms delay to protect API credits.

    Calculations: UI performs quantity * currentPrice. Totals are calculated via sumOf.