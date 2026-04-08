package com.swanie.portfolio.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.*
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var vaultDao: VaultDao

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🛡️ DEFAULT: Set result to CANCELED. This ensures that if the user backs out,
        // the widget isn't placed in an unconfigured state.
        setResult(Activity.RESULT_CANCELED)

        // 🛡️ HANDSHAKE: Retrieve the appWidgetId directly from the intent.
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        // If this activity was started with an invalid widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e("SWANIE_WIDGET", "Invalid AppWidgetId received in ConfigActivity")
            finish()
            return
        }

        setContent {
            var vaults by remember { mutableStateOf<List<VaultEntity>>(emptyList()) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                vaults = vaultDao.getAllVaultsFlow().first()
            }

            SwaniesPortfolioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF000416) // SwanieNavy background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SELECT VAULT",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color(0xFFFFD700), // Gold/Yellow accent
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        )
                        
                        Text(
                            text = "Choose which portfolio to track on this widget.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )

                        if (vaults.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFFFD700))
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(vaults) { vault ->
                                    VaultSelectionCard(vault) {
                                        scope.launch {
                                            handleVaultSelection(vault.id)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun VaultSelectionCard(vault: VaultEntity, onSelect: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = vault.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Base: ${vault.baseCurrency}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFFFD700)
                )
            }
        }
    }

    private suspend fun handleVaultSelection(vaultId: Int) {
        val context = this@WidgetConfigActivity
        
        try {
            val glanceManager = GlanceAppWidgetManager(context)
            val glanceId = glanceManager.getGlanceIdBy(appWidgetId)

            Log.d("SWANIE_WIDGET", "Binding Vault ID $vaultId to AppWidgetId $appWidgetId (GlanceId: $glanceId)")

            // 🛡️ SOVEREIGN SHIELD: Save the VAULT_ID_KEY for this specific glanceId
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[PortfolioWidget.VAULT_ID_KEY] = vaultId
                }.toPreferences()
            }

            // 🛡️ COLD START FIX: Trigger immediate sync for this vault
            val workRequest = OneTimeWorkRequestBuilder<WidgetSyncWorker>()
                .setInputData(workDataOf("force_vault_id" to vaultId))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)

            // Trigger the first render immediately
            PortfolioWidget().update(context, glanceId)

            // 🛡️ THE HANDSHAKE: Pass back the original appWidgetId and set RESULT_OK
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        } catch (e: Exception) {
            Log.e("SWANIE_WIDGET", "Failed to bind widget state during configuration", e)
            finish()
        }
    }
}
