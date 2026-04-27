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

- [x] V40.47 Localization Phase 2 - Top-20 Key Parity - COMPLETED
  - Added 17 new locale resource directories for Top-20 coverage (`ar`, `de`, `fr`, `hi`, `id`, `it`, `ja`, `nl`, `pl`, `pt-BR`, `ru`, `th`, `tr`, `uk`, `vi`, `zh-CN`, `zh-TW`).
  - Completed key parity across all new locale files against the English baseline to eliminate missing-key fallback gaps.
  - Preserved translated high-visibility strings while safely backfilling remaining keys for runtime stability.
  - Validation passed with debug Kotlin compile success.

- [x] V40.48 Localization Quality Pass (Batches A-D) - COMPLETED
  - Improved auth-critical strings across all 17 new locale bundles (`FORGOT PASSWORD?`, `VERIFYING...`, `DONE`).
  - Added translated recovery/auth error and hint-flow labels/messages for all 17 locale bundles.
  - Improved Create Account + Settings Security copy and translated Terms/translation-feedback UI labels.
  - Validation passed with debug Kotlin compile success and no lint issues.

- [x] V40.56 RevenueCat Monetization Wiring + Pro Gating - COMPLETED
  - Integrated RevenueCat SDK bootstrap with safe no-key fallback behavior.
  - Added centralized monetization boundary (`MonetizationManager`) for entitlement refresh, offerings fetch, purchase, and restore.
  - Gated Theme Manager, Portfolio Manager, Analytics, and Widget configuration behind Pro entitlement.
  - Added reusable Pro paywall flow and Settings monetization diagnostics surface.

- [x] V40.57 RevenueCat Stabilization + Access Enforcement - COMPLETED
  - Added app-user identity synchronization before restore/purchase/offerings flows to reduce anonymous entitlement drift.
  - Upgraded restore UX to deterministic result states (`already active`, `restored`, `no entitlement`, `failed`).
  - Split monetization UX into dedicated routes/screens (`UPGRADE TO PRO NOW`, `TEST INFO`) with Settings entry actions.
  - Enforced Pro gating on holdings multi-portfolio swipe access while preserving single-vault access for Free users.
  - Added Free-user Holdings upsell banner linking directly to upgrade flow.
  - Added Home/Unlock biometric auto-prompt sequencing when biometric login is enabled.
  - Added Settings scroll-position persistence across sub-page navigation.
  - Validation passed via repeated debug Kotlin compile checks.

- [x] V40.58 Analytics Premium Experience + Upgrade Flow UX - COMPLETED
  - Re-architected Analytics into mixed-access pages:
    - Free pages: `START`, `PIE`, `DONUT`, `BAR`.
    - Premium teaser pages: `RISK`, `ATTRIBUTION`, `REBALANCE` with upgrade paths.
  - Added swipe-first analytics flow and compact chart/list presentation for better vertical fit and interaction clarity.
  - Added premium visual system consistency:
    - Shared `ProPalette` token usage.
    - Reusable `ProLockBadge` treatment across locked surfaces.
  - Upgraded Pro preview content to larger in-page ad panels for clearer premium value presentation.
  - Improved Upgrade screen usability by moving back action to immediate visibility under primary upgrade CTA.
  - Finalized premium panel CTA behavior to single full-width `UPGRADE TO PRO` action (temporary `MAYBE LATER` removed).
  - Validation passed with repeated debug Kotlin compile success during iterative UI refinements.

- [x] V40.59 Analytics Premium Live Engines + Modular Refactor - COMPLETED
  - Completed/refined live premium analytics engines for `RISK`, `ATTRIBUTION`, `REBALANCE`, and `SCENARIOS`.
  - Added/retained premium interaction polish: animated number transitions, scenario presets, and small-device overflow hardening.
  - Fully modularized premium analytics code by splitting into:
    - `AnalyticsProUpsellPages.kt`
    - `AnalyticsProLivePages.kt`
    - `AnalyticsProUiComponents.kt`
  - Removed legacy monolithic premium file (`AnalyticsPremiumSections.kt`) after migration.
  - Performed hardening pass to preserve visual fidelity after split (shared spacing/typography details restored).
  - Validation passed with clean lint and repeated debug Kotlin compile success.

- [x] V40.59.1 Analytics Hub Navigation Polish + Free Widget Upsell Flow - COMPLETED
  - Added actionable Analytics hub quick-jump navigation and removed `START` from quick-jump options.
  - Reworked Analytics header behavior on hub page to fixed swan + fixed one-line hub title; removed duplicate scrolling swan/title.
  - Increased pager fade intensity for more noticeable transition feedback during horizontal swipes.
  - Added context-aware Analytics back navigation:
    - sub-pages -> Analytics hub page,
    - hub page -> Holdings list.
  - Added small in-widget Pro upsell banner for free users and centered upsell text.
  - Replaced free widget edit fast-pass with a Pro-styled stop screen offering:
    - `UPGRADE TO PRO`,
    - `CONTINUE WITH FREE WIDGET`.
  - Preserved free default widget path (3 assets, default style, no customization) behind explicit continue action.
  - Validation passed with repeated debug Kotlin compile checks and clean lints on touched files.

- [ ] V40.36 Auth Flow Instrumentation Harness - NEXT
  - Add instrumentation coverage for login navigation and biometric success/cancel/failure UI behavior.
  - Add debug-only auth diagnostics surface for state transition tracing.
  - Execute cross-device biometric callback validation and capture OEM behavior notes.
  - Revisit account recovery strategy (post-hint path, fallback options, and anti-lockout policy) after current auth hardening cycle.

- [ ] V40.60 Monetization Conversion + Trust Polish - NEXT
  - Add lightweight event tracking for paywall and analytics premium funnel (`view`, `select package`, `upgrade tap`, `purchase outcome`, `restore outcome`).
  - Run end-to-end on-device validation for Free, Active Pro, and Expired Pro states across Analytics, Holdings, Theme Manager, Portfolio Manager, and Widget pathways.
  - Tighten remaining monetization copy and button wording for clarity/consistency.
  - Confirm navigation return paths preserve user context and avoid dead-end loops.

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
