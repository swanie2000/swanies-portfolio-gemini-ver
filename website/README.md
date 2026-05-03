# Swanie's Portfolio — static website

Single-page marketing site plus a **privacy policy shell** for your Play Console URL.

## Before you publish

1. **`index.html`** — Set the real Google Play link on the primary button (see comments in the file). Remove `aria-disabled` when the URL is live.
2. **`privacy.html`** — Replace `[bracketed]` placeholders and the yellow callout with your final policy text. Remove `noindex` from the meta tag when you want search engines to index it.
3. **`privacy.html`** — Remove or relax the draft callout once the page is final.

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
