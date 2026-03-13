📄 BROWSER_CONTEXT_NARRATIVE.md (Updated 2026-03-13)

1. Project Overview

   App Name: Swanie’s Portfolio

   Current Branch: main (Restored to Command & Control Phase 1 | Hash: df88dbb)

   Status: STABLE RETREAT. All experimental "Clean Slate" code has been discarded.

2. Architectural Status

   The Tower (DataSyncCoordinator): Manages the 30s Rate Limiter.

   The Vault (AssetRepository): Handles the database and delegates timing to the Tower.

   The Bridge (AssetViewModel): Standardizes UI data flow.

   UI Components: FullAssetCard and CompactAssetCard are strictly defined. DO NOT RENAME PARAMETERS (e.g., isDragging and onExpandToggle are hard requirements).

3. Failed Protocols & Post-Mortem (The "March 13th Experiment")

   Warning to Future AI Agents: > We attempted a "Sequential Sync" refactor today that failed.

        Failed Idea: We tried to move all saving/fetching logic into a single "Fire-and-Forget" sequential block.

        The Result: It caused massive "Unresolved Reference" errors because the UI parameter signatures (Card components) were not preserved during the refactor.

        The Lesson: Do not attempt a "Clean Slate" rebuild of the Repository or ViewModel without first locking the existing UI function signatures. The UI and the Data Engine are tightly coupled via specific parameter names.

4. The Path Forward (The "Almost Working" Blueprint)
   The goal remains to solve the 429 Rate Limit and "No Data" bug on new asset adds. The proven path forward is:

   The Sequence: Database Write -> 500ms Delay -> API Price Fetch -> Database Update.

   The Implementation: This must be done inside the existing AssetViewModel or Repository functions without changing the function names or parameter types that MyHoldingsScreen.kt relies on.

5. Feature Map & UI Status

   🟢 Metals Market Watch: Functional with direct-draw sparklines.

   🟢 Holdings Dashboard: Visuals restored. Drag-to-delete and Reordering are active.

   🟡 Rate Limiter: Active, but requires "Surgical" refinement to handle the initial fetch of a brand-new asset without hitting 429 errors.

6. Engineering Protocols ("Safe Harbor" Rules)

   Signature Integrity: AI must check the FullAssetCard and CompactAssetCard definitions before suggesting any LazyColumn changes.

   Full File Authority: Always provide the complete file output.

   The 500ms Buffer: Never trigger an API call immediately after a Room write; the ID must exist in the DB first.