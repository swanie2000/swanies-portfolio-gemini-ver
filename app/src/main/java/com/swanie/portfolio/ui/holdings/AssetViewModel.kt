package com.swanie.portfolio.ui.holdings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinMarketResponse
import com.swanie.portfolio.data.repository.AssetRepository
import com.swanie.portfolio.data.repository.MarketPriceData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetViewModel @Inject constructor(
    private val repository: AssetRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("portfolio_prefs", Context.MODE_PRIVATE)

    val holdings = repository.allAssets

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AssetEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun searchCoins(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _searchResults.value = repository.searchCoins(query)
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    suspend fun fetchMarketPriceData(symbol: String): MarketPriceData {
        return repository.fetchMarketPrice(symbol)
    }

    suspend fun fetchLivePrice(coinId: String): CoinMarketResponse? {
        return repository.fetchLivePriceForAsset(coinId)
    }

    fun refreshAssets() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshAssets()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun saveNewAsset(asset: AssetEntity, amount: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveAsset(asset.copy(amountHeld = amount))
            onComplete()
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    fun updateAssetOrder(assets: List<AssetEntity>) {
        viewModelScope.launch {
            repository.updateAssetOrder(assets)
        }
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
            if (updatedList.isNotEmpty()) {
                repository.updateAssetOrder(updatedList)
            }
        }
    }

    fun getMetalDisplayOrder(): List<String>? {
        return sharedPrefs.getString("metals_order", null)?.split(",")
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