UPDATED MASTER DOCUMENT: SOVEREIGN SHIELD (V10.0.0)
🎯 THE CORE MISSION

To maintain a high-performance, multi-instance portfolio tracker where Widgets and the Main App operate as independent, data-isolated entities ("Sovereign Shield"). Every portfolio (Swanie 1-5) must possess its own unique identity, appearance, and privacy settings.
🛡️ 1. ARCHITECTURAL RECAP (THE GOLDEN BASELINE)

    Database: V25 Schema (Room).

        VaultEntity now acts as the Sovereign Registry, storing unique colors (widgetBgColor, etc.), asset IDs, and the privacy toggle (showWidgetTotal).

    Hilt Isolation: The SettingsViewModel is decoupled from MainViewModel to prevent circular dependencies and startup deadlocks.

    Initialization Lock: The UI uses a LaunchedEffect lock to ensure draft states are re-hydrated from the database immediately upon switching portfolios.

    The Triple-Stamp: Every "Save" operation commits to the Database, updates the Local State, and broadcasts a manual refresh to the Home Screen widgets.

✅ 2. TODAY’S VICTORIES (MISSION ACCOMPLISHED)

    Startup Deadlock Broken: Implemented an emergency timeout and lazy DAO injection to fix the Splash Screen hang.

    Registry Dropdown: Created a centralized "Pick a Portfolio to edit" menu that switches contexts without data leakage.

    Color Sovereignty: Swanie 1-5 can now hold 5 independent color schemes persistently.

    Privacy Isolation: Moved "Hide Totals" from a global setting to a per-vault setting.

    Sleek Command UI: Implemented a top-right icon-based Save/Undo system with "Dirty State" detection.

🎨 3. ACTIVE PHASE: "POLISHING THE SHIELD"
Task	Description	Status
Living Preview	Update WidgetPreviewSlim to show real asset names/counts from the selected vault.	NEXT PRIORITY
Color Swatch Menu	Add color indicators to the "Pick a Portfolio" dropdown items.	PENDING
Haptic Lock-In	Add haptic feedback (vibration) to the Header Save Icon.	PENDING
Visual Fades	Animate color transitions in the preview UI for a premium feel.	PENDING
Metals Audit	READ-ONLY VERIFICATION. Ensure V25 weight units render correctly.	AUDIT ONLY
⚠️ 4. DEVELOPER GUARDRAILS (FOR THE AGENT)

CRITICAL: READ BEFORE EDITING

    The Sovereign Rule: Never use UserConfigEntity (Global) for properties that should be per-portfolio (Vault).

    Full File Outputs: Always provide full file contents. No partial snippets.

    Dirty State Integrity: Ensure isDirty remains the gatekeeper for the Save/Undo icons.

    Log Tagging: Use Log.d("DATABASE_SAVE", ...) for persistence auditing.

🐞 KNOWN "GHOSTS"

    The "Blank Swanie 1": Appears in the Main App due to a temporary debug bypass of the Login/Password screen. DO NOT ATTEMPT TO FIX.

🚀 Next Agent Command

    "I have updated the narrative to V10.0.0. The plumbing is solid. We are now entering the 'Polishing' phase. Please implement the Living Preview in WidgetManagerScreen.kt so the preview reflects real asset data from the selected vault instead of dummy text. Provide full files."