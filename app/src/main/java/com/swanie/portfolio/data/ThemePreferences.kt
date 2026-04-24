package com.swanie.portfolio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
    private val appContext = context.applicationContext

    private object PreferencesKeys {
        val SITE_BACKGROUND_COLOR = stringPreferencesKey("site_background_color")
        val SITE_TEXT_COLOR = stringPreferencesKey("site_text_color")
        val CARD_BACKGROUND_COLOR = stringPreferencesKey("card_background_color")
        val CARD_TEXT_COLOR = stringPreferencesKey("card_text_color")
        val USE_GRADIENT = booleanPreferencesKey("use_gradient")
        val GRADIENT_AMOUNT = floatPreferencesKey("gradient_amount")
        val IS_COMPACT_VIEW_ENABLED = booleanPreferencesKey("is_compact_view_enabled")
        val IS_HIGH_VISIBILITY_MODE = booleanPreferencesKey("is_high_visibility_mode")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val METALS_DISPLAY_ORDER = stringPreferencesKey("metals_display_order")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val CURRENT_VAULT_ID = intPreferencesKey("current_vault_id")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")

        // --- DEFAULT VAULT LOGIC ---
        val DEFAULT_VAULT_ID = intPreferencesKey("default_vault_id")
        val RESET_TO_DEFAULT_ON_START = booleanPreferencesKey("reset_to_default_on_start")

        // --- NEW WIDGET KEYS (V8.0.0) ---
        val WIDGET_BG_COLOR = stringPreferencesKey("widget_bg_color")
        val WIDGET_BG_TEXT_COLOR = stringPreferencesKey("widget_bg_text_color")
        val WIDGET_CARD_COLOR = stringPreferencesKey("widget_card_color")
        val WIDGET_CARD_TEXT_COLOR = stringPreferencesKey("widget_card_text_color")
    }

    // High-Resolution Theme Flows
    val siteBackgroundColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.SITE_BACKGROUND_COLOR] ?: "#000416" }
    val siteTextColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.SITE_TEXT_COLOR] ?: "#FFFFFF" }
    val cardBackgroundColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.CARD_BACKGROUND_COLOR] ?: "#121212" }
    val cardTextColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.CARD_TEXT_COLOR] ?: "#FFFFFF" }
    val useGradient: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.USE_GRADIENT] ?: false }
    val gradientAmount: Flow<Float> = appContext.dataStore.data.map { it[PreferencesKeys.GRADIENT_AMOUNT] ?: 0.5f }
    val isCompactViewEnabled: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.IS_COMPACT_VIEW_ENABLED] ?: false }
    val isHighVisibilityMode: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.IS_HIGH_VISIBILITY_MODE] ?: false }
    val isDarkMode: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.IS_DARK_MODE] ?: true }
    val confirmDelete: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.CONFIRM_DELETE] ?: true }
    val metalsDisplayOrder: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.METALS_DISPLAY_ORDER] ?: "XAU,XAG,XPT,XPD" }
    val themeMode: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.THEME_MODE_KEY] ?: "SYSTEM" }
    val currentVaultId: Flow<Int> = appContext.dataStore.data.map { it[PreferencesKeys.CURRENT_VAULT_ID] ?: 1 }
    val isBiometricEnabled: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: false }

    // Default Vault Flows
    val defaultVaultId: Flow<Int> = appContext.dataStore.data.map { it[PreferencesKeys.DEFAULT_VAULT_ID] ?: 1 }
    val resetToDefaultOnStart: Flow<Boolean> = appContext.dataStore.data.map { it[PreferencesKeys.RESET_TO_DEFAULT_ON_START] ?: false }

    // --- NEW WIDGET FLOWS (V8.0.0) ---
    val widgetBgColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.WIDGET_BG_COLOR] ?: "#1C1C1E" }
    val widgetBgTextColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.WIDGET_BG_TEXT_COLOR] ?: "#FFFFFF" }
    val widgetCardColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.WIDGET_CARD_COLOR] ?: "#2C2C2E" }
    val widgetCardTextColor: Flow<String> = appContext.dataStore.data.map { it[PreferencesKeys.WIDGET_CARD_TEXT_COLOR] ?: "#FFFFFF" }

    // Persistence Functions
    suspend fun saveSiteBackgroundColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.SITE_BACKGROUND_COLOR] = color } }
    suspend fun saveSiteTextColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.SITE_TEXT_COLOR] = color } }
    suspend fun saveCardBackgroundColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.CARD_BACKGROUND_COLOR] = color } }
    suspend fun saveCardTextColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.CARD_TEXT_COLOR] = color } }
    suspend fun saveUseGradient(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.USE_GRADIENT] = enabled } }
    suspend fun saveGradientAmount(amount: Float) { appContext.dataStore.edit { it[PreferencesKeys.GRADIENT_AMOUNT] = amount } }
    suspend fun saveIsCompactViewEnabled(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.IS_COMPACT_VIEW_ENABLED] = enabled } }
    suspend fun saveIsHighVisibilityMode(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.IS_HIGH_VISIBILITY_MODE] = enabled } }
    suspend fun saveIsDarkMode(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.IS_DARK_MODE] = enabled } }
    suspend fun saveConfirmDelete(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.CONFIRM_DELETE] = enabled } }
    suspend fun saveMetalsDisplayOrder(order: String) { appContext.dataStore.edit { it[PreferencesKeys.METALS_DISPLAY_ORDER] = order } }
    suspend fun saveThemeMode(mode: String) { appContext.dataStore.edit { it[PreferencesKeys.THEME_MODE_KEY] = mode } }
    suspend fun saveCurrentVaultId(id: Int) { appContext.dataStore.edit { it[PreferencesKeys.CURRENT_VAULT_ID] = id } }
    suspend fun saveIsBiometricEnabled(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.IS_BIOMETRIC_ENABLED] = enabled } }

    // Default Vault Persistence
    suspend fun saveDefaultVaultId(id: Int) { appContext.dataStore.edit { it[PreferencesKeys.DEFAULT_VAULT_ID] = id } }
    suspend fun saveResetToDefaultOnStart(enabled: Boolean) { appContext.dataStore.edit { it[PreferencesKeys.RESET_TO_DEFAULT_ON_START] = enabled } }

    // --- NEW WIDGET SAVE FUNCTIONS (V8.0.0) ---
    suspend fun saveWidgetBgColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.WIDGET_BG_COLOR] = color } }
    suspend fun saveWidgetBgTextColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.WIDGET_BG_TEXT_COLOR] = color } }
    suspend fun saveWidgetCardColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.WIDGET_CARD_COLOR] = color } }
    suspend fun saveWidgetCardTextColor(color: String) { appContext.dataStore.edit { it[PreferencesKeys.WIDGET_CARD_TEXT_COLOR] = color } }
}