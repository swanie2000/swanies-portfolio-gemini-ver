SWANIES PORTFOLIO: MASTER NARRATIVE (V40.72: FEATURE-COMPLETE — PLAY LAUNCH / SHIP TRACK ONLY)

---

## AI AGENT HANDOFF (READ FIRST)

**Why this file exists:** Continuity for Cursor/Studio agents and browser-side advisors across days and sessions. **Keep this section accurate** when milestones or priorities change so the next agent does not infer “today” from older `CURRENT CONDITION` blocks further down (those belong to past drops only).

**Canonical vs excerpt:** This file (`docs/BROWSER_CONTEXT_NARRATIVE.md`) is the **canonical** product timeline + handoff. `docs/BROWSER_CONTEXT_MASTER.md` embeds a copy under `BEGIN_NARRATIVE` for paste bundles; if the two diverge, **update this file first**, then refresh the MASTER excerpt.

**Last handoff update:** 2026-02-25 (~23:30 local, owner EOD) — **Product:** Owner considers the **app feature-complete**; no further feature roadmap—**only shipping** (Play Console, compliance, listing, AAB, purchase/restore QA). **Repo:** V40.71 product stack + **i18n parity** (64 keys × 19 locales, hand-edited). **Play:** Account fee paid; identity verification submitted (await Google). *Next session: resume Play checklist when ready.*

### Where we left off (engineering truth)

- **Product stance:** **Feature freeze** from the owner’s perspective—treat the codebase as **ship-ready** aside from store/console work, release QA, and any **must-fix** bugs found during testing. Do not assume greenfield feature requests unless the owner reopens scope.
- **Stack:** Android (Kotlin, Compose, Hilt, Room). Pro monetization via **RevenueCat** + Play billing when distributed on Play; gated surfaces include Theme Manager, multi-portfolio holdings swipe, full Analytics experience, widget customization (see monetization package and `SettingsScreen` Pro flows).
- **Vault backup / restore (VER1):** **Working on device** — engine unchanged in spirit: `VaultBackupEngine.kt` (magic `SWPB`, WAL `query` checkpoint, SAF paths, cold restart). **UI split:** dedicated **`BackupRestoreScreen.kt`** + **`Routes.BACKUP_RESTORE`** + **`NavGraph`** composable; Settings navigates here so export/import/passphrase UX lives off the main settings scroll. VM hooks remain **`SettingsViewModel`** (+ factory wiring).
- **Metal spot valuation (V40.71):** **`data/local/MetalSpotMath.kt`** defines troy-oz conversion (**GRAM/KILO/G** → troy oz) and `spotUsdForMass`. **`AssetValuation`** (same file) exposes **`spotMassHoldingsUsd`**, **`holdingValueUsd`** (spot + premium), **`cardPriceRowUsd`** (per-line metal mass vs whole-crypto token). Wired through **`HoldingsUIComponents`** (collapsed totals + expanded price row), **`MyHoldingsScreen`**, **`AnalyticsScreen`**, **`AssetRepository`** (live refresh totals/line prices), **`SettingsViewModel`** (vault totals for theme/widget data), **`ThemeStudioScreen`**, **`WidgetManagerScreen`**, **`PortfolioWidget`**, **`AssetArchitectScreen`**.
- **Holdings UI:** Metal compact **total** and **price** row corrected to use `AssetValuation` (no more wrong gram/kilo math on collapsed lines). V40.69 metal naming / two-line cards unchanged in intent (`underIconTickerText`, **`AssetRepository`** `displayName`).
- **About / legal:** **`AboutScreen.kt`**, **`Routes.ABOUT`**, **`NavGraph`**, **`MainActivity`** (bottom bar hidden on About). Settings **Legal & about** + **`UnlockVaultScreen`** footer link. Copy in **`values/strings.xml`** + **all maintained `values-*` bundles**. Full ToS still **`TermsAndConditionsScreen`** + `terms_*` / `tos_*`.
- **In-app feedback:** **`BugReportSubmitter`** (`data/feedback/`) posts via **FormSubmit** using a dedicated **`@Named("Feedback")` OkHttpClient** (browser-like UA/timeouts) from **`NetworkModule`**. Settings: dialog + **`SettingsViewModel.submitBugReport`**, **`SettingsViewModelFactory`** / Hilt constructor injection. Log tag **`SwanieBugReport`** for QA.
- **i18n:** Residual literals pushed to resources across key flows; **`LanguageDisplay.kt`** for native language picker labels. **MissingTranslation closure:** the **64** default keys that were absent from locale bundles (vault/amount-entry/widget/analytics/theme strings, bug-report + About/legal, architect metal/unit labels) were added **per file** to **ar, de, es, fr, hi, in, it, ja, ko, nl, pl, pt-rBR, ru, th, tr, uk, vi, zh-rCN, zh-rTW** with translated copy (no bulk scripts). Default **`values/strings.xml`** already holds English + funnel/architect/translation-feedback defaults from the earlier merge fix.
- **Settings cleanup:** Debug **TEST INFO** (RevenueCat test nav) **removed** from **`SettingsScreen`**; route **`REVENUECAT_TEST_INFO`** may remain for dev builds.
- **Other touches:** **`MainViewModel`** small fixes (e.g. application `Context` usage where needed for widget/portfolio labeling). **`VaultBackupEngine`** minor follow-ups if any (see diff). **`AmountEntryScreen`**, **`CreateAccountScreen`**, **`HomeScreen`** aligned with new strings/flows.
- **Quality gates:** Run `:app:compileDebugKotlin` and `:app:lintDebug` before calling a milestone done; lint policy in **`app/lint.xml`**.

### What to do next (owner intent, priority order) — **finish line only**

1. **Play Console (human):** Identity verification result → **Play Console mobile app** device verification → **phone** verification → create/list the app when the console allows.
2. **Store / compliance:** **Internal (then closed) testing** AAB from Android Studio; **Data safety**, content rating, target audience, **privacy policy URL** (hosted), store listing assets and copy; **Google Play subscription SKUs ↔ RevenueCat** offerings and **`pro`** entitlement testing with license testers — see **`Master_Build_Checklist.md`** Play section.
3. **Pre-launch QA (targeted):** Encrypted backup round-trip; purchase + restore + expiry paths; widgets + Pro-gated surfaces; **GRAM/KILO** metal display sanity on a physical device.
4. **Backlog (explicitly non-blocking for v1 ship):** V40.36 auth instrumentation, V40.61 monetization telemetry, V40.69 small-screen metal polish—**only if** the owner schedules post-1.0 work.

### Quick file map

| Area | Start here |
|------|------------|
| Encrypted backup engine | `VaultBackupEngine.kt` |
| Backup / restore UI | `BackupRestoreScreen.kt`, `SettingsViewModel.kt`, `Routes.kt` (`BACKUP_RESTORE`), `NavGraph.kt` |
| Settings shell / feedback | `SettingsScreen.kt`, `SettingsViewModel.kt`, `SettingsViewModelFactory.kt`, `BugReportSubmitter.kt`, `NetworkModule.kt` (`Feedback` client) |
| Metal spot / USD valuation | `MetalSpotMath.kt` (`MetalSpotMath` + `AssetValuation`) |
| Pro / billing | `billing/` package, `MonetizationManager.kt` |
| Holdings / metals UI | `HoldingsUIComponents.kt`, `MyHoldingsScreen.kt`, `AssetRepository.kt` |
| About / nav | `AboutScreen.kt`, `NavGraph.kt`, `Routes.kt`, `MainActivity.kt` |
| Language picker labels | `LanguageDisplay.kt`, `values/strings.xml` + `values-*` |
| Nav / routes (general) | `NavGraph.kt`, `Routes.kt` |

---

V40.72 UPDATE (owner: feature-complete — i18n parity pass; ship-only path)
- **Intent:** No further product features planned; focus is **Google Play launch** (verification chain, compliance, AAB, listing, billing alignment).
- **i18n:** All maintained **`values-*`** files now include the **64** previously missing keys (manual per-locale edits); aligns with lint **MissingTranslation** expectations for that key set.

CURRENT CONDITION (END OF SESSION)
- **Code + locales:** Treated as **complete for v1**; remaining risk is **store/process + QA**, not missing app features.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- Execute **`Master_Build_Checklist.md`** Play / RevenueCat items until production or staged rollout decision.
- Reopen engineering backlog only after ship or explicit owner reprioritization.

V40.71 UPDATE (full session — metal math, backup screen split, feedback relay, About, i18n, Play queue)
- **Valuation pipeline:** Single source `MetalSpotMath.kt`: **USD/troy oz** spot × **troy oz** mass (with **GRAM/KILO/KG** conversion). `AssetValuation` used for list rows, aggregates, repo refresh fields, widget + theme previews, architect draft total, settings-driven totals.
- **Holdings cards:** Collapsed metal line **total** and **price** row use `AssetValuation` so gram/kilo holdings match industry bullion math.
- **Settings architecture:** **`BackupRestoreScreen`** owns encrypted backup UX; **`SettingsScreen`** slimmed — navigates `Routes.BACKUP_RESTORE`; export/import still drive `SettingsViewModel` + `VaultBackupEngine`.
- **Bug / feedback:** `BugReportSubmitter` + FormSubmit path + Feedback-scoped OkHttp; dialog from Settings; structured logging for field debugging.
- **About + legal entry points:** New screen + route; bottom nav suppressed on About; Settings + Unlock links.
- **i18n:** New/updated keys in **default + ar, de, es, fr, hi, in, it, ja, ko, nl, pl, pt-rBR, ru, th, tr, uk, vi, zh-rCN, zh-rTW**; `LanguageDisplay` for native option labels.
- **Play (human ops):** Developer **registration paid**; **identity verification** docs **submitted** — email-driven next step; then Play Console **mobile app** device check and **phone** verification per console.

CURRENT CONDITION (END OF SESSION)
- **V40.71** product stack + **V40.72** locale parity in repo; **compile** green after pull (`mergeDebugResources` / `compileDebugKotlin`).
- Play: **publishing blocked** until Google completes verification chain.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- **Owner-only ship track:** verification → AAB → compliance + listing + **SKUs ↔ RevenueCat** per checklist.
- **Optional QA** before promote: backup round-trip, Pro purchase matrix, metal **GRAM/KILO** spot-check.

V40.70 UPDATE (Vault backup / restore VER1 — engine hardening + device-verified round-trip)
- **Export:** `VaultBackupEngine` checkpoints WAL with `writableDatabase.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))` (cursor closed); Android SQLite forbids `execSQL` for pragmas that return rows, which previously broke export at zip time.
- **Import:** Reads encrypted payload via `ContentResolver.openFileDescriptor` + capped stream (falls back to `openInputStream`); strips leading UTF-8 BOM before `SWPB` magic check; detects UTF-16 LE “text save” and surfaces a clear error; magic parsed with `String(bytes, offset, length, US_ASCII)`.
- **User verification:** Export → SAF save → import with passphrase → success toast → cold process restart; holdings/theme/icons load from restored snapshot.

CURRENT CONDITION (END OF SESSION)
- VER1 encrypted local backup (Room DB + `theme_settings.preferences_pb` + `icons` / `custom_icons`) is credible for sovereign moves and disaster-recovery drills ahead of store listing.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- **Play Store (priority order):** Play Console record complete (category, contact, privacy policy URL, support); Data safety + content rating + target audience; subscription SKUs / base plans + RevenueCat offering ↔ `pro` entitlement mapping; internal then closed track AAB; 48–72h monitoring (crashes, ANRs, purchases, subscription restore); screenshots, feature graphic, short/long description; promote to production when matrix is green.
- **QA:** Post-restore passes on widgets, multi-portfolio swipe, custom crypto icons, and Analytics gated states.
- **Backlog when prioritized:** Auth instrumentation (V40.36), monetization funnel telemetry (V40.61), optional locale parity for any new backup strings.

V40.69 UPDATE (Metals architect / repository / holdings cards)
- **Repository:** Metal `displayName` from architect/funnel is preserved on `upsertAsset` and live refresh; `cleanMetalName` extended for `XAU`/`XAG`/`XPT`/`XPD` so spot tickers map without replacing user text.
- **Holdings UI:** `underIconTickerText(asset)` for `MetalIcon` + under-icon label (metal = display name path, crypto = symbol). Expanded compact + `FullAssetCard`: no duplicate gray middle title for metals; under-icon label up to **two lines** (removed fixed `95.dp` clip); collapsed compact metal title can show two lines via `AutoResizingText`; slightly taller expanded compact min height; header row top-aligned.
- **Funnel surfaces:** `AssetArchitectScreen`, amount entry, metals audit, asset picker, and related strings/locales updated in this drop (see git history for file-level detail).

CURRENT CONDITION (END OF SESSION)
- Metal holdings show user-authored names consistently; expanded cards no longer clip the second line of a two-line metal name.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- Device QA: small screens + large font scale on metal cards.
- Optional parity: two-line labels on other metal lists if desired.

V40.67 UPDATE (Free-tier upsell copy + expanded custom icon refresh)
- **Holdings free-tier banner:** Uses holdings-only string keys (`holdings_upsell_*`), multi-portfolio / swipe messaging, short `PRO UPGRADE` CTA; single-line layout and smaller subtitle font so nothing wraps under the lock row or clips beside the button.
- **Custom crypto icon after save:** `cryptoIconReloadNonce` on crypto save + `localIconReloadNonce` into `MetalIcon` so Coil/Compose refresh when the file path is unchanged but bytes changed (expanded card no longer stale until collapse).

CURRENT CONDITION (END OF SESSION)
- Holdings upsell and post-edit icon behavior match on-device expectations; build remains compile-green.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- Localize `holdings_upsell_*` across locale bundles when ready.
- Continue monetization / Play readiness work from earlier roadmap items.

V40.66 UPDATE (Azbit Search, Live Data, Custom Icons + uCrop)
- **Azbit exchange search:** New `AzbitSearchProvider` + `AzbitApiService` (Retrofit) wired in `SearchEngineRegistry` and DI. OHLC requests use an explicit UTC window (e.g. 72h) so sparklines populate; quote preference includes `MUSDT` where applicable.
- **Icons + picker:** Search-time icons for Azbit-sourced assets improved (Jupiter token metadata, batching, CoinCap fallback, IPFS gateway handling). `AssetRepository` / live refresh now merge `imageUrl` / `iconUrl` so list rows and pickers update when URLs arrive.
- **Custom crypto icon:** `IconManager` persists user photos under `filesDir/custom_icons/`. Crypto edit sheet (`CryptoEditFunnel`) supports pick → **uCrop** (JitPack) → save; `FileProvider` + cache paths; `UCrop.Theme` as floating dialog with opaque colors and min-width fraction; `AndroidManifest` declares `UCropActivity`.
- **Holdings UI:** `MetalIcon` uses Coil `ImageRequest` with disk-busting cache keys (`lastModified` + length) for local files. `MyHoldingsScreen` merges **optimistic** post-save `AssetEntity` into vault holdings until Room Flow matches (reduces lag after async `updateAssetEntity`).

**Resolved in V40.67:** Expanded-card custom icon refresh fixed via `cryptoIconReloadNonce` / `localIconReloadNonce` (see V40.67 block above).

V40.65 UPDATE (Bottom Nav Gradient Sync + Content Safety)
- **Single gradient source restored:** Moved to one authoritative background render path at app root (`MainActivity`) and removed duplicate gradient layers from `NavGraph`/screen roots that caused visual seams.
- **Bottom bar blend fix:** Updated bottom navigation container to avoid local solid/independent gradient painting so it inherits the same global gradient without offset artifacts.
- **Holdings parity:** Removed local holdings root background override that was still forcing a mismatch against the shared gradient pipeline.
- **Scroll/readability protection:** Kept full scaffold content insets so list content stops above nav icons (no text sliding under icons).
- **Outcome:** Settings, Analytics, and Holdings now share consistent gradient continuity while preserving readable, reachable scroll content.

CURRENT CONDITION (END OF SESSION)
- Gradient/nav integration is stable with one shared background path and compile-green status.
- Bottom menu no longer appears as a separate block while content remains safely above icon row.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- Optional visual polish: add subtle nav icon glow/contrast compensation for low-contrast gradient segments.
- Run device spot-check matrix (small/large phones, dark/light text themes, gradient intensity extremes).

V40.64 UPDATE (Widget Style + Theme Manager UX Alignment)
- **Unified edit workflow:** Both Widget Style and Theme Manager now use the same top-row interaction model: target dropdown -> unsaved state switches to red `CANCEL` + pulsing `SAVE`.
- **Target naming normalization:** Updated style targets to `APP Background`, `App Text`, `Card Background`, `Card Text` in Theme Manager; widget style keeps matching terminology for card/background/text choices.
- **Header action correction:** Theme Manager reverted to refresh icon action to avoid text overlap with branding.
- **Live preview upgrade:** Theme Manager sample now renders a real live asset card + live total from holdings data (same visual language as widget preview) instead of static mock content.
- **Small-screen usability:** Style controls remain pinned near top to reduce scrolling friction on smaller devices.

CURRENT CONDITION (END OF SESSION)
- Theme and Widget style tabs now feel behaviorally consistent and easier to understand.
- Live visual feedback is immediate on both surfaces, including real portfolio preview in Theme Manager.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- Optional polish: Replace deprecated `Icons.Filled.Undo` with `Icons.AutoMirrored.Filled.Undo`.
- Device QA pass on smaller screens for touch targets, dropdown behavior, and save/cancel discoverability.

V40.63 PLAN (Play Console + RevenueCat Release Readiness)
- **Account + console setup**
  - Confirm Play Developer account is active and app record/package name are finalized.
  - Complete Play app profile basics (app category, contact email, privacy policy URL, support URL).
- **Billing + RevenueCat mapping**
  - Create Google Play subscriptions/base plans (monthly/yearly, optional trial).
  - Link Play products to RevenueCat offerings/packages and verify entitlement mapping (`pro` unlock path).
  - Validate purchase/restore/cancel/expiry behavior in sandbox/internal track.
- **Testing matrix before production**
  - Run full state passes: Free, Active Pro, Grace Period, Expired Pro, Restored Pro.
  - Validate gated surfaces: Theme Manager, Multiple Portfolios, Analytics Pro, Widget Manager.
  - Confirm widget config/edit flow remains stable after entitlement transitions.
- **Store + compliance gates**
  - Complete Data safety form accurately.
  - Complete content rating and target audience.
  - Verify screenshots, short/full description, feature graphic, app icon, and release notes.
- **Go-live controls**
  - Start with Internal -> Closed track rollout.
  - Watch crash-free sessions, ANR, purchase conversion, restore success, and refund signals for 48-72h.
  - Promote only after metrics are stable and no entitlement regressions are observed.

NEXT SESSION START (V40.63 EXECUTION ORDER)
- 1) Play subscriptions/base plans + RC offering/package link validation.
- 2) Internal track build upload and tester matrix execution.
- 3) Store listing/compliance completion and final go/no-go review.

V40.62 UPDATE (Warnings Burn-Down + Lint Zero Lock)
- **`lintDebug` status:** `:app:lintDebug` now reports **0 errors / 0 warnings** (`BUILD SUCCESSFUL`).
- **Warning fixes shipped:** Cleared `UseKtx` and `ModifierParameter` with safe code updates (KTX extension usage, composable signature ordering, lambda `offset` overload for state-backed value).
- **Resource hygiene:** Removed dead assets/colors flagged by lint and validated compile/lint stability after cleanup.
- **Lint policy lock:** Added module `app/lint.xml` to explicitly suppress non-functional advisory buckets (typography/style advisories, dependency/version nudges, locale-folder naming, selected heuristics) to keep CI signal focused on regressions.
- **Result:** Project is now lint-clean in current policy mode and ready for focused feature/testing/release work.

CURRENT CONDITION (END OF SESSION)
- Lint gate is green with **0/0** and no IDE linter regressions on touched files.
- Holdings/reorder, localization, monetization, and widget flows remain compile-stable after the warning sweep.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- **Policy follow-up:** Optionally revisit `app/lint.xml` suppressions one-by-one if you want stricter warning enforcement before release.
- **Release readiness:** Continue Play Console prep, internal track validation, and RevenueCat purchase/restore matrix testing.

V40.61 UPDATE (Lint Error Burn-Down + Translation Completion)
- **`lintDebug` status:** Reduced from ~69 errors to **0 errors** (`BUILD SUCCESSFUL`) while keeping existing warnings/hints visible for later cleanup.
- **Compose lint error fixes:** Replaced `LocalContext.current as FragmentActivity` with `LocalActivity.current as FragmentActivity` in `HomeScreen`, `NavGraph`, `UnlockVaultScreen`, `RestoreVaultScreen`, and `PortfolioManagerScreen` (matching AndroidX guidance for `ContextCastToActivity`).
- **Holdings UI lint fix:** Replaced unused-scope `BoxWithConstraints` with `Box` in `HoldingsUIComponents` to resolve `UnusedBoxWithConstraintsScope`.
- **Localization completion (monetization + analytics premium):** Added real locale translations for the new Settings/RevenueCat/Pro-gate block and the `analytics_premium_*` CTA/badge/title keys across all shipped locale files (`values-ar`, `de`, `es`, `fr`, `hi`, `id`, `it`, `ja`, `ko`, `nl`, `pl`, `pt-rBR`, `ru`, `th`, `tr`, `uk`, `vi`, `zh-rCN`, `zh-rTW`).
- **Terms coverage parity:** Filled missing `terms_section_*` + `terms_last_updated` entries for locales previously flagged by lint (`de`, `fr`, `ar`, `ja`).

CURRENT CONDITION (END OF SESSION)
- `:app:lintDebug` is **passing** (errors cleared).
- RevenueCat monetization copy and analytics premium copy are now localized across all maintained locales.
- Codebase is compile-green and lint-error-green, suitable for continued product polish and Play readiness work.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- **Play + monetization:** Register Play Developer account; create subscription SKUs/base plans; bind SKUs to RevenueCat offerings; validate on internal/closed track.
- **Conversion hardening (V40.62 candidate):** Add purchase/restore funnel telemetry and run Free/Active Pro/Expired Pro end-to-end state checks on device.
- **Lint warnings strategy:** Decide whether to incrementally clean warnings (DefaultLocale, UnusedResources, etc.) or freeze with a baseline for CI signal-only on regressions.

V40.60 UPDATE (Localization, Holdings Crash Fix, Auth/Lint Hygiene, Launch Path)
- **Pro analytics live strings (`analytics_live_*`):** Completed real translations for **`values-vi`** (including model-line labels and risk-engine wording), **`values-zh-rCN`**, and **`values-zh-rTW`**; aligned copy with `AnalyticsProLivePages.kt` string keys. (Earlier milestone locales such as `ar`, `hi`, `id`, `uk` remain part of the same wave.)
- **Holdings reorder crash (free + Pro verified):** Fixed `MyHoldingsScreen` drag-and-drop when a **non-reorderable Pro upsell `item`** precedes holdings in the `LazyColumn`. `sh.calvin.reorderable` reports **absolute** lazy indices; `onMove` now subtracts **`holdingsLazyListStartIndex`** (`0` if Pro, else `1`) and guards out-of-range indices before mutating `localHoldingsForPage`.
- **`CreateAccountScreen`:** Replaced `LocalContext.current as FragmentActivity` with **`LocalActivity.current as FragmentActivity`** for `hiltViewModel` ownership (aligns with AndroidX `ContextCastToActivity` guidance; compiles clean).
- **Build sanity:** `:app:mergeDebugResources` and `:app:compileDebugKotlin` succeed after the above. **`lintDebug`** still reports broader pre-existing issues (e.g. `MissingTranslation` across settings/monetization keys); treat as a **separate** hardening track, not a regression from this drop.

CURRENT CONDITION (END OF SESSION)
- Holdings card reorder is **stable** for free-tier (banner + rows) and Pro-tier (rows only).
- Premium live analytics copy is **localized** for Vietnamese and both Chinese script variants alongside the existing locale set.
- RevenueCat remains the **entitlement and subscription orchestration** layer; **Google Play Console** (account, in-app products, Play Billing) is the **next prerequisite** for public sale—not a replacement for RevenueCat.

FUTURE PATH (NEXT IMPLEMENTATION TRACK)
- **Play + monetization:** Register Play Developer account when ready; create subscription base plans; wire SKUs to **RevenueCat offerings**; validate on **internal/closed** tracks before production. Optional **Play-managed free trial** reads through RC as today’s entitlements do.
- **Compliance prep (first-ship quality):** Privacy policy URL, accurate **Data safety** form, content rating, and store listing assets in parallel with technical testing.
- **V40.61 candidate — Monetization conversion + trust:** Funnel telemetry (`view`, `package select`, `upgrade tap`, `purchase` / `restore` outcomes), on-device matrix passes (Free / Active Pro / Expired), paywall copy polish, small regression tests for entitlement-driven UI.

NEXT SESSION START — LINT / TRANSLATIONS (START HERE TOMORROW)
- **Facts:** `:app:lintDebug` reports on the order of **~69 errors** and **~350 warnings** (counts drift as code changes). A large share of **errors** is **`MissingTranslation`**: English keys added for **Settings / RevenueCat test / Pro gate** (`values/strings.xml` ~line 100+) not yet present in every `values-xx/strings.xml`. **Google Play does not require lint-all-green** to publish; this is your own quality / CI bar.
- **You do not need** to clear every lint item before Play signup or internal testing. Choose a **policy**, then execute.
- **Pick one lane (or combine A + D):**
  - **(A) `lint.xml` (module):** Set **`MissingTranslation`** severity to **`warning`** so builds/CI can still fail on remaining **errors** while locales catch up.
  - **(B) `lint-baseline.xml`:** In `app/build.gradle.kts`, `lint { baseline = file("lint-baseline.xml") }`, run **`./gradlew :app:updateLintBaseline`**, commit baseline — freezes known debt; CI only flags **new** issues.
  - **(C) i18n sweep:** Script or batch-add the new keys to each locale file (English placeholder acceptable until translated).
  - **(D) Code fixes first:** Same pattern as `CreateAccountScreen` — **`HomeScreen.kt`** still uses `LocalContext.current as FragmentActivity` (**`ContextCastToActivity`**); fix with **`LocalActivity.current as FragmentActivity`**. Address **`UnusedBoxWithConstraintsScope`** in `HoldingsUIComponents.kt` (replace with `Box` if constraints unused). Re-run **`./gradlew :app:lintDebug`** and open **`app/build/reports/lint-results-debug.html`** for the live list.
- **Report path:** `app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt` (text) and HTML report under `app/build/reports/`.

V40.59 UPDATE (Analytics Premium Live Engines + Modular Refactor)
- Completed/refined live premium analytics engines across `RISK`, `ATTRIBUTION`, `REBALANCE`, and `SCENARIOS`.
- Finalized split architecture for premium analytics:
  - `AnalyticsProUpsellPages.kt`
  - `AnalyticsProLivePages.kt`
  - `AnalyticsProUiComponents.kt`
- Removed legacy combined premium file to reduce complexity and improve change safety.
- Preserved visual parity after split by restoring shared spacing/typography details and fixing a subtle post-split spacing regression.
- Stability confirmation: repeated `:app:compileDebugKotlin` runs pass; lints clean on newly split files.

V40.58 UPDATE (Analytics Premium Experience + Upgrade Flow UX)
- Reworked Analytics into a mixed-access model: free chart pages remain available, while Pro insights are presented as premium teaser pages with upgrade entry points.
- Added swipe-first analytics page flow with compact chart/list layout and a dedicated `START` instructional page for clearer onboarding.
- Standardized premium visuals across monetization surfaces via shared `ProPalette` tokens and reusable `ProLockBadge` treatment.
- Upgraded Pro analytics content into larger in-page ad panels and finalized CTA behavior to a single full-width `UPGRADE TO PRO` action.
- Improved `UPGRADE TO PRO` page usability by surfacing an immediate back action near the primary purchase action.
- Verified stability through repeated `:app:compileDebugKotlin` successes during iterative UI refinement.

V40.57 UPDATE (RevenueCat Stabilization + Access Enforcement)
- Added RevenueCat app-user identity synchronization before restore/purchase/offering calls to reduce anonymous entitlement drift.
- Upgraded restore outcomes to deterministic user-facing statuses (`already active`, `restored`, `no entitlement`, `failed`).
- Split monetization into dedicated routes (`UPGRADE TO PRO NOW`, `TEST INFO`) and improved settings flow clarity.
- Enforced Free-tier holdings restrictions to prevent multi-vault swipe access without Pro and added contextual upgrade upsell entry.

V40.56 UPDATE (RevenueCat Monetization Wiring + Pro Gating)
- Integrated RevenueCat Android SDK and app startup initialization with safe no-key fallback behavior.
- Added a centralized monetization layer (`MonetizationManager`) with entitlement refresh, restore, offerings fetch, and purchase methods.
- Gated paid surfaces behind Pro entitlement: Theme Manager, Multiple Portfolios, Analytics, and Widget Manager configuration flow.
- Built a reusable Pro gate screen with package selection, upgrade action, restore purchases, retry plan load, and manage subscription deep link.
- Added Settings monetization diagnostics: Pro status, RevenueCat configured flag, loaded plan count, expected entitlement/offering IDs, and checklist copy action.
- Hardened configuration targeting with explicit `REVENUECAT_PRO_ENTITLEMENT` and `REVENUECAT_OFFERING_ID` build fields.

V40.55 UPDATE (RevenueCat Monetization Plan + Execution Start)
- Locked monetization direction to RevenueCat with a staged rollout path that protects current app stability.
- Defined product model: one `pro` entitlement, monthly/yearly subscriptions, and a future one-time lifetime SKU path.
- Planned implementation sequence: SDK bootstrap -> entitlement state layer -> gated Pro surfaces -> purchase/restore UX -> telemetry + staged release checks.
- Added launch guardrails: no hard paywall before restore/account edge-case validation, and centralized billing logic behind one repository boundary.
- Started execution by introducing an in-app monetization boundary layer so RevenueCat wiring can be added safely without touching feature logic.

V40.52 UPDATE (Settings UX Stabilization)
- Shifted app-wide font scaling from hard lock to capped scaling (`coerceAtMost(1.40f)`) to preserve readability while limiting overshoot.
- Fixed language-picker first-selection race by persisting language code before activity recreation.
- Normalized key Settings labels to all-caps for visual consistency and changed `REPORT TRANSLATION ISSUE` to `REPORT TRANSLATION`.
- Resolved two-line clipping for the translation call-to-action area by replacing button chrome with full-width clickable text and explicit line-height handling.

V40.50 UPDATE (Localization Finalization)
- Completed direct, new-data translation cleanup for `values-hi`, `values-th`, `values-uk`, and `values-zh-rTW`.
- Resolved remaining English fallback blocks on auth/settings/terms/holdings surfaces for those locales.
- Preserved formatting tokens (`%1$s`, `%1$d`) and validated UTF-8 readability.
- Verified compile sanity with `:app:compileDebugKotlin` (BUILD SUCCESSFUL).

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â½ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ THE CORE MISSION
To maintain a commercial-grade financial vault where user data is sovereign, biometrics are hardware-enforced, and the UI is a cinematic, high-precision experience that survives high-density settings and real-world widget workflows.

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 1. MANDATORY AGENT OPERATING PROCEDURES (THE LANE LOCK)

    FULL FILE OUTPUTS ONLY: Swanie requires full file context. Partial snippets are forbidden unless explicitly requested.

    SOVEREIGN SHIELD: Never push local.properties, secrets, or API keys. Enforce .gitignore discipline.

    STABILITY FIRST: Prefer smallest safe changes. Preserve existing architecture unless explicitly directed.

    DENSITY SHIELD: UI text scaling must respect the clamp pattern: (originalSp.toPx() / fontScale.coerceAtMost(1.15f)).toSp().

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 2. V40.4 FINAL ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â PURE PENCIL (DETERMINISTIC ENTRY)

    Single entry point:
      - There is no in-app ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“Widget ManagerÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â button in Settings. That path was removed.
      - The user opens the editor only from the home widget (Glance pencil): [WidgetClickCallback] starts [WidgetConfigActivity] with [AppWidgetManager.EXTRA_APPWIDGET_ID] (and a relay URI for anti-conflation).

    Activity + manifest contract:
      - [WidgetConfigActivity] is `android:exported="false"` and has **no** `<intent-filter>`. It is not launchable as a generic `APPWIDGET_CONFIGURE` target from the system.
      - If `appWidgetId` is missing or invalid after [resolveAppWidgetId], the activity calls `finish()` immediately ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â no portfolio picker, no multi-widget dialog, no inference from ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“any widget on the host.ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â

    Deterministic binding:
      - [SettingsViewModel.forceVaultSwitch(appWidgetId, isAppWidgetId = true)] resolves the vault row for that exact widget instance.
      - [WidgetManagerScreen] receives a valid `configAppWidgetId` only when the activity has already accepted the intent.

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 3. V40.4 FINAL ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â WIZARD FUNNEL + TACTILE REORDER (WIDGET MANAGER UI)

    Wizard Funnel layout ([WidgetManagerScreen]):
      - Funnel navigation uses integer page state (`currentPage`): `0=SETUP`, `1=ASSETS`, `2=STYLE`, `3=PREVIEW`.
      - **SETUP**: portfolio selector + show total control.
      - **ASSETS**: sticky header + flat reorder stage with persisted ordering.
      - **STYLE**: compact color studio controls.
      - **PREVIEW**: guarded preview path with immutable snapshot filtering and fallback error surface.

    600 ms weighted reorder physics:
      - **Row resize / slot feel:** `WidgetReorderItemAnimationSpec` ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â `tween<IntSize>(600, LinearOutSlowInEasing)` on checked/dragging rows.
      - **List placement / slide-to-slot:** `WidgetFlatListPlacementSpec` ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â `tween<IntOffset>(600, LinearOutSlowInEasing)` for `animateItem` placement on reorderable asset rows.
      - After drag stop, a short settle delay (`WIDGET_DRAG_SETTLE_DELAY_MS` = 380 ms) runs before persisting order via [AssetViewModel.updateWidgetSelectionForCurrentVault].

    Tactile reorder UX (unchanged):
      - Long-press on the asset row handle initiates drag; haptics on drag start; persistence on drag end after settle.

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 4. V40.4 FINAL ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â SAVE, REFRESH, AND TASK LIFECYCLE

    Save pipeline:
      - [AssetViewModel.saveWidgetConfiguration] persists vault widget fields (including optional `appWidgetId` rebind), pushes Glance data via [AssetRepository.pushFreshAssetsToWidget], and sends [ACTION_APPWIDGET_UPDATE] for the affected instance(s).
      - [SettingsViewModel] still applies appearance and totals (`saveWidgetAppearance`, `updateShowWidgetTotal`) and may call `forceImmediateRemoteViewsUpdate` for instant RemoteViews feedback.

    Primary completion behavior ([WidgetConfigActivity]):
      - On **SAVE & EXIT** success and on **back**, the activity uses **`finishAndRemoveTask()`** as the primary way to tear down the translucent config task and return the user cleanly to the home screen, alongside the post-save broadcast so the launcher-bound widget redraws with the same `appWidgetId` the pencil supplied.

    Vault-scoped truth (unchanged):
      - Canonical widget fields live on [VaultEntity]: `selectedWidgetAssets`, `showWidgetTotal`, widget colors, `appWidgetId`.
      - [AssetViewModel.widgetSelectedAssetIds] / `setWidgetSelectionVaultId` keep selection aligned with the vault being edited.

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â° 5. V40.4 CURRENT STATE (TOMORROW TROUBLESHOOTING SNAPSHOT)

    Widget Manager Screen:
      - [SettingsViewModel]: `targetVaultId` / `targetVault` / `targetVaultAssets` for the resolved vault.
      - [AssetViewModel]: ordered selection, toggles, drag-end persistence, and `saveWidgetConfiguration` on save.
      - Config mode: `configAppWidgetId` is always the pencilÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¾ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢s widget id when the screen is shown.
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
      - Widget-edit vault: driven by the widget instance ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾Ãƒâ€šÃ‚Â¢ [VaultDao.getVaultByAppWidgetId] path when opened from the pencil (may differ from the active app vault while editing).

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 6. V40.34 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â AUTH SURFACE UNIFICATION + SESSION CONTROL (LATEST STABLE)

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â· 7. DO NOT REGRESS (AUTH + SESSION HARD RULES)

    - Do NOT reintroduce automatic auth bypass on app resume.
    - Do NOT set background timeout stamps from generic lifecycle edges that can fire during overlays (`onStop`).
    - Do NOT expose biometric login button when `Allow biometrics for login` is disabled.
    - Do NOT split auth state ownership across multiple screen-scoped auth viewmodels.
    - Do NOT remove `Never` from login timeout options.
    - Do NOT bypass `UNLOCK_VAULT` from HOME login flow for standard credentialed login.
    - Do NOT silently swallow biometric errors; surface user-friendly messages.
    - Do NOT break portrait lock in `MainActivity` manifest contract.

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­ 8. AUTH SOURCE OF TRUTH (FILES + SYMBOLS)

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âª 9. V40.35 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â AUTH RELIABILITY HARNESS (LATEST TEST SAFETY NET)

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 10. V40.36 BLUEPRINT ROADMAP TRACK (LOCALIZATION + BILLING + TIERS)

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 11. V40.38 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â RECOVERY + PASSWORD MANAGEMENT + THEME PARITY

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âª 12. V40.39 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â SHARED PASSWORD POLICY + AUTH GUARDRAIL TESTS

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 13. V40.40 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â LOCALIZATION FOUNDATION (PHASE 1)

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â· 14. V40.41 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â KOREAN LANGUAGE EXTENSION

    - Locale expansion:
      - Added Korean (`ko`) to Settings language selector.
      - Added Korean (`ko`) to Home-screen globe quick switcher.
    - Resource coverage:
      - Added `values-ko/strings.xml` with Korean language selector strings.
      - Added `language_korean` to default and Spanish bundles for option label consistency.
    - Validation:
      - Lint clean and debug Kotlin compile successful after integration.

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­ 15. V40.42ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œV40.44 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â LOCALIZATION SURFACE EXPANSION + LOCALE STABILIZATION

    - V40.42:
      - Migrated high-visibility Home/Unlock/Create/Restore/Settings/Portfolio Manager/Theme Manager literals to string resources.
      - Expanded Korean resource coverage for those migrated keys.
    - V40.43:
      - Resolved locale host crash and stabilized runtime locale application path in `MainActivity` for non-AppCompat theme stack.
    - V40.44:
      - Continued migration across Terms, Holdings, Amount Entry, and Widget Manager visible labels/dialogs/tabs.
    - Validation:
      - Lint clean on touched files and debug Kotlin compile passed.

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ 16. THE FUTURE PATH (CURRENT WORK-IN-PROGRESS)
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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¹ 17. V40.45 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â LANGUAGE PICKER FINAL CLEANUP

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 18. V40.46 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â TOP-20 LANGUAGE EXPANSION + TRANSLATION FEEDBACK INTAKE

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 19. V40.47 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â LOCALIZATION PHASE 2 (TOP-20 KEY PARITY)

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

ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 20. V40.48 ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â LOCALIZATION QUALITY PASS (BATCHES A-D)

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
