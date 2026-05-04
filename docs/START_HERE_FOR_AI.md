# START HERE (Swanie’s Portfolio — Cursor / AI)

## One document

Open **`docs/AI_HANDOFF.md`** and read it end-to-end. That file is the **only** handoff the next agent needs: current product stance, engineering snapshot, next steps, recent session history, and a compact file map.

When the owner says **“update the handoff”** or **“update the narrative and push”**, edit **`docs/AI_HANDOFF.md`**, commit, and push (see **`.cursor/rules/update-handoff.mdc`**). Cursor agents are nudged to suggest that phrase at closure, after wins, and **EOD** — see **`docs/AI_HANDOFF.md`** (§ *Nudge the owner*).

## Optional references

- **`Master_Build_Checklist.md`** — shipped milestones and Play Store checklist (ticks), not narrative prose.
- **`Narrative_Log.md`** — short dated milestone bullets; optional to append when you ship something notable.

Old browser-context files (`BROWSER_CONTEXT_NARRATIVE.md`, `MASTER.md`, `HEADER.txt`) were **removed**; deep history lives in **git** if you ever need archaeology.

## Daily habit (optional)

1. `git pull`
2. Skim **`docs/AI_HANDOFF.md`** if you are picking up after a break.

No generated dumps, no batch rebuild step.
