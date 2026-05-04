# Refreshes the AUTO-GENERATED tail (git + path indexes) in BROWSER_CONTEXT_MASTER.md
# and writes BROWSER_CONTEXT_DUMP.md (short paste: pointers + same auto block).
# Safe: does not replace HEADER or narrative; only content after ### END_NARRATIVE in MASTER.
$ErrorActionPreference = 'Stop'
$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location $RepoRoot

$masterPath = Join-Path $RepoRoot 'docs\BROWSER_CONTEXT_MASTER.md'
$dumpPath = Join-Path $RepoRoot 'docs\BROWSER_CONTEXT_DUMP.md'

$utf8 = New-Object System.Text.UTF8Encoding $false
$master = [System.IO.File]::ReadAllText($masterPath)
$needle = '### END_NARRATIVE'
$idx = $master.IndexOf($needle, [StringComparison]::Ordinal)
if ($idx -lt 0) { throw "docs/BROWSER_CONTEXT_MASTER.md is missing ### END_NARRATIVE" }

$head = $master.Substring(0, $idx + $needle.Length).TrimEnd()

function Add-Lines([System.Text.StringBuilder]$b, [string[]]$lines) {
    foreach ($line in $lines) { [void]$b.AppendLine($line) }
}

$sb = [System.Text.StringBuilder]::new()
[void]$sb.AppendLine()
Add-Lines $sb @(
    '============================================================'
    'AUTO-GENERATED DAILY SECTION (REBUILT EVERY RUN)'
    '============================================================'
    ''
    ('Generated: {0}' -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'))
    ''
    'Branch:'
    (git branch --show-current 2>$null).Trim()
    'Commit:'
    (git rev-parse HEAD 2>$null).Trim()
    'Working tree status (git status --porcelain):'
)
$gsLines = @(git status --porcelain 2>$null)
if ($gsLines.Count -eq 0 -or [string]::IsNullOrWhiteSpace(($gsLines -join ''))) {
    [void]$sb.AppendLine('(clean)')
} else {
    foreach ($line in $gsLines) { [void]$sb.AppendLine($line.TrimEnd("`r")) }
}
[void]$sb.AppendLine()

Add-Lines $sb @(
    '--------------------------------------------------'
    'KEY CONFIG FILES (paths)'
    '--------------------------------------------------'
    ''
)
$config = @(
    'settings.gradle.kts', 'build.gradle.kts', 'gradle.properties',
    'gradle/wrapper/gradle-wrapper.properties', 'app/build.gradle.kts',
    'app/src/main/AndroidManifest.xml'
)
foreach ($f in $config) {
    if (Test-Path (Join-Path $RepoRoot $f)) { [void]$sb.AppendLine($f.Replace('\', '/')) }
}
[void]$sb.AppendLine()

Add-Lines $sb @(
    '--------------------------------------------------'
    'WEBSITE / GITHUB PAGES (paths)'
    '--------------------------------------------------'
    ''
)
$webPaths = @('website', '.github/workflows/deploy-website.yml')
foreach ($wp in $webPaths) {
    if (Test-Path (Join-Path $RepoRoot $wp)) { [void]$sb.AppendLine($wp.Replace('\', '/')) }
}
Get-ChildItem -Path (Join-Path $RepoRoot 'website') -Recurse -File -ErrorAction SilentlyContinue |
    Sort-Object FullName | ForEach-Object {
        $rel = $_.FullName.Substring($RepoRoot.Length).TrimStart('\', '/').Replace('\', '/')
        [void]$sb.AppendLine($rel)
    }
[void]$sb.AppendLine()

Add-Lines $sb @(
    '--------------------------------------------------'
    'SOURCE FILE INDEX (Kotlin/Java paths)'
    '--------------------------------------------------'
    ''
)
$javaRoot = Join-Path $RepoRoot 'app\src\main\java'
if (Test-Path $javaRoot) {
    Get-ChildItem -Path $javaRoot -Recurse -File -Include *.kt, *.java |
        Sort-Object FullName | ForEach-Object {
            $rel = $_.FullName.Substring($RepoRoot.Length).TrimStart('\', '/').Replace('\', '/')
            [void]$sb.AppendLine($rel)
        }
}
[void]$sb.AppendLine()

Add-Lines $sb @(
    '--------------------------------------------------'
    'RESOURCES INDEX (res paths)'
    '--------------------------------------------------'
    ''
)
$resRoot = Join-Path $RepoRoot 'app\src\main\res'
if (Test-Path $resRoot) {
    Get-ChildItem -Path $resRoot -Recurse -File |
        Sort-Object FullName | ForEach-Object {
            $rel = $_.FullName.Substring($RepoRoot.Length).TrimStart('\', '/').Replace('\', '/')
            [void]$sb.AppendLine($rel)
        }
}
[void]$sb.AppendLine()

Add-Lines $sb @(
    '--------------------------------------------------'
    'BROWSER AI REMINDERS'
    '--------------------------------------------------'
    '- Follow the LEVEL 4 AI CONTROL HEADER in docs/BROWSER_CONTEXT_MASTER.md when pasting the full doc.'
    '- Canonical product handoff: docs/BROWSER_CONTEXT_NARRATIVE.md -> AI AGENT HANDOFF (READ FIRST).'
    '- If you need file contents, request: NEED FILE: path/to/file'
    '- Prefer minimal safe changes; avoid refactors unless asked.'
    ''
)

$autoBlock = $sb.ToString().TrimEnd() + "`r`n"
$newMaster = $head + "`r`n`r`n" + $autoBlock
[System.IO.File]::WriteAllText($masterPath, $newMaster, $utf8)

$dumpIntro = @'
# BROWSER_CONTEXT_DUMP (auto-generated, short paste)

**Canonical product state:** `docs/BROWSER_CONTEXT_NARRATIVE.md` -> **AI AGENT HANDOFF (READ FIRST)**.

**Process rules + narrative excerpt:** paste `docs/BROWSER_CONTEXT_MASTER.md` (through **### END_NARRATIVE** if you want to trim size).

**Below:** git fingerprint + path indexes only (regenerated by `docs/Rebuild-BrowserContextPaths.ps1` via `docs/rebuild_browser_context_dump.bat`).

---

'@
[System.IO.File]::WriteAllText($dumpPath, $dumpIntro + $autoBlock, $utf8)

Write-Host "OK: updated AUTO-GENERATED section in BROWSER_CONTEXT_MASTER.md and wrote BROWSER_CONTEXT_DUMP.md"
