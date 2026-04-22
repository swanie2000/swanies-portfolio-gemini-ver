package com.swanie.portfolio.ui.holdings

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import com.swanie.portfolio.data.remote.GoogleDriveService // 🛡️ Local-First Stub
import com.swanie.portfolio.widget.PortfolioWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
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
    private val googleDriveService: GoogleDriveService, // 🛡️ Local-First Stub
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("portfolio_prefs", Context.MODE_PRIVATE)

    /**
     * 🎯 V38.9 NITRO: AUTO-WAKE HEARTBEAT
     */
    init {
        viewModelScope.launch {
            Log.d("NITRO_SYNC", "Wake-up Signal Sent: Initializing asset pipe.")
            repository.refreshAssets()
        }
    }

    // 🌐 GLOBAL VISTA: Track Current Vault
    val currentVaultId = themePreferences.currentVaultId.stateIn(
        viewModelScope, 
        SharingStarted.Eagerly, 
        runBlocking { themePreferences.currentVaultId.first() }
    )

    // 🛡️ DELETION SHIELD: Track the confirm delete setting
    val confirmDelete = themePreferences.confirmDelete
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // 🌐 GLOBAL VISTA: Filter holdings by currentVaultId
    val holdings: StateFlow<List<AssetEntity>?> = currentVaultId
        .flatMapLatest { id ->
            Log.d("VM_TRACE", "Fetching assets for vault: $id")
            assetDao.getAssetsByVault(id)
        }
        .onEach { Log.d("VM_TRACE", "UI observing ${it?.size ?: 0} assets for vault ${currentVaultId.value}") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * 🎯 V38.14 TRANSACTIONAL GRAVITY: EXPLICIT ORDERED FEED
     * Explicitly sorted stream for the Widget Manager/Config screens.
     */
    val widgetAssetsOrdered: StateFlow<List<AssetEntity>> = currentVaultId
        .flatMapLatest { id ->
            assetDao.getAssetsOrderedByWidget(id)
        }
        .onEach { Log.d("VM_PIPE", "Ordered Gravity Feed: ${it.size} assets flowing.") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * 🎯 V38.5 IRON GRAVITY: RAW PIPE
     */
    val allAssets: StateFlow<List<AssetEntity>> = assetDao.getAllAssetsGlobalFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * 🛡️ INTERNAL HELPER: Local Sync Heartbeat.
     */
    private fun triggerLocalSync() {
        viewModelScope.launch {
            try {
                val allAssets = assetDao.getAllAssetsGlobal()
                if (allAssets.isNotEmpty()) {
                    googleDriveService.uploadFullVaultBackup(allAssets)
                    Log.d("VAULT_DEBUG", "Heartbeat: Local Sync Triggered.")
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
            triggerLocalSync()
            onComplete()
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
            triggerLocalSync()
        }
    }

    fun updateAssetOrder(assets: List<AssetEntity>) {
        viewModelScope.launch {
            repository.updateAssetOrder(assets)
            triggerLocalSync()
        }
    }

    /**
     * 🎯 V38.14 DECK SHUFFLE: Save-on-Move Persistence
     * Hard-coded sequence assignment with transactional write and broadcast.
     */
    fun updateWidgetOrderBulk(assets: List<AssetEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val assetIds = assets.map { it.coinId }
            // 🛡️ Atomic Deck Shuffle
            assetDao.updateWidgetOrderBulk(assetIds)
            Log.d("NITRO_SYNC", "Transactional Gravity: Sequence Saved (${assets.size} items)")
            
            // 🚀 Final Broadcast
            delay(300) 
            PortfolioWidget().updateAll(context)
            Log.d("NITRO_SYNC", "Broadast Complete.")
            
            triggerLocalSync()
        }
    }

    fun updateWidgetOrder(vaultId: Int, selectedIds: List<String>) {
        viewModelScope.launch {
            vaultDao.updateSelectedWidgetAssets(vaultId, selectedIds.joinToString(","))
            delay(250)
            PortfolioWidget().updateAll(context)
            Log.d("WIDGET_ORDER", "Synced selection for vault $vaultId.")
        }
    }

    fun toggleWidgetVisibility(asset: AssetEntity) {
        viewModelScope.launch {
            repository.toggleWidgetVisibility(asset.coinId, !asset.showOnWidget)
            delay(250)
            PortfolioWidget().updateAll(context)
            triggerLocalSync()
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
                triggerLocalSync()
            }
        }
    }

    fun getMetalDisplayOrder(): List<String>? =
        sharedPrefs.getString("metals_order", null)?.split(",")

    fun updateAssetEntity(asset: AssetEntity) {
        viewModelScope.launch {
            repository.updateAssetEntity(asset)
            triggerLocalSync()
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
            triggerLocalSync()
        }
    }

    fun getPriceHistory(assetId: String): Flow<List<Double>> = flow {
        val history = priceHistoryDao.getRecentHistory(assetId).map { it.price }.reversed()
        emit(history)
    }

    fun addTestAsset() {
        viewModelScope.launch {
            val testAsset = AssetEntity(
                coinId = "test-swan-${System.currentTimeMillis()}",
                symbol = "SWAN",
                name = "Test Asset",
                vaultId = currentVaultId.value,
                portfolioId = "MAIN",
                category = AssetCategory.CRYPTO,
                officialSpotPrice = 1.0,
                priceChange24h = 5.0,
                amountHeld = 1.0
            )
            repository.executeSurgicalAdd(testAsset) { _, _ -> }
            Log.d("VM_PIPE", "Added Test Asset to Vault ${currentVaultId.value}")
        }
    }
}
