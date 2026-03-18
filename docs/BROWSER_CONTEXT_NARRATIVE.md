Updated Project Narrative: March 18, 2026
✅ Current Working State (The "Surgical" Fortress)

    The Reset Mandate Achieved: The "429 Death Loop" is officially extinct. The network is silent except for intentional, manual triggers.

    Surgical Isolation Protocol: AssetRepository.kt uses executeSurgicalAdd to bypass global refreshes. New assets land with 100% data accuracy without alerting rate-limit sensors.

    The Search Straitjacket: Logic features a 700ms debounce and a 2-character floor, preventing "search-as-you-type" spam while remaining responsive to user intent.

    The Search Gatekeeper: A mandatory Provider Selection UI is active. Users must choose a source (CoinGecko, Yahoo, etc.) before the search box enables, adding a human-driven delay that protects API quotas.

    Metals Shield (30s Cooldown): A strict cooldown is applied to the MarketWatch fetch. Navigating the metals screen no longer "attacks" Yahoo Finance servers.

    The "Black Box" Transaction Log: A permanent ledger (TransactionEntity) tracks every asset addition. This provides a "birth certificate" for every asset, recording the initial price and source for total transparency.

    The Multi-Source Journey: The "Add Asset" routine features a randomized 6-9 second animation with dynamic, provider-specific milestones (e.g., "Checking MEXC order book..."). It concludes with a success checkmark and confirmation that the transaction was logged to the Black Box.

🛠️ Next Objectives (The "Premium & Precision" Era)

    Provider Expansion: Plug in the specific API logic for MEXC and CryptoCompare to utilize the newly minted SearchProvider interface.

    The "Black Box" UI: Create a dedicated History screen or bottom-sheet to allow users to view their transaction ledger and "First Purchase" data.

    Portfolio Screen Overhaul: Transition to a premium "Command Center" look:

        Glassmorphism: Suble blurs and semi-transparent cards.

        Collapsible Buckets: Grouping assets by category (Metals, Main-Cap, Alt-Gems).

        Live Pulse: Visual indicators showing the "freshness" of the last price sync.

    Source-Aware Global Sync: Ensure the background refresh routine respects the priceSource tag on every asset, only hitting the appropriate API for that specific item.

🧪 Status of the "Add Asset" Audit

    PASS: New assets land with full price and source metadata.

    PASS: Database v5 migration is stable and correctly maps Asset/Transaction relationships.

    PASS: Adding an asset triggers ZERO recursive sync loops.

    PASS: UI feedback (The Journey) effectively hides technical speed to prevent user spamming.