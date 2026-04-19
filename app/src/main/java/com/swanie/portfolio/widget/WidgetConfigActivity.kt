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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.ui.settings.SettingsViewModel
import com.swanie.portfolio.ui.settings.ThemeViewModel
import com.swanie.portfolio.ui.settings.WidgetManagerScreen
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var vaultDao: VaultDao
    
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    private var appWidgetId by mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setResult(Activity.RESULT_CANCELED)
        handleIntent(intent)

        setContent {
            val siteBgColorHex by themeViewModel.siteBackgroundColor.collectAsState()
            val siteBgColor = try { Color(siteBgColorHex.toColorInt()) } catch (e: Exception) { Color(0xFF000416) }

            SwaniesPortfolioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = siteBgColor
                ) {
                    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        WidgetManagerScreen(
                            isConfigMode = true,
                            configAppWidgetId = appWidgetId,
                            onConfigComplete = {
                                val resultValue = Intent().apply {
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                    // 🛡️ Ensure the data URI is mirrored in the result
                                    data = Uri.parse("swanie://widget/$appWidgetId/${System.currentTimeMillis()}")
                                }
                                setResult(Activity.RESULT_OK, resultValue)
                                finish()
                            },
                            onBack = {
                                finish()
                            }
                        )
                    } else {
                        // Error fallback
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Invalid Widget ID", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 🚀 THE LOCK: Explicitly set the new intent and force re-processing
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // 🔍 PRIMARY: URI Path Segment (swanie://widget/ID/TIMESTAMP)
        // This bypasses Intent Extra conflation entirely.
        val idFromUri = intent.data?.lastPathSegment?.toIntOrNull() ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // 🔍 SECONDARY: Standard Extra
        val idFromExtra = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        
        val resolvedId = if (idFromUri != AppWidgetManager.INVALID_APPWIDGET_ID) idFromUri else idFromExtra
        
        Log.d("SWANIE", "Activity Created with ID: $resolvedId (URI: $idFromUri, Extra: $idFromExtra)")
        
        if (resolvedId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e("SWANIE_WIDGET", "No valid AppWidgetId found in intent/URI")
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()
        } else {
            // 🚀 THE SINGULARITY: Force state reset in the UI by toggling the ID
            appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            appWidgetId = resolvedId

            // 🚀 THE SMART RESOLVER: Resolve the appWidgetId to its vault and flicker
            settingsViewModel.forceVaultSwitch(resolvedId, isAppWidgetId = true)
        }
    }
}
