UPDATED MASTER DOCUMENT: SOVEREIGN SHIELD (V8.6.0)
🎯 THE CORE MISSION

To maintain a high-performance, multi-instance portfolio tracker where Widgets and the Main App operate as independent, data-isolated entities ("Sovereign Shield").
🛡️ 1. ARCHITECTURAL RECAP (THE STABLE BASELINE)

    Database: V22 Schema (Room). Uses vaultId (Int) as the primary key for portfolio isolation.

    Widget Engine: Jetpack Glance 1.1.0.

    Identity Logic: Each GlanceId is strictly bound via "The Double-Stamp":

        PreferencesGlanceStateDefinition (Persistent DataStore).

        AppWidgetManager Options Bundle (Synchronous System memory).

    Navigation: Decoupled. The Main App's "Last Viewed" state must never influence what a Widget displays.

🚧 2. THE CURRENT BLOCKER: "THE FIRST-FRAME HYDRATION"

We have successfully solved the Identity Handshake (the widget now knows who it is instantly). However, we are now fighting the Content Lag.

    The Symptom: New widgets land instantly with the correct Vault Name, but show an empty "Portfolio Linked" message instead of assets.

    The Cause: The widget renders its first frame using only the identity "Snapshot." The actual asset list still requires a background database query that isn't fast enough for the initial render.

    The Planned Fix: Upgrade the "Snapshot" to a "Full Portfolio Stamp." The Config Activity will now push the Top 5 Assets directly into the AppWidgetOptions bundle alongside the Vault Name.

🛠️ 3. ACTIVE PHASE: 19 "FIRST-FRAME FIDELITY"
Task	Description	Status
Top 5 Asset Stamp	Serialize/Push Top 5 Assets into AppWidgetOptions during config.	PRIORITY 1
Snapshot Parsing	Update PortfolioWidget to render assets from the Bundle if DB is slow.	PENDING
"Initializing" State	UI feedback ("Syncing live prices...") while background worker runs.	PENDING
Swan Twinkle	Recover logo shimmer logic from OpeningPage history.	ARCHAEOLOGY
Metals Audit	READ-ONLY VERIFICATION. Ensure V22 weight units render correctly.	AUDIT ONLY
⚠️ 4. DEVELOPER GUARDRAILS (FOR THE AGENT)

CRITICAL: READ BEFORE EDITING

    No Scope Creep: Do not modify AssetRepository or Metals logic. The core app logic is stable.

    Full File Outputs: Always provide full file contents for code changes to avoid partial snippet corruption.

    The Double-Stamp Rule: Every identity change must be written to both updateAppWidgetState and updateAppWidgetOptions.

    Logging: Use Log.d("WIDGET_FIX", ...) for all handshake troubleshooting.

🐞 KNOWN "GHOSTS"

    The "Blank Swanie 1": Appears in the Main App due to a temporary debug bypass of the Login/Password screen. DO NOT ATTEMPT TO FIX.

🚀 Next Agent Command

"I have updated the narrative to V8.6.0. We are moving from 'Identity' to 'Content Hydration'. Please implement the Top 5 Asset Stamp in WidgetConfigActivity.kt and PortfolioWidget.kt. Provide full files."