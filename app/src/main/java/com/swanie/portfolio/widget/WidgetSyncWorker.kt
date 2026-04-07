package com.swanie.portfolio.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class WidgetSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetWorkerEntryPoint {
        fun assetRepository(): AssetRepository
        fun themePreferences(): ThemePreferences
    }

    override suspend fun doWork(): Result {
        Log.d("SWANIE_TRACE", "8. Worker waking up for Multi-Instance Sync")
        
        return try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(PortfolioWidget::class.java)
            val entryPoint = EntryPointAccessors.fromApplication(context, WidgetWorkerEntryPoint::class.java)
            
            val refreshedVaultIds = mutableSetOf<Int>()

            glanceIds.forEach { id ->
                // 🛡️ Read the bound vault ID for this specific widget instance
                val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
                val boundVaultId = prefs[PortfolioWidget.VAULT_ID_KEY]
                
                // Fallback to the global active vault if no binding exists
                val vaultIdToRefresh = boundVaultId ?: entryPoint.themePreferences().currentVaultId.first()
                
                // 🛡️ Deduplication: Only refresh each unique vault once per worker run
                if (!refreshedVaultIds.contains(vaultIdToRefresh)) {
                    Log.d("SWANIE_TRACE", "Refreshing Bound Vault ID: $vaultIdToRefresh")
                    entryPoint.assetRepository().refreshAssets(force = true, portfolioId = vaultIdToRefresh.toString())
                    refreshedVaultIds.add(vaultIdToRefresh)
                }
                
                // Trigger a redraw for this specific widget
                PortfolioWidget().update(context, id)
            }

            // Legacy broadcast to ensure OS wakes up (Optional but kept for stability)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, PortfolioWidgetReceiver::class.java))
            
            if (ids != null && ids.isNotEmpty()) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = ComponentName(context, PortfolioWidgetReceiver::class.java)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                }
                context.sendBroadcast(intent)
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SWANIE_TRACE", "Multi-Instance Worker failed", e)
            Result.retry()
        }
    }
}
