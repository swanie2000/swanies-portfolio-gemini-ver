# Swanie's Portfolio — static website

Marketing site (**`index.html`**) plus **`privacy.html`**, **`press.html`** (press kit), and shared **`styles.css`** / **`bg-pattern.svg`**.

## Before you publish

1. **Tab icon** — **`favicon-tab.png`** is a **512×512** composite: **`#000416`** background with **`ic_swan_website.png`** centered so the swan reads on light browser chrome. After you change **`ic_swan_website.png`**, regenerate **`favicon-tab.png`** the same way (or bump **`?v=`** on the `<link>` tags in **`index.html`** / **`privacy.html`** after replacing it). Header logo still uses **`ic_swan_website.png`** directly.
2. **Screenshots (optional)** — Add images under **`website/images/`** with the names referenced in `index.html` (currently JPEGs: `screen-01-vault-home.jpg`, `screen-02-asset-details.jpg`, `screen-03-widget.jpg`, `screen-03-widget-manager.jpg`). If you rename files, update the `<img src="…">` paths to match.
3. **`index.html`** — Set `PLAY_URL` / `TESTER_URL` in the script at the bottom when links are ready. When **`TESTER_URL`** is a valid `http(s)` URL, **Join testing** shows a **QR code on desktop-width viewports** (hidden on narrow screens). The QR image is requested from **`api.qrserver.com`** (your URL is sent there as query data). Swap the `wireTesterQr` implementation if you need fully offline or self-hosted QR generation.
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
