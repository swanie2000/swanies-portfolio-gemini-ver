# Google Search Console — verify swaniedesigns.com

Use this after the SEO deploy is live on **https://swaniedesigns.com**. Goal: prove you own the site so Google can show search performance and index your pages.

**Property to add:** `https://swaniedesigns.com` (URL-prefix property — include `https://`)

---

## Step 1 — Open Search Console

1. Go to [Google Search Console](https://search.google.com/search-console).
2. Sign in with the Google account you use for Play Console (or your main Gmail).
3. Click **Add property**.
4. Choose **URL prefix** and enter: `https://swaniedesigns.com`
5. Click **Continue** — Google shows verification methods.

---

## Step 2 — Pick a verification method

### Option A — HTML meta tag (easiest if you can edit the site)

1. On the verification screen, choose **HTML tag**.
2. Google shows something like:
   ```html
   <meta name="google-site-verification" content="AbCdEf1234567890..." />
   ```
3. Copy only the **content** value (the long token inside `content="..."`).
4. Open **`website/index.html`** in the repo.
5. Find this line in `<head>`:
   ```html
   <!-- <meta name="google-site-verification" content="PASTE_TOKEN_HERE" /> -->
   ```
6. Replace `PASTE_TOKEN_HERE` with your token and **remove the comment markers** so the line is active:
   ```html
   <meta name="google-site-verification" content="AbCdEf1234567890..." />
   ```
7. Commit and push to **`main`** (GitHub Actions redeploys the site in ~1–2 minutes).
8. In Search Console, click **Verify**.

If verification fails, wait 2–5 minutes for Pages deploy, hard-refresh `https://swaniedesigns.com`, then try again.

---

### Option B — HTML file upload

1. On the verification screen, choose **HTML file**.
2. Google gives you a filename like **`google1234567890abcdef.html`** and a one-line file body.
3. Create that file in the repo at **`website/google1234567890abcdef.html`** (exact name Google gave you).
4. File contents are only one line, e.g.:
   ```
   google-site-verification: google1234567890abcdef.html
   ```
5. Push to **`main`** and wait for deploy.
6. Open `https://swaniedesigns.com/google1234567890abcdef.html` in a browser — you should see that one line.
7. Click **Verify** in Search Console.

---

### Option C — DNS TXT record (Cloudflare)

Best if you prefer not to touch HTML again. Domain **swaniedesigns.com** uses Cloudflare (GitHub Pages + CNAME).

1. On the verification screen, choose **Domain name provider** or **TXT record**.
2. Google shows a TXT record like:
   - **Host / Name:** `@` (or `swaniedesigns.com`)
   - **Value:** `google-site-verification=AbCdEf1234567890...`
3. Log in to **Cloudflare** → select **swaniedesigns.com** → **DNS** → **Records**.
4. Click **Add record**:
   - **Type:** TXT
   - **Name:** `@`
   - **Content:** paste the full value Google gave you (including `google-site-verification=`)
   - **TTL:** Auto
5. Save. DNS can take **5 minutes to a few hours** (usually under 30 min).
6. In Search Console, click **Verify**.

**Note:** If GitHub Pages also uses an apex redirect, TXT on `@` still works for Search Console — it does not break the site.

---

## Step 3 — Submit your sitemap

After verification succeeds:

1. Search Console → **Sitemaps** (left sidebar).
2. Enter: `sitemap.xml`
3. Click **Submit**.

Google will crawl:

- `https://swaniedesigns.com/`
- `https://swaniedesigns.com/privacy.html`
- `https://swaniedesigns.com/press.html`

---

## Step 4 — Request indexing (optional, once)

1. **URL inspection** (top search bar) → paste `https://swaniedesigns.com/`
2. Click **Request indexing** if offered.
3. Repeat for `https://swaniedesigns.com/#join-testing` — Google may treat hash URLs as the same page; the homepage request is enough.

Indexing is not instant. New sites often take **days to a few weeks** for meaningful impressions.

---

## Step 5 — Bing (optional, 5 minutes)

1. [Bing Webmaster Tools](https://www.bing.com/webmasters)
2. **Import from Google Search Console** — fastest path after Google is verified.
3. Or add site manually and submit the same `https://swaniedesigns.com/sitemap.xml`.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Verify failed (HTML tag) | Confirm deploy finished; View Page Source on homepage; search for `google-site-verification`. |
| Verify failed (DNS) | Cloudflare DNS only — wait 30–60 min; use [Google Admin Toolbox Dig](https://toolbox.googleapps.com/apps/dig/) for TXT on `swaniedesigns.com`. |
| Sitemap “Couldn’t fetch” | Open `https://swaniedesigns.com/sitemap.xml` in browser; must show XML. |
| No search traffic yet | Normal pre-launch; SEO helps long-term; **tester recruiting** needs direct outreach (FB, communities) — see handoff **§ Next steps**. |

---

## What Search Console does *not* do

- It does **not** add Play Store testers — use **Play Console → Internal testing → Testers** + **`#join-testing`** form.
- It does **not** guarantee rankings for “crypto portfolio app” — competitive term; brand + niche copy helps over time.

When the app is **public** on Play, update **`PLAY_URL`** in **`website/index.html`** and request re-indexing so Google sees the live store link.
