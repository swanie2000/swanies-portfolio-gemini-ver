package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetEntity
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

    private val _searchResults = MutableStateFlow<List<AssetEntity>>(emptyList())
    val searchResults: StateFlow<List<AssetEntity>> = _searchResults.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isRefreshEnabled = MutableStateFlow(true)
    val isRefreshEnabled: StateFlow<Boolean> = _isRefreshEnabled.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Perform an automatic refresh on initial load
        refreshAllPrices()
    }

    fun refreshAllPrices() {
        viewModelScope.launch {
            if (!_isRefreshEnabled.value) return@launch // Exit if on cooldown

            _isRefreshing.value = true
            _isRefreshEnabled.value = false

            repository.refreshAssetPrices()

            _isRefreshing.value = false

            // Start 60-second cooldown
            delay(60000)
            _isRefreshEnabled.value = true
        }
    }

    suspend fun getSingleCoinPrice(coinId: String): Double {
        return repository.getSingleCoinPrice(coinId)
    }

    fun searchCoins(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce search input
            val results = repository.searchCoins(query)
            _searchResults.value = results
        }
    }

    fun saveNewAsset(asset: AssetEntity, onSaveComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveAsset(asset)
            onSaveComplete()
        }
    }
}
