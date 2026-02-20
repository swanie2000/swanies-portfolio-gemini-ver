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

1) DO NOT assume missing files.
2) If code detail is required, request:

   NEED FILE: path/to/file

3) Prefer minimal, safe edits.
4) Avoid large refactors unless explicitly asked.
5) Explain reasoning BEFORE suggesting code changes.

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

Your default action is to REDUCE risk, not increase capability.

You should:
- detect risky ideas
- suggest smallest safe change
- prefer stability over cleverness
- protect project structure

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

============================================================
END CONTROL HEADER
============================================================
============================================================
AUTO-GENERATED DAILY SECTION (SHORT)
============================================================

Generated: Thu 02/19/2026 17:14:32.63

Branch:
main
Commit:
66ab1bb25cb81e51ec7aa7ce778884a0ed5fcf72
Working tree status (git status --porcelain):
 M .idea/misc.xml
AM docs/BROWSER_CONTEXT_DUMP.md
A  docs/BROWSER_CONTEXT_HEADER.txt
AM scripts/rebuild_browser_context_dump.bat

--------------------------------------------------
KEY PATHS (high signal)
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

C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\MainActivity.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\data\local\AppDatabase.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\data\local\AssetDao.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\data\local\AssetEntity.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\data\network\CoinGeckoApiService.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\data\network\RetrofitClient.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\data\repository\AssetRepository.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\Color.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\Theme.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\Type.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\components\AlphaKeyboard.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\components\BottomNavigationBar.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\features\HomeScreen.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\features\SettingsScreen.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\holdings\AmountEntryScreen.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\holdings\AmountEntryViewModel.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\holdings\AmountEntryViewModelFactory.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\holdings\AssetPickerScreen.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\holdings\AssetViewModel.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\holdings\AssetViewModelFactory.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\holdings\MyHoldingsScreen.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\holdings\MyHoldingsViewModel.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\navigation\NavGraph.kt
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\java\com\swanie\portfolio\ui\navigation\Routes.kt

--------------------------------------------------
RESOURCES INDEX (res paths)
--------------------------------------------------

C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\drawable
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-anydpi-v26
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-hdpi
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-mdpi
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xhdpi
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xxhdpi
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xxxhdpi
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\values
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\drawable\bg_navy_gradient.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\drawable\ic_launcher_background.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\drawable\ic_launcher_foreground.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\drawable\swanie_foreground.png
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\drawable\swanie_splash.png
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-anydpi-v26\ic_launcher.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-anydpi-v26\ic_launcher_round.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-hdpi\ic_launcher.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-hdpi\ic_launcher_round.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-mdpi\ic_launcher.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-mdpi\ic_launcher_round.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xhdpi\ic_launcher.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xhdpi\ic_launcher_round.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xxhdpi\ic_launcher.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xxhdpi\ic_launcher_round.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xxxhdpi\ic_launcher.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xxxhdpi\ic_launcher_round.webp
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\mipmap-xxxhdpi\ic_launcher_swanie.png
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\values\colors.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\values\strings.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\values\themes.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\xml\backup_rules.xml
C:\Users\MichaelSwanson\AndroidStudioProjects\SwaniesPortfolio\app\src\main\res\xml\data_extraction_rules.xml

--------------------------------------------------
BROWSER AI INSTRUCTIONS
--------------------------------------------------
- If you need file contents, request: NEED FILE: path/to/file
- If this dump is older than 24 hours, remind user to rebuild it.
- Prefer minimal safe changes; avoid refactors unless asked.

===== END OF FILE =====
