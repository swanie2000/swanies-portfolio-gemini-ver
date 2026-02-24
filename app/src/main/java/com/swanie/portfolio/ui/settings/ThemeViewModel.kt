package com.swanie.portfolio.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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

    private val _cardBackgroundColor = MutableStateFlow("#FFFFFF")
    val cardBackgroundColor: StateFlow<String> = _cardBackgroundColor.asStateFlow()

    private val _cardTextColor = MutableStateFlow("#000000")
    val cardTextColor: StateFlow<String> = _cardTextColor.asStateFlow()

    private val _siteBackgroundColor = MutableStateFlow("#FFFFFF")
    val siteBackgroundColor: StateFlow<String> = _siteBackgroundColor.asStateFlow()

    private val _siteTextColor = MutableStateFlow("#000000")
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
        viewModelScope.launch {
            themePreferences.saveCardBackgroundColor(hex)
        }
    }

    fun saveCardTextColor(hex: String) {
        viewModelScope.launch {
            themePreferences.saveCardTextColor(hex)
        }
    }

    fun saveSiteBackgroundColor(hex: String) {
        viewModelScope.launch {
            themePreferences.saveSiteBackgroundColor(hex)
        }
    }

    fun saveSiteTextColor(hex: String) {
        viewModelScope.launch {
            themePreferences.saveSiteTextColor(hex)
        }
    }

    fun saveUseGradient(useGradient: Boolean) {
        viewModelScope.launch {
            themePreferences.saveUseGradient(useGradient)
        }
    }
}
