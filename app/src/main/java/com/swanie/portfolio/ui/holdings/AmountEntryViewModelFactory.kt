package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swanie.portfolio.data.local.AssetDao

class AmountEntryViewModelFactory(private val assetDao: AssetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AmountEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AmountEntryViewModel(assetDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}