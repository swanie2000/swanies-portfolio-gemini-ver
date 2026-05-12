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

**Last updated:** 2026-05-12 — **Workflow:** **`git pull`** from **`origin`** before edits on any machine (multi-PC + Cursor); see **Working agreements** + **`.cursor/rules/git-pull-first.mdc`**.

**Play Console — where things stand (human progress):**

- **Data safety:** Questionnaire **saved** (optional **Export to CSV** archive); canonical answers remain **`§ Play Data safety — facts from codebase`** below.
- **Declarations / App content:** Owner stepped through multiple forms (**financial features**, **advertising ID** = no ads SDK / no **`AD_ID`** in manifest, **health**, **government apps**, store listing–adjacent tasks). Continue until **Dashboard** shows nothing blocking.
- **Store settings:** **Finance** category; tags (**Cryptocurrency**, **Investment**, **Personal finance**, **Productivity**); **contact email** + **`https://swaniedesigns.com`** (phone optional); **external marketing** opt-in per preference.
- **Default store listing (en-US):** **App name** + **full description** — canonical draft **`docs/play_store_long_description_en-US.txt`** (~**3948** characters on Windows checkout / **4000** Play cap — re-count in Console before save). **Short description** — Play may flag wording that looks like **ranking / performance** claims (e.g. rewrite **"local-first"** if **"first"** triggers the automated hint); finish **screenshots**, **512 app icon**, **1024×500 feature graphic** uploads.
- **Listing assets (repo):** **`website/play_store_app_icon_512.png`**; **`website/play_store_feature_graphic_1024x500.png`** (required size); optional **`website/play_store_feature_graphic_1024x512.png`**; regenerate feature banner with **`scripts/compose-play-feature-graphic.ps1`** from **`website/play_store_feature_icon_1024x512.png`** (strip edge BG → **`#000416`**, centered scale).
- **Publishing overview:** **`Send app for review`** stays **disabled** until **Dashboard** + **store listing** requirements are complete — then bundle pending changes.
- **Testing path:** **Studio sideload** to family devices continues for private QA; **Internal testing** track (**signed AAB**, **`TESTER_URL`** on **`website/index.html`**) when ready for Play-delivered testers — see **§ Play Console — ordered steps**.

**Marketing site (2026-05-11 evening — shipped on `main`):** **Screenshots** — two-line **`figcaption`** copy; **‹ ›** controls moved **below** the horizontal strip (full-width phones; hover auto-pan removed as awkward). **Mobile layout** — root **`overflow-x: hidden`** + **`overscroll-behavior-x`** (Firefox Android sideways wobble); **`.wrap`** + **`min-width: 0`** / **`width: 100%`** on cards; **feature grid** explicit **1 / 2 / 3** column breakpoints (replacing fragile **`auto-fill`** **`minmax`** on narrow viewports); **QR deck** — responsive **`drawQr`** pixel size + debounced **`resize`**, **`.tester-qr-frame`** constrained (**no** oversized **`fit-content`**), **`.qr-card`** flex column + **`qr-deck-grid`** **`justify-items: stretch`**. Owner: **Firefox mobile** reads stable after last pass.

**Repo hygiene (2026-05-12):** Owner standardizing on **one laptop** (dropped dual home/work Studio sync). **`47e6f40`**: **`gradle/libs.versions.toml`** accidental **AGP 9.2.1** bump **reverted** to **9.2.0**; **`.idea/assetWizardSettings.xml`** **removed from git** (Image Asset Wizard noise); **`.gitignore`** — correct **`/.idea/assetWizardSettings.xml`** (old pattern had a typo), **`/docs/drawable-backups/`** for local PNG experiments; untracked **`docs/drawable-backups/`** folder **deleted** from working tree.

**This session (2026-05-12):** **`docs/play_store_long_description_en-US.txt`** — Play **en-US** full listing copy (~**3948** chars); **`docs/AI_HANDOFF.md`**; **push `main`**.

**i18n next:** **`values/strings.xml`** (default English) and **`website/`** have diverged from **`values-*`** during recent edits — schedule a **locale parity pass** (legal About/privacy/deletion alignment + any new UI strings).

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

**Adaptive launcher + fingerprint (owner verified):** Vector **`drawable/swan_launcher_extra_small_hq.xml`** — **`108×108`** viewport, **1104×859** art, **nested `<group>`** (scale **~0.0554** + translate); **mipmap** **`foreground`** → **`@drawable/swan_launcher_extra_small_hq` directly** (no **`InsetDrawable`** on adaptive foreground); **`ic_launcher_foreground.xml`** = thin **`layer-list`** alias. **Other Cursor / second PC:** **`git pull`** **`main`** + full wiring; vector XML alone is not enough.

**Also shipped (same week):** **Portfolio toast** plumbing — **`showPortfolioToast`** + **`toast_portfolio.xml`**; **Home** language **slow slide from left** (**`LOGIN_COLUMN_ENTER_*`**, **`LANGUAGE_GLOBE_AFTER_LOGIN_MS`**).

**Icons elsewhere:** Splash / toast / widget still use **PNG + wrapper XML** as before; Image Asset wizard → **PNG** paths.

**Recently shipped (same week):** Marketing site screenshot captions; truthful Drive copy; TOS §7 / privacy §8; **`AssetViewModel`** Drive comments honest.

**Product:** Android app **Swanie’s Portfolio** — crypto & precious metals tracker. Owner considers the app **feature-complete for v1** (**feature freeze**). Remaining work is **shipping** (Play Console, compliance, listing, AAB, RevenueCat/Play QA), not new product features unless the owner reopens scope.

**Repo / branch:** `swanies-portfolio-gemini-ver` on GitHub (`swanie2000`), default branch **`main`**. Legacy repo **`swanies-portfolio`** was deleted.

**Public site:** **`https://swaniedesigns.com`** — static marketing + privacy page from **`website/`**, deployed by **GitHub Actions** (`.github/workflows/deploy-website.yml`). Custom domain + **HTTPS** on GitHub Pages. **`website/privacy.html`**: **§4** with **`#account-deletion`** and **`#data-deletion`** (Play **Data safety** URLs as applicable), **§9** terms (mirrors in-app §7). Push **`main`** after edits so the live URL matches Play.

**Play / Google:** **App created** in Play Console (**Swanie’s Portfolio**). **Android developer verification:** Google emailed that **Play apps are auto-registered** to the verified account — confirm status on **Play Console → Home**. **By September 2026:** register any **extra signing keys** used outside Play and any apps you ship **outside Play** (see [Android developer verification](https://developer.android.com/developer-verification)); unregistered package+key pairs may stop installing on certified devices in some regions. **Dashboard (2026-05-08 snapshot):** “**Finish setting up your app**” looked **complete**; **Internal testing** still needs **Create new release** + **signed release AAB**. **Closed testing** unlocks after internal track is live; **production** path may require **≥12 testers × 14 days** closed test. **Policy email** from Play is generic pre-launch guidance — align **app access** test credentials, **listing** accuracy vs screenshots, and **Data safety** with reality before **Send for review**. **Release signing:** **`swanie_portfolio_release.jks`** — CLI **`keytool`** use **`-storetype PKCS12`** / Studio **JBR** if needed.

### Play Console — ordered steps (next session; do in order)

1. **Bump `versionCode`** in `app/build.gradle.kts` if re-uploading a build (Play rejects duplicate codes).
2. **Android Studio:** **Build → Generate Signed App Bundle** → **release** AAB with **`swanie_portfolio_release.jks`**.
3. **Play Console → Test and release → Internal testing → Create new release** → upload AAB → release notes → **Save** → **Review release** → **Roll out** to internal testers (testers already selected per Dashboard).
4. **Dashboard:** Confirm **Internal testing** checklist shows **Create release** / **Preview and confirm** done; refresh if **Closed testing** still says “finish setup” until Play updates.
5. **Test and release → Closed testing:** When unlocked — new release, invite **≥12** testers who **opt in**; run **≥14 days** for production-access requirement.
6. **Production:** When eligible, **Apply for production** / staged rollout per Console.
7. **Grow users → Store presence / Monetize with Play:** Screenshots, feature graphic, subscriptions + **RevenueCat** SKU alignment, **license testers** for `pro`.
8. **Publishing overview:** When **Send app for review** is enabled and you intend listing/metadata review — send; optional **Managed publishing** if you want manual go-live after approval.
9. **Website:** After **`privacy.html`** on **`main`** deploys to **Pages**, paste Play **Data safety** URLs: **`…/privacy.html#account-deletion`** (delete account), and if the form has **Delete data URL** (optional “delete some data without deleting account” = **Yes**), **`…/privacy.html#data-deletion`**. Remove **`[bracket]`** placeholders / **`noindex`** when listing is public; confirm **§9** liability wording with counsel if needed.

---

## Next steps (priority order)

1. **Play (human):** **Dashboard** + **Create default store listing** — finish **screenshots**, **icon**, **feature graphic**, **content rating**, **target audience**, **privacy URL**, **ads declaration**, short-description wording (avoid ranking-style keywords). When green, **Publishing overview → Send app for review**. Then **§ Play Console — ordered steps** (**internal testing AAB**, closed track, etc.); **`Master_Build_Checklist.md`** in sync. **Data safety** already **saved**; CSV export optional archive.
2. **i18n (required parity):** Propagate recent **`values/strings.xml`** + **`website/`** English changes into **`values-*`** (all maintained locales) — privacy/deletion/account copy, marketing-adjacent strings, any new keys; avoid English-only drift before listing finalization.
3. **Website (when listing exists):** Set **`PLAY_URL`** / **`TESTER_URL`** in **`website/index.html`** script block so CTAs go live. **2026-05-11:** mobile layout / QR / screenshot carousel hardened in **`styles.css`** + **`index.html`** — spot-check **GitHub Pages** after each **`main`** deploy if a device looks off.
4. **Optional cleanup:** Remove or keep **`app/src/main/assets/adi-registration.properties`** (ADI challenge); not needed on device after registration. **`.idea`:** **`assetWizardSettings.xml`** is intentionally **untracked** (wizard UI); do not re-add without cause.
5. **Pre-launch QA:** Backup round-trip; purchase / restore / expiry; widgets + Pro gates; GRAM/KILO metals on device.
6. **Backlog (non-blocking for v1):** V40.36 auth instrumentation, V40.61 monetization telemetry, V40.69 small-screen polish — only if scheduled post-1.0.
7. **Icons (optional / post-v1):** Fine-tune **`swan_launcher_extra_small_hq.xml`** group **scale/translate** (launcher vs fingerprint share one foreground); splash/toast/widget wrappers; toast size in **`toast_portfolio.xml`**.

---

## Engineering snapshot (v1 ship stack)

- **Stack:** Kotlin, Jetpack Compose, Hilt, Room.
- **Pro:** RevenueCat + Play billing when on store; gates Theme Manager, multi-portfolio swipe, full Analytics, widget customization, etc.
- **Backup:** `VaultBackupEngine.kt` + `BackupRestoreScreen.kt` / `Routes.BACKUP_RESTORE` / `SettingsViewModel` — encrypted `.swpb`, WAL checkpoint via `query`, SAF, cold restart after restore.
- **Metals:** `MetalSpotMath.kt` + `AssetValuation` — GRAM/KILO/G → troy oz, USD valuation across holdings, analytics, `AssetRepository`, widget, theme, architect, settings.
- **Feedback:** `BugReportSubmitter` + `@Named("Feedback")` OkHttp in `NetworkModule`; Settings dialog; tag **`SwanieBugReport`** — POST **`formsubmit.co`** (email in form body).
- **Play Data safety:** See **§ Current session** → **Play Data safety — facts from codebase** (RevenueCat `logIn` id = email or username; purchases; local Room profile).
- **i18n:** `LanguageDisplay.kt`; maintained **`values-*`** locales — **2026-05:** English **`values/strings.xml`** + **`website/`** moved ahead of translations; **parity pass pending** (see **§ Next steps**).
- **Quality gates before “done”:** `:app:compileDebugKotlin`, `:app:lintDebug` (`app/lint.xml` policy).

---

## Quick file map

| Area | Start here |
|------|------------|
| Backup engine | `VaultBackupEngine.kt` |
| Backup UI | `BackupRestoreScreen.kt`, `SettingsViewModel.kt`, `Routes.kt`, `NavGraph.kt` |
| Settings / feedback | `SettingsScreen.kt`, `BugReportSubmitter.kt`, `NetworkModule.kt` |
| Metals / valuation | `MetalSpotMath.kt`, `AssetRepository.kt`, `HoldingsUIComponents.kt`, `MyHoldingsScreen.kt` |
| Home screen widget | `PortfolioWidget.kt` (Glance rows; metal labels reuse **`metalCardPrimaryLabel`** / **`metalShouldShowSymbolSubtitle`**) |
| App / splash / toast | **Adaptive icon:** **`mipmap-anydpi-v26/ic_launcher.xml`** + **`ic_launcher_round.xml`** (foreground **`@drawable/swan_launcher_extra_small_hq`**); **`drawable/swan_launcher_extra_small_hq.xml`** (vector + group transforms); **`drawable/ic_launcher_foreground.xml`** (layer-list alias). **`swan_splash_icon_wrapper.xml`**, **`ic_toast_swan.xml`**, **`swan_widget_icon_padded.xml`**; **toasts:** **`CustomToast.kt`** (`showPortfolioToast`) + **`layout/toast_portfolio.xml`** + **`toast_chip_background.xml`** (solid **`launcher_navy`** chip); **SVG → vector scripts:** **`scripts/svg_path_to_vector.mjs`** / **`.py`** |
| Home (login) | **`HomeScreen.kt`** — swan hero, **`AnimatedVisibility`** login column, language globe slide-in timing |
| Pro / billing | `billing/`, `MonetizationManager.kt` |
| About / legal | `AboutScreen.kt`, `TermsAndConditionsScreen.kt` (§1–§7), `Routes.kt`, `MainActivity.kt`, `values/strings.xml` + `values-*` (incl. **`terms_section_7_*`** per locale) |
| Marketing site | `website/` — **`index.html`** (**`PLAY_URL`** / **`TESTER_URL`**, **QR** **`drawQr`** + debounced **`resize`**, **`.shots-carousel`** + **arrows-below** nav), **`privacy.html`** (**§4** **`#account-deletion`**, **`#data-deletion`**), **`styles.css`** (mobile overflow / overscroll, **`.feature-grid`** breakpoints, **`.qr-deck`** / **`.qr-card`**), **`ic_swan_website.png`**, **`favicon-tab.png`**, **`images/*.jpg`**, **`js/qrcode.min.js`**, **`play_store_app_icon_512.png`** / **`play_store_feature_graphic_1024x500.png`**, **`scripts/compose-play-feature-graphic.ps1`**, `.github/workflows/deploy-website.yml` |
| Play Data safety (truth from code) | **`§ Current session`** → **Play Data safety — facts from codebase**; **`MainViewModel.kt`** (`syncMonetizationUser`), **`billing/RevenueCatMonetizationManager.kt`**, **`data/feedback/BugReportSubmitter.kt`**, **`AndroidManifest.xml`**, **`app/build.gradle.kts`** (deps) |
| Play listing copy (en-US full description) | **`docs/play_store_long_description_en-US.txt`** — paste into Play Console default listing (**4000** char max; draft ~**3948** on Windows checkout) |
| Play checklist | `Master_Build_Checklist.md` |
| Play ADI challenge file | `app/src/main/assets/adi-registration.properties` (verification token; optional to remove after registration approved) |
| Cursor rules | **`.cursor/rules/git-pull-first.mdc`** (pull before edits), **`update-handoff.mdc`** (handoff + push trigger) |
| Repo / IDE noise | **`.gitignore`** — **`/.idea/assetWizardSettings.xml`**, **`/docs/drawable-backups/`** (local icon raster dumps; not shipped) |

---

## Session history (newest first)

- **2026-05-12 — Play en-US long description + handoff:** **`docs/play_store_long_description_en-US.txt`** — expanded **Google Play** default **full description** (~**3948** / **4000** chars). **`docs/AI_HANDOFF.md`** — **§ Current session** (listing bullet), **Quick file map**, **§ Session history**. **Push `main`**.
- **2026-05-12 — Repo hygiene + handoff:** **`47e6f40`** — **`gradle/libs.versions.toml`** AGP **9.2.0** (revert stray **9.2.1**); **`git rm --cached`** **`.idea/assetWizardSettings.xml`**; **`.gitignore`** fix **`assetWizardSettings.xml`** + **`docs/drawable-backups/`**; delete local **`docs/drawable-backups/`**. Owner: **laptop-only** dev. **`docs/AI_HANDOFF.md`** + **push `main`**.
- **2026-05-11 (EOD) — Marketing site mobile + handoff:** **`website/index.html`** + **`website/styles.css`** — screenshot **two-line captions**, **carousel** (**arrows below** strip; **no** hover auto-scroll), **Firefox** **overscroll** / horizontal overflow clamp, **QR** responsive draw + **resize**, **feature** grid **1→2→3** columns, **QR cards** flex + **`.tester-qr-frame`** **`max-width`**. **`docs/AI_HANDOFF.md`** — **§ Current session**, **§ Next steps**, **Quick file map**. **No Android app edits this session.** **Push `main`**.
- **2026-05-11 — Handoff only (Play Console snapshot):** **`docs/AI_HANDOFF.md`** — **§ Current session** Play progress (declarations, store settings, listing, graphics paths, Publishing overview gate, testing path); **§ Next steps** tightened; **Quick file map** Play assets on **`website/`**. **No app code changes that day.** **Push `main`**.
- **2026-05-10 (EOD) — Data safety completed + handoff:** Owner finished **Google Play → Data safety** questionnaire (saved; preview + optional **Export CSV**). **`Send for review`** blocked until **Dashboard** tasks — documented in **§ Current session** / **§ Next steps**. **`docs/AI_HANDOFF.md`** — **i18n** raised to honest priority (**`values/strings.xml`** + **`website/`** ahead of **`values-*`**). **Push `main`**.
- **2026-05-10 — Play Data safety canon + handoff:** **`docs/AI_HANDOFF.md`** — new **§ Play Data safety — facts from codebase** (permissions, RevenueCat **`logIn`** email/username, FormSubmit, purchases, photos/SAF backup, no Crashlytics in **`build.gradle.kts`**); **§ Next steps** + **Quick file map** pointer. **Purpose:** single source for Google Play **Data safety** so owner does not guess AI wiring. **Push `main`**.
- **2026-05-10 — Privacy `#data-deletion` + handoff (Play Delete data URL):** **`website/privacy.html`** — subsection **`id="data-deletion"`** (in-app partial removal: holdings / vaults) for optional Play **Data safety → Delete data URL** when **Yes** to deleting data without deleting account; **`#account-deletion`** unchanged. **`docs/AI_HANDOFF.md`** — **§ Current session**, ordered step **9**, **§ Next steps**, **Quick file map**. Site change was on **`main`** (`14258fb`); this session **handoff + push** so owner can paste **`https://swaniedesigns.com/privacy.html#data-deletion`** after **Actions** deploy.
- **2026-05-09 — Privacy §4 + handoff (Play Delete account URL):** **`website/privacy.html`** — **§4 Account and data deletion** (`#account-deletion`), section renumber, effective date **2026-05-08**; supports **Data safety** “Delete account URL” after **Pages deploy**. **`docs/AI_HANDOFF.md`** — Play progress note, **§ Next steps** item for **i18n** parity (`values` / `values-*`) with deletion verbiage when in-app copy should match site. **Push `main`** for live site.
- **Play / verification:** Google email — Play apps **auto-registered** to verified developer account; **Sept 2026** deadline for extra keys / sideload registration noted in **`docs/AI_HANDOFF.md`** + **`Master_Build_Checklist.md`**. Next human step: **internal testing AAB** (ordered steps in handoff).
- **2026-05-09 — Workflow: git pull first:** Owner rule — **`git pull`** before changes on any machine; **`.cursor/rules/git-pull-first.mdc`** + **Working agreements** updated. **`docs/AI_HANDOFF.md`** + push.
- **2026-05-09 — Toast chip solid navy:** **`toast_chip_background.xml`** → opaque **`@color/launcher_navy`** (was **`#CC000000`**). Owner prefers look. **`docs/AI_HANDOFF.md`** + push.
- **2026-05-09 — Adaptive vector launcher + fingerprint (owner verified):** **`swan_launcher_extra_small_hq.xml`** on **`main`** with **108×108 viewport**, **group** scale/translate (no **`InsetDrawable`** on adaptive foreground); **mipmap** foreground points **direct** at vector; **~10%** scale-down for launcher margin. **`scripts/svg_path_to_vector.*`** (CLI input/output). **`docs/AI_HANDOFF.md`** + push. *(Owner: same vector file on second desktop Cursor did not converge — **`git pull`** + full resource wiring required.)*
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

- **Git first (owner + agents):** **`git pull`** from **`origin`** (usually **`main`**) **before** starting substantive edits — multi-machine + Cursor; keeps GitHub as source of truth. Cursor rule **`.cursor/rules/git-pull-first.mdc`** reinforces this.
- Prefer **minimal, safe edits**; don’t refactor unrelated code.
- Don’t assume files exist — read before changing.
- **Canonical state for the next agent** = **this file** + the actual repo. If something disagrees with code, **code wins** — then fix this doc.
- Browser-era paste bundles are **removed** from the tree; use **git** if you need old prose.
