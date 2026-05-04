# START HERE (Swanie’s Portfolio AI Workflow)

## Purpose
This repo uses an AI-safe context system so browser AIs and Studio agents stay in sync.

## Where we left off (read first)
1. Open **`docs/BROWSER_CONTEXT_NARRATIVE.md`** and read **`AI AGENT HANDOFF (READ FIRST)`** at the top. That block is the **canonical** “current engineering + product + next steps” summary for any new agent.
2. **`docs/BROWSER_CONTEXT_MASTER.md`** — Level-4 process rules plus a **synced excerpt** of the narrative for paste-only workflows. If MASTER and NARRATIVE disagree, **fix NARRATIVE first**, then refresh the `BEGIN_NARRATIVE` section in MASTER.
3. **`Master_Build_Checklist.md`** — Shipped milestones and the **Play Store path forward** checklist.

## Files
- **`website/`** — Static marketing site deployed to **`https://swaniedesigns.com`** (see `website/README.md`, workflow `.github/workflows/deploy-website.yml`).
- `docs/BROWSER_CONTEXT_HEADER.txt`  
  Level-4 rules. Do not change unless intentionally updating behavior rules.
- `docs/BROWSER_CONTEXT_DUMP.md`  
  **Auto-generated** short paste: pointers + git fingerprint + path indexes (run **`docs\rebuild_browser_context_dump.bat`** to refresh after narrative/master updates).
- `docs/rebuild_browser_context_dump.bat`  
  Runs **`docs\Rebuild-BrowserContextPaths.ps1`**: refreshes the **AUTO-GENERATED** tail in **`docs/BROWSER_CONTEXT_MASTER.md`** (everything after **`### END_NARRATIVE`**) and rewrites **`docs/BROWSER_CONTEXT_DUMP.md`**. Does **not** replace the Level-4 header or narrative body in MASTER.

## Daily Workflow
### Start of day
1) `git pull`
2) Run: `docs\rebuild_browser_context_dump.bat`
3) Paste `docs/BROWSER_CONTEXT_DUMP.md` (or `docs/BROWSER_CONTEXT_MASTER.md`) into browser AI as needed.

### During work / milestones
- After major milestones: `git commit` + `git push`

### End of day
1) Optional: run `docs\rebuild_browser_context_dump.bat` again
2) `git commit` + `git push`

## Browser AI Rules
- Browser AI is advisory only.
- Must follow the Level-4 header.
- If it needs file contents: `NEED FILE: path/to/file`.

## Studio Agent Rules
- Studio agent can edit code.
- Make minimal edits.
- Prefer one file at a time.
- Provide FULL file replacements when changing code.