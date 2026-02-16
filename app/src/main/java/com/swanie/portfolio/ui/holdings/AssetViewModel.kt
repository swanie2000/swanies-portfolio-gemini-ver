package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.MarketData
import com.swanie.portfolio.data.repository.AssetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssetViewModel(private val repository: AssetRepository) : ViewModel() {

    val holdings: StateFlow<List<AssetEntity>> = repository.allAssets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchResults = MutableStateFlow<List<MarketData>>(emptyList())
    val searchResults: StateFlow<List<MarketData>> = _searchResults.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshPrices()
    }

    fun refreshPrices() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshAssetPrices()
            _isRefreshing.value = false
        }
    }

    fun searchCoins(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            val results = repository.searchCoinsWithPrices(query)
            _searchResults.value = results
        }
    }

    /**
     * THE FIX: A single function to save an asset and then immediately refresh prices,
     * ensuring the UI gets the final, correct data.
     */
    fun saveNewAsset(asset: AssetEntity, onSaveComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveAsset(asset)
            repository.refreshAssetPrices() // Refresh prices for all assets after saving
            onSaveComplete()
        }
    }
}
