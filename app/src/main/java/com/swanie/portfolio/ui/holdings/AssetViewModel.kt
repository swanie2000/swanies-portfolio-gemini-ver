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

    private val _isSearchBusy = MutableStateFlow(false)
    val isSearchBusy: StateFlow<Boolean> = _isSearchBusy.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<AssetEntity>> = _searchQuery
        .debounce(700)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            val cleanQuery = query.trim()
            if (cleanQuery.length < 2) { // Floor restored to 2 characters
                _isSearchBusy.value = false
                flowOf(emptyList())
            } else {
                flow {
                    _isSearchBusy.value = true
                    try {
                        val results = searchRegistry.getDefaultProvider().search(cleanQuery)
                        emit(results.map { it.toAssetEntity() })
                    } catch (e: Exception) {
                        Log.e("SEARCH_TRACE", "Search failed for $cleanQuery: ${e.message}")
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

    suspend fun fetchMarketPriceData(symbol: String): MarketPriceData = repository.fetchMarketPrice(symbol)
    suspend fun fetchLivePrice(coinId: String): CoinMarketResponse? = repository.fetchLivePriceForAsset(coinId)

    fun refreshAssets() {
        viewModelScope.launch { repository.refreshAssets() }
    }

    fun refreshMarketWatch() {
        viewModelScope.launch { repository.refreshMarketWatch() }
    }

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
            repository.updateAssetEntity(asset.copy(
                name = newName,
                amountHeld = newAmount,
                weight = newWeight,
                decimalPreference = decimals
            ))
        }
    }
}