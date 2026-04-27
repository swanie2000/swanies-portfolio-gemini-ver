## Standard Operating Procedure (SOP)

For every completed milestone, the agent must automatically execute:

1. Narrative Update: Log the version number, specific technical wins (logic and UI), and any ghosts or bugs resolved.
2. Checklist Maintenance: Update `Master_Build_Checklist.md` to reflect the latest Golden Vault state.
3. Projected Path: Update the Next Phase section based on current conversation intent.
4. Terminal Handover: Provide a formatted git command block with `git add .`, `git commit -m "[Version]: [Summary]"`, and `git push`.

---

## V40.22 - The High-Fidelity Mirror

Release recorded: V40.22 "The High-Fidelity Mirror".

- Widget preview lane system upgraded from weighted lanes to Absolute Lane Geometry: `100dp / Flex / 100dp` for 1:1 home-screen parity.
- Resolved the "Infinity Height" crash by enforcing single-owner scrolling in the preview flow.
- Synced 24h trend color logic with sparkline rendering so percentage text and line color remain visually consistent.

[2026-04-23] V40.33: Security & Interface Sync

    Rebrand: Renamed "Sovereign Vault Lock" to "LOGIN OPTION" across the UI.

    Interface: Converted all remaining "Pill" toggles in Settings to Boutique Checkboxes (Color.Yellow/Black).

    Biometric Logic: Implemented "Smart Gating."

        Auto-launch paths (MainActivity onResume, UnlockVaultScreen) now strictly respect the isBiometricEnabled setting.

        Manual overrides (Login button, Settings toggle) remain ungated to allow user-initiated authentication.

    Stability: Resolved "Always-On" biometric ghost trigger by synchronizing logic between MainActivity, UnlockVaultScreen, and AuthViewModel.

### Next Phase (Projected Path)

- V40.34 "Auth Surface Unification": consolidate biometric trigger UX labels and action semantics across Home, Unlock, and Settings surfaces.
- Add a regression pass for biometric states (enabled, disabled, canceled prompt, failed prompt) covering launch, resume, and manual login.
- Validate navigation/state continuity so Authenticated transitions remain deterministic across lifecycle edges.

---

## V40.34 - Auth Surface Unification + Session Control

Release recorded: V40.34 "Auth Surface Unification + Session Control".

- Resolved login lockout loops by unifying auth state ownership across screens to a shared activity-scoped `AuthViewModel`.
- Rewired login flow to always route `HOME -> UNLOCK_VAULT -> HOLDINGS`, removing unintended direct bypasses.
- Added startup/login security controls:
  - `Allow biometrics for login` toggle now strictly controls biometric button visibility on login screen.
  - Login timeout added with options `Never`, `15s`, `30s`, `60s`, `5m`, `15m`.
  - Background timeout enforcement now uses true user-leave signal (`onUserLeaveHint`) to avoid false lockouts during system overlays.
- Hardened authentication behavior:
  - Password verification now supports normalized username, display name, or email identity matching.
  - Biometric failures now provide friendly user-facing messages and optional password-first recovery flow.
  - Added `Require Password After Biometric Failure` safety toggle (default ON).
- Completed UI/UX polish:
  - Unlock screen rebrand and structure polish (`PORTFOLIO LOCKED`, swan-first header).
  - Biometric CTA updated to `USE BIOMETRICS`.
  - Security setting copy simplified for plain-language readability.
  - App orientation locked to portrait via manifest.
- Stability validation:
  - Repeated lint checks passed on touched files.
  - Repeated Kotlin compile checks passed after each auth/security increment.

### Next Phase (Projected Path)

- V40.35 "Auth Reliability Harness": add targeted regression tests for credential success/failure, biometric success/cancel/fail, and timeout boundary behavior (`timeout - 1s`, `timeout + 1s`, `Never`).
- Add lightweight auth diagnostics mode (developer-only) to surface active auth state and session timeout decisions for faster field debugging.
- Validate cross-device biometric behavior matrix (Pixel, Samsung, emulator) to confirm consistent callback handling and error wording.
- Optional UX refinement: make timeout selector more explicit with labeled tick marks or chips while preserving current behavior.

---

## V40.35 - Auth Reliability Harness

Release recorded: V40.35 "Auth Reliability Harness".

- Added centralized auth policy module `AuthPolicy` for deterministic, reusable credential and timeout decision logic.
- Refactored runtime auth checks to use shared policy methods in `MainViewModel` and `MainActivity` (reduced drift between production behavior and test expectations).
- Added focused unit coverage in `AuthPolicyTest` for:
  - username/displayName/email credential matching,
  - password failure behavior,
  - timeout boundary logic including `Never`.
- Added GitHub CI workflow `.github/workflows/android-auth-safety.yml` to automatically run auth safety unit tests and debug Kotlin compile on push/PR to `main`.
- Validation: local `testDebugUnitTest` and `:app:compileDebugKotlin` pass.

### Next Phase (Projected Path)

- Expand harness into instrumentation flow tests for `HOME -> UNLOCK_VAULT -> HOLDINGS` and biometric success/cancel/failure UI states.
- Add a lightweight developer diagnostics panel for auth state transitions and timeout decisions (debug-only).
- Run cross-device biometric callback verification matrix and log OEM-specific behavior notes.

---

## V40.36 - Blueprint Roadmap Track (Localization + Billing + Tiered Access)

Release planning recorded: V40.36 "Blueprint Roadmap Track".

- Added roadmap priority for multi-language support:
  - externalized localized strings,
  - persisted language selection,
  - startup locale application path.
- Added roadmap priority for billing and structured subscription tiers:
  - `Trial` (limited entry),
  - `Paid` (standard full-access),
  - `Premium` (advanced/elite feature set).
- Added roadmap priority for entitlement and profile persistence:
  - plan to track `language_setting` and `user_tier` in profile/domain schema,
  - enforce tier-gated feature behavior with safe downgrade handling.
- Sequencing guidance:
  1) localization infrastructure,
  2) tier/entitlement matrix,
  3) billing integration + persistence wiring.

### Next Phase (Projected Path)

- V40.36.1 Localization Foundation: extract UI strings, add language selector, persist and apply locale on launch.
- V40.36.2 Subscription Tier Matrix: define trial/paid/premium feature boundaries and entitlement checks.
- V40.36.3 Billing + Profile Integration: wire billing provider state into `user_tier` and account settings UX.

---

## V40.38 - Recovery + Password Management + Theme Parity

Release recorded: V40.38 "Recovery + Password Management + Theme Parity".

- Hardened account recovery flow:
  - `Forgot Password` now requires username + email + biometric verification to reveal stored hint.
  - Recovery dialog completes cleanly via `DONE` back to login context without password reset bypass.
- Added secure password management in Settings:
  - New password update flow requires current password + biometric confirmation.
  - Enforced same password policy as create-account (`8+`, uppercase, number, symbol, confirm match).
  - Moved password flow into popup to keep Settings compact and improved keyboard handling/scroll behavior.
- UX reliability improvements:
  - Preserved login and create-account form input during correction/navigation.
  - Tightened unlock screen layout and embedded compact `FORGOT PASSWORD?` affordance near password entry.
- Theme fidelity pass across touched auth/settings surfaces:
  - Replaced hardcoded colors with user-selected theme colors.
  - Updated popup/dialog containers to use user-selected card background color for consistency.
- Validation:
  - Repeated lint checks and Kotlin compile checks passed during iterative updates.

### Next Phase (Projected Path)

- V40.36.1 Localization Foundation: extract UI strings, add language selector, persist and apply locale on launch.
- V40.36.2 Subscription Tier Matrix: define trial/paid/premium feature boundaries and entitlement checks.
- V40.36.3 Billing + Profile Integration: wire billing provider state into `user_tier` and account settings UX.

---

## V40.39 - Shared Password Policy + Auth Guardrail Tests

Release recorded: V40.39 "Shared Password Policy + Auth Guardrail Tests".

- Unified password validation policy:
  - Added centralized `AuthPolicy.evaluatePasswordStrength(...)` and `PasswordStrength` contract.
  - Replaced duplicated password-rule logic in Create Account and Settings password update with shared policy usage.
- Expanded auth guardrail tests:
  - Added coverage for display-name fallback when username is blank.
  - Added coverage for whitespace normalization in password credential checks.
  - Added coverage confirming resume-timeout lock never triggers for unauthenticated users.
  - Added coverage for password strength validity/invalidity and normalization behavior.
- Outcome:
  - Reduced chance of behavior drift between account creation and password update flows.
  - Strengthened regression detection for auth policy changes before runtime.
- Validation:
  - `AuthPolicyTest` targeted run passed.
  - `:app:compileDebugKotlin` passed.

### Next Phase (Projected Path)

- V40.36.1 Localization Foundation: extract UI strings, add language selector, persist and apply locale on launch.
- V40.36.2 Subscription Tier Matrix: define trial/paid/premium feature boundaries and entitlement checks.
- V40.36.3 Billing + Profile Integration: wire billing provider state into `user_tier` and account settings UX.

---

## V40.40 - Localization Foundation (Phase 1)

Release recorded: V40.40 "Localization Foundation (Phase 1)".

- Added persisted language preference foundation:
  - Introduced `language_code` in `ThemePreferences` with flow + save path.
  - Exposed language state via `MainViewModel` and `SettingsViewModel`.
- Applied locale globally:
  - `MainActivity` now applies selected locale using `AppCompatDelegate.setApplicationLocales(...)`.
  - Supports `system`, `en`, and `es` language tags.
- Added user language controls:
  - Settings interface now includes language selector dropdown (`System default`, `English`, `Espanol`).
  - Home screen now provides immediate language access via top-left globe menu.
  - Globe control was refined with larger icon and current selected language label beneath it.
- Added initial localization resources:
  - Added language strings in `values/strings.xml`.
  - Added Spanish variant file `values-es/strings.xml`.
- Validation:
  - Lint checks stayed clean on touched files.
  - `:app:compileDebugKotlin` passed after integration.

### Next Phase (Projected Path)

- V40.36.1 Localization Surface Expansion: migrate high-visibility auth/settings/home user-facing literals to string resources.
- V40.36.2 Subscription Tier Matrix: define trial/paid/premium feature boundaries and entitlement checks.
- V40.36.3 Billing + Profile Integration: wire billing provider state into `user_tier` and account settings UX.

---

## V40.41 - Korean Language Extension

Release recorded: V40.41 "Korean Language Extension".

- Expanded localization selector coverage:
  - Added Korean (`ko`) as selectable locale in Settings language dropdown.
  - Added Korean (`ko`) to Home-screen globe quick switcher for pre-login selection.
- Added language label parity:
  - Added `language_korean` in default and Spanish bundles so option labeling remains consistent across active locales.
- Added Korean string resources:
  - Introduced `app/src/main/res/values-ko/strings.xml` with language selector labels in Korean (`언어`, `시스템 기본값`, `영어`, `스페인어`, `한국어`).
- Validation:
  - Lint checks stayed clean on touched files.
  - `:app:compileDebugKotlin` passed after integration.

### Next Phase (Projected Path)

- V40.36.1 Localization Surface Expansion: migrate high-visibility auth/settings/home user-facing literals to string resources.
- V40.36.2 Subscription Tier Matrix: define trial/paid/premium feature boundaries and entitlement checks.
- V40.36.3 Billing + Profile Integration: wire billing provider state into `user_tier` and account settings UX.

---

## V40.42 - Localization Surface Expansion (Core Auth + Settings)

Release recorded: V40.42 "Localization Surface Expansion (Core Auth + Settings)".

- Migrated core auth/settings surfaces to string resources:
  - Home, Unlock, Create Account, Restore, Settings, Portfolio Manager, and Theme Manager.
- Expanded Korean resource coverage in `values-ko/strings.xml` for new labels/messages.
- Added explicit refresh behavior after language selection to apply locale changes immediately in-session.
- Validation:
  - Lint checks stayed clean on touched files.
  - `:app:compileDebugKotlin` passed after integration.

---

## V40.43 - Locale Runtime Stabilization (Non-AppCompat Host)

Release recorded: V40.43 "Locale Runtime Stabilization (Non-AppCompat Host)".

- Diagnosed and resolved startup crash caused by switching `MainActivity` host type without an AppCompat theme stack.
- Restored compatible host activity and applied selected locale using runtime resource configuration updates in `MainActivity`.
- Confirmed Korean strings now render on front/login flows when `ko` is selected.
- Validation:
  - `:app:compileDebugKotlin` passed after crash fix and locale-path update.

---

## V40.44 - Localization Surface Expansion (Holdings + Widget + Terms)

Release recorded: V40.44 "Localization Surface Expansion (Holdings + Widget + Terms)".

- Localized additional high-visibility UI surfaces:
  - `TermsAndConditionsScreen` title/back label,
  - `MyHoldingsScreen` onboarding empty-state + delete dialog,
  - `AmountEntryScreen` amount input/save/discard dialog labels,
  - `WidgetManagerScreen` preview/loading/tab/action labels.
- Added aligned English + Korean string keys for all new migrated literals.
- Validation:
  - Lint checks stayed clean on touched files.
  - `:app:compileDebugKotlin` passed after integration.

---

## V40.45 - Language Picker Final Cleanup

Release recorded: V40.45 "Language Picker Final Cleanup".

- Removed `System default` from language picker option lists on both Home (globe menu) and Settings.
- Kept behavior deterministic by using English (`en`) as the effective/default fallback label path where needed.
- Removed unused `language_system_default` resource keys from:
  - `app/src/main/res/values/strings.xml`,
  - `app/src/main/res/values-es/strings.xml`,
  - `app/src/main/res/values-ko/strings.xml`.
- Validation:
  - `:app:compileDebugKotlin` passed after cleanup.

---

## V40.46 - Top-20 Language Expansion + Translation Feedback Intake

Release recorded: V40.46 "Top-20 Language Expansion + Translation Feedback Intake".

- Expanded language support controls to a Top-20 locale list on both:
  - Home globe quick-switch menu,
  - Settings language selector.
- Added native-language labels for all Top-20 options in `values/strings.xml` for cleaner picker readability.
- Added user-driven translation feedback intake in Settings:
  - New `REPORT TRANSLATION ISSUE` action opens a keyboard-safe feedback dialog.
  - Submit action launches prefilled email handoff for quick issue reporting without backend overhead.
- Added Terms and Conditions disclaimer confirming:
  - English is the authoritative/master legal and product language,
  - Non-English locales may be AI-translated and can contain wording differences.
- Validation:
  - `:app:compileDebugKotlin` passed after integration.

---

## V40.47 - Localization Phase 2 (Top-20 Key Parity)

Release recorded: V40.47 "Localization Phase 2 (Top-20 Key Parity)".

- Added new locale bundles for Top-20 rollout:
  - `values-ar`, `values-de`, `values-fr`, `values-hi`, `values-id`, `values-it`, `values-ja`, `values-nl`, `values-pl`, `values-pt-rBR`, `values-ru`, `values-th`, `values-tr`, `values-uk`, `values-vi`, `values-zh-rCN`, `values-zh-rTW`.
- Completed full key parity pass:
  - Backfilled each new locale file to include every key from `values/strings.xml`.
  - Preserved translated high-visibility strings from Phase 1 and safely inherited remaining keys for stability.
- Runtime impact:
  - Eliminates missing-key locale gaps across the Top-20 language selector paths.
  - Keeps app behavior deterministic while enabling iterative translation quality upgrades over time.
- Validation:
  - `:app:compileDebugKotlin` passed after parity rollout.

---

## V40.48 - Localization Quality Pass (Batches A-D)

Release recorded: V40.48 "Localization Quality Pass (Batches A-D)".

- Executed iterative quality passes across all 17 new locale bundles to improve user-facing translation quality on high-impact paths.
- Batch A (auth core):
  - Updated `action_forgot_password`, `status_verifying`, and `action_done`.
- Batch B (recovery/auth messaging):
  - Updated `msg_auth_failed_try_again`, `msg_incorrect_name_or_password`, `recover_access_title`, `recover_access_description`, and `action_verify_show_hint`.
- Batch C (create + security):
  - Updated `create_account_title`, `label_email_address`, `label_confirm_password`, `settings_security`, `settings_login_option`, and `settings_login_option_subtitle`.
- Batch D (terms + feedback intake):
  - Updated `terms_title`, translation-feedback CTA/title/subtitle/submit/chooser/no-email strings.
- Outcome:
  - Top-20 locale experience now has stronger native phrasing across login, recovery, create-account, security settings, and translation-feedback surfaces.
- Validation:
  - `:app:compileDebugKotlin` passed after each batch.
  - Lint remained clean on touched resource files.

---

## V40.57 - RevenueCat Stabilization + Access Enforcement

Release recorded: V40.57 "RevenueCat Stabilization + Access Enforcement".

- Hardened RevenueCat identity and restore reliability:
  - Added app-user sync hooks so entitlement checks/restore/purchase paths align to a stable profile identity.
  - Updated restore outcome handling to surface deterministic user-facing results (`already active`, `restored`, `no entitlement`, `failed`) instead of ambiguous success states.
- Improved monetization UX architecture:
  - Moved paywall and diagnostics into dedicated routes/screens (`UPGRADE TO PRO NOW`, `TEST INFO`) and simplified Settings entry points.
  - Added a lifecycle test checklist to the in-app `TEST INFO` page for rapid sandbox validation loops.
- Enforced Pro boundaries more strictly:
  - Blocked Free/expired users from side-to-side multi-portfolio swipe access in Holdings while preserving single-vault access.
  - Added contextual Holdings upsell banner with direct upgrade route.
- Refined auth surface behavior:
  - Added automatic biometric prompt sequencing after Home animation/login reveal and on Unlock screen when biometric login is enabled.
  - Preserved password-first behavior when biometric login is disabled.
- Quality/flow polish:
  - Required explicit plan selection before enabling `UPGRADE TO PRO`.
  - Preserved Settings scroll position when navigating to/from sub-pages.
- Validation:
  - Repeated `:app:compileDebugKotlin` passes after each integration cluster.

### Next Phase (Projected Path)

- V40.58 Monetization Release Hardening:
  - Replace any remaining test-oriented copy with release-safe language.
  - Add lightweight analytics hooks for paywall views/select/purchase/restore outcomes.
  - Finalize production key rotation/secret handling checklist before store release.

---

## V40.58 - Analytics Premium Experience + Upgrade Flow UX

Release recorded: V40.58 "Analytics Premium Experience + Upgrade Flow UX".

- Expanded Analytics into a mixed free + premium model instead of hard-locking the entire screen:
  - Free users can access core chart pages (`START`, `PIE`, `DONUT`, `BAR`).
  - Premium pages (`RISK`, `ATTRIBUTION`, `REBALANCE`) now act as large in-context Pro previews with upgrade paths.
- Reworked Analytics navigation and readability:
  - Added swipe-first page architecture with synchronized top labeling and compacted chart/list density for smaller screens.
  - Added dedicated `START` instruction page and shortened attribution header naming to avoid title overflow.
- Standardized monetization UI styling under a shared Pro visual system:
  - Added `ProPalette` design tokens and `ProLockBadge` reusable component.
  - Applied black/white/yellow Pro treatment across paywall/test/pro-preview surfaces for consistent premium branding.
- Improved upgrade flow continuity and back navigation behavior:
  - Added immediate back action visibility on `UPGRADE TO PRO` screen so users can safely return without dead-end feel.
  - Finalized Pro analytics ad panels to show a single full-width `UPGRADE TO PRO` CTA (removed temporary `MAYBE LATER` variant).
- Stabilized monetization interaction details:
  - Kept plan-selection-required purchase behavior.
  - Preserved prior restore/status reliability and compile stability while iterating UI.
- Validation:
  - Repeated `:app:compileDebugKotlin` checks remained successful after each major analytics/paywall iteration.

### Current Status (End of Session)

- RevenueCat wiring and Pro gating are operational.
- Settings now routes cleanly to dedicated upgrade/test pages.
- Analytics free/premium split is live with premium teaser pages and direct upgrade CTA.
- Upgrade screen now includes prominent back navigation for safer user flow.
- Codebase is in a compile-green state after latest UI adjustments.

### Next Phase (Projected Path)

- V40.59 Monetization Conversion + Trust Polish:
  - Add lightweight funnel telemetry for Analytics Pro panels and Upgrade screen (`view`, `cta tap`, `purchase success/fail`, `restore result`).
  - Run final copy pass for all monetization CTAs/dialogs (reduce ambiguity, keep tone consistent).
  - Add small UX safeguards for upgrade return paths (preserve source context where applicable).
  - Perform on-device pass for free user journey, expired Pro journey, and restore journey.

---

## V40.59 - Analytics Premium Live Engines + Modular Refactor

Release recorded: V40.59 "Analytics Premium Live Engines + Modular Refactor".

- Completed premium analytics feature depth and production polish:
  - Added/refined live Pro engines for `RISK`, `ATTRIBUTION`, `REBALANCE`, and `SCENARIOS`.
  - Included scenario presets, animated metric transitions, and stronger small-screen overflow protection.
- Finalized free vs Pro analytics architecture:
  - Free users remain on `START`, `PIE`, `DONUT`, `BAR`.
  - Pro users receive live premium engines; non-Pro users see actionable teaser pages with direct upgrade paths.
- Completed structural refactor of analytics premium code:
  - Split premium content into focused files:
    - `AnalyticsProUpsellPages.kt`
    - `AnalyticsProLivePages.kt`
    - `AnalyticsProUiComponents.kt`
  - Removed the old combined premium file to reduce maintenance risk and improve iteration speed.
- Preserved UI fidelity during split:
  - Restored shared-card spacing/typography details (`divider`, `lineHeight`, `chip letter spacing`) and fixed a subtle spacing regression found during hardening pass.
- Validation:
  - `:app:compileDebugKotlin` passed after each refactor phase and final polish.
  - Lint checks remained clean on newly split premium files.

### Current Status (End of Session)

- RevenueCat monetization and entitlement gating are stable.
- Analytics premium stack is now both feature-rich and modularized for safer future changes.
- Upgrade flows remain consistent with clear return paths and no dead-end behavior.
- Project is compile-green with no lint issues on touched premium analytics files.

### Next Phase (Projected Path)

- V40.60 Monetization Conversion + Trust Polish:
  - Add lightweight funnel telemetry (`view`, `select package`, `cta tap`, `purchase outcome`, `restore outcome`).
  - Complete end-to-end on-device journey validation for Free, Active Pro, and Expired Pro states.
  - Tighten monetization copy consistency across paywall, analytics upsell, and restore messaging.
  - Add/verify regression checks for analytics tab behavior under both entitlement states.

---

## V40.59.1 - Analytics Hub Navigation Polish + Free Widget Upsell Flow

Release recorded: V40.59.1 "Analytics Hub Navigation Polish + Free Widget Upsell Flow".

- Refined Analytics hub navigation and presentation:
  - Added `QUICK JUMP` page links on hub page with direct pager navigation.
  - Removed `START` from quick-jump list and replaced the top `START` title state with a fixed swan + fixed one-line title (`ANALYTICS HUB`).
  - Simplified hub instruction copy to a single line (`Swipe left or right to move.`).
  - Added stronger pager fade transitions while swiping for clearer page-to-page motion feedback.
- Updated Analytics back-arrow behavior:
  - Sub-pages now return to Analytics main hub page.
  - Hub-page back arrow returns to Holdings list.
- Reworked free widget UX to improve conversion clarity:
  - Added a small in-widget Pro banner for free users (`PRO: Customize widget colors and layout`) and centered its text.
  - Replaced the fast free-edit auto-pass-through with a deliberate black/white/yellow Pro-styled stop screen.
  - Added explicit choices on free edit path:
    - `UPGRADE TO PRO`
    - `CONTINUE WITH FREE WIDGET` (default 3-asset, non-customizable setup).
- Validation:
  - Iterative `:app:compileDebugKotlin` passes remained green after each UX/navigation/widget adjustment.
  - Lint stayed clean on touched files.

### Current Status (End of Session)

- Analytics hub navigation is now faster and clearer (fixed header identity, quick page jumps, context-aware back behavior).
- Free widget flow now communicates Pro customization value without blocking free default setup.
- Widget and analytics UI adjustments are compile-stable and ready for on-device entitlement-expiry verification.

### Next Phase (Projected Path)

- V40.60 Monetization Conversion + Trust Polish:
  - Add funnel telemetry (`view`, `select package`, `upgrade tap`, `purchase outcome`, `restore outcome`).
  - Execute end-to-end on-device matrix for Free, Active Pro, and Expired Pro journeys (including widget edit entry).
  - Finalize monetization copy consistency pass across paywall, analytics upsell, and widget-upgrade surfaces.
  - Add regression checks for analytics hub navigation rules and widget free-vs-pro behavior.
