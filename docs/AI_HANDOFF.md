# Swanie’s Portfolio — AI handoff (single document)

**One file for the next Cursor agent:** product stance, engineering snapshot, what changed recently, what to do next, and where to look in code.  
**You maintain this file** when the owner says things like *“update the handoff”* or *“update the narrative and push.”*

### Nudge the owner (Cursor AI — keep GitHub current)

The owner wants the **remote repo** to stay aligned with reality (easy to forget when sessions go well). **Proactively** remind them, in **one short sentence**, to say **Update the handoff & push** when:

- You reach a **natural closure** (task done, PR merged, decision recorded, good stopping point).
- There is a **clear win** (shipped fix, Play/listing progress, site change, checklist tick, etc.).
- **End of day (EOD)** — always remind before the session ends, even if the day was quiet (note “no code changes” in the handoff if true).

Do not lecture; a single nudge is enough. If they decline, respect that.

---

## How to update this doc (agent checklist)

1. Bump **Last updated** (date) at the top of **§ Current session**.
2. Edit **§ Current session** — where we are, blockers, wins this session.
3. Edit **§ Next steps** — numbered priorities (keep them honest).
4. Add a short bullet under **§ Session history** (newest first) summarizing this session.
5. Adjust **§ Engineering snapshot** or **§ Quick file map** only if the codebase or priorities actually changed.
6. If a milestone closed, tick **`Master_Build_Checklist.md`** (Play path / shipped items) and optionally **`Narrative_Log.md`** one-liner — those stay separate *lists*, not duplicate prose.
7. **`git add docs/AI_HANDOFF.md`** (+ any checklist/log you touched) → **`git commit`** → **`git push`**.

---

## Current session

**Last updated:** 2026-05-16 — **Widget polish + marketing site sticky header (owner verified).** **Widget (`PortfolioWidget.kt`):** metal rows **3-line** compact type + **`WidgetAssetCardHeight` = 62dp** + weighted row spacers (no **`fillMaxHeight()`** on row wrapper); swan tap **`widgetLaunchMainActivityIntent`** (**`NEW_TASK` + `REORDER_TO_FRONT`** — cold start or bring task forward); larger refresh hit target (**52×48dp**). **Website (`website/`):** restored **sticky** **`.site-header`** (moved outside **`.wrap`**; removed **`overflow-x`** on **`html`/`body`** — it broke sticky); scroll underline via **`html.is-scrolled`**; horizontal clip on **`.wrap`** only. **Push `main`** → **GitHub Actions** deploys **`https://swaniedesigns.com`**. **Next (shipping):** Play internal **`versionCode` 3** AAB — **§ Next steps** item **1**.

### Resume when you reopen (RevenueCat + Play only)

| Where | State |
|-------|--------|
| **RevenueCat** | App **Swanies Portfolio (Play Store)** · package **`com.swanie.portfolio`** · **Service account JSON uploaded** · dashboard shows **Valid credentials** (per-API checks resolved). **Google developer notifications** (Pub/Sub) still **optional** — **Select…** / **Connect to Google** can wait until after first Play-delivered billing QA. |
| **Google Cloud** | Project **`gen-lang-client-0826062826`** (“Default Gemini Project” in UI). **Google Play Android Developer API** and **Google Play Developer Reporting API** — **Enabled** (owner verified). Service account **`RevenueCat Play billing sync`** · email **`revenuecat-play-billing-sync@gen-lang-client-0826062826.iam.gserviceaccount.com`**. JSON key on disk (do **not** commit). |
| **Play Console** | Developer **8313761372289928213** · **Users and permissions** shows **2 users**, both **Active** (you + service account). **Invite URL** used: `https://play.google.com/console/developers/8313761372289928213/users-and-permissions/invite`. Service account **Account permissions** (grant for all apps): **View app information and download bulk reports (read-only)** + **View app quality information (read-only)** (check **read-only pair first** if financial boxes fight the UI) + **View financial data…** + **Manage orders and subscriptions**. **App permissions**: **Swanie's Portfolio** with financial + orders + view app info as applied. |
| **Ship next** | RevenueCat → **API keys** → **`goog_…`** → **`local.properties`** **`REVENUECAT_PUBLIC_API_KEY=`** (never commit) if not already set → signed **release AAB** → **Internal testing** new release (**repo** **`versionCode` 3** / **`1.0.2`** — replace Play’s **2** when uploaded). |

**Android repo (already on `main`):** Release builds use **`REVENUECAT_PUBLIC_API_KEY`** from **`local.properties`**; debug uses **`REVENUECAT_API_KEY`** (`test_…`). **`app/build.gradle.kts`**: **`versionCode` 3**, **`versionName` `1.0.2`**. Internal Play track previously had **2 (1.0.1)**; next store upload should be **3** once **`goog_`** is set and bundle built.

**Play Console — where things stand (human progress):**

- **Data safety:** Questionnaire **saved** (optional **Export to CSV** archive); canonical answers remain **`§ Play Data safety — facts from codebase`** below.
- **Declarations / App content:** Owner stepped through multiple forms (**financial features**, **advertising ID** = no ads SDK / no **`AD_ID`** in manifest, **health**, **government apps**, store listing–adjacent tasks). Continue until **Dashboard** shows nothing blocking.
- **Store settings:** **Finance** category; tags (**Cryptocurrency**, **Investment**, **Personal finance**, **Productivity**); **contact email** + **`https://swaniedesigns.com`** (phone optional); **external marketing** opt-in per preference.
- **Default store listing (en-US):** **App name** + **full description** — canonical draft **`docs/play_store_long_description_en-US.txt`** (~**3948** characters on Windows checkout / **4000** Play cap — re-count in Console before save). **Short description** — Play may flag wording that looks like **ranking / performance** claims (e.g. rewrite **"local-first"** if **"first"** triggers the automated hint); finish **screenshots**, **512 app icon**, **1024×500 feature graphic** uploads.
- **Listing assets (repo):** **`website/play_store_app_icon_512.png`**; **`website/play_store_feature_graphic_1024x500.png`** (required size); optional **`website/play_store_feature_graphic_1024x512.png`**; regenerate feature banner with **`scripts/compose-play-feature-graphic.ps1`** from **`website/play_store_feature_icon_1024x512.png`** (strip edge BG → **`#000416`**, centered scale).
- **Publishing overview:** **`Send app for review`** stays **disabled** until **Dashboard** + **store listing** requirements are complete — then bundle pending changes.
- **Testing path:** **Internal testing** release **2 (1.0.1)** on Play + **Swanie's Portfolio Testers**; **`website/index.html`** **`TESTER_URL`**. **Next:** upload **AAB `versionCode` 3** (RC credentials **valid**; ensure **`goog_`** in **`local.properties`** for release builds). Opt-in link on site.

**Marketing site (2026-05-11 + 2026-05-16):** **Sticky header** — **`.site-header`** sibling of **`.wrap`** (not inside **`overflow-x: clip`** on wrap); **`html.is-scrolled`** gold underline on scroll; do **not** put **`overflow-x: hidden`** on **`html`/`body`** (breaks sticky). **Screenshots** carousel, **feature grid** breakpoints, **QR** responsive sizing, **`overscroll-behavior-x`** on **`html`** (Firefox). **`index.html`** / **`privacy.html`** / **`press.html`** share layout.

**Repo hygiene (2026-05-12):** Owner moved to **laptop-only** dev (dropped dual Studio installs and laptop/desktop pairing). **`47e6f40`**: **`gradle/libs.versions.toml`** accidental **AGP 9.2.1** bump **reverted** to **9.2.0**; **`.idea/assetWizardSettings.xml`** **removed from git** (Image Asset Wizard noise); **`.gitignore`** — correct **`/.idea/assetWizardSettings.xml`** (old pattern had a typo), **`/docs/drawable-backups/`** for local PNG experiments; untracked **`docs/drawable-backups/`** folder **deleted** from working tree.

**Earlier (2026-05-12):** **`docs/play_store_long_description_en-US.txt`** — Play **en-US** full listing copy (~**3948** chars); **`docs/AI_HANDOFF.md`**; **push `main`**.

**Locale / Play terms (2026-05-12, agent):** **`terms_last_updated`** + **`terms_section_1`–`terms_section_6`** refreshed in **every `values-*`** to match Play-aligned default English (who we are / how data is stored / optional network / data limits & access / not financial advice / children, language, policy changes). **`terms_section_7_*`** left as already present per locale. **Check:** all locale **`strings.xml`** files contain the same **514** string keys as **`values/strings.xml`** (no missing, no extras). **Build:** **`:app:compileDebugKotlin`** OK on agent checkout. **Commit + push `main`** with **`docs/AI_HANDOFF.md`** update.

**i18n maintenance:** Re-sync **`values-*`** whenever **default `values/strings.xml`** changes meaning again. **`website/`** is deployed separately from app strings—align **`privacy.html`** / marketing copy on its own timeline when English there shifts.

### Play Data safety — facts from codebase (canonical for Console answers)

Use this table so **Data safety** matches the wired app (AI-built; owner should not guess). Full detail lives in code paths cited below.

| Topic | What the app actually does |
|-------|----------------------------|
| **Permissions** | **`AndroidManifest.xml`**: **`INTERNET`**, **`ACCESS_NETWORK_STATE`**, **`USE_BIOMETRIC`** only — **no** location, contacts, calendar, broad storage/camera. |
| **Local-first data** | Portfolio + profile in **Room** (`UserProfileEntity`: username, email, password, password hint, etc.). No cloud vault sync in current release (see **`website/privacy.html`**). |
| **RevenueCat** | **`RevenueCatMonetizationManager`** / **`MainViewModel.syncMonetizationUser`**: `Purchases.logIn` uses **`email`** (lowercased) when set, else **`userName`** as RevenueCat **App User ID**. Treat **email** and **user IDs** as **collected** and **shared** with **RevenueCat** (plus Play billing). **Device / install identifiers** — RevenueCat SDK: declare **Device or other IDs** as applicable; **shared** with RevenueCat/Google when the form asks. |
| **Purchase history** | **Yes** — subscriptions via RevenueCat + Play (`purchasePackage`, `restorePurchases`, entitlement checks). |
| **Bug / feedback** | **`BugReportSubmitter`** POSTs to **`formsubmit.co`** with **`email`** (reply field) + message body — **third party**; counts as **sharing** email when user submits feedback. |
| **Crash / analytics SDKs** | **`app/build.gradle.kts`**: no Firebase/Crashlytics; major SDK besides AndroidX is **`libs.revenuecat.purchases`**. In-app **“Analytics”** screen is **portfolio analytics UI**, not Firebase. |
| **Photos** | **`HoldingsUIComponents`** — **`PickVisualMedia`** + **uCrop** for **custom asset icons** → declare **Photos** if users use it. |
| **Files** | **`VaultBackupEngine`** / **`BackupRestoreScreen`** — user-driven **`.swpb`** via SAF → **Files and docs**. |
| **Search** | CoinGecko/Binance/etc. — **query strings** for assets; not user profile PII. |
| **Purposes** | **App functionality**, **Account management** for account/email; **not** “Developer communications” unless the **developer** proactively mails marketing — replying to feedback is support, not that category. |

**Code anchors:** `MainViewModel.kt` (`syncMonetizationUser`), `RevenueCatMonetizationManager.kt` (`setAppUser` / `awaitLogIn`), `BugReportSubmitter.kt`, `data/di/NetworkModule.kt` (API base URLs), `AndroidManifest.xml`.

**Website / Play Data safety — deletion URLs:** **`website/privacy.html`** **§4** includes (1) **`#account-deletion`** — local-only story, **uninstall** / **Clear storage** / **factory reset**, **Google Play** subscriptions, support via Play listing; (2) **`#data-deletion`** — removing **some** portfolio data **in-app** (holdings / vaults) without uninstalling. **Effective date** in file **2026-05-08**. **Owner:** after **GitHub Actions** deploys **`main`**, paste **`https://swaniedesigns.com/privacy.html#account-deletion`** into **Delete account URL**, and if Data safety asks for **delete data without deleting account** (**Yes**), paste **`https://swaniedesigns.com/privacy.html#data-deletion`** into **Delete data URL** (or adjust path if Pages domain differs). **Follow-up:** sync **in-app** copy in **`values/strings.xml`** + **`values-*`** when About / privacy / settings should match (see **§ Next steps**).

**Portfolio toast chip:** **`toast_chip_background.xml`** — solid opaque **`@color/launcher_navy`** (`#000416`); **`showPortfolioToast`** / **`toast_portfolio.xml`** unchanged.

**Adaptive launcher + fingerprint (owner verified):** Vector **`drawable/swan_launcher_extra_small_hq.xml`** — **`108×108`** viewport, **1104×859** art, **nested `<group>`** (scale **~0.0554** + translate); **mipmap** **`foreground`** → **`@drawable/swan_launcher_extra_small_hq` directly** (no **`InsetDrawable`** on adaptive foreground); **`ic_launcher_foreground.xml`** = thin **`layer-list`** alias. **If the icon looks wrong after a shallow or partial sync:** **`git pull`** **`main`** and ensure **mipmap + drawable** wiring matches **`main`**; vector XML alone is not enough.

**Also shipped (same week):** **Portfolio toast** plumbing — **`showPortfolioToast`** + **`toast_portfolio.xml`**; **Home** language **slow slide from left** (**`LOGIN_COLUMN_ENTER_*`**, **`LANGUAGE_GLOBE_AFTER_LOGIN_MS`**).

**Icons elsewhere:** Splash / toast / widget still use **PNG + wrapper XML** as before; Image Asset wizard → **PNG** paths.

**Recently shipped (same week):** Marketing site screenshot captions; truthful Drive copy; TOS §7 / privacy §8; **`AssetViewModel`** Drive comments honest.

**Product:** Android app **Swanie’s Portfolio** — crypto & precious metals tracker. Owner considers the app **feature-complete for v1** (**feature freeze**). Remaining work is **shipping** (Play Console, compliance, listing, AAB, RevenueCat/Play QA), not new product features unless the owner reopens scope.

**Repo / branch:** `swanies-portfolio-gemini-ver` on GitHub (`swanie2000`), default branch **`main`**. Legacy repo **`swanies-portfolio`** was deleted.

**Public site:** **`https://swaniedesigns.com`** — static marketing + privacy page from **`website/`**, deployed by **GitHub Actions** (`.github/workflows/deploy-website.yml`). Custom domain + **HTTPS** on GitHub Pages. **`website/privacy.html`**: **§4** with **`#account-deletion`** and **`#data-deletion`** (Play **Data safety** URLs as applicable), **§9** terms (mirrors in-app §7). Push **`main`** after edits so the live URL matches Play.

**Play / Google:** **Internal testing** release **2 (1.0.1)** published; **Testers** list **Swanie's Portfolio Testers** (owner adds new addresses in Console after **`mailto`** requests from site). **App created** in Play Console (**Swanie’s Portfolio**, **`com.swanie.portfolio`**). **Android developer verification:** Play apps **auto-registered** to the verified account — confirm on **Play Console → Home**. **By September 2026:** register any **extra signing keys** used outside Play (see [Android developer verification](https://developer.android.com/developer-verification)). **Closed testing** unlocks per Console when prerequisites met; **production** may require **≥12 testers × 14 days** closed test. **Release signing:** **`swanie_portfolio_release.jks`** — CLI **`keytool`** use **`-storetype PKCS12`** / Studio **JBR** if needed.

### Play Console — ordered steps (next session; do in order)

1. **Bump `versionCode`** in `app/build.gradle.kts` before each **new** Play upload (Play rejects duplicate codes). **Repo:** **`versionCode` 3** / **`1.0.2`** — use next number (**4+**) when uploading **after** **3** is on Play.
2. **Android Studio:** **Build → Generate Signed App Bundle** → **release** AAB with **`swanie_portfolio_release.jks`** (requires **`REVENUECAT_PUBLIC_API_KEY`** in **`local.properties`** for RC on device).
3. **Play Console → Internal testing:** Live track had **2 (1.0.1)**; ship **3** when bundle ready. **Create new release** → upload AAB → **Save** → **Roll out**.
4. **Website:** After **`main`** push, confirm **GitHub Pages** updated **`https://swaniedesigns.com`** (Actions). **`PLAY_URL`** in **`website/index.html`** when public listing exists.
5. **Test and release → Closed testing:** When unlocked — new release, invite **≥12** testers who **opt in**; run **≥14 days** for production-access requirement.
6. **Production:** When eligible, **Apply for production** / staged rollout per Console.
7. **Grow users → Store presence / Monetize with Play:** Screenshots, feature graphic, subscriptions + **RevenueCat** SKU alignment, **license testers** for `pro`.
8. **Publishing overview:** When **Send app for review** is enabled and you intend listing/metadata review — send; optional **Managed publishing** if you want manual go-live after approval.
9. **Website:** After **`privacy.html`** on **`main`** deploys to **Pages**, paste Play **Data safety** URLs: **`…/privacy.html#account-deletion`** (delete account), and if the form has **Delete data URL** (**Yes**), **`…/privacy.html#data-deletion`**. Remove **`[bracket]`** placeholders / **`noindex`** when listing is public; confirm **§9** liability wording with counsel if needed.

---

## Next steps (priority order)

1. **Play internal — ship `versionCode` 3:** Confirm **`REVENUECAT_PUBLIC_API_KEY`** (`goog_…`) in **`local.properties`**. **Android Studio** → **Generate Signed App Bundle** (**release**) → **Play Console → Internal testing** → new release → upload AAB → roll out. **Device QA:** install from Play (tester account) → **subscribe / restore** / Pro gates.
2. **Play (human) — listing:** Continue **default store listing** + **Publishing overview** when ready. **`Master_Build_Checklist.md`** when milestones tick.
3. **Verify cloud surfaces:** Confirm **GitHub Actions** deployed latest **`website/`** to **`https://swaniedesigns.com`** (`#join-testing`, tester form, **`TESTER_URL`** QR). Install **Swanie's Portfolio** from **Play** on a device using a listed tester account.
4. **i18n (maintenance):** **Privacy & Terms §1–§6** synced across **`values-*`** (2026-05-12). When **`values/strings.xml`** changes again, propagate keys; spot-check **About** before listing freeze. **`website/`** deploy is separate from app strings.
5. **Website:** Set **`PLAY_URL`** in **`website/index.html`** when the **public** Play listing URL exists (`TESTER_URL` + **`TESTER_REQUEST_EMAIL`** already set for internal path).
6. **Optional cleanup:** **`app/src/main/assets/adi-registration.properties`** (ADI); **`.idea`** **`assetWizardSettings.xml`** untracked — do not re-add without cause.
7. **Pre-launch QA:** Backup round-trip; purchase / restore / expiry; **widgets** (Pro **8**-row, metal **3-line** + **62dp** row height — verified on device 2026-05-16) + Pro gates; GRAM/KILO metals on device (Play-delivered build).
8. **Backlog (post-v1):** V40.36 auth instrumentation, V40.61 monetization telemetry, V40.69 small-screen polish — only if scheduled.
9. **Icons (optional / post-v1):** **`swan_launcher_extra_small_hq.xml`** scale/translate; splash/toast/widget wrappers.

---

## Engineering snapshot (v1 ship stack)

- **Stack:** Kotlin, Jetpack Compose, Hilt, Room.
- **Widget (Glance):** Pro **8** / free **3** holdings rows; per-line preference packing + pipe-tolerant parse; list column nests each row (**RemoteViews** direct-child limit); metal **`metalWidgetHeadlinePair`** (3 lines, **9sp** / **7sp**) + **`WidgetAssetCardHeight` (62dp)**; swan → **`widgetLaunchMainActivityIntent`**; **`__METAL_DEFAULT__`** / **`file:`** icon packing on push.
- **Pro:** RevenueCat + Play billing when on store; gates Theme Manager, multi-portfolio swipe, full Analytics, widget customization, etc.
- **Backup:** `VaultBackupEngine.kt` + `BackupRestoreScreen.kt` / `Routes.BACKUP_RESTORE` / `SettingsViewModel` — encrypted `.swpb`, WAL checkpoint via `query`, SAF, cold restart after restore.
- **Metals:** `MetalSpotMath.kt` + `AssetValuation` — GRAM/KILO/G → troy oz, USD valuation across holdings, analytics, `AssetRepository`, widget, theme, architect, settings.
- **Feedback:** `BugReportSubmitter` + `@Named("Feedback")` OkHttp in `NetworkModule`; Settings dialog; tag **`SwanieBugReport`** — POST **`formsubmit.co`** (email in form body).
- **Play Data safety:** See **§ Current session** → **Play Data safety — facts from codebase** (RevenueCat `logIn` id = email or username; purchases; local Room profile).
- **i18n:** `LanguageDisplay.kt`; maintained **`values-*`** — **2026-05-12:** Play-aligned **`terms_last_updated`** + **`terms_section_1`–`6`** in all locales; **514** keys match **`values/strings.xml`**. **`website/`** deploy via **GitHub Actions** on **`main`** (see **`index.html`** **`TESTER_URL`** / **`TESTER_REQUEST_EMAIL`** for internal testing UX).
- **Quality gates before “done”:** `:app:compileDebugKotlin`, `:app:lintDebug` (`app/lint.xml` policy).

---

## Quick file map

| Area | Start here |
|------|------------|
| Backup engine | `VaultBackupEngine.kt` |
| Backup UI | `BackupRestoreScreen.kt`, `SettingsViewModel.kt`, `Routes.kt`, `NavGraph.kt` |
| Settings / feedback | `SettingsScreen.kt`, `BugReportSubmitter.kt`, `NetworkModule.kt` |
| Metals / valuation | `MetalSpotMath.kt`, `AssetRepository.kt`, `HoldingsUIComponents.kt`, `MyHoldingsScreen.kt` |
| Home screen widget | **`PortfolioWidget.kt`** — **`WidgetAssetLimits`** Pro **8** / free **3**; **`writeWidgetPackedAssetRows`**; **`parseSingleWidgetAssetEntry`**; **`WidgetAssetCardHeight` = 62dp**; **`widgetLaunchMainActivityIntent`**; nested **`Column` + `defaultWeight()`** per row; **`AssetRepository.kt`**, **`SettingsViewModel.kt`**, **`WidgetManagerScreen.kt`**, **`WidgetConfigActivity.kt`** |
| Marketing site layout | **`website/styles.css`** — **`.site-header`** sticky + **`html.is-scrolled`** underline; **`.site-header-inner`** max-width **1080px**; **`overflow-x: clip`** on **`.wrap`** only (not **`html`/`body`**) |
| App / splash / toast | **Adaptive icon:** **`mipmap-anydpi-v26/ic_launcher.xml`** + **`ic_launcher_round.xml`** (foreground **`@drawable/swan_launcher_extra_small_hq`**); **`drawable/swan_launcher_extra_small_hq.xml`** (vector + group transforms); **`drawable/ic_launcher_foreground.xml`** (layer-list alias). **`swan_splash_icon_wrapper.xml`**, **`ic_toast_swan.xml`**, **`swan_widget_icon_padded.xml`**; **toasts:** **`CustomToast.kt`** (`showPortfolioToast`) + **`layout/toast_portfolio.xml`** + **`toast_chip_background.xml`** (solid **`launcher_navy`** chip); **SVG → vector scripts:** **`scripts/svg_path_to_vector.mjs`** / **`.py`** |
| Home (login) | **`HomeScreen.kt`** — swan hero, **`AnimatedVisibility`** login column, language globe slide-in timing |
| Pro / billing | **`app/build.gradle.kts`** — release **`REVENUECAT_API_KEY`** from **`REVENUECAT_PUBLIC_API_KEY`** in **`local.properties`**; debug from **`REVENUECAT_API_KEY`** (`test_`). **`billing/`**, **`RevenueCatInitializer.kt`**, **`RevenueCatMonetizationManager.kt`**, **`MonetizationModule.kt`** |
| About / legal | `AboutScreen.kt`, `TermsAndConditionsScreen.kt` (§1–§7), `Routes.kt`, `MainActivity.kt`, `values/strings.xml` + `values-*` (incl. **`terms_section_7_*`** per locale) |
| Marketing site | `website/` — **`index.html`** (**`PLAY_URL`** / **`TESTER_URL`** / **`TESTER_REQUEST_EMAIL`**, **QR**, carousel), **`privacy.html`**, **`press.html`**, **`styles.css`** (see **Marketing site layout** row), **`.github/workflows/deploy-website.yml`** → **`https://swaniedesigns.com`** |
| Play Data safety (truth from code) | **`§ Current session`** → **Play Data safety — facts from codebase**; **`MainViewModel.kt`** (`syncMonetizationUser`), **`billing/RevenueCatMonetizationManager.kt`**, **`data/feedback/BugReportSubmitter.kt`**, **`AndroidManifest.xml`**, **`app/build.gradle.kts`** (deps) |
| Play listing copy (en-US full description) | **`docs/play_store_long_description_en-US.txt`** — paste into Play Console default listing (**4000** char max; draft ~**3948** on Windows checkout) |
| Play checklist | `Master_Build_Checklist.md` |
| Play ADI challenge file | `app/src/main/assets/adi-registration.properties` (verification token; optional to remove after registration approved) |
| Cursor rules | **`.cursor/rules/git-pull-first.mdc`** (pull before edits), **`update-handoff.mdc`** (handoff + push trigger) |
| Repo / IDE noise | **`.gitignore`** — **`/.idea/assetWizardSettings.xml`**, **`/docs/drawable-backups/`** (local icon raster dumps; not shipped) |

---

## Session history (newest first)

- **2026-05-16 — Widget UX + website sticky header + push:** **`PortfolioWidget.kt`** — **`widgetLaunchMainActivityIntent`** (swan opens or foregrounds app); larger refresh tap target; metal **62dp** rows (prior session). **`website/`** — sticky **`.site-header`** outside **`.wrap`**, **`html.is-scrolled`** underline, fix **`overflow-x`** regression on **`html`/`body`**. Owner verified widget + site behavior. **`docs/AI_HANDOFF.md`** + **push `main`** (Pages deploy).
- **2026-05-16 — Widget metal card layout + handoff + push:** **`PortfolioWidget.kt`** — metal rows keep **3 lines** (**`metalWidgetHeadlinePair`**) at **9sp** / **7sp**; uniform **`WidgetAssetCardHeight` = 62dp** so weighted gap below each card matches (tuned from 52→62; **61** still clipped **$**). Restored **`defaultWeight()`** row + trailing spacer for resize fill; removed **`fillMaxHeight()`** on row wrapper (had collapsed list to one visible card). Owner verified: full-height widget, **8** assets, silver bar price visible, even spacing. **~45 min** iteration. **`docs/AI_HANDOFF.md`** + **push `main`**.
- **2026-05-14 (EOD) — RevenueCat valid + GCP APIs + handoff note:** Owner: RevenueCat **Configurations** shows **Valid credentials**; **Google Cloud** (**Default Gemini Project** / **`gen-lang-client-0826062826`**) — **Google Play Android Developer API** + **Google Play Developer Reporting API** **Enabled**. **`docs/AI_HANDOFF.md`** — **Last updated**, **Resume** table, **Testing path**, **Next steps** item **1** = ship **internal AAB `versionCode` 3** + billing QA; optional **Google developer notifications** called out. **Push `main`**.
- **2026-05-11 (EOD) — Handoff + push (laptop / tabs close):** **`docs/AI_HANDOFF.md`** — **§ Current session** resume table (**RevenueCat** ↔ **Play** service account: **subscriptions** OK, **inappproducts** + **monetization** still “need attention” / propagation); explicit **reopen = this file** after **`git pull`**. **§ Next steps** — RevenueCat+Play first; fixed duplicate numbering. **§ Play ordered steps** + testing path + **Quick file map** billing row aligned with **`main`** (**`030ed23`**: release **`REVENUECAT_PUBLIC_API_KEY`**, **`versionCode` 3** / **`1.0.2`**). **Push `main`**.
- **2026-05-16 — Play internal + website tester flow + handoff (EOD):** **Play Console** — internal release **2 (1.0.1)** rolled out; **Swanie's Portfolio Testers** list; opt-in link on marketing site. **`website/index.html`** + **`styles.css`** + **`README.md`** — **`TESTER_URL`**, **`TESTER_REQUEST_EMAIL`**, mailto request form + manual-add copy; **`docs/AI_HANDOFF.md`** — **§ Current session**, **§ Next steps**, **§ Play ordered steps**, **Quick file map**, **Engineering snapshot**. Prior commits on **`main`**: **`swan_asset_toast.jpg`** rename (release AAPT fix); **`versionCode` 2**; laptop-only rule. **Push `main`**.
- **2026-05-15 — Laptop-only dev (docs + Cursor rule):** Owner: **all** Swanie’s Portfolio work on **one laptop** (no laptop/desktop pair). **`docs/AI_HANDOFF.md`** — **§ Current session**, **§ Working agreements**, adaptive icon note, session-history wording; **`.cursor/rules/git-pull-first.mdc`** — pull rationale = **GitHub ↔ laptop**, not multi-PC. **Push `main`**.
- **2026-05-14 — Metal headlines + widget metal polish + handoff + push:** **`HoldingsUIComponents.kt`** — **`metalWidgetHeadlinePair`** (space / **`_`** / camelCase / **`SILVERBAR`**-style run-on); collapsed compact + expanded under-icon **two-line** metal titles (no mid-word wrap); **`metalShouldShowSymbolSubtitle`** unchanged for expanded ticker when applicable. **`PortfolioWidget.kt`** — **`AssetCardOriginal`** same headline pair + custom **`file:`** icon plate and crop. **`AssetRepository`** + **`SettingsViewModel`** — widget pack **`file:`** / local path before **`__METAL_DEFAULT__`**. Owner verified. **`docs/AI_HANDOFF.md`**. **Push `main`**.
- **2026-05-13 — Pro widget 8 rows + Glance/RemoteViews fix + handoff + push:** **`WidgetAssetLimits.kt`**; **`AssetViewModel`** (**`widgetAssetCap`**, tier trim); **`WidgetManagerScreen`**, **`WidgetConfigActivity`**; **`AssetRepository`** + **`DatabaseModule`** (**`MonetizationManager`**, **`pushFreshAssetsToWidget`** sets **`IS_PRO_USER_KEY`**, per-line **`writeWidgetPackedAssetRows`**); **`SettingsViewModel.triggerWidgetUpdate`**; **`PortfolioWidget`** (**`parseSingleWidgetAssetEntry`**, **`WidgetContent`** one nested **`Column` per asset** — fixes **~5** visible rows from **2×N** children hitting RemoteViews **~10** cap); **`values/strings.xml`** + **`values-*`** (widget copy / toasts). Owner verified. **Handoff + push `main`.**
- **2026-05-12 — Locale terms (Play) + handoff + push:** **`values-*`/`strings.xml`** — **`terms_last_updated`** + **`terms_section_1`–`6`** aligned to default English (Play listing contact, storage, optional network, data limits, not advice, children/language/policy); **514**-key parity vs **`values/strings.xml`**. **`docs/AI_HANDOFF.md`** — **§ Current session**, **§ Next steps** (i18n), **§ Engineering snapshot**. **Push `main`**.
- **2026-05-12 — Play en-US long description + handoff:** **`docs/play_store_long_description_en-US.txt`** — expanded **Google Play** default **full description** (~**3948** / **4000** chars). **`docs/AI_HANDOFF.md`** — **§ Current session** (listing bullet), **Quick file map**, **§ Session history**. **Push `main`**.
- **2026-05-12 — Repo hygiene + handoff:** **`47e6f40`** — **`gradle/libs.versions.toml`** AGP **9.2.0** (revert stray **9.2.1**); **`git rm --cached`** **`.idea/assetWizardSettings.xml`**; **`.gitignore`** fix **`assetWizardSettings.xml`** + **`docs/drawable-backups/`**; delete local **`docs/drawable-backups/`**. Owner: **laptop-only** dev. **`docs/AI_HANDOFF.md`** + **push `main`**.
- **2026-05-11 (EOD) — Marketing site mobile + handoff:** **`website/index.html`** + **`website/styles.css`** — screenshot **two-line captions**, **carousel** (**arrows below** strip; **no** hover auto-scroll), **Firefox** **overscroll** / horizontal overflow clamp, **QR** responsive draw + **resize**, **feature** grid **1→2→3** columns, **QR cards** flex + **`.tester-qr-frame`** **`max-width`**. **`docs/AI_HANDOFF.md`** — **§ Current session**, **§ Next steps**, **Quick file map**. **No Android app edits this session.** **Push `main`**.
- **2026-05-11 — Handoff only (Play Console snapshot):** **`docs/AI_HANDOFF.md`** — **§ Current session** Play progress (declarations, store settings, listing, graphics paths, Publishing overview gate, testing path); **§ Next steps** tightened; **Quick file map** Play assets on **`website/`**. **No app code changes that day.** **Push `main`**.
- **2026-05-10 (EOD) — Data safety completed + handoff:** Owner finished **Google Play → Data safety** questionnaire (saved; preview + optional **Export CSV**). **`Send for review`** blocked until **Dashboard** tasks — documented in **§ Current session** / **§ Next steps**. **`docs/AI_HANDOFF.md`** — **i18n** raised to honest priority (**`values/strings.xml`** + **`website/`** ahead of **`values-*`**). **Push `main`**.
- **2026-05-10 — Play Data safety canon + handoff:** **`docs/AI_HANDOFF.md`** — new **§ Play Data safety — facts from codebase** (permissions, RevenueCat **`logIn`** email/username, FormSubmit, purchases, photos/SAF backup, no Crashlytics in **`build.gradle.kts`**); **§ Next steps** + **Quick file map** pointer. **Purpose:** single source for Google Play **Data safety** so owner does not guess AI wiring. **Push `main`**.
- **2026-05-10 — Privacy `#data-deletion` + handoff (Play Delete data URL):** **`website/privacy.html`** — subsection **`id="data-deletion"`** (in-app partial removal: holdings / vaults) for optional Play **Data safety → Delete data URL** when **Yes** to deleting data without deleting account; **`#account-deletion`** unchanged. **`docs/AI_HANDOFF.md`** — **§ Current session**, ordered step **9**, **§ Next steps**, **Quick file map**. Site change was on **`main`** (`14258fb`); this session **handoff + push** so owner can paste **`https://swaniedesigns.com/privacy.html#data-deletion`** after **Actions** deploy.
- **2026-05-09 — Privacy §4 + handoff (Play Delete account URL):** **`website/privacy.html`** — **§4 Account and data deletion** (`#account-deletion`), section renumber, effective date **2026-05-08**; supports **Data safety** “Delete account URL” after **Pages deploy**. **`docs/AI_HANDOFF.md`** — Play progress note, **§ Next steps** item for **i18n** parity (`values` / `values-*`) with deletion verbiage when in-app copy should match site. **Push `main`** for live site.
- **Play / verification:** Google email — Play apps **auto-registered** to verified developer account; **Sept 2026** deadline for extra keys / sideload registration noted in **`docs/AI_HANDOFF.md`** + **`Master_Build_Checklist.md`**. Next human step: **internal testing AAB** (ordered steps in handoff).
- **2026-05-09 — Workflow: git pull first:** Owner rule — **`git pull`** before substantive edits; **`.cursor/rules/git-pull-first.mdc`** + **Working agreements** updated. **`docs/AI_HANDOFF.md`** + push.
- **2026-05-09 — Toast chip solid navy:** **`toast_chip_background.xml`** → opaque **`@color/launcher_navy`** (was **`#CC000000`**). Owner prefers look. **`docs/AI_HANDOFF.md`** + push.
- **2026-05-09 — Adaptive vector launcher + fingerprint (owner verified):** **`swan_launcher_extra_small_hq.xml`** on **`main`** with **108×108 viewport**, **group** scale/translate (no **`InsetDrawable`** on adaptive foreground); **mipmap** foreground points **direct** at vector; **~10%** scale-down for launcher margin. **`scripts/svg_path_to_vector.*`** (CLI input/output). **`docs/AI_HANDOFF.md`** + push. *(At the time, a partial second checkout did not show the full adaptive stack until **`git pull`** + full **mipmap/drawable** wiring.)*
- **2026-05-08 (EOD) — Portfolio toast + home globe:** **`showPortfolioToast`** + **`toast_portfolio.xml`** / **`toast_chip_background`**; wired across settings flows; **36dp** swan. **HomeScreen** language control **slow slide from left** after login buttons **`tween(800,1600)`** finish. **`docs/AI_HANDOFF.md`** + push.
- **2026-05-08 — Icon pipeline lock-in (owner frustrated, EOD):** Per-surface **`swan_asset_*.png`** copies; **`ic_launcher_foreground`**, splash/toast/widget XML; toast asset **`ic_toast_swan`**; symmetric insets + comments; **fingerprint = same `ic_launcher` as launcher** (OS rendering differs). **`docs/AI_HANDOFF.md`** + **push `main`**.
- **2026-05-09 — Screenshot captions (mobile):** **`website/index.html`** — shorter **`figcaption`** lines under the four device shots; **`website/styles.css`** — **`shot-card`** column flex + centered caption, **`max-width`** / **`text-wrap: balance`**, **`@media (max-width: 480px)`** tweak. **`docs/AI_HANDOFF.md`** + push.
- **2026-05-08 — Truthful copy + TOS §7 + i18n:** Marketing and in-app/legal strings no longer claim **live Google Drive vault sync** (stub only; deferred); **website** index/press/privacy aligned; **§7** limitation of liability / indemnity in **`values/strings.xml`**, **`TermsAndConditionsScreen.kt`**, **`privacy.html`** §8; **all 19 locales** carry **`terms_section_7_*`** (+ prior Drive-truth strings); **`AssetViewModel`** Drive-sync comments honest; **`setting_sync_drive`** “planned” label. **Handoff + push** with this commit.
- **2026-05-08 — Play playbook:** Owner created **Play app**; **Dashboard** shows setup tasks done; **Internal testing** still missing **AAB release**; **Publishing overview** / **Send for review** gated until Console allows. Captured **ordered next-session steps** in **§ Current session** (internal → closed → production → listing/monetization → privacy site).
- **2026-05-07 — Marketing site lock-in:** **`website/ic_swan_website.png`** in header + two-line title/tagline aligned with **`home_title`** / **`home_subtitle`**; **`favicon-tab.png`** for tab / apple-touch (**`#000416`** plate + centered swan); screenshot grid **JPEGs** in **`website/images/`** (four cards incl. widget manager); **`.gitignore`** **`*.aab`** + **`/app/release/`**; **`docs/AI_HANDOFF.md`** refreshed (**Update the handoff & push**).
- **2026-05-07 — Widget metal parity:** **`PortfolioWidget.kt`** **`AssetCardOriginal`** now uses **`metalCardPrimaryLabel`** / **`metalShouldShowSymbolSubtitle`** (same as compact/full holdings cards). **`SettingsViewModel`** widget serialization uses **`AssetValuation.cardPriceRowUsd`** for per-line spot string to match **`AssetRepository.pushFreshAssetsToWidget`**. Owner confirmed widget behavior OK.
- **2026-05-05 — Release signing closure:** Confirmed **`swanie_portfolio_release.jks`** unlocks with **`keytool`**; **`app\release\app-release.aab`** signer **SHA256** matches keystore (release path aligned with Play’s separate **debug** package-registration proof). Noted **`keytool -storetype PKCS12`** / Studio **JBR** vs PATH JDK for future CLI checks. Owner pausing for the day.
- **2026-05-04 (EOD) — Package name registration:** Play **Android developer verification** — eligible cert was **debug** SHA-256 (not new release `.jks`). Added **`adi-registration.properties`** under **`app/src/main/assets/`**, removed bogus **`androidTest`** duplicate **`test holding file.kt`** that blocked **`assembleDebug`**. Owner **submitted** registration; Console **In review**. Release keystore path: **`AndroidStudioProjects\Android-Signing\swanie_portfolio_release.jks`**.
- **2026-05-04 — Play verification:** Google confirmed **driver’s license**; owner completed **email** + **phone** verification in Play Console. Ship track moves to listing, compliance, AAB tracks, and RevenueCat alignment (see checklist).
- **2026-05-03 — Repo cleanup:** Deleted `docs/BROWSER_CONTEXT_NARRATIVE.md`, `docs/BROWSER_CONTEXT_MASTER.md`, `docs/BROWSER_CONTEXT_HEADER.txt` (browser-era bundle). Canonical doc remains **`docs/AI_HANDOFF.md`**; old prose recoverable from **git history** only.
- **2026-05-03 — Cursor reminder policy:** Instructed agents to nudge **Update the handoff & push** at closure, after wins, and **always EOD** (see § Nudge the owner above); Cursor rule updated to match.
- **2026-05-03 — Handoff simplification:** Retired multi-file browser bundle (dump scripts, `DUMP.md`). **Canonical handoff = this file only** (`docs/AI_HANDOFF.md`). `START_HERE_FOR_AI.md` and **Update the Handoff** Cursor rule updated. *(Same period: legacy `BROWSER_CONTEXT_*` files later deleted from tree — see **Repo cleanup** in session history.)*
- **2026-05-03 — V40.73 / public site:** `https://swaniedesigns.com` live (Pages + Cloudflare); `deploy-website.yml` build/deploy fix; `website/CNAME`; legacy GitHub repo removed. *(Identity was still pending Google until 2026-05-04.)*
- **2026-05-02 — V40.72:** i18n MissingTranslation closure (64 keys × 19 locales); ship-only narrative handoff.
- **2026-05-02 — V40.71:** Metal spot pipeline, backup screen split, bug reports, About, i18n wave; Play fee paid + identity submitted.

*(Older milestone detail: search **git history** for removed paths under `docs/`, or read milestone bullets in `Narrative_Log.md`.)*

---

## Working agreements (Cursor)

- **Git first (owner + agents):** **`git pull`** from **`origin`** (usually **`main`**) **before** starting substantive edits — keeps GitHub as source of truth for the **single laptop** dev checkout. Cursor rule **`.cursor/rules/git-pull-first.mdc`** reinforces this.
- Prefer **minimal, safe edits**; don’t refactor unrelated code.
- Don’t assume files exist — read before changing.
- **Canonical state for the next agent** = **this file** + the actual repo. If something disagrees with code, **code wins** — then fix this doc.
- Browser-era paste bundles are **removed** from the tree; use **git** if you need old prose.
