Project Intent

    Goal: A premium, personal portfolio tracker for Crypto (XRP focus) and Metals.

    Target: Personal use first, then Google Play Store (commercial).

    Aesthetic: Dark/Neon theme utilizing custom high-res keyboard assets.

Technical Stack

    Language: Kotlin / Jetpack Compose.

    Database: Room (for persistent local storage of assets and amounts).

    API: Retrofit (connecting to Coingecko for real-time prices/icons).

    Navigation: Compose Navigation with type-safe routes.

Feature Requirements (Phase 1)

    Data Persistence: Local Asset table containing: coinId, symbol, name, amountHeld, currentPrice, change24h, and displayOrder.

    Asset Picker: Custom Search screen using AlphaKeyboard linked to a live API list.

    Holdings Dashboard: A list of cards showing holdings.

        Interactive Cards: Must include a drag-handle for manual reordering and a delete button.

        Visuals: Icon, Price, 24h % change with up/down indicators.

    Flow: Asset Picker -> Amount Entry -> Save to Room -> MyHoldings View.

🚀 Next Step: Triggering the Agent

Now that you have the