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

    val holdings: StateFlow<List<AssetEntity>> = assetDao.getAllAssets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

// Factory for creating the ViewModel with dependencies
class MyHoldingsViewModelFactory(private val assetDao: AssetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyHoldingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyHoldingsViewModel(assetDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
