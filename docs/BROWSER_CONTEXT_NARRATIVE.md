📄 BROWSER_CONTEXT_NARRATIVE.md (Updated)

1. Project Overview

   App Name: Swanie's Portfolio

   Current Branch: main (Dual-Path Logic & 1:1 State Parity Locked)

2. Architectural Status

   Status: UNIFIED SINGLE-SOURCE ARCHITECTURE

        Single Source of Truth: Room Database is now the primary state for all screens.

        Yahoo Integration: Unified YahooFinanceResponse handles price, 24h change, and hourly sparklines (interval=1h&range=7d).

        The "Ghost Row" Solution: Market Watch now observes the database for 1:1 parity with Holdings, only performing "Ghost Fetches" for metals the user does not yet own.

3. Feature Map & UI Status

🟢 Completed & Locked

    1:1 Price Parity: Sync disparity between Market Watch and Holdings is resolved.

    Persistent Trends: 24h percentage changes (priceChange24h) are now saved to the database for all asset types.

    Visual Ownership: Market Watch now features an "OWNED" badge for items existing in the user's portfolio.

🔴 Bug Tracker (Upcoming)

    Refresh Button Polish: Verify the main screen icon correctly triggers the parallel background refresh.

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. UNIFIED REFRESH (AssetRepository)
// Now persists 24h change for parity across all screens
assetDao.upsertAll(owned.map { it.copy(
currentPrice = data.current,
priceChange24h = data.changePercent, // Persistence locked
sparklineData = data.sparkline
)})

// 2. GHOST ROW PARITY (MetalsAuditScreen)
// Prioritizes DB state; falls back to live fetch if not owned
val currentPrice = ownedAsset?.currentPrice ?: ghostData?.current ?: 0.0
val changePct = ownedAsset?.priceChange24h ?: ghostData?.changePercent ?: 0.0

🛡️ Narrative Synchronized