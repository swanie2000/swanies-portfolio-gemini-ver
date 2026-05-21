# Beta unlock codes (planned — not implemented yet)

**Status:** Design approved by owner **2026-05-22**; **no app code** in this commit. Next agent implements per **`docs/AI_HANDOFF.md`** § *Beta unlock codes*.

**Problem:** Free tier is intentionally naggy; beta testers need **Pro** without RevenueCat dashboard grants (sandbox/production confusion, no “add customer” UI). **RevenueCat stays** for real Play purchases and restore.

---

## What testers see

1. Submit **`https://swaniedesigns.com/#join-testing`** with **Play Store Gmail**.
2. Owner adds Gmail to **Play Console → Internal testing → Testers** only (not License testing).
3. Owner emails install steps + a **personal unlock code** (e.g. `SWANIE-K7M2-9X4P`).
4. Tester installs from Play, **creates in-app account with the same Gmail**.
5. **Settings** (or Pro paywall) → **Enter beta unlock code** → Pro features unlock.
6. **Do not** buy Monthly / Yearly / Lifetime during beta.

Testers never open RevenueCat.

---

## What the owner does (after implementation)

1. Play Console → add Gmail (list 1).
2. Run generator (planned **`scripts/generate-beta-unlock-code.ps1`**) with:
   - **Email** = same Gmail as Play + in-app account (lowercase).
   - **Expires** = calendar date (~12 months for “1 year Pro free”).
3. Paste code into Web3Forms **READY TO REPLY** email (replace RevenueCat checklist).
4. Optional: note expiry date in plain English in the email.

---

## How a code works (technical)

### Email-bound

- Payload signed by app secret: `lowercase_email + "|" + expiry_yyyy-MM-dd`.
- Code = short readable string derived from **HMAC-SHA256** (e.g. grouped as `SWANIE-XXXX-XXXX`).
- On redeem, app requires:
  - User **logged in**.
  - Profile email (lowercase) **matches** email in signed payload.
  - Signature valid.
  - Today ≤ expiry (end of expiry day — pick one rule in implementation and document).

Wrong account → clear error: *code is for a different email*.

### Per-code expiry (paywall returns)

- After redeem, persist **`pro_unlock_expires_at`** (DataStore).
- **`isProUser`** = `(unlock valid and not expired)` **OR** `(RevenueCat entitlement active)`.
- When per-code expiry passes → unlock path off → user sees free tier / paywall again unless they **purchased via Play/RevenueCat**.

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

## Implementation checklist (next coding session)

1. **`ProUnlockPreferences`** (DataStore): `unlocked`, `expires_at`, optional `redeemed_email_hash` for audit.
2. **`BetaUnlockValidator`**: verify code + email + dates; check program sunset before accept.
3. **`SettingsViewModel`**: combine RC entitlement + unlock → **`isProUser`**; refresh widgets on change.
4. **UI**: Settings + Pro gate — code field, submit, error strings (all locales when strings added).
5. **`app/build.gradle.kts`**: `BETA_UNLOCK_SECRET` from `local.properties`; `BETA_UNLOCK_PROGRAM_END` (string date).
6. **`scripts/generate-beta-unlock-code.ps1`**: owner runs locally; same algorithm as app.
7. **`website/index.html`**: admin email template — code + expiry, **remove** RevenueCat grant checklist for testers.
8. **Tests**: validator unit tests (email match, expired, sunset, valid).

**Do not remove RevenueCat** in this pass.

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
