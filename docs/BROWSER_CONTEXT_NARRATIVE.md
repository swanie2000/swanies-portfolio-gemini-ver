UPDATED MASTER DOCUMENT: SOVEREIGN SHIELD (V12.0.1)
🎯 THE CORE MISSION

To maintain a high-performance, multi-instance portfolio tracker where Widgets and the Main App operate as independent, data-isolated entities. Every portfolio (Swanie 1-5) must possess its own unique identity, appearance, and privacy settings with hardware-verified targeting.
🛡️ 1. ARCHITECTURAL RECAP (THE SYNCED BASELINE)

    The Registration Lock (V11.9.7): Hard-links appWidgetId to a specific VaultEntity in Room (v26).

    The URI-First Identity: Uses Data URIs (swanie://relayed/id/$id/$timestamp) to neutralize Intent Conflation.

    The Three-Zone Layout: Locked Proportional Trinity (Identity Left, Pulse Center, Data Right).

    The OAuth Handshake (V12.0.0): Transitioned to a verified Web Client ID architecture within the google-services.json to enable secure Google Drive appdata sync for Eun and Mom.

✅ 2. TODAY’S VICTORIES (THE OAUTH GAUNTLET)

    The Identity Fix: Manually generated the missing Web Application Client ID in Google Cloud Console, resolving the oauth_client: [] empty array bug.

    Fingerprint Alignment: Successfully synchronized the local development SHA-1 with the Firebase/Google Cloud production environment.

    VIP Whitelisting: Explicitly authorized eun.oh70@gmail.com and other family testers in the OAuth Audience settings to bypass "Unverified App" blocks.

    The Debug Auto-Unlock: Implemented a temporary logic bypass in AuthViewModel.kt that forces AuthState.Authenticated upon a successful Google handshake, ensuring the "Loop" is dead.

⚠️ 3. THE NEXT TRENCHES (PICKING UP TOMORROW)
Task	Description	Status
The Asset Stamp	Serialize Top 5 assets into DataStore "Handshake" for instant rendering.	IMMEDIATE PRIORITY
Login Shield	Re-implement the Vault Password layer once the Google Drive sync is verified.	HIGH PRIORITY
Registry Cleanup	Re-verify that the newly minted google-services.json is survived by the "Elephant" Gradle sync.	PENDING
Living Preview	Sync the internal Settings Preview to reflect "Handshake" data 1:1.	PENDING
⚠️ 4. DEVELOPER GUARDRAILS (FOR THE AGENT)

    CRITICAL: IDENTITY INTEGRITY: Do not revert the google-services.json. The client_type: 3 entry is the only thing keeping the "Access Blocked" bouncer away.

    THE BYPASS NOTICE: The current AuthViewModel.kt contains a Debug Auto-Unlock. This is intentional for the next 24 hours to ensure connectivity is stable before re-locking the vault.

    FULL FILE OUTPUTS: No partial snippets. Provide entire files to maintain context and avoid breaking the newly established OAuth handshake.

🚀 Next Agent Command

    "I have updated the narrative to V12.0.1. The OAuth Handshake is fixed and the 'Access Blocked' error is dead. We are currently in Debug Auto-Unlock mode. Task 1: Implement the Asset Stamp. Update SettingsViewModel.kt to serialize Top 5 asset names and prices into the PreferencesGlanceStateDefinition during the save. Task 2: Update PortfolioWidget.kt to render these cached assets immediately for instant visual feedback. Task 3: Verify the default_web_client_id resource is still resolving. Provide full files. Confirm 'KEEP ALL'."