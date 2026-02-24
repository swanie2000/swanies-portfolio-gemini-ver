package com.swanie.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val themePreferences: ThemePreferences // Injected by Hilt
) : ViewModel() {

    // These flows provide the data your MainActivity is looking for
    private val _isThemeReady = MutableStateFlow(false)
    val isThemeReady: StateFlow<Boolean> = _isThemeReady.asStateFlow()

    private val _themeColorHex = MutableStateFlow("#FF6200EE")
    val themeColorHex: StateFlow<String> = _themeColorHex.asStateFlow()

    private val _isGradientEnabled = MutableStateFlow(false)
    val isGradientEnabled: StateFlow<Boolean> = _isGradientEnabled.asStateFlow()

    private val _isLightTextEnabled = MutableStateFlow(false)
    val isLightTextEnabled: StateFlow<Boolean> = _isLightTextEnabled.asStateFlow()

    private val _isCompactViewEnabled = MutableStateFlow(false)
    val isCompactViewEnabled: StateFlow<Boolean> = _isCompactViewEnabled.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        observeThemePreferences()
    }

    private fun observeThemePreferences() {
        viewModelScope.launch {
            // This is where you connect to your ThemePreferences data
            // For now, we mark it ready so the splash screen disappears
            _isThemeReady.value = true
        }
    }
}