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

**Last updated:** 2026-06-01 — **Ready for Testers Community:** Internal **23 (1.0.23)** live with **auto-Pro** (**`GRANT_DAYS=30`**, expiry **~July 1** baked at build). Owner verified **22** (**grant=0**, Pro off) and **23** (Pro on + paywall dialog). **Beta unlock removed**; **`ClosedTestProAccess`** + **`verify-play-release.ps1`**. Site **`TESTER_URL`** QR restored (**`938dc58`**). **Next:** promote **23** to **Closed testing** → submit opt-in URL to **Testers Community**.

### Resume when you reopen (RevenueCat + Play)

| Where | State |
|-------|--------|
| **RevenueCat** | Play products **Published** + entitlement **Swanies Portfolio Pro** · offering **`default`** (blue check) uses **`pro_monthly:monthly`**, **`pro_yearly:yearly`**, **`pro_lifetime`**. Debug **`test_…`** = Test Store only. |
| **Play products** | **`pro_monthly`** + base **`monthly`** · **`pro_yearly`** + **`yearly`** · **`pro_lifetime`** + purchase option **`lifetime`** — all **Active**, regional prices from US anchor. |
| **Play internal testing** | **Active: 23 (1.0.23)** — auto-Pro until **~2026-07-01** (build-time cutoff). Prior **22 (1.0.22)** = **grant=0** control build (owner verified Pro off). Family on **Internal testing → Testers** (list 1). |
| **Version on device** | Play internal **`v1.0.23 (23)`** (owner verified Pro + dialog). |
| **License testing (list 2)** | **Unchecked** — **Swanie's Portfolio Testers** email list **not** selected on **Settings → License testing** (saved). **Do not** check for friends or Testers Community. |
| **Internal testers (list 1)** | **Swanie's Portfolio Testers** (3 family). **Closed-test gate:** promote **23** to **Closed testing** → **Testers Community** opt-in URL (**25** testers paid, **≥12** opted-in **14+** consecutive days). **Not** **`#join-testing`** / **not** license testing. |
| **Verify build** | **`verify-release-config.ps1`** → Signed Bundle → **`verify-play-release.ps1`** — owner verified on **22** and **23**; **always before Play upload**. |

**Play Console — where things stand (human progress):**

- **Data safety:** Questionnaire **saved** (optional **Export to CSV** archive); canonical answers remain **`§ Play Data safety — facts from codebase`** below.
- **Declarations / App content:** Owner stepped through multiple forms (**financial features**, **advertising ID** = no ads SDK / no **`AD_ID`** in manifest, **health**, **government apps**, store listing–adjacent tasks). Continue until **Dashboard** shows nothing blocking.
- **Store settings:** **Finance** category; tags (**Cryptocurrency**, **Investment**, **Personal finance**, **Productivity**); **contact email** + **`https://swaniedesigns.com`** (phone optional); **external marketing** opt-in per preference.
- **Default store listing (en-US):** **App name** + **full description** — canonical draft **`docs/play_store_long_description_en-US.txt`** (~**3948** characters on Windows checkout / **4000** Play cap — re-count in Console before save). **Short description** — Play may flag wording that looks like **ranking / performance** claims (e.g. rewrite **"local-first"** if **"first"** triggers the automated hint); finish **screenshots**, **512 app icon**, **1024×500 feature graphic** uploads.
- **Listing assets (repo):** **`website/play_store_app_icon_512.png`**; **`website/play_store_feature_graphic_1024x500.png`** (required size); optional **`website/play_store_feature_graphic_1024x512.png`**; regenerate feature banner with **`scripts/compose-play-feature-graphic.ps1`** from **`website/play_store_feature_icon_1024x512.png`** (strip edge BG → **`#000416`**, centered scale).
- **Publishing overview:** **`Send app for review`** stays **disabled** until **Dashboard** + **store listing** requirements are complete — then bundle pending changes.
- **Testing path:** **Testers Community (now):** Play Console → **Promote release** **23** to **Closed testing** → copy **closed-test opt-in URL** → submit to **testerscommunity.com** (**25** paid). **≥12 opted-in for 14 consecutive days** before **Production access**. **License testing** stays **unchecked** for TC testers. **Site:** **`#get-app`** has **Play internal test** QR (**`TESTER_URL`**) for family; switch to closed-test URL on site when closed track is live.
- **Marketing site (2026-06-01):** Restored **dual QR** on **`#get-app`** — site + **Play internal test** (**`TESTER_URL`**). Push **`938dc58`**.
- **Marketing site (2026-05-19):** **`website/contact.html`** + **`js/contact-form.js`** — **Contact** nav on all pages; **`#get-app`** links to contact form (replaced **`mailto:`**). Web3Forms submit → owner inbox; success shows **“Thanks — your message was sent. A copy is below.”** + on-page **“Message sent”** copy block (reliable confirmation; **not** Web3Forms Pro email autoresponder). Showcase + swipe hint unchanged from **2026-05-18** lock. Set **`PLAY_URL`** when Play **public** listing URL exists.
- **Marketing site (2026-05-18 — showcase):** **`#app-showcase`** — demo **video left**, **stacked screenshot viewer** (**12** slides, arrows + swipe); mobile swipe hint (touch, contextual arrows, no dimming); screenshot preload; scroll perf. See commits **`17565c2`**–**`b634553`** on **`main`**.

**Marketing site (2026-05-11 + 2026-05-16):** **Sticky header** — **`.site-header`** sibling of **`.wrap`** (not inside **`overflow-x: clip`** on wrap); **`html.is-scrolled`** gold underline on scroll; do **not** put **`overflow-x: hidden`** on **`html`/`body`** (breaks sticky). **Screenshots** carousel, **feature grid** breakpoints, **QR** responsive sizing, **`overscroll-behavior-x`** on **`html`** (Firefox). **`index.html`** / **`privacy.html`** / **`press.html`** share layout.

**Repo hygiene (2026-05-12):** Owner moved to **laptop-only** dev (dropped dual Studio installs and laptop/desktop pairing). **`47e6f40`**: **`gradle/libs.versions.toml`** accidental **AGP 9.2.1** bump **reverted** to **9.2.0**; **`.idea/assetWizardSettings.xml`** **removed from git** (Image Asset Wizard noise); **`.gitignore`** — correct **`/.idea/assetWizardSettings.xml`** (old pattern had a typo), **`/docs/drawable-backups/`** for local PNG experiments; untracked **`docs/drawable-backups/`** folder **deleted** from working tree.

**Earlier (2026-05-12):** **`docs/play_store_long_description_en-US.txt`** — Play **en-US** full listing copy (~**3948** chars); **`docs/AI_HANDOFF.md`**; **push `main`**.

**Locale / Play terms (2026-05-12, agent):** **`terms_last_updated`** + **`terms_section_1`–`terms_section_6`** refreshed in **every `values-*`** to match Play-aligned default English (who we are / how data is stored / optional network / data limits & access / not financial advice / children, language, policy changes). **`terms_section_7_*`** left as already present per locale. **Check:** all locale **`strings.xml`** files contain the same **514** string keys as **`values/strings.xml`** (no missing, no extras). **Build:** **`:app:compileDebugKotlin`** OK on agent checkout. **Commit + push `main`** with **`docs/AI_HANDOFF.md`** update.

**i18n maintenance:** Re-sync **`values-*`** whenever **default `values/strings.xml`** changes meaning again. **`website/`** is deployed separately from app strings—align **`privacy.html`** / marketing copy on its own timeline when English there shifts.

**Locale files — do not use bulk scripts (owner rule):** Never run PowerShell/Python/bash scripts to batch-edit **`app/src/main/res/values-*/strings.xml`**. Scripts repeatedly corrupt UTF-8, break **`%1$s`** placeholders (PowerShell eats **`$s`**), and merge XML lines. **Edit each locale file directly** in the IDE (or careful per-file agent edits). After default **`values/strings.xml`** changes: add the same keys to every **`values-*`** in the **next** handoff/update pass—manual translations, same key order as default. **Verify:** every locale has the same key set as **`values/strings.xml`** (no missing, no extras); **`:app:lintVitalRelease`** before Play AAB. OK to use **`scripts/verify-play-release.ps1`** / **`verify-aab-revenuecat-key.ps1`** (reads AAB only)—that is not locale editing.

**Play + in-app language:** App Bundles default to **language splits** — Play installs only the **device** language, so the in-app picker looks “all English” on Play builds even though **`values-ko`** etc. exist in the repo. Fix: **`app/build.gradle.kts`** → **`bundle { language { enableSplit = false } }`** so all **19** locales ship in the base install. Bump **`versionCode`** for each Play upload that changes this.

### Play Data safety — facts from codebase (canonical for Console answers)

Use this table so **Data safety** matches the wired app (AI-built; owner should not guess). Full detail lives in code paths cited below.

| Topic | What the app actually does |
|-------|----------------------------|
| **Permissions** | **`AndroidManifest.xml`**: **`INTERNET`**, **`ACCESS_NETWORK_STATE`**, **`USE_BIOMETRIC`** only — **no** location, contacts, calendar, broad storage/camera. |
| **Local-first data** | Portfolio + profile in **Room** (`UserProfileEntity`: username, email, password, password hint, etc.). No cloud vault sync in current release (see **`website/privacy.html`**). |
| **RevenueCat** | **`RevenueCatMonetizationManager`** / **`MainViewModel.syncMonetizationUser`**: `Purchases.logIn` uses **`email`** (lowercased) when set, else **`userName`** as RevenueCat **App User ID**. Treat **email** and **user IDs** as **collected** and **shared** with **RevenueCat** (plus Play billing). **Device / install identifiers** — RevenueCat SDK: declare **Device or other IDs** as applicable; **shared** with RevenueCat/Google when the form asks. |
| **Purchase history** | **Yes** — subscriptions via RevenueCat + Play (`purchasePackage`, `restorePurchases`, entitlement checks). |
| **Bug / feedback** | **`BugReportSubmitter`** POSTs to **`api.web3forms.com`** with **`email`** (reply field) + message body — **third party**; counts as **sharing** email when user submits feedback. |
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

**Play / Google:** **Internal testing** **23 (1.0.23)** live with **auto-Pro** QA passed. **License testing** **unchecked**. **Production** inactive. **Next:** **Closed testing** + **Testers Community**.

### Play AAB verify — copy-paste (Android Studio Terminal)

**Owner workflow (canonical — use every Play upload):**

1. **Before build:** **`.\scripts\verify-release-config.ps1`** — **`REVENUECAT_PUBLIC_API_KEY`**, version, **`CLOSED_TEST_PRO_GRANT_DAYS`**.
2. Studio **Generate Signed Bundle** → **`app\release\app-release.aab`** (overwrites prior file).
3. **Before upload:** **`.\scripts\verify-play-release.ps1`** — runs RC key scan + auto-Pro check vs **`local.properties`**.

```
.\scripts\verify-play-release.ps1
```

Expect final line: **`All checks passed - OK to upload this AAB to Play Console.`**

With **`CLOSED_TEST_PRO_GRANT_DAYS=0`**: auto-Pro line reads **`OK: Auto-Pro disabled (until epoch = 0)`**.  
With **`30`**: shows expiry date baked into the AAB.

If Terminal is not at project root:

```
cd C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio
.\scripts\verify-play-release.ps1
```

Individual scripts (only if debugging one check): **`verify-aab-revenuecat-key.ps1`**, **`verify-aab-closed-test-pro.ps1`**. **Do not** use retired **`verify-aab-beta-unlock-secret.ps1`**.

### Play billing QA — owner verified on **1.0.10 (11)** (reference)

| Scenario | Expected | Owner |
|----------|----------|-------|
| Sub **expired** + Restore | No previous Pro purchase | OK |
| Active sub + Restore | Pro already active | OK |
| Expired → subscribe yearly (test) → Pro on | Paywall down | OK |
| **Factory reset / delete local account** | Google email: sub **not** cancelled | OK |
| Reinstall + **same email** + **active Play sub** | Pro without new purchase | OK (by design) |
| Reinstall, **no** active sub, Restore | No purchase found | OK |
| Subscribe → uninstall → reinstall → Restore | Pro without paying again | OK |

**Lifetime:** owner hit Play purchase limit during license-tester yearly renewals; **refunded 26** orders. **Closed-test Pro:** **auto-Pro** via **`CLOSED_TEST_PRO_GRANT_DAYS=30`** in release builds — paywall dialog warns testers **not to purchase**; production builds use **`GRANT_DAYS=0`**.

### Play testing — two lists (canonical; owner asked to re-read when recruiting)

Google Play uses **two different lists**. Confusing them caused **5‑min / 30‑min** test renewals and **24h purchase blocks** for family (all three were on **license testing**).

| | **List 1 — Internal (or closed) testers** | **List 2 — License testing** |
|---|-------------------------------------------|------------------------------|
| **Console path** | App → **Test and release → Testing → Internal testing** (later **Closed testing**) → **Testers** | Account **Settings → License testing** |
| **Purpose** | Who can **install** the test app from the invite link | Who gets **test cards** + **accelerated** sub renewals |
| **Need 12+?** | **Yes** — for recruiting and (later) **closed** track / production path | **No** — optional; keep **unchecked** for friends |
| **Family / friends** | **Add here** | **Do not add** (leave unchecked) |
| **Owner billing QA** | N/A | Optional: small dev-only list or publisher account only |

**When recruiting closed testers (Testers Community playbook):**

1. Promote **23 (1.0.23)** (or current **grant=30** build) to **Closed testing** in Play Console.
2. Copy **closed testing opt-in URL** (not internal-test URL) — submit to **Testers Community**.
3. **Do not** add TC emails to **Settings → License testing**.
4. Tell testers: install from Play opt-in link; **Pro is included until [date]** — **do not purchase** (dialog on paywall repeats this).
5. **`local.properties`:** **`CLOSED_TEST_PRO_GRANT_DAYS=30`** for closed-test AABs; **`0`** for production store builds.

**Family (owner, wife, mom):** internal testers via **`TESTER_URL`** QR on **`swaniedesigns.com/#get-app`**; **no** Play subscriptions for beta.

### Closed-test auto-Pro (shipped in app **1.0.22** — replaces beta unlock)

| Piece | Behavior |
|-------|----------|
| **Grant window** | **`CLOSED_TEST_PRO_GRANT_DAYS`** in **`local.properties`** (default **30**) → **`CLOSED_TEST_PRO_UNTIL_EPOCH_MS`** baked at build time. **`0`** = no auto-Pro (production / owner “Pro off” test). |
| **`isProUser`** | RevenueCat Pro **or** auto-Pro active until epoch. **`ClosedTestProAccess.kt`**. |
| **Paywall UX** | **`ProFeatureGateScreen`** — blocking dialog when auto-Pro active (expiry date, **OK** only). |
| **Verify** | **`.\scripts\verify-play-release.ps1`** after Signed Bundle. **`lintVitalRelease`** — removed orphan **`beta_unlock_*`** from all **`values-*`** (ExtraTranslation blocked release). |

**Beta unlock codes:** **removed** from app (**1.0.22**). Legacy docs/scripts (**`docs/BETA_UNLOCK_CODE.md`**, **`generate-beta-unlock-code.ps1`**) are historical only.

### Play Console — ordered steps (next session; do in order)

1. **Closed testing + Testers Community (primary):** **Promote release** **23 (1.0.23)** to **Closed testing** → copy **opt-in URL** → submit on **testerscommunity.com** (**25** paid). Target **≥12 opted-in for 14 consecutive days**. **License testing** **unchecked** for TC.
2. **Optional site update:** When closed-test URL is live, consider replacing **`TESTER_URL`** on **`index.html`** with closed-test opt-in link (or add second QR).
3. **Listing (parallel):** Finish Play **store listing** uploads; **`docs/play_store_long_description_en-US.txt`** → Console.
4. **Console hygiene:** **Dashboard** / **App content** / **Data safety** — nothing blocking.
5. **Production access:** After closed-test gate passes.
6. **Production release:** **`CLOSED_TEST_PRO_GRANT_DAYS=0`**, bump version, **`verify-play-release.ps1`**, promote to **Production**, **Send app for review**.
7. **At public listing:** Set **`PLAY_URL`** in **`website/index.html`** → push **`main`**.

---

## Next steps (priority order)

### Closed testing → production (ship path)

1. **Testers Community:** Promote **23 (1.0.23)** to **Closed testing** → submit **opt-in URL** → **≥12** opted-in, **14+** consecutive days. **License testing** **unchecked**.
2. **Play listing:** Screenshots, feature graphic, short/long copy; clear **Dashboard** blockers.
3. **Data safety / privacy URLs:** Live **`privacy.html`** anchors in Console if not pasted.
4. **Production access:** Apply after closed-test gate passes.
5. **Production release:** **`GRANT_DAYS=0`** build → **`verify-play-release.ps1`** → **Send app for review** → staged rollout.
6. **Set `PLAY_URL`** on site when public listing URL exists.

### Website

- **Shipped** — showcase, swipe hint, **contact page** (**`contact.html`**). No further site work unless owner requests a change.

---

## Engineering snapshot (v1 ship stack)

- **Stack:** Kotlin, Jetpack Compose, Hilt, Room.
- **Widget (Glance):** Pro **8** / free **3** holdings rows; per-line preference packing + pipe-tolerant parse; **`pushFreshAssetsToWidget`** targets only widgets bound to that vault (**`appWidgetIdsForPortfolioVault`** / **`resolveVaultForAppWidgetId`**) — fixes multi-widget blanking; **`RefreshCallback`** → full per-widget push; metal **`metalWidgetHeadlinePair`**; swan → **`widgetLaunchMainActivityIntent`**.
- **Pro:** RevenueCat + Play billing; **closed-test auto-Pro** via **`ClosedTestProAccess`** + **`CLOSED_TEST_PRO_GRANT_DAYS`** / **`CLOSED_TEST_PRO_UNTIL_EPOCH_MS`** in **`local.properties`**. **Before Play upload:** **`.\scripts\verify-play-release.ps1`** (canonical; owner verified **2026-06-01**). Gradle: **`validateRevenueCatReleaseKey`**, **`verifyReleaseBundleRevenueCatKey`**, **`verifyReleaseBundleClosedTestPro`**. Beta unlock **removed** (**1.0.22**).
- **Backup:** `VaultBackupEngine.kt` + `BackupRestoreScreen.kt` / `Routes.BACKUP_RESTORE` / `SettingsViewModel` — encrypted `.swpb`, WAL checkpoint via `query`, SAF, cold restart after restore.
- **Metals:** `MetalSpotMath.kt` + `AssetValuation` — GRAM/KILO/G → troy oz, USD valuation across holdings, analytics, `AssetRepository`, widget, theme, architect, settings.
- **Custom asset icons:** `IconManager` (`custom_icons/{coinId}.png`), `HoldingsUIComponents` (`MetalIcon`, `CryptoEditFunnel`, `ArchitectIconSelectionStep`), `MyHoldingsScreen` (optimistic merge + per-coin reload epoch); `AssetRepository.refreshAssets` preserves user icon fields at upsert time.
- **Feedback:** `BugReportSubmitter` → **Web3Forms** (`WEB3FORMS_ACCESS_KEY` in `local.properties`; same key in **`website/js/contact-form.js`**). **`RevenueCatInitializer`:** skips `test_` key in release (avoids SDK force-close); log tag **`SwanieRevenueCat`**. See **Pro** bullet for verify scripts.
- **Play Data safety:** See **§ Current session** → **Play Data safety — facts from codebase** (RevenueCat `logIn` id = email or username; purchases; local Room profile).
- **i18n:** `LanguageDisplay.kt`; **`values-*`** — keys match **`values/strings.xml`** (beta_unlock strings removed **2026-06-01**; add **`closed_test_pro_*`** to locales when translating paywall dialog).
- **Quality gates before “done”:** `:app:compileDebugKotlin`, **`:app:lintVitalRelease`** before Play AAB (required — **ExtraTranslation** fails release if locale keys drift from default).

---

## Quick file map

| Area | Start here |
|------|------------|
| Backup engine | `VaultBackupEngine.kt` |
| Backup UI | `BackupRestoreScreen.kt`, `SettingsViewModel.kt`, `Routes.kt`, `NavGraph.kt` |
| Settings / feedback | `SettingsScreen.kt`, `BugReportSubmitter.kt`, `NetworkModule.kt` |
| Metals / valuation | `MetalSpotMath.kt`, `AssetRepository.kt`, `HoldingsUIComponents.kt`, `MyHoldingsScreen.kt` |
| Custom asset icons | `IconManager.kt`, `HoldingsUIComponents.kt` (`MetalIcon`, edit funnels), `MyHoldingsScreen.kt`, `AssetArchitectScreen.kt` |
| Home screen widget | **`PortfolioWidget.kt`** — **`WidgetAssetLimits`** Pro **8** / free **3**; **`writeWidgetPackedAssetRows`**; **`parseSingleWidgetAssetEntry`**; **`WidgetAssetCardHeight` = 62dp**; **`widgetLaunchMainActivityIntent`**; nested **`Column` + `defaultWeight()`** per row; **`AssetRepository.kt`**, **`SettingsViewModel.kt`**, **`WidgetManagerScreen.kt`** (Style tab scroll), **`WidgetConfigActivity.kt`** |
| Marketing site layout | **`website/styles.css`** — **`#app-showcase`**; mobile swipe hint; **`#get-app`** |
| Marketing site / contact | **`website/contact.html`**, **`website/js/contact-form.js`** — Web3Forms contact (no mailto) |
| Marketing site / video | **`website/index.html`** — **`wireShotsViewer`**, **`wireDemoVideo`**; **`#get-app`**, **`PLAY_URL`**, **`SITE_SHARE_URL`**; **`marketing/final_swanies_portfolio_demo_video_web.mp4`** |
| CTA end card (promo video) | **`website/marketing/CTA_end_picture.png`** — production QR → **swaniedesigns.com** |
| App / splash / toast | **Adaptive icon:** **`mipmap-anydpi-v26/ic_launcher.xml`** + **`ic_launcher_round.xml`** (foreground **`@drawable/swan_launcher_extra_small_hq`**); **`drawable/swan_launcher_extra_small_hq.xml`** (vector + group transforms); **`drawable/ic_launcher_foreground.xml`** (layer-list alias). **`swan_splash_icon_wrapper.xml`**, **`ic_toast_swan.xml`**, **`swan_widget_icon_padded.xml`**; **toasts:** **`CustomToast.kt`** (`showPortfolioToast`) + **`layout/toast_portfolio.xml`** + **`toast_chip_background.xml`** (solid **`launcher_navy`** chip); **SVG → vector scripts:** **`scripts/svg_path_to_vector.mjs`** / **`.py`** |
| App / launcher | **`AndroidManifest.xml`** → **`@string/launcher_short_name`** (**Portfolio** under icon); **`app_name`** / Play listing still full brand; widget label unchanged |
| Pro / billing | **`ProFeatureGateScreen.kt`** — branded plan cards; **11.sp** label while purchasing. **`RevenueCatMonetizationManager.kt`**. Play SKUs: **`pro_monthly`**, **`pro_yearly`**, **`pro_lifetime`**. RC offering **`default`** → Play products |
| About | **`AboutScreen.kt`** — intro + **Privacy & terms** button; no Play Console placeholder footer |
| Theme Manager | **`ThemeStudioScreen.kt`** — scrollable color picker (saturation + hue bar); `userInitiatedEdit`; dropdown until real color edit; red Cancel reverts |
| Play internal ship | **23 / 1.0.23** on Play — auto-Pro **grant=30**; **`verify-play-release.ps1`** |
| Marketing site / internal QR | **`website/index.html`** — **`TESTER_URL`** + **`#get-app`** dual QR (**`938dc58`**) |
| Closed-test auto-Pro | **`ClosedTestProAccess.kt`**, **`SettingsViewModel`**, **`ProFeatureGateScreen`** dialog; **`scripts/verify-play-release.ps1`**, **`verify-release-config.ps1`**, **`verify-aab-closed-test-pro.ps1`** |
| Tester recruitment (legacy) | **`docs/RECRUIT_INTERNAL_TESTERS.md`**, **`facebook-join-testing-post.png`** — use **Closed testing + Testers Community** instead |
| Play Console — license vs internal | **List 1:** App → **Testing → Internal / Closed → Testers**. **List 2:** **Settings → License testing** — leave **unchecked** for recruits and TC testers |
| About / legal | `AboutScreen.kt`, `TermsAndConditionsScreen.kt` (§1–§7), `Routes.kt`, `MainActivity.kt`, `values/strings.xml` + `values-*` (incl. **`terms_section_7_*`** per locale) |
| Marketing site | **`website/index.html`**, **`website/contact.html`** — FAQ, SEO, **`#get-app`**, site QR; **`robots.txt`**, **`sitemap.xml`** (incl. contact); **`deploy-website.yml`** → **https://swaniedesigns.com** |
| SEO / Search Console | **`docs/SEARCH_CONSOLE_SETUP.md`** — verify **`swaniedesigns.com`**, submit sitemap |
| Play Data safety (truth from code) | **`§ Current session`** → **Play Data safety — facts from codebase**; **`MainViewModel.kt`** (`syncMonetizationUser`), **`billing/RevenueCatMonetizationManager.kt`**, **`data/feedback/BugReportSubmitter.kt`**, **`AndroidManifest.xml`**, **`app/build.gradle.kts`** (deps) |
| Play listing copy (en-US full description) | **`docs/play_store_long_description_en-US.txt`** — paste into Play Console default listing (**4000** char max; draft ~**3948** on Windows checkout) |
| Play checklist | `Master_Build_Checklist.md` |
| Play ADI challenge file | `app/src/main/assets/adi-registration.properties` (verification token; optional to remove after registration approved) |
| Cursor rules | **`.cursor/rules/git-pull-first.mdc`** (pull before edits), **`update-handoff.mdc`** (handoff + push trigger) |
| Repo / IDE noise | **`.gitignore`** — **`/.idea/assetWizardSettings.xml`**, **`/docs/drawable-backups/`** (local icon raster dumps; not shipped) |

---

## Session history (newest first)

- **2026-06-01 — Auto-Pro shipped + Testers Community ready:** App **1.0.22** (**grant=0**, Pro off) + **1.0.23** (**grant=30**, Pro until **~July 1**, dialog OK) on **internal** — owner verified both. **`ClosedTestProAccess`**, beta unlock removed, **`verify-play-release.ps1`**, **`lintVitalRelease`** locale cleanup. Site **TESTER_URL** QR (**`938dc58`**). **Handoff + push `main`**. **Next:** promote **23** → **Closed testing** → **Testers Community**.
- **2026-06-01 — Auto-Pro 1.0.22 + Play verify workflow:** Replaced beta unlock with **`ClosedTestProAccess`** (**`CLOSED_TEST_PRO_GRANT_DAYS`**); paywall dialog; **`verify-play-release.ps1`** (+ **`verify-release-config.ps1`**, **`verify-aab-closed-test-pro.ps1`**); removed **`verify-aab-beta-unlock-secret.ps1`**. Owner phone QA: **grant=0**, RC expired → Pro off. **`lintVitalRelease`**: stripped **`beta_unlock_*`** from all **`values-*`**. **Handoff + push `main`**. **Next:** upload **1.0.22** owner test → **`GRANT_DAYS=30`** closed-test build.
- **2026-05-19 — Contact page + owner site sign-off:** **`contact.html`** / **`contact-form.js`** — Web3Forms (no mailto); on-page **“Message sent”** confirmation copy; rejected unreliable **email-me-a-copy** (Web3Forms Pro). Owner reviewed site — **likes contact flow**. **Handoff + push `main`**. **Next:** **closed testing**.
- **2026-05-18 (EOD) — Marketing site showcase locked:** Owner **good for now**; will read live site over **1–2 days**. **`app-showcase`** final polish — desktop gap, mobile sizing/nav, **touch** swipe hint (contextual end arrows, no dimming, auto-fade), screenshot preload, CSS cache bust (**`17565c2`**–**`1f73936`**). **Handoff + push `main`**. **Next:** owner QA → **closed testing** → **production**.
- **2026-06-01 — App showcase (no carousel):** **`app-showcase`** grid — video **left**, **stacked** screenshot viewer **right** (**`wireShotsViewer`**, swipe + arrows); removed horizontal scroll carousel entirely. **Handoff + push `main`** → Pages deploy.
- **2026-06-01 — Split demo video from screenshot carousel:** Video moved to **`#app-demo`** (centered, ~**240px**); carousel **images only**, compact **`clamp(168–220px)`** cards (~**4** visible); removed video from horizontal scroll strip (scroll perf). **Handoff + push `main`** → Pages deploy.
- **2026-06-01 — Carousel layout + wheel over strip:** **Container-query** card sizing (**3 full cards**, dropped **`52vw`** + **`scrollbar-gutter: stable`**); **wheel forward** on carousel (Chrome latch); **`overscroll-behavior: none`**. **Handoff + push `main`** → Pages deploy.
- **2026-06-01 — Scroll perf + red Play buttons:** **Solid red** center + bar Play (**`#e62117`**, no gold bleed-through); removed **header `backdrop-filter`**, **aurora animation**, custom **wheel** handler; carousel **`overflow-y: hidden`**. **Handoff + push `main`** → Pages deploy.
- **2026-06-01 — Carousel scroll + no hover lift:** Removed **phone-frame hover lift** on all screenshot cards; **wheel pass-through** over horizontal carousel (fixes Chrome scroll latch); dropped carousel **`tabindex`**; pause **aurora** bg while scrolling; instant carousel arrow scroll. **Handoff + push `main`** → Pages deploy.
- **2026-06-01 — Demo video custom controls + scroll fix:** Replaced native **`controls`** (hidden by CTA overlay, auto-fade) with **custom Play button** + **always-visible bar**; **`preload="none"`**; **`scroll-behavior: auto`** on **`html`** (nav **`#`** links smooth on click). **Handoff + push `main`** → Pages deploy.
- **2026-06-01 — Carousel video UX polish:** **`index.html`** / **`styles.css`** — **always-visible** native controls (WebKit); **CTA_end_picture** poster + overlay on **idle/ended**; **`wireDemoVideo`** sync. **ffmpeg** **2× audio** on web MP4 (~**13 MB**). **Handoff + push `main`** → Pages deploy.
- **2026-06-01 — Demo video re-export on site:** Replaced **`final_swanies_portfolio_demo_video_web.mp4`** (~**14 MB**, H.264 + AAC, **720×1280**) with owner export including **production CTA end card**; same carousel path in **`index.html`**. **Handoff + push `main`** → Pages deploy.
- **2026-05-18 — CTA footer two lines + carousel frame lock:** **`CTA_end_picture.png`** footer split; video phone frame **`overflow:hidden` + absolute video** matches screenshot card height. Owner re-exporting demo video (<50 MB).
- **2026-05-18 — Production website + promo assets:** **`index.html`** — **`#get-app`**, removed join-testing / Web3Forms / beta-unlock page scripts; demo video first in carousel; **`CTA_end_picture.png`** production end card; **`compose-cta-end-card.ps1`**, **`generate-qr-png.js`**. **`press.html`**, **`deploy-website.yml`**, **`website/README.md`** updated. **Next:** closed testing + Testers Community.
- **2026-05-18 — Multi-widget blank fix (1.0.21):** One portfolio refresh was pushing empty/wrong asset rows to **all** homscreen widgets — **`appWidgetIdsForPortfolioVault`**, per-vault DB load, skip empty wipe when selection intact. Owner saw blank widgets during website video (3 widgets). **Handoff + push `main`** → upload **21**.
- **2026-05-18 (EOD) — 1.0.20 shipped + family QA pass:** Owner uploaded internal **20**; Theme Manager + Widget Manager Style scroll OK on wife’s phone; wife Pro stable (unlock code + RC lifetime promo, restore → already active). **`18`–`20`** arc: beta-unlock verify, i18n, scroll fixes. **Handoff + push `main`**. **Next:** recruit testers **4 → 12**.
- **2026-05-18 — Widget Manager Style scroll (1.0.20):** **`WidgetManagerScreen.kt`** Style tab — single **`verticalScroll`** + **`navigationBarsPadding`** (preview + color picker); same fix pattern as Theme Manager. Owner verified **19** Theme Manager OK. **Handoff + push `main`**.
- **2026-05-18 — i18n beta-unlock + Theme Manager scroll (1.0.19):** **3** strings in all **`values-*`**; **`ThemeStudioScreen.kt`** scroll + nav bar padding. Wife Pro + RC confirmed on **18**. **Handoff + push `main`** → owner uploads **19**.
- **2026-05-18 — Theme Manager scroll + i18n note:** **`ThemeStudioScreen.kt`** — **`verticalScroll`** + **`navigationBarsPadding`** so saturation/hue bars reachable on tall phones / large display size. Handoff: **3** beta-unlock strings still missing in **`values-*`** (next release). Wife Pro + RC lifetime confirmed on **1.0.18**.
- **2026-05-18 — Beta unlock verify + UX (1.0.18):** Paywall shows signed-in Gmail; **`INVALID_SIGNATURE`** distinct error; **`validateBetaUnlockReleaseSecret`** + Gradle **`verifyReleaseBundleBetaUnlockSecret`**; **`scripts/verify-aab-beta-unlock-secret.ps1`** (ASCII-only for Windows PS 5). Wife code “not valid” on **17** — regenerate after verify **OK** on **18** AAB. **Handoff + push `main`**.
- **2026-05-28 — Beta unlock recovery + paywall keyboard (1.0.16):** Codes work when RC inactive despite prior supersede; **`saveUnlock`** clears superseded; **`ProFeatureGateScreen`** **`imePadding`**; code field **`bringIntoView`**. Wife lost RC Pro after customer delete — family path = unlock code on **16**. **Handoff + push `main`**.
- **2026-05-28 — Launcher label Portfolio + 1.0.15:** **`launcher_short_name`**; **`versionCode` 15**. Owner uploaded internal **15**; verified on device. **Handoff + push `main`**.
- **2026-05-18 — Website SEO + Search Console + recruit docs:** **`robots.txt`**, **`sitemap.xml`**, canonical/OG/Twitter/JSON-LD, FAQ; **`google4604fa7d884d9a10.html`** verified; **`docs/RECRUIT_INTERNAL_TESTERS.md`**. **Handoff + push `main`**.
- **2026-05-18 — Play 1.0.14 uploaded + marketing assets:** Owner uploaded internal **`14 (1.0.14)`** (widget fix). **`website/marketing/facebook-join-testing-post.png`** added to repo. **Handoff + push `main`**.
- **2026-05-18 — Widget refresh total fix + 1.0.14:** **`RefreshCallback`** — full **`pushAssetsToGlance`** to tapped widget (rows + header total); removed partial prefs write that could leave **`STATIC_TOTAL_BALANCE_KEY`** stale; **`AssetRepository.pushAssetsToGlance`**, total via **`getAssetsByVaultOnce`**. **`versionCode` 14** / **`1.0.14`**. **Handoff + push `main`** → owner uploads internal **14**.
- **2026-05-23 — Beta unlock + website auto-code:** App **1.0.13 (13)** — `BetaUnlockValidator`, `ProUnlockPreferences`, paywall unlock UI; website form generates code in admin email; deploy injects secret; **`scripts/generate-beta-unlock-code.ps1`**. Push **`main`**.
- **2026-05-22 — Beta unlock code design (docs only, no code):** Owner approved **email-bound** codes, **per-code expiry**, **program sunset** (stop accepting new codes → RC/purchases only). **`docs/BETA_UNLOCK_CODE.md`** + handoff/playbook updated; recruiting deferred until implemented. Push **`main`**.
- **2026-05-21 — Beta tester email playbook + handoff:** **`index.html`** — 1 year **RC promotional Pro**, same Gmail Play + in-app, no in-app purchase; Web3Forms **READY TO REPLY** + RC checklist. Billing: **26** refunds, **$0** earnings, W-9 deferred. **`9c34395`** + handoff push.
- **2026-05-18 — Website recruit conversion + handoff:** Join-testing copy for FB funnel (**Android only**, free, lifetime beta); hero **gold** link fix. **`a500966`** + handoff. Recruit link: **`#join-testing`** not **`/index.html`**.
- **2026-05-18 — License vs internal testers (canonical):** Family (3) were on **license testing** → fast sub renewals / 24h blocks. Owner **unchecked** list on **Settings → License testing**. Clarified: need **≥12 internal testers**, **not** license testers; billing QA done; recruits **lifetime** or free. FB recruit **0** signups. **Handoff + push `main`**.
- **2026-05-18 — Internal 1.0.11 verified + recruit testers:** Owner **`v1.0.11 (12)`** from Play Store; tweaks OK. **Next:** share join-testing link, manual Console adds; **≥12** testers → **closed testing** (agent helps then). **Handoff + push `main`**.
- **2026-05-18 — Website join-testing + hero copy:** **`website/index.html`** — Step 1 field **Name** (label + placeholder); removed hero **`hero-note`** under CTA buttons. **Handoff + push `main`** for Pages.
- **2026-05-18 — Website footer cleanup + handoff push:** **`website/index.html`** — removed visible owner setup line (**WEB3FORMS** / **`local.properties`** / **`PLAY_URL`**) above footer; Web3Forms integration unchanged in script. **Handoff + push `main`** for Pages deploy.
- **2026-05-18 — Internal 1.0.11 + UI polish:** **`versionCode` 12** / **`1.0.11`** uploaded to Play internal. **`ProFeatureGateScreen`** — **11.sp** processing label; **`AboutScreen`** — removed **`about_play_console_hint`** (all locales, **522** keys). Prior commit **`46959b4`** on **`main`**.
- **2026-05-18 — UI polish + handoff:** **`ProFeatureGateScreen`** — **11.sp** for **PROCESSING PURCHASE...** (single line). **`AboutScreen`** — removed **`about_play_console_hint`** (all locales, **522** keys). Owner approved both. **Push `main`** **`46959b4`**; Play still on **11** until **12** upload.
- **2026-05-18 — Internal 1.0.10 Play QA complete (owner):** Device **`v1.0.10 (11)`** via tester link (Play Store cache clear if lag). **Locales** OK (KO + others). **Billing:** full matrix — expire/restore, active/restore, reinstall without sub, subscribe + reinstall + restore; local account delete ≠ Play cancel. **Handoff + push `main`**. **Next:** closed testing / listing.
- **2026-05-18 — Play `11 (1.0.10)` + locale split fix + verify copy-paste:** **`bundle.language.enableSplit = false`** — Play was installing English-only splits; in-app language looked “all English.” Internal **11 (1.0.10)** published. Handoff: Studio Terminal one-liner for **`verify-aab-revenuecat-key.ps1`** (no **`-AabPath`**).
- **2026-05-18 — i18n paywall + locale policy + Play upload prep:** **8** new **`pro_gate_*` / `pro_plan_*`** strings translated in **all 19** locales (manual edits after script attempt corrupted UTF-8 — **removed** **`sync-pro-gate-locale-strings.ps1`**). Handoff: **no bulk scripts** on **`values-*/strings.xml`**; Studio on same folder = no pull before AAB. Release lint: drop **`pro_gate_upgrade_coming`** from locales. **Push `main`** → owner builds signed AAB → Play internal **10**.
- **2026-05-18 — Ship prep (paywall, version stamp, Theme Manager) + icon UX:** **Icons (earlier):** compact/full metal+crypto photo cycles; optimistic list, reload epoch, `MetalIcon`/`AssetRepository` upsert fixes — owner verified. **This push:** **`ProFeatureGateScreen.kt`** branded plan cards + auto-select yearly; **`BuildVersionLabel`** top-right on auth screens; **`ThemeStudioScreen.kt`** — dropdown on entry, red **Cancel** only after real edit. **Handoff + push `main`** → owner uploads **1.0.9 (10)** to Play internal.
- **2026-05-17 (EOD) — Play subscriptions + RC offering + 1.0.9 prep:** **Payments profile** + **merchant** info. Play **subscriptions** **`pro_monthly`** / **`pro_yearly`**, one-time **`pro_lifetime`** (**$9.99 / $79.99 / $129.99**). **RevenueCat** import + **`default`** offering wired to **Play** (was Test Store only → empty paywall on Play builds). **Internal 9 (1.0.8)** published; phone stuck **1.0.7** (Play lag). **Studio:** 3 plans work; **`BuildVersionLabel`** on auth screens; paywall row layout fix. **`verify-aab-revenuecat-key.ps1`** PS parse fix. Repo **`versionCode` 10** / **`1.0.9`**. Owner: ship **1.0.9** + retry install tomorrow. **Handoff + push `main`**.
- **2026-05-16 (EOD) — Play internal 1.0.7 + license testers + long ship day:** **Web3Forms** join-testing + reply-ready emails (site). **RevenueCat:** fixed **Wrong API Key** — real **`goog_…`** in **`REVENUECAT_PUBLIC_API_KEY`** (was placeholder / **`test_`** in old AABs); **`validateRevenueCatReleaseKey`**, **`verify-aab-revenuecat-key.ps1`**, release **`test_` block** in **`RevenueCatInitializer`**. **Play:** burned codes **3–7**; live **8 (1.0.7)**; **Create new release** + **Upload** lesson; **tester QR** not site QR; **Download test app** not public version. Owner: **1.0.7** opens from Play, account + asset + paywalls OK; **License testing** — **Swanie's Portfolio Testers** checked, saved. **Next:** re-test subscribe/restore. **`docs/AI_HANDOFF.md`** + **push `main`**.
- **2026-05-16 — FormSubmit outage → Web3Forms (app + site):** **formsubmit.co** returning **521** (both website join-testing + in-app bug report failed). **`BugReportSubmitter.kt`** + **`website/index.html`** → **Web3Forms**; **`WEB3FORMS_ACCESS_KEY`** in **`local.properties`** + site script (owner must create key at web3forms.com). **`docs/AI_HANDOFF.md`**. **Push `main`** (key not in repo).
- **2026-05-16 — Website tester request form + push:** **`website/index.html`** / **`styles.css`** — join-testing form: **name**, **Play email**, optional **message**; **FormSubmit** AJAX submit (no **mailto**); success/error status. **`docs/AI_HANDOFF.md`**. **Push `main`** → Pages.
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
- **Never batch-edit `values-*/strings.xml` with scripts** — see **§ Current session → Locale files — do not use bulk scripts**.
- **`git pull`** before edits is for staying aligned with **`origin`** (another machine or an older checkout). If the agent and Android Studio share **the same folder** and edits were saved there, Studio already has the files—**no pull needed** unless **`git status`** shows you’re behind remote.
- Don’t assume files exist — read before changing.
- **Canonical state for the next agent** = **this file** + the actual repo. If something disagrees with code, **code wins** — then fix this doc.
- Browser-era paste bundles are **removed** from the tree; use **git** if you need old prose.
