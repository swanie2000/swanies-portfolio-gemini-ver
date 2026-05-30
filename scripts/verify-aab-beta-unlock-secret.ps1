# Verifies the release AAB embeds BETA_UNLOCK_SECRET from local.properties (beta codes won't work without it).
# Usage:
#   .\scripts\verify-aab-beta-unlock-secret.ps1
#   .\scripts\verify-aab-beta-unlock-secret.ps1 -AabPath "C:\full\path\to\app-release.aab"
param(
    [string]$AabPath = ""
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$propsPath = Join-Path $repoRoot "local.properties"

if (-not (Test-Path $propsPath)) {
    Write-Error "local.properties not found. Set BETA_UNLOCK_SECRET before building the Play bundle."
}

$secret = ""
foreach ($line in Get-Content $propsPath) {
    $t = $line.Trim()
    if ($t.StartsWith("BETA_UNLOCK_SECRET=")) {
        $secret = $t.Substring("BETA_UNLOCK_SECRET=".Length).Trim()
        break
    }
}

if ([string]::IsNullOrWhiteSpace($secret)) {
    Write-Host "FAIL: BETA_UNLOCK_SECRET missing in local.properties - beta unlock codes will never validate in Play builds." -ForegroundColor Red
    exit 1
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
    Write-Error "AAB not found. Pass -AabPath to your signed app-release.aab (not the placeholder path)."
}

$needle = $secret.Substring(0, [Math]::Min(16, $secret.Length))
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $AabPath))
$found = $false
try {
    foreach ($entry in $zip.Entries) {
        if (-not $entry.Name.EndsWith(".dex")) { continue }
        $ms = New-Object System.IO.MemoryStream
        $stream = $entry.Open()
        try { $stream.CopyTo($ms) } finally { $stream.Close() }
        $text = [System.Text.Encoding]::UTF8.GetString($ms.ToArray())
        if ($text.Contains($needle)) {
            $found = $true
            break
        }
    }
} finally {
    $zip.Dispose()
}

Write-Host "AAB: $((Resolve-Path $AabPath).Path)"
if (-not $found) {
    Write-Host "FAIL: BETA_UNLOCK_SECRET from local.properties is NOT in this AAB." -ForegroundColor Red
    Write-Host "Codes from generate-beta-unlock-code.ps1 will show 'not valid' on this build."
    Write-Host "Fix: set BETA_UNLOCK_SECRET in local.properties, Sync Gradle, Clean, rebuild Signed Bundle."
    exit 1
}

Write-Host "OK: BETA_UNLOCK_SECRET is embedded - beta unlock codes from this PC should validate." -ForegroundColor Green
exit 0
