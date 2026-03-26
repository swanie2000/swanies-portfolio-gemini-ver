package com.swanie.portfolio.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SWANIE_TRACE", "8. Worker waking up to finalize sync")
        
        return try {
            // 1. Force Glance redraw
            PortfolioWidget().updateAll(applicationContext)

            // 2. Legacy broadcast to ensure OS wakes up
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, PortfolioWidgetReceiver::class.java))
            
            if (ids != null && ids.isNotEmpty()) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = ComponentName(applicationContext, PortfolioWidgetReceiver::class.java)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                }
                applicationContext.sendBroadcast(intent)
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SWANIE_TRACE", "Worker failed", e)
            Result.retry()
        }
    }
}
