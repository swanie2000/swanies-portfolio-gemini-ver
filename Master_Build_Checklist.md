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

- [x] V40.35 Auth Reliability Harness - COMPLETED
  - Added centralized `AuthPolicy` module for credential matching and timeout lock decisions.
  - Added `AuthPolicyTest` unit coverage for identity/password matching and timeout thresholds including `Never`.
  - Wired runtime auth decision points (`MainViewModel`, `MainActivity`) to shared policy methods.
  - Added automated GitHub workflow (`android-auth-safety.yml`) to run auth unit tests + Kotlin compile on push/PR.

- [x] V40.38 Recovery + Password Management + Theme Parity - COMPLETED
  - Shipped secure hint-recovery UX requiring username + email + biometric verification before revealing hint.
  - Added biometric-gated password update flow in Settings with create-account password rules and keyboard-safe popup behavior.
  - Preserved user input on login/create-account correction paths and tightened unlock-screen layout density.
  - Applied user-selected theme colors (including card background) to all touched popup/dialog surfaces for visual consistency.
  - Added follow-up UI polish in Security section (`RESET PASSWORD` placement/alignment) and validated with clean Kotlin compile.

- [ ] V40.36 Auth Flow Instrumentation Harness - NEXT
  - Add instrumentation coverage for login navigation and biometric success/cancel/failure UI behavior.
  - Add debug-only auth diagnostics surface for state transition tracing.
  - Execute cross-device biometric callback validation and capture OEM behavior notes.
  - Revisit account recovery strategy (post-hint path, fallback options, and anti-lockout policy) after current auth hardening cycle.

- [ ] V40.37 Localization + Billing Blueprint Track - PLANNED
  - Multi-language support foundation:
    - Extract user-facing strings for localization.
    - Add persisted language selection and apply locale on startup.
  - Subscription tiers:
    - Define and enforce `Trial`, `Paid`, and `Premium` feature access matrix.
    - Add safe downgrade/entitlement fallback behavior.
  - Profile/schema integration:
    - Track `language_setting` and `user_tier` in user profile persistence model.
    - Surface tier and language controls in Settings/account UX.
