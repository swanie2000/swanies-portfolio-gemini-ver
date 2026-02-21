package com.swanie.portfolio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swanie.portfolio.data.ThemePreferences

class SettingsViewModelFactory(private val themePreferences: ThemePreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(themePreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
