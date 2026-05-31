# Swanie's Portfolio — static website

Marketing site (**`index.html`**) plus **`privacy.html`**, **`press.html`** (press kit), and shared **`styles.css`** / **`bg-pattern.svg`**.

## Before you publish

1. **Tab icon** — **`favicon-tab.png`** is a **512×512** composite: **`#000416`** background with **`ic_swan_website.png`** centered so the swan reads on light browser chrome. After you change **`ic_swan_website.png`**, regenerate **`favicon-tab.png`** the same way (or bump **`?v=`** on the `<link>` tags in **`index.html`** / **`privacy.html`** after replacing it). Header logo still uses **`ic_swan_website.png`** directly.
2. **Demo video + screenshots** — **`website/marketing/final_swanies_portfolio_demo_video_web.mp4`** (720p, muted-friendly web export) is the first card in the **See the app in action** carousel on **`index.html`** (autoplay muted loop). Source masters can stay local; keep the web export under **100 MB** for GitHub. **`website/images/01_sp_*.jpg`** … **`12_sp_*.jpg`** (JPEG, **1080×2181**) follow in the same row. For **Play tablet** slots (**7-inch** and **10-inch**), use the letterboxed **9:16** exports: **`website/images/play_tablet_7inch/`** (**1080×1920**) and **`website/images/play_tablet_10inch/`** (**1440×2560**). Regenerate tablets: **`scripts/export-play-tablet-screenshots.ps1`** from repo root.
3. **`index.html` — Get the app** — Production **`#get-app`** section: Google Play CTA (set **`PLAY_URL`** in the page script when the listing is live), support email, and a **site QR** for **`SITE_SHARE_URL`** (defaults to **`https://swaniedesigns.com/`**). QR is drawn in-browser via **`website/js/qrcode.min.js`** (MIT — **`website/js/NOTICE-qrcodejs.txt`**).
4. **`press.html`** — One-pager for reviewers (facts, package id, links). Update the “Last updated” line when you change copy.
5. **`privacy.html`** — Keep policy text aligned with Play **Data safety** and your in-app **Privacy & terms** strings.
6. **`bg-pattern.svg`** — Very light dot/grid/wave texture behind **`bg-layer`**; tweak opacity in **`styles.css`** (`.bg-pattern` and `prefers-color-scheme` blocks) if you want it stronger or softer.

## SEO & Search Console

- **`robots.txt`** — allows crawlers; points to **`sitemap.xml`**.
- **`sitemap.xml`** — home, privacy, press (canonical **`https://swaniedesigns.com/`** URLs).
- **`index.html`**, **`privacy.html`**, **`press.html`** — canonical links, Open Graph / Twitter cards, JSON-LD on the homepage.
- **Verify Google Search Console** — step-by-step: **`docs/SEARCH_CONSOLE_SETUP.md`** (HTML meta tag, HTML file, or Cloudflare DNS TXT).
- **Recruit testers (legacy)** — organic beta posts: **`docs/RECRUIT_INTERNAL_TESTERS.md`** (site is now production-focused; use closed testing / Testers Community for Play gates).

After deploy, confirm **`https://swaniedesigns.com/robots.txt`** and **`https://swaniedesigns.com/sitemap.xml`** load in a browser.

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
