package com.swanie.portfolio.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import com.swanie.portfolio.R
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.swanie.portfolio.MainActivity
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.settings.SettingsViewModel
import com.swanie.portfolio.ui.settings.ThemeViewModel
import com.swanie.portfolio.ui.settings.WidgetManagerScreen
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Opens only from the home widget (pencil) with a valid [AppWidgetManager.EXTRA_APPWIDGET_ID].
 * No intent-filters; not exported — see [AndroidManifest.xml].
 */
@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val assetViewModel: AssetViewModel by viewModels()

    private var appWidgetId by mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.widget_manager_title)
        setResult(Activity.RESULT_CANCELED)

        if (!applyIntent(intent)) {
            Log.w(TAG, "finish: missing or invalid appWidgetId")
            finish()
            return
        }

        setContent {
            val scope = rememberCoroutineScope()
            val siteBgColorHex by themeViewModel.siteBackgroundColor.collectAsState()
            val siteTextColorHex by themeViewModel.siteTextColor.collectAsState()
            val isProUser by settingsViewModel.isProUser.collectAsState()
            val targetVaultId by settingsViewModel.targetVaultId.collectAsState()
            val targetVaultAssets by settingsViewModel.targetVaultAssets.collectAsState()
            val siteBgColor = try {
                Color(siteBgColorHex.toColorInt())
            } catch (e: Exception) {
                Color(0xFF000416)
            }

            SwaniesPortfolioTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = siteBgColor) {
                    if (isProUser) {
                        WidgetManagerScreen(
                            isConfigMode = true,
                            configAppWidgetId = appWidgetId,
                            onConfigComplete = {
                                val resultValue = Intent().apply {
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                    data = "swanie://widget/$appWidgetId/${System.currentTimeMillis()}".toUri()
                                }
                                setResult(Activity.RESULT_OK, resultValue)

                                AppWidgetManager.getInstance(this@WidgetConfigActivity)
                                    .notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_root)

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
                    } else {
                        val textColor = try {
                            Color(siteTextColorHex.toColorInt())
                        } catch (e: Exception) {
                            Color.White
                        }
                        var isApplyingFree by remember { mutableStateOf(false) }
                        if (isApplyingFree) {
                            androidx.compose.runtime.LaunchedEffect(targetVaultId, targetVaultAssets) {
                                val currentVaultId = targetVaultId
                                if (currentVaultId <= 0) return@LaunchedEffect
                                val selectedIds = targetVaultAssets.take(3).map { it.coinId }
                                assetViewModel.saveWidgetConfiguration(
                                    portfolioVaultId = currentVaultId,
                                    appWidgetId = appWidgetId,
                                    selectedIds = selectedIds
                                ) {
                                    scope.launch {
                                        settingsViewModel.saveWidgetAppearance(
                                            currentVaultId,
                                            "#1C1C1E",
                                            "#FFFFFF",
                                            "#2C2C2E",
                                            "#FFFFFF"
                                        )
                                        settingsViewModel.updateShowWidgetTotal(currentVaultId, true)
                                        settingsViewModel.getVaultById(currentVaultId)
                                        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                            settingsViewModel.forceImmediateRemoteViewsUpdate(currentVaultId, appWidgetId)
                                        }
                                        setWidgetResultOk()
                                        finishAndRemoveTask()
                                    }
                                }
                            }
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = textColor, strokeWidth = 2.dp)
                                    Text(
                                        text = stringResource(R.string.widget_loading),
                                        color = textColor,
                                        modifier = Modifier.padding(top = 10.dp)
                                    )
                                }
                            }
                        } else {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.Black,
                                    border = BorderStroke(1.dp, Color(0x55FFD54F)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    androidx.compose.foundation.layout.Column(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.widget_pro_customization_title),
                                            color = Color(0xFFFFD54F),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = stringResource(R.string.widget_pro_customization_body),
                                            color = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.padding(top = 8.dp),
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = {
                                                startActivity(Intent(this@WidgetConfigActivity, MainActivity::class.java).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                })
                                                finishAndRemoveTask()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black)
                                        ) {
                                            Text(stringResource(R.string.widget_upgrade_to_pro), fontWeight = FontWeight.Black)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { isApplyingFree = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(46.dp),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                        ) {
                                            Text(stringResource(R.string.widget_continue_with_free), color = Color.White)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
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

    private fun setWidgetResultOk() {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = "swanie://widget/$appWidgetId/${System.currentTimeMillis()}".toUri()
        }
        setResult(Activity.RESULT_OK, resultValue)
    }
}
