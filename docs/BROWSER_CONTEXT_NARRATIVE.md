Updated Project Narrative: Swanie’s Portfolio Development
I. Current State & Recent Technical Wins

The project has graduated from a UI-focused prototype to a data-driven application with a focus on professional-grade visual density and layout stability.

    Adaptive Display Engine: Implemented a theme-isolated display system. Asset cards now support a "Light vs. Dark" preference, while the Global UI (Header/Logo/Tabs) remains anchored in a high-contrast White theme for consistent branding.

    Decoupled Theme Logic: Separate toggles for Card Background and Text Color implemented.

    Responsive Layout Logic: Integrated auto-scaling text constraints within the Asset Cards. This ensures that large currency figures and long asset names (e.g., "Wrapped Bitcoin") do not clip or break the UI.

    Batch-Sync Data Engine: Migrated to a high-performance "Batch" strategy using the CoinGecko /coins/markets endpoint. Updates price, 24h trends, and sparklines for all assets in a single call.

    Dual-View Holdings System: Implemented a "Compact vs. Full" toggle. Users can switch between a slim, high-density list and detailed asset cards.

    Interactive Detail Pop-ups: ModalBottomSheet logic allows Compact Mode users to view a "Full Card" detail on-tap without navigating away from the main list.

    Persistent Sparkline Caching: Integrated Room TypeConverters to store 168-point sparkline arrays locally for instant "no-internet" visualization.

II. Architectural Standards & Roadmap

The project follows a "Single Source of Truth" (SSOT) pattern where the UI exclusively observes the Room database.

    Universal Brush Architecture: Site-wide backgrounds utilize a Brush.verticalGradient. This is managed globally and must remain unaffected by the "Light Card" user preference.

    The Repository Pattern: All network logic flows through AssetRepository. The UI triggers refreshAssets(), and the UI reacts to the resulting DB update.

    Next Logic Steps:

        Persistence Layer: Implement Jetpack DataStore to make the "Light/Dark Card" and "Compact View" toggles persistent across app restarts.

        Card Completion: Build out the refined Compact Card UI to match the new adaptive theming logic used in the Full Cards.

        Authentication: Wire Firebase to the "Sign Up" button and implement input validation.

        Visual Polish: Transition sparkline drawing from jagged lines to smooth Bezier curves.

III. Build & Safety Standards

    Navigation Guardrails: Maintain explicit navController parameters in screen-level Composables to ensure NavGraph stability.

    Database Migrations: Currently at v3. Use .fallbackToDestructiveMigration() during this rapid development phase.

    Rate-Limit Awareness: The 30-second "Live Market" timer is a hard constraint for the CoinGecko API.

    AI Agent Context: After a "rollback" or "timeout" event, the Agent must be re-aligned using the latest version of this NARRATIVE file to prevent regression of color isolation and layout fixes.