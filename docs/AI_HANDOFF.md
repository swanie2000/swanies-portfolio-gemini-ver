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

**Last updated:** 2026-05-09 — **Adaptive launcher + fingerprint (owner verified):** Vector asset **`drawable/swan_launcher_extra_small_hq.xml`** — **`108×108`** viewport, art in **1104×859** space, **nested `<group>`** (scale **~0.0554** + translate) for safe-zone centering; **~10% smaller** than max width for extra launcher margin. **`mipmap-anydpi-v26/ic_launcher.xml`** + **`ic_launcher_round.xml`** **`foreground`** → **`@drawable/swan_launcher_extra_small_hq` directly** (not **`InsetDrawable`**). **`ic_launcher_foreground.xml`** kept as thin **`layer-list`** alias for tooling/docs only. **Why it mattered:** wrong intrinsic **`dp`** broke some OEMs; then **`InsetDrawable`** foreground made **credential / fingerprint** UI show **navy-only** while the launcher looked fine — both fixed. **Other Cursor / second PC:** same model ≠ same outcome without **`git pull`** of **`main`** and these files; handing an agent **only** the vector XML without **mipmap + viewport/group** wiring reproduces failures.

**Also shipped (prior same week):** **Portfolio toast** — **`showPortfolioToast`** + **`toast_portfolio.xml`** / **`toast_chip_background`**; **Home** language control **slow slide from left** after login column (**`LOGIN_COLUMN_ENTER_*`**, **`LANGUAGE_GLOBE_AFTER_LOGIN_MS`**).

**Icons elsewhere:** Splash / toast / widget still use **PNG + wrapper XML** as before; Image Asset wizard → **PNG** paths.

**Recently shipped (same week):** Marketing site screenshot captions; truthful Drive copy; TOS §7 / privacy §8; **`AssetViewModel`** Drive comments honest.

**Product:** Android app **Swanie’s Portfolio** — crypto & precious metals tracker. Owner considers the app **feature-complete for v1** (**feature freeze**). Remaining work is **shipping** (Play Console, compliance, listing, AAB, RevenueCat/Play QA), not new product features unless the owner reopens scope.

**Repo / branch:** `swanies-portfolio-gemini-ver` on GitHub (`swanie2000`), default branch **`main`**. Legacy repo **`swanies-portfolio`** was deleted.

**Public site:** **`https://swaniedesigns.com`** — static marketing + privacy page from **`website/`**, deployed by **GitHub Actions** (`.github/workflows/deploy-website.yml`). Custom domain + **HTTPS** on GitHub Pages. **`website/privacy.html`** now includes **§8 Terms of use / limitation of liability** (mirrors in-app §7) — **re-read vs Play Data safety** and counsel before calling the policy URL final.

**Play / Google:** **App created** in Play Console (**Swanie’s Portfolio**). **Dashboard (2026-05-08 snapshot):** “**Finish setting up your app**” items (privacy, app access, ads, content rating, target audience, data safety, store listing, category, contact, etc.) appear **complete**; **Internal testing** still needs **Create a new release** (AAB not rolled out yet). **Closed testing** stays locked until Play unlocks it (often after internal release is live). **Production** path shows **12 testers × 14 days** closed test before **Apply for production**. **Publishing overview** may show **Send app for review** disabled until Dashboard / quick checks allow — use banner link **Go to dashboard** when shown. **Release signing:** **`swanie_portfolio_release.jks`** — CLI **`keytool`** use **`-storetype PKCS12`** / Studio **JBR** if needed.

### Play Console — ordered steps (next session; do in order)

1. **Bump `versionCode`** in `app/build.gradle.kts` if re-uploading a build (Play rejects duplicate codes).
2. **Android Studio:** **Build → Generate Signed App Bundle** → **release** AAB with **`swanie_portfolio_release.jks`**.
3. **Play Console → Test and release → Internal testing → Create new release** → upload AAB → release notes → **Save** → **Review release** → **Roll out** to internal testers (testers already selected per Dashboard).
4. **Dashboard:** Confirm **Internal testing** checklist shows **Create release** / **Preview and confirm** done; refresh if **Closed testing** still says “finish setup” until Play updates.
5. **Test and release → Closed testing:** When unlocked — new release, invite **≥12** testers who **opt in**; run **≥14 days** for production-access requirement.
6. **Production:** When eligible, **Apply for production** / staged rollout per Console.
7. **Grow users → Store presence / Monetize with Play:** Screenshots, feature graphic, subscriptions + **RevenueCat** SKU alignment, **license testers** for `pro`.
8. **Publishing overview:** When **Send app for review** is enabled and you intend listing/metadata review — send; optional **Managed publishing** if you want manual go-live after approval.
9. **Website:** Finalize **`website/privacy.html`** (remove any **`[bracket]`** placeholders if present, **`noindex`** when final) → push **`main`** so Play policy URL matches; confirm **§8 liability** wording with counsel if needed.

---

## Next steps (priority order)

1. **Play (human):** Follow **§ Play Console — ordered steps** above (starts at **Internal testing AAB**). Keep **`Master_Build_Checklist.md`** in sync as items complete.
2. **Website (when listing exists):** Set **`PLAY_URL`** / **`TESTER_URL`** in **`website/index.html`** script block so CTAs go live.
3. **Optional cleanup:** Remove or keep **`app/src/main/assets/adi-registration.properties`** (ADI challenge); not needed on device after registration.
4. **Pre-launch QA:** Backup round-trip; purchase / restore / expiry; widgets + Pro gates; GRAM/KILO metals on device.
5. **Backlog (non-blocking for v1):** V40.36 auth instrumentation, V40.61 monetization telemetry, V40.69 small-screen polish — only if scheduled post-1.0.
6. **Icons (optional / post-v1):** Fine-tune **`swan_launcher_extra_small_hq.xml`** group **scale/translate** (launcher vs fingerprint share one foreground); splash/toast/widget wrappers; toast size in **`toast_portfolio.xml`**.

---

## Engineering snapshot (v1 ship stack)

- **Stack:** Kotlin, Jetpack Compose, Hilt, Room.
- **Pro:** RevenueCat + Play billing when on store; gates Theme Manager, multi-portfolio swipe, full Analytics, widget customization, etc.
- **Backup:** `VaultBackupEngine.kt` + `BackupRestoreScreen.kt` / `Routes.BACKUP_RESTORE` / `SettingsViewModel` — encrypted `.swpb`, WAL checkpoint via `query`, SAF, cold restart after restore.
- **Metals:** `MetalSpotMath.kt` + `AssetValuation` — GRAM/KILO/G → troy oz, USD valuation across holdings, analytics, `AssetRepository`, widget, theme, architect, settings.
- **Feedback:** `BugReportSubmitter` + `@Named("Feedback")` OkHttp in `NetworkModule`; Settings dialog; tag **`SwanieBugReport`**.
- **i18n:** `LanguageDisplay.kt`; maintained **`values-*`** locales include the former MissingTranslation key set (incl. 64-key parity pass).
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
| App / splash / toast | **Adaptive icon:** **`mipmap-anydpi-v26/ic_launcher.xml`** + **`ic_launcher_round.xml`** (foreground **`@drawable/swan_launcher_extra_small_hq`**); **`drawable/swan_launcher_extra_small_hq.xml`** (vector + group transforms); **`drawable/ic_launcher_foreground.xml`** (layer-list alias). **`swan_splash_icon_wrapper.xml`**, **`ic_toast_swan.xml`**, **`swan_widget_icon_padded.xml`**; **toasts:** **`CustomToast.kt`** (`showPortfolioToast`) + **`layout/toast_portfolio.xml`** + **`toast_chip_background.xml`**; **SVG → vector scripts:** **`scripts/svg_path_to_vector.mjs`** / **`.py`** |
| Home (login) | **`HomeScreen.kt`** — swan hero, **`AnimatedVisibility`** login column, language globe slide-in timing |
| Pro / billing | `billing/`, `MonetizationManager.kt` |
| About / legal | `AboutScreen.kt`, `TermsAndConditionsScreen.kt` (§1–§7), `Routes.kt`, `MainActivity.kt`, `values/strings.xml` + `values-*` (incl. **`terms_section_7_*`** per locale) |
| Marketing site | `website/` — **`index.html`**, **`styles.css`**, **`ic_swan_website.png`** (header), **`favicon-tab.png`** (tab / apple-touch, navy **`#000416`** plate), **`images/*.jpg`** (screenshots; `#screenshots` / **`.shot-card figcaption`**), legacy **`favicon.svg`** unused by HTML; `.github/workflows/deploy-website.yml` |
| Play checklist | `Master_Build_Checklist.md` |
| Play ADI challenge file | `app/src/main/assets/adi-registration.properties` (verification token; optional to remove after registration approved) |

---

## Session history (newest first)

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

- Prefer **minimal, safe edits**; don’t refactor unrelated code.
- Don’t assume files exist — read before changing.
- **Canonical state for the next agent** = **this file** + the actual repo. If something disagrees with code, **code wins** — then fix this doc.
- Browser-era paste bundles are **removed** from the tree; use **git** if you need old prose.
