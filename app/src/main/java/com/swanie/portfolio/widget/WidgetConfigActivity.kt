package com.swanie.portfolio.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.ui.settings.WidgetManagerScreen
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var vaultDao: VaultDao

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e("SWANIE_WIDGET", "Invalid AppWidgetId received in ConfigActivity")
            finish()
            return
        }

        setContent {
            SwaniesPortfolioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF000416)
                ) {
                    WidgetManagerScreen(
                        isConfigMode = true,
                        configAppWidgetId = appWidgetId,
                        onConfigComplete = {
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)
                            // 🚀 THE SNAP: Finish and remove from recents immediately
                            finishAndRemoveTask()
                        },
                        onBack = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}
