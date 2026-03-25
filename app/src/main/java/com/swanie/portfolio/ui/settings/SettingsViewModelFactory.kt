package com.swanie.portfolio.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.UserConfigDao
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.VaultDao

class SettingsViewModelFactory(
    private val context: Context,
    private val themePreferences: ThemePreferences,
    private val userConfigDao: UserConfigDao,
    private val assetDao: AssetDao,
    private val vaultDao: VaultDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(context, themePreferences, userConfigDao, assetDao, vaultDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
