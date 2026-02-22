Project Narrative: Swanie’s Portfolio Development
I. Current State & Recent Technical Wins

The project is currently at a stable, high-performance baseline following a successful "Git Reset" to a known-good state. The architecture now supports a professional-grade customization engine.

    Stable HSV Color Engine: Implemented a non-reactive sliding logic that separates Hue, Saturation, and Value states. This eliminated "slider jumping" and UI stuttering by decoupling the movement from the database write.

    The "Stage & Commit" Pattern: Introduced a Color Preview Box and Manual Hex Input. Users can fine-tune their brand colors in real-time with an instant visual feedback loop, only "committing" the change to the app-wide theme via the Apply button.

    Brand Recovery: The "Default" button is now a high-priority UX feature, instantly restoring the signature Swanie Navy (#000416) across the entire application in a single click.

    Version Control Discipline: The repository is currently clean at commit c7aa3a8, serving as the "Stable Master" for all future UI experiments.

II. The "Tonal Gradient" Roadmap

The next phase of development focuses on moving from flat colors to dynamic depth. The goal is to implement an optional vertical gradient that derives its light/dark tones mathematically from the user's selected primary color.

    Mathematical Depth: Utilizing HSV "Value" shifts (±15%) to create top and bottom anchors for a Brush.verticalGradient.

    Architectural Robustness: To prevent previous compilation failures, the app will transition to a Universal Brush Architecture. This means both solid colors and gradients will be rendered using a Brush object, ensuring the Kotlin compiler has a consistent parameter type.

    User Control: A new isGradientEnabled preference will be added to the DataStore, toggled via a professional-style Switch in the Settings menu.

III. Build & Safety Standards

    Named Parameters: All background modifiers must explicitly use named parameters (e.g., Modifier.background(brush = ...) or Modifier.background(color = ...)) to avoid function overloading ambiguity.

    Input Validation: Hex strings must be strictly validated for a length of 6 and valid hexadecimal characters before parsing to prevent StringIndexOutOfBoundsException.

How to use this file

You can keep this in a docs/NARRATIVE.md file in your project. It acts as the "Source of Truth" for your AI Agent. If the Agent ever gets lost again, you can paste this narrative to remind it exactly what the architectural rules and current progress are.