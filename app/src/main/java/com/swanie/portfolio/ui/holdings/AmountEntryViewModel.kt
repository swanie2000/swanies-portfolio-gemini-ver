package com.swanie.portfolio.ui.holdings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmountEntryViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress = _loadingProgress.asStateFlow()

    private val _loadingStatus = MutableStateFlow("")
    val loadingStatus = _loadingStatus.asStateFlow()

    /**
     * NEW SURGICAL ENTRY POINT: Wraps the repository's execution logic
     * and updates the local UI states.
     */
    fun performSurgicalAdd(asset: AssetEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("ADD_TRACE", "VM: performSurgicalAdd triggered for ${asset.symbol}")
            repository.executeSurgicalAdd(asset) { progress, status ->
                _loadingProgress.value = progress
                _loadingStatus.value = status
            }
            _loadingProgress.value = 1.0f
            onComplete()
        }
    }
}
