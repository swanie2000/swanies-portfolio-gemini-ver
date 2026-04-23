🦢 SWANIES PORTFOLIO: MASTER NARRATIVE (V40.4 FINAL: WIZARD FUNNEL + PURE PENCIL)

🎯 THE CORE MISSION
To maintain a commercial-grade financial vault where user data is sovereign, biometrics are hardware-enforced, and the UI is a cinematic, high-precision experience that survives high-density settings and real-world widget workflows.

⚠️ 1. MANDATORY AGENT OPERATING PROCEDURES (THE LANE LOCK)

    FULL FILE OUTPUTS ONLY: Swanie requires full file context. Partial snippets are forbidden unless explicitly requested.

    SOVEREIGN SHIELD: Never push local.properties, secrets, or API keys. Enforce .gitignore discipline.

    STABILITY FIRST: Prefer smallest safe changes. Preserve existing architecture unless explicitly directed.

    DENSITY SHIELD: UI text scaling must respect the clamp pattern: (originalSp.toPx() / fontScale.coerceAtMost(1.15f)).toSp().

🛡️ 2. V40.4 FINAL — PURE PENCIL (DETERMINISTIC ENTRY)

    Single entry point:
      - There is no in-app “Widget Manager” button in Settings. That path was removed.
      - The user opens the editor only from the home widget (Glance pencil): [WidgetClickCallback] starts [WidgetConfigActivity] with [AppWidgetManager.EXTRA_APPWIDGET_ID] (and a relay URI for anti-conflation).

    Activity + manifest contract:
      - [WidgetConfigActivity] is `android:exported="false"` and has **no** `<intent-filter>`. It is not launchable as a generic `APPWIDGET_CONFIGURE` target from the system.
      - If `appWidgetId` is missing or invalid after [resolveAppWidgetId], the activity calls `finish()` immediately — no portfolio picker, no multi-widget dialog, no inference from “any widget on the host.”

    Deterministic binding:
      - [SettingsViewModel.forceVaultSwitch(appWidgetId, isAppWidgetId = true)] resolves the vault row for that exact widget instance.
      - [WidgetManagerScreen] receives a valid `configAppWidgetId` only when the activity has already accepted the intent.

🛡️ 3. V40.4 FINAL — WIZARD FUNNEL + TACTILE REORDER (WIDGET MANAGER UI)

    Wizard Funnel layout ([WidgetManagerScreen]):
      - Funnel navigation uses integer page state (`currentPage`): `0=SETUP`, `1=ASSETS`, `2=STYLE`, `3=PREVIEW`.
      - **SETUP**: portfolio selector + show total control.
      - **ASSETS**: sticky header + flat reorder stage with persisted ordering.
      - **STYLE**: compact color studio controls.
      - **PREVIEW**: guarded preview path with immutable snapshot filtering and fallback error surface.

    600 ms weighted reorder physics:
      - **Row resize / slot feel:** `WidgetReorderItemAnimationSpec` — `tween<IntSize>(600, LinearOutSlowInEasing)` on checked/dragging rows.
      - **List placement / slide-to-slot:** `WidgetFlatListPlacementSpec` — `tween<IntOffset>(600, LinearOutSlowInEasing)` for `animateItem` placement on reorderable asset rows.
      - After drag stop, a short settle delay (`WIDGET_DRAG_SETTLE_DELAY_MS` = 380 ms) runs before persisting order via [AssetViewModel.updateWidgetSelectionForCurrentVault].

    Tactile reorder UX (unchanged):
      - Long-press on the asset row handle initiates drag; haptics on drag start; persistence on drag end after settle.

🛡️ 4. V40.4 FINAL — SAVE, REFRESH, AND TASK LIFECYCLE

    Save pipeline:
      - [AssetViewModel.saveWidgetConfiguration] persists vault widget fields (including optional `appWidgetId` rebind), pushes Glance data via [AssetRepository.pushFreshAssetsToWidget], and sends [ACTION_APPWIDGET_UPDATE] for the affected instance(s).
      - [SettingsViewModel] still applies appearance and totals (`saveWidgetAppearance`, `updateShowWidgetTotal`) and may call `forceImmediateRemoteViewsUpdate` for instant RemoteViews feedback.

    Primary completion behavior ([WidgetConfigActivity]):
      - On **SAVE & EXIT** success and on **back**, the activity uses **`finishAndRemoveTask()`** as the primary way to tear down the translucent config task and return the user cleanly to the home screen, alongside the post-save broadcast so the launcher-bound widget redraws with the same `appWidgetId` the pencil supplied.

    Vault-scoped truth (unchanged):
      - Canonical widget fields live on [VaultEntity]: `selectedWidgetAssets`, `showWidgetTotal`, widget colors, `appWidgetId`.
      - [AssetViewModel.widgetSelectedAssetIds] / `setWidgetSelectionVaultId` keep selection aligned with the vault being edited.

📉 5. V40.4 CURRENT STATE (TOMORROW TROUBLESHOOTING SNAPSHOT)

    Widget Manager Screen:
      - [SettingsViewModel]: `targetVaultId` / `targetVault` / `targetVaultAssets` for the resolved vault.
      - [AssetViewModel]: ordered selection, toggles, drag-end persistence, and `saveWidgetConfiguration` on save.
      - Config mode: `configAppWidgetId` is always the pencil’s widget id when the screen is shown.
      - PREVIEW stability guard:
        - Uses immutable list snapshot (`orderedWidgetAssets.toList()` then filter/take).
        - Uses data-prep `runCatching` gate; renders **"Preview Error"** fallback on preparation failure.
        - Uses static simulated rows (`SimulatedAssetRow`) with hardcoded price/total strings to isolate render-path crashes.
      - Known next-debug target:
        - If PREVIEW still hard-crashes on some devices, first inspect launcher/Activity lifecycle boundary around widget config return and any stale Compose state entering page `3`.

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
"The narrative is now V40.4 FINAL: Wizard Funnel + Pure Pencil architecture.

Current Objective: Stabilize PREVIEW page rendering path and verify wizard funnel page-state persistence (`currentPage`) across widget-config lifecycle edges.

Constraint: Keep changes minimal and safe. Maintain Sovereign Shield. Confirm 'SOVEREIGN LOCK' before any architectural shift."
