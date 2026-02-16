package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import kotlinx.coroutines.launch

class AmountEntryViewModel(private val assetDao: AssetDao) : ViewModel() {

    fun saveAsset(asset: AssetEntity) {
        viewModelScope.launch {
            assetDao.insertAsset(asset)
        }
    }
}