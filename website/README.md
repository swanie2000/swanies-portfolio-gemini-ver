# Swanie's Portfolio — static website

Marketing site (**`index.html`**) plus **`privacy.html`**, **`press.html`** (press kit), and shared **`styles.css`** / **`bg-pattern.svg`**.

## Before you publish

1. **Tab icon** — **`favicon-tab.png`** is a **512×512** composite: **`#000416`** background with **`ic_swan_website.png`** centered so the swan reads on light browser chrome. After you change **`ic_swan_website.png`**, regenerate **`favicon-tab.png`** the same way (or bump **`?v=`** on the `<link>` tags in **`index.html`** / **`privacy.html`** after replacing it). Header logo still uses **`ic_swan_website.png`** directly.
2. **Screenshots** — **`website/images/01_sp_*.jpg`** … **`12_sp_*.jpg`** (JPEG, **1080×2181**). The **See the app in action** block on **`index.html`** is a **horizontal scroll** row; same files are suitable for **Play Console** **phone** screenshots (order 01→12 matches the page). For **Play tablet** slots (**7-inch** and **10-inch**), use the letterboxed **9:16** exports (same filenames, navy `#000416` bars): **`website/images/play_tablet_7inch/`** (**1080×1920**) and **`website/images/play_tablet_10inch/`** (**1440×2560**). Regenerate after changing sources: **`scripts/export-play-tablet-screenshots.ps1`** from repo root.
3. **`index.html` — QR codes (self-contained)** — The **Join testing** section has two meanings:
   - **This page on your phone:** a QR for the **marketing site URL** (defaults to `window.location` so people at a desk can open the same page on mobile). Override with **`SITE_SHARE_URL`** if you need a fixed canonical `https://…` link.
   - **Google Play tester invite:** when **`TESTER_URL`** is a valid `http(s)` URL, a second QR encodes that **Play tester** link (opt-in / install), not the website.
   Both QRs are generated **in the browser** using vendored **`website/js/qrcode.min.js`** (QRCode.js, MIT — see **`website/js/NOTICE-qrcodejs.txt`**). No third-party QR image API is used.
4. **`press.html`** — One-pager for reviewers (facts, package id, links). Update the “Last updated” line when you change copy.
5. **`privacy.html`** — Keep policy text aligned with Play **Data safety** and your in-app **Privacy & terms** strings.
6. **`bg-pattern.svg`** — Very light dot/grid/wave texture behind **`bg-layer`**; tweak opacity in **`styles.css`** (`.bg-pattern` and `prefers-color-scheme` blocks) if you want it stronger or softer.

## Free hosting options

### GitHub Pages (this repo)

1. In GitHub: **Settings → Pages → Build and deployment**, set **Source** to **GitHub Actions**.
2. Push the workflow in `.github/workflows/deploy-website.yml` (included). On each push to `main` that touches `website/` or the workflow, the site deploys from the `website/` folder.
3. Your site URL will look like `https://<username>.github.io/<repo>/` (project site) unless you use a custom domain.

### Netlify or Vercel (free tiers)

- Connect the repo and set the **publish directory** to `website` (or deploy only that folder). No backend required.

### Cloudflare Pages

- Same idea: static site, root = `website`.

All of these serve static HTML/CSS at no charge within normal hobby limits.
