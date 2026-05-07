# Swanie's Portfolio — static website

Single-page marketing site plus a **privacy policy shell** for your Play Console URL.

## Before you publish

1. **`favicon.png`** — Put your **square** swan PNG in `website/` with this exact name: **`favicon.png`** (recommended **512×512** or **256×256** PNG with transparent or solid background). The HTML already points `<link rel="icon">` and `apple-touch-icon` at this file; until it exists in the repo, browsers fall back to `favicon.svg`.
2. **`index.html`** — Set `PLAY_URL` / `TESTER_URL` in the script at the bottom when links are ready.
3. **`privacy.html`** — Keep policy text aligned with Play **Data safety** and your in-app **Privacy & terms** strings.

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
