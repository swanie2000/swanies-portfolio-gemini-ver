package com.swanie.portfolio.data.repository

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncCoordinator @Inject constructor() {
    private val TAG = "SWAN_SYNC"
    private val SYNC_THRESHOLD = 30_000L 
    private val STARTUP_FORGIVE_THRESHOLD = 30_000L // Reduced to 30s to match sync threshold
    private val appStartTime = System.currentTimeMillis()
    private var lastSyncTimestamp = 0L

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    fun getRemainingCooldown(): Int {
        val currentTime = System.currentTimeMillis()
        
        // Startup Lock check (Now 30s)
        val startupWait = (STARTUP_FORGIVE_THRESHOLD - (currentTime - appStartTime)) / 1000
        if (startupWait > 0) return startupWait.toInt()

        val timeSinceLastSync = currentTime - lastSyncTimestamp
        val syncWait = (SYNC_THRESHOLD - timeSinceLastSync) / 1000
        return if (syncWait > 0) syncWait.toInt() else 0
    }

    fun canRefresh(force: Boolean = false): Boolean {
        if (force) {
            Log.d(TAG, "Rate Limiter: BYPASS (Forced Sync).")
            return true
        }
        
        return getRemainingCooldown() <= 0
    }

    fun startSync() {
        _syncStatus.value = SyncStatus.Syncing
    }

    fun endSync(success: Boolean) {
        if (success) {
            lastSyncTimestamp = System.currentTimeMillis()
        }
        _syncStatus.value = if (success) SyncStatus.Idle else SyncStatus.Error("Sync Failed")
    }
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
