@echo off
cls
setlocal EnableDelayedExpansion

REM ==========================================================
REM rebuild_browser_context_dump.bat
REM ONE-FILE SYSTEM (MASTER ONLY)
REM
REM Creates/Updates:
REM   - docs\BROWSER_CONTEXT_MASTER.md   (PASTE THIS INTO AI CHATS)
REM
REM Uses:
REM   - docs\BROWSER_CONTEXT_HEADER.txt  (Level-4 rules template)
REM
REM Preserves:
REM   - Narrative block between:
REM       ### BEGIN_NARRATIVE
REM       ### END_NARRATIVE
REM ==========================================================

REM Move to project root (one level up from docs folder)
cd /d "%~dp0.."

echo.
echo Rebuilding BROWSER_CONTEXT_MASTER.md (MASTER ONLY)...
echo.

REM Ensure docs folder exists
if not exist docs mkdir docs

REM Verify header exists
if not exist docs\BROWSER_CONTEXT_HEADER.txt (
echo ERROR: docs\BROWSER_CONTEXT_HEADER.txt not found.
echo Fix: create docs\BROWSER_CONTEXT_HEADER.txt with your Level 4 header text.
pause
exit /b 1
)

set "MASTER=docs\BROWSER_CONTEXT_MASTER.md"
set "TMP=docs\__master_tmp.md"
set "NARR=docs\__narrative_tmp.md"

set "BEGIN_MARK=### BEGIN_NARRATIVE"
set "END_MARK=### END_NARRATIVE"

REM ----------------------------------------------------------
REM Extract existing narrative block (if MASTER exists)
REM ----------------------------------------------------------
if exist "%NARR%" del /q "%NARR%" >nul 2>&1

set "foundNarr=0"
set "inNarr=0"

if exist "%MASTER%" (
for /f "usebackq delims=" %%L in ("%MASTER%") do (
set "line=%%L"

        if "!line!"=="%BEGIN_MARK%" (
            set "inNarr=1"
            set "foundNarr=1"
        ) else if "!line!"=="%END_MARK%" (
            set "inNarr=0"
        ) else (
            if "!inNarr!"=="1" (
                >>"%NARR%" echo %%L
            )
        )
    )
)

REM ----------------------------------------------------------
REM If no narrative exists yet, create a clean starter narrative
REM ----------------------------------------------------------
if "%foundNarr%"=="0" (
>"%NARR%" echo PROJECT OVERVIEW
>>"%NARR%" echo - (fill in)
>>"%NARR%" echo.
>>"%NARR%" echo CURRENT APP FLOW
>>"%NARR%" echo - (fill in)
>>"%NARR%" echo.
>>"%NARR%" echo KEY FILE INDEX (high signal files)
>>"%NARR%" echo - (fill in)
>>"%NARR%" echo.
>>"%NARR%" echo KNOWN PROBLEMS / RISKS
>>"%NARR%" echo - (fill in)
>>"%NARR%" echo.
>>"%NARR%" echo CURRENT FEATURE STATUS
>>"%NARR%" echo - (fill in)
)

REM ----------------------------------------------------------
REM Build MASTER file into TMP, then replace MASTER
REM ----------------------------------------------------------
if exist "%TMP%" del /q "%TMP%" >nul 2>&1

REM 1) Header (rules)
copy /Y docs\BROWSER_CONTEXT_HEADER.txt "%TMP%" >nul

REM 2) Narrative block (preserved across rebuilds)
>>"%TMP%" echo.
>>"%TMP%" echo ============================================================
>>"%TMP%" echo NARRATIVE SECTION (PRESERVED - EDIT BETWEEN MARKERS)
>>"%TMP%" echo ============================================================
>>"%TMP%" echo %BEGIN_MARK%
type "%NARR%" >>"%TMP%"
>>"%TMP%" echo %END_MARK%
>>"%TMP%" echo.

REM 3) Auto-generated daily section (rebuilt every run)
>>"%TMP%" echo ============================================================
>>"%TMP%" echo AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)
>>"%TMP%" echo ============================================================
>>"%TMP%" echo.
>>"%TMP%" echo Generated: %date% %time%
>>"%TMP%" echo.

>>"%TMP%" echo Branch:
git branch --show-current >>"%TMP%"
>>"%TMP%" echo Commit:
git rev-parse HEAD >>"%TMP%"
>>"%TMP%" echo Working tree status (git status --porcelain):
git status --porcelain >>"%TMP%"
>>"%TMP%" echo.

REM Key config files list (paths only)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo KEY CONFIG FILES (paths)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo.
if exist settings.gradle.kts >>"%TMP%" echo settings.gradle.kts
if exist settings.gradle     >>"%TMP%" echo settings.gradle
if exist build.gradle.kts    >>"%TMP%" echo build.gradle.kts
if exist build.gradle        >>"%TMP%" echo build.gradle
if exist gradle.properties   >>"%TMP%" echo gradle.properties
if exist gradle\wrapper\gradle-wrapper.properties >>"%TMP%" echo gradle\wrapper\gradle-wrapper.properties
if exist app\build.gradle.kts >>"%TMP%" echo app\build.gradle.kts
if exist app\build.gradle     >>"%TMP%" echo app\build.gradle
if exist app\src\main\AndroidManifest.xml >>"%TMP%" echo app\src\main\AndroidManifest.xml
>>"%TMP%" echo.

REM Source index (paths only)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo SOURCE FILE INDEX (Kotlin/Java paths)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo.
if exist app\src\main\java (
dir app\src\main\java /s /b | findstr /i "\.kt$ \.java$" >>"%TMP%"
)
if exist app\src\main\kotlin (
dir app\src\main\kotlin /s /b | findstr /i "\.kt$ \.java$" >>"%TMP%"
)
>>"%TMP%" echo.

REM Resources index (paths only)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo RESOURCES INDEX (res paths)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo.
if exist app\src\main\res (
dir app\src\main\res /s /b >>"%TMP%"
)
>>"%TMP%" echo.

REM Browser AI reminders
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo BROWSER AI REMINDERS
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo - Follow the LEVEL 4 AI CONTROL HEADER above.
>>"%TMP%" echo - If you need file contents, request: NEED FILE: path/to/file
>>"%TMP%" echo - If this document is older than 24 hours, remind the user to rebuild it.
>>"%TMP%" echo - Prefer minimal safe changes; avoid refactors unless asked.
>>"%TMP%" echo.

>>"%TMP%" echo ===== END OF FILE =====

REM Swap in the new MASTER
copy /Y "%TMP%" "%MASTER%" >nul

REM Cleanup temp files ALWAYS
if exist "%TMP%" del /q "%TMP%" >nul 2>&1
if exist "%NARR%" del /q "%NARR%" >nul 2>&1

echo.
echo DONE.
echo Paste this into AI chats: docs\BROWSER_CONTEXT_MASTER.md
echo.
pause

REM ===== END OF FILE =====