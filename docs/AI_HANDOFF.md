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

**Last updated:** 2026-07-09 — **`main`** — **Website rebrand + polish live on Pages.** Navy/gold theme, new **Play Store** screenshots/logo, **`#download`** + QR, **screenshot carousel** (4/2/1 responsive, lightbox, auto-advance, hover pause), **Play-aligned captions**, subtle **hover polish**, footer **Designed in the USA**. **Play listing** brand assets updated **2026-07-09**. **Production 30 (1.0.30)** still live. **Next:** owner **trailer** video (new images + voiceover) — target **weekend ~2026-07-12/13**; optional **favicon-tab.png** regen from new swan. **Maintenance mode** — no app release unless owner reopens scope.

### Resume when you reopen (RevenueCat + Play)

| Where | State |
|-------|--------|
| **RevenueCat** | **Verified** — offering **`default`** → Play **`pro_*`**; **`goog_…`** in release AAB. **Production billing QA (2026-06-19):** monthly purchase **`GPA.3314-0009-6695-24453`** → refund via dashboard (**§ Refund dev test purchases**). **Owner rule:** refund Play subs in **RC**, not Play Console order management (often missing in sidebar). |
| **Play products** | **`pro_monthly`** + base **`monthly`** · **`pro_yearly`** + **`yearly`** · **`pro_lifetime`** + purchase option **`lifetime`** — all **Active**, regional prices from US anchor. |
| **Play internal testing** | **Active: 24 (1.0.24)** — auto-Pro until **~2026-07-01**. Family still on **Internal testing → Testers** (separate track). **Internal opt-in does not count** toward closed **12+** gate. |
| **Play closed testing (Alpha)** | **Active: 27 (1.0.27)** — full rollout **live**; testers **notified**. **Email lists:** **FIVERR** (**20**) + **Swanie's Portfolio Testers** (**3**). Feedback **`https://swaniedesigns.com/contact.html?topic=tester`**. Opt-in: **`https://play.google.com/apps/testing/com.swanie.portfolio`**. |
| **Play closed testing (testers community)** | **Active: 27 (1.0.27)** — same bundle via **Add from library**; batched with Alpha; testers **notified**. |
| **Version on device** | **30 (1.0.30)** — **Production live** on Play (**Jun 19, 2026** listing update). Studio **`main`** = **`versionCode` 30**. |
| **Production access** | **Granted** — **Production 30 (1.0.30)** **rolled out** (177 countries). |
| **Production access clock** | **Closed test complete** — **12+** opted-in, **14 days**; form submitted. |
| **License testing (list 2)** | **Unchecked** — **do not** add Fiverr or family emails. |
| **Closed testers (email lists)** | **FIVERR** + **Swanie's Portfolio Testers** on **Closed Alpha → Testers**. Each Gmail must **opt in** via closed link (site QR or Play). **Not** license testing. |
| **Verify build** | **`verify-release-config.ps1`** → Signed Bundle → **`verify-play-release.ps1`** — owner verified on **22** and **23**; **always before Play upload**. |

**Play Console — where things stand (human progress):**

- **Data safety:** Questionnaire **saved** (optional **Export to CSV** archive); canonical answers remain **`§ Play Data safety — facts from codebase`** below.
- **Declarations / App content:** Owner stepped through multiple forms (**financial features**, **advertising ID** = no ads SDK / no **`AD_ID`** in manifest, **health**, **government apps**, store listing–adjacent tasks). Continue until **Dashboard** shows nothing blocking.
- **Store settings:** **Finance** category; tags (**Cryptocurrency**, **Investment**, **Personal finance**, **Productivity**); **contact email** + **`https://swaniedesigns.com`** (phone optional); **external marketing** opt-in per preference.
- **Default store listing (en-US):** **App name** + **full description** — canonical draft **`docs/play_store_long_description_en-US.txt`**. **Live listing:** **preview video** (Trailer), **8** phone screenshots, feature graphic. **Brand refresh (2026-07-09):** owner updated **Play listing** graphics/screenshots to match new navy/gold brand (same asset set now on **`swaniedesigns.com`**). Public URL: **`https://play.google.com/store/apps/details?id=com.swanie.portfolio`** — **`PLAY_URL`** on site.
- **Listing assets (repo):** **`website/images/swan-no-background.png`** (header logo); **`website/images/PlayStore_01_Everything_You_Own_One_Portfolio.png`** … **`PlayStore_08_Detailed_Holdings.png`** (screenshot grid); **`website/play_store_feature_graphic_1024x500.png`** (OG + Play feature graphic; source **`images/Feature_Graphic_V6_Final_Python.png`**); **`website/play_store_app_icon_512.png`**.
- **Publishing overview:** **Production 30** published; **IARC** age ratings also went live **Jun 19** (separate email). **Production → Release dashboard** shows Play **recommendations** (not blockers) — see **§ Next steps → Post-launch backlog**.
- **Testing path:** Closed testing complete → **Production access granted**. **License testing** **unchecked**. **Site:** **`#download`** = public **Play** CTA + QR (**`PLAY_URL`**); closed-test join button **removed** (**2026-06-19**). **Site rebrand + polish (2026-07-09):** **`styles.css`**, **`index.html`**, subpages, **`js/shot-carousel.js`**, **`images/PlayStore_*`**, **`swan-no-background.png`**; draft **`index_new_brand_theme.html`** reference only.
- **Closed tester onboarding:** Opt in → install **27** → **create local account** (email required) → use app; Pro until **~July 2026** — **do not purchase**; feedback **`contact.html?topic=tester`** or in-app **Settings → Send feedback**.
- **Marketing site (2026-06-04):** **`#get-app`** QR switched from **internal** to **closed** testing so family (wife/mom) can count toward closed gate after opt-in + update.
- **Marketing site (2026-06-01):** **`contact.html`** — **Tester feedback** topic + **`?topic=tester`** preselect (**`4f6197f`**); Play **Feedback URL** aligned.
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

**Play / Google:** **Production 30 (1.0.30) LIVE** — **`GRANT_DAYS=0`**, 177 countries. Public listing **Updated Jun 19, 2026**. Closed tracks may still serve older builds to opted-in testers. **License testing** **unchecked**.

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

**When recruiting closed testers (Fiverr / email-list playbook):**

1. **Closed Alpha** must serve current **`grant=30`** build (**24+**); use **Add from library** for same version code on additional tracks (do not re-upload AAB).
2. Closed opt-in URL: **`https://play.google.com/apps/testing/com.swanie.portfolio`** (not **`store/apps/details`**).
3. **Play Console → Closed → Testers:** **email lists only** — **cannot** use **Google Groups** and email lists together on same track.
4. **Do not** add recruit emails to **Settings → License testing**.
5. Testers: opt in → install → **create in-app account** (owner verifies via **RevenueCat** email App User ID); **Pro included until ~July 2026** — **do not purchase**; feedback URL + in-app feedback.
6. **`local.properties`:** **`CLOSED_TEST_PRO_GRANT_DAYS=30`** for closed-test AABs; **`0`** for production store builds only.

**Family (owner, wife, mom):** on **internal** and **closed** email lists; **closed opt-in** required for production gate — **`swaniedesigns.com/#get-app`** QR now points to **closed** link; internal-only opt-in **does not** count toward **12+** closed.

### Closed-test auto-Pro (shipped in app **1.0.22** — replaces beta unlock)

| Piece | Behavior |
|-------|----------|
| **Grant window** | **`CLOSED_TEST_PRO_GRANT_DAYS`** in **`local.properties`** (default **30**) → **`CLOSED_TEST_PRO_UNTIL_EPOCH_MS`** baked at build time. **`0`** = no auto-Pro (production / owner “Pro off” test). |
| **`isProUser`** | RevenueCat Pro **or** auto-Pro active until epoch. **`ClosedTestProAccess.kt`**. |
| **Paywall UX** | **`ProFeatureGateScreen`** — blocking dialog when auto-Pro active (expiry date, **OK** only). |
| **Verify** | **`.\scripts\verify-play-release.ps1`** after Signed Bundle. **`lintVitalRelease`** — removed orphan **`beta_unlock_*`** from all **`values-*`** (ExtraTranslation blocked release). |

**Beta unlock codes:** **removed** from app (**1.0.22**). Legacy docs/scripts (**`docs/BETA_UNLOCK_CODE.md`**, **`generate-beta-unlock-code.ps1`**) are historical only.

### Production access — Google requirements (2025–2026)

Many personal accounts are **rejected** after closed testing even with 12+ testers. Google’s stated reasons and community playbook — **document all of this during the 14-day window** so the **Production access** form is credible.

| Pillar | Requirement | Swanie’s Portfolio plan |
|--------|-------------|-------------------------|
| **1. Multiple closed releases** | **≥3** closed-track releases **during** the 14-day test (not one build sitting idle) | **24** · **25** · **26** · **27** — **four** releases during closed test (**27** = exit + refresh UX from tester feedback). Use **Add from library** for extra tracks. |
| **2. App quality** | Professional UI/UX; fix pre-launch issues | **Test and release → Pre-launch report** — fix high-impact items; keep open issue count low before applying. **24** already fixes real tester-facing bug (widget Pro). |
| **3. Production access form** | **~250–300+ characters per answer** on **10 questions**; accurate, app-specific | **Fiverr (Grayo)** assisting. Mention **email-list** closed cohort, closed opt-in link, **Web3Forms** + **`contact.html?topic=tester`**, in-app **Settings → Send feedback**, **build numbers** and **what changed**. **Do not** copy generic templates verbatim. Releases **25–27** = Take Tour + polish + **27** exit/refresh UX (**§ Production access form — activity log**). |
| **Engagement** | Testers actually use the app | Fiverr promised daily use + account creation; **RevenueCat** identifiable emails + feedback for form. **releases + form depth** are the lever. |

**Common rejection themes (email / community):** incomplete production form; no visible app updates after feedback; perceived low tester engagement.

**During closed test (owner / agent checklist):**

1. **Dashboard** — track **opted-in** count toward **≥12**.
2. Ship **25** and **26** on **Closed Alpha** before day **14** (minor real changes OK).
3. Run **Pre-launch report**; fix and reference in release notes.
4. Collect feedback (Fiverr, Web3Forms **Tester feedback** topic, in-app).
5. When **Apply for production access** unlocks (~**2026-06-15** if Day 0 = **2026-06-01**), draft **10 answers** (250+ chars each) — agent can help tailor to portfolio app.
6. **Production AAB** only with **`CLOSED_TEST_PRO_GRANT_DAYS=0`** + **`verify-play-release.ps1`**.

**References:** Testers Community guarantee/docs; owner-shared blog on 2025–2026 rejections (recruit TC, 3 releases, form length, pre-launch).

### Production access form — activity log (copy for 250+ char answers)

**Purpose:** Factual bullets for the **10-question Production access** form and **Fiverr (Grayo)** draft — expand each into app-specific paragraphs; do not paste generic templates.

### Play release 28 — copy-paste (owner upload)

**Prereq:** **`CRYPTOCOMPARE_API_KEY`** in **`local.properties`** before Signed Bundle (key baked into AAB). **`verify-release-config.ps1`** checks RC + CC keys.

**Play Console → Release notes (en-US):**

```
Fix: CryptoCompare price feed (provider now requires an API key). Tokens not on CoinGecko—such as ATLA—update on refresh instead of showing a stuck price. Pro for closed testers until ~July 2026—do not purchase during test.
```

**Production questionnaire (tester-found bug):** Owner/tester found **stale CryptoCompare prices** (e.g. **ATLA**) during closed test; root cause **401 without API key** + refresh keeping old DB price; fixed in **28**; demonstrates responsive iteration (~**35** testers; **CryptoCompare** was only needed for tokens CoinGecko does not list).

### Play release 27 — copy-paste (owner upload)

**Play Console → Release notes (en-US):**

```
Exit button on Holdings — closes the app and removes it from Recents. Refresh moved to each portfolio card (updates that vault only). More space between compact view toggle and the add button. Pro for closed testers until ~July 2026 — do not purchase during test.
```

**Four facts for production questionnaire** (expand each to **250+ chars** in the form):

1. **Exit control** — Top-left **Exit** on Holdings; **`finishAndRemoveTask()`** fully quits and removes the task from Android Recents (not just backgrounding). Disabled during **Take Tour** so walkthrough cannot be killed accidentally.
2. **Refresh UX** — Manual refresh moved from the fixed header to the **top-left of each portfolio card** in the vault pager; refreshes **that vault only** (`refreshAssetsForVault`); portfolio name and total stay **centered** on the card.
3. **Header layout** — Compact/normal view toggle shifted slightly **left** for clearer separation from the yellow **+** add button (add button position unchanged).
4. **Fourth closed release** — **27** ships **after** tour polish (**26**) in direct response to closed-tester feedback — demonstrates **ongoing iteration** during the 14-day window (**24** widget fix · **25** Take Tour · **26** tour hardening · **27** exit + refresh).

**2026-06-10 — Closed release 27 live**

| Item | Detail |
|------|--------|
| **Build** | **`versionCode` 27** / **`versionName` `1.0.27`**. Signed AAB after **`verify-release-config.ps1`** + **`verify-play-release.ps1`**. **`CLOSED_TEST_PRO_GRANT_DAYS=30`** (auto-Pro until **~July 2026**). |
| **What changed** | **Holdings exit** — **`ExitToApp`** top-left; **`finishAndRemoveTask()`** (Recents removed). **Refresh** — portfolio card top-left; **`refreshAssetsForVault`**. **Layout** — compact toggle spacing; card title/total centered. |
| **Play release notes (en-US)** | See **copy-paste block** above (live on public listing **What's new**). |
| **Tracks rolled out** | **Closed testing — Alpha** + **testers community** (same **27** bundle, **Publishing overview** submission after quick checks). Owner **notified both groups**. |
| **Store listing** | **Preview video** change approved — **Trailer** visible on public Play listing; listing looks polished with video + screenshots. |
| **Closed-release milestone** | **Fourth** closed release during test window (**24** · **25** · **26** · **27**). |

**2026-06-09 — Closed release 27 (dev complete)**

| Item | Detail |
|------|--------|
| **Build** | **`versionCode` 27** / **`versionName` `1.0.27`**. Laptop owner QA **OK** — exit, refresh per vault, header spacing. **`CLOSED_TEST_PRO_GRANT_DAYS=30`** (auto-Pro until **~July 2026**). |
| **What changed** | **Holdings exit** — **`ExitToApp`** top-left; **`finishAndRemoveTask()`** (Recents removed). **Refresh** — header bar → portfolio card top-left; **`AssetViewModel.refreshAssetsForVault`**. **Layout** — compact toggle **`offset`** left; card title/total centered (no asymmetric padding). **No new strings** (icons only). |
| **Why it matters for testers** | Addresses closed-test feedback on how to leave the app and where refresh lives; small polish shows the developer is still shipping during the test window. |
| **Play release notes (en-US)** | See **copy-paste block** above. |
| **Closed-release milestone** | **Fourth** closed release during test window (**24** · **25** · **26** · **27**) — exceeds **≥3 releases** requirement. |

**2026-06-02 — Closed release 26 live**

| Item | Detail |
|------|--------|
| **Build** | **`versionCode` 26** / **`versionName` `1.0.26`**. Signed AAB after **`verify-release-config.ps1`** + **`verify-play-release.ps1`**. **`CLOSED_TEST_PRO_GRANT_DAYS=30`** (auto-Pro until **~July 2026**). |
| **What changed** | **Holdings Take Tour** polish: touch lockdown (panel scrim + pass-through holes), **back** / bottom nav blocked during tour, metal premium **ask → mode → amount**, live-card **Save** on keyboard **Return**, icon **Choose Photo**, picker search gated by step, finale drag/arrow fixes, edit pencil **completes** tour. Tour strings in **all 19** locales. |
| **Play release notes (en-US)** | Tour polish, bug fixes, harder to leave tour by accident; tour text in all supported languages; Pro for closed testers until ~July 2026 — do not purchase during test. |
| **Tracks rolled out** | **Closed testing — Alpha** + **testers community** (same **26** bundle, single **Publishing overview** submission). Owner **notified both groups**. |
| **Closed-release milestone** | **Third** closed release during test window (**24** · **25** · **26**) — satisfies **≥3 releases** requirement for production-access form. |

**2026-06-08 — Release 26 tour polish (dev + locale sync)**

| Item | Detail |
|------|--------|
| **Build** | **`versionCode` 26** / **`versionName` `1.0.26`**. Laptop QA **OK**; owner could not break tour on normal or stress paths after fixes. |
| **Tour — accidental exit** | **Back** / bottom nav / system back blocked during tour; intentional exit via red **×** only; larger **×** hit target; edit pencil on finale **completes** tour cleanly. |
| **Tour — touch lockdown** | Panel scrim + pass-through holes only on intended targets; metal live-card **Save** survives keyboard **Return**; premium split into **ask → mode → amount**; icon **Choose Photo** tappable; picker **search locked** until type-search step. |
| **Tour — finale** | Card-gestures hint **pinned top**; arrow fixed; highlight ring hidden when card dragged off-screen; anchor **frozen** while dragging. |
| **Pre-upload** | **i18n synced** — premium hint split + **`architect_tour_*`** in all **19** locales; **`lintVitalRelease`** OK. |

**2026-06-02 — Closed release 25 submitted**

| Topic | Facts for the form |
|-------|-------------------|
| **Build** | **`versionCode` 25** / **`versionName` `1.0.25`**. Signed AAB after **`verify-release-config.ps1`** + **`verify-play-release.ps1`**. **`CLOSED_TEST_PRO_GRANT_DAYS=30`** (auto-Pro until **~July 2026**); testers instructed **not to purchase** during closed test. |
| **What changed** | **Holdings Take Tour** — replayable onboarding: yellow pill hints, highlights, exit dialog (**Don't show tour again**). **Crypto path:** add asset → search provider → amount → card gestures. **Metal path:** **METAL** provider → **Asset Architect** (blueprint, live card, icon) → save → holdings. **Settings:** **HELP & FEEDBACK** section, **Show Take Tour** toggle, **Report a BUG**. Tour strings in **all 19** app languages (manual locale sync, **574** keys, **`lintVitalRelease`** OK). |
| **Why it matters for testers** | Reduces support burden for first-time users; closed cohort (Fiverr + family) can onboard without developer walkthrough. Demonstrates **ongoing iteration** during closed test (release **2** of **≥3**). |
| **Play release notes (en-US)** | New Take Tour on Holdings; metal full tour through Asset Architect; tour text in all supported languages; Pro included for closed testers until ~July 2026 — do not purchase during test. |
| **Tracks rolled out** | **Closed testing — Alpha** (Fiverr + family **email lists**). **Closed testing — testers community** (same **25** bundle, **Add from library**). Single **Publishing overview** submission; adding second track restarted quick checks (normal). |
| **Tester recruitment** | **FIVERR** list (**20** paid cohort, **Grayo**) + **Swanie's Portfolio Testers** (**3** family) on Alpha. Closed opt-in: **`https://play.google.com/apps/testing/com.swanie.portfolio`** (site **`#get-app`** QR). **License testing** **unchecked**. |
| **Feedback channels** | Play Console feedback URL → **`https://swaniedesigns.com/contact.html?topic=tester`**. In-app **Settings → Report a BUG** (Web3Forms, account email auto-included). Owner monitors during **14-day** window. |
| **Prior closed release** | **24 (1.0.24)** — widget **Pro** matched in-app closed-test auto-Pro (**`isProForWidget`**); real bugfix from internal/closed QA. |
| **Owner QA before upload** | Device install from Studio; full **crypto** and **metal** tour paths; holdings card highlights **newly added** asset; compile + push to phone OK. |
| **Still to do (form honesty)** | ~~**26** third closed release~~ ✓ **live 2026-06-02**; **Pre-launch report** fixes in release notes; collect identifiable tester engagement (**RevenueCat** emails after in-app account, not opt-in alone). Apply for production after **14+ days** at **≥12** opted-in. |

**Draft sentence starters (expand to 250+ chars each):**

- *How did you recruit testers?* — Paid Fiverr closed-test cohort (20 Gmail addresses on Play email list FIVERR) plus three family testers on Swanie's Portfolio Testers list; closed opt-in link on marketing site and Play; Testers Community track also receives builds; license testing not used.
- *How do testers give feedback?* — Play feedback URL to website contact form with Tester topic preselect; in-app bug report under Settings with automatic account email; owner reads Web3Forms inbox and Play Console feedback during the 14-day closed test.
- *What have you shipped during closed testing?* — … release 27 exit + refresh UX; release **28** fixed CryptoCompare stuck prices (tester-reported, tokens not on CoinGecko such as ATLA).

### Post–closed-test search engines

| Item | State |
|------|--------|
| **MEXC** | **Shipped in 29** — registry + **`api.mexc.com`**; sparkline **`60m`** klines; owner QA **ATLA** OK. |
| **WEEX** | **Shipped in 29** — v3 **`api-spot.weex.com`**; owner QA OK. |
| **CryptoCompare** | **Removed from picker (29)** — legacy **`priceSource=CryptoCompare`** holdings: re-add via **MEXC** or migrate. **`CRYPTOCOMPARE_API_KEY`** in **`local.properties`** optional for **29** builds (code retained for legacy refresh paths in **`AssetRepository`** only). |
| **Binance** | **Deleted** — US API geo-block; re-add only if policy changes. |
| **Production Pro grant** | **`CLOSED_TEST_PRO_GRANT_DAYS=0`** → production AAB only. **Keep `30` on closed-test builds.** |

### RevenueCat vs Play vs closed testers (do not confuse)

Three separate systems — **only Play opted-in** counts for the **12 / 14-day** production gate; **RevenueCat identifiable emails** = owner’s proof testers **entered the app** (create-account/login).

| System | What it measures | Swanie’s Portfolio notes |
|--------|------------------|-------------------------|
| **Play Console** | **Opted-in** Google accounts on **Closed Alpha**; **14-day** continuous test | **Canonical** for **production access**. Re-watch after **TC → Fiverr** pool swap. |
| **Fiverr (Grayo)** | Paid closed-test cohort (**20** emails on **FIVERR** list) | Active **2026-06-04**; **24–48h** rollout; form help included. **TC stopped.** |
| **RevenueCat** | **`logIn(email)`** after create-account/password-login | **Owner headcount for real testing** — export CSV, filter non-**`$RCAnonymousID`**. Opt-in alone ≠ email row. |

**When RevenueCat shows a customer**

1. **App launch** → `Purchases.configure` → **`$RCAnonymousID:…`** (happens **before** login).
2. **Create account** or **successful login** → `MainViewModel.syncMonetizationUser` → `Purchases.logIn(email)` (email required on create-account form).

**Owner observation (2026-06-02, TC era):** RC **“last seen”** showed **~23** anonymous rows — **opt-in without in-app account**. **TC produced Play opt-ins, not identifiable RC emails.** **Fiverr** engaged with owner’s RC verification standard (account + daily use).

**Finding a tester in RC:** **`Ctrl+K`** → exact **in-app email** they used at signup (not Play opt-in Gmail unless they typed the same). Closed **auto-Pro** → no purchase rows; many testers never appear as paid subscribers.

**RC dashboard limits:** Customer lists show **100 most recently seen** only; **no column sort** — use **Export CSV** and sort `last_seen_at` / `first_seen_at` if needed.

### Refund dev test purchases (canonical — use RevenueCat)

When the owner (or support) needs to **refund a real Google Play subscription** after a smoke test — **use RevenueCat**, not Play Console **Order management** (often absent from the left nav on this account).

**Workflow (verified 2026-06-19 on Production `pro_monthly`):**

1. **Sandbox data → OFF** (top of RC dashboard — production purchases only).
2. **Customers** → search **in-app email** (same as **`Purchases.logIn`** — e.g. owner Gmail).
3. Open **Customer profile** → **Customer history** → **View details** on **`INITIAL_PURCHASE`** (or **`⋯` → Refund** on **Entitlements → Pro**).
4. Click **Refund** on **Event details** (shows **Google Play Order Id** `GPA.…`).
5. Confirm → **Entitlements** show **Inactive · refunded**; **Total spent** → **0**; app **paywall returns** on reopen.

**Do not use for refunds:** Play Console **Order management** hunt; **Report a problem** on order history (slow 1–4 day review) unless RC **Refund** is unavailable (inactive sub). **Do not** double-refund if both paths were triggered.

**Optional:** **License testing** in Play Console for **owner Gmail only** → test purchases not charged (Fiverr list stays **unchecked**).

**Docs:** [RevenueCat — Handling Refunds](https://www.revenuecat.com/docs/subscription-guidance/refunds) · [Customer profile — Refunding](https://www.revenuecat.com/docs/dashboard-and-metrics/customer-profile)

### Play Console — ordered steps (next session; do in order)

1. **Upload 28** — **`verify-release-config.ps1`** (RC + **`CRYPTOCOMPARE_API_KEY`**) → Signed Bundle → **`verify-play-release.ps1`** → **Closed Alpha** + **testers community**; **`GRANT_DAYS=30`** unchanged.
2. **Monitor closed test** — watch **opted-in ≥12**; ~**35** testers after Fiverr growth.
3. ~~**Ship 25 + 26**~~ — **Done** (**25** **2026-06-02**, **26** **2026-06-02**).
4. **Pre-launch report** — fix issues; mention fixes in release notes.
5. **Save feedback** for production form (Fiverr, contact form, in-app); seller assists form draft.
6. After **14+ days** + **≥12** opted-in → **Apply for production access** (long form per **`§ Production access — Google requirements`**).
7. **Production release:** **`GRANT_DAYS=0`**, verify, rollout; set **`PLAY_URL`** on site when listing is public.

---

## Next steps (priority order)

### Post-launch (now)

1. **Website trailer (owner — soon):** Shorter **promo/trailer** video with **new brand images** + **voiceover**; owner targeting **weekend (2026-07-12/13)**. When exported: replace or supplement current Play **Trailer** + embed on **`index.html`** (web export to **`website/marketing/`**, keep under GitHub **100 MB** limit).
2. ~~**Paywall smoke test**~~ ✓ **2026-06-19** — Play install → **pro_monthly** $9.99 → Pro → **RC Refund** → paywall back.
3. **Optional listing polish:** **Grow users → Store presence** — fix **What's new** typos if any; remove stale **closed beta / Pro through July 2026** copy from long description if still present.
4. **Optional:** Regenerate **`favicon-tab.png`** from **`images/swan-no-background.png`** so browser tab matches new swan.

### Post-launch backlog — Play Console recommendations (v31+, not urgent)

Play **Production → Release dashboard** flagged these on **30 (1.0.30)** — informational; app works on phones today.

1. **Edge-to-edge (Android 15+):** App targets SDK 35; some screens may not draw edge-to-edge for all users. Adopt Android 15 edge-to-edge guidance (Compose **`enableEdgeToEdge`**, inset handling).
2. **Deprecated status/nav bar colors:** Replace **`Window.setStatusBarColor`** / **`setNavigationBarColor`** (Play cites **`HoldingsUIComponents.kt`** and related Compose/system UI). Migrate to **`WindowInsetsController`** / Material 3 system bar APIs.
3. **Large screens / resizability:** Play recommends relaxing **`android:screenOrientation="portrait"`** and **`android:resizableActivity="false"`** in **`AndroidManifest.xml`** (activities **`MainActivity`**, **`WidgetConfigActivity`**) for tablets/foldables — or document intentional phone-only portrait if owner keeps scope narrow.

### Optional — closed track

- Upload **30** with **`GRANT_DAYS=30`** only if testers still need auto-Pro (separate AAB from Production).

### Backlog

- Quit confirmation + **`confirmQuit`** toggle.

### Website

- **Shipped (2026-07-09)** — Full **brand rebrand** + **same-day polish**: layout/spacing, **screenshot carousel**, lightbox, **Play Store caption copy**, hover states, screenshot **`object-fit: contain`** fix, carousel **page-centered**. **`#download`** + **`PLAY_URL`** + QR. Deploy: push **`main`** → GitHub Actions → **https://swaniedesigns.com**.
- **Pending (owner)** — New shorter **trailer** video (voiceover + new images); add to site + Play when ready (~**weekend 2026-07-12/13**). Optional: regenerate **`favicon-tab.png`** from **`images/swan-no-background.png`**.

---

## Engineering snapshot (v1 ship stack)

- **Stack:** Kotlin, Jetpack Compose, Hilt, Room.
- **Widget (Glance):** Pro **8** / free **3** holdings rows; per-line preference packing + pipe-tolerant parse; **`pushFreshAssetsToWidget`** targets only widgets bound to that vault (**`appWidgetIdsForPortfolioVault`** / **`resolveVaultForAppWidgetId`**) — fixes multi-widget blanking; **`RefreshCallback`** → full per-widget push; metal **`metalWidgetHeadlinePair`**; swan → **`widgetLaunchMainActivityIntent`**. **30:** removed **`ACTION_SCREEN_ON`** → **`updateAll()`** (stuck **`glance_default_layout`** spinner on wake); parse-fallback shows vault total instead of endless loading. **30-min `updatePeriodMillis`** redraws cached prefs only — **not** live price fetch; manual widget/app refresh updates prices.
- **Pro:** RevenueCat + Play billing; **closed-test auto-Pro** via **`ClosedTestProAccess`** + **`CLOSED_TEST_PRO_GRANT_DAYS`** / **`CLOSED_TEST_PRO_UNTIL_EPOCH_MS`**. **Widget Pro** must use **`WidgetAssetLimits.isProForWidget`** (same rules as app). **Before Play upload:** **`.\scripts\verify-play-release.ps1`**. **Dev/test refunds:** **RevenueCat** → **Customers** → purchase event → **Refund** (**§ Refund dev test purchases**); keep **Sandbox data OFF** for production charges.
- **Backup:** `VaultBackupEngine.kt` + `BackupRestoreScreen.kt` / `Routes.BACKUP_RESTORE` / `SettingsViewModel` — encrypted `.swpb`, WAL checkpoint via `query`, SAF, cold restart after restore.
- **Metals:** `MetalSpotMath.kt` + `AssetValuation` — GRAM/KILO/G → troy oz, USD valuation across holdings, analytics, `AssetRepository`, widget, theme, architect, settings.
- **Custom asset icons:** `IconManager` (`custom_icons/{coinId}.png`), `HoldingsUIComponents` (`MetalIcon`, `CryptoEditFunnel`, `ArchitectIconSelectionStep`), `MyHoldingsScreen` (optimistic merge + per-coin reload epoch); `AssetRepository.refreshAssets` preserves user icon fields at upsert time.
- **Feedback:** `BugReportSubmitter` → **Web3Forms** (`WEB3FORMS_ACCESS_KEY` in `local.properties`; same key in **`website/js/contact-form.js`**). **`RevenueCatInitializer`:** skips `test_` key in release (avoids SDK force-close); log tag **`SwanieRevenueCat`**. See **Pro** bullet for verify scripts.
- **Play Data safety:** See **§ Current session** → **Play Data safety — facts from codebase** (RevenueCat `logIn` id = email or username; purchases; local Room profile).
- **Holdings UX (27):** **`MyHoldingsScreen.kt`** — exit, per-vault refresh, header spacing. **`AssetViewModel.refreshAssetsForVault`**.
- **CryptoCompare (legacy):** Removed from picker in **29**; **`AssetRepository`** still recognizes **`priceSource=CryptoCompare`** for **`healMetadata`**. **`CRYPTOCOMPARE_API_KEY`** optional for **29** builds.
- **MEXC (29):** **`MexcSearchProvider`** — sparkline **`60m`** klines.
- **WEEX (29):** **`WeexSearchProvider`** — v3 **`api-spot.weex.com`**; **`1h`** klines; decimal **`priceChangePercent`** × 100 for UI.
- **Holdings walkthrough (25–26):** `HoldingsWalkthrough.kt`, `HoldingsWalkthroughViewModel.kt`, overlay in **`MainActivity`**; **`Take Tour`** on holdings (Settings toggle **Show Take Tour button**); yellow pill hints + glossy arrows + panel scrim with pass-through holes; red **×** exit + **Don't show tour again**. **Tour lockdown (26):** system/back/bottom-nav blocked during tour; picker steps gate search/provider/results; metal premium **ask → mode → amount**; live-card **Return** = **Save**; icon **Choose Photo** + **Add Asset** pass-through; finale hint pinned top; drag anchor freeze. **Crypto path:** add → provider → search → amount → card gestures → **End** (pencil opens edit → tour completes). **Metal fork:** **AssetArchitectScreen** → save → **`highlightCoinId`** card. **`walkthroughAnchor`** + window bounds. **26** laptop QA **OK** (**2026-06-08**).
- **Settings UX (25):** **HELP & FEEDBACK** section near top — **Take Tour** toggle + **Report a BUG** card (tour no longer routes to feedback).
- **i18n:** `LanguageDisplay.kt`; **`values-*`** — **574** keys each, match **`values/strings.xml`** (**2026-06-02:** **`walkthrough_*`**, **`settings_help_feedback`**, **`closed_test_pro_dialog_*`** in all **19** locales — manual per-file edits, no bulk scripts).
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
| Marketing site layout | **`website/styles.css`** — navy/gold brand; **`#screenshots`** **`shot-carousel`** (4/2/1); **`website/js/shot-carousel.js`**; lightbox **`#shot-lightbox`**; **`#download`** + QR |
| Marketing site / contact | **`website/contact.html`**, **`website/js/contact-form.js`** — Web3Forms; **Tester feedback** topic; **`?topic=tester`** |
| Marketing site / brand assets | **`website/images/swan-no-background.png`**, **`website/images/PlayStore_01_…`–`08_…`**, **`website/play_store_feature_graphic_1024x500.png`**; draft **`website/index_new_brand_theme.html`** |
| Marketing site / video (pending) | Owner **trailer** — shorter promo with new images + voiceover (~**weekend 2026-07-12/13**); export to **`website/marketing/`** when ready |
| CTA end card (promo video) | **`website/marketing/CTA_end_picture.png`** — production QR → **swaniedesigns.com** |
| App / splash / toast | **Adaptive icon:** **`mipmap-anydpi-v26/ic_launcher.xml`** + **`ic_launcher_round.xml`** (foreground **`@drawable/swan_launcher_extra_small_hq`**); **`drawable/swan_launcher_extra_small_hq.xml`** (vector + group transforms); **`drawable/ic_launcher_foreground.xml`** (layer-list alias). **`swan_splash_icon_wrapper.xml`**, **`ic_toast_swan.xml`**, **`swan_widget_icon_padded.xml`**; **toasts:** **`CustomToast.kt`** (`showPortfolioToast`) + **`layout/toast_portfolio.xml`** + **`toast_chip_background.xml`** (solid **`launcher_navy`** chip); **SVG → vector scripts:** **`scripts/svg_path_to_vector.mjs`** / **`.py`** |
| App / launcher | **`AndroidManifest.xml`** → **`@string/launcher_short_name`** (**Portfolio** under icon); **`app_name`** / Play listing still full brand; widget label unchanged |
| Pro / billing | **`ProFeatureGateScreen.kt`** — **`pro_gate_manage_subscription`** opens Play subscription URL; **`RevenueCatMonetizationManager.kt`**. Play SKUs: **`pro_monthly`**, **`pro_yearly`**, **`pro_lifetime`**. RC offering **`default`**. **Refunds:** **`§ Refund dev test purchases`** (RC dashboard, not Play Order management) |
| About | **`AboutScreen.kt`** — intro + **Privacy & terms** button; no Play Console placeholder footer |
| Theme Manager | **`ThemeStudioScreen.kt`** — scrollable color picker (saturation + hue bar); `userInitiatedEdit`; dropdown until real color edit; red Cancel reverts |
| Play internal ship | **23 / 1.0.23** on Play — auto-Pro **grant=30**; **`verify-play-release.ps1`** |
| MEXC price / sparkline | **`MexcSearchProvider.kt`**, **`MexcApiService.kt`**, **`NetworkModule.kt`** (`@Named("MEXC")`), **`SearchEngineRegistry.kt`**, **`AssetRepository.kt`** |
| WEEX price / sparkline | **`WeexSearchProvider.kt`**, **`WeexApiService.kt`**, **`NetworkModule.kt`** (`@Named("WEEX")`) |
| Marketing site / closed-test QR | **`website/index.html`** — **`TESTER_URL`** = **`apps/testing/com.swanie.portfolio`** invite-only QR on **`#get-app`** |
| Closed-test auto-Pro | **`ClosedTestProAccess.kt`**, **`SettingsViewModel`**, **`ProFeatureGateScreen`** dialog; **`WidgetAssetLimits.isProForWidget`**, **`AssetRepository`** widget push; **`scripts/verify-play-release.ps1`** |
| Tester recruitment (legacy) | **`docs/RECRUIT_INTERNAL_TESTERS.md`**, **`facebook-join-testing-post.png`** — superseded by **Fiverr closed email lists** |
| Play Console — license vs testers | **List 1:** **Internal** and/or **Closed → Testers** (email lists). **List 2:** **Settings → License testing** — **unchecked** for Fiverr and family |
| About / legal | `AboutScreen.kt`, `TermsAndConditionsScreen.kt` (§1–§7), `Routes.kt`, `MainActivity.kt`, `values/strings.xml` + `values-*` (incl. **`terms_section_7_*`** per locale) |
| Marketing site | **`website/index.html`**, **`website/contact.html`** — FAQ, SEO, **`#download`**, site QR; **`robots.txt`**, **`sitemap.xml`** (incl. contact); **`deploy-website.yml`** → **https://swaniedesigns.com** |
| SEO / Search Console | **`docs/SEARCH_CONSOLE_SETUP.md`** — verify **`swaniedesigns.com`**, submit sitemap |
| Play Data safety (truth from code) | **`§ Current session`** → **Play Data safety — facts from codebase**; **`MainViewModel.kt`** (`syncMonetizationUser`), **`billing/RevenueCatMonetizationManager.kt`**, **`data/feedback/BugReportSubmitter.kt`**, **`AndroidManifest.xml`**, **`app/build.gradle.kts`** (deps) |
| Play listing copy (en-US full description) | **`docs/play_store_long_description_en-US.txt`** — paste into Play Console default listing (**4000** char max; draft ~**3948** on Windows checkout) |
| Play checklist | `Master_Build_Checklist.md` |
| Production access (Google 2025–2026) | **`docs/AI_HANDOFF.md`** → **§ Production access — Google requirements** + **§ Production access form — activity log** |
| RevenueCat vs closed testers | **`docs/AI_HANDOFF.md`** → **§ RevenueCat vs Play vs closed testers**; `MainViewModel.syncMonetizationUser`, `RevenueCatMonetizationManager.setAppUser` |
| Play ADI challenge file | `app/src/main/assets/adi-registration.properties` (verification token; optional to remove after registration approved) |
| Holdings walkthrough | **`ui/onboarding/HoldingsWalkthrough.kt`**, **`HoldingsWalkthroughViewModel.kt`**, **`MainActivity.kt`** (overlay), **`MyHoldingsScreen.kt`**, **`AssetPickerScreen.kt`**, **`AmountEntryScreen.kt`**, **`AssetArchitectScreen.kt`**, **`NavGraph.kt`**, **`SettingsScreen.kt`**, **`ThemePreferences.kt`** (`showTakeTourButton`, `holdingsWalkthroughCompleted`) |
| Play Console post-launch | **`§ Next steps`** → *Post-launch backlog — Play Console recommendations*; **`AndroidManifest.xml`**, **`HoldingsUIComponents.kt`** |
| Cursor rules | **`.cursor/rules/git-pull-first.mdc`** (pull before edits), **`update-handoff.mdc`** (handoff + push trigger) |
| Repo / IDE noise | **`.gitignore`** — **`/.idea/assetWizardSettings.xml`**, **`/docs/drawable-backups/`** (local icon raster dumps; not shipped) |

---

## Session history (newest first)

- **2026-07-09 — Website carousel, captions, hover polish (handoff + push):** Same-day follow-up to rebrand — **screenshot carousel** (**`shot-carousel.js`**: auto-play on load, hover pause, arrows/dots/swipe, lightbox); **Play Store caption copy** on 8 slides; layout/spacing polish; carousel **centered** on page; **`object-fit: contain`** for screenshots; subtle **desktop hover** on cards/nav/CTA; footer **Designed in the USA**. **Handoff + push `main`** → Pages deploy.
- **2026-07-09 — Website rebrand + Play listing brand update (handoff + push):** Owner updated **Google Play listing** with new brand screenshots/graphics. **`website/`** — full rebrand from **`index_new_brand_theme.html`**: navy/gold **`styles.css`**, **`index.html`** + subpages, **`images/PlayStore_01`–`08`**, **`swan-no-background.png`**, updated feature graphic. **`#download`** + **`PLAY_URL`** + QR preserved. **Next (owner):** shorter **trailer** video — target **weekend**. **Handoff + push `main`** → Pages deploy.
- **2026-06-19 — Production billing smoke test + RC refund playbook (handoff + push):** Owner installed **Production 30** from Play; **pro_monthly** purchase → Pro → **RevenueCat Refund** on event **`GPA.3314-0009-6695-24453`** → paywall restored. Documented **`§ Refund dev test purchases`** — **always use RC** (Sandbox **OFF**), not Play Console Order management. **Handoff + push `main`**.
- **2026-06-19 — Production 30 LIVE + Play backlog in handoff (handoff + push):** **Production 30 (1.0.30)** approved and live; store **Updated Jun 19**; Release dashboard active. Documented Play **recommendations** for **v31+** (edge-to-edge Android 15+, deprecated status-bar APIs, large-screen resizability). **Site** already pushed (**`e27c88a`**). **Next:** paywall smoke test.
- **2026-06-19 — Site: remove closed-test button + Production 30 in review (handoff + push):** **`website/index.html`** — deleted **Join closed testing** CTA; QR/CTA **`PLAY_URL`** only. Owner submitted **Production 30 (1.0.30)** after catching **1.0.20** in draft; **Publishing overview** in review. **IARC** ratings live (**June 19** email) ≠ APK publish. **Handoff + push `main`**.
- **2026-06-02 — Production go-live: site PLAY_URL + RevenueCat verified (handoff + push):** Owner walked **RevenueCat** checklist (Apps → Products → Entitlements → **`default`** offering) — all pass. **`website/index.html`** — **`PLAY_URL`** → public listing; closed-test QR/button removed; JSON-LD **1.0.30**; **`press.html`** + checklist tick. **Production 30** already **in review** on Play. **Handoff + push `main`** for Pages deploy.
- **2026-06-17 — Release 30 production + widget wake fix (handoff + push):** **`1.0.30`** — **`GRANT_DAYS=0`** for Production AAB; removed widget **`SCREEN_ON`** refresh (stuck spinner); parse fallback for widget loading state. **Production access granted.** Owner QA **29** on device. **Handoff + push `main`**. **Next:** verify scripts → **Production** upload → **`PLAY_URL`**.
- **2026-06-02 — Release 29 MEXC + WEEX; drop CC picker; delete Binance (handoff + push):** **`1.0.29`** on **`main`** — **WEEX** v3 wired; **Binance** removed (US geo-block); **CryptoCompare** removed from **`SearchEngineRegistry`**; owner QA **MEXC** + **WEEX** OK. **`GRANT_DAYS=30`** for closed upload. **Handoff + push `main`**. **Next:** verify scripts → upload **29** both closed tracks (after **28** live if needed).
- **2026-06-02 — MEXC wired + sparkline fix (handoff + push):** **`MexcSearchProvider`** registered; **`NetworkModule`** + **`AssetRepository`** exchange-provider paths; sparkline **`60m`** klines (MEXC rejects **`1h`**). Owner QA **ATLA** — price, refresh on load, sparkline OK. **`versionCode` still 28**; ship **29** with **drop CryptoCompare** after **28** Play approval. **Handoff + push `main`**. **Next:** **28** rollout → bump **29** → Play upload.
- **2026-06-11 — Release 28 CryptoCompare fix (handoff + push):** **`1.0.28`** on **`main`** — CC API key wiring, **`healMetadata`** / refresh fixes for stuck prices (**ATLA** / tokens not on CoinGecko). Owner QA OK. **Keep `GRANT_DAYS=30`** for closed **28** upload; **production** = **`GRANT_DAYS=0`** later. **Post-test plan:** wire **MEXC**, drop **CryptoCompare** from picker. **Handoff + push `main`**. **Next:** verify scripts → upload **28** both closed tracks.
- **2026-06-10 — Play closed release 27 live + listing video (handoff + push):** **`1.0.27`** approved and **live** on **Closed Alpha** + **testers community**; owner **notified both groups**. **Store listing** — **preview video** approved (**Trailer** on public Play page); **What's new** shows **27** notes; listing looks polished. **`≥4` closed releases** complete. **Next:** monitor Dashboard **14-day** clock + feedback; **v28** quit confirm + **`GRANT_DAYS=0`** at production. **Handoff + push `main`**.
- **2026-06-09 — Release 27 exit + refresh UX (handoff + push):** **`versionCode` 27** / **`1.0.27`**. **Exit** top-left — **`finishAndRemoveTask()`** (Recents cleared); tour-shielded. **Refresh** on portfolio card top-left, per-vault; title/total centered. **Header** — compact toggle shifted left. Owner QA **OK**. **Play upload pending**; release notes + **four** production-form facts in **`§ Production access form — activity log`**. **v28:** quit confirm + **`GRANT_DAYS=0`**. **Handoff + push `main`**.
- **2026-06-02 — Play closed release 26 live (EOD):** **`1.0.26`** approved and **live** on **Closed Alpha** + **testers community**; owner **notified both groups**. **`≥3` closed releases** complete (**24** · **25** · **26**). **Good for now** — monitor Dashboard + feedback; **v27** (exit button) deferred until second tester recommendations. **Handoff + push `main`**.
- **2026-06-02 — Release 26 locale sync pushed + v27 note:** Manual **`values-*`** for premium tour hints + **`architect_tour_*`** (all **19** locales); **`lintVitalRelease`** OK; **`main`** pushed. **v27 backlog:** **exit button** to close app — **deferred** until second round of tester recommendations. **Next:** verify scripts → upload **26** Closed Alpha.
- **2026-06-08 — Release 26 locale sync:** Manual **`values-*`** updates for **`walkthrough_hint_metal_live_premium_ask/pick_mode/amount`**, **`architect_tour_value_required`**, **`architect_tour_skip_premium`** (removed obsolete **`premium_mode`**); **`lintVitalRelease`** OK. **Next:** verify scripts → Play upload.
- **2026-06-08 — Tour polish release 26 (local; handoff + push):** **`versionCode` 26** / **`1.0.26`**. Major **Holdings Take Tour** hardening: touch scrim lockdown, back/nav blocked, metal premium sub-steps + currency display, keyboard **Save** on live-card fields, icon pick **Choose Photo**, picker search gated by step, finale drag/arrow/highlight fixes, edit pencil ends tour cleanly, stress-test guards (anchor freeze, invalid bounds). Owner **could not break** tour after fixes. **English-only** new strings — **sync 19 locales** before Play upload. **Handoff + push `main`**. **Next:** i18n → verify scripts → upload **26** Closed Alpha.
- **2026-06-02 — Play closed release 25 submitted (in review):** Owner built **`1.0.25`** AAB (**`verify-release-config.ps1`** → Signed Bundle → **`verify-play-release.ps1`**). Uploaded **Closed Alpha** full rollout + release notes (Take Tour). Added **testers community** track via **Add from library** (batched review). **`§ Production access form — activity log`** added for Google **10-question** draft. **Handoff + push `main`**. **Next:** approval → tester updates → **26**.
- **2026-06-02 — Tour polish + i18n (25, local only):** **Metal** tour fork polished (**Asset Architect** blueprint/live-card/icon steps, touch blockers, **`highlightCoinId`** holdings card fix). **`walkthrough_*`** + settings + **`closed_test_pro_dialog_*`** added to **all 19 `values-*`** (manual edits; **574** keys; **`lintVitalRelease`** OK). Owner QA **tour good**; **not** uploaded to Play. **Handoff + push `main`**.
- **2026-06-02 — Holdings walkthrough tour (25, local only):** Shipped replayable **Take Tour** for closed-test onboarding: yellow pill hints, arrows, highlights, exit + **Don't show tour again**, **HELP & FEEDBACK** on Settings. Full **crypto** path (add → search → amount → card gestures → **End Tour**). **Metal fork** when user picks **METAL** provider → **Asset Architect** tour steps → save → holdings. **`versionCode` 25** / **`1.0.25`**; owner QA OK on device; **not** uploaded to Play — **metal polish** next. **Handoff + push `main`**.
- **2026-06-04 — Fiverr closed test + site QR → closed:** **TC stopped**; **Fiverr (Grayo)** **20** testers + family **3** on **Closed Alpha email lists** (Google Group removed; Play **list or group, not both**). **`website/index.html`** **`TESTER_URL`** → closed opt-in for wife/mom. Owner: **RC email** = real app tester; Fiverr promises account + daily use + form help. **Handoff + push `main`**.
- **2026-06-02 — Play ✓12 opted-in; RevenueCat vs testers clarified:** **24** internal + closed; **TC** **25/25**, Day **0/16**. Play **Apply for production** still blocked on **14-day** run. Owner: RC **anonymous** list ≠ TC testers; **`logIn`** only after in-app account — use **Play Dashboard** for gate, not RC **171** / **23** counts. **Handoff + push `main`**.
- **2026-06-01 — TC started + production-access playbook in handoff:** **24** live internal + closed; **TC** submitted (**Day 0 / 16**). Documented **Google production-access** requirements (**≥3 closed releases**, pre-launch, **250+ char** form, engagement). **Handoff + push `main`**.
- **2026-06-02 — Widget auto-Pro fix (1.0.24):** **23** gave app Pro but widget stayed free (RevenueCat-only in **`AssetRepository`** / **`WidgetAssetLimits`**). **`isProForWidget`** + widget refresh on Pro change; **`versionCode` 24**. Unit test **`WidgetAssetLimitsTest`**. **Handoff + push `main`**. **Next:** upload **24**, QA widget on phone.
- **2026-06-01 — Closed Alpha live + tester feedback; waiting on Google review:** **Closed Test 23** on **Alpha**; TC **Google Group** + opt-in submitted; Play **feedback URL** → **`contact.html?topic=tester`** (**`4f6197f`**). Site **invite-only internal** QR (**`d64e728`**). Owner **waiting on Google review**; closed opt-in **1** (need **≥12** / **14+** days). **Handoff + push `main`**.
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
