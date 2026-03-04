📄 BROWSER_CONTEXT_NARRATIVE.md
1. Project Overview

   App Name: Swanie's Portfolio

   Purpose: Professional-grade Crypto & Precious Metals tracking with a high-end, user-curated visual theme.

   Current Branch: main (Personal Vault & Unified Funnel Logic Locked)

   Tech Stack: Kotlin, Jetpack Compose, Hilt, Room, Retrofit, StateFlow, DataStore.

2. Architectural Status

Status: UNIFIED FUNNEL & PERSONAL VAULT ENABLED

    The Personal Vault: Assets can now be designated as "True Custom." This allows for manual valuation of non-tracked items (watches, heirlooms, collectibles) using a manual price-per-unit engine while still contributing to the total portfolio balance.

    Unified Funnel System: Add and Edit flows are now merged into a single multi-step Dialog system. This solved the "Keyboard Obstruction" issue by centering inputs and focus, providing a consistent UX across the app.

    Hero Visuals (Swan Logic): Implemented a high-priority rendering check for custom assets. If no photo is uploaded, a solid white, 1.5x scaled Swan logo is forced as the hero icon to maintain branding.

    Themed Confirmation Engine: The "Are You Sure" deletion safety switch is now fully wired. It respects the user's confirmDelete preference and dynamically pulls colors from the active cardBg and cardText theme choices.

3. Feature Map & UI Status

🟢 Completed & Locked

    True Custom Branching: Funnel logic now successfully maps "Icon Name" to symbol and "Description Lines" to center-card text.

    Photo Integration: Support for local URI icon uploads with circular cropping and themed borders.

    Themed Safety Switches: Deletion popups follow user-defined styling and toggle on/off via Settings.

    Zero-State Inputs: Custom funnel fields now start empty with greyed-out placeholders for faster, friction-free data entry.

    Segmented Tab Totals: ALL, CRYPTO, and METAL tabs accurately filter the header's total valuation.

🔴 Upcoming Features

    Interactive Donut Deep-Dive: Enabling legend-clicks on the Analytics screen to trigger the asset detail overlay.

    Premium Logic Toggles: A switch to toggle Premium entry between "Total Amount" and "Per Ounce/Unit."

4. Key Logic Snippets (The Build-Savers)
   Kotlin

// 1. HERO SWAN PRIORITY
// Forces the Swan logo to 1.5x scale if "SWAN_DEFAULT" flag is present
if (imageUrl == "SWAN_DEFAULT") {
Box(modifier = Modifier.size((size * 1.2).dp).clip(CircleShape)) {
Image(painter = painterResource(R.drawable.swanie_foreground),
modifier = Modifier.fillMaxSize().scale(1.5f))
}
}

// 2. THEMED SAFETY SWITCH
// Checks MainViewModel preference before launching the popup
if (isOverTrash.value) {
if (confirmDeleteSetting) {
assetPendingDeletion = asset
} else {
viewModel.deleteAsset(asset)
}
}

// 3. TRUE CUSTOM MAPPING
// funnelMetal -> icon label | line1 + line2 -> center card text
val displaySymbol = if(isTrueCust) funnelMetal.uppercase() else "CUST"
val finalName = if(isTrueCust) "$line1\n$line2" else standardName

🛡️ Narrative Synchronized
The app now functions as a comprehensive wealth vault. It correctly handles identity mapping for custom items and enforces a high-end visual hierarchy regardless of the user's data choice.

🎯 Next Steps (Future Session)

    Donut Interaction: Wire the Analytics screen legend to the holdings detail.

    Premium Toggle: Implement the Unit vs. Total premium math.