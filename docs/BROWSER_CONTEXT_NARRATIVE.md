🛡️ NARRATIVE: THE "VISUAL SYNC" STRIKE (V7.2.3)

Current Version: 7.2.3 (The "Parity" Edition)

Build Status: 🔴 VISUAL MISMATCH (App V18 Stable / Widget Out-of-Sync)

    Critical Observation: The main app is successfully displaying the V18 "Precision Vault" data (Rectangular bars, specific unit stamps). However, the Home Screen Widget is failing to render these specific V18 attributes, defaulting to generic "G" placeholders.

1. THE V7.2.3 MISSION: TOTAL PARITY

We are currently aligning PortfolioWidget.kt to the V18 schematic. The goal is to move from the "Generic State" to the "Precision State" seen in the app.

🚀 Engineering Blockers to Resolve:

    Icon Stamping Failure: The widget is still rendering a static circle with a "G". It needs to be updated to a Box layout that mimics the MetalIcon logic, checking the weightUnit to render "1k", "1g", etc.

    Label Truncation: The widget is pulling asset.name (Gold) instead of asset.displayName (Gold (1kg)). This is causing the user to lose critical context on the home screen.

    Shape Consistency: The widget is not respecting the "Bar vs. Coin" distinction. Bars should be rendered as Rounded Rectangles, not circles.

💎 Current UX State:

    App: 🟢 Precision Stamping (1/10oz, 1oz, 10oz, 100oz, 1k, 1g).

    Widget: 🔴 Legacy Generic (G, G, G, G, G).

2. THE "FORTRESS" SPECS (V7.2.3)
   Component	Status	Logic Requirement
   Data Layer	🟢 STABLE	V18 Unit-Driven displayName
   Database	🟢 V18	Explicit weightUnit (GRAM, KILO, OZ)
   Widget UI	🔴 MISALIGNED	Needs AssetCardOriginal refactor
   Sparklines	🟡 OPTIMIZING	Transitioning to RGB_565 memory safety
3. THE PATH FORWARD: LOCKING THE SYNC

To fix the images you just showed me, I need to provide the Full File for PortfolioWidget.kt that specifically addresses the AssetCardOriginal layout. We must replace the generic ImageProvider with a Box/Text combo for the icon and switch the title to asset.displayName.