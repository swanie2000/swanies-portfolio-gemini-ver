### BEGIN_NARRATIVE
PROJECT OVERVIEW
- Android portfolio tracking app (Swanie’s Portfolio).
- Uses local Room database + CoinGecko API.
- Focus is stability and incremental improvements.
- AI agents must avoid large refactors unless directed for architecture (Level 4).

CURRENT APP FLOW
- HomeScreen displays overview with custom splash-to-home animation.
- MyHoldings manages assets and features a header-integrated settings link.
- AmountEntryScreen handles numeric input.
- AssetPicker used for selecting coins/assets (Branded with Swan logo).
- SettingsScreen allows user-defined themes (Persistent via DataStore).
- Branding: Dynamic engine supporting Dark Navy (#000416) default, plus Emerald, Royal, Charcoal, and Burgundy.

KEY FILE INDEX (high signal files)
- ui/theme/Theme.kt (Global theme logic & color scheme mapping)
- ui/theme/Color.kt (Primary brand color definitions)
- data/ThemePreferences.kt (DataStore logic for persistent settings)
- ui/settings/SettingsScreen.kt (Theme selection & Light/Dark toggle UI)
- ui/settings/SettingsViewModel.kt (Theme state management)
- app/src/main/res/drawable/swan_launcher_icon.png (Primary brand asset)
- ui/navigation/NavGraph.kt (Navigation wiring including Settings route)

KNOWN PROBLEMS / RISKS
- UI Scoping: Ensure @Composable calls stay within correct scopes to avoid build errors.
- Role-based theming: Avoid hardcoded colors; use MaterialTheme.colorScheme.background for screen roots.
- Gradle sync: Rebuild project after theme changes to ensure R classes update.

CURRENT FEATURE STATUS
- COMPLETED: Refactored theme files to dedicated ui/theme package.
- COMPLETED: Integrated Jetpack DataStore for persistent theme and dark mode settings.
- COMPLETED: Implemented 5-color selectable theme system.
- COMPLETED: Connected all major screens (Holdings, Settings, AssetPicker) to the dynamic theme engine.
- IN PROGRESS: Visual polish for Light Mode tonal palettes.
- PLANNED: Visual "Rainbow" color picker (HSV) for custom color selection.
- PLANNED: Sync Splash Screen and Adaptive Icon color to user-selected theme.
### END_NARRATIVE