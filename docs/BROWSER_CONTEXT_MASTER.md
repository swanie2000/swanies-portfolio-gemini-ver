============================================================
LEVEL 4 AI CONTROL HEADER ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â SWANIE'S PORTFOLIO
============================================================

THIS DOCUMENT IS THE SINGLE SOURCE OF TRUTH.

The browser AI does NOT have file access.
All decisions must be based ONLY on this document.

------------------------------------------------------------
ROLE ASSIGNMENT
------------------------------------------------------------

Android Studio Agent:
- Implementation agent
- Can see files
- Makes code changes

Browser AI:
- Architecture + reasoning advisor
- NEVER assumes files not listed here
- NEVER invents structure
- Must request files when needed

------------------------------------------------------------
MANDATORY BEHAVIOR RULES
------------------------------------------------------------
1) NEVER make code changes unless the user explicitly asks for implementation.
2) DO NOT assume missing files.
3) If code detail is required, request:

   NEED FILE: path/to/file

4) Prefer minimal, safe edits.
5) Avoid large refactors unless explicitly asked.
6) Explain reasoning BEFORE suggesting code changes.

------------------------------------------------------------
SESSION START CHECKLIST (ALWAYS)
------------------------------------------------------------

Before giving advice, remind user to:

[ ] git pull
[ ] regenerate context dump if older than 24 hours
[ ] confirm working branch
[ ] verify this dump matches current files before major decisions

------------------------------------------------------------
WORKFLOW RULES
------------------------------------------------------------

- Context dump should be refreshed regularly.
- After milestones:
  remind user to commit + push.
- Browser AI gives guidance only.
- Studio agent performs edits.

------------------------------------------------------------
USER WORKING STYLE (IMPORTANT)
------------------------------------------------------------

- Provide FULL FILE outputs when code changes are needed.
- Avoid partial snippets unless requested.
- Give step-by-step instructions.
- Reduce complexity; avoid large rewrites.

------------------------------------------------------------
LEVEL 4 MODE (IMPORTANT)
------------------------------------------------------------

Act as a SENIOR REVIEWER.

CORE REVIEWER MINDSET (FINAL RULE):

Act as a SENIOR CODE REVIEWER first, not a code generator.

Your primary job is to:
- protect project stability
- detect risk early
- ask clarifying questions before changing architecture

Only generate code after explaining:
1) what risk you are avoiding
2) why the change is safe
3) why smaller options were rejected


Your default action is to REDUCE risk, not increase capability.

You should:
- detect risky ideas
- suggest smallest safe change
- prefer stability over cleverness
- protect project structure

ADDITIONAL SAFETY RULE (CRITICAL):

Before proposing ANY code change, the AI must:

1) Identify the SMALLEST POSSIBLE SAFE CHANGE.
2) Explain why this change is low-risk.
3) Avoid multi-file rewrites unless explicitly requested.
4) Prefer:
   - one file
   - one function
   - one behavior change at a time.

If the change affects architecture or more than 2 files:
- STOP
- ask user for confirmation first.

If uncertain:
ASK QUESTIONS instead of guessing.

------------------------------------------------------------
AGENT AUTHORITY ORDER (LEVEL 4 CORE RULE)
------------------------------------------------------------

1) REAL SOURCE OF TRUTH:
    - Actual files inside Android Studio project.

2) STUDIO AGENT:
    - Can inspect files.
    - Implementation authority.

3) THIS CONTEXT DUMP:
    - Snapshot of project state.
    - Used for browser AI reasoning.

4) BROWSER AI:
    - Advisory only.
    - Never overrides file reality.

If conflict exists between browser AI advice and real files:
REAL FILES ALWAYS WIN.

------------------------------------------------------------
SESSION END CHECKLIST (ALWAYS)
------------------------------------------------------------

Before ending a session or after a major milestone,
the AI should remind the user to:

[ ] regenerate context dump if major changes were made
[ ] update narrative section if architecture changed
[ ] git add / commit / push
[ ] confirm current branch
[ ] note next-step tasks for the next session
[ ] ask if user wants to save a short "next session plan"

AI behavior rule:
- Do NOT silently end a workflow.
- Ask: "Do you want to run the end-of-session checklist?"

Confirm you understand the Level-4 rules and reviewer mindset before giving advice.

DO NOT START PROCESSING ANYTHING UNTIL THE USER CONFIRMS.!!!!!!!!!!!!

USER WORKING STYLE (IMPORTANT)
------------------------------------------------------------

- Provide FULL FILE outputs when code changes are needed.
- Avoid partial snippets unless requested.
- Give step-by-step instructions.
- Reduce complexity; avoid large rewrites.


============================================================
END CONTROL HEADER
============================================================
============================================================
NARRATIVE SECTION (SOURCE FILE - EDIT docs/BROWSER_CONTEXT_NARRATIVE.md)
============================================================
### BEGIN_NARRATIVE
SWANIES PORTFOLIO: MASTER NARRATIVE (V40.50: FINAL LOCALE CLEAN PASS + BUILD SANITY VERIFIED)

V40.50 UPDATE (Localization Finalization)
- Completed direct, new-data translation cleanup for `values-hi`, `values-th`, `values-uk`, and `values-zh-rTW`.
- Resolved remaining English fallback blocks on auth/settings/terms/holdings surfaces for those locales.
- Preserved formatting tokens (`%1$s`, `%1$d`) and validated UTF-8 readability.
- Verified compile sanity with `:app:compileDebugKotlin` (BUILD SUCCESSFUL).

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â½ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ THE CORE MISSION
To maintain a commercial-grade financial vault where user data is sovereign, biometrics are hardware-enforced, and the UI is a cinematic, high-precision experience that survives high-density settings and real-world widget workflows.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 1. MANDATORY AGENT OPERATING PROCEDURES (THE LANE LOCK)

    FULL FILE OUTPUTS ONLY: Swanie requires full file context. Partial snippets are forbidden unless explicitly requested.

    SOVEREIGN SHIELD: Never push local.properties, secrets, or API keys. Enforce .gitignore discipline.

    STABILITY FIRST: Prefer smallest safe changes. Preserve existing architecture unless explicitly directed.

    DENSITY SHIELD: UI text scaling must respect the clamp pattern: (originalSp.toPx() / fontScale.coerceAtMost(1.15f)).toSp().

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 2. V40.4 FINAL ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â PURE PENCIL (DETERMINISTIC ENTRY)

    Single entry point:
      - There is no in-app ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œWidget ManagerÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â button in Settings. That path was removed.
      - The user opens the editor only from the home widget (Glance pencil): [WidgetClickCallback] starts [WidgetConfigActivity] with [AppWidgetManager.EXTRA_APPWIDGET_ID] (and a relay URI for anti-conflation).

    Activity + manifest contract:
      - [WidgetConfigActivity] is `android:exported="false"` and has **no** `<intent-filter>`. It is not launchable as a generic `APPWIDGET_CONFIGURE` target from the system.
      - If `appWidgetId` is missing or invalid after [resolveAppWidgetId], the activity calls `finish()` immediately ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â no portfolio picker, no multi-widget dialog, no inference from ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œany widget on the host.ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â

    Deterministic binding:
      - [SettingsViewModel.forceVaultSwitch(appWidgetId, isAppWidgetId = true)] resolves the vault row for that exact widget instance.
      - [WidgetManagerScreen] receives a valid `configAppWidgetId` only when the activity has already accepted the intent.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 3. V40.4 FINAL ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â WIZARD FUNNEL + TACTILE REORDER (WIDGET MANAGER UI)

    Wizard Funnel layout ([WidgetManagerScreen]):
      - Funnel navigation uses integer page state (`currentPage`): `0=SETUP`, `1=ASSETS`, `2=STYLE`, `3=PREVIEW`.
      - **SETUP**: portfolio selector + show total control.
      - **ASSETS**: sticky header + flat reorder stage with persisted ordering.
      - **STYLE**: compact color studio controls.
      - **PREVIEW**: guarded preview path with immutable snapshot filtering and fallback error surface.

    600 ms weighted reorder physics:
      - **Row resize / slot feel:** `WidgetReorderItemAnimationSpec` ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â `tween<IntSize>(600, LinearOutSlowInEasing)` on checked/dragging rows.
      - **List placement / slide-to-slot:** `WidgetFlatListPlacementSpec` ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â `tween<IntOffset>(600, LinearOutSlowInEasing)` for `animateItem` placement on reorderable asset rows.
      - After drag stop, a short settle delay (`WIDGET_DRAG_SETTLE_DELAY_MS` = 380 ms) runs before persisting order via [AssetViewModel.updateWidgetSelectionForCurrentVault].

    Tactile reorder UX (unchanged):
      - Long-press on the asset row handle initiates drag; haptics on drag start; persistence on drag end after settle.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 4. V40.4 FINAL ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â SAVE, REFRESH, AND TASK LIFECYCLE

    Save pipeline:
      - [AssetViewModel.saveWidgetConfiguration] persists vault widget fields (including optional `appWidgetId` rebind), pushes Glance data via [AssetRepository.pushFreshAssetsToWidget], and sends [ACTION_APPWIDGET_UPDATE] for the affected instance(s).
      - [SettingsViewModel] still applies appearance and totals (`saveWidgetAppearance`, `updateShowWidgetTotal`) and may call `forceImmediateRemoteViewsUpdate` for instant RemoteViews feedback.

    Primary completion behavior ([WidgetConfigActivity]):
      - On **SAVE & EXIT** success and on **back**, the activity uses **`finishAndRemoveTask()`** as the primary way to tear down the translucent config task and return the user cleanly to the home screen, alongside the post-save broadcast so the launcher-bound widget redraws with the same `appWidgetId` the pencil supplied.

    Vault-scoped truth (unchanged):
      - Canonical widget fields live on [VaultEntity]: `selectedWidgetAssets`, `showWidgetTotal`, widget colors, `appWidgetId`.
      - [AssetViewModel.widgetSelectedAssetIds] / `setWidgetSelectionVaultId` keep selection aligned with the vault being edited.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â° 5. V40.4 CURRENT STATE (TOMORROW TROUBLESHOOTING SNAPSHOT)

    Widget Manager Screen:
      - [SettingsViewModel]: `targetVaultId` / `targetVault` / `targetVaultAssets` for the resolved vault.
      - [AssetViewModel]: ordered selection, toggles, drag-end persistence, and `saveWidgetConfiguration` on save.
      - Config mode: `configAppWidgetId` is always the pencilÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾Ãƒâ€šÃ‚Â¢s widget id when the screen is shown.
      - PREVIEW stability guard:
        - Uses immutable list snapshot (`orderedWidgetAssets.toList()` then filter/take).
        - Uses data-prep `runCatching` gate; renders **"Preview Error"** fallback on preparation failure.
        - Uses static simulated rows (`SimulatedAssetRow`) with hardcoded price/total strings to isolate render-path crashes.
      - Known next-debug target:
        - If PREVIEW still hard-crashes on some devices, first inspect launcher/Activity lifecycle boundary around widget config return and any stale Compose state entering page `3`.

    AssetViewModel contract (widget slice):
      - `saveWidgetConfiguration(portfolioVaultId, appWidgetId, selectedIds, onComplete)` is the save entry used from the widget manager.
      - Ongoing edits still use `updateWidgetSelectionForCurrentVault`, `pushAssetsToWidget` / `pushFreshAssetsToWidget` as appropriate.

    Vault selection contract:
      - Global app vault: [ThemePreferences.currentVaultId].
      - Widget-edit vault: driven by the widget instance ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ [VaultDao.getVaultByAppWidgetId] path when opened from the pencil (may differ from the active app vault while editing).

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 6. V40.34 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â AUTH SURFACE UNIFICATION + SESSION CONTROL (LATEST STABLE)

    Authentication architecture:
      - Auth ownership is unified to shared activity-scoped [AuthViewModel] across Home, Unlock, NavGraph, Create, Restore, and Portfolio Manager surfaces.
      - Login path is deterministic: `HOME -> UNLOCK_VAULT -> HOLDINGS`.
      - Removed unintended direct auth bypass behaviors from startup/resume and login entry routines.

    Session controls:
      - Login timeout is user-configurable: `Never`, `15s`, `30s`, `60s`, `5m`, `15m`.
      - Timeout enforcement now uses true user-leave signal (`onUserLeaveHint`) to avoid false lockouts during system overlays.
      - Orientation is locked to portrait at [MainActivity] manifest contract.

    Credential + biometric reliability:
      - Password verification accepts normalized identity input against username/displayName/email.
      - Biometric prompt copy and error messages are user-friendly and deterministic.
      - Safety control added: `Require Password After Biometric Failure` (default ON).
      - Biometric login CTA is exposed only when `Allow biometrics for login` is enabled in Settings.

    UX polish:
      - Unlock header aligned with app style (`PORTFOLIO LOCKED`, swan-first layout).
      - CTA language simplified and standardized (`LOGIN`, `USE BIOMETRICS`, `CREATE ACCOUNT`).
      - Settings security copy simplified for non-technical readability.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â· 7. DO NOT REGRESS (AUTH + SESSION HARD RULES)

    - Do NOT reintroduce automatic auth bypass on app resume.
    - Do NOT set background timeout stamps from generic lifecycle edges that can fire during overlays (`onStop`).
    - Do NOT expose biometric login button when `Allow biometrics for login` is disabled.
    - Do NOT split auth state ownership across multiple screen-scoped auth viewmodels.
    - Do NOT remove `Never` from login timeout options.
    - Do NOT bypass `UNLOCK_VAULT` from HOME login flow for standard credentialed login.
    - Do NOT silently swallow biometric errors; surface user-friendly messages.
    - Do NOT break portrait lock in `MainActivity` manifest contract.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­ 8. AUTH SOURCE OF TRUTH (FILES + SYMBOLS)

    - Primary auth state authority:
      - [AuthViewModel.authState] in `app/src/main/java/com/swanie/portfolio/ui/features/AuthViewModel.kt`
    - Navigation gate authority:
      - [NavGraph.shouldForceUnlock] in `app/src/main/java/com/swanie/portfolio/ui/navigation/NavGraph.kt`
    - Login/auth execution surface:
      - `app/src/main/java/com/swanie/portfolio/ui/features/UnlockVaultScreen.kt`
      - Password path uses [MainViewModel.verifyCredentials]
      - Biometric path uses [AuthViewModel.triggerBiometricUnlock]
    - Session timeout authority:
      - Preference key + flow in `app/src/main/java/com/swanie/portfolio/data/ThemePreferences.kt`
      - Resume lock enforcement in `app/src/main/java/com/swanie/portfolio/MainActivity.kt`
    - Settings authority for security toggles:
      - `app/src/main/java/com/swanie/portfolio/ui/settings/SettingsViewModel.kt`
      - `app/src/main/java/com/swanie/portfolio/ui/settings/SettingsScreen.kt`

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âª 9. V40.35 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â AUTH RELIABILITY HARNESS (LATEST TEST SAFETY NET)

    - Centralized policy module:
      - [AuthPolicy] in `app/src/main/java/com/swanie/portfolio/security/AuthPolicy.kt`
      - Encapsulates identity normalization, credential matching, and resume-timeout lock decision.
    - Runtime integration:
      - [MainViewModel.verifyCredentials] delegates to [AuthPolicy.matchesCredentials].
      - [MainActivity.onResume] delegates timeout lock decision to [AuthPolicy.shouldLockAfterResume].
    - Unit test harness:
      - `app/src/test/java/com/swanie/portfolio/security/AuthPolicyTest.kt`
      - Covers username/displayName/email matches, password failure, threshold behavior, and `Never`.
    - CI enforcement:
      - `.github/workflows/android-auth-safety.yml` runs `testDebugUnitTest` and `:app:compileDebugKotlin` on push/PR to `main`.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 10. V40.36 BLUEPRINT ROADMAP TRACK (LOCALIZATION + BILLING + TIERS)

    - Localization objective:
      - Externalize and localize user-facing strings.
      - Persist user language preference and apply locale at app startup.
    - Subscription objective:
      - Define entitlement matrix for `Trial`, `Paid`, and `Premium`.
      - Enforce tier-gated feature behavior with safe downgrade handling.
    - Profile/schema objective:
      - Track language and tier preferences (`language_setting`, `user_tier`) in the user profile model.
      - Reflect entitlement + language controls in account/settings UX.
    - Recommended sequence:
      1) Localization foundation,
      2) Tier/entitlement matrix,
      3) Billing integration + profile persistence wiring.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 11. V40.38 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â RECOVERY + PASSWORD MANAGEMENT + THEME PARITY

    - Recovery hardening:
      - `Forgot Password` now requires username + email + biometric verification to reveal hint.
      - Recovery flow returns cleanly to login context via `DONE` without password-reset bypass.
    - Settings password management:
      - Added biometric-gated password update flow requiring current password.
      - Enforced create-account password standards (`8+`, uppercase, number, symbol, confirm match).
      - Moved change-password UI into popup with keyboard-safe scrolling and action visibility fixes.
    - UX and theme consistency:
      - Preserved user input during login/create-account correction flows.
      - Applied user-selected theme colors and card-background dialog surfaces across touched auth/settings/holdings popups.
      - Security section action polish: `RESET PASSWORD` moved to top and left-aligned.
    - Validation:
      - Iterative lint checks and Kotlin compile checks remained green through rollout.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âª 12. V40.39 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â SHARED PASSWORD POLICY + AUTH GUARDRAIL TESTS

    - Policy unification:
      - Added centralized `AuthPolicy.evaluatePasswordStrength(...)` and `PasswordStrength` contract.
      - Create-account and settings password update now share one validation source of truth.
    - Test hardening:
      - Added `AuthPolicyTest` coverage for display-name fallback with blank username.
      - Added password whitespace normalization verification.
      - Added lock gating coverage for unauthenticated timeout path.
      - Added strong/weak password validity and normalization coverage.
    - Stability impact:
      - Prevents silent drift between account creation and password update requirements.
      - Tightens regression safety around auth and password policy changes.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 13. V40.40 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â LOCALIZATION FOUNDATION (PHASE 1)

    - Persistence + runtime locale:
      - Added `language_code` persistence flow in `ThemePreferences`.
      - `MainActivity` now applies locale via `AppCompatDelegate.setApplicationLocales(...)`.
      - Supports `system`, `en`, and `es`.
    - User language controls:
      - Added Settings language selector (`System default`, `English`, `Espanol`).
      - Added Home quick-access globe (top-left) for language switching before login.
      - Globe now shows selected language label below icon for immediate visibility.
    - Resource groundwork:
      - Added localization strings in `values/strings.xml`.
      - Added Spanish resource file `values-es/strings.xml`.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â· 14. V40.41 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â KOREAN LANGUAGE EXTENSION

    - Locale expansion:
      - Added Korean (`ko`) to Settings language selector.
      - Added Korean (`ko`) to Home-screen globe quick switcher.
    - Resource coverage:
      - Added `values-ko/strings.xml` with Korean language selector strings.
      - Added `language_korean` to default and Spanish bundles for option label consistency.
    - Validation:
      - Lint clean and debug Kotlin compile successful after integration.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­ 15. V40.42ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“V40.44 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â LOCALIZATION SURFACE EXPANSION + LOCALE STABILIZATION

    - V40.42:
      - Migrated high-visibility Home/Unlock/Create/Restore/Settings/Portfolio Manager/Theme Manager literals to string resources.
      - Expanded Korean resource coverage for those migrated keys.
    - V40.43:
      - Resolved locale host crash and stabilized runtime locale application path in `MainActivity` for non-AppCompat theme stack.
    - V40.44:
      - Continued migration across Terms, Holdings, Amount Entry, and Widget Manager visible labels/dialogs/tabs.
    - Validation:
      - Lint clean on touched files and debug Kotlin compile passed.

ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ 16. THE FUTURE PATH (CURRENT WORK-IN-PROGRESS)
Task	Description	Priority
Auth Flow Instrumentation Harness	Add end-to-end instrumentation coverage for `HOME -> UNLOCK_VAULT -> HOLDINGS` plus biometric success/cancel/failure UI paths.	IMMEDIATE
Auth Diagnostics Mode	Add a developer-only diagnostics pane to show auth state transitions and timeout decisions.	HIGH
Cross-Device Biometric Validation	Validate biometric callback consistency and prompt/error UX across major OEM devices.	HIGH
Localization Foundation	Extract localized strings and add persisted runtime language selection.	HIGH
Subscription Tier Matrix	Define and enforce Trial/Paid/Premium entitlement boundaries.	HIGH
Billing + Profile Integration	Wire billing status into persisted `user_tier` and account UI.	MEDIUM
Widget Contract Hardening	Keep planned widget regression coverage for reorder persistence and instance rebinding.	MEDIUM
Market Watch Rebuild	Apply Pin-Anchor architecture across Market/Price surfaces for full app parity.	MEDIUM
Sovereign Bridge	Harden cloud sync behavior around vault-scoped widget mutations.	MEDIUM

NEXT AGENT COMMAND
"The narrative is now V40.44: Localization Surface Expansion (Holdings + Widget + Terms).

Current Objective: Continue V40.36.1 by migrating remaining high-traffic user-facing literals to string resources while preserving V40.44 locale behavior and prior auth/security guardrails.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."
ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¹ 17. V40.45 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â LANGUAGE PICKER FINAL CLEANUP

    - Picker simplification:
      - Removed `System default` as a visible option from Home and Settings language selectors.
      - Effective fallback behavior now resolves to English (`en`) for display/selection safety.
    - Resource hygiene:
      - Removed unused `language_system_default` from default, Spanish, and Korean string bundles.
    - Validation:
      - Debug Kotlin compile passed after cleanup.

NEXT AGENT COMMAND
"The narrative is now V40.45: Language Picker Final Cleanup.

Current Objective: Continue localization surface migration (V40.36.1) and translation parity while preserving V40.45 picker behavior and existing auth/session guardrails.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."
ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 18. V40.46 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â TOP-20 LANGUAGE EXPANSION + TRANSLATION FEEDBACK INTAKE

    - Language surface expansion:
      - Expanded both Home globe menu and Settings selector to Top-20 locale coverage.
      - Added native labels for each supported locale in the base string bundle for consistent picker rendering.
    - Translation quality intake:
      - Added `REPORT TRANSLATION ISSUE` in Settings.
      - Users can submit screen/current/suggested text via keyboard-safe dialog and prefilled email handoff.
      - Chosen approach intentionally avoids backend complexity while enabling real-user correction loops.
    - Legal/clarity guardrail:
      - Added Terms disclaimer establishing English as master/authoritative text with AI-translated locale caveat.
    - Validation:
      - Debug Kotlin compile passed after rollout.

NEXT AGENT COMMAND
"The narrative is now V40.46: Top-20 Language Expansion + Translation Feedback Intake.

Current Objective: Continue localization rollout by adding resource bundles for prioritized Top-20 locales and applying translation parity checks while preserving V40.46 picker/feedback and existing auth/session guardrails.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."
ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 19. V40.47 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â LOCALIZATION PHASE 2 (TOP-20 KEY PARITY)

    - Locale bundle expansion:
      - Added 17 new locale resource directories to complete Top-20 selection coverage:
        `ar`, `de`, `fr`, `hi`, `id`, `it`, `ja`, `nl`, `pl`, `pt-BR`, `ru`, `th`, `tr`, `uk`, `vi`, `zh-CN`, `zh-TW`.
    - Parity hardening:
      - Per-locale `strings.xml` files were backfilled to include all keys present in base English bundle.
      - Preserved Phase 1 translated high-visibility text while inheriting remaining keys for deterministic fallback behavior.
    - Stability outcome:
      - Prevents missing-key resource failures and hidden fallback drift in top-20 language paths.
      - Establishes safe foundation for iterative translation-quality passes without runtime risk.
    - Validation:
      - Debug Kotlin compile passed after parity completion.

NEXT AGENT COMMAND
"The narrative is now V40.47: Localization Phase 2 (Top-20 Key Parity).

Current Objective: Execute translation quality pass by replacing inherited English placeholders in new locale bundles, prioritize auth/home/settings/terms surfaces first, and preserve V40.47 parity guarantees.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."
ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 20. V40.48 ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â LOCALIZATION QUALITY PASS (BATCHES A-D)

    - Scope:
      - Completed quality-focused translation passes across all 17 new locale bundles introduced in V40.47.
    - Batch A:
      - Auth core string upgrades (`FORGOT PASSWORD?`, `VERIFYING...`, `DONE`).
    - Batch B:
      - Recovery/auth error and hint-flow messaging upgrades.
    - Batch C:
      - Create-account and security settings copy upgrades.
    - Batch D:
      - Terms heading and translation-feedback UI copy upgrades (CTA/title/subtitle/submit/chooser/no-email).
    - Result:
      - Stronger native phrasing on high-traffic auth/settings/terms surfaces while retaining key parity guarantees.
    - Validation:
      - Debug Kotlin compile passed after batch rollout; lint remained clean on touched resource files.

NEXT AGENT COMMAND
"The narrative is now V40.48: Localization Quality Pass (Batches A-D).

Current Objective: Continue translation refinement with placeholder/format safety checks and RTL visual verification (Arabic) while preserving V40.48 coverage and parity guardrails.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."

21. V40.49 - LOCALIZATION REPAIR + MASTER-TEXT STABILIZATION

    - Emergency repair pass:
      - Repaired locale bundles that were unintentionally re-saved with wrong text encoding.
      - Restored Korean and other affected language files to valid UTF-8 content and revalidated readability.
    - Translation continuity:
      - Preserved Top-20 locale parity while replacing corrupted placeholders with stable localized text where available.
      - Kept English base bundle as legal/master fallback for unresolved phrases.
    - Process hardening:
      - Localization batch workflows now require UTF-8-safe reads/writes to prevent future mojibake/corruption.
      - Manual review checkpoint is required before bulk locale script runs.
    - Validation:
      - Debug Kotlin compile passed after repair and stabilization.

NEXT AGENT COMMAND
"The narrative is now V40.49: Localization Repair + Master-Text Stabilization.

Current Objective: Continue incremental translation quality improvements with UTF-8-safe tooling, prioritize remaining English placeholders by high-traffic screen, and preserve repaired locale integrity.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Tue 04/21/2026 14:55:21.81

Branch:
main
Commit:
3ccbaa35291e5132d9b679d78136f0bce2a4d53a
Working tree status (git status --porcelain):
 M docs/BROWSER_CONTEXT_NARRATIVE.md

--------------------------------------------------
KEY CONFIG FILES (paths)
--------------------------------------------------

settings.gradle.kts
build.gradle.kts
gradle.properties
gradle\wrapper\gradle-wrapper.properties
app\build.gradle.kts
app\src\main\AndroidManifest.xml

--------------------------------------------------
SOURCE FILE INDEX (Kotlin/Java paths)
--------------------------------------------------

app/src/main/java/com/swanie/portfolio/MainActivity.kt
app/src/main/java/com/swanie/portfolio/MainViewModel.kt
app/src/main/java/com/swanie/portfolio/PortfolioApplication.kt
app/src/main/java/com/swanie/portfolio/data/ThemePreferences.kt
app/src/main/java/com/swanie/portfolio/data/api/SearchEngineRegistry.kt
app/src/main/java/com/swanie/portfolio/data/api/SearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/BinanceSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/CoinbaseSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/CoinGeckoSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/CryptoCompareSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/KuCoinSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/MetalSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/MexcSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/WeexSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/di/DatabaseModule.kt
app/src/main/java/com/swanie/portfolio/data/di/NetworkModule.kt
app/src/main/java/com/swanie/portfolio/data/local/AppDatabase.kt
app/src/main/java/com/swanie/portfolio/data/local/AssetDao.kt
app/src/main/java/com/swanie/portfolio/data/local/AssetEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/Converters.kt
app/src/main/java/com/swanie/portfolio/data/local/IconManager.kt
app/src/main/java/com/swanie/portfolio/data/local/PortfolioEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/PriceHistoryDao.kt
app/src/main/java/com/swanie/portfolio/data/local/PriceHistoryEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/SystemLogEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/TransactionDao.kt
app/src/main/java/com/swanie/portfolio/data/local/TransactionEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/UserConfigDao.kt
app/src/main/java/com/swanie/portfolio/data/local/UserConfigEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/VaultDao.kt
app/src/main/java/com/swanie/portfolio/data/local/VaultEntity.kt
app/src/main/java/com/swanie/portfolio/data/network/BinanceApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/CoinbaseApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/CoinGeckoApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/CoinMarketResponse.kt
app/src/main/java/com/swanie/portfolio/data/network/CryptoCompareApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/KuCoinApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/MexcApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/RetrofitClient.kt
app/src/main/java/com/swanie/portfolio/data/network/WeexApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/YahooFinanceApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/YahooFinanceResponse.kt
app/src/main/java/com/swanie/portfolio/data/remote/GoogleDriveService.kt
app/src/main/java/com/swanie/portfolio/data/repository/AssetRepository.kt
app/src/main/java/com/swanie/portfolio/data/repository/DataSyncCoordinator.kt
app/src/main/java/com/swanie/portfolio/data/repository/MarketPriceData.kt
app/src/main/java/com/swanie/portfolio/security/SecurityManager.kt
app/src/main/java/com/swanie/portfolio/ui/Type.kt
app/src/main/java/com/swanie/portfolio/ui/components/AlphaKeyboard.kt
app/src/main/java/com/swanie/portfolio/ui/components/BottomNavigationBar.kt
app/src/main/java/com/swanie/portfolio/ui/components/BoutiqueHeader.kt
app/src/main/java/com/swanie/portfolio/ui/components/CustomToast.kt
app/src/main/java/com/swanie/portfolio/ui/components/SkeletonAssetCard.kt
app/src/main/java/com/swanie/portfolio/ui/entry/AssetArchitectScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/AuthViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/features/CreateAccountScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/HomeScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/RestoreVaultScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/TermsAndConditionsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/UnlockVaultScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AnalyticsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AssetPickerScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AssetViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/HoldingsUIComponents.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/MyHoldingsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/metals/MetalsAuditScreen.kt
app/src/main/java/com/swanie/portfolio/ui/navigation/NavGraph.kt
app/src/main/java/com/swanie/portfolio/ui/navigation/Routes.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ColorPicker.kt
app/src/main/java/com/swanie/portfolio/ui/settings/PortfolioManagerScreen.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsViewModelFactory.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ThemeStudioScreen.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ThemeViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/settings/WidgetManagerScreen.kt
app/src/main/java/com/swanie/portfolio/ui/theme/Color.kt
app/src/main/java/com/swanie/portfolio/ui/theme/Theme.kt
app/src/main/java/com/swanie/portfolio/ui/theme/ThemeDefaults.kt
app/src/main/java/com/swanie/portfolio/widget/PortfolioWidget.kt
app/src/main/java/com/swanie/portfolio/widget/PortfolioWidgetReceiver.kt
app/src/main/java/com/swanie/portfolio/widget/SparklineDrawUtils.kt
app/src/main/java/com/swanie/portfolio/widget/WidgetClickCallback.kt
app/src/main/java/com/swanie/portfolio/widget/WidgetConfigActivity.kt
app/src/main/java/com/swanie/portfolio/widget/WidgetSyncWorker.kt

--------------------------------------------------
RESOURCES INDEX (res paths)
--------------------------------------------------

app/src/main/res/drawable
app/src/main/res/layout
app/src/main/res/mipmap-anydpi-v26
app/src/main/res/mipmap-hdpi
app/src/main/res/mipmap-mdpi
app/src/main/res/mipmap-xhdpi
app/src/main/res/mipmap-xxhdpi
app/src/main/res/mipmap-xxxhdpi
app/src/main/res/values
app/src/main/res/xml
app/src/main/res/drawable/bg_navy_gradient.xml
app/src/main/res/drawable/ic_launcher_background.xml
app/src/main/res/drawable/ic_launcher_foreground.xml
app/src/main/res/drawable/ic_vault_auth.xml
app/src/main/res/drawable/swanie_foreground.png
app/src/main/res/drawable/swanie_splash.png
app/src/main/res/drawable/swan_launcher_icon.png
app/src/main/res/drawable/swan_splash_icon_wrapper.xml
app/src/main/res/drawable/widget_background.xml
app/src/main/res/layout/glance_default_layout.xml
app/src/main/res/layout/widget_layout_fallback.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
app/src/main/res/mipmap-hdpi/ic_launcher.webp
app/src/main/res/mipmap-hdpi/ic_launcher_round.webp
app/src/main/res/mipmap-mdpi/ic_launcher.webp
app/src/main/res/mipmap-mdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xhdpi/ic_launcher.webp
app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp
app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xxxhdpi/ic_launcher_swanie.png
app/src/main/res/values/colors.xml
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
app/src/main/res/xml/backup_rules.xml
app/src/main/res/xml/data_extraction_rules.xml
app/src/main/res/xml/portfolio_widget_info.xml

--------------------------------------------------
BROWSER AI REMINDERS
--------------------------------------------------
- Follow the LEVEL 4 AI CONTROL HEADER above.
- If you need file contents, request: NEED FILE: path/to/file
- If this document is older than 24 hours, remind the user to rebuild it.
- Prefer minimal safe changes; avoid refactors unless asked.

===== END OF FILE =====
