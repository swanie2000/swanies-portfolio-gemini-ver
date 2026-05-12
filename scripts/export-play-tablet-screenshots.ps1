<#
.SYNOPSIS
  Letterbox website/screenshots into Play Console tablet portrait sizes (9:16).

.DESCRIPTION
  Reads JPEGs from website/images/01_sp_*.jpg … 12_sp_*.jpg and writes:
    website/images/play_tablet_7inch/   — 1080 x 1920 (9:16)
    website/images/play_tablet_10inch/  — 1440 x 2560 (9:16)
  Same UI content in both; only canvas resolution differs (two Play upload slots).
  Letterboxing uses app navy #000416 to match launcher / site.

.PARAMETER RepoRoot
  Path to repo root (folder containing website/).
#>
param(
    [string] $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$srcDir = Join-Path $RepoRoot "website\images"
$out7 = Join-Path $srcDir "play_tablet_7inch"
$out10 = Join-Path $srcDir "play_tablet_10inch"
foreach ($d in @($out7, $out10)) {
    if (-not (Test-Path $d)) { New-Item -ItemType Directory -Path $d | Out-Null }
}

$navy = [System.Drawing.Color]::FromArgb(255, 0x00, 0x04, 0x16)

function Save-Jpeg([System.Drawing.Bitmap]$bmp, [string]$path, [int]$quality) {
    $codec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() | Where-Object { $_.MimeType -eq "image/jpeg" }
    $ep = New-Object System.Drawing.Imaging.EncoderParameters(1)
    $ep.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter(
        [System.Drawing.Imaging.Encoder]::Quality, [long]$quality)
    $bmp.Save($path, $codec, $ep)
}

function Export-Letterboxed([string]$srcPath, [string]$dstPath, [int]$cw, [int]$ch, [System.Drawing.Color]$bg, [int]$jpegQuality) {
    $src = [System.Drawing.Image]::FromFile($srcPath)
    try {
        $canvas = New-Object System.Drawing.Bitmap($cw, $ch)
        $g = [System.Drawing.Graphics]::FromImage($canvas)
        try {
            $g.Clear($bg)
            $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality

            $sw = [double]$src.Width
            $sh = [double]$src.Height
            $scale = [Math]::Min($cw / $sw, $ch / $sh)
            $tw = [int][Math]::Round($sw * $scale)
            $th = [int][Math]::Round($sh * $scale)
            $x = [int](($cw - $tw) / 2)
            $y = [int](($ch - $th) / 2)
            $rect = New-Object System.Drawing.Rectangle($x, $y, $tw, $th)
            $g.DrawImage($src, $rect)
        }
        finally { $g.Dispose() }
        Save-Jpeg $canvas $dstPath $jpegQuality
        $canvas.Dispose()
    }
    finally { $src.Dispose() }
}

$files = Get-ChildItem (Join-Path $srcDir "*_sp_*.jpg") | Sort-Object Name
if (-not $files) { throw "No source screenshots in $srcDir" }

foreach ($f in $files) {
    $name = $f.Name
    Write-Host "Processing $name"
    Export-Letterboxed $f.FullName (Join-Path $out7 $name) 1080 1920 $navy 92
    Export-Letterboxed $f.FullName (Join-Path $out10 $name) 1440 2560 $navy 92
}

Write-Host "Done. 7-inch -> $out7 ; 10-inch -> $out10"
