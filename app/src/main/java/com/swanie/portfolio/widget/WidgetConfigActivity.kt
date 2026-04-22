package com.swanie.portfolio.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.swanie.portfolio.ui.settings.SettingsViewModel
import com.swanie.portfolio.ui.settings.ThemeViewModel
import com.swanie.portfolio.ui.settings.WidgetManagerScreen
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Opens only from the home widget (pencil) with a valid [AppWidgetManager.EXTRA_APPWIDGET_ID].
 * No intent-filters; not exported — see [AndroidManifest.xml].
 */
@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    private var appWidgetId by mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Widget Manager"
        setResult(Activity.RESULT_CANCELED)

        if (!applyIntent(intent)) {
            Log.w(TAG, "finish: missing or invalid appWidgetId")
            finish()
            return
        }

        setContent {
            val siteBgColorHex by themeViewModel.siteBackgroundColor.collectAsState()
            val siteBgColor = try {
                Color(siteBgColorHex.toColorInt())
            } catch (e: Exception) {
                Color(0xFF000416)
            }

            SwaniesPortfolioTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = siteBgColor) {
                    WidgetManagerScreen(
                        isConfigMode = true,
                        configAppWidgetId = appWidgetId,
                        onConfigComplete = {
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                data = Uri.parse("swanie://widget/$appWidgetId/${System.currentTimeMillis()}")
                            }
                            setResult(Activity.RESULT_OK, resultValue)

                            sendBroadcast(
                                Intent(this@WidgetConfigActivity, PortfolioWidgetReceiver::class.java).apply {
                                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                                },
                            )

                            finishAndRemoveTask()
                        },
                        onBack = { finishAndRemoveTask() },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (!applyIntent(intent)) {
            finish()
        }
    }

    private fun applyIntent(intent: Intent): Boolean {
        val resolved = resolveAppWidgetId(intent)
        if (resolved == AppWidgetManager.INVALID_APPWIDGET_ID) return false
        appWidgetId = resolved
        settingsViewModel.forceVaultSwitch(resolved, isAppWidgetId = true)
        return true
    }

    private fun resolveAppWidgetId(intent: Intent): Int {
        val fromExtra = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (fromExtra != AppWidgetManager.INVALID_APPWIDGET_ID) return fromExtra

        // Fallback: Glance relay URI swanie://relayed/id/{widgetId}/… (extra is authoritative when present)
        val segments = intent.data?.pathSegments ?: return AppWidgetManager.INVALID_APPWIDGET_ID
        val idIdx = segments.indexOf("id")
        if (idIdx >= 0 && idIdx + 1 < segments.size) {
            return segments[idIdx + 1].toIntOrNull() ?: AppWidgetManager.INVALID_APPWIDGET_ID
        }
        return AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private companion object {
        private const val TAG = "WidgetConfigActivity"
    }
}
