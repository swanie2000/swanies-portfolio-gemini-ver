package com.swanie.portfolio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create the DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemePreferences(context: Context) {

    private val appContext = context.applicationContext

    // Define keys for the preferences
    private object PreferencesKeys {
        val THEME_INDEX = intPreferencesKey("theme_index")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    // Flow to get the current theme index
    val themeIndex: Flow<Int> = appContext.dataStore.data.map {
        it[PreferencesKeys.THEME_INDEX] ?: 0 // Default to 0 (Navy)
    }

    // Flow to get the dark mode setting
    val isDarkMode: Flow<Boolean> = appContext.dataStore.data.map {
        it[PreferencesKeys.IS_DARK_MODE] ?: true // Default to dark mode
    }

    // Function to save the theme index
    suspend fun saveThemeIndex(index: Int) {
        appContext.dataStore.edit {
            it[PreferencesKeys.THEME_INDEX] = index
        }
    }

    // Function to save the dark mode setting
    suspend fun saveIsDarkMode(isDark: Boolean) {
        appContext.dataStore.edit {
            it[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }
}
