package com.swanie.portfolio.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class PortfolioWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PortfolioWidget()
    
    // 🛡️ Multi-Instance Binding Protection:
    // We rely on standard GlanceAppWidgetReceiver behavior. 
    // The specific VAULT_ID_KEY is stored in the widget's local Preferences 
    // and is not affected by global update broadcasts unless explicitly overwritten.
}
