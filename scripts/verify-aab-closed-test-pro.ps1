# Verifies release AAB auto-Pro settings match local.properties (before Play upload).
# Usage:
#   .\scripts\verify-aab-closed-test-pro.ps1
#   .\scripts\verify-aab-closed-test-pro.ps1 -AabPath "C:\path\to\app-release.aab"
param(
    [string]$AabPath = ""
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$propsPath = Join-Path $repoRoot "local.properties"
$buildConfigPath = Join-Path $repoRoot "app\build\generated\source\buildConfig\release\com\swanie\portfolio\BuildConfig.java"

function Read-LocalProperty {
    param([string]$Name, [string]$Default = "")
    if (-not (Test-Path $propsPath)) { return $Default }
    foreach ($line in Get-Content $propsPath) {
        $t = $line.Trim()
        if ($t.StartsWith("$Name=")) {
            return $t.Substring($Name.Length + 1).Trim()
        }
    }
    return $Default
}

function Read-BuildConfigLong {
    param([string]$FieldName)
    if (-not (Test-Path $buildConfigPath)) {
        return $null
    }
    $content = Get-Content $buildConfigPath -Raw
    if ($content -match "$FieldName\s*=\s*(\d+)L") {
        return [long]$Matches[1]
    }
    return $null
}

function Read-BuildConfigInt {
    param([string]$FieldName)
    if (-not (Test-Path $buildConfigPath)) {
        return $null
    }
    $content = Get-Content $buildConfigPath -Raw
    if ($content -match "$FieldName\s*=\s*(\d+)") {
        return [int]$Matches[1]
    }
    return $null
}

function Read-BuildConfigString {
    param([string]$FieldName)
    if (-not (Test-Path $buildConfigPath)) {
        return $null
    }
    $content = Get-Content $buildConfigPath -Raw
    if ($content -match "$FieldName\s*=\s*""([^""]*)""") {
        return $Matches[1]
    }
    return $null
}

if (-not $AabPath) {
    $candidates = @(
        (Join-Path $repoRoot "app\release\app-release.aab"),
        (Join-Path $repoRoot "app\build\outputs\bundle\release\app-release.aab")
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) {
            $AabPath = $c
            break
        }
    }
}

if (-not $AabPath -or -not (Test-Path $AabPath)) {
    Write-Error "AAB not found. Build a signed release bundle first, or pass -AabPath."
}

if (-not (Test-Path $buildConfigPath)) {
    Write-Error "Release BuildConfig not found at $buildConfigPath. Run bundleRelease (or Android Studio Signed Bundle) first."
}

$grantDaysRaw = Read-LocalProperty "CLOSED_TEST_PRO_GRANT_DAYS" "30"
$grantDays = 30
if ($grantDaysRaw -match '^\d+$') {
    $grantDays = [int]$grantDaysRaw
}

$explicitUntil = Read-LocalProperty "CLOSED_TEST_PRO_UNTIL_EPOCH_MS" ""
$untilMs = Read-BuildConfigLong "CLOSED_TEST_PRO_UNTIL_EPOCH_MS"
$versionCode = Read-BuildConfigInt "VERSION_CODE"
$versionName = Read-BuildConfigString "VERSION_NAME"
$nowMs = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$dayMs = 86400000L

Write-Host "AAB: $((Resolve-Path $AabPath).Path)"
Write-Host "Build: $versionName ($versionCode)"
Write-Host "local.properties CLOSED_TEST_PRO_GRANT_DAYS=$grantDays"

if ($null -eq $untilMs) {
    Write-Host "FAIL: Could not read CLOSED_TEST_PRO_UNTIL_EPOCH_MS from BuildConfig.java." -ForegroundColor Red
    exit 1
}

if ($grantDays -le 0) {
    if ($untilMs -ne 0L) {
        Write-Host "FAIL: Grant days is 0 but AAB has CLOSED_TEST_PRO_UNTIL_EPOCH_MS=$untilMs (auto-Pro still enabled)." -ForegroundColor Red
        Write-Host "Fix: set CLOSED_TEST_PRO_GRANT_DAYS=0, Sync Gradle, Clean, rebuild Signed Bundle."
        exit 1
    }
    Write-Host "OK: Auto-Pro disabled (until epoch = 0)." -ForegroundColor Green
    exit 0
}

if ($untilMs -le 0L) {
    Write-Host "FAIL: Grant days is $grantDays but AAB has auto-Pro disabled (until = 0)." -ForegroundColor Red
    Write-Host "Fix: set CLOSED_TEST_PRO_GRANT_DAYS=$grantDays, Sync Gradle, rebuild Signed Bundle."
    exit 1
}

if ($untilMs -le $nowMs) {
    $expired = [DateTimeOffset]::FromUnixTimeMilliseconds($untilMs).ToLocalTime().ToString("yyyy-MM-dd HH:mm")
    Write-Host "FAIL: Auto-Pro expiry is in the past ($expired)." -ForegroundColor Red
    exit 1
}

$maxUntil = $nowMs + ([long]$grantDays * $dayMs) + 600000L
if ($untilMs -gt $maxUntil) {
    Write-Host "FAIL: Auto-Pro expiry is more than $grantDays days out (until=$untilMs)." -ForegroundColor Red
    exit 1
}

$expiresLocal = [DateTimeOffset]::FromUnixTimeMilliseconds($untilMs).ToLocalTime().ToString("yyyy-MM-dd HH:mm")
Write-Host "OK: Auto-Pro active until $expiresLocal (epoch $untilMs)." -ForegroundColor Green
if (-not [string]::IsNullOrWhiteSpace($explicitUntil)) {
    Write-Host "Note: CLOSED_TEST_PRO_UNTIL_EPOCH_MS override is set in local.properties."
}
exit 0
