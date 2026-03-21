package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmountEntryViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    private val _amount = MutableStateFlow(0.0f)
    val amount: StateFlow<Float> = _amount.asStateFlow()

    private val _isValid = MutableStateFlow(true)
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    fun updateAmount(input: String) {
        val parsed = input.toFloatOrNull()
        if (parsed != null) {
            _amount.value = parsed
            _isValid.value = true
        } else {
            _amount.value = 0.0f
            _isValid.value = false
        }
    }

    /**
     * SURGICAL: Core DB insertion logic.
     * Updated to fetch live price and sparkline data before saving,
     * ensuring the asset lands with full data.
     */
    fun performSurgicalAdd(asset: AssetEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            // 1. Fetch live data (Price + Sparkline) while the screen animation is playing
            val fullyPopulatedAsset = repository.fetchLiveAssetData(asset)
            
            // 2. Save the complete asset to the database
            repository.executeSurgicalAdd(fullyPopulatedAsset) { success, _ ->
                if (success) onComplete()
            }
        }
    }
}