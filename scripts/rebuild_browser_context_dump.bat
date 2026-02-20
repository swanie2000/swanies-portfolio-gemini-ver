@echo off
cls

REM ==========================================================
REM rebuild_browser_context_dump.bat
REM Creates:
REM   1) docs\BROWSER_CONTEXT_DUMP.md       (SHORT, paste-friendly)
REM   2) docs\BROWSER_CONTEXT_DUMP_FULL.md  (FULL, reference)
REM ==========================================================

REM Move to project root (one level up from scripts folder)
cd /d "%~dp0.."

echo.
echo Rebuilding browser context dumps...
echo.

REM Ensure docs folder exists
if not exist docs mkdir docs

REM Verify header exists
if not exist docs\BROWSER_CONTEXT_HEADER.txt (
    echo ERROR: docs\BROWSER_CONTEXT_HEADER.txt not found.
    pause
    exit /b
)

set SHORT=docs\BROWSER_CONTEXT_DUMP.md
set FULL=docs\BROWSER_CONTEXT_DUMP_FULL.md

REM ----------------------------
REM Build SHORT dump
REM ----------------------------
copy /Y docs\BROWSER_CONTEXT_HEADER.txt "%SHORT%" >nul

echo.>> "%SHORT%"
echo ============================================================>> "%SHORT%"
echo AUTO-GENERATED DAILY SECTION (SHORT)>> "%SHORT%"
echo ============================================================>> "%SHORT%"
echo.>> "%SHORT%"

echo Generated: %date% %time%>> "%SHORT%"
echo.>> "%SHORT%"

echo Branch:>> "%SHORT%"
git branch --show-current >> "%SHORT%"
echo Commit:>> "%SHORT%"
git rev-parse HEAD >> "%SHORT%"
echo Working tree status (git status --porcelain):>> "%SHORT%"
git status --porcelain >> "%SHORT%"
echo.>> "%SHORT%"

echo -------------------------------------------------->> "%SHORT%"
echo KEY PATHS (high signal)>> "%SHORT%"
echo -------------------------------------------------->> "%SHORT%"
echo.>> "%SHORT%"

if exist settings.gradle.kts echo settings.gradle.kts>> "%SHORT%"
if exist settings.gradle echo settings.gradle>> "%SHORT%"
if exist build.gradle.kts echo build.gradle.kts>> "%SHORT%"
if exist build.gradle echo build.gradle>> "%SHORT%"
if exist gradle.properties echo gradle.properties>> "%SHORT%"
if exist gradle\wrapper\gradle-wrapper.properties echo gradle\wrapper\gradle-wrapper.properties>> "%SHORT%"
if exist app\build.gradle.kts echo app\build.gradle.kts>> "%SHORT%"
if exist app\build.gradle echo app\build.gradle>> "%SHORT%"
if exist app\src\main\AndroidManifest.xml echo app\src\main\AndroidManifest.xml>> "%SHORT%"

echo.>> "%SHORT%"

echo -------------------------------------------------->> "%SHORT%"
echo SOURCE FILE INDEX (Kotlin/Java paths)>> "%SHORT%"
echo -------------------------------------------------->> "%SHORT%"
echo.>> "%SHORT%"

if exist app\src\main\java (
  dir app\src\main\java /s /b | findstr /i "\.kt$ \.java$" >> "%SHORT%"
)
if exist app\src\main\kotlin (
  dir app\src\main\kotlin /s /b | findstr /i "\.kt$ \.java$" >> "%SHORT%"
)

echo.>> "%SHORT%"

echo -------------------------------------------------->> "%SHORT%"
echo RESOURCES INDEX (res paths)>> "%SHORT%"
echo -------------------------------------------------->> "%SHORT%"
echo.>> "%SHORT%"

if exist app\src\main\res (
  dir app\src\main\res /s /b >> "%SHORT%"
)

echo.>> "%SHORT%"
echo -------------------------------------------------->> "%SHORT%"
echo BROWSER AI INSTRUCTIONS>> "%SHORT%"
echo -------------------------------------------------->> "%SHORT%"
echo - If you need file contents, request: NEED FILE: path/to/file>> "%SHORT%"
echo - If this dump is older than 24 hours, remind user to rebuild it.>> "%SHORT%"
echo - Prefer minimal safe changes; avoid refactors unless asked.>> "%SHORT%"
echo.>> "%SHORT%"

echo ===== END OF FILE =====>> "%SHORT%"

REM ----------------------------
REM Build FULL dump
REM ----------------------------
copy /Y docs\BROWSER_CONTEXT_HEADER.txt "%FULL%" >nul

echo.>> "%FULL%"
echo ============================================================>> "%FULL%"
echo AUTO-GENERATED DAILY SECTION (FULL)>> "%FULL%"
echo ============================================================>> "%FULL%"
echo.>> "%FULL%"

echo Generated: %date% %time%>> "%FULL%"
echo Branch:>> "%FULL%"
git branch --show-current >> "%FULL%"
echo Commit:>> "%FULL%"
git rev-parse HEAD >> "%FULL%"
echo Working tree status (git status --porcelain):>> "%FULL%"
git status --porcelain >> "%FULL%"
echo.>> "%FULL%"

echo -------------------------------------------------->> "%FULL%"
echo FULL PROJECT FILE LIST (app/)>> "%FULL%"
echo -------------------------------------------------->> "%FULL%"
echo.>> "%FULL%"

if exist app (
  dir app /s /b >> "%FULL%"
)

echo.>> "%FULL%"
echo ===== END OF FILE =====>> "%FULL%"

echo.
echo DONE.
echo - SHORT: %SHORT%
echo - FULL : %FULL%
echo.
pause

REM ===== END OF FILE =====