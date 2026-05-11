# Replaces transparent / near-black background with solid #000416 (launcher_navy).
# Usage: .\scripts\replace-icon-background.ps1 -InputPath "in.png" -OutputPath "out.png"

param(
    [Parameter(Mandatory = $true)][string] $InputPath,
    [Parameter(Mandatory = $true)][string] $OutputPath,
    [int] $RgbThreshold = 18
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$typeDef = @'
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;

public static class IconBgReplace {
  const int AlphaCutoff = 10;

  public static void Run(string inputPath, string outputPath, byte nr, byte ng, byte nb, int rgbThresh) {
    using (var bmp = new Bitmap(inputPath)) {
      int w = bmp.Width;
      int h = bmp.Height;
      Color newBg = Color.FromArgb(255, nr, ng, nb);
      bool[,] vis = new bool[w, h];
      var q = new Queue<Point>();
      int[] dx = new int[] { -1, 1, 0, 0 };
      int[] dy = new int[] { 0, 0, -1, 1 };

      Point[] corners = new Point[] {
        new Point(0, 0),
        new Point(w - 1, 0),
        new Point(0, h - 1),
        new Point(w - 1, h - 1)
      };

      foreach (Point s in corners) {
        if (s.X < 0 || s.Y < 0 || s.X >= w || s.Y >= h) continue;
        if (vis[s.X, s.Y]) continue;
        Color sc = bmp.GetPixel(s.X, s.Y);
        if (!IsBackgroundPixel(sc, rgbThresh)) continue;
        vis[s.X, s.Y] = true;
        bmp.SetPixel(s.X, s.Y, newBg);
        q.Enqueue(s);
      }

      while (q.Count > 0) {
        Point p = q.Dequeue();
        for (int k = 0; k < 4; k++) {
          int nx = p.X + dx[k];
          int ny = p.Y + dy[k];
          if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
          if (vis[nx, ny]) continue;
          Color c = bmp.GetPixel(nx, ny);
          if (!IsBackgroundPixel(c, rgbThresh)) continue;
          vis[nx, ny] = true;
          bmp.SetPixel(nx, ny, newBg);
          q.Enqueue(new Point(nx, ny));
        }
      }

      bmp.Save(outputPath, ImageFormat.Png);
    }
  }

  static bool IsBackgroundPixel(Color c, int t) {
    if (c.A < AlphaCutoff) return true;
    if (c.R > t || c.G > t || c.B > t) return false;
    return true;
  }
}
'@

Add-Type -TypeDefinition $typeDef -ReferencedAssemblies System.Drawing

if (-not (Test-Path -LiteralPath $InputPath)) { throw "Input not found: $InputPath" }
$dir = Split-Path -Parent $OutputPath
if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

[IconBgReplace]::Run($InputPath, $OutputPath, 0x00, 0x04, 0x16, $RgbThreshold)
Write-Host "Wrote $OutputPath"
