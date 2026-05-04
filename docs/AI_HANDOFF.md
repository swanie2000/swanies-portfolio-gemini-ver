# Swanie’s Portfolio — AI handoff (single document)

**One file for the next Cursor agent:** product stance, engineering snapshot, what changed recently, what to do next, and where to look in code.  
**You maintain this file** when the owner says things like *“update the handoff”* or *“update the narrative and push.”*

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

**Last updated:** 2026-05-03 (evening — single-doc handoff simplification)  

**Product:** Android app **Swanie’s Portfolio** — crypto & precious metals tracker. Owner considers the app **feature-complete for v1** (**feature freeze**). Remaining work is **shipping** (Play Console, compliance, listing, AAB, RevenueCat/Play QA), not new product features unless the owner reopens scope.

**Repo / branch:** `swanies-portfolio-gemini-ver` on GitHub (`swanie2000`), default branch **`main`**. Legacy repo **`swanies-portfolio`** was deleted.

**Public site:** **`https://swaniedesigns.com`** — static marketing + **draft** privacy page from **`website/`**, deployed by **GitHub Actions** (`.github/workflows/deploy-website.yml`). Custom domain + **HTTPS** on GitHub Pages. **`website/privacy.html`** still has **`[bracket]`** placeholders — finalize before Play uses the URL as final policy.

**Play blocker:** **Google identity verification** still pending (human / email). Publishing stays blocked until Google finishes that chain (then Play Console app + phone checks, etc.).

---

## Next steps (priority order)

1. **Play (human):** When identity clears → device / phone verification in Play Console → create listing, AAB to internal → closed testing, Data safety, content rating, SKUs ↔ RevenueCat, license testers — details in **`Master_Build_Checklist.md`** § Play Store path forward.
2. **Privacy URL:** Replace placeholders in **`website/privacy.html`**, align with in-app Privacy & Terms + Data safety; remove **`noindex`** when ready; push **`main`** to redeploy.
3. **Pre-launch QA:** Backup round-trip; purchase / restore / expiry; widgets + Pro gates; GRAM/KILO metals on a real device.
4. **Backlog (non-blocking for v1):** V40.36 auth instrumentation, V40.61 monetization telemetry, V40.69 small-screen polish — only if scheduled post-1.0.

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
| Pro / billing | `billing/`, `MonetizationManager.kt` |
| About / legal | `AboutScreen.kt`, `Routes.kt`, `MainActivity.kt`, `values/strings.xml` + `values-*` |
| Marketing site | `website/`, `.github/workflows/deploy-website.yml` |
| Play checklist | `Master_Build_Checklist.md` |

---

## Session history (newest first)

- **2026-05-03 — Handoff simplification:** Retired multi-file browser bundle (`BROWSER_CONTEXT_MASTER` dump scripts, `DUMP.md`). **Canonical handoff = this file only** (`docs/AI_HANDOFF.md`). `START_HERE_FOR_AI.md` and **Update the Handoff** Cursor rule updated. Archived narrative log kept under `docs/BROWSER_CONTEXT_NARRATIVE.md` (historical).
- **2026-05-03 — V40.73 / public site:** `https://swaniedesigns.com` live (Pages + Cloudflare); `deploy-website.yml` build/deploy fix; `website/CNAME`; legacy GitHub repo removed; Play identity still waiting on Google.
- **2026-05-02 — V40.72:** i18n MissingTranslation closure (64 keys × 19 locales); ship-only narrative handoff.
- **2026-05-02 — V40.71:** Metal spot pipeline, backup screen split, bug reports, About, i18n wave; Play fee paid + identity submitted.

*(Older milestone detail lives in git history on `docs/BROWSER_CONTEXT_NARRATIVE.md` if you need archaeology.)*

---

## Working agreements (Cursor)

- Prefer **minimal, safe edits**; don’t refactor unrelated code.
- Don’t assume files exist — read before changing.
- **Canonical state for the next agent** = **this file** + the actual repo. If something disagrees with code, **code wins** — then fix this doc.
- Browser-era paste bundles (`BROWSER_CONTEXT_MASTER.md` long form) are **retired**; ignore unless you intentionally need old text from history.
