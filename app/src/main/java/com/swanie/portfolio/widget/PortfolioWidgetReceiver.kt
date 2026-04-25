package com.swanie.portfolio.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PortfolioWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PortfolioWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            Log.d("SWANIE_WIDGET", "ACTION_SCREEN_ON received, forcing widget refresh")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    PortfolioWidget().updateAll(context.applicationContext)
                } catch (e: Exception) {
                    Log.e("SWANIE_WIDGET", "Screen-on refresh failed", e)
                }
            }
        }
    }
}
