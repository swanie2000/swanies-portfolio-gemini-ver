package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.MetalsProvider
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

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshAssets()
    }

    fun refreshAssets() {
        viewModelScope.launch {
            if (!_isRefreshEnabled.value) return@launch

            _isRefreshing.value = true
            _isRefreshEnabled.value = false

            try {
                repository.refreshAssets() // Updated function call
                _lastSyncTimestamp.value = System.currentTimeMillis()
            } catch (e: Exception) {
                // Handle error appropriately
            }

            _isRefreshing.value = false

            delay(60000) // Cooldown to prevent spamming the refresh
            _isRefreshEnabled.value = true
        }
    }

    fun searchCoins(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // Slightly faster search delay

            // 1. Search Metals (Local and Fast)
            val metalResults = MetalsProvider.searchMetals(query)

            // 2. Search Cryptos (From API/Repository)
            val cryptoResults = repository.searchCoins(query)

            // 3. Combine and Update
            _searchResults.value = metalResults + cryptoResults
        }
    }

    fun saveNewAsset(asset: AssetEntity, onSaveComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveAsset(asset)
            onSaveComplete()
        }
    }
}
