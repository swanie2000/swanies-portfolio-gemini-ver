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

USER WORKING STYLE (IMPORTANT)
------------------------------------------------------------

- Provide FULL FILE outputs when code changes are needed.
- Avoid partial snippets unless requested.
- Give step-by-step instructions.
- Reduce complexity; avoid large rewrites.


============================================================
END CONTROL HEADER
============================================================
============================================================
NARRATIVE SECTION (SOURCE FILE - EDIT docs/BROWSER_CONTEXT_NARRATIVE.md)
============================================================
### BEGIN_NARRATIVE
NARRATIVE: THE "GOLDEN VAULT" STABILIZATION (V7.2.4)

Current Version: 7.2.4 (The "Parity Achieved" Edition)

Build Status: 🟢 ULTRA-STABLE (Database V19 Active)

    Note: We have successfully moved beyond the "Sync Gap." The app and the home screen widget are now 1:1 in visual logic, color accuracy, and data persistence. The "Silver Ghost" and "Generic G" bugs are officially resolved.

1. THE V19 REVOLUTION: DATA & VISUAL PARITY

This session transformed the app from a "guessing" engine into a "fact-based" vault. We hardened the database schema to ensure the UI never has to guess what it is drawing.

🚀 Today’s Engineering Wins:

    V19 Schema Hardening: Implemented physicalForm (Bar/Coin/Round) and weightUnit (GRAM, KILO, OZ) directly into the AssetEntity.

    The "Chrome & Gold" Sync: Fixed the widget tint logic. Gold assets now render as Gold (#FFD700) and Silver as Silver (#C0C0C0) on the home screen.

    Shape Consistency: The widget now respects the database—Bars render as Rectangles (4dp), and Coins/Rounds render as Circles (16dp).

    Precision Stamping: Replaced the "Generic G" with high-contrast black stamps ("1k", "1g", "1/10") that pull directly from the V19 unit data.

    Build Stabilization: Resolved critical "Unresolved Reference" errors by restoring the FORCE_UPDATE_KEY and fixing the SparklineDrawUtils package path.

    Memory Firewall: Verified RGB_565 sparkline optimization is active in the widget, protecting the 2MB Binder limit.

💎 Current UX State:

    App: 🟢 Precision Vault. (Correct shapes, correct stamps, correct names).

    Widget: 🟢 Mirror Image. (1:1 visual parity with the main app).

2. THE "GOLDEN" SPECS (V7.2.4)
   Component	Status	Tech Stack / Logic
   Data Layer	🟢 STABLE	V19 Unit-Driven displayName logic
   Database	🟢 V19	Explicit physicalForm & weightUnit fields
   Widget UI	🟢 SYNCED	Dynamic Tinting (Gold/Silver) & Shape Logic
   Sparklines	🟢 OPTIMIZED	RGB_565 Memory-safe bitmaps
   Build State	🟢 GREEN	All constants restored; zero compilation errors
3. THE PATH FORWARD: GLOBAL VISTA (PHASE 4)

With the foundation now logically and visually sound, we move toward asset management and global settings.

🛠️ Refinement & Expansion:

    Vault-Aware Headers: Ensure the widget header dynamically toggles between "METALS" and "CRYPTO" based on the currentVaultId.

    Instant Sync: Implement a WorkManager trigger to update the widget the moment an asset is added, rather than waiting for the system broadcast.

    Global Vista Icons: Begin integrating the localIconPath logic to allow custom user icons to persist across migrations.

🔄 Git Hygiene Protocol:

    V7.2.4 Baseline: Commit and push immediately. This is the new "Golden Build" for all future development.
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Fri 03/27/2026 17:18:15.76

Branch:
main
Commit:
385294bd3d3034128582a834e0805d393ade4099
Working tree status (git status --porcelain):
 M app/src/main/java/com/swanie/portfolio/data/local/AppDatabase.kt
 M app/src/main/java/com/swanie/portfolio/data/local/AssetEntity.kt
 M app/src/main/java/com/swanie/portfolio/data/repository/AssetRepository.kt
 M app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryScreen.kt
 M app/src/main/java/com/swanie/portfolio/ui/holdings/HoldingsUIComponents.kt
 M app/src/main/java/com/swanie/portfolio/widget/PortfolioWidget.kt
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
app/src/main/java/com/swanie/portfolio/data/ThemePreferences.kt
app/src/main/java/com/swanie/portfolio/data/api/SearchEngineRegistry.kt
app/src/main/java/com/swanie/portfolio/data/api/SearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/BinanceSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/CoinbaseSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/CoinGeckoSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/CryptoCompareSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/KuCoinSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/MetalSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/MexcSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/api/impl/WeexSearchProvider.kt
app/src/main/java/com/swanie/portfolio/data/di/DatabaseModule.kt
app/src/main/java/com/swanie/portfolio/data/di/NetworkModule.kt
app/src/main/java/com/swanie/portfolio/data/local/AppDatabase.kt
app/src/main/java/com/swanie/portfolio/data/local/AssetDao.kt
app/src/main/java/com/swanie/portfolio/data/local/AssetEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/Converters.kt
app/src/main/java/com/swanie/portfolio/data/local/IconManager.kt
app/src/main/java/com/swanie/portfolio/data/local/PortfolioEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/PriceHistoryDao.kt
app/src/main/java/com/swanie/portfolio/data/local/PriceHistoryEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/SystemLogEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/TransactionDao.kt
app/src/main/java/com/swanie/portfolio/data/local/TransactionEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/UserConfigDao.kt
app/src/main/java/com/swanie/portfolio/data/local/UserConfigEntity.kt
app/src/main/java/com/swanie/portfolio/data/local/VaultDao.kt
app/src/main/java/com/swanie/portfolio/data/local/VaultEntity.kt
app/src/main/java/com/swanie/portfolio/data/network/BinanceApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/CoinbaseApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/CoinGeckoApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/CoinMarketResponse.kt
app/src/main/java/com/swanie/portfolio/data/network/CryptoCompareApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/KuCoinApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/MexcApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/RetrofitClient.kt
app/src/main/java/com/swanie/portfolio/data/network/WeexApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/YahooFinanceApiService.kt
app/src/main/java/com/swanie/portfolio/data/network/YahooFinanceResponse.kt
app/src/main/java/com/swanie/portfolio/data/repository/AssetRepository.kt
app/src/main/java/com/swanie/portfolio/data/repository/DataSyncCoordinator.kt
app/src/main/java/com/swanie/portfolio/data/repository/MarketPriceData.kt
app/src/main/java/com/swanie/portfolio/ui/Type.kt
app/src/main/java/com/swanie/portfolio/ui/components/AlphaKeyboard.kt
app/src/main/java/com/swanie/portfolio/ui/components/BottomNavigationBar.kt
app/src/main/java/com/swanie/portfolio/ui/components/CustomToast.kt
app/src/main/java/com/swanie/portfolio/ui/features/CreateAccountScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/HomeScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AnalyticsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AssetPickerScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AssetViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/HoldingsUIComponents.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/MyHoldingsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/metals/MetalsAuditScreen.kt
app/src/main/java/com/swanie/portfolio/ui/navigation/NavGraph.kt
app/src/main/java/com/swanie/portfolio/ui/navigation/Routes.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ColorPicker.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/settings/SettingsViewModelFactory.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ThemeStudioScreen.kt
app/src/main/java/com/swanie/portfolio/ui/settings/ThemeViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/settings/WidgetManagerScreen.kt
app/src/main/java/com/swanie/portfolio/ui/theme/Color.kt
app/src/main/java/com/swanie/portfolio/ui/theme/Theme.kt
app/src/main/java/com/swanie/portfolio/ui/theme/ThemeDefaults.kt
app/src/main/java/com/swanie/portfolio/widget/PortfolioWidget.kt
app/src/main/java/com/swanie/portfolio/widget/PortfolioWidgetReceiver.kt
app/src/main/java/com/swanie/portfolio/widget/SparklineDrawUtils.kt
app/src/main/java/com/swanie/portfolio/widget/WidgetSyncWorker.kt

--------------------------------------------------
RESOURCES INDEX (res paths)
--------------------------------------------------

app/src/main/res/drawable
app/src/main/res/layout
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
app/src/main/res/drawable/widget_background.xml
app/src/main/res/layout/glance_default_layout.xml
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
app/src/main/res/xml/portfolio_widget_info.xml

--------------------------------------------------
BROWSER AI REMINDERS
--------------------------------------------------
- Follow the LEVEL 4 AI CONTROL HEADER above.
- If you need file contents, request: NEED FILE: path/to/file
- If this document is older than 24 hours, remind the user to rebuild it.
- Prefer minimal safe changes; avoid refactors unless asked.

===== END OF FILE =====
