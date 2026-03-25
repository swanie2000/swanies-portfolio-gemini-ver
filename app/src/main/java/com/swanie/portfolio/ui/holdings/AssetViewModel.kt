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
import com.swanie.portfolio.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("portfolio_prefs", Context.MODE_PRIVATE)

    // 🌐 GLOBAL VISTA: Track Current Vault
    val currentVaultId = themePreferences.currentVaultId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    // 🌐 GLOBAL VISTA: Filter holdings by currentVaultId
    val holdings: StateFlow<List<AssetEntity>> = currentVaultId
        .flatMapLatest { id -> assetDao.getAssetsByVault(id) }
        .onEach { Log.d("VM_TRACE", "UI observing ${it.size} assets for vault") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                            // 🚀 V8 Identity: Ensure search results land with 'MAIN' portfolio default
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
            onComplete()
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    fun updateAssetOrder(assets: List<AssetEntity>) {
        viewModelScope.launch { repository.updateAssetOrder(assets) }
    }

    fun toggleWidgetVisibility(asset: AssetEntity) {
        viewModelScope.launch {
            repository.toggleWidgetVisibility(asset.coinId, !asset.showOnWidget)
        }
    }

    fun saveMetalDisplayOrder(symbols: List<String>) {
        sharedPrefs.edit().putString("metals_order", symbols.joinToString(",")).apply()
        viewModelScope.launch {
            val currentHoldings = holdings.value
            val updatedList =
                currentHoldings.filter { it.category == AssetCategory.METAL }.map { asset ->
                    val newIndex = symbols.indexOf(asset.baseSymbol)
                    if (newIndex != -1) asset.copy(displayOrder = newIndex) else asset
                }
            if (updatedList.isNotEmpty()) repository.updateAssetOrder(updatedList)
        }
    }

    fun getMetalDisplayOrder(): List<String>? =
        sharedPrefs.getString("metals_order", null)?.split(",")

    fun updateAssetEntity(asset: AssetEntity) {
        viewModelScope.launch {
            repository.updateAssetEntity(asset)
        }
    }

    fun updateAsset(asset: AssetEntity, newName: String, newAmount: Double, newWeight: Double, decimals: Int) {
        viewModelScope.launch {
            repository.updateAssetEntity(asset.copy(
                name = newName,
                amountHeld = newAmount,
                weight = newWeight,
                decimalPreference = decimals
            ))
        }
    }
}
