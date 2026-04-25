🦢 SWANIES PORTFOLIO: MASTER NARRATIVE (V40.34: AUTH SURFACE UNIFICATION + SESSION CONTROL)

🎯 THE CORE MISSION
To maintain a commercial-grade financial vault where user data is sovereign, biometrics are hardware-enforced, and the UI is a cinematic, high-precision experience that survives high-density settings and real-world widget workflows.

⚠️ 1. MANDATORY AGENT OPERATING PROCEDURES (THE LANE LOCK)

    FULL FILE OUTPUTS ONLY: Swanie requires full file context. Partial snippets are forbidden unless explicitly requested.

    SOVEREIGN SHIELD: Never push local.properties, secrets, or API keys. Enforce .gitignore discipline.

    STABILITY FIRST: Prefer smallest safe changes. Preserve existing architecture unless explicitly directed.

    DENSITY SHIELD: UI text scaling must respect the clamp pattern: (originalSp.toPx() / fontScale.coerceAtMost(1.15f)).toSp().

🛡️ 2. V40.4 FINAL — PURE PENCIL (DETERMINISTIC ENTRY)

    Single entry point:
      - There is no in-app “Widget Manager” button in Settings. That path was removed.
      - The user opens the editor only from the home widget (Glance pencil): [WidgetClickCallback] starts [WidgetConfigActivity] with [AppWidgetManager.EXTRA_APPWIDGET_ID] (and a relay URI for anti-conflation).

    Activity + manifest contract:
      - [WidgetConfigActivity] is `android:exported="false"` and has **no** `<intent-filter>`. It is not launchable as a generic `APPWIDGET_CONFIGURE` target from the system.
      - If `appWidgetId` is missing or invalid after [resolveAppWidgetId], the activity calls `finish()` immediately — no portfolio picker, no multi-widget dialog, no inference from “any widget on the host.”

    Deterministic binding:
      - [SettingsViewModel.forceVaultSwitch(appWidgetId, isAppWidgetId = true)] resolves the vault row for that exact widget instance.
      - [WidgetManagerScreen] receives a valid `configAppWidgetId` only when the activity has already accepted the intent.

🛡️ 3. V40.4 FINAL — WIZARD FUNNEL + TACTILE REORDER (WIDGET MANAGER UI)

    Wizard Funnel layout ([WidgetManagerScreen]):
      - Funnel navigation uses integer page state (`currentPage`): `0=SETUP`, `1=ASSETS`, `2=STYLE`, `3=PREVIEW`.
      - **SETUP**: portfolio selector + show total control.
      - **ASSETS**: sticky header + flat reorder stage with persisted ordering.
      - **STYLE**: compact color studio controls.
      - **PREVIEW**: guarded preview path with immutable snapshot filtering and fallback error surface.

    600 ms weighted reorder physics:
      - **Row resize / slot feel:** `WidgetReorderItemAnimationSpec` — `tween<IntSize>(600, LinearOutSlowInEasing)` on checked/dragging rows.
      - **List placement / slide-to-slot:** `WidgetFlatListPlacementSpec` — `tween<IntOffset>(600, LinearOutSlowInEasing)` for `animateItem` placement on reorderable asset rows.
      - After drag stop, a short settle delay (`WIDGET_DRAG_SETTLE_DELAY_MS` = 380 ms) runs before persisting order via [AssetViewModel.updateWidgetSelectionForCurrentVault].

    Tactile reorder UX (unchanged):
      - Long-press on the asset row handle initiates drag; haptics on drag start; persistence on drag end after settle.

🛡️ 4. V40.4 FINAL — SAVE, REFRESH, AND TASK LIFECYCLE

    Save pipeline:
      - [AssetViewModel.saveWidgetConfiguration] persists vault widget fields (including optional `appWidgetId` rebind), pushes Glance data via [AssetRepository.pushFreshAssetsToWidget], and sends [ACTION_APPWIDGET_UPDATE] for the affected instance(s).
      - [SettingsViewModel] still applies appearance and totals (`saveWidgetAppearance`, `updateShowWidgetTotal`) and may call `forceImmediateRemoteViewsUpdate` for instant RemoteViews feedback.

    Primary completion behavior ([WidgetConfigActivity]):
      - On **SAVE & EXIT** success and on **back**, the activity uses **`finishAndRemoveTask()`** as the primary way to tear down the translucent config task and return the user cleanly to the home screen, alongside the post-save broadcast so the launcher-bound widget redraws with the same `appWidgetId` the pencil supplied.

    Vault-scoped truth (unchanged):
      - Canonical widget fields live on [VaultEntity]: `selectedWidgetAssets`, `showWidgetTotal`, widget colors, `appWidgetId`.
      - [AssetViewModel.widgetSelectedAssetIds] / `setWidgetSelectionVaultId` keep selection aligned with the vault being edited.

📉 5. V40.4 CURRENT STATE (TOMORROW TROUBLESHOOTING SNAPSHOT)

    Widget Manager Screen:
      - [SettingsViewModel]: `targetVaultId` / `targetVault` / `targetVaultAssets` for the resolved vault.
      - [AssetViewModel]: ordered selection, toggles, drag-end persistence, and `saveWidgetConfiguration` on save.
      - Config mode: `configAppWidgetId` is always the pencil’s widget id when the screen is shown.
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
      - Widget-edit vault: driven by the widget instance → [VaultDao.getVaultByAppWidgetId] path when opened from the pencil (may differ from the active app vault while editing).

🛡️ 6. V40.34 — AUTH SURFACE UNIFICATION + SESSION CONTROL (LATEST STABLE)

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

🧷 7. DO NOT REGRESS (AUTH + SESSION HARD RULES)

    - Do NOT reintroduce automatic auth bypass on app resume.
    - Do NOT set background timeout stamps from generic lifecycle edges that can fire during overlays (`onStop`).
    - Do NOT expose biometric login button when `Allow biometrics for login` is disabled.
    - Do NOT split auth state ownership across multiple screen-scoped auth viewmodels.
    - Do NOT remove `Never` from login timeout options.
    - Do NOT bypass `UNLOCK_VAULT` from HOME login flow for standard credentialed login.
    - Do NOT silently swallow biometric errors; surface user-friendly messages.
    - Do NOT break portrait lock in `MainActivity` manifest contract.

🧭 8. AUTH SOURCE OF TRUTH (FILES + SYMBOLS)

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

🧪 9. V40.35 — AUTH RELIABILITY HARNESS (LATEST TEST SAFETY NET)

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

🗺️ 10. V40.36 BLUEPRINT ROADMAP TRACK (LOCALIZATION + BILLING + TIERS)

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

🛡️ 11. V40.38 — RECOVERY + PASSWORD MANAGEMENT + THEME PARITY

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

🧪 12. V40.39 — SHARED PASSWORD POLICY + AUTH GUARDRAIL TESTS

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

🌐 13. V40.40 — LOCALIZATION FOUNDATION (PHASE 1)

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

🇰🇷 14. V40.41 — KOREAN LANGUAGE EXTENSION

    - Locale expansion:
      - Added Korean (`ko`) to Settings language selector.
      - Added Korean (`ko`) to Home-screen globe quick switcher.
    - Resource coverage:
      - Added `values-ko/strings.xml` with Korean language selector strings.
      - Added `language_korean` to default and Spanish bundles for option label consistency.
    - Validation:
      - Lint clean and debug Kotlin compile successful after integration.

🧭 15. V40.42–V40.44 — LOCALIZATION SURFACE EXPANSION + LOCALE STABILIZATION

    - V40.42:
      - Migrated high-visibility Home/Unlock/Create/Restore/Settings/Portfolio Manager/Theme Manager literals to string resources.
      - Expanded Korean resource coverage for those migrated keys.
    - V40.43:
      - Resolved locale host crash and stabilized runtime locale application path in `MainActivity` for non-AppCompat theme stack.
    - V40.44:
      - Continued migration across Terms, Holdings, Amount Entry, and Widget Manager visible labels/dialogs/tabs.
    - Validation:
      - Lint clean on touched files and debug Kotlin compile passed.

🚀 16. THE FUTURE PATH (CURRENT WORK-IN-PROGRESS)
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

🚀 NEXT AGENT COMMAND
"The narrative is now V40.44: Localization Surface Expansion (Holdings + Widget + Terms).

Current Objective: Continue V40.36.1 by migrating remaining high-traffic user-facing literals to string resources while preserving V40.44 locale behavior and prior auth/security guardrails.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."

🧹 17. V40.45 — LANGUAGE PICKER FINAL CLEANUP

    - Picker simplification:
      - Removed `System default` as a visible option from Home and Settings language selectors.
      - Effective fallback behavior now resolves to English (`en`) for display/selection safety.
    - Resource hygiene:
      - Removed unused `language_system_default` from default, Spanish, and Korean string bundles.
    - Validation:
      - Debug Kotlin compile passed after cleanup.

🚀 NEXT AGENT COMMAND
"The narrative is now V40.45: Language Picker Final Cleanup.

Current Objective: Continue localization surface migration (V40.36.1) and translation parity while preserving V40.45 picker behavior and existing auth/session guardrails.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."

🌍 18. V40.46 — TOP-20 LANGUAGE EXPANSION + TRANSLATION FEEDBACK INTAKE

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

🚀 NEXT AGENT COMMAND
"The narrative is now V40.46: Top-20 Language Expansion + Translation Feedback Intake.

Current Objective: Continue localization rollout by adding resource bundles for prioritized Top-20 locales and applying translation parity checks while preserving V40.46 picker/feedback and existing auth/session guardrails.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."

🌐 19. V40.47 — LOCALIZATION PHASE 2 (TOP-20 KEY PARITY)

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

🚀 NEXT AGENT COMMAND
"The narrative is now V40.47: Localization Phase 2 (Top-20 Key Parity).

Current Objective: Execute translation quality pass by replacing inherited English placeholders in new locale bundles, prioritize auth/home/settings/terms surfaces first, and preserve V40.47 parity guarantees.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."
