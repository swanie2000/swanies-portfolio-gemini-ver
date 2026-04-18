UPDATED MASTER DOCUMENT: SOVEREIGN SHIELD (V24: FULL-SPECTRUM PRECISION)
🎯 THE CORE MISSION

To transition "Swanie’s Portfolio" into a commercial-grade financial vault. The mission prioritizes user-owned data, high-precision asset tracking (8 decimals), and a professional "Boutique" aesthetic.
🛡️ 1. ARCHITECTURAL RECAP (THE KINETIC RESTORATION)

    Visual Logic: Home Screen animations are synchronized. Coordinates are LOCKED.

    Signage & Branding: Manifest and SecurityManager updated to "Swanie's Portfolio."

    Security: Legacy credentials purged; Biometric Handshake is the standard.

    Suitcase Protocol: Asset data is serialized into a 10-part string for the widget to prevent Binder transaction overhead and rounding errors.

📉 2. THE BATTLE REPORT (TODAY’S VICTORIES: V24)

    Tiered Precision Logic: Implemented across Repository, Widget, and UI.

        ≥$0.10→ 2 decimals.

        $0.0001 to $0.10→ 5 decimals.

        <$0.0001→ 8 decimals (Squashed the "$0.00" bug for micro-assets like PEPECAT).

    Widget Mastering: Expanded the "Identity Lane" to 130dp for breathing room and centered the sparklines.

    UI Uniformity: Synchronized the "Compact" and "Full" asset cards in HoldingsUIComponents.kt to match the 8-decimal standard.

    Database Stability: Reverted AssetEntity schema changes to fix Room Identity Hash crashes. The "Suitcase" carries the display strings, keeping the DB clean.

    Status: Working tree is CLEAN (Git Commit: 1ed166a).

🚀 3. THE FUTURE PATH (THE BOUTIQUE UPGRADE)
Task	Description	Priority
Big Bug Squash	[Insert specific details of the newly found bug here]	IMMEDIATE
Responsive Shield	Fix layout breakage on high-font-scale devices (Wife's Phone) using maxLines=1 and LocalDensity clamping.	HIGH
Silent Vault	Re-implement Google Drive sync using the App Data Folder API.	HIGH
Boutique Reveal	Implement a flicker-free "Soft Pulse" entry transition (One-Shot only).	MEDIUM
⚠️ 4. DEVELOPER GUARDRAILS (FOR THE NEXT AGENT)

    NO SCHEMA CHANGES: Do not touch AssetEntity.kt without a version bump. Use the "Suitcase" for UI-only display strings.

    PRECISION LOCK: Always use the 2/5/8 decimal tier system for price displays.

    WIDGET HITBOXES: Only the Swan (top-left) and Pencil (top-right) are reactive. The body of the widget is a "View Only" zone.

    FULL FILE OUTPUTS ONLY: Do not provide snippets.