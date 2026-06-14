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

- [x] V40.60 Localization Completion for New Monetization + Analytics Strings - COMPLETED
  - Performed full manual (no-script) update across all 20 locale files for newly introduced analytics/widget/upgrade strings.
  - Added and localized new keys for analytics hub labels, quick-jump title, swipe instruction, widget Pro customization banner, and free-flow CTA labels.
  - Replaced newly introduced hardcoded literals in analytics and widget flows with string resources.
  - Fixed locale-specific XML escaping issues found during compile validation (French/Turkish apostrophe cases).
  - Validation passed with clean lint and successful debug Kotlin compile after localization pass.

- [x] V40.66 Azbit Search + Custom Crypto Icons (uCrop) - COMPLETED
  - Shipped `AzbitSearchProvider` / `AzbitApiService`, registry + DI, windowed OHLC for sparklines, and icon URL merge behavior in `AssetRepository` and related flows.
  - Shipped user custom crypto icons: `IconManager`, `CryptoEditFunnel` (pick + uCrop + save), `FileProvider`, manifest, themes, and Coil cache-bust keys in `MetalIcon`.
  - Holdings: optimistic post-save row merge in `MyHoldingsScreen` to align with async DB updates.

- [x] V40.68.1 Full locale string parity (holdings upsell + asset custom icon keys) - COMPLETED
  - Scripted diff of `values/strings.xml` vs all `values-*/strings.xml`; added missing `asset_custom_icon_*` translations to every shipped locale; confirmed `holdings_upsell_*` present everywhere; `lintDebug` green.

- [x] V40.66.1 / V40.67 Custom icon reload on expanded card + Holdings free upsell banner - COMPLETED
  - `cryptoIconReloadNonce` after crypto save + `localIconReloadNonce` through asset cards into `MetalIcon` (Coil keys + error reset) so custom photo updates without collapsing the card.
  - Free-tier Holdings upsell uses `holdings_upsell_*` strings, tightened single-line layout and `9.sp` swipe hint so copy fits next to `PRO UPGRADE` CTA.

- [x] V40.69 Metals funnel + holdings card display truth - COMPLETED
  - `AssetRepository`: preserve metal `displayName` on upsert/refresh; `cleanMetalName` recognizes `XAU`/`XAG`/`XPT`/`XPD`; avoid overwriting user labels with generic clean names when `displayName` is set.
  - `HoldingsUIComponents`: `underIconTickerText` for icon + under-icon text; metal duplicate middle title removed on expanded compact + full card; two-line under-icon + collapsed two-line title for metals; flexible icon column height; `expandedMinHeight` nudged for expanded compact.
  - Architect / amount entry / metals audit / asset picker / analytics strings updated per session diff; compile verified with `:app:compileDebugKotlin`.

- [x] V40.70 Encrypted vault backup (VER1) export/import + engine hardening - COMPLETED
  - `VaultBackupEngine` (`data/backup/`): encrypted `.swpb` payload (magic `SWPB`, format v1, PBKDF2 + AES-GCM) wrapping a zip of Room DB, theme DataStore file, and icon trees; Settings SAF create/open + `SettingsViewModel` export/import; cold `killProcess` restart after successful import so Room/DataStore reload.
  - **WAL checkpoint:** `PRAGMA wal_checkpoint(FULL)` executed with `SupportSQLiteDatabase.query(SimpleSQLiteQuery(...))` + cursor `use {}` — required on Android because `execSQL` cannot run result-set pragmas (fixed export-time and pre-replace DB import-time crashes).
  - **Import robustness:** Prefer `openFileDescriptor` + capped byte read; fallback `openInputStream`; strip UTF-8 BOM before header; explicit ASCII magic decode; friendly error if file looks UTF-16 re-saved from an editor.
  - **Verification:** On-device round-trip (save backup → restore → app restart) succeeds; `:app:compileDebugKotlin` green.

- [x] V40.71 Metal spot valuation + backup UI split + feedback + About + i18n — COMPLETED (2026-05-02)
  - **`MetalSpotMath` / `AssetValuation`** (`data/local/MetalSpotMath.kt`): troy-oz conversion for **GRAM/KILO**; single valuation API for holdings, analytics, `AssetRepository`, widget/theme/architect/settings totals.
  - **`BackupRestoreScreen`** + **`Routes.BACKUP_RESTORE`**: dedicated encrypted backup UX; `SettingsScreen` navigates; VM/engine unchanged in behavior.
  - **`BugReportSubmitter`** + **`@Named("Feedback")` OkHttp** + Settings dialog + `SettingsViewModel.submitBugReport`.
  - **`AboutScreen`** + **`Routes.ABOUT`**; `MainActivity` hides bottom bar on About; Settings + Unlock links; **TEST INFO** removed from Settings.
  - **Locales:** default + all maintained `values-*` bundles updated for new strings; **`LanguageDisplay.kt`** for native language picker labels.
  - **Play (ops):** Developer fee paid; identity verification submitted (later **completed** 2026-05-04 — see Play Store path forward).

- [x] V40.72 Feature-freeze + i18n parity + narrative handoff — COMPLETED (2026-05-02)
  - **Product:** Owner considers app **feature-complete** for v1; remaining work is **shipping** (checklist Play/RevenueCat + QA), not new product features unless scope reopens.
  - **Locales:** MissingTranslation key set closed across **ar → zh-rTW** (64 keys each, hand-edited per `strings.xml`).
  - **Docs:** `docs/BROWSER_CONTEXT_NARRATIVE.md` + `docs/BROWSER_CONTEXT_MASTER.md` handoff synced to ship-only path.

- [x] V40.73 Public marketing site + GitHub Pages + domain — COMPLETED (2026-05-03)
  - **`website/`** static landing + **`privacy.html`** draft + **`CNAME`** (`swaniedesigns.com`); **`deploy-website.yml`** (**build**/**deploy**, actions v5); live **`https://swaniedesigns.com`** with **Enforce HTTPS**; Cloudflare DNS (**DNS only** to GitHub); legacy **`swanies-portfolio`** repo removed on GitHub.
  - **Owner follow-up:** Finalize **`website/privacy.html`** before Play Data safety / listing uses URL as “final.”

### Play Store path forward (as of 2026-05-08; verification email logged)

**Ordered “next session” steps** (internal AAB → closed → production, etc.) live in **`docs/AI_HANDOFF.md`** § *Play Console — ordered steps*.

- [x] **Android developer verification — Play auto-registration (Google email to owner):** All Play apps **registered** to the verified account; confirm on **Console Home**. **Before Sept 2026:** add any **other signing keys** used outside Play and register **non-Play** distributions per [Android developer verification](https://developer.android.com/developer-verification).
- [x] Play Developer account: **registration fee paid** ($25).
- [x] **Account verification (owner, 2026-05-04):** Google verified **driver’s license**; **emails** + **phone number** verified in Play Console. *(No longer waiting on this step.)*
- [x] **Public marketing + privacy shell host:** **`https://swaniedesigns.com`** (landing) + **`https://swaniedesigns.com/privacy.html`** (draft—replace **`[bracket]`** placeholders + remove **`noindex`** when copy is final); GitHub Pages + Cloudflare per narrative **V40.73**.
- [x] **Package name registration submitted (owner, 2026-05-04):** **`com.swanie.portfolio`** — Android developer verification: **debug** upload cert + APK with **`assets/adi-registration.properties`**.
- [x] **Package name registration approved (owner):** Console shows **Registered** / fingerprint **Verified** for **`com.swanie.portfolio`**.
- [x] **Release signing sanity-check (2026-05-05):** **`swanie_portfolio_release.jks`** ↔ **`app/release/app-release.aab`** — matching **SHA256** (`keytool` / `printcert`); no blocker on signing **AAB** for Play tracks when ready.
- [x] **Widget metal display parity (2026-05-07):** Glance **`AssetCardOriginal`** uses same **metal title + optional XAG subtitle** as holdings cards; **`SettingsViewModel`** widget price string uses **`AssetValuation.cardPriceRowUsd`** (matches **`AssetRepository`** widget push).
- [x] **Play app record + initial Dashboard setup (owner, 2026-05-08):** App **created** in Console; **Finish setting up your app** checklist (policy, listing, data safety, etc.) **complete** on Dashboard snapshot — confirm in Console if anything reopens.
- [x] **Internal testing:** **23 (1.0.23)** on track — auto-Pro QA (**22** grant=0 / **23** grant=30) owner verified **2026-06-01**; family on **internal testers** + site invite QR.
- [x] **Closed-test auto-Pro:** **`ClosedTestProAccess`** + paywall dialog; **`verify-play-release.ps1`**; beta unlock removed (**1.0.22+**).
- [x] **License testing policy:** **Swanie's Portfolio Testers** **unchecked** on **Settings → License testing** — friends/family on **internal testers only**; see **`docs/AI_HANDOFF.md`** § *Play testing — two lists*.
- [x] **Marketing site production showcase (2026-05-18):** **`#app-showcase`** — video + stacked screenshots, mobile swipe hint, scroll perf.
- [x] **Marketing site contact + owner QA (2026-05-19):** **`contact.html`** — Web3Forms, on-page confirmation; owner reviewed site and approved.
- [x] **Closed testing track (2026-06-01):** **24** on **Closed Alpha** + **internal**; opt-in **`apps/testing/com.swanie.portfolio`**; feedback **`contact.html?topic=tester`**.
- [x] **Play upload 1.0.24 (24):** Widget auto-Pro fix on internal + closed (**2026-06-01**).
- [x] **Testers Community (2026-06-01):** Submitted; **stopped** **2026-06-04** — opt-in-only cohort; replaced by **Fiverr**.
- [x] **Closed test opt-in (2026-06-02):** Play Dashboard **✓ 12+ testers opted-in** (TC era).
- [x] **Fiverr closed testers (2026-06-04):** **FIVERR** email list (**20**) + **Swanie's Portfolio Testers** (**3**) on **Closed Alpha**; Google Group removed; **24–48h** rollout; form assistance.
- [x] **Site closed-test QR (2026-06-04):** **`#get-app`** **`TESTER_URL`** → **`apps/testing/com.swanie.portfolio`** (family closed opt-in).
- [ ] **Closed test window:** **14+** consecutive days with **12+** opted-in — re-watch after **Fiverr** swap. **License testing** **unchecked**.
- [x] **Holdings Take Tour polish (26, 2026-06-08):** Touch lockdown, accidental-exit prevention, metal/picker/finale bug fixes; owner stress QA OK on laptop.
- [x] **Play upload 1.0.26 (26):** **Live** on **Closed Alpha** + **testers community** (**2026-06-02**); both groups notified; tour polish release notes.
- [x] **Holdings exit + refresh UX (27, 2026-06-09):** Exit button (`finishAndRemoveTask`), refresh on portfolio card per vault, header spacing; owner QA OK on laptop.
- [x] **Play upload 1.0.27 (27):** **Live** on **Closed Alpha** + **testers community** (**2026-06-10**); both groups notified; release notes on public listing.
- [ ] **Play upload 1.0.28 (28):** CryptoCompare price fix; **`CRYPTOCOMPARE_API_KEY`** required at build; **`GRANT_DAYS=30`**.
- [ ] **Post–closed test:** Wire **MEXC** search engine; remove **CryptoCompare** from picker (**ATLA** via free exchange API).
- [ ] **Production store AAB:** **`CLOSED_TEST_PRO_GRANT_DAYS=0`** (remove 30-day auto-Pro) + **`verify-play-release.ps1`**.
- [ ] **v29+ backlog:** Quit confirmation + **`confirmQuit`** toggle.
- [ ] **Production access prep:** **≥3** closed releases ✓ (**24** · **25** · **26**); **Pre-launch report** fixes; **10-question** form **250+ chars**/answer — draft bullets in **`docs/AI_HANDOFF.md`** § *Production access form — activity log*.
- [ ] **Production access gate:** Apply after metrics + prep above.
- [ ] **Production:** Apply / staged rollout when Console requirements met.
- [ ] **Publishing overview:** **Send app for review** when enabled (listing/metadata); optional **Managed publishing**.
- [x] **Monetization (internal QA):** Subscribe/expire/restore matrix on **1.0.10** **2026-05-18**; sufficient for ship. **Beta Pro:** **lifetime** or promotional — avoid monthly/yearly for daily use on internal.
- [ ] Tracks & quality: Crash/ANR; **local vault backup restore** on Play build (purchase + **Play restore** verified **2026-05-18**).
- [ ] Listing assets: screenshots, feature graphic, short/long description polish as needed per track.
- [ ] Go / no-go: stable internal/closed metrics before broad **production**; watch refunds / entitlements.

- [ ] V40.36 Auth Flow Instrumentation Harness - NEXT
  - Add instrumentation coverage for login navigation and biometric success/cancel/failure UI behavior.
  - Add debug-only auth diagnostics surface for state transition tracing.
  - Execute cross-device biometric callback validation and capture OEM behavior notes.
  - Revisit account recovery strategy (post-hint path, fallback options, and anti-lockout policy) after current auth hardening cycle.

- [ ] V40.61 Monetization Conversion + Trust Polish - NEXT
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
