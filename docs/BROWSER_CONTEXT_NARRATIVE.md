📄 BROWSER_CONTEXT_NARRATIVE.md (Updated 2026-03-10)

1. Project Overview
   App Name: Swanie’s Portfolio
   Current Branch: main (Command & Control Phase 1 | Tower & Vault Integrated)

2. Architectural Status
   Status: COMMAND & CONTROL (CENTRALIZED GOVERNANCE)

   The "Tower" (DataSyncCoordinator): A specialized singleton that manages the "Leaky Bucket" (30s Rate Limiter). 

   The "Vault" (AssetRepository): Now refactored to delegate timing authority to the Tower. It handles the "Forced Nudge" protocol for critical user actions.

   The "Bridge" (AssetViewModel): Stabilized via distinctUntilChanged() to prevent UI loops.

3. Feature Map & UI Status

🟢 Completed & Locked (Today’s Wins)
    - DataSyncCoordinator Implementation (30s Rate Limiting).
    - Refined Force-Sync Protocol for asset addition.
    - Loop Stabilization in AssetViewModel.

🔴 Bug Tracker (Current Priority)
    - 500ms Handoff verification for new asset data population.
