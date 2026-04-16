package com.swanie.portfolio.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class WidgetClickCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 🛡️ SYNC FIX: Match the "widgetId" key sent from PortfolioWidget.kt
        val widgetId = parameters[PortfolioWidget.WIDGET_ID_KEY] ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val intent = Intent(context, WidgetConfigActivity::class.java).apply {
                // 🛡️ THE RELAY SHIELD: Unique URI prevents Intent conflation at the OS level
                data = Uri.parse("swanie://relayed/id/$widgetId/${System.currentTimeMillis()}")
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }
}