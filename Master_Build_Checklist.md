## Master Build Checklist

- [x] Widget Manager Preview Fidelity - COMPLETED
  - Absolute Lane Geometry locked to `100dp / Flex / 100dp` for mirror-accurate layout.
  - "Zero-Gap" vertical tuck achieved by line-height override logic (`lineHeight = 11.sp`) on the compact title/value rails.
- [x] Security & Interface Sync (V40.33) - COMPLETED
  - Settings security label rebranded to `LOGIN OPTION` and remaining settings toggles converted to Boutique checkboxes.
  - Smart biometric gating enforced for auto-launch paths while preserving manual override and settings handshake prompts.
  - Ghost startup biometric trigger resolved via synchronized gate logic in `MainActivity`, `UnlockVaultScreen`, and `AuthViewModel`.

- [x] Auth Surface Unification + Session Control (V40.34) - COMPLETED
  - Unified auth state handling to shared activity-scoped `AuthViewModel` across Home/Unlock/NavGraph/Create/Restore/Portfolio Manager.
  - Enforced deterministic login path (`HOME -> UNLOCK_VAULT -> HOLDINGS`) with no accidental direct bypass.
  - Added session timeout controls with persisted settings (`Never`, `15s`, `30s`, `60s`, `5m`, `15m`) and resilient background detection via `onUserLeaveHint`.
  - Added biometric UX hardening:
    - `Allow biometrics for login` gating for login-screen biometric button visibility.
    - Friendly biometric prompt and error messaging.
    - Safety toggle: `Require Password After Biometric Failure` (default ON).
  - Hardened credential verification by normalized matching against username/display name/email.
  - Final polish bundle shipped:
    - Unlock screen visual alignment and copy cleanup.
    - Security verbiage simplification in Settings.
    - App orientation locked to portrait.

- [x] V40.35 Auth Reliability Harness - COMPLETED
  - Added centralized `AuthPolicy` module for credential matching and timeout lock decisions.
  - Added `AuthPolicyTest` unit coverage for identity/password matching and timeout thresholds including `Never`.
  - Wired runtime auth decision points (`MainViewModel`, `MainActivity`) to shared policy methods.
  - Added automated GitHub workflow (`android-auth-safety.yml`) to run auth unit tests + Kotlin compile on push/PR.

- [x] V40.38 Recovery + Password Management + Theme Parity - COMPLETED
  - Shipped secure hint-recovery UX requiring username + email + biometric verification before revealing hint.
  - Added biometric-gated password update flow in Settings with create-account password rules and keyboard-safe popup behavior.
  - Preserved user input on login/create-account correction paths and tightened unlock-screen layout density.
  - Applied user-selected theme colors (including card background) to all touched popup/dialog surfaces for visual consistency.
  - Added follow-up UI polish in Security section (`RESET PASSWORD` placement/alignment) and validated with clean Kotlin compile.

- [x] V40.39 Shared Password Policy + Auth Guardrail Tests - COMPLETED
  - Centralized password strength requirements in `AuthPolicy.evaluatePasswordStrength` to prevent rule drift.
  - Wired both Create Account and Settings password update flows to the same validation contract.
  - Expanded `AuthPolicyTest` with coverage for display-name fallback, whitespace normalization, auth lock gating, and password strength validity.
  - Validation passed via targeted unit test run and debug Kotlin compile.

- [x] V40.40 Localization Foundation (Phase 1) - COMPLETED
  - Added persisted `language_code` preference flow and save path in `ThemePreferences`.
  - Applied app locale at runtime/launch via `AppCompatDelegate.setApplicationLocales` in `MainActivity`.
  - Added language selector in Settings (`System default`, `English`, `Espanol`) with persisted selection.
  - Added Home-screen quick language access (top-left globe icon) with current language label shown beneath icon.
  - Added base localization string resources in `values/strings.xml` and `values-es/strings.xml`.
  - Validation passed with lint clean and debug Kotlin compile success.

- [x] V40.41 Korean Language Extension - COMPLETED
  - Added `Korean` (`ko`) option to both Settings language selector and Home-screen globe menu.
  - Added localized display label mapping for Korean in language pickers.
  - Added Korean resource bundle `values-ko/strings.xml` and extended English/Spanish bundles with `language_korean`.
  - Validation passed with lint clean and debug Kotlin compile success.

- [x] V40.42 Localization Surface Expansion (Core Auth + Settings) - COMPLETED
  - Migrated high-visibility Home, Unlock, Create Account, Restore, Settings, Portfolio Manager, and Theme Manager labels/messages to string resources.
  - Expanded Korean translations for the new keys in `values-ko/strings.xml`.
  - Added immediate language-switch refresh on Home and Settings selectors.
  - Validation passed with lint clean and debug Kotlin compile success.

- [x] V40.43 Locale Runtime Stabilization (Non-AppCompat Host) - COMPLETED
  - Resolved startup crash from AppCompat theme mismatch by restoring `MainActivity` host compatibility.
  - Applied selected locale via runtime resource configuration update path in `MainActivity`.
  - Confirmed Korean text now renders on front/login flows after switch.
  - Validation passed with debug Kotlin compile success.

- [x] V40.44 Localization Surface Expansion (Holdings + Widget + Terms) - COMPLETED
  - Localized additional user-facing strings in `TermsAndConditionsScreen`, `MyHoldingsScreen`, `AmountEntryScreen`, and `WidgetManagerScreen`.
  - Added matching English and Korean string resources for dialogs, button labels, and section/tab text.
  - Validation passed with lint clean and debug Kotlin compile success.

- [x] V40.45 Language Picker Final Cleanup - COMPLETED
  - Removed `System default` from Home and Settings language picker options.
  - Set English (`en`) as the effective/default fallback for language-selection display logic.
  - Removed unused `language_system_default` string keys from English, Spanish, and Korean resources.
  - Validation passed with debug Kotlin compile success.

- [x] V40.46 Top-20 Language Expansion + Translation Feedback Intake - COMPLETED
  - Expanded Home and Settings language pickers to a Top-20 locale list with native-language labels.
  - Added Settings `REPORT TRANSLATION ISSUE` dialog with prefilled email handoff for low-cost user correction intake.
  - Added Terms disclaimer clarifying English as the authoritative language and non-English locales as AI-translated.
  - Added supporting string resources for new language labels and translation feedback UI copy.
  - Validation passed with debug Kotlin compile success.

- [ ] V40.36 Auth Flow Instrumentation Harness - NEXT
  - Add instrumentation coverage for login navigation and biometric success/cancel/failure UI behavior.
  - Add debug-only auth diagnostics surface for state transition tracing.
  - Execute cross-device biometric callback validation and capture OEM behavior notes.
  - Revisit account recovery strategy (post-hint path, fallback options, and anti-lockout policy) after current auth hardening cycle.

- [ ] V40.37 Localization + Billing Blueprint Track - PLANNED
  - Multi-language support foundation:
    - Extract user-facing strings for localization.
    - Add persisted language selection and apply locale on startup.
  - Subscription tiers:
    - Define and enforce `Trial`, `Paid`, and `Premium` feature access matrix.
    - Add safe downgrade/entitlement fallback behavior.
  - Profile/schema integration:
    - Track `language_setting` and `user_tier` in user profile persistence model.
    - Surface tier and language controls in Settings/account UX.
