package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MyHoldingsViewModel(assetDao: AssetDao) : ViewModel() {

    /**
     * Holds the state of the asset list from the database, observed as a StateFlow.
     * This will automatically update the UI whenever the data in the 'assets' table changes.
     */
    val holdings: StateFlow<List<AssetEntity>> = assetDao.getAllAssets()
        .stateIn(
            scope = viewModelScope,
            // Keep the data alive for 5 seconds after the UI is gone, to avoid re-querying on quick configuration changes.
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

/**
 * Factory for creating MyHoldingsViewModel with a constructor that takes an AssetDao.
 * This allows us to inject the DAO dependency into the ViewModel.
 */
class MyHoldingsViewModelFactory(private val assetDao: AssetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyHoldingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyHoldingsViewModel(assetDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
