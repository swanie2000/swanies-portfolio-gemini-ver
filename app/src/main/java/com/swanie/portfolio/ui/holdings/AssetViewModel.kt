package com.swanie.portfolio.ui.holdings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.PriceHistoryDao
import com.swanie.portfolio.data.local.VaultDao
import com.swanie.portfolio.data.repository.*
import com.swanie.portfolio.data.remote.GoogleDriveService // 🛰️ Essential Import
import com.swanie.portfolio.widget.PortfolioWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val syncCoordinator: DataSyncCoordinator,
    private val searchRegistry: SearchEngineRegistry,
    private val themePreferences: ThemePreferences,
    private val assetDao: AssetDao,
    private val vaultDao: VaultDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val googleDriveService: GoogleDriveService, // 🛰️ Cloud Service
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("portfolio_prefs", Context.MODE_PRIVATE)
    private val _confirmDelete = MutableStateFlow<AssetEntity?>(null)
    val confirmDelete: StateFlow<AssetEntity?> = _confirmDelete.asStateFlow()
    private val _widgetSelectionVaultId = MutableStateFlow<Int?>(null)
    private val _vaultAssets = MutableStateFlow<List<AssetEntity>>(emptyList())
    private val _isUpdating = MutableStateFlow(false)

    // 🌐 GLOBAL VISTA: Track Current Vault
    // Task 2: Instant Data Handshake - Use runBlocking for the absolute first state to avoid "Vault 1" flicker
    val currentVaultId = themePreferences.currentVaultId.stateIn(
        viewModelScope, 
        SharingStarted.Eagerly, 
        runBlocking { themePreferences.currentVaultId.first() }
    )

    // 🌐 GLOBAL VISTA: Filter holdings by currentVaultId
    // Ensure we emit the Room flow immediately. The skeleton is handled by the initial null value.
    val holdings: StateFlow<List<AssetEntity>?> = currentVaultId
        .flatMapLatest { id ->
            assetDao.getAssetsByVault(id)
        }
        .onEach { Log.d("VM_TRACE", "UI observing ${it.size} assets for vault ${currentVaultId.value}") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            currentVaultId
                .drop(1)
                .collect {
                    clearVaultState()
                }
        }
        viewModelScope.launch {
            holdings.collect { latest ->
                _vaultAssets.value = latest ?: emptyList()
            }
        }
    }

    // Keep widget state vault-scoped to match SettingsViewModel contract.
    // Can be overridden by screens that edit a vault different from currentVaultId.
    private val widgetContractVaultId: StateFlow<Int> = combine(
        _widgetSelectionVaultId,
        currentVaultId
    ) { overrideId, currentId ->
        overrideId ?: currentId
    }.stateIn(viewModelScope, SharingStarted.Eagerly, currentVaultId.value)

    val widgetSelectedAssetIds: StateFlow<List<String>> = combine(
        widgetContractVaultId,
        vaultDao.getAllVaultsFlow()
    ) { vaultId, vaults ->
        vaults.find { it.id == vaultId }
            ?.selectedWidgetAssets
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val widgetShowTotal: StateFlow<Boolean> = combine(
        widgetContractVaultId,
        vaultDao.getAllVaultsFlow()
    ) { vaultId, vaults ->
        vaults.find { it.id == vaultId }?.showWidgetTotal ?: true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setWidgetSelectionVaultId(vaultId: Int?) {
        _widgetSelectionVaultId.value = vaultId?.takeIf { it > 0 }
    }

    fun clearVaultState() {
        _vaultAssets.value = emptyList()
    }

    /**
     * 🛰️ INTERNAL HELPER: Pushes the current vault state to Google Drive.
     * Triggered after any local database modification.
     * 
     * REVISION V7.5.0: Now fetches ALL assets across ALL vaults to ensure 
     * the cloud backup is a complete mirror of the local database.
     */
    private fun triggerCloudSync() {
        viewModelScope.launch {
            try {
                // Fetch EVERY asset from the DB (Global Capture)
                val allAssets = assetDao.getAllAssetsGlobal()
                if (allAssets.isNotEmpty()) {
                    val success = googleDriveService.uploadFullVaultBackup(allAssets)
                    if (success) {
                        Log.d("VAULT_DEBUG", "Heartbeat: Cloud Sync Successful (${allAssets.size} assets)")
                    } else {
                        Log.w("VAULT_DEBUG", "Heartbeat: Cloud Sync Skipped or Failed.")
                    }
                } else {
                    Log.d("VAULT_DEBUG", "Heartbeat: No assets to sync.")
                }
            } catch (e: Exception) {
                Log.e("VAULT_DEBUG", "Heartbeat: Auto-sync error", e)
            }
        }
    }

    val isRefreshing: StateFlow<Boolean> = syncCoordinator.syncStatus
        .map { it is SyncStatus.Syncing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val remainingCooldown: StateFlow<Int> = flow {
        while (true) {
            emit(syncCoordinator.getRemainingCooldown())
            delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedProvider = MutableStateFlow<String?>(null)
    val selectedProvider: StateFlow<String?> = _selectedProvider.asStateFlow()

    private val _isSearchBusy = MutableStateFlow(false)
    val isSearchBusy: StateFlow<Boolean> = _isSearchBusy.asStateFlow()

    fun getAvailableProviders(): List<String> = searchRegistry.getAvailableProviders()

    fun selectProvider(name: String) {
        _selectedProvider.value = name
        _searchQuery.value = ""
    }

    val searchResults: StateFlow<List<AssetEntity>> =
        combine(_searchQuery, _selectedProvider) { query, provider ->
            query to provider
        }
            .debounce(700)
            .distinctUntilChanged()
            .flatMapLatest { (query, provider) ->
                val cleanQuery = query.trim()
                if (cleanQuery.length < 2 || provider == null) {
                    _isSearchBusy.value = false
                    flowOf(emptyList())
                } else {
                    flow {
                        _isSearchBusy.value = true
                        try {
                            val searchProvider = searchRegistry.getProvider(provider)
                                ?: searchRegistry.getDefaultProvider()
                            val results = searchProvider.search(cleanQuery)
                            emit(results.map { it.toAssetEntity().copy(portfolioId = "MAIN", vaultId = currentVaultId.value) })
                        } catch (e: Exception) {
                            Log.e("SEARCH_ERROR", "Search failed: ${e.message}")
                            emit(emptyList())
                        } finally {
                            _isSearchBusy.value = false
                        }
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun searchCoins(query: String) {
        _searchQuery.value = query
    }

    suspend fun fetchMarketPriceData(symbol: String): MarketPriceData =
        repository.fetchMarketPrice(symbol)

    suspend fun healMetadata(asset: AssetEntity): AssetEntity = repository.healMetadata(asset)

    fun refreshAssets() {
        viewModelScope.launch { repository.refreshAssets() }
    }

    fun refreshMarketWatch() {
        viewModelScope.launch { repository.refreshMarketWatch() }
    }

    fun performSurgicalAdd(asset: AssetEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            val taggedAsset = asset.copy(vaultId = currentVaultId.value)
            repository.executeSurgicalAdd(taggedAsset) { _, _ -> }
            triggerCloudSync() // 🛰️ Backup to Cloud
            onComplete()
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
            triggerCloudSync() // 🛰️ Sync Delete
        }
    }

    fun requestDeleteConfirmation(asset: AssetEntity) {
        _confirmDelete.value = asset
    }

    fun clearDeleteConfirmation() {
        _confirmDelete.value = null
    }

    fun updateAssetOrder(assets: List<AssetEntity>) {
        viewModelScope.launch {
            repository.updateAssetOrder(assets)
            triggerCloudSync() // 🛰️ Sync Order
        }
    }

    fun toggleWidgetVisibility(asset: AssetEntity) {
        // Backward-compatible shim: unified widget selection now lives on VaultEntity.
        toggleAssetInWidgetSelection(asset)
    }

    fun updateWidgetSelectionForCurrentVault(selectedIds: List<String>) {
        if (_isUpdating.value) return
        viewModelScope.launch(Dispatchers.IO) { persistWidgetSelectionOrderForCurrentVault(selectedIds) }
    }

    /**
     * Same as [updateWidgetSelectionForCurrentVault] but suspends until the DB write and widget push finish.
     * Returns false if normalization produced nothing to write or the vault id was invalid.
     */
    suspend fun persistWidgetSelectionOrderForCurrentVault(selectedIds: List<String>): Boolean {
        if (_isUpdating.value) return false
        val vaultId = widgetContractVaultId.value
        if (vaultId <= 0) return false
        val normalized = selectedIds
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(5)
        _isUpdating.value = true
        return try {
            val persisted = withTimeoutOrNull(3000L) {
                withContext(Dispatchers.IO) {
                    vaultDao.updateSelectedWidgetAssets(vaultId, normalized.joinToString(","))
                    repository.pushAssetsToWidget(context, vaultId.toString())
                }
                true
            }
            persisted == true
        } finally {
            _isUpdating.value = false
        }
    }

    fun toggleAssetInWidgetSelection(asset: AssetEntity, maxSelected: Int = 5) {
        if (_isUpdating.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isUpdating.value = true
            try {
                val currentIds = widgetSelectedAssetIds.value
                val nextIds = if (asset.coinId in currentIds) {
                    currentIds.filter { it != asset.coinId }
                } else if (currentIds.size < maxSelected) {
                    currentIds + asset.coinId
                } else {
                    currentIds
                }

                if (nextIds != currentIds) {
                    val vaultId = widgetContractVaultId.value
                    vaultDao.updateSelectedWidgetAssets(vaultId, nextIds.joinToString(","))
                    repository.pushAssetsToWidget(context, vaultId.toString())
                }
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * Persists widget asset selection and optional home-screen binding for [portfolioVaultId],
     * then pushes Glance state and sends [ACTION_APPWIDGET_UPDATE] for the affected instance(s).
     */
    fun saveWidgetConfiguration(
        portfolioVaultId: Int,
        appWidgetId: Int?,
        selectedIds: List<String>,
        onComplete: () -> Unit,
    ) {
        if (portfolioVaultId <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (appWidgetId != null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    vaultDao.clearAppWidgetId(appWidgetId)
                    vaultDao.updateAppWidgetId(portfolioVaultId, appWidgetId)
                }
                val orderedCsv = buildList {
                    val seen = LinkedHashSet<String>()
                    for (raw in selectedIds) {
                        val id = raw.trim()
                        if (id.isEmpty() || id in seen) continue
                        seen.add(id)
                        add(id)
                    }
                }.joinToString(",")
                vaultDao.updateSelectedWidgetAssets(portfolioVaultId, orderedCsv)
                val fresh = assetDao.getAssetsByVaultOnce(portfolioVaultId)
                repository.pushFreshAssetsToWidget(
                    context.applicationContext,
                    portfolioVaultId.toString(),
                    fresh,
                )
                val widgetIdsToRefresh =
                    if (appWidgetId != null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        intArrayOf(appWidgetId)
                    } else {
                        val component = ComponentName(context, PortfolioWidgetReceiver::class.java)
                        AppWidgetManager.getInstance(context).getAppWidgetIds(component)
                    }
                context.sendBroadcast(
                    Intent(context, PortfolioWidgetReceiver::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIdsToRefresh)
                    },
                )
            } finally {
                onComplete()
            }
        }
    }

    fun updateWidgetShowTotalForCurrentVault(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val vaultId = widgetContractVaultId.value
            vaultDao.updateShowWidgetTotal(vaultId, show)
            repository.pushAssetsToWidget(context, vaultId.toString())
        }
    }

    fun saveMetalDisplayOrder(symbols: List<String>) {
        sharedPrefs.edit().putString("metals_order", symbols.joinToString(",")).apply()
        viewModelScope.launch {
            val currentHoldings = holdings.value ?: emptyList()
            val updatedList =
                currentHoldings.filter { it.category == AssetCategory.METAL }.map { asset ->
                    val newIndex = symbols.indexOf(asset.baseSymbol)
                    if (newIndex != -1) asset.copy(displayOrder = newIndex) else asset
                }
            if (updatedList.isNotEmpty()) {
                repository.updateAssetOrder(updatedList)
                triggerCloudSync() // 🛰️ Sync Metal Order
            }
        }
    }

    fun getMetalDisplayOrder(): List<String>? =
        sharedPrefs.getString("metals_order", null)?.split(",")

    fun updateAssetEntity(asset: AssetEntity) {
        viewModelScope.launch {
            repository.updateAssetEntity(asset)
            triggerCloudSync() // 🛰️ Sync Update
        }
    }

    fun updateAsset(asset: AssetEntity, newName: String, newAmount: Double, newWeight: Double, weightUnit: String, decimals: Int) {
        viewModelScope.launch {
            repository.updateAssetEntity(asset.copy(
                name = newName,
                amountHeld = newAmount,
                weight = newWeight,
                weightUnit = weightUnit,
                decimalPreference = decimals
            ))
            triggerCloudSync() // 🛰️ Sync Detailed Update
        }
    }

    fun getPriceHistory(assetId: String): Flow<List<Double>> = flow {
        val history = priceHistoryDao.getRecentHistory(assetId).map { it.price }.reversed()
        emit(history)
    }
}
