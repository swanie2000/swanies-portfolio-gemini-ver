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
UPDATED MASTER DOCUMENT: SOVEREIGN SHIELD (V20: KINETIC REBIRTH)
🎯 THE CORE MISSION

To transition "Swanie’s Portfolio" into a commercial-grade financial vault. The mission prioritizes user-owned data (Zero-Liability), hardware-level biometrics, and a professional, "Cinematic" user experience.
🛡️ 1. ARCHITECTURAL RECAP (THE KINETIC RESTORATION)

    Visual Logic: Home Screen animations are 20% faster and perfectly synchronized. Coordinates are LOCKED.

    Front Door Signage: AndroidManifest.xml and SecurityManager.kt updated. The biometric prompt now correctly displays "Swanie's Portfolio" (fixed apostrophe/space).

    Clean Slate: All legacy Google/Firebase "ghost" credentials have been revoked and purged from the cloud.

📉 2. THE RECENT BATTLE REPORT (UI STABILIZATION)

    Status: Text formatting is verified fixed. Biometric icon scaling is currently a "System Ghost" (ignoring insets); decision made to bypass further icon troubleshooting until after the Cloud Migration to avoid data loss from uninstalls.

    Working Tree: Clean.

🚀 3. THE FUTURE PATH (THE BOUTIQUE UPGRADE)
Task	Description	Priority
Boutique Widget	Next Up: Increase Header size, add top-left Swan shortcut, and yellow Edit icon with precision touch targets.	IMMEDIATE
Silent Vault	Re-implement Google Drive sync using the App Data Folder API (User-Owned).	HIGH
UI Uniformity	Align 'Portfolio Manager', 'Theme Studio', and 'Widget Manager' headers.	MEDIUM
Nuclear Reset	Ensure Factory Reset clears all local data and the future cloud-stored database file.	MEDIUM
⚠️ 4. DEVELOPER GUARDRAILS (FOR THE NEXT AGENT)

    DATA PRESERVATION: Do not suggest uninstalls. The user is holding live data that must be migrated to the Cloud Vault first.

    WIDGET PRECISION: Only the Swan (top-left), Pencil (top-right), and Refresh icons should be reactive. Use fingertip-sized hitboxes.

    LOCK THE LOOK: No changes to Home Screen coordinates.

🚀 Next Agent Command

    "The narrative is updated to V20: KINETIC REBIRTH. The Front Door signage is fixed.

    Task 1: The Boutique Widget. Overhaul the PortfolioWidget.kt.

        Make the Portfolio name a large, bold header.

        Add a small Swan icon to the top-left (action: open app).

        Add a yellow Edit Pencil icon to the top-right.

        Interaction Lock: Create a specific 'finger-tip size' box for the Pencil and Swan. The rest of the widget body must be non-reactive to touch.

    Task 2: UI Consistency. Update the headers for PortfolioManagerScreen.kt, ThemeStudioScreen.kt, and WidgetManagerScreen.kt. Each needs the Swan icon above the title, matching the visual style of the main app.

    Provide full file outputs only. Confirm 'KEEP ALL' before proceeding."
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Wed 04/15/2026 10:13:10.09

Branch:
main
Commit:
ee631532b40f80d9d757908cc5942653d5aa0c06
Working tree status (git status --porcelain):
 M app/src/main/AndroidManifest.xml
 M app/src/main/java/com/swanie/portfolio/security/SecurityManager.kt
 M app/src/main/java/com/swanie/portfolio/ui/features/HomeScreen.kt
 M app/src/main/java/com/swanie/portfolio/ui/features/UnlockVaultScreen.kt
AM app/src/main/res/drawable/ic_vault_auth.xml
 M docs/BROWSER_CONTEXT_MASTER.md
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
app/src/main/java/com/swanie/portfolio/security/SecurityManager.kt
app/src/main/java/com/swanie/portfolio/ui/Type.kt
app/src/main/java/com/swanie/portfolio/ui/components/AlphaKeyboard.kt
app/src/main/java/com/swanie/portfolio/ui/components/BottomNavigationBar.kt
app/src/main/java/com/swanie/portfolio/ui/components/CustomToast.kt
app/src/main/java/com/swanie/portfolio/ui/entry/AssetArchitectScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/AuthViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/features/CreateAccountScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/HomeScreen.kt
app/src/main/java/com/swanie/portfolio/ui/features/LegacyHomeScreen.kt
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
app/src/main/java/com/swanie/portfolio/widget/WidgetClickCallback.kt
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
app/src/main/res/drawable/ic_vault_auth.xml
app/src/main/res/drawable/swanie_foreground.png
app/src/main/res/drawable/swanie_splash.png
app/src/main/res/drawable/swan_launcher_icon.png
app/src/main/res/drawable/swan_splash_icon_wrapper.xml
app/src/main/res/drawable/widget_background.xml
app/src/main/res/layout/glance_default_layout.xml
app/src/main/res/layout/widget_layout_fallback.xml
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
