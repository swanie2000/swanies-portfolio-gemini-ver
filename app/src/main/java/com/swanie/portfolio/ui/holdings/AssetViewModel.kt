package com.swanie.portfolio.ui.holdings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinMarketResponse
import com.swanie.portfolio.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class AssetViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val syncCoordinator: DataSyncCoordinator,
    private val searchRegistry: SearchEngineRegistry,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("portfolio_prefs", Context.MODE_PRIVATE)

    // FLIGHT RECORDER: Reactive Observation of the Database
    val holdings: StateFlow<List<AssetEntity>> = repository.allAssets
        .onEach { list ->
            Log.d("API_TRACE", "VM: UI observing ${list.size} assets from DB")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isRefreshing = syncCoordinator.syncStatus
        .map { it is SyncStatus.Syncing }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _searchQuery = MutableStateFlow("")
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<AssetEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length < 2) {
                flowOf(emptyList())
            } else {
                flow {
                    val results = searchRegistry.getDefaultProvider().search(query)
                    emit(results.map { it.toAssetEntity() })
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * UI Entry point for searching.
     * Restored to maintain Signature Integrity.
     */
    fun searchCoins(query: String) {
        _searchQuery.value = query
    }

    suspend fun fetchMarketPriceData(symbol: String): MarketPriceData = repository.fetchMarketPrice(symbol)
    suspend fun fetchLivePrice(coinId: String): CoinMarketResponse? = repository.fetchLivePriceForAsset(coinId)

    fun refreshAssets() {
        viewModelScope.launch { repository.refreshAssets() }
    }

    /**
     * Strictly handles the Market Watch "Big 4" cache.
     */
    fun refreshMarketWatch() {
        viewModelScope.launch { repository.refreshMarketWatch() }
    }

    /**
     * NEW SURGICAL ENTRY POINT: Used for direct additions (e.g. Metals)
     */
    fun performSurgicalAdd(asset: AssetEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("ADD_TRACE", "VM: performSurgicalAdd (Direct) triggered for ${asset.symbol}")
            repository.executeSurgicalAdd(asset) { _, _ -> }
            onComplete()
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch { repository.deleteAsset(asset) }
    }

    fun updateAssetOrder(assets: List<AssetEntity>) {
        viewModelScope.launch { repository.updateAssetOrder(assets) }
    }

    fun saveMetalDisplayOrder(symbols: List<String>) {
        sharedPrefs.edit().putString("metals_order", symbols.joinToString(",")).apply()
        viewModelScope.launch {
            val currentHoldings = holdings.value
            val metalsInDb = currentHoldings.filter { it.category == AssetCategory.METAL }
            val updatedList = metalsInDb.map { asset ->
                val newIndex = symbols.indexOf(asset.baseSymbol)
                if (newIndex != -1) asset.copy(displayOrder = newIndex) else asset
            }
            if (updatedList.isNotEmpty()) repository.updateAssetOrder(updatedList)
        }
    }

    fun getMetalDisplayOrder(): List<String>? = sharedPrefs.getString("metals_order", null)?.split(",")

    fun updateAsset(asset: AssetEntity, newName: String, newAmount: Double, newWeight: Double, decimals: Int) {
        viewModelScope.launch {
            repository.updateAssetEntity(asset.copy(name = newName, amountHeld = newAmount, weight = newWeight, decimalPreference = decimals))
        }
    }
}
