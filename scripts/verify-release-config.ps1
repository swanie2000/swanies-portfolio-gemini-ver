# Pre-build checks from local.properties (run before Signed Bundle / bundleRelease).
# Usage: .\scripts\verify-release-config.ps1
$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$propsPath = Join-Path $repoRoot "local.properties"
$gradlePath = Join-Path $repoRoot "app\build.gradle.kts"

function Read-LocalProperty {
    param([string]$Name)
    if (-not (Test-Path $propsPath)) { return "" }
    foreach ($line in Get-Content $propsPath) {
        $t = $line.Trim()
        if ($t.StartsWith("$Name=")) {
            return $t.Substring($Name.Length + 1).Trim()
        }
    }
    return ""
}

if (-not (Test-Path $propsPath)) {
    Write-Host "FAIL: local.properties not found." -ForegroundColor Red
    exit 1
}

$rcKey = Read-LocalProperty "REVENUECAT_PUBLIC_API_KEY"
$grantDays = Read-LocalProperty "CLOSED_TEST_PRO_GRANT_DAYS"
$versionCode = ""
$versionName = ""
if (Test-Path $gradlePath) {
    $gradle = Get-Content $gradlePath -Raw
    if ($gradle -match 'versionCode\s*=\s*(\d+)') { $versionCode = $Matches[1] }
    if ($gradle -match 'versionName\s*=\s*"([^"]+)"') { $versionName = $Matches[1] }
}

Write-Host "Release config check"
Write-Host "  Version (gradle): $versionName ($versionCode)"
if ([string]::IsNullOrWhiteSpace($grantDays)) {
    Write-Host "  CLOSED_TEST_PRO_GRANT_DAYS: (not set, Gradle default 30)"
} else {
    Write-Host "  CLOSED_TEST_PRO_GRANT_DAYS: $grantDays"
}

$failed = $false
if ([string]::IsNullOrWhiteSpace($rcKey)) {
    Write-Host "FAIL: REVENUECAT_PUBLIC_API_KEY missing in local.properties." -ForegroundColor Red
    $failed = $true
} elseif ($rcKey.StartsWith("test_")) {
    Write-Host "FAIL: REVENUECAT_PUBLIC_API_KEY must be goog_... for release, not test_..." -ForegroundColor Red
    $failed = $true
} elseif ($rcKey.Length -lt 20 -or $rcKey -match "paste") {
    Write-Host "FAIL: REVENUECAT_PUBLIC_API_KEY looks like a placeholder." -ForegroundColor Red
    $failed = $true
} else {
    Write-Host "OK: REVENUECAT_PUBLIC_API_KEY looks like a Play production key." -ForegroundColor Green
}

$ccKey = Read-LocalProperty "CRYPTOCOMPARE_API_KEY"
if ([string]::IsNullOrWhiteSpace($ccKey)) {
    Write-Host "WARN: CRYPTOCOMPARE_API_KEY missing — CryptoCompare search/prices will fail (401)." -ForegroundColor Yellow
} elseif ($ccKey.Length -lt 10) {
    Write-Host "WARN: CRYPTOCOMPARE_API_KEY looks too short." -ForegroundColor Yellow
} else {
    Write-Host "OK: CRYPTOCOMPARE_API_KEY is set." -ForegroundColor Green
}

if ($failed) {
    exit 1
}

Write-Host ""
Write-Host "Running Gradle validateRevenueCatReleaseKey..."
Push-Location $repoRoot
try {
    & .\gradlew.bat :app:validateRevenueCatReleaseKey --quiet
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally {
    Pop-Location
}

Write-Host "OK: Pre-build release config checks passed. Build Signed Bundle next." -ForegroundColor Green
exit 0
