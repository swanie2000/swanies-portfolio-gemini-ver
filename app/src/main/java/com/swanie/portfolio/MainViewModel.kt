package com.swanie.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(themePreferences: ThemePreferences) : ViewModel() {

    private val _isThemeReady = MutableStateFlow(false)
    val isThemeReady: StateFlow<Boolean> = _isThemeReady.asStateFlow()

    val themeColorHex: StateFlow<String> = themePreferences.themeColorHex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#000416")

    val isDarkMode: StateFlow<Boolean> = themePreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isGradientEnabled: StateFlow<Boolean> = themePreferences.isGradientEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isCompactViewEnabled: StateFlow<Boolean> = themePreferences.isCompactViewEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // This ensures the splash screen waits until the initial values are loaded.
        viewModelScope.launch {
            // The first emission of the flows will make them ready.
            themeColorHex.first()
            isDarkMode.first()
            isGradientEnabled.first()
            isCompactViewEnabled.first() // Add the new preference here
            _isThemeReady.value = true
        }
    }
}
