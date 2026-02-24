package com.swanie.portfolio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val CARD_BACKGROUND_COLOR = stringPreferencesKey("card_background_color")
        val CARD_TEXT_COLOR = stringPreferencesKey("card_text_color")
        val SITE_BACKGROUND_COLOR = stringPreferencesKey("site_background_color")
        val SITE_TEXT_COLOR = stringPreferencesKey("site_text_color")
        val USE_GRADIENT = booleanPreferencesKey("use_gradient")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_COMPACT_VIEW_ENABLED = booleanPreferencesKey("is_compact_view_enabled")
        val IS_LIGHT_TEXT_ENABLED = booleanPreferencesKey("is_light_text_enabled")
    }

    val cardBackgroundColor: Flow<String> = context.dataStore.data.map {
        it[PreferencesKeys.CARD_BACKGROUND_COLOR] ?: "#FFFFFF"
    }

    val cardTextColor: Flow<String> = context.dataStore.data.map {
        it[PreferencesKeys.CARD_TEXT_COLOR] ?: "#000000"
    }

    val siteBackgroundColor: Flow<String> = context.dataStore.data.map {
        it[PreferencesKeys.SITE_BACKGROUND_COLOR] ?: "#FFFFFF"
    }

    val siteTextColor: Flow<String> = context.dataStore.data.map {
        it[PreferencesKeys.SITE_TEXT_COLOR] ?: "#000000"
    }

    val useGradient: Flow<Boolean> = context.dataStore.data.map {
        it[PreferencesKeys.USE_GRADIENT] ?: false
    }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map {
        it[PreferencesKeys.IS_DARK_MODE] ?: true // Default to dark mode
    }

    val isCompactViewEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[PreferencesKeys.IS_COMPACT_VIEW_ENABLED] ?: false // Default to full view
    }

    val isLightTextEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[PreferencesKeys.IS_LIGHT_TEXT_ENABLED] ?: true // Default to Light Text
    }

    suspend fun saveCardBackgroundColor(hex: String) {
        context.dataStore.edit {
            it[PreferencesKeys.CARD_BACKGROUND_COLOR] = hex
        }
    }

    suspend fun saveCardTextColor(hex: String) {
        context.dataStore.edit {
            it[PreferencesKeys.CARD_TEXT_COLOR] = hex
        }
    }

    suspend fun saveSiteBackgroundColor(hex: String) {
        context.dataStore.edit {
            it[PreferencesKeys.SITE_BACKGROUND_COLOR] = hex
        }
    }

    suspend fun saveSiteTextColor(hex: String) {
        context.dataStore.edit {
            it[PreferencesKeys.SITE_TEXT_COLOR] = hex
        }
    }

    suspend fun saveUseGradient(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.USE_GRADIENT] = enabled
        }
    }
    suspend fun saveIsDarkMode(isDark: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun saveIsCompactViewEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.IS_COMPACT_VIEW_ENABLED] = enabled
        }
    }

    suspend fun saveIsLightTextEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_LIGHT_TEXT_ENABLED] = enabled }
    }
}