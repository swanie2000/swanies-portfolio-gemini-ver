/**
 * Local copy for testing the join-testing form before deploy.
 * Copy to beta-unlock-config.js (gitignored) and set the same secret as
 * BETA_UNLOCK_SECRET in Android local.properties.
 *
 * Live site: GitHub Actions injects beta-unlock-config.js from repo secret
 * BETA_UNLOCK_SECRET during deploy (see .github/workflows/deploy-website.yml).
 *
 * WARNING: The deployed config is visible in browser DevTools — same class of
 * exposure as the secret in the APK. Use programEnd to stop new redemptions.
 */
window.BETA_UNLOCK_CONFIG = {
  secret: "REPLACE_WITH_YOUR_SECRET_MIN_16_CHARS",
  programEnd: "2027-06-01",
};
