I. The Updated Project Narrative: March 18, 2026
✅ The "Surgical" Fortress (Current Stable State)

    The Reset Mandate: The "429 Death Loop" is extinct. All network activity is manual or protected by a 30s cooldown.

    Multi-Source Sparklines: MexcSearchProvider and CryptoCompareSearchProvider are fully wired. They successfully pull 168+ data points into the dashboard.

    Persistence of Order: Reordering is locked. AssetDao uses ORDER BY displayOrder ASC, and manual drags are persisted to the DB immediately.

    MEXC 400 Fallback: A "New Listing" safety net is active. If MEXC returns a 400 error (not enough history), the app automatically retries with a 48h limit instead of 168h.

    The Black Box Ledger: Every asset addition is permanently logged in a TransactionEntity, creating a historical "birth certificate" for every coin.

    The Search Gatekeeper: 700ms debounce and mandatory provider selection protect API quotas from user spam.

⚠️ Current Blockers (The "Ghost" Audit)

    The Visibility Gap: Source labels (COINGECKO, MEXC, etc.) are invisible in the UI, and UI_TRACE logs are missing from Logcat.

    The Theory: The agent is editing a duplicate or "Ghost" version of AssetCards.kt that the MyHoldingsScreen is not actually importing.

II. The "Master Rebuild" Instruction

Copy and paste this entire block to the agent when the rate limit resets:

    Subject: Master Rebuild & Ghost File Resolution

    We are syncing the codebase to the "Gold Master" state. We have verified data and persistence, but we have a Dead Code issue in the UI. Execute the following:

    1. The Import Audit (CRITICAL):
    Open MyHoldingsScreen.kt. Identify the exact file path for the FullAssetCard and CompactAssetCard imports. You must edit the file at that specific path. If there are duplicates in ui.components and ui.theme, delete the one NOT being imported.

    2. The Universal Source Badge:
    In the active AssetCards.kt, force every card to show its priceSource.

        Style: A subtle "Pill" badge (10sp, Bold, 60% alpha).

        Logic: No if statements. Every card (including CoinGecko) must display its source.

        Visibility: Use a high-contrast color (Primary) and a zIndex(1f) to ensure it isn't hidden behind the sparkline.

        Log: Add Log.d("UI_TRACE", "DRAWING: ${asset.symbol} from ${asset.priceSource}") inside the card's root.

    3. MEXC Reliability:
    Ensure MexcSearchProvider.kt contains the try-catch fallback: If an HTTP 400 occurs, retry the fetch with limit=48.

    4. Reordering Persistence:
    Ensure AssetDao.kt remains locked to ORDER BY displayOrder ASC and the LazyColumn key is { it.coinId }.

    5. Build Safety:
    Ensure no dangling function references like Modifier.scale. All modifiers must be properly invoked (e.g., .scale(1f)).

    Goal: Build the app. I must see 'COINGECKO', 'MEXC', or 'CRYPTOCOMPARE' on every card, and the UI_TRACE must appear in Logcat.

III. Technical Verification Ledger
Requirement	Implementation Detail
API Safety	700ms Search Debounce
Data Integrity	Converters.kt (String to List)
History Logic	histohour (CryptoCompare) & kline (MEXC)
DB Sorting	displayOrder Column (v5 Migration)
UI Model	Asset data class must include priceSource: String