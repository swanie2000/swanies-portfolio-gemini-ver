# Swanie's Portfolio — static website

Marketing site (**`index.html`**) plus **`privacy.html`**, **`press.html`** (press kit), **`contact.html`**, and shared **`styles.css`**.

## Brand assets (2026 rebrand)

- **Logo:** **`images/swan-no-background.png`** (header on all pages).
- **Screenshots:** **`images/PlayStore_01_Everything_You_Own_One_Portfolio.png`** … **`PlayStore_08_Detailed_Holdings.png`** — same set as the Play Store listing.
- **Feature graphic:** **`play_store_feature_graphic_1024x500.png`** (Open Graph / Twitter cards); source also in **`images/Feature_Graphic_V6_Final_Python.png`**.
- **Theme:** Navy **`#000416`** + gold **`#d4af37`**, Inter font — see **`styles.css`**. Draft reference: **`index_new_brand_theme.html`** (superseded by **`index.html`**).

## Before you publish

1. **Tab icon** — **`favicon-tab.png`** is a **512×512** composite with **`#000416`** background. After changing the swan logo, regenerate **`favicon-tab.png`** and bump **`?v=`** on `<link rel="icon">` tags if needed.
2. **`index.html` — Get the app** — Public Play CTA and QR when **`PLAY_URL`** is set in the page script.
3. **`contact.html`** — Support / contact page; **`js/contact-form.js`** posts to Web3Forms (no mailto). Key must match **`WEB3FORMS_ACCESS_KEY`** in **`local.properties`**; restrict domains in the Web3Forms dashboard.
4. **`press.html`** — One-pager for reviewers (facts, package id, links). Update the “Last updated” line when you change copy.
5. **`privacy.html`** — Keep policy text aligned with Play **Data safety** and your in-app **Privacy & terms** strings.

## SEO & Search Console

- **`robots.txt`** — allows crawlers; points to **`sitemap.xml`**.
- **`sitemap.xml`** — home, privacy, press, contact (canonical **`https://swaniedesigns.com/`** URLs).
- **`index.html`**, **`privacy.html`**, **`press.html`**, **`contact.html`** — canonical links, Open Graph / Twitter cards, JSON-LD on the homepage.
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
