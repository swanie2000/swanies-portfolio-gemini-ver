package com.swanie.portfolio.ui.holdings

import android.util.Log
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
                repository.refreshAssets()
            } catch (e: Exception) {
                Log.e("AssetViewModel", "Refresh failed", e)
            } finally {
                _isRefreshing.value = false
                delay(30000)
                _isRefreshEnabled.value = true
            }
        }
    }

    fun searchCoins(query: String) {
        searchJob?.cancel()
        val cleanQuery = query.trim()

        if (cleanQuery.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)

            val metalResults = MetalsProvider.searchMetals(cleanQuery)
            _searchResults.value = metalResults

            try {
                val cryptoResults = repository.searchCoins(cleanQuery)
                _searchResults.value = (metalResults + cryptoResults).distinctBy { it.coinId }
            } catch (e: Exception) {
                Log.e("AssetViewModel", "Search error", e)
                _searchResults.value = metalResults
            }
        }
    }

    // FIXED: This function now forces the category and price to persist into the DB
    fun saveNewAsset(asset: AssetEntity, amount: Double, onSaveComplete: () -> Unit) {
        viewModelScope.launch {
            val holdingToSave = asset.copy(
                amountHeld = amount,
                category = asset.category, // Explicitly keep the METAL category
                currentPrice = asset.currentPrice, // Ensure price isn't saved as 0.0
                lastUpdated = System.currentTimeMillis()
            )

            Log.d("PortfolioDebug", "Saving ${holdingToSave.name} as ${holdingToSave.category} with price ${holdingToSave.currentPrice}")

            repository.saveAsset(holdingToSave)
            onSaveComplete()
        }
    }
}