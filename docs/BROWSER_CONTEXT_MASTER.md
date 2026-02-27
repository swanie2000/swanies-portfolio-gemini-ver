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
Markdown

# BROWSER_CONTEXT_NARRATIVE.md

## 1. Project Overview
**App Name:** Swanie's Portfolio  
**Purpose:** Crypto & Precious Metals tracking with a high-end, custom-themed UI.  
**Current Branch:** main (Merged and Locked)  
**Tech Stack:** Kotlin, Jetpack Compose, Hilt (Dependency Injection), Room, Retrofit, StateFlow.

## 2. Architectural Status (The "Hilt" Restoration)
**Status: COMPLETED** Today marked a major milestone: the successful migration from a failing manual ViewModel factory system to a clean, production-ready **Hilt-injected architecture**.

* **ThemeViewModel:** Acts as the single source of truth for UI colors, persisting hex strings and allowing real-time theme updates across all screens without "ghost instances."
* **AssetViewModel:** Fully integrated with Hilt to manage asset data, search logic, and database persistence.
* **Application Setup:** Hilt entry points and the single `PortfolioApplication` class are now correctly configured and stable.

## 3. Feature Map & UI Status

### 🟢 Completed & Locked
* **Hilt Integration:** The entire dependency tree is now stabilized.
* **Home Screen:** Splash-to-App transition preserved; dynamic "Radial Burst" logic implemented.
* **Theme Studio:** Full interactive control over background and text colors with "Reset to Default" logic.
* **Asset Picker:** "Ghost Swan" empty state implemented with keyboard-aware positioning.
* **Git Foundation:** The project is merged into `main` with a "Working Tree Clean" status on GitHub.

### 🟡 In Progress / Work-in-Progress (Current Focus)
* **Holdings Reorder Logic (DRAG & DROP):** * **Status:** Extremely volatile. Compiles and runs, but custom manual drag math is violently fighting Jetpack Compose's internal `LazyColumn` layout engine.
  * **The Problem:** We are experiencing 1-frame latency jitters, "glass ceiling" scroll fighting at the top index, and `animateItem` crossfade flickering. We have mitigated Z-index bleed-through using hardware layers (`shadowElevation`), but the core math is still unstable.
  * **🚨 CRITICAL DIRECTIVE FOR NEXT AI 🚨:** DO NOT ATTEMPT TO WRITE MORE CUSTOM PIXEL-MATH FOR THIS DRAG EVENT. The manual translation math over a `LazyColumn` is a failed architectural approach.
  * **NEXT STEPS:** The next AI must research how top-tier open-source libraries (specifically **`compose-reorderable`** by Calintz) handle this. You must either:
    1. Implement a true "God Listener" `pointerInput(Unit)` that relies strictly on `LazyListState` layout lookups for offset translation rather than manual drag delta accumulators.
    2. Suggest integrating a battle-tested third-party reorder library to handle the state desync and 1-frame latency issues natively.
    3. Suggest pivoting the UX to an "Edit Mode" with simple up/down arrows to completely bypass the GPU physics war.

### 🔴 Upcoming Features
* **Portfolio Analytics:** Donut chart visualization for Metals vs. Crypto breakdown.
* **Trash Zone Refinement:** Fine-tuning collision detection for the floating delete button during asset drags.
* **Price Refresh Polling:** Real-time background updates for asset values.
* **Manual Asset Entry:** UI exists; logic for custom price/weight inputs for non-API assets is pending final validation.

## 4. Key Logic Snippets (The Build-Saver & Render Fixes)
To stop the build-crash loop and fix rendering glitches, we learned that Jetpack Compose requires atomic state updates and strict modifier ordering to prevent UI flickering during rapid list swaps:

```kotlin
// 1. ATOMIC STATE UPDATES: Prevents the 2-step visual flash during a swap
var localHoldings by remember { mutableStateOf<List<AssetEntity>>(emptyList()) }

// 2. DISABLING CROSSFADES: Prevents cards from becoming "see-through" during animateItem swaps
animateItem(
    fadeInSpec = null,
    fadeOutSpec = null,
    // Snap active item to finger, Spring neighbor items out of the way
    placementSpec = if (isDraggingThisItem) snap() else spring(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )
)

// 3. HARDWARE LAYER LOCKING: Prevents bottom cards from Z-index bleeding through the dragged card
.graphicsLayer {
    scaleX = scale; scaleY = scale
    shadowElevation = if (isDraggingThisItem) 30f else 0f // Forces a dedicated GPU layer
}
### END_NARRATIVE

============================================================
AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
============================================================

Generated: Thu 02/26/2026 22:07:41.95

Branch:
main
Commit:
43c36c701b9bf74c0d169015be0c44de2550cb2d
Working tree status (git status --porcelain):
D  app/src/androidTest/java/com/example/swaniesportfolio/ExampleInstrumentedTest.kt
AM "app/src/androidTest/java/com/example/swaniesportfolio/test holding file.kt"
 M app/src/main/java/com/swanie/portfolio/ui/holdings/MyHoldingsScreen.kt
 M docs/BROWSER_CONTEXT_HEADER.txt
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
