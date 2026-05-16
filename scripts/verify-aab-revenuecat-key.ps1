# Scans a release .aab for RevenueCat SDK key strings (before Play upload).
# Usage:
#   .\scripts\verify-aab-revenuecat-key.ps1
#   .\scripts\verify-aab-revenuecat-key.ps1 -AabPath "C:\path\to\app-release.aab"
param(
    [string]$AabPath = ""
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not $AabPath) {
    $candidates = @(
        (Join-Path $repoRoot "app\release\app-release.aab"),
        (Join-Path $repoRoot "app\build\outputs\bundle\release\app-release.aab")
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { $AabPath = $c; break }
    }
}

if (-not (Test-Path $AabPath)) {
    Write-Error "AAB not found. Pass -AabPath to your Studio export (e.g. app\release\app-release.aab)."
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $AabPath))
$hasTest = $false
$hasGoog = $false
try {
    foreach ($entry in $zip.Entries) {
        if (-not $entry.Name.EndsWith(".dex")) { continue }
        $ms = New-Object System.IO.MemoryStream
        $stream = $entry.Open()
        $stream.CopyTo($ms)
        $stream.Close()
        $text = [System.Text.Encoding]::UTF8.GetString($ms.ToArray())
        if ($text.Contains("test_dz")) { $hasTest = $true }
        if ($text.Contains("goog_")) { $hasGoog = $true }
    }
} finally {
    $zip.Dispose()
}

Write-Host "AAB: $((Resolve-Path $AabPath).Path)"
if ($hasTest) {
    Write-Host "FAIL: sandbox RevenueCat key (test_dz) found — do not upload." -ForegroundColor Red
    Write-Host "Fix: Sync Gradle, Clean Project, rebuild Signed App Bundle (release) with REVENUECAT_PUBLIC_API_KEY=goog_ in local.properties."
    exit 1
}
if (-not $hasGoog) {
    Write-Host "FAIL: production key (goog_) not found in bundle." -ForegroundColor Red
    exit 1
}
Write-Host "OK: production RevenueCat key (goog_), no test_dz — safe to upload to Play." -ForegroundColor Green
exit 0
