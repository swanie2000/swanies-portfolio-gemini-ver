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
