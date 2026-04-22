🦢 SWANIES PORTFOLIO: MASTER NARRATIVE (V39.0: THE UNIFIED CONTRACT)

🎯 THE CORE MISSION
To maintain a commercial-grade financial vault where user data is sovereign, biometrics are hardware-enforced, and the UI is a cinematic, high-precision experience that survives high-density settings and real-world widget workflows.

⚠️ 1. MANDATORY AGENT OPERATING PROCEDURES (THE LANE LOCK)

    FULL FILE OUTPUTS ONLY: Swanie requires full file context. Partial snippets are forbidden unless explicitly requested.

    SOVEREIGN SHIELD: Never push local.properties, secrets, or API keys. Enforce .gitignore discipline.

    STABILITY FIRST: Prefer smallest safe changes. Preserve existing architecture unless explicitly directed.

    DENSITY SHIELD: UI text scaling must respect the clamp pattern: (originalSp.toPx() / fontScale.coerceAtMost(1.15f)).toSp().

🛡️ 2. V39.0 ARCHITECTURAL PILLARS (UNIFIED CONTRACT)

    Vault-Scoped Widget Truth:
    Widget configuration belongs to VaultEntity, not individual assets.
    Canonical fields:
      - selectedWidgetAssets (ordered CSV of coin IDs)
      - showWidgetTotal
      - widgetBgColor / widgetBgTextColor / widgetCardColor / widgetCardTextColor
      - appWidgetId binding

    Unified ViewModel Contract:
      - SettingsViewModel manages target vault selection and configuration save pipeline.
      - AssetViewModel exposes widgetSelectedAssetIds + widgetShowTotal and writes vault-scoped widget updates.
      - Both reference the same Room-backed vault fields, eliminating split-brain widget state.

    Sequence-Driven Widget Composition:
    Manual 1-5 selection logic has been removed from Widget Manager. Ordering now defines inclusion + rank.
    Reordering writes exact sequence into widgetSelectedAssetIds and persists to vault.selectedWidgetAssets.

    Tactile Reorder UX:
    Reorderable list uses long-press anywhere on card to initiate drag, with haptic + subtle lift scale feedback.
    UI updates immediately while dragging; persistence occurs on drag stop.

📉 3. V39.0 SYSTEM SNAPSHOT (FINAL LOGIC)

    Widget Manager Screen:
      - Uses SettingsViewModel for targetVaultId / targetVault / targetVaultAssets.
      - Uses AssetViewModel.widgetSelectedAssetIds to resolve and render ordered widget assets.
      - ReorderableLazyColumn updates local order instantly, then commits reordered IDs through:
            AssetViewModel.updateWidgetSelectionForCurrentVault(reorderedIds)
      - Save action commits:
            saveWidgetConfiguration(...)
            saveWidgetAppearance(...)
            updateShowWidgetTotal(...)
      - Config mode supports direct widget feedback via forceImmediateRemoteViewsUpdate(...).

    AssetViewModel Contract:
      - currentVaultId derives from ThemePreferences.currentVaultId.
      - holdings derive from assetDao.getAssetsByVault(currentVaultId).
      - widgetSelectedAssetIds and widgetShowTotal are vault-scoped flows.
      - updateWidgetSelectionForCurrentVault(...) persists ordered IDs (trim/distinct; no legacy 1-5 cap).
      - updateWidgetShowTotalForCurrentVault(...) writes vault total visibility.
      - Widget refresh is triggered via repository.pushAssetsToWidget(...).

    Vault Selection Contract:
      - Global app vault: ThemePreferences.currentVaultId (selected through MainViewModel.selectVault).
      - Widget-edit vault: SettingsViewModel.targetVaultId (can differ from active app vault).
      - Widget instance binding: appWidgetId -> vault resolved through VaultDao.getVaultByAppWidgetId.
      - DAO boundaries:
            AssetDao handles vault-filtered assets.
            VaultDao owns widget state fields and widget-vault binding.

🚀 4. THE FUTURE PATH (CURRENT WORK-IN-PROGRESS)
Task	Description	Priority
Widget Contract Hardening	Add regression coverage for reorder persistence, vault switching, and widget instance rebinding.	IMMEDIATE
Market Watch Rebuild	Apply Pin-Anchor architecture across Market/Price surfaces for full app parity.	HIGH
Sovereign Bridge	Harden cloud sync behavior around vault-scoped widget mutations.	MEDIUM

🚀 NEXT AGENT COMMAND
"The narrative is now V39.0: THE UNIFIED CONTRACT and represents the current single-truth widget architecture.

Current Objective: Preserve the unified vault-scoped contract while adding regression tests for reorder persistence and vault-binding correctness.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."