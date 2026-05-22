# Generate a beta unlock code for one tester email (same as website form + Android app).
# Requires Node.js. Secret from BETA_UNLOCK_SECRET env or repo local.properties.
param(
    [Parameter(Mandatory = $true)]
    [string] $Email,
    [string] $Expires = ""
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$nodeScript = Join-Path $scriptDir "beta-unlock-code.mjs"
if (-not (Test-Path $nodeScript)) {
    Write-Error "Missing $nodeScript"
}
$args = @($nodeScript, $Email)
if ($Expires) { $args += $Expires }
& node @args
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
