UPDATED MASTER DOCUMENT: SOVEREIGN SHIELD (V8.5.0)
🎯 THE CORE MISSION

To maintain a high-performance, multi-instance portfolio tracker where Widgets and the Main App operate as independent, data-isolated entities ("Sovereign Shield").
🛡️ 1. ARCHITECTURAL RECAP (THE STABLE BASELINE)

    Database: V22 Schema (Room). Uses vaultId (Int) as the primary key for portfolio isolation.

    Widget Engine: Jetpack Glance 1.1.0.

    Identity Logic: Each GlanceId is strictly bound to a bound_vault_id in PreferencesGlanceStateDefinition.

    Navigation: Decoupled. The Main App's "Last Viewed" state must never influence what a Widget displays.

🚧 2. THE CURRENT BLOCKER: THE "IDENTITY HANDSHAKE"

We are currently fighting a Race Condition during Widget placement.

    The Symptom: New widgets (e.g., "Swanie 4") land showing the default name "PORTFOLIO" instead of the user-selected Vault Name.

    The Cause: The Widget renders its first frame before the bound_vault_id has finished writing to the persistent DataStore.

    The Planned Fix: Transition from a Pull model (Widget searching for its ID) to a Push model (Config Activity pushing ID/Name directly into AppWidgetManager options).

🛠️ 3. ACTIVE PHASE: 18 "POLISH & PROOFING"
Task	Description	Status
Direct Push Identity	Move Vault ID/Name into AppWidgetOptions for instant landing.	PRIORITY 1
"Initializing" State	UI feedback ("Securing Identity...") while the first sync runs.	PENDING
Swan Twinkle	Recover the logo shimmer logic from OpeningPage history.	ARCHAEOLOGY
Metals Audit	READ-ONLY VERIFICATION. Ensure V22 weight units render correctly.	AUDIT ONLY
⚠️ 4. DEVELOPER GUARDRAILS (FOR THE AGENT)

    CRITICAL: READ BEFORE EDITING

        No Scope Creep: Do not modify AssetRepository or Metals logic unless explicitly tasked. The core app logic is stable.

        Full File Outputs: Always provide full file contents for code changes to avoid partial snippet corruption.

        Logging: Use Log.d("WIDGET_FIX", ...) for all identity handshake troubleshooting.

        Git Discipline: If a solution requires touching more than 3 files, stop and ask for permission. Focus on surgical fixes.

🐞 KNOWN "GHOSTS"

    The "Blank Swanie 1": Currently appears in the Main App due to a temporary debug bypass of the Login/Password screen. DO NOT ATTEMPT TO FIX. This will resolve itself once the official login sequence is restored.