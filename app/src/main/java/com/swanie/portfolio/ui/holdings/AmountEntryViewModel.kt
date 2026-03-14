package com.swanie.portfolio.ui.holdings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AmountEntryViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    fun saveAsset(asset: AssetEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("API_TRACE", "VM: saveAsset (AmountEntry) triggered for ${asset.symbol}")
            // 1. Initial save
            repository.saveAsset(asset)
            
            // 2. Surgical fetch for THIS specific asset to ensure real price data
            // This is non-blocking for the UI but ensures the DB is updated before the dashboard loads
            repository.refreshSingleAsset(asset)
            
            onComplete()
        }
    }
}
