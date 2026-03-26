package com.swanie.portfolio.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    // FIXED: Initial states synced with Brand Navy to prevent white flicker
    private val _cardBackgroundColor = MutableStateFlow("#121212")
    val cardBackgroundColor: StateFlow<String> = _cardBackgroundColor.asStateFlow()

    private val _cardTextColor = MutableStateFlow("#FFFFFF")
    val cardTextColor: StateFlow<String> = _cardTextColor.asStateFlow()

    private val _siteBackgroundColor = MutableStateFlow("#000416")
    val siteBackgroundColor: StateFlow<String> = _siteBackgroundColor.asStateFlow()

    private val _siteTextColor = MutableStateFlow("#FFFFFF")
    val siteTextColor: StateFlow<String> = _siteTextColor.asStateFlow()

    private val _useGradient = MutableStateFlow(false)
    val useGradient: StateFlow<Boolean> = _useGradient.asStateFlow()

    private val _gradientAmount = MutableStateFlow(0.5f)
    val gradientAmount: StateFlow<Float> = _gradientAmount.asStateFlow()

    private val _metalsDisplayOrder = MutableStateFlow("XAU,XAG,XPT,XPD")
    val metalsDisplayOrder: StateFlow<String> = _metalsDisplayOrder.asStateFlow()

    // --- WIDGET SPECIFIC THEME PROPERTIES (Added for V8.0.0) ---
    private val _widgetBgColor = MutableStateFlow("#1C1C1E")
    val widgetBgColor: StateFlow<String> = _widgetBgColor.asStateFlow()

    private val _widgetBgTextColor = MutableStateFlow("#FFFFFF")
    val widgetBgTextColor: StateFlow<String> = _widgetBgTextColor.asStateFlow()

    private val _widgetCardColor = MutableStateFlow("#2C2C2E")
    val widgetCardColor: StateFlow<String> = _widgetCardColor.asStateFlow()

    private val _widgetCardTextColor = MutableStateFlow("#FFFFFF")
    val widgetCardTextColor: StateFlow<String> = _widgetCardTextColor.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferences.cardBackgroundColor.collect { _cardBackgroundColor.value = it }
        }
        viewModelScope.launch {
            themePreferences.cardTextColor.collect { _cardTextColor.value = it }
        }
        viewModelScope.launch {
            themePreferences.siteBackgroundColor.collect { _siteBackgroundColor.value = it }
        }
        viewModelScope.launch {
            themePreferences.siteTextColor.collect { _siteTextColor.value = it }
        }
        viewModelScope.launch {
            themePreferences.useGradient.collect { _useGradient.value = it }
        }
        viewModelScope.launch {
            themePreferences.gradientAmount.collect { _gradientAmount.value = it }
        }
        viewModelScope.launch {
            themePreferences.metalsDisplayOrder.collect { _metalsDisplayOrder.value = it }
        }
        // Collect widget-specific preferences
        viewModelScope.launch {
            themePreferences.widgetBgColor.collect { _widgetBgColor.value = it }
        }
        viewModelScope.launch {
            themePreferences.widgetBgTextColor.collect { _widgetBgTextColor.value = it }
        }
        viewModelScope.launch {
            themePreferences.widgetCardColor.collect { _widgetCardColor.value = it }
        }
        viewModelScope.launch {
            themePreferences.widgetCardTextColor.collect { _widgetCardTextColor.value = it }
        }
    }

    // Existing save functions...
    fun saveCardBackgroundColor(hex: String) {
        _cardBackgroundColor.value = hex
        viewModelScope.launch { themePreferences.saveCardBackgroundColor(hex) }
    }

    fun saveCardTextColor(hex: String) {
        _cardTextColor.value = hex
        viewModelScope.launch { themePreferences.saveCardTextColor(hex) }
    }

    fun saveSiteBackgroundColor(hex: String) {
        _siteBackgroundColor.value = hex
        viewModelScope.launch { themePreferences.saveSiteBackgroundColor(hex) }
    }

    fun saveSiteTextColor(hex: String) {
        _siteTextColor.value = hex
        viewModelScope.launch { themePreferences.saveSiteTextColor(hex) }
    }

    fun saveUseGradient(useGradient: Boolean) {
        _useGradient.value = useGradient
        viewModelScope.launch { themePreferences.saveUseGradient(useGradient) }
    }

    fun saveGradientAmount(amount: Float) {
        _gradientAmount.value = amount
        viewModelScope.launch { themePreferences.saveGradientAmount(amount) }
    }

    fun saveMetalsDisplayOrder(order: String) {
        _metalsDisplayOrder.value = order
        viewModelScope.launch { themePreferences.saveMetalsDisplayOrder(order) }
    }

    // --- WIDGET THEME UPDATE FUNCTIONS (Added for V8.0.0) ---
    fun updateWidgetBgColor(hex: String) {
        _widgetBgColor.value = hex
        viewModelScope.launch { themePreferences.saveWidgetBgColor(hex) }
    }

    fun updateWidgetBgTextColor(hex: String) {
        _widgetBgTextColor.value = hex
        viewModelScope.launch { themePreferences.saveWidgetBgTextColor(hex) }
    }

    fun updateWidgetCardColor(hex: String) {
        _widgetCardColor.value = hex
        viewModelScope.launch { themePreferences.saveWidgetCardColor(hex) }
    }

    fun updateWidgetCardTextColor(hex: String) {
        _widgetCardTextColor.value = hex
        viewModelScope.launch { themePreferences.saveWidgetCardTextColor(hex) }
    }
}