package com.swanie.portfolio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val themePreferences: ThemePreferences) : ViewModel() {

    val themeColorHex: StateFlow<String> = themePreferences.themeColorHex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#000416")

    val isDarkMode: StateFlow<Boolean> = themePreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun saveThemeColorHex(hex: String) {
        viewModelScope.launch {
            themePreferences.saveThemeColorHex(hex)
        }
    }

    fun saveIsDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            themePreferences.saveIsDarkMode(isDark)
        }
    }
}
