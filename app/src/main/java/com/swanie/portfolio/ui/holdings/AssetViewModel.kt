package com.swanie.portfolio.ui.holdings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.MetalsProvider
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder {
    VALUE, NAME, CATEGORY, MANUAL
}

@HiltViewModel
class AssetViewModel @Inject constructor(private val repository: AssetRepository) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.VALUE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val holdings: StateFlow<List<AssetEntity>> = repository.allAssets
        .combine(_sortOrder) { assets, order ->
            when (order) {
                SortOrder.VALUE -> assets.sortedByDescending { it.currentPrice * it.amountHeld }
                SortOrder.NAME -> assets.sortedBy { it.name }
                SortOrder.CATEGORY -> assets.sortedBy { it.category.name }
                SortOrder.MANUAL -> assets.sortedBy { it.displayOrder }
            }
        }
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

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
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

    fun saveNewAsset(asset: AssetEntity, amount: Double, onSaveComplete: () -> Unit) {
        viewModelScope.launch {
            val holdingToSave = asset.copy(
                amountHeld = amount,
                category = asset.category,
                currentPrice = asset.currentPrice,
                lastUpdated = System.currentTimeMillis()
            )
            repository.saveAsset(holdingToSave)
            onSaveComplete()
        }
    }

    // ADDED: This function connects the Swipe-to-Delete UI to the Database
    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            try {
                repository.deleteAsset(asset)
            } catch (e: Exception) {
                Log.e("AssetViewModel", "Delete failed for ${asset.name}", e)
            }
        }
    }

    fun updateAssetOrder(assets: List<AssetEntity>) {
        viewModelScope.launch {
            repository.updateAssetOrder(assets)
        }
    }
}