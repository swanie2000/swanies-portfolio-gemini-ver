package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinMarketResponse
import com.swanie.portfolio.data.repository.AssetRepository
import com.swanie.portfolio.data.repository.MarketPriceData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel for handling asset holdings and market data fetching.
 */
@HiltViewModel
class AssetViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    // Observed holdings from the local Room database.
    val holdings = repository.allAssets

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AssetEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    /**
     * Search for crypto assets via Repository.
     */
    fun searchCoins(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _searchResults.value = repository.searchCoins(query)
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    /**
     * Independent fetch for a specific metal's market data (used by Market Watch).
     */
    suspend fun fetchMarketPriceData(symbol: String): MarketPriceData {
        return repository.fetchMarketPrice(symbol)
    }

    /**
     * Get live price for a specific crypto asset before addition.
     */
    suspend fun fetchLivePrice(coinId: String): CoinMarketResponse? {
        return repository.fetchLivePriceForAsset(coinId)
    }

    /**
     * Synchronize all holdings with live market data.
     * Includes an API Safety Lock to prevent redundant concurrent fetches.
     */
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

    /**
     * Save a new asset to the local database.
     */
    fun saveNewAsset(asset: AssetEntity, amount: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveAsset(asset.copy(amountHeld = amount))
            onComplete()
        }
    }

    /**
     * Delete an asset from the local database.
     */
    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    /**
     * Update the display order of assets in the database.
     */
    fun updateAssetOrder(assets: List<AssetEntity>) {
        viewModelScope.launch {
            repository.updateAssetOrder(assets)
        }
    }

    /**
     * Update an existing asset's properties.
     */
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
