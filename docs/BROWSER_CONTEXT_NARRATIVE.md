PROJECT STATUS: THE FORTRESS RESTORED

Current Version: 3.1 (Stable)

Build Status: 🟢 SUCCESS

Database Schema: V5 (Legacy Symmetry)
1. The Post-Mortem: Why V3.2 "Broke"

The previous attempt failed because of a "Symmetry Fracture." We tried to upgrade the engine (Database V6) while the tires were still spinning on V5 logic (ViewModel/Providers).
Component	Status during Crash	Current Restored State
Database	V6 (Partial Migration)	V5 (Gold Master)
Glance Widget	Mismatched Handshakes	Deleted (Clean Slate)
Naming	sortOrder / showOnWidget	displayOrder / officialSpotPrice
Hilt	Dependency Loops	Purged & Realigned
2. The Current "Fortress" Specs

The app is currently in a "High-Density Stable" state. It’s fast, it’s clean, and the connectivity to KuCoin and Coinbase is solid.

    Networking: Retrofit instances are correctly named and injected.

    Branding: The 100dp "Hero Swan" logo is persistent and centers the UI.

    Persistence: DataStore successfully remembers your "Compact/Full" view preferences.

    Providers: CoinGecko acts as the "Safety Net" while KuCoin/Coinbase handle the heavy lifting.

THE REVISED V3.2 BATTLE PLAN: "SYMMETRY-FIRST"

We aren't giving up on the widget; we’re just changing how we invade. Instead of a multi-file "scatter-gun" update, we will move in a single, synchronized strike.
The Phase 2 Objective

    "The Color Pulse Engine" — A home screen widget that renders live trend colors (Green/Red) using a Bitmap Courier system to bypass Glance’s hardware limitations.

The Three-Pronged Strike

    The Bitmap Courier: Create a WidgetIconManager to handle the "Fax Machine" constraint of Jetpack Glance.

    The UI Bridge: Add the "Show on Home Screen" toggle to the Asset Detail screen before we touch the database.

    The Schema Lockdown: Perform the V5 ➔ V6 migration only when the Repository and ViewModel are already "standing by" to receive the new data fields.