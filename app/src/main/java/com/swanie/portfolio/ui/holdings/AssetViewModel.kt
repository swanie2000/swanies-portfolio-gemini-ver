package com.swanie.portfolio.ui.holdings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinMarketResponse
import com.swanie.portfolio.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val syncCoordinator: DataSyncCoordinator,
    private val searchRegistry: SearchEngineRegistry,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("portfolio_prefs", Context.MODE_PRIVATE)
    val holdings = repository.allAssets

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

    fun saveNewAsset(asset: AssetEntity, amount: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveAsset(asset.copy(amountHeld = amount))
            delay(500) 
            repository.refreshAssets(force = true) 
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
            val currentHoldings = holdings.first()
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
