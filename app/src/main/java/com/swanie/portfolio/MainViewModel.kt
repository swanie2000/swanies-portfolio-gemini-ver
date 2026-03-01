package com.swanie.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _isThemeReady = MutableStateFlow(false)
    val isThemeReady: StateFlow<Boolean> = _isThemeReady.asStateFlow()

    // Preferences observed as StateFlows for UI synchronization
    val siteBackgroundColor = themePreferences.siteBackgroundColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#000416")
    val siteTextColor = themePreferences.siteTextColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#FFFFFF")
    val cardBackgroundColor = themePreferences.cardBackgroundColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#121212")
    val cardTextColor = themePreferences.cardTextColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "#FFFFFF")
    val useGradient = themePreferences.useGradient.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isCompactViewEnabled = themePreferences.isCompactViewEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isDarkMode = themePreferences.isDarkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val confirmDelete = themePreferences.confirmDelete.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Bridge for Settings Screen toggle
    fun setConfirmDelete(enabled: Boolean) = viewModelScope.launch {
        themePreferences.saveConfirmDelete(enabled)
    }

    init {
        _isThemeReady.value = true
    }
}
