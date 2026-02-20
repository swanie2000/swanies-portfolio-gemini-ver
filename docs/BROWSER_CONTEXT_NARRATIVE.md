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

KEY FILE INDEX (high signal files)
- ui/holdings/AmountEntryScreen.kt (numeric input flow)
- ui/holdings/AmountEntryViewModel.kt (entry logic)
- data/repository/AssetRepository.kt (data source bridge)
- ui/navigation/NavGraph.kt (navigation wiring)

KNOWN PROBLEMS / RISKS
- Numeric keyboard/input is sensitive; avoid rewrites.
- CoinGecko integration still evolving.
- UI architecture still stabilizing.
- Prefer single-file fixes.

CURRENT FEATURE STATUS
- Basic navigation working.
- Holdings flow functional.
- Custom assets partially supported.
- UX polish ongoing.