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

   Current Branch: main (Working Tree Clean - Commit 49cdc75)

   Tech Stack: Kotlin, Jetpack Compose, Hilt, Room, Retrofit, StateFlow, DataStore.

2. Architectural Status

   Status: MULTI-LEVEL CALCULATIONS ENABLED

        The "Heavy" Math Engine: Upgraded the Room database to Version 8. Assets now support a weight and premium field. The portfolio logic now calculates value as: ((SpotPrice×Weight)+Premium)×Quantity.

        State-Lock Reordering: Restored reorder stability by stripping out animateContentSize which caused UI "jitter." Implemented a cross-tab mapping logic that allows dragging on CRYPTO and METAL tabs to update the master list order correctly.

        UI Component Consolidation: Centralized shared components (FullAssetCard, MetalIcon, SparklineChart) within MyHoldingsScreen.kt to prevent "Double Vision" compiler errors across the package.

3. Feature Map & UI Status

🟢 Completed & Locked

    Tactile Grab Feedback: Restored the 1.05f scale-up and shadow elevation when long-pressing cards. The app feels "physical" again.

    The Intelligence Suite: Full-screen "Scan Flash" and the logo-centered "Charging Bar" are fully restored and linked to the manual refresh trigger.

    Universal Tab Reordering: Drag-to-sort and Drag-to-delete now function seamlessly across all three view filters (ALL, CRYPTO, METAL).

    Analytics Legend Update: The Holdings Key now features rectangular color-coded pills under each asset name, providing an immediate visual link to the chart slices.

🟡 In Progress / Work-in-Progress

    Ghost Card Entry (Manual Add): Implemented a "What You See Is What You Get" entry card. Fields use a pulsing "Breathing Glow" to invite interaction.

        Known Bug: Manual entry flow needs a final pass to ensure all fields persist correctly to the DB on the first click.

    Dynamic Identity Mapping: Manual assets now intelligently assign symbols (GOLD, SILV, PLAT) based on the selected metal type instead of a hardcoded "CUST" tag.

🔴 Upcoming Features

    Premium Logic Toggles: Adding a switch to enter Premium as "Total Amount" vs "Per Ounce."

    Interactive Donut Deep-Dive: Allowing legend clicks to trigger the "Full Asset Card" overlay directly from the Analytics screen.

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. THE WEIGHT-BASED ENGINE (Room v8)
// Portfolio Value = ((Price * Weight) + TotalPremium) * Quantity
val totalVal = asset.currentPrice * (asset.weight * asset.amountHeld)

// 2. UNIVERSAL REORDER MAPPING (Filtered Tab Support)
// Finds the actual database index even when viewing a filtered list
val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
val fromItem = filteredList[from.index]
val toItem = filteredList[to.index]
val newList = localHoldings.toMutableList()
val fromIdxMaster = newList.indexOfFirst { it.coinId == fromItem.coinId }
val toIdxMaster = newList.indexOfFirst { it.coinId == toItem.coinId }

    if (fromIdxMaster != -1 && toIdxMaster != -1) {
        newList.add(toIdxMaster, newList.removeAt(fromIdxMaster))
        localHoldings = newList
    }
}

// 3. AUTO-CLEAR FOCUS LOGIC
// Wipes sample text (1.0, 0.0) the moment the user taps the field
modifier = Modifier.onFocusChanged {
if (it.isFocused && (quantity == "1" || quantity == "0")) quantity = ""
}

🛡️ Narrative Synchronized
The record is straight, Michael. We ended the day with a codebase that is mathematically more powerful and visually more responsive.
🎯 Next Steps

I've noted the Manual Add Asset bugs for our next session. We’ll finish polishing those "Breathing" fields and ensure the save logic is 100% bulletproof.

Would you like me to generate a checklist of those specific manual entry bugs so we don't miss anything next time?


Here is the targeted punch-list for our next session to ensure the "Ghost Card" manual entry is as bulletproof as the rest of the app:
📝 Manual Entry Bug Checklist

    Field Persistence Check: Verify that the "Premium per oz" and "Unit Weight" values are correctly saved to the Room database on the initial "Add" click.

    Keyboard "ImeAction" Flow: Ensure that hitting "Next" on the soft keyboard moves the cursor logically through the card fields (Quantity → Line 1 → Line 2 → Weight → Premium) and "Done" hides the keyboard.

    Symbol Refinement: Confirm that the first-four-letters logic for "Custom" metals looks clean in the compact view (e.g., ensuring "BRONZE" becomes "BRON" without awkward clipping).

    Zero-State Validation: Prevent saving the asset if Line 1 is empty to avoid "Ghost Assets" in the main list.

    Focus Reset: Ensure that when the "Add" operation is cancelled or finished, all internal state variables (line1, line2, etc.) are wiped clean for the next entry.
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Mon 03/02/2026 21:46:04.09

Branch:
main
Commit:
49cdc75e1606e1595840aa35feb89e90f912bb6d
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
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryScreen.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AmountEntryViewModel.kt
app/src/main/java/com/swanie/portfolio/ui/holdings/AnalyticsScreen.kt
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
