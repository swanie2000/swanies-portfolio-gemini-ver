package com.swanie.portfolio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemePreferences(context: Context) {
    private val appContext = context.applicationContext

    private object PreferencesKeys {
        val SITE_BACKGROUND_COLOR = stringPreferencesKey("site_background_color")
        val SITE_TEXT_COLOR = stringPreferencesKey("site_text_color")
        val CARD_BACKGROUND_COLOR = stringPreferencesKey("card_background_color")
        val CARD_TEXT_COLOR = stringPreferencesKey("card_text_color")
        val USE_GRADIENT = booleanPreferencesKey("use_gradient")
        val IS_COMPACT_VIEW_ENABLED = booleanPreferencesKey("is_compact_view_enabled")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
    }

    // Flows
    val siteBackgroundColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.SITE_BACKGROUND_COLOR] ?: "#000416" }
    val siteTextColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.SITE_TEXT_COLOR] ?: "#FFFFFF" }
    val cardBackgroundColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.CARD_BACKGROUND_COLOR] ?: "#121212" }
    val cardTextColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.CARD_TEXT_COLOR] ?: "#FFFFFF" }
    val useGradient: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.USE_GRADIENT] ?: false }
    val isCompactViewEnabled: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.IS_COMPACT_VIEW_ENABLED] ?: false }
    val isDarkMode: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.IS_DARK_MODE] ?: true }
    val confirmDelete: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.CONFIRM_DELETE] ?: true }

    // Save Functions
    suspend fun saveSiteBackgroundColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.SITE_BACKGROUND_COLOR] = color } }
    suspend fun saveSiteTextColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.SITE_TEXT_COLOR] = color } }
    suspend fun saveCardBackgroundColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.CARD_BACKGROUND_COLOR] = color } }
    suspend fun saveCardTextColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.CARD_TEXT_COLOR] = color } }
    suspend fun saveUseGradient(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.USE_GRADIENT] = enabled } }
    suspend fun saveIsCompactViewEnabled(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.IS_COMPACT_VIEW_ENABLED] = enabled } }
    suspend fun saveIsDarkMode(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.IS_DARK_MODE] = enabled } }
    suspend fun saveConfirmDelete(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.CONFIRM_DELETE] = enabled } }
}
