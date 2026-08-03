# Swanie Designs — static website

Hub landing (**`index.html`**) for everything Swanie creates, plus product pages and shared chrome.

| Page | Role |
|------|------|
| **`index.html`** | Swanie Designs hub — Portfolio + Camera Viewer (+ room for future apps) |
| **`portfolio.html`** | Swanie's Portfolio marketing (Play CTA, trailer, screenshots) |
| **`camview.html`** | Swanie's Camera Viewer marketing + download placeholders |
| **`privacy.html`** | Portfolio privacy policy |
| **`press.html`** | Portfolio press kit |
| **`contact.html`** | Studio contact form |
| **`styles.css`** | Shared navy / gold theme |

## Brand assets (2026)

- **Logo:** **`images/swan-no-background.png`**
- **Portfolio screenshots:** **`images/PlayStore_01_…`** through **`PlayStore_08_…`**
- **CamView art:** **`images/camview-splash.png`**
- **Feature graphics:** **`play_store_feature_graphic_1024x500.png`** (landscape / OG) · **`images/feature_graphic_vertical_1080x1920.png`** (portrait)
- **Trailers:** **`marketing/trailer_landscape_1920x1080.mp4`** · **`marketing/trailer_portrait_1080x1920.mp4`**
- **Theme:** Navy **`#000416`** + gold **`#d4af37`**, Inter — see **`styles.css`**

## Before you publish

1. **Tab icon** — **`favicon-tab.png`**. After changing the swan logo, regenerate and bump **`?v=`** on favicon links if needed.
2. **`portfolio.html`** — Play CTA + QR via **`PLAY_URL`**; trailer at **`#trailer`**; screenshot carousel at **`#screenshots`**.
3. **`camview.html`** — Wire Android APK + Windows Setup zip download URLs when storefront files are hosted.
4. **`contact.html`** — Web3Forms via **`js/contact-form.js`** (key in **`local.properties`** / dashboard domain allowlist).
5. **`press.html`** / **`privacy.html`** — keep aligned with Play listing and Data safety.

## SEO & Search Console

- **`robots.txt`**, **`sitemap.xml`**, canonical / OG / JSON-LD on pages.
- Verify: **`docs/SEARCH_CONSOLE_SETUP.md`**.

Deploy: push **`main`** → GitHub Actions (`.github/workflows/deploy-website.yml`) → **https://swaniedesigns.com**
