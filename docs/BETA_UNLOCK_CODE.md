# Beta unlock codes (planned — not implemented yet)

**Status:** **Implemented** (app **1.0.13** + website join-testing form). Play upload: **`versionCode` 13** after current live **12 (1.0.11)**. Owner must set **`BETA_UNLOCK_SECRET`** in **`local.properties`** (Android) and GitHub repo secret (live site deploy).

**Problem:** Free tier is intentionally naggy; beta testers need **Pro** without RevenueCat dashboard grants (sandbox/production confusion, no “add customer” UI). **RevenueCat stays** for real Play purchases and restore.

---

## What testers see

1. Submit **`https://swaniedesigns.com/#join-testing`** with **Play Store Gmail**.
2. Owner adds Gmail to **Play Console → Internal testing → Testers** only (not License testing).
3. Owner emails install steps + a **personal unlock code** (auto-generated in Web3Forms admin email when form is submitted, e.g. `SWANIE-20270521-A1B2C3D4`).
4. Tester installs from Play, **creates in-app account with the same Gmail**.
5. **Settings** (or Pro paywall) → **Enter beta unlock code** → Pro features unlock.
6. **Do not** buy Monthly / Yearly / Lifetime during beta.

Testers never open RevenueCat.

---

## What the owner does (after implementation)

1. Play Console → add Gmail (list 1).
2. Open Web3Forms inbox — **READY TO REPLY** block already includes the code (or run **`scripts/generate-beta-unlock-code.ps1`**) with:
   - **Email** = same Gmail as Play + in-app account (lowercase).
   - **Expires** = **30 days** from generation (time to move them to RevenueCat promotional).
3. Paste code into Web3Forms **READY TO REPLY** email (replace RevenueCat checklist).
4. Optional: note expiry date in plain English in the email.

---

## How a code works (technical)

### Email-bound

- Payload signed by app secret: `lowercase_email + "|" + expiry_yyyy-MM-dd`.
- Code = `SWANIE-YYYYMMDD-HHHHHHHH` (8 hex chars = first 4 bytes of **HMAC-SHA256**).
- On redeem, app requires:
  - User **logged in**.
  - Profile email (lowercase) **matches** email in signed payload.
  - Signature valid.
  - Today ≤ expiry (end of expiry day — pick one rule in implementation and document).

Wrong account → clear error: *code is for a different email*.

### Per-code expiry (30 days)

- Codes are generated with **30-day** expiry (website form + CLI).
- After redeem, persist expiry in DataStore.
- When per-code expiry passes → unlock path off unless RevenueCat already took over.

### RevenueCat supersedes code (shipped)

- When RevenueCat reports **active Pro** (purchase or promotional grant), app sets **`supersededByRevenueCat`** and clears local unlock.
- After that: **`isProUser` = RevenueCat only** — code ignored even if still within 30 days.
- When RC entitlement ends → Pro off (unless they subscribe). Use RC **6–12 month** grants for managed testers; **lifetime** for family.

### Program sunset (global — owner security ask)

Separate from per-tester expiry:

- **`BETA_UNLOCK_PROGRAM_END`** — calendar date in **BuildConfig** (e.g. `2027-06-01`).
- **After this date:** app **rejects all new unlock redemptions** (even valid signatures). UI can hide or disable “Enter beta code”.
- **After sunset:** Pro only via **RevenueCat / Play purchase** (normal paid path).
- **Already-redeemed** unlocks: honor stored **`pro_unlock_expires_at`** until that date (do not extend program past per-user expiry). If program ends before a user’s personal expiry, implementation choice (recommend: **per-user expiry still honored** until their date; only **new** redemptions blocked).

This limits leak impact: even if someone extracts the signing secret from the APK, **redemption window closes** on a ship you control.

---

## Security (honest)

| Risk | Mitigation |
|------|------------|
| Secret in APK reverse-engineered | Acceptable for **beta**; not DRM. **Program sunset** + **per-email** codes limit blast radius. |
| One code shared on Reddit | Email binding → only works for that login email. |
| Universal “god code” | **Avoid**; use per-email generator only. |
| Bypass paid app forever | Sunset stops **new** unlocks; public launch relies on **RC/Play**; remove or hide unlock UI on production if desired later. |
| Secret in git | Keep **`BETA_UNLOCK_SECRET`** in **`local.properties`** only → **BuildConfig** at compile time; document in handoff, never commit secret. |

This is **weaker than** server-side keys but **much simpler** than RevenueCat per-tester grants for internal testing.

---

## Files (shipped)

| Piece | Location |
|-------|----------|
| Validator | `app/.../billing/BetaUnlockValidator.kt` |
| Storage | `app/.../data/ProUnlockPreferences.kt` |
| UI | `BetaUnlockCodeSection.kt` on Pro paywall |
| Website generator | `website/js/beta-unlock-code.js` + config injected in **deploy-website.yml** |
| CLI | `scripts/generate-beta-unlock-code.ps1`, `scripts/beta-unlock-code.mjs` |

**Owner setup:** Add to **`local.properties`**: `BETA_UNLOCK_SECRET=your-long-random-string` (same value in GitHub **Settings → Secrets → Actions → BETA_UNLOCK_SECRET**). Optional: `BETA_UNLOCK_PROGRAM_END=2027-06-01`.

**Do not remove RevenueCat** — paid path unchanged.

---

## RevenueCat role after this ships

| User | Pro source |
|------|------------|
| Beta tester | Email-bound unlock code |
| Paying customer | Play purchase → RevenueCat entitlement |
| Beta after code expires | Free tier or subscribe |

---

## Related files (today)

| File | Role |
|------|------|
| `SettingsViewModel.kt` | `isProUser` today = RC only |
| `RevenueCatMonetizationManager.kt` | Purchases / restore |
| `MonetizationModule.kt` | Binds RC manager |
| `website/index.html` | `buildTesterRequestAdminEmail()` — still RC checklist until website pass |
