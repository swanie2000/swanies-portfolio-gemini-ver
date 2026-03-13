📄 BROWSER_CONTEXT_NARRATIVE.md (Updated 2026-03-13)
Markdown

# 📄 BROWSER_CONTEXT_NARRATIVE.md (Updated 2026-03-13)

## 1. Project Overview
- **App Name:** Swanie’s Portfolio
- **Current Branch:** `main` (Restored to **Command & Control Phase 1** | Hash: `df88dbb`)
- **Status:** **STABLE RETREAT.** All experimental "Clean Slate" code has been discarded. Project is building and UI is fully functional.

## 2. Architectural Status
- **The Tower (DataSyncCoordinator):** Manages the 30s Rate Limiter.
- **The Vault (AssetRepository):** Handles the database and delegates timing to the Tower.
- **The Bridge (AssetViewModel):** Standardizes UI data flow.
- **UI Components:** `FullAssetCard` and `CompactAssetCard` are strictly defined. **DO NOT RENAME PARAMETERS** (e.g., `isDragging` and `onExpandToggle` are hard requirements).

## 3. Failed Protocols & Post-Mortem (The "March 13th Experiment")
> **⚠️ CRITICAL WARNING TO FUTURE AI AGENTS:**
> We attempted a "Sequential Sync" refactor that resulted in a total UI failure.
> - **Failed Idea:** Moving all saving/fetching logic into a single "Fire-and-Forget" sequential block.
> - **The Result:** Massive "Unresolved Reference" errors. The UI parameter signatures (Card components) were accidentally altered, breaking the `MyHoldingsScreen` compilation.
> - **The Lesson:** **NEVER** attempt a "Clean Slate" rebuild of the Repository or ViewModel without first locking the existing UI function signatures. The UI and the Data Engine are tightly coupled via specific parameter names.

## 4. The Path Forward (The "Almost Working" Blueprint)
The goal remains to solve the 429 Rate Limit and "No Data" bug on new asset adds.
- **The Sequence:** `Database Write` -> `500ms Delay` -> `API Price Fetch` -> `Database Update`.
- **The Implementation:** This must be performed "surgically" inside the existing `AssetViewModel` or `Repository` functions **without** changing the function names or parameter types that `MyHoldingsScreen.kt` relies on.

## 5. Feature Map & UI Status
- 🟢 **Metals Market Watch:** Functional with direct-draw sparklines. **(PROTECT THIS CODE)**.
- 🟢 **Holdings Dashboard:** Visuals restored. Drag-to-delete and Reordering are active.
- 🟡 **Rate Limiter:** Active, but requires refinement for the initial fetch of a brand-new asset.

## 6. Engineering Protocols ("Safe Harbor" Rules)
- **Signature Integrity:** AI must verify `FullAssetCard` and `CompactAssetCard` definitions before suggesting any `LazyColumn` changes.
- **Full File Authority:** AI must provide the **complete file output** for any changes. No partial snippets.
- **The 500ms Buffer:** Observe a mandatory delay between a Room write and a network refresh.

## 7. Modularization Roadmap (The "Firewall" Strategy)
**Strategy:** Break the code apart to prevent one error from destroying the entire UI.
- **Phase 1 (UI Isolation):** Extract `FullAssetCard` and `CompactAssetCard` into `ui/components/AssetCards.kt`.
- **Phase 2 (Logic Extraction):** Move CoinGecko and Yahoo API logic into dedicated service files (e.g., `data/api/MetalPriceService.kt`).
- **Phase 3 (Screen Isolation):** Ensure `AmountEntryScreen` and `MetalsAuditScreen` remain standalone with their own ViewModels.
- **Protocol:** Push to GitHub after **EVERY** successful file separation.