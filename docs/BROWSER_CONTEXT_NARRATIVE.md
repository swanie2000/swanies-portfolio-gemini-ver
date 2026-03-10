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
5. Engineering Protocols (The "Safe Harbor" Rules)

- Zero-Tolerance for Spaghetti: No UI component is permitted to trigger a network call directly. All requests must pass through the AssetViewModel to the DataSyncCoordinator.
- The 30-Second Rule: The SYNC_THRESHOLD is a hard limit. Bypassing this via the `force` flag is reserved EXCLUSIVELY for manual user "Save" or "Add" actions.
- Room-First Handoff: Always ensure a minimum 500ms delay between a Database Write and a Network Refresh to prevent "Race Conditions" where the API is called before the Database has the new ID ready.
- Full File Authority: AI agents must provide full file outputs for any changes to the Tower, Vault, or Bridge to maintain structural integrity.