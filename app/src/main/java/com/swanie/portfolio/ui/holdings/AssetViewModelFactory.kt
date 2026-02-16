package com.swanie.portfolio.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.network.RetrofitClient
import com.swanie.portfolio.data.repository.AssetRepository

class AssetViewModelFactory(private val assetDao: AssetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssetViewModel::class.java)) {
            val repository = AssetRepository(assetDao, RetrofitClient.instance)
            @Suppress("UNCHECKED_CAST")
            return AssetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
