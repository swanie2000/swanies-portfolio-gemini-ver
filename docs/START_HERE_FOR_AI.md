# START HERE (Swanie’s Portfolio AI Workflow)

## Purpose
This repo uses an AI-safe context system so browser AIs and Studio agents stay in sync.

## Where we left off (read first)
1. Open **`docs/BROWSER_CONTEXT_NARRATIVE.md`** and read **`AI AGENT HANDOFF (READ FIRST)`** at the top. That block is the **canonical** “current engineering + product + next steps” summary for any new agent.
2. **`docs/BROWSER_CONTEXT_MASTER.md`** — Level-4 process rules plus a **synced excerpt** of the narrative for paste-only workflows. If MASTER and NARRATIVE disagree, **fix NARRATIVE first**, then refresh the `BEGIN_NARRATIVE` section in MASTER.
3. **`Master_Build_Checklist.md`** — Shipped milestones and the **Play Store path forward** checklist.

## Files
- `docs/BROWSER_CONTEXT_HEADER.txt`  
  Level-4 rules. Do not change unless intentionally updating behavior rules.
- `docs/BROWSER_CONTEXT_DUMP.md`  
  SHORT paste-friendly dump (generated).
- `docs/BROWSER_CONTEXT_DUMP_FULL.md`  
  FULL reference dump (generated).
- `scripts/rebuild_browser_context_dump.bat`  
  Rebuilds both dumps.

## Daily Workflow
### Start of day
1) `git pull`
2) Run: `scripts\rebuild_browser_context_dump.bat`
3) Paste `docs/BROWSER_CONTEXT_DUMP.md` into browser AI.

### During work / milestones
- After major milestones: `git commit` + `git push`

### End of day
1) Optional: run rebuild again
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