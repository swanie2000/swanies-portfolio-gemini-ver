@echo off
cls
setlocal EnableDelayedExpansion

REM ==========================================================
REM rebuild_browser_context_dump.bat
REM MASTER GENERATED FROM SOURCE FILES (BULLETPROOF)
REM
REM MASTER OUTPUT:
REM   - docs\BROWSER_CONTEXT_MASTER.md
REM
REM INPUT SOURCES:
REM   - docs\BROWSER_CONTEXT_HEADER.txt
REM   - docs\BROWSER_CONTEXT_NARRATIVE.md
REM
REM No narrative is ever read from MASTER.
REM ==========================================================

cd /d "%~dp0.."

echo.
echo Rebuilding BROWSER_CONTEXT_MASTER.md (BULLETPROOF)...
echo.

if not exist docs mkdir docs

if not exist docs\BROWSER_CONTEXT_HEADER.txt (
  echo ERROR: docs\BROWSER_CONTEXT_HEADER.txt not found.
  pause
  exit /b 1
)

if not exist docs\BROWSER_CONTEXT_NARRATIVE.md (
  echo ERROR: docs\BROWSER_CONTEXT_NARRATIVE.md not found.
  echo Fix: create it with your narrative sections.
  pause
  exit /b 1
)

set "MASTER=docs\BROWSER_CONTEXT_MASTER.md"
set "TMP=%TEMP%\sw_master_tmp_%RANDOM%%RANDOM%.md"
set "ROOT=%CD%\"

if exist "%TMP%" del /q "%TMP%" >nul 2>&1

REM 1) Header
copy /Y docs\BROWSER_CONTEXT_HEADER.txt "%TMP%" >nul

REM 2) Narrative (always from the narrative source file)
>>"%TMP%" echo.
>>"%TMP%" echo ============================================================
>>"%TMP%" echo NARRATIVE SECTION (SOURCE FILE - EDIT docs/BROWSER_CONTEXT_NARRATIVE.md)
>>"%TMP%" echo ============================================================
>>"%TMP%" echo ### BEGIN_NARRATIVE
type docs\BROWSER_CONTEXT_NARRATIVE.md >>"%TMP%"
>>"%TMP%" echo.
>>"%TMP%" echo ### END_NARRATIVE
>>"%TMP%" echo.

REM 3) Auto-generated daily section
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

>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo KEY CONFIG FILES (paths)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo.
if exist settings.gradle.kts >>"%TMP%" echo settings.gradle.kts
if exist build.gradle.kts    >>"%TMP%" echo build.gradle.kts
if exist gradle.properties   >>"%TMP%" echo gradle.properties
if exist gradle\wrapper\gradle-wrapper.properties >>"%TMP%" echo gradle\wrapper\gradle-wrapper.properties
if exist app\build.gradle.kts >>"%TMP%" echo app\build.gradle.kts
if exist app\src\main\AndroidManifest.xml >>"%TMP%" echo app\src\main\AndroidManifest.xml
>>"%TMP%" echo.

>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo SOURCE FILE INDEX (Kotlin/Java paths)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo.
if exist app\src\main\java (
  for /f "delims=" %%F in ('dir /s /b app\src\main\java ^| findstr /i "\.kt$ \.java$"') do (
    set "FULL=%%F"
    set "REL=!FULL:%ROOT%=!"
    >>"%TMP%" echo !REL:\=/!
  )
)
>>"%TMP%" echo.

>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo RESOURCES INDEX (res paths)
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo.
if exist app\src\main\res (
  for /f "delims=" %%R in ('dir /s /b app\src\main\res') do (
    set "FULL=%%R"
    set "REL=!FULL:%ROOT%=!"
    >>"%TMP%" echo !REL:\=/!
  )
)
>>"%TMP%" echo.

>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo BROWSER AI REMINDERS
>>"%TMP%" echo --------------------------------------------------
>>"%TMP%" echo - Follow the LEVEL 4 AI CONTROL HEADER above.
>>"%TMP%" echo - If you need file contents, request: NEED FILE: path/to/file
>>"%TMP%" echo - If this document is older than 24 hours, remind the user to rebuild it.
>>"%TMP%" echo - Prefer minimal safe changes; avoid refactors unless asked.
>>"%TMP%" echo.
>>"%TMP%" echo ===== END OF FILE =====

copy /Y "%TMP%" "%MASTER%" >nul
if exist "%TMP%" del /q "%TMP%" >nul 2>&1

echo.
echo DONE.
echo Paste this into AI chats: docs\BROWSER_CONTEXT_MASTER.md
echo.
pause

REM ===== END OF FILE =====