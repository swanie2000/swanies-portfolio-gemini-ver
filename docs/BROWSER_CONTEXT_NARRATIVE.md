Updated Project Narrative: March 14, 2026
✅ Current Working State (Safe Harbor)

    UI Firewall: AssetCards.kt is the centralized "factory" for all card types (Full, Compact, and Metal Market). This isolates design from logic.

    API Silence: The "Machine Gun" loop is dead. The Metals screen now uses a One-Time-Fetch protocol (LaunchedEffect(Unit)), ensuring it only hits the API once upon entry.

    Data Accuracy: Yahoo Finance Day High and Day Low mapping is fully functional and visible on the UI.

    Performance: The app is snappy with zero recomposition lag during drag-and-drop or scrolling.

🛠️ Next Objectives (The "Zero-Ghost" Audit)

    Add Asset Validation: Test the "Add Asset" screen to ensure new assets land with full price data. We need to verify that adding a new item doesn't trigger a recursive sync loop.

    Leaky Bucket Implementation: Now that the "Bad Actors" are gone, transition the repository to a Leaky Bucket model to allow for respectable burst hits (like adding 3 assets in a row) while maintaining long-term API safety.

    UI Feedback: Add the 10-second "Refresh" button lockout and gray-out effect to manage user expectations.

🧪 Testing the "Add Asset" Screen

This is a critical test. When you add a new asset, the app usually has to:

    Save the new item to the Database.

    Fetch the initial price for that specific item.

    Update the UI.

The Danger Zone: If the "Add" logic triggers a refreshAll(), it might kick off the loop we just killed.