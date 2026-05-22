package com.swanie.portfolio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.proUnlockDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pro_unlock_prefs",
)

data class ProUnlockState(
    val email: String = "",
    val expiryEpochDay: Long = 0L,
) {
    val isRedeemed: Boolean = email.isNotBlank() && expiryEpochDay > 0L

    fun isActive(today: LocalDate = LocalDate.now(ZoneId.systemDefault())): Boolean {
        if (!isRedeemed) return false
        val expiry = LocalDate.ofEpochDay(expiryEpochDay)
        return !today.isAfter(expiry)
    }
}

@Singleton
class ProUnlockPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val appContext = context.applicationContext

    private object Keys {
        val EMAIL = stringPreferencesKey("pro_unlock_email")
        val EXPIRY_EPOCH_DAY = longPreferencesKey("pro_unlock_expiry_epoch_day")
    }

    val state: Flow<ProUnlockState> = appContext.proUnlockDataStore.data.map { prefs ->
        ProUnlockState(
            email = prefs[Keys.EMAIL].orEmpty(),
            expiryEpochDay = prefs[Keys.EXPIRY_EPOCH_DAY] ?: 0L,
        )
    }

    suspend fun saveUnlock(email: String, expiryDate: LocalDate) {
        appContext.proUnlockDataStore.edit { prefs ->
            prefs[Keys.EMAIL] = email.trim().lowercase()
            prefs[Keys.EXPIRY_EPOCH_DAY] = expiryDate.toEpochDay()
        }
    }

    suspend fun clearUnlock() {
        appContext.proUnlockDataStore.edit { prefs ->
            prefs.remove(Keys.EMAIL)
            prefs.remove(Keys.EXPIRY_EPOCH_DAY)
        }
    }
}
