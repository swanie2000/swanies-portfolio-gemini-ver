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
UPDATED MASTER DOCUMENT: SOVEREIGN SHIELD (V8.5.0)
🎯 THE CORE MISSION

To maintain a high-performance, multi-instance portfolio tracker where Widgets and the Main App operate as independent, data-isolated entities ("Sovereign Shield").
🛡️ 1. ARCHITECTURAL RECAP (THE STABLE BASELINE)

    Database: V22 Schema (Room). Uses vaultId (Int) as the primary key for portfolio isolation.

    Widget Engine: Jetpack Glance 1.1.0.

    Identity Logic: Each GlanceId is strictly bound to a bound_vault_id in PreferencesGlanceStateDefinition.

    Navigation: Decoupled. The Main App's "Last Viewed" state must never influence what a Widget displays.

🚧 2. THE CURRENT BLOCKER: THE "IDENTITY HANDSHAKE"

We are currently fighting a Race Condition during Widget placement.

    The Symptom: New widgets (e.g., "Swanie 4") land showing the default name "PORTFOLIO" instead of the user-selected Vault Name.

    The Cause: The Widget renders its first frame before the bound_vault_id has finished writing to the persistent DataStore.

    The Planned Fix: Transition from a Pull model (Widget searching for its ID) to a Push model (Config Activity pushing ID/Name directly into AppWidgetManager options).

🛠️ 3. ACTIVE PHASE: 18 "POLISH & PROOFING"
Task	Description	Status
Direct Push Identity	Move Vault ID/Name into AppWidgetOptions for instant landing.	PRIORITY 1
"Initializing" State	UI feedback ("Securing Identity...") while the first sync runs.	PENDING
Swan Twinkle	Recover the logo shimmer logic from OpeningPage history.	ARCHAEOLOGY
Metals Audit	READ-ONLY VERIFICATION. Ensure V22 weight units render correctly.	AUDIT ONLY
⚠️ 4. DEVELOPER GUARDRAILS (FOR THE AGENT)

    CRITICAL: READ BEFORE EDITING

        No Scope Creep: Do not modify AssetRepository or Metals logic unless explicitly tasked. The core app logic is stable.

        Full File Outputs: Always provide full file contents for code changes to avoid partial snippet corruption.

        Logging: Use Log.d("WIDGET_FIX", ...) for all identity handshake troubleshooting.

        Git Discipline: If a solution requires touching more than 3 files, stop and ask for permission. Focus on surgical fixes.

🐞 KNOWN "GHOSTS"

    The "Blank Swanie 1": Currently appears in the Main App due to a temporary debug bypass of the Login/Password screen. DO NOT ATTEMPT TO FIX. This will resolve itself once the official login sequence is restored.
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Thu 04/09/2026 12:15:38.06

Branch:
main
Commit:
30666242b150d4697c23f745ce6507dec97f2c77
Working tree status (git status --porcelain):
 M .idea/misc.xml
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
app/src/main/java/com/swanie/portfolio/data/remote/GoogleDriveService.kt
app/src/main/java/com/swanie/portfolio/data/repository/AssetRepository.kt
app/src/main/java/com/swanie/portfolio/data/repository/DataSyncCoordinator.kt
app/src/main/java/com/swanie/portfolio/data/repository/MarketPriceData.kt
app/src/main/java/com/swanie/portfolio/ui/Type.kt
app/src/main/java/com/swanie/portfolio/ui/components/AlphaKeyboard.kt
app/src/main/java/com/swanie/portfolio/ui/components/BottomNavigationBar.kt
app/src/main/java/com/swanie/portfolio/ui/components/CustomToast.kt
app/src/main/java/com/swanie/portfolio/ui/entry/AssetArchitectScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/AuthViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/features/CreateAccountScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/HomeScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/RestoreVaultScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/TermsAndConditionsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/UnlockVaultScreen.kt
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
app/src/main/java/com/swanie/portfolio/ui/settings/PortfolioManagerScreen.kt
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
app/src/main/java/com/swanie/portfolio/widget/WidgetConfigActivity.kt
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
