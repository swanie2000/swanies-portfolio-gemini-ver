Updated Project Narrative: March 18, 2026
✅ Current Working State (The "Surgical" Fortress)

    The Reset Mandate Achieved: The "429 Death Loop" is officially extinct. The network is silent except for intentional, manual triggers.

    Surgical Isolation Protocol: AssetRepository.kt now uses executeSurgicalAdd. This routine bypasses global refreshes, hitting the API for one ID only, ensuring new assets land with 100% data accuracy without alerting rate-limit sensors.

    The Search Straitjacket: Search logic now features a 700ms debounce and a 2-character floor, preventing "search-as-you-type" spam while remaining responsive to user intent.

    The Confirmation Journey: The "Add Asset" screen has been transformed from a technical blip into a premium UX experience. It features a randomized 6-9 second animation with a pulsing icon, real-time status milestones, and an animated Success Checkmark to pace the user and protect the API.

🛠️ Next Objectives (The "Multi-Server" Expansion)

    The Search Gatekeeper: Implement a Required Provider Selection dropdown (CoinGecko, MEXC, Yahoo Finance). This adds a "user-driven delay" and allows access to "hard-to-find" assets across different ecosystems.

    Metals Shield (30s Cooldown): Apply a strict cooldown to the MarketWatch fetch. Rapidly entering and exiting the metals screen will no longer "attack" the Yahoo Finance servers.

    The "Black Box" Transaction Log: Introduce a TransactionEntity database table to track every add, edit, and price sync, providing the user with a historical "receipt" of their portfolio's growth.

    Multi-Server Journey UI: Update the AmountEntryScreen to dynamically display the server currently being accessed (e.g., "Verifying listing on MEXC...").

🧪 Status of the "Add Asset" Audit

    PASS: New assets land with full price and 24h data.

    PASS: Adding an asset triggers ZERO recursive sync loops.

    PASS: UI remains fluid and responsive during the background database write.