Updated Project Narrative: Swanie’s Portfolio Development
I. Current State & Recent Technical Wins

The project has graduated from a UI-focused prototype to a data-driven application. The architecture now supports real-time market data persistence and a flexible, user-defined display system.

    Batch-Sync Data Engine: Migrated from individual asset refreshes to a high-performance "Batch" strategy using the CoinGecko /coins/markets endpoint. This allows updating price, 24h trends, and sparklines for all assets in a single network call, significantly reducing API rate-limit risks.

    Dual-View Holdings System: Implemented a "Compact vs. Full" toggle. Users can switch between a slim, high-density list and detailed asset cards.

    Persistent Sparkline Caching: Integrated Room TypeConverters to store 168-point sparkline arrays locally. This ensures that visual data is available instantly upon app launch, even before the first network refresh.

    Interactive Detail Pop-ups: Implemented ModalBottomSheet logic. When in Compact Mode, clicking an asset triggers a slide-up "Full Card" view, maintaining a clean UI without sacrificing data depth.

    Stable HSV Color Engine: Decoupled Hue, Saturation, and Value states from database writes, ensuring that the custom theme picker remains fluid and stutter-free.

    Onboarding UI Shell: The CreateAccountScreen is fully wired with glassmorphism-styled input cards and a hero header, ready for authentication logic.

II. Architectural Standards & Roadmap

The project follows a "Single Source of Truth" (SSOT) pattern where the UI exclusively observes the Room database.

    Universal Brush Architecture: Site-wide backgrounds utilize a Brush.verticalGradient derived from HSV "Value" shifts (±15%). This is already implemented and must be maintained across all new feature screens.

    The Repository Pattern: All network logic must flow through the AssetRepository. The UI should never call the API directly; it triggers a refreshAssets() call in the Repository, which then updates the Local DB.

    Canvas Rendering: Sparklines are custom-drawn on a Canvas using a Path with vertical gradient fills. Any future charts should follow this coordinate-normalization math to ensure performance.

    Next Logic Steps:

        Authentication: Wire Firebase to the "Sign Up" button and implement input validation.

        Visual Polish: Transition sparkline drawing from jagged lines to smooth Bezier curves.

        Settings Expansion: Add "Currency Selection" (USD/EUR/BTC) to the Batch-Sync parameters.

III. Build & Safety Standards

    Database Migrations: Due to the introduction of sparklineData and marketCapRank, the database version is currently at v3. Use .fallbackToDestructiveMigration(dropAllTables = true) during this rapid development phase.

    Rate-Limit Awareness: The 30-second "Live Market" timer is a hard constraint for the free-tier CoinGecko API. Do not bypass the timer logic in the ViewModel.

    Named Parameters: All Compose modifiers must explicitly use named parameters (e.g., Modifier.background(brush = ...)).

    Source of Truth: The docs/ folder contains the latest master headers. Always refer to the NARRATIVE file to re-align the AI Agent after a major refactor or "rollback" event.