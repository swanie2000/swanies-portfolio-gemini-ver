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
📄 BROWSER_CONTEXT_NARRATIVE.md
1. Project Overview

   App Name: Swanie's Portfolio

   Purpose: Crypto & Precious Metals tracking with a high-end, custom-themed UI.

   Current Branch: main (Working Tree Clean - Commit b977b0d)

   Tech Stack: Kotlin, Jetpack Compose, Hilt, Room, Retrofit, StateFlow, DataStore.

2. Architectural Status

   Status: TAB-AWARE TOTALS & PRECISION FORMATTING ENABLED

        Segmented Portfolio Totals: The main header total now dynamically switches based on the active tab (ALL, CRYPTO, or METAL), providing category-specific valuations.

        Isolated Decimal Precision: Implemented a split formatting logic where the Unit Price respects user-defined slider precision (essential for low-satoshi crypto), while Total Value and Portfolio Totals are hard-locked to standard 2-decimal currency ($#,##0.00).

        Integrated Entry & Edit Flow: The 5-step funnel handles all new entries, while the refined FullAssetCard handles edits, both feeding into the unit-aware math engine.

3. Feature Map & UI Status
   🟢 Completed & Locked

   Segmented Header Logic: Switching tabs now instantly updates the top portfolio value to reflect only the assets in view.

   Currency Standardization: Fixed the "Too Many Decimals" bug in the balance displays. Your net worth now always displays as clean, standard currency.

   Edit Overlay Logic: Swapped the field order for better flow—Quantity/Weight (Logical variables) are now on top, while Name/Precision (Descriptive variables) are on the bottom.

   Unit-Aware Math Engine: Fully handles KILO, GRAM, and DWT conversions relative to troy ounce spot prices.

🟡 In Progress / Work-in-Progress

    Edit Screen Layout (Keyboard Obstruction): In the current edit overlay, the software keyboard covers the "Save/Cancel" buttons on smaller screens or when many fields are present.

    Focus Management: Ensuring the Quantity field in the Edit Overlay auto-clears and focuses as reliably as it does in the Add Funnel.

🔴 Upcoming Features

    IME Inset Padding: Implementing Modifier.imePadding() or WindowInsets.ime to ensure the Edit Overlay slides up when the keyboard is active.

    Interactive Donut Deep-Dive: Legend clicks on the Analytics screen triggering the asset detail overlay.

    Premium Logic Toggles: Adding a switch to enter Premium as "Total Amount" vs "Per Ounce."

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. ISOLATED PRECISION FORMATTING
// Price uses slider pref; Total Value uses hard-coded 2 decimals
Text("PRICE: " + formatCurrency(asset.currentPrice, asset.decimalPreference))
Text("TOTAL: " + formatCurrency(calculatedTotal, 2))

// 2. DYNAMIC TAB-BASED TOTALS
val totalValueFormatted by remember(holdings, selectedTab) {
derivedStateOf {
val filtered = when (selectedTab) {
1 -> holdings.filter { it.category == AssetCategory.CRYPTO }
2 -> holdings.filter { it.category == AssetCategory.METAL }
else -> holdings
}
val sum = filtered.sumOf { it.currentPrice * getUnitMultiplier(it) * it.weight * it.amountHeld }
formatCurrency(sum, 2)
}
}

// 3. VISUAL HIERARCHY (EDIT MODE)
// Dimming non-editable elements to highlight the Yellow input fields
val activeTint = if(isEditing) Color.Yellow else cardText
val backgroundAlpha = if(isEditing) 0.3f else 1.0f

🛡️ Narrative Synchronized
The portfolio now behaves like a professional financial tool—precise where it needs to be (unit price) and standard where it counts (account balance).

🎯 Next Steps (Session Kickoff)

    Keyboard Fix: Apply Inset handling to the MyHoldingsScreen edit overlay so the "Save" button is always reachable.

    State Persistence: Verify that the "Weight" field updates correctly when editing existing assets vs. new funnel entries.
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Tue 03/03/2026 15:55:07.88

Branch:
main
Commit:
b977b0d76c326d22bbc9497d31afad63ad895138
Working tree status (git status --porcelain):
 M app/src/main/java/com/swanie/portfolio/ui/holdings/MyHoldingsScreen.kt
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
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AnalyticsScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AssetPickerScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AssetViewModel.kt
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
