@echo off
REM ==========================================================
REM rebuild_browser_context_dump.bat
REM
REM Refreshes ONLY the auto-generated appendix:
REM   - Truncates docs\BROWSER_CONTEXT_MASTER.md after ### END_NARRATIVE
REM     and appends a new AUTO-GENERATED block (git + path indexes).
REM   - Writes docs\BROWSER_CONTEXT_DUMP.md (short paste: pointers + same block).
REM
REM Run this after updating narrative / MASTER handoff so path lists stay current.
REM Full narrative source of truth: docs\BROWSER_CONTEXT_NARRATIVE.md
REM ==========================================================

cd /d "%~dp0.."

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Rebuild-BrowserContextPaths.ps1"
if errorlevel 1 (
  echo FAILED.
  pause
  exit /b 1
)

echo.
echo DONE.
echo - docs\BROWSER_CONTEXT_MASTER.md  (AUTO-GENERATED section refreshed^)
echo - docs\BROWSER_CONTEXT_DUMP.md    (short paste bundle^)
echo.
pause
