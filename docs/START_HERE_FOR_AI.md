# START HERE (Swanie’s Portfolio AI Workflow)

## Purpose
This repo uses an AI-safe context system so browser AIs and Studio agents stay in sync.

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