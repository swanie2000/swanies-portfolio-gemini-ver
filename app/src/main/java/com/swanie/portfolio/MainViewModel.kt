package com.swanie.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _isThemeReady = MutableStateFlow(false)
    val isThemeReady: StateFlow<Boolean> = _isThemeReady.asStateFlow()

    private val _themeColorHex = MutableStateFlow("#000416")
    val themeColorHex: StateFlow<String> = _themeColorHex.asStateFlow()

    private val _isGradientEnabled = MutableStateFlow(false)
    val isGradientEnabled: StateFlow<Boolean> = _isGradientEnabled.asStateFlow()

    private val _isLightTextEnabled = MutableStateFlow(true)
    val isLightTextEnabled: StateFlow<Boolean> = _isLightTextEnabled.asStateFlow()

    private val _isCompactViewEnabled = MutableStateFlow(false)
    val isCompactViewEnabled: StateFlow<Boolean> = _isCompactViewEnabled.asStateFlow()

    init {
        observeThemePreferences()
    }

    private fun observeThemePreferences() {
        viewModelScope.launch {
            // Launch separate collectors to ensure all state is updated independently
            launch {
                themePreferences.siteBackgroundColor.collectLatest { hex ->
                    _themeColorHex.value = hex
                    checkReady()
                }
            }
            launch {
                themePreferences.useGradient.collectLatest { enabled ->
                    _isGradientEnabled.value = enabled
                }
            }
            launch {
                themePreferences.isLightTextEnabled.collectLatest { enabled ->
                    _isLightTextEnabled.value = enabled
                }
            }
            launch {
                themePreferences.isCompactViewEnabled.collectLatest { enabled ->
                    _isCompactViewEnabled.value = enabled
                }
            }
        }
    }

    private fun checkReady() {
        // Mark as ready once we have at least the primary background color
        if (!_isThemeReady.value) {
            _isThemeReady.value = true
        }
    }
}