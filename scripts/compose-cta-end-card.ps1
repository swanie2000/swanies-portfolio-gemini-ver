# Builds the promo-video end card (9:16) with a real QR code to swaniedesigns.com.
# Production messaging — no internal-testing copy.
#
# Usage (from repo root):
#   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\compose-cta-end-card.ps1

param(
    [string] $SiteUrl = "https://swaniedesigns.com/",
    [string] $OutputPath = "",
    [string] $LogoPath = "",
    [int] $CanvasW = 1080,
    [int] $CanvasH = 1920
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent

if (-not $OutputPath) {
    $OutputPath = Join-Path $repoRoot "website\marketing\CTA_end_picture.png"
}
if (-not $LogoPath) {
    $LogoPath = Join-Path $repoRoot "website\ic_swan_website.png"
}

$OutputPath = [System.IO.Path]::GetFullPath($OutputPath)
$LogoPath = [System.IO.Path]::GetFullPath($LogoPath)
$qrTemp = [System.IO.Path]::GetFullPath((Join-Path $env:TEMP "swanies-cta-qr.png"))

$nodeCmd = Get-Command node -ErrorAction SilentlyContinue
if (-not $nodeCmd) {
    $fallbackNode = "c:\Program Files\cursor\resources\app\resources\helpers\node.exe"
    if (-not (Test-Path $fallbackNode)) { throw "Node.js not found (needed to generate QR PNG)." }
    $nodeExe = $fallbackNode
} else {
    $nodeExe = $nodeCmd.Source
}

$genScript = Join-Path $PSScriptRoot "generate-qr-png.js"
& $nodeExe $genScript $SiteUrl $qrTemp 16 | Out-Host
if (-not (Test-Path $qrTemp)) { throw "QR generation failed: $qrTemp" }

Add-Type -AssemblyName System.Drawing

$cs = @'
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Drawing.Text;

public static class CtaEndCardComposer {
  static readonly Color Bg = Color.FromArgb(255, 0x00, 0x04, 0x16);
  static readonly Color Gold = Color.FromArgb(255, 0xD4, 0xA8, 0x53);
  static readonly Color GoldLight = Color.FromArgb(255, 0xF0, 0xD9, 0x8A);
  static readonly Color GoldDark = Color.FromArgb(255, 0x9A, 0x7A, 0x3D);
  static readonly Color Text = Color.FromArgb(255, 0xE8, 0xEC, 0xF4);
  static readonly Color Muted = Color.FromArgb(255, 0x8B, 0x95, 0xA8);

  public static void Compose(string logoPath, string qrPath, string outputPath, int w, int h) {
    using (var canvas = new Bitmap(w, h, PixelFormat.Format24bppRgb))
    using (var g = Graphics.FromImage(canvas)) {
      g.Clear(Bg);
      g.SmoothingMode = SmoothingMode.HighQuality;
      g.InterpolationMode = InterpolationMode.HighQualityBicubic;
      g.PixelOffsetMode = PixelOffsetMode.HighQuality;
      g.TextRenderingHint = TextRenderingHint.AntiAliasGridFit;

      int cx = w / 2;
      int topPad = (int)Math.Round(h * 0.04);
      int topZoneH = (int)Math.Round(h * 0.17);
      int y = topPad + topZoneH + 28;

      using (var logo = TrimLogoContent(logoPath)) {
        int logoW = (int)Math.Round(w * 0.38);
        int logoH = (int)Math.Round(logoW * ((double)logo.Height / logo.Width));
        int logoY = topPad + Math.Max(0, (topZoneH - logoH) / 2);
        PointF centroid = GetForegroundCentroid(logo, 28);
        float centroidRatioX = centroid.X / logo.Width;
        int logoX = cx - (int)Math.Round(logoW * centroidRatioX);
        g.DrawImage(logo, logoX, logoY, logoW, logoH);
      }

      using (var serif = new Font("Georgia", 82f, FontStyle.Bold, GraphicsUnit.Pixel))
      using (var sans = new Font("Segoe UI Semibold", 44f, FontStyle.Regular, GraphicsUnit.Pixel))
      using (var caption = new Font("Segoe UI Semibold", 36f, FontStyle.Regular, GraphicsUnit.Pixel))
      using (var btnMain = new Font("Segoe UI Bold", 40f, FontStyle.Bold, GraphicsUnit.Pixel))
      using (var btnSub = new Font("Segoe UI Semibold", 24f, FontStyle.Regular, GraphicsUnit.Pixel))
      using (var footer = new Font("Segoe UI Semibold", 38f, FontStyle.Regular, GraphicsUnit.Pixel))
      using (var goldBrush = new SolidBrush(Gold))
      using (var textBrush = new SolidBrush(Text))
      using (var mutedBrush = new SolidBrush(Muted))
      using (var blackBrush = new SolidBrush(Color.Black)) {
        y = DrawCentered(g, "Take Control", serif, goldBrush, cx, y) + 12;
        y = DrawCentered(g, "Swanie\u2019s Portfolio\u2122", sans, textBrush, cx, y) + 36;

        int qrBox = (int)Math.Round(w * 0.54);
        int qrX = cx - qrBox / 2;
        using (var qr = Image.FromFile(qrPath)) {
          g.FillRectangle(Brushes.White, qrX - 8, y - 8, qrBox + 16, qrBox + 16);
          g.DrawImage(qr, qrX, y, qrBox, qrBox);
        }
        y += qrBox + 28;
        y = DrawCentered(g, "Scan to visit swaniedesigns.com", caption, textBrush, cx, y) + 36;

        int btnW = (int)Math.Round(w * 0.86);
        int btnH = 148;
        int btnX = cx - btnW / 2;
        DrawGoldButton(g, btnX, y, btnW, btnH);
        DrawButtonLabel(g, btnMain, btnSub, blackBrush, cx, y, btnH,
          "Get it on Google Play",
          "AVAILABLE NOW ON ANDROID");
        y += btnH + 36;

        DrawCenteredWrapped(g,
          "Track crypto & precious metals \u2014 local-first vault on your phone.",
          footer, textBrush, cx, y, w - 96);
      }

      canvas.Save(outputPath, ImageFormat.Png);
    }
  }

  static PointF GetForegroundCentroid(Bitmap bmp, int thresh) {
    double sumX = 0, sumY = 0, count = 0;
    for (int y = 0; y < bmp.Height; y++) {
      for (int x = 0; x < bmp.Width; x++) {
        if (!IsBackground(bmp.GetPixel(x, y), thresh)) {
          sumX += x;
          sumY += y;
          count++;
        }
      }
    }
    if (count == 0) return new PointF(bmp.Width / 2f, bmp.Height / 2f);
    return new PointF((float)(sumX / count), (float)(sumY / count));
  }

  static Bitmap TrimLogoContent(string path) {
    using (var src = LoadArgb(path)) {
      Rectangle bbox = ComputeForegroundBounds(src, 28);
      if (bbox.Width < 2 || bbox.Height < 2) return (Bitmap)src.Clone();
      return Crop(src, bbox);
    }
  }

  static Bitmap LoadArgb(string path) {
    using (var src = new Bitmap(path)) {
      var bmp = new Bitmap(src.Width, src.Height, PixelFormat.Format32bppArgb);
      using (var g = Graphics.FromImage(bmp)) { g.DrawImage(src, 0, 0); }
      return bmp;
    }
  }

  static bool IsBackground(Color c, int thresh) {
    if (c.A < 16) return true;
    return c.R <= thresh && c.G <= thresh && c.B <= thresh;
  }

  static Rectangle ComputeForegroundBounds(Bitmap bmp, int thresh) {
    int w = bmp.Width, h = bmp.Height;
    int minX = w, minY = h, maxX = 0, maxY = 0;
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        if (!IsBackground(bmp.GetPixel(x, y), thresh)) {
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

  static int DrawCentered(Graphics g, string text, Font font, Brush brush, int cx, int y) {
    SizeF size = g.MeasureString(text, font);
    g.DrawString(text, font, brush, cx - size.Width / 2f, y);
    return y + (int)Math.Ceiling(size.Height);
  }

  static void DrawCenteredWrapped(Graphics g, string text, Font font, Brush brush, int cx, int y, int maxWidth) {
    SizeF size = g.MeasureString(text, font, maxWidth);
    g.DrawString(text, font, brush, new RectangleF(cx - maxWidth / 2f, y, maxWidth, size.Height + 4),
      new StringFormat { Alignment = StringAlignment.Center, LineAlignment = StringAlignment.Near });
  }

  static void DrawGoldButton(Graphics g, int x, int y, int w, int h) {
    int r = 28;
    using (var path = RoundedRect(x, y, w, h, r))
    using (var brush = new LinearGradientBrush(
      new Rectangle(x, y, w, h),
      GoldLight,
      GoldDark,
      LinearGradientMode.Vertical)) {
      g.FillPath(brush, path);
    }
    using (var path = RoundedRect(x, y, w, h, r))
    using (var pen = new Pen(Color.FromArgb(80, 255, 255, 255), 1.5f)) {
      g.DrawPath(pen, path);
    }
    DrawAndroidIcon(g, x + w - 92, y + h / 2 - 24, 48);
  }

  static void DrawButtonLabel(Graphics g, Font mainFont, Font subFont, Brush brush, int cx, int y, int h,
    string line1, string line2) {
    SizeF s1 = g.MeasureString(line1, mainFont);
    SizeF s2 = g.MeasureString(line2, subFont);
    float total = s1.Height + s2.Height + 2f;
    float top = y + (h - total) / 2f;
    float left = cx - (s1.Width + 56f) / 2f;
    g.DrawString(line1, mainFont, brush, left, top);
    g.DrawString(line2, subFont, brush, left, top + s1.Height + 2f);
  }

  static void DrawAndroidIcon(Graphics g, int x, int y, int size) {
    float s = size / 48f;
    using (var brush = new SolidBrush(Color.Black)) {
      g.FillEllipse(brush, x + 8 * s, y + 4 * s, 32 * s, 32 * s);
      g.FillRectangle(brush, x + 14 * s, y + 30 * s, 6 * s, 14 * s);
      g.FillRectangle(brush, x + 28 * s, y + 30 * s, 6 * s, 14 * s);
      g.FillRectangle(brush, x + 6 * s, y + 18 * s, 8 * s, 3 * s);
      g.FillRectangle(brush, x + 34 * s, y + 18 * s, 8 * s, 3 * s);
    }
  }

  static GraphicsPath RoundedRect(int x, int y, int w, int h, int r) {
    var path = new GraphicsPath();
    int d = r * 2;
    path.AddArc(x, y, d, d, 180, 90);
    path.AddArc(x + w - d, y, d, d, 270, 90);
    path.AddArc(x + w - d, y + h - d, d, d, 0, 90);
    path.AddArc(x, y + h - d, d, d, 90, 90);
    path.CloseFigure();
    return path;
  }
}
'@

Add-Type -TypeDefinition $cs -ReferencedAssemblies System.Drawing

[CtaEndCardComposer]::Compose($LogoPath, $qrTemp, $OutputPath, $CanvasW, $CanvasH)

Remove-Item -Force $qrTemp -ErrorAction SilentlyContinue

Write-Host "Wrote $OutputPath (${CanvasW}x${CanvasH}) QR -> $SiteUrl"
