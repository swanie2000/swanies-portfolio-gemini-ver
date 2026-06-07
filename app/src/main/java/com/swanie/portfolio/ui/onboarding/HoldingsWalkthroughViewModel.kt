package com.swanie.portfolio.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HoldingsWalkthroughViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
) : ViewModel() {

    val controller = HoldingsWalkthroughController()

    val walkthroughCompleted = themePreferences.holdingsWalkthroughCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showTakeTourButton = themePreferences.showTakeTourButton
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun markCompleted() {
        viewModelScope.launch {
            themePreferences.saveHoldingsWalkthroughCompleted(true)
        }
    }

    fun startTour() {
        controller.startTour()
    }

    fun setShowTakeTourButton(show: Boolean) {
        viewModelScope.launch {
            themePreferences.saveShowTakeTourButton(show)
        }
    }
}
