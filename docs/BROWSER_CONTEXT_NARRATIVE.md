🦢 SWANIES PORTFOLIO: MASTER NARRATIVE (V39.0 FINAL: PURE PENCIL ARCHITECTURE)

🎯 THE CORE MISSION
To maintain a commercial-grade financial vault where user data is sovereign, biometrics are hardware-enforced, and the UI is a cinematic, high-precision experience that survives high-density settings and real-world widget workflows.

⚠️ 1. MANDATORY AGENT OPERATING PROCEDURES (THE LANE LOCK)

    FULL FILE OUTPUTS ONLY: Swanie requires full file context. Partial snippets are forbidden unless explicitly requested.

    SOVEREIGN SHIELD: Never push local.properties, secrets, or API keys. Enforce .gitignore discipline.

    STABILITY FIRST: Prefer smallest safe changes. Preserve existing architecture unless explicitly directed.

    DENSITY SHIELD: UI text scaling must respect the clamp pattern: (originalSp.toPx() / fontScale.coerceAtMost(1.15f)).toSp().

🛡️ 2. V39.0 FINAL — PURE PENCIL (DETERMINISTIC ENTRY)

    Single entry point:
      - There is no in-app “Widget Manager” button in Settings. That path was removed.
      - The user opens the editor only from the home widget (Glance pencil): [WidgetClickCallback] starts [WidgetConfigActivity] with [AppWidgetManager.EXTRA_APPWIDGET_ID] (and a relay URI for anti-conflation).

    Activity + manifest contract:
      - [WidgetConfigActivity] is `android:exported="false"` and has **no** `<intent-filter>`. It is not launchable as a generic `APPWIDGET_CONFIGURE` target from the system.
      - If `appWidgetId` is missing or invalid after [resolveAppWidgetId], the activity calls `finish()` immediately — no portfolio picker, no multi-widget dialog, no inference from “any widget on the host.”

    Deterministic binding:
      - [SettingsViewModel.forceVaultSwitch(appWidgetId, isAppWidgetId = true)] resolves the vault row for that exact widget instance.
      - [WidgetManagerScreen] receives a valid `configAppWidgetId` only when the activity has already accepted the intent.

🛡️ 3. V39.0 FINAL — FLAT STAGE + TACTILE REORDER (WIDGET MANAGER UI)

    Flat stage layout ([WidgetManagerScreen]):
      - One primary [LazyColumn] (“flat stage”): preview, appearance, hide totals, then assets — no nested asset list.
      - **Sticky header** for the **ASSETS** section (`stickyHeader`) so the section title stays visible while scrolling the reorder surface.
      - Reorder uses `sh.calvin.reorderable` on the same list; slot indices account for leading items (`WIDGET_FLAT_LIST_FIRST_ASSET_INDEX`).

    600 ms weighted reorder physics:
      - **Row resize / slot feel:** `WidgetReorderItemAnimationSpec` — `tween<IntSize>(600, LinearOutSlowInEasing)` on checked/dragging rows.
      - **List placement / slide-to-slot:** `WidgetFlatListPlacementSpec` — `tween<IntOffset>(600, LinearOutSlowInEasing)` for `animateItem` placement on reorderable asset rows.
      - After drag stop, a short settle delay (`WIDGET_DRAG_SETTLE_DELAY_MS` = 380 ms) runs before persisting order via [AssetViewModel.updateWidgetSelectionForCurrentVault].

    Tactile reorder UX:
      - Long-press on the asset row handle initiates drag; haptics on drag start; persistence on drag end after settle.

🛡️ 4. V39.0 FINAL — SAVE, REFRESH, AND TASK LIFECYCLE

    Save pipeline:
      - [AssetViewModel.saveWidgetConfiguration] persists vault widget fields (including optional `appWidgetId` rebind), pushes Glance data via [AssetRepository.pushFreshAssetsToWidget], and sends [ACTION_APPWIDGET_UPDATE] for the affected instance(s).
      - [SettingsViewModel] still applies appearance and totals (`saveWidgetAppearance`, `updateShowWidgetTotal`) and may call `forceImmediateRemoteViewsUpdate` for instant RemoteViews feedback.

    Primary completion behavior ([WidgetConfigActivity]):
      - On **SAVE & EXIT** success and on **back**, the activity uses **`finishAndRemoveTask()`** as the primary way to tear down the translucent config task and return the user cleanly to the home screen, alongside the post-save broadcast so the launcher-bound widget redraws with the same `appWidgetId` the pencil supplied.

    Vault-scoped truth (unchanged):
      - Canonical widget fields live on [VaultEntity]: `selectedWidgetAssets`, `showWidgetTotal`, widget colors, `appWidgetId`.
      - [AssetViewModel.widgetSelectedAssetIds] / `setWidgetSelectionVaultId` keep selection aligned with the vault being edited.

📉 5. V39.0 SYSTEM SNAPSHOT (FINAL LOGIC)

    Widget Manager Screen:
      - [SettingsViewModel]: `targetVaultId` / `targetVault` / `targetVaultAssets` for the resolved vault.
      - [AssetViewModel]: ordered selection, toggles, drag-end persistence, and `saveWidgetConfiguration` on save.
      - Config mode: `configAppWidgetId` is always the pencil’s widget id when the screen is shown.

    AssetViewModel contract (widget slice):
      - `saveWidgetConfiguration(portfolioVaultId, appWidgetId, selectedIds, onComplete)` is the save entry used from the widget manager.
      - Ongoing edits still use `updateWidgetSelectionForCurrentVault`, `pushAssetsToWidget` / `pushFreshAssetsToWidget` as appropriate.

    Vault selection contract:
      - Global app vault: [ThemePreferences.currentVaultId].
      - Widget-edit vault: driven by the widget instance → [VaultDao.getVaultByAppWidgetId] path when opened from the pencil (may differ from the active app vault while editing).

🚀 6. THE FUTURE PATH (CURRENT WORK-IN-PROGRESS)
Task	Description	Priority
Widget Contract Hardening	Add regression coverage for reorder persistence, pencil-only entry, and widget instance rebinding.	IMMEDIATE
Market Watch Rebuild	Apply Pin-Anchor architecture across Market/Price surfaces for full app parity.	HIGH
Sovereign Bridge	Harden cloud sync behavior around vault-scoped widget mutations.	MEDIUM

🚀 NEXT AGENT COMMAND
"The narrative is now V39.0 FINAL: Pure Pencil Architecture & tactile reorder sync.

Current Objective: Preserve deterministic pencil-only entry and the flat LazyColumn contract; add regression tests for reorder persistence and instance binding.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."
