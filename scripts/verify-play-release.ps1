# Run all Play upload checks on a release AAB (after Signed Bundle build).
# Usage:
#   .\scripts\verify-play-release.ps1
#   .\scripts\verify-play-release.ps1 -AabPath "C:\path\to\app-release.aab"
param(
    [string]$AabPath = ""
)

$ErrorActionPreference = "Stop"
$scriptDir = $PSScriptRoot
$args = @()
if ($AabPath) {
    $args += "-AabPath"
    $args += $AabPath
}

Write-Host "=== 1/2 RevenueCat key (AAB scan) ===" -ForegroundColor Cyan
& (Join-Path $scriptDir "verify-aab-revenuecat-key.ps1") @args
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "=== 2/2 Closed-test auto-Pro (BuildConfig vs local.properties) ===" -ForegroundColor Cyan
& (Join-Path $scriptDir "verify-aab-closed-test-pro.ps1") @args
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "All checks passed - OK to upload this AAB to Play Console." -ForegroundColor Green
exit 0
