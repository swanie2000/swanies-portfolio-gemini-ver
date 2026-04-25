## Master Build Checklist

- [x] Widget Manager Preview Fidelity - COMPLETED
  - Absolute Lane Geometry locked to `100dp / Flex / 100dp` for mirror-accurate layout.
  - "Zero-Gap" vertical tuck achieved by line-height override logic (`lineHeight = 11.sp`) on the compact title/value rails.
- [x] Security & Interface Sync (V40.33) - COMPLETED
  - Settings security label rebranded to `LOGIN OPTION` and remaining settings toggles converted to Boutique checkboxes.
  - Smart biometric gating enforced for auto-launch paths while preserving manual override and settings handshake prompts.
  - Ghost startup biometric trigger resolved via synchronized gate logic in `MainActivity`, `UnlockVaultScreen`, and `AuthViewModel`.

- [x] Auth Surface Unification + Session Control (V40.34) - COMPLETED
  - Unified auth state handling to shared activity-scoped `AuthViewModel` across Home/Unlock/NavGraph/Create/Restore/Portfolio Manager.
  - Enforced deterministic login path (`HOME -> UNLOCK_VAULT -> HOLDINGS`) with no accidental direct bypass.
  - Added session timeout controls with persisted settings (`Never`, `15s`, `30s`, `60s`, `5m`, `15m`) and resilient background detection via `onUserLeaveHint`.
  - Added biometric UX hardening:
    - `Allow biometrics for login` gating for login-screen biometric button visibility.
    - Friendly biometric prompt and error messaging.
    - Safety toggle: `Require Password After Biometric Failure` (default ON).
  - Hardened credential verification by normalized matching against username/display name/email.
  - Final polish bundle shipped:
    - Unlock screen visual alignment and copy cleanup.
    - Security verbiage simplification in Settings.
    - App orientation locked to portrait.

- [ ] V40.35 Auth Reliability Harness - NEXT
  - Add automated regression coverage for login success/failure, biometric success/cancel/fail, and timeout edge boundaries.
  - Add optional developer diagnostics surface for auth state + timeout decision tracing.
  - Validate biometric behavior consistency across representative OEM devices.
