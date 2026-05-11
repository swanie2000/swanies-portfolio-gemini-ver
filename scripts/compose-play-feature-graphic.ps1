# Builds Google Play feature graphic: remove uniform dark edge background, composite artwork
# centered on #000416 at 1024 x 500 (Play requirement — not 512).
#
# Usage (from repo root):
#   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\compose-play-feature-graphic.ps1

param(
    [string] $SourcePath = "",
    [string] $OutputPath = "",
    [int] $CanvasW = 1024,
    [int] $CanvasH = 500,
    [int] $PaddingPx = 48,
    [int] $BgRgbThreshold = 38
)

$ErrorActionPreference = 'Stop'
if (-not $SourcePath) {
    $SourcePath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\website\play_store_feature_icon_1024x512.png"))
}
if (-not $OutputPath) {
    $OutputPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\website\play_store_feature_graphic_1024x500.png"))
}

Add-Type -AssemblyName System.Drawing

$cs = @'
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;

public static class FeatureGraphicComposer {
  const int AlphaCutoff = 16;

  public static void Compose(string sourcePath, string outputPath, int canvasW, int canvasH, int padding, int rgbThresh) {
    using (var src = new Bitmap(sourcePath)) {
      using (var working = ToArgb(src)) {
        Color seed = working.GetPixel(0, 0);
        FloodRemoveBackground(working, seed, rgbThresh);

        Rectangle bbox = ComputeContentBounds(working);
        if (bbox.Width < 2 || bbox.Height < 2)
          throw new InvalidOperationException("Could not find foreground content after background removal.");

        using (Bitmap cropped = Crop(working, bbox)) {
          float innerW = canvasW - 2f * padding;
          float innerH = canvasH - 2f * padding;
          float scale = Math.Min(innerW / cropped.Width, innerH / cropped.Height);
          int dw = Math.Max(1, (int)Math.Round(cropped.Width * scale));
          int dh = Math.Max(1, (int)Math.Round(cropped.Height * scale));

          using (var canvas = new Bitmap(canvasW, canvasH, PixelFormat.Format24bppRgb))
          using (var g = Graphics.FromImage(canvas)) {
            g.Clear(Color.FromArgb(255, 0x00, 0x04, 0x16));
            g.InterpolationMode = InterpolationMode.HighQualityBicubic;
            g.PixelOffsetMode = PixelOffsetMode.HighQuality;
            g.SmoothingMode = SmoothingMode.HighQuality;
            g.CompositingQuality = CompositingQuality.HighQuality;

            int dx = (canvasW - dw) / 2;
            int dy = (canvasH - dh) / 2;
            var dest = new Rectangle(dx, dy, dw, dh);
            g.DrawImage(cropped, dest, new Rectangle(0, 0, cropped.Width, cropped.Height), GraphicsUnit.Pixel);
            canvas.Save(outputPath, ImageFormat.Png);
          }
        }
      }
    }
  }

  static Bitmap ToArgb(Bitmap src) {
    var bmp = new Bitmap(src.Width, src.Height, PixelFormat.Format32bppArgb);
    using (var g = Graphics.FromImage(bmp)) {
      g.DrawImage(src, 0, 0);
    }
    return bmp;
  }

  static void FloodRemoveBackground(Bitmap bmp, Color seed, int t) {
    int w = bmp.Width, h = bmp.Height;
    bool[,] vis = new bool[w, h];
    var q = new Queue<Point>();
    int[] dx = new int[] { -1, 1, 0, 0 };
    int[] dy = new int[] { 0, 0, -1, 1 };

    Point[] corners = new Point[] {
      new Point(0, 0), new Point(w - 1, 0), new Point(0, h - 1), new Point(w - 1, h - 1)
    };

    foreach (Point s in corners) {
      if (vis[s.X, s.Y]) continue;
      Color sc = bmp.GetPixel(s.X, s.Y);
      if (!SimilarBg(sc, seed, t)) continue;
      q.Enqueue(s);
      vis[s.X, s.Y] = true;
      bmp.SetPixel(s.X, s.Y, Color.Transparent);
    }

    while (q.Count > 0) {
      Point p = q.Dequeue();
      for (int k = 0; k < 4; k++) {
        int nx = p.X + dx[k], ny = p.Y + dy[k];
        if (nx < 0 || ny < 0 || nx >= w || ny >= h || vis[nx, ny]) continue;
        Color c = bmp.GetPixel(nx, ny);
        if (!SimilarBg(c, seed, t)) continue;
        vis[nx, ny] = true;
        bmp.SetPixel(nx, ny, Color.Transparent);
        q.Enqueue(new Point(nx, ny));
      }
    }
  }

  static bool SimilarBg(Color c, Color seed, int t) {
    if (c.A < AlphaCutoff) return true;
    return Math.Abs(c.R - seed.R) <= t && Math.Abs(c.G - seed.G) <= t && Math.Abs(c.B - seed.B) <= t;
  }

  static Rectangle ComputeContentBounds(Bitmap bmp) {
    int w = bmp.Width, h = bmp.Height;
    int minX = w, minY = h, maxX = 0, maxY = 0;
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        Color c = bmp.GetPixel(x, y);
        if (c.A > 40) {
          if (x < minX) minX = x;
          if (y < minY) minY = y;
          if (x > maxX) maxX = x;
          if (y > maxY) maxY = y;
        }
      }
    }
    if (minX > maxX) return Rectangle.Empty;
    return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
  }

  static Bitmap Crop(Bitmap src, Rectangle r) {
    var dst = new Bitmap(r.Width, r.Height, PixelFormat.Format32bppArgb);
    using (var g = Graphics.FromImage(dst)) {
      g.DrawImage(src, 0, 0, r, GraphicsUnit.Pixel);
    }
    return dst;
  }
}
'@

Add-Type -TypeDefinition $cs -ReferencedAssemblies System.Drawing

[FeatureGraphicComposer]::Compose(
    [string]$SourcePath,
    [string]$OutputPath,
    [int]$CanvasW,
    [int]$CanvasH,
    [int]$PaddingPx,
    [int]$BgRgbThreshold
)

Write-Host "Wrote $OutputPath (${CanvasW}x${CanvasH}, navy #000416, centered scaled artwork)"
