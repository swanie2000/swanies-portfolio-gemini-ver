# Swanie's Portfolio — static website

Marketing site (**`index.html`**) plus **`privacy.html`**, **`press.html`**, **`contact.html`**, and shared **`styles.css`**.

## Brand assets (2026)

- **Logo:** **`images/swan-no-background.png`**
- **Screenshots:** **`images/PlayStore_01_…`** through **`PlayStore_08_…`** (same set as Play Store)
- **Feature graphics:** **`play_store_feature_graphic_1024x500.png`** (landscape / OG) · **`images/feature_graphic_vertical_1080x1920.png`** (portrait poster)
- **Trailers:** **`marketing/trailer_landscape_1920x1080.mp4`** (desktop) · **`marketing/trailer_portrait_1080x1920.mp4`** (mobile / upright)
- **Theme:** Navy **`#000416`** + gold **`#d4af37`**, Inter — see **`styles.css`**

## Before you publish

1. **Tab icon** — **`favicon-tab.png`**. After changing the swan logo, regenerate and bump **`?v=`** on favicon links if needed.
2. **`index.html`** — Play CTA + QR via **`PLAY_URL`**; trailer at **`#trailer`**; screenshot carousel at **`#screenshots`**.
3. **`contact.html`** — Web3Forms via **`js/contact-form.js`** (key in **`local.properties`** / dashboard domain allowlist).
4. **`press.html`** / **`privacy.html`** — keep aligned with Play listing and Data safety.

## SEO & Search Console

- **`robots.txt`**, **`sitemap.xml`**, canonical / OG / JSON-LD on pages.
- Verify: **`docs/SEARCH_CONSOLE_SETUP.md`**.

Deploy: push **`main`** → GitHub Actions (`.github/workflows/deploy-website.yml`) → **https://swaniedesigns.com**
