package com.swanie.portfolio.ui.holdings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.PriceHistoryDao
import com.swanie.portfolio.data.repository.*
import com.swanie.portfolio.data.remote.GoogleDriveService // 🛰️ Essential Import
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    private val priceHistoryDao: PriceHistoryDao,
    private val googleDriveService: GoogleDriveService, // 🛰️ Cloud Service
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("portfolio_prefs", Context.MODE_PRIVATE)

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

    fun updateAssetOrder(assets: List<AssetEntity>) {
        viewModelScope.launch {
            repository.updateAssetOrder(assets)
            triggerCloudSync() // 🛰️ Sync Order
        }
    }

    fun toggleWidgetVisibility(asset: AssetEntity) {
        viewModelScope.launch {
            repository.toggleWidgetVisibility(asset.coinId, !asset.showOnWidget)
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
