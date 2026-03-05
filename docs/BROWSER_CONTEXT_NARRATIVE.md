📄 BROWSER_CONTEXT_NARRATIVE.md

1. Project Overview

   App Name: Swanie's Portfolio

   Purpose: Professional-grade Crypto & Precious Metals tracking with a high-end, user-curated visual theme.

   Current Branch: main (Dual-Path Data Logic & Market Watch Locked)

   Tech Stack: Kotlin, Jetpack Compose, Hilt, Room, Retrofit, Coroutines (Async/AwaitAll), DataStore.

2. Architectural Status

   Status: DUAL-PATH DATA & MARKET WATCH ENABLED

        The Yahoo Single-Source: Abandoned unreliable CoinGecko/Kinesis mappings for metals. Yahoo Finance is now the sole source of truth for Gold (GC=F), Silver (SI=F), Platinum (PL=F), and Palladium (PA=F).

        Dual-Path Fetching:

            Holdings Path: The main screen ONLY updates metals that explicitly exist in the Room database. No more "Zombie Assets" auto-populating after deletion.

            Market Watch Path: A dedicated screen that fetches all 4 metals independently of the database. This allows market observation without forcing items into the personal portfolio.

        Background "Burst" Logic: Decoupled data fetching from the splash screen. MainViewModel now sets isDataReady = true immediately, allowing theme colors to "burst" onto the screen instantly while AssetRepository loads data in the background via parallel coroutines.

3. Feature Map & UI Status

🟢 Completed & Locked

    Market Watch UI: High-fidelity cards showing Live Price, 24H % Change, and Day High/Low ranges.

    7-Day Sparklines: Successfully integrated 1-hour interval data (interval=1h&range=7d) from Yahoo into the SparklineChart component for all metals.

    Parallel Refresh: refreshAssets() now uses async/awaitAll to fetch Yahoo and CoinGecko data simultaneously, eliminating UI hangs during refreshes.

    Unified Model: Consolidated all Yahoo metadata into a single YahooFinanceResponse.kt to prevent "Unresolved Reference" errors.

🔴 Bug Tracker (Current Focus)

    Sync Disparity: Identifying why "Market Watch" prices may occasionally lead "Holdings" prices during rapid refreshes.

    Asset Picker Verification: Ensuring the search logic still maps CoinGecko IDs correctly to the new Repository structure.

    Sparkline Overflow: Monitoring hourly data points to ensure they scale correctly within the SparklineChart box.

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. DUAL-PATH REFRESH LOGIC (AssetRepository)
// Only updates metals that the user actually owns
metalTickers.map { (symbol, _) ->
async {
val data = fetchMarketPrice(symbol)
if (data.current > 0.0) {
val owned = allLocalAssets.filter { it.baseSymbol == symbol }
if (owned.isNotEmpty()) {
assetDao.upsertAll(owned.map { it.copy(currentPrice = data.current, sparklineData = data.sparkline) })
}
}
}
}.awaitAll()

// 2. SEARCH LOGIC PRESERVATION
// Verifies that CoinGecko search still returns clean AssetEntities for the Picker
suspend fun searchCoins(query: String): List<AssetEntity> = try {
val result = coinGeckoApiService.search(query)
result.coins.map { coin ->
AssetEntity(coinId = coin.id, symbol = coin.symbol ?: "", name = coin.name ?: "",
imageUrl = coin.large ?: "", category = AssetCategory.CRYPTO, baseSymbol = coin.symbol ?: "")
}
} catch (e: Exception) { emptyList() }

// 3. IMMEDIATE UI UNLOCK (MainViewModel)
init {
_isDataReady.value = true // Unlocks splash screen instantly
viewModelScope.launch { repository.refreshAssets() } // Loads in background
}

🛡️ Narrative Synchronized
The app has moved past the "Audit" over-engineering. By separating market observation from portfolio holdings, we have eliminated "Ghost Assets" and restored high-speed UI transitions. Yahoo Finance provides high-fidelity, hourly sparklines for metals, while CoinGecko remains the search and price engine for Crypto.
🏁 Clear Path to Fix Current Bugs

    Search Logic Verification: We need to confirm that AssetPickerScreen is calling viewModel.searchCoins(query) and that the UI correctly handles the cryptoResults flow. We must ensure the agent didn't "simplify" the search so much that it stopped hitting the network.

    State Sync Check: If "Market Watch" shows a different price than "Holdings," it’s because Market Watch fetches its own MarketPriceData object rather than observing the database. We should pivot Market Watch to observe the database while keeping the unowned metals as "Ghost Rows" to ensure 1:1 price matching.

    Refresh Button Polish: Ensure the refresh icon on the main screen triggers viewModel.refreshAssets(), which now handles both the parallel Yahoo and CoinGecko calls.

Would you like me to generate the specific prompt for the Studio Agent to fix the "Search Logic" first? I can have him verify that the network calls are actually firing when you type in the picker.