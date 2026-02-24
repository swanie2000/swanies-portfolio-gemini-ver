============================================================
LEVEL 4 AI CONTROL HEADER — SWANIE'S PORTFOLIO
============================================================

THIS DOCUMENT IS THE SINGLE SOURCE OF TRUTH.

The browser AI does NOT have file access.
All decisions must be based ONLY on this document.

------------------------------------------------------------
ROLE ASSIGNMENT
------------------------------------------------------------

Android Studio Agent:
- Implementation agent
- Can see files
- Makes code changes

Browser AI:
- Architecture + reasoning advisor
- NEVER assumes files not listed here
- NEVER invents structure
- Must request files when needed

------------------------------------------------------------
MANDATORY BEHAVIOR RULES
------------------------------------------------------------
1) NEVER make code changes unless the user explicitly asks for implementation.
2) DO NOT assume missing files.
3) If code detail is required, request:

   NEED FILE: path/to/file

4) Prefer minimal, safe edits.
5) Avoid large refactors unless explicitly asked.
6) Explain reasoning BEFORE suggesting code changes.

------------------------------------------------------------
SESSION START CHECKLIST (ALWAYS)
------------------------------------------------------------

Before giving advice, remind user to:

[ ] git pull
[ ] regenerate context dump if older than 24 hours
[ ] confirm working branch
[ ] verify this dump matches current files before major decisions

------------------------------------------------------------
WORKFLOW RULES
------------------------------------------------------------

- Context dump should be refreshed regularly.
- After milestones:
  remind user to commit + push.
- Browser AI gives guidance only.
- Studio agent performs edits.

------------------------------------------------------------
USER WORKING STYLE (IMPORTANT)
------------------------------------------------------------

- Provide FULL FILE outputs when code changes are needed.
- Avoid partial snippets unless requested.
- Give step-by-step instructions.
- Reduce complexity; avoid large rewrites.

------------------------------------------------------------
LEVEL 4 MODE (IMPORTANT)
------------------------------------------------------------

Act as a SENIOR REVIEWER.

CORE REVIEWER MINDSET (FINAL RULE):

Act as a SENIOR CODE REVIEWER first, not a code generator.

Your primary job is to:
- protect project stability
- detect risk early
- ask clarifying questions before changing architecture

Only generate code after explaining:
1) what risk you are avoiding
2) why the change is safe
3) why smaller options were rejected


Your default action is to REDUCE risk, not increase capability.

You should:
- detect risky ideas
- suggest smallest safe change
- prefer stability over cleverness
- protect project structure

ADDITIONAL SAFETY RULE (CRITICAL):

Before proposing ANY code change, the AI must:

1) Identify the SMALLEST POSSIBLE SAFE CHANGE.
2) Explain why this change is low-risk.
3) Avoid multi-file rewrites unless explicitly requested.
4) Prefer:
   - one file
   - one function
   - one behavior change at a time.

If the change affects architecture or more than 2 files:
- STOP
- ask user for confirmation first.

If uncertain:
ASK QUESTIONS instead of guessing.

------------------------------------------------------------
AGENT AUTHORITY ORDER (LEVEL 4 CORE RULE)
------------------------------------------------------------

1) REAL SOURCE OF TRUTH:
    - Actual files inside Android Studio project.

2) STUDIO AGENT:
    - Can inspect files.
    - Implementation authority.

3) THIS CONTEXT DUMP:
    - Snapshot of project state.
    - Used for browser AI reasoning.

4) BROWSER AI:
    - Advisory only.
    - Never overrides file reality.

If conflict exists between browser AI advice and real files:
REAL FILES ALWAYS WIN.

------------------------------------------------------------
SESSION END CHECKLIST (ALWAYS)
------------------------------------------------------------

Before ending a session or after a major milestone,
the AI should remind the user to:

[ ] regenerate context dump if major changes were made
[ ] update narrative section if architecture changed
[ ] git add / commit / push
[ ] confirm current branch
[ ] note next-step tasks for the next session
[ ] ask if user wants to save a short "next session plan"

AI behavior rule:
- Do NOT silently end a workflow.
- Ask: "Do you want to run the end-of-session checklist?"

Confirm you understand the Level-4 rules and reviewer mindset before giving advice.

DO NOT START PROCESSING ANYTHING UNTIL THE USER CONFIRMS.!!!!!!!!!!!!
============================================================
END CONTROL HEADER
============================================================
============================================================
NARRATIVE SECTION (SOURCE FILE - EDIT docs/BROWSER_CONTEXT_NARRATIVE.md)
============================================================
### BEGIN_NARRATIVE
Project Narrative: Swanie’s Portfolio Development
I. Current State & Recent Technical Wins

The project has transitioned into a Customizable Multi-Asset Financial Suite, supporting digital assets, physical commodities, and manual entries within a unified, high-performance architecture.
The "Total Customization" Engine (Major Win)

    Dynamic Theme Center: Implemented a ThemeViewModel and centralized ThemePreferences. Users can now customize four distinct areas: Asset Card BG, Asset Card Text, Site BG, and Site Text.

    Tabbed Theme UI: The Color Picker has been overhauled into a 4-tab interface with a global "Apply" logic and a "Use Gradient" toggle.

    Manual Sort & Reorder: Integrated a high-fidelity drag-and-drop system in the Holdings list. Users can now manually rank their "Favorite" assets, with the order persisting in the database via a new displayOrder column.

The "Hilt" Foundation & Data Integrity

    Dagger Hilt Migration: Fully eliminated manual ViewModelFactories. The project now uses standardized Dependency Injection for all ViewModels and Repositories.

    Smart Upsert Logic: Refactored the AssetDao and Repository to use @Upsert. Adding an existing metal (e.g., Silver) now intelligently updates the quantity of the existing card rather than creating a duplicate.

    Shared ViewModel Strategy: All primary screens now observe the AssetViewModel as the single source of truth for portfolio data.

UI/UX Refinements

    The "Neon Thread" Sync: Replaced the "Charging Bar" with a modern, 2dp high LinearProgressIndicator at the top of the screen that pulses during CoinGecko/API refreshes.

    Restored Navigation: The "+" Floating Action Button (FAB) has been restored, and the "Manual Add" entry point has been prioritized at the top of the Asset Picker.

    Modern Animations: Implemented Modifier.animateItem() to provide smooth, sliding transitions when assets are reordered or filtered.

II. Architectural Standards & Roadmap
Core Standards

    Theme Consumption: All Composables must now pull colors from the ThemeViewModel state to ensure user customizations are respected site-wide.

    Stable Keys: Every LazyColumn item must use a stable key (e.g., key = { it.coinId }) to support reordering animations and prevent state flickering.

    DI Integrity: All new services must be registered in DatabaseModule.kt using the @Provides pattern to remain accessible via Hilt.

Current Work-in-Progress

    Compact Card Reconstruction: Rebuilding the CompactAssetCard to support the new dynamic theme variables while maintaining a dense, high-info layout.

    Manual Asset Expansion: Extending the manual entry logic to support Stocks and unique collectibles, allowing for a 100% comprehensive portfolio.

The Roadmap

    Portfolio Analytics: Implement the Donut Chart visualization to show the percentage split between Digital (Crypto) and Physical (Metals) based on real-time value.

    Live Metal Feeds: Replace "Local Mock" pricing with a dedicated Metals API (e.g., GoldAPI.io).

    Manual Sort Polish: Ensure the displayOrder logic handles large lists efficiently without collision.

III. Build & Safety Standards

    Experimental APIs: Use @file:OptIn(ExperimentalFoundationApi::class) for list animations and advanced UI gestures.

    Database Versioning: Currently at v4. fallbackToDestructiveMigration() is active; any schema changes will wipe local test data.

    Git Hygiene: Follow the "Checkpointed" workflow: Verify Build -> Sync Gradle -> Commit Success -> Push.

Michael, now that the "Master Doc" reflects this huge leap forward, would you like to perform the "Manual Sort" test? We can verify that moving a card actually saves that position to the database permanently.
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Tue 02/24/2026 15:55:44.45

Branch:
wip-hilt-fix
Commit:
44a8a15be47b913569e8e5fa5e98c8554d4f0441
Working tree status (git status --porcelain):
 M app/src/main/java/com/swanie/portfolio/data/ThemePreferences.kt
 M app/src/main/java/com/swanie/portfolio/data/local/AssetDao.kt
 M app/src/main/java/com/swanie/portfolio/data/repository/AssetRepository.kt
 M app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryViewModel.kt
 M app/src/main/java/com/swanie/portfolio/ui/holdings/AssetPickerScreen.kt
 M app/src/main/java/com/swanie/portfolio/ui/holdings/AssetViewModel.kt
 M app/src/main/java/com/swanie/portfolio/ui/holdings/MyHoldingsScreen.kt
 M app/src/main/java/com/swanie/portfolio/ui/navigation/NavGraph.kt
 M app/src/main/java/com/swanie/portfolio/ui/settings/SettingsScreen.kt
 M app/src/main/java/com/swanie/portfolio/ui/settings/SettingsViewModel.kt
 M app/src/main/java/com/swanie/portfolio/ui/settings/ThemeStudioScreen.kt
AM app/src/main/java/com/swanie/portfolio/ui/settings/ThemeViewModel.kt
 M docs/BROWSER_CONTEXT_NARRATIVE.md

--------------------------------------------------
KEY CONFIG FILES (paths)
--------------------------------------------------

settings.gradle.kts
build.gradle.kts
gradle.properties
gradle\wrapper\gradle-wrapper.properties
app\build.gradle.kts
app\src\main\AndroidManifest.xml

--------------------------------------------------
SOURCE FILE INDEX (Kotlin/Java paths)
--------------------------------------------------

app/src/main/java/com/swanie/portfolio/MainActivity.kt
app/src/main/java/com/swanie/portfolio/MainViewModel.kt
app/src/main/java/com/swanie/portfolio/PortfolioApplication.kt
app/src/main/java/com/swanie/portfolio/data/MetalsProvider.kt
app/src/main/java/com/swanie/portfolio/data/ThemePreferences.kt
app/src/main/java/com/swanie/portfolio/data/di/DatabaseModule.kt
app/src/main/java/com/swanie/portfolio/data/local/AppDatabase.kt
app/src/main/java/com/swanie/portfolio/data/local/AssetDao.kt
app/src/main/java/com/swanie/portfolio/data/local/AssetEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/Converters.kt
app/src/main/java/com/swanie/portfolio/data/network/CoinGeckoApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/CoinMarketResponse.kt
app/src/main/java/com/swanie/portfolio/data/network/RetrofitClient.kt
app/src/main/java/com/swanie/portfolio/data/repository/AssetRepository.kt
app/src/main/java/com/swanie/portfolio/ui/Type.kt
app/src/main/java/com/swanie/portfolio/ui/components/AlphaKeyboard.kt
app/src/main/java/com/swanie/portfolio/ui/components/BottomNavigationBar.kt
app/src/main/java/com/swanie/portfolio/ui/components/CustomToast.kt
app/src/main/java/com/swanie/portfolio/ui/features/CreateAccountScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/HomeScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/SettingsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AssetPickerScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AssetViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/ManualAssetEntryScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/MyHoldingsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/navigation/NavGraph.kt
app/src/main/java/com/swanie/portfolio/ui/navigation/Routes.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ColorPicker.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsViewModelFactory.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ThemeStudioScreen.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ThemeViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/theme/Color.kt
app/src/main/java/com/swanie/portfolio/ui/theme/Theme.kt

--------------------------------------------------
RESOURCES INDEX (res paths)
--------------------------------------------------

app/src/main/res/drawable
app/src/main/res/mipmap-anydpi-v26
app/src/main/res/mipmap-hdpi
app/src/main/res/mipmap-mdpi
app/src/main/res/mipmap-xhdpi
app/src/main/res/mipmap-xxhdpi
app/src/main/res/mipmap-xxxhdpi
app/src/main/res/values
app/src/main/res/xml
app/src/main/res/drawable/bg_navy_gradient.xml
app/src/main/res/drawable/ic_launcher_background.xml
app/src/main/res/drawable/ic_launcher_foreground.xml
app/src/main/res/drawable/swanie_foreground.png
app/src/main/res/drawable/swanie_splash.png
app/src/main/res/drawable/swan_launcher_icon.png
app/src/main/res/drawable/swan_splash_icon_wrapper.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
app/src/main/res/mipmap-hdpi/ic_launcher.webp
app/src/main/res/mipmap-hdpi/ic_launcher_round.webp
app/src/main/res/mipmap-mdpi/ic_launcher.webp
app/src/main/res/mipmap-mdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xhdpi/ic_launcher.webp
app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp
app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp
app/src/main/res/mipmap-xxxhdpi/ic_launcher_swanie.png
app/src/main/res/values/colors.xml
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
app/src/main/res/xml/backup_rules.xml
app/src/main/res/xml/data_extraction_rules.xml

--------------------------------------------------
BROWSER AI REMINDERS
--------------------------------------------------
- Follow the LEVEL 4 AI CONTROL HEADER above.
- If you need file contents, request: NEED FILE: path/to/file
- If this document is older than 24 hours, remind the user to rebuild it.
- Prefer minimal safe changes; avoid refactors unless asked.

===== END OF FILE =====
