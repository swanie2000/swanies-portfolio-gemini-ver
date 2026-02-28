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
    }

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
}