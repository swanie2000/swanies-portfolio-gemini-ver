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
📝 UPDATED NARRATIVE: THE "ZERO-KNOWLEDGE" BREAKTHROUGH (V7.5.0)

Current Version: 7.5.0 (The "Sovereign Vault" Edition)

Build Status: 🟢 ULTRA-STABLE (Theme-Hardened & Global-Ready)

    Note: We have redefined the relationship between the developer and the user. By moving to a Client-Side Google Drive model, we have eliminated the need for a central database. The user is now the sole custodian of their data, and the app is the high-performance interface that unlocks it.

🛡️ 1. THE ARCHITECTURAL PIVOT: THE "SOVEREIGN VAULT" PROTOCOL

This session transformed the "front door" of the app. We moved away from a traditional sign-up model to a decentralized, encrypted sync strategy.

🚀 Today’s Engineering Wins:

    The Privacy Pledge (Zero-Knowledge): Established the legal and technical "Manifesto." The app now explicitly communicates that the creator has NO access to user data. All portfolio files are AES-256 encrypted and stored in a hidden "App Data" folder on the user’s own Google Drive.

    High-Fidelity Account Funnel: Built a premium, animated entry screen featuring the Floating Gold Shield and the Swan Logo. This screen serves as the "Universal Header" blueprint for all future top-level pages.

    Universal Header Alignment: Standardized the UI with the Language Globe (Top-Left) and the Security Shield (Top-Right), creating a symmetrical, professional layout that mirrors the "Elite" specs of the Holdings screen.

    Theme-Responsive "Brackets": Eliminated all hardcoded colors in the entry funnel. Input field outlines (Brackets) and buttons now dynamically glow with the user's selected siteTextColor, making the "Create Account" experience feel native to their custom theme.

    Keyboard-Aware UX: Implemented a "Deep Scroll" protocol using imePadding and bottom spacers. This ensures that even on long forms, the keyboard never obscures critical inputs or the "Connect" button.

    Security Safeguards: Added dynamic Password Criteria Chips (8+ Chars, Number, Symbol) and a Recovery Hint field. This ensures users create strong keys while providing a "Safety Net" that survives device swaps without compromising encryption.

🛡️ 2. THE "ELITE" SPECS (V7.5.0)
Component	Status	Tech Stack / Logic
Data Model	🟢 SOVEREIGN	User-Owned Google Drive / Hidden App Folder Sync
Security	🟢 ENCRYPTED	AES-256 Client-Side / Zero-Knowledge Developer Access
UI Aesthetics	🟢 DYNAMIC	Theme-Aware Outlines / Animated Shield Scale-In
Nav Logic	🟢 CLEAN	Named Parameter Safeguards / Terms & Conditions Route
Recovery	🟢 SURVIVABLE	Google 2FA + Vault Password + Password Hint
🛡️ 3. THE PATH FORWARD: THE "DRIVE BRIDGE" (PHASE 5)

With the entry funnel and privacy terms locked in, we move to the technical "Plumbing" that makes the vault real.

🛠️ Refinement & Expansion:

    The Google Drive Bridge: Integrating the Google Drive REST API to initialize the hidden app folder and handle the first "Handshake" between the app and the user's cloud.

    Market Watch "Bug-Hunt": Fixing the "MyHoldings" refresh button and the "Tap to Edit" logic that broke during the recent layout refactors.

    Global Expansion: Populating the "Full Page" searchable lists for the 6 core languages and 6 core currencies (including the XRP-Base option).

    The "Security Auditor" Hub: Developing the "Gold Shield" pop-up to show real-time encryption status and data-source verification.

🔄 Git Hygiene Protocol:

    V7.5.0 Baseline: This is the new "Privacy Gold." The "Connect Google Vault" flow is now the mandatory entry point. All future features must be built to sync with the hidden Drive folder logic.
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Wed 04/01/2026 16:26:27.35

Branch:
main
Commit:
d30acbbbf9f642050cc74c78eeb40429285c8e5d
Working tree status (git status --porcelain):
 M app/src/main/java/com/swanie/portfolio/ui/features/CreateAccountScreen.kt
AM app/src/main/java/com/swanie/portfolio/ui/features/TermsAndConditionsScreen.kt
 M app/src/main/java/com/swanie/portfolio/ui/navigation/NavGraph.kt
 M app/src/main/java/com/swanie/portfolio/ui/navigation/Routes.kt
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
app/src/main/java/com/swanie/portfolio/ui/entry/AssetArchitectScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/CreateAccountScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/HomeScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/TermsAndConditionsScreen.kt
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
