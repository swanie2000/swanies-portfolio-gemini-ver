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

The project has evolved from a Crypto-only tracker into a Multi-Asset Financial Suite. It now supports both digital assets and physical commodities (Precious Metals) within a unified, modern architecture.
The Hilt Migration (Major Milestone)

    Architectural Overhaul: Successfully transitioned from fragile manual ViewModelFactories to Dagger Hilt Dependency Injection.

    Shared ViewModel Strategy: Consolidated fragmented UI logic into a single AssetViewModel. All screens (Home, Search, Manual Entry) now observe a single source of truth.

    Structural Cleanup: Neutralized and deleted all obsolete factory files. The "working tree" is now clean and follows official Android development standards.

    Build Stability: Resolved critical runtime crashes by correcting the Gradle plugin order (KSP before Hilt), ensuring the dependency graph is generated correctly.

The Hybrid Data Engine

    MetalsProvider Integration: Implemented a dedicated provider for physical assets that works alongside the AssetRepository.

    Global Discovery: The search engine now returns both coins and bullion in a single list, differentiated by custom UI badges and icons.

    Enum-Driven Logic: Migrated categorization from raw Strings to a robust AssetCategory Enum (CRYPTO vs. METAL) to prevent logic leaks and compilation errors.

Premium UI/UX Transitions

    High-Energy Entrance: Perfected the Splash-to-Home transition. The "Radial Burst" (1000ms) and "Swan Glide" (900ms) now overlap with a 120ms delay for a snappy, premium feel.

    Adaptive Rendering: Metals display color-coded circular vectors (Gold/Silver/Platinum), while Crypto utilizes remote-fetched thumbnails.

II. Architectural Standards & Roadmap
Core Standards

    Single Source of Truth: All UI screens must observe the AssetViewModel to ensure data consistency across the app.

    Injection Rules: All new ViewModels must be annotated with @HiltViewModel. Manual factory creation is strictly prohibited.

    Inset Management: Every full-screen Composable must implement statusBarsPadding() to maintain a clean, edge-to-edge look that respects the system UI.

Current Work-in-Progress

    Tab Filtering Logic: Refining the filteredHoldings logic in MyHoldingsScreen to ensure assets stay strictly within their respective categories (Crypto vs. Metal).

    Metals Pricing: Currently utilizing "Local Mock" pricing (e.g., Gold at $2000) while the live Commodity API bridge is under construction.

The Roadmap

    Portfolio Analytics: Implement a Donut Chart visualization to show the percentage split between physical and digital holdings.

    Live Metal Feeds: Integrate a dedicated Metals API (e.g., GoldAPI.io) to replace placeholder spot prices.

    Persistence Audit: Verify the "Save" function in the shared ViewModel correctly triggers Room updates across all observing screens.

III. Build & Safety Standards

    Database Versioning: Currently at v4. fallbackToDestructiveMigration() is active; schema changes will wipe local test data to maintain integrity.

    Dependency Management: Any new network service must be registered in DatabaseModule.kt using the @Provides pattern to remain accessible via Hilt.

    Git Hygiene: Follow the "Checkpointed" workflow: Verify Build -> Clean Obsolete Files -> Commit -> Push.

Would you like me to help you verify that the "Save" button in the ManualAssetEntryScreen is properly updating the database and refreshing the main list?
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Tue 02/24/2026 12:47:32.30

Branch:
wip-hilt-fix
Commit:
cacfaa53f624c75a9054c2b83097860811ee5e08
Working tree status (git status --porcelain):
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
