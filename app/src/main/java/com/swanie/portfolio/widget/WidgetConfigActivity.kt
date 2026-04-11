package com.swanie.portfolio.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.*
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.local.VaultEntity
import com.swanie.portfolio.ui.theme.SwaniesPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var vaultDao: VaultDao

    @Inject
    lateinit var assetDao: AssetDao

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            var vaults by remember { mutableStateOf<List<VaultEntity>>(emptyList()) }
            var selectedVaultName by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                vaults = vaultDao.getAllVaultsFlow().first()
            }

            SwaniesPortfolioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF000416)
                ) {
                    if (selectedVaultName == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SELECT VAULT",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color(0xFFFFD700),
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
                                                selectedVaultName = vault.name
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        SuccessUI(selectedVaultName!!) {
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SuccessUI(vaultName: String, onDismiss: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF00FF00),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Linked to $vaultName!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Now, open the App > Settings > Widget Manager to pick your assets and colors.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("GOT IT", color = Color.Black, fontWeight = FontWeight.Bold)
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
            val vault = vaultDao.getVaultById(vaultId) ?: return
            val assets = assetDao.getAssetsByVault(vaultId).first()
            
            var totalValue = 0.0
            assets.forEach { asset ->
                totalValue += (asset.officialSpotPrice * asset.weight * asset.amountHeld) + asset.premium
            }
            val formattedTotal = NumberFormat.getCurrencyInstance(Locale.US).format(totalValue)

            val glanceManager = GlanceAppWidgetManager(context)
            val glanceId = glanceManager.getGlanceIdBy(appWidgetId)

            Log.d("SWANIE_LINK", "Initial placement link for ID: $appWidgetId to Vault: $vaultId")

            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[PortfolioWidget.VAULT_ID_KEY] = vaultId
                    this[PortfolioWidget.STATIC_VAULT_NAME_KEY] = vault.name
                    this[PortfolioWidget.STATIC_TOTAL_BALANCE_KEY] = formattedTotal
                }.toPreferences()
            }

            // 🚀 SYNCHRONIZED HANDSHAKE: Write to Options for instant OS recognition
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val options = Bundle().apply {
                putInt("vault_id", vaultId)
                putString("static_vault_name", vault.name)
                putString("static_total_balance", formattedTotal)
                putString("static_bg_color", vault.widgetBgColor)
                putString("static_bg_text_color", vault.widgetBgTextColor)
                putString("static_card_color", vault.widgetCardColor)
                putString("static_card_text_color", vault.widgetCardTextColor)
            }
            appWidgetManager.updateAppWidgetOptions(appWidgetId, options)

            // 🛠️ THE IDENTITY GLUE: Bind appWidgetId to Vault in DB
            vaultDao.updateBoundAppWidgetId(vaultId, appWidgetId)

            // 📢 MANUAL BROADCAST RESET: Force the OS to acknowledge the update immediately
            val updateIntent = Intent(context, PortfolioWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            context.sendBroadcast(updateIntent)

            val workRequest = OneTimeWorkRequestBuilder<WidgetSyncWorker>()
                .setInputData(workDataOf("force_vault_id" to vaultId))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)

            PortfolioWidget().update(context, glanceId)
        } catch (e: Exception) {
            Log.e("SWANIE_WIDGET", "Failed to bind widget state during configuration", e)
        }
    }
}