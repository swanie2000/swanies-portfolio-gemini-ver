### BEGIN_NARRATIVE
PROJECT OVERVIEW
- Android portfolio tracking app (Swanie’s Portfolio).
- Uses local Room database + CoinGecko API.
- Focus is stability and incremental improvements.
- AI agents must avoid large refactors.

CURRENT APP FLOW
- HomeScreen displays overview.
- MyHoldings manages assets.
- AmountEntryScreen handles numeric input.
- AssetPicker used for selecting coins/assets.
- Branding: Custom Navy Blue (#000416) theme with Swan logo.

KEY FILE INDEX (high signal files)
- app/src/main/res/drawable/swan_launcher_icon.png (Primary brand asset)
- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml (Adaptive icon config)
- ui/holdings/AmountEntryScreen.kt (numeric input flow)
- ui/holdings/AmountEntryViewModel.kt (entry logic)
- data/repository/AssetRepository.kt (data source bridge)
- ui/navigation/NavGraph.kt (navigation wiring)

KNOWN PROBLEMS / RISKS
- IDE Stability: Gradle sync can occasionally lose track of modules (resolved via cache invalidation).
- Numeric keyboard/input is sensitive; avoid rewrites.
- CoinGecko integration still evolving.
- UI architecture still stabilizing.
- Prefer single-file fixes.

CURRENT FEATURE STATUS
- COMPLETED: New branding implementation (Adaptive Icon with Navy #000416 background).
- Basic navigation working.
- Holdings flow functional.
- Custom assets partially supported.
- UX polish ongoing.
### END_NARRATIVE