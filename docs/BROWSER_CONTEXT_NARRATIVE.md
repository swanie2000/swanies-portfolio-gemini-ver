UPDATED MASTER DOCUMENT: SOVEREIGN SHIELD (V11.2.0)
🎯 THE CORE MISSION

To maintain a high-performance, multi-instance portfolio tracker where Widgets and the Main App operate as independent, data-isolated entities ("Sovereign Shield"). Every portfolio (Swanie 1-5) must possess its own unique identity, appearance, and privacy settings with instantaneous visual feedback.
🛡️ 1. ARCHITECTURAL RECAP (THE SYNCED BASELINE)

    Database: V25 Schema (Room). Stores the "Source of Truth" for every vault.

    The Synchronous Handshake (Glance 1.1.0): We have abandoned the "Passive Update" (waiting for the database) in favor of Synchronous State Persistence.

        The SettingsViewModel now writes directly to the PreferencesGlanceStateDefinition (DataStore) during the save.

        The Widget renders from this "Shared Mental State" instantly, bypassing the Android 30-second update throttle.

    The Sovereign Task: The WidgetConfigActivity is now a "Ghost Task" (noHistory, excludeFromRecents) that pops up for edits and vanishes immediately upon saving, matching the "Day Counter" UX.

    UI Foundation: Replaced OutlinedTextField with a BasicTextField + custom DecorationBox to solve the long-standing "Text Clipping" bug in asset search.

✅ 2. TODAY’S VICTORIES (THE BREAKTHROUGH)

    The 30-Second Wall Breached: Successfully implemented a sub-second "Instant Snap" for background colors and vault names.

    Multi-Instance Fluidity: Confirmed Swanie 1-5 can be toggled and saved rapidly with 100% update reliability.

    Search Box Alignment: Finally achieved perfect vertical centering in the Asset Search bar.

    Task Management: Secured the "Task-Only" configuration mode, keeping the user's "Recents" list clean and focused.

🎨 3. ACTIVE PHASE: "FULL-FREIGHT POLISH"
Task	Description	Status
The Asset Stamp	Serialize Top 5 assets into the DataStore "Handshake" so they appear instantly with the color.	NEXT PRIORITY
Portfolio Elevator	Implement imePadding and scroll-back logic to keep edited portfolios above the keyboard.	PENDING
Living Preview	Sync the internal Settings Preview to reflect the same "Handshake" data as the real widget.	PENDING
Color Swatch Menu	Add visual indicators to the "Pick a Portfolio" dropdown items.	PENDING
Haptic Lock-In	Add vibration feedback to the Header Save Icon.	PENDING
⚠️ 4. DEVELOPER GUARDRAILS (FOR THE AGENT)

CRITICAL: READ BEFORE EDITING

    The Synchronous Rule: Every vault property change (color, name, assets) MUST be written to the PreferencesGlanceStateDefinition inside the same scope as the database save to ensure an instant snap.

    Full File Outputs: Always provide full file contents. No partial snippets.

    Dirty State Integrity: Ensure isDirty remains the gatekeeper for the Save/Undo icons.

    Log Tagging: Use Log.d("WIDGET_SYNC", ...) for lifecycle auditing.

🐞 KNOWN "GHOSTS"

    The "Blank Swanie 1": Appears in the Main App due to a temporary debug bypass of the Login/Password screen. DO NOT ATTEMPT TO FIX.

🚀 Next Agent Command

    "I have updated the narrative to V11.2.0. The Synchronous Handshake is working for colors and names. We are now entering the 'Full-Freight' phase. Please implement the Asset Stamp in SettingsViewModel.kt and PortfolioWidget.kt so the Top 5 assets are pushed into the DataStore and rendered instantly. Provide full files."