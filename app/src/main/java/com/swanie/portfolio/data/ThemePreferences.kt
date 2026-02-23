package com.swanie.portfolio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create the DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemePreferences(context: Context) {

    private val appContext = context.applicationContext

    // Define keys for the preferences
    private object PreferencesKeys {
        val THEME_COLOR_HEX = stringPreferencesKey("theme_color_hex")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_GRADIENT_ENABLED = booleanPreferencesKey("is_gradient_enabled")
        val IS_COMPACT_VIEW_ENABLED = booleanPreferencesKey("is_compact_view_enabled")
        val IS_LIGHT_TEXT_ENABLED = booleanPreferencesKey("is_light_text_enabled")
    }

    // Flow to get the current theme color HEX
    val themeColorHex: Flow<String> = appContext.dataStore.data.map {
        it[PreferencesKeys.THEME_COLOR_HEX] ?: "#000416" // Default to Swanie Navy
    }

    // Flow to get the dark mode setting
    val isDarkMode: Flow<Boolean> = appContext.dataStore.data.map {
        it[PreferencesKeys.IS_DARK_MODE] ?: true // Default to dark mode
    }

    // Flow to get the gradient setting
    val isGradientEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[PreferencesKeys.IS_GRADIENT_ENABLED] ?: false // Default to false
    }

    val isCompactViewEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[PreferencesKeys.IS_COMPACT_VIEW_ENABLED] ?: false // Default to full view
    }

    val isLightTextEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[PreferencesKeys.IS_LIGHT_TEXT_ENABLED] ?: true // Default to Light Text
    }

    // Function to save the theme color HEX
    suspend fun saveThemeColorHex(hex: String) {
        appContext.dataStore.edit {
            it[PreferencesKeys.THEME_COLOR_HEX] = hex
        }
    }

    // Function to save the dark mode setting
    suspend fun saveIsDarkMode(isDark: Boolean) {
        appContext.dataStore.edit {
            it[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    // Function to save the gradient setting
    suspend fun saveIsGradientEnabled(enabled: Boolean) {
        appContext.dataStore.edit {
            it[PreferencesKeys.IS_GRADIENT_ENABLED] = enabled
        }
    }

    suspend fun saveIsCompactViewEnabled(enabled: Boolean) {
        appContext.dataStore.edit {
            it[PreferencesKeys.IS_COMPACT_VIEW_ENABLED] = enabled
        }
    }

    suspend fun saveIsLightTextEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[PreferencesKeys.IS_LIGHT_TEXT_ENABLED] = enabled }
    }
}
