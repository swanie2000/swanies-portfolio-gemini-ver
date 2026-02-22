package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.RetrofitClient
import com.swanie.portfolio.data.repository.AssetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyHoldingsViewModel(private val repository: AssetRepository) : ViewModel() {

    // Expose the holdings from the repository. The UI will collect this.
    val holdings: StateFlow<List<AssetEntity>> = repository.allAssets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * Triggers a refresh of all asset data from the CoinGecko API.
     * It sets the isRefreshing state to true during the operation.
     */
    fun refreshAssets() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshAssets()
            _isRefreshing.value = false
        }
    }
}

class MyHoldingsViewModelFactory(private val assetDao: AssetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyHoldingsViewModel::class.java)) {
            val repository = AssetRepository(assetDao, RetrofitClient.instance)
            @Suppress("UNCHECKED_CAST")
            return MyHoldingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}