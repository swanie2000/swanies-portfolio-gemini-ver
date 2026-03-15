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
    private val STARTUP_FORGIVE_THRESHOLD = 60_000L // 60s block for 429 forgiveness
    private val appStartTime = System.currentTimeMillis()
    private var lastSyncTimestamp = 0L

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    fun canRefresh(force: Boolean = false): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // 60-second Startup Lock
        if (currentTime - appStartTime < STARTUP_FORGIVE_THRESHOLD) {
            val wait = (STARTUP_FORGIVE_THRESHOLD - (currentTime - appStartTime)) / 1000
            Log.w(TAG, "COOLDOWN: 429 Forgiveness active. Remaining: $wait s")
            return false
        }

        if (force) {
            Log.d(TAG, "Rate Limiter: BYPASS (Forced Sync).")
            return true
        }
        
        val timeSinceLastSync = currentTime - lastSyncTimestamp
        return if (timeSinceLastSync >= SYNC_THRESHOLD) {
            Log.d(TAG, "Rate Limiter: GREEN. Ready for sync.")
            true
        } else {
            val remaining = (SYNC_THRESHOLD - timeSinceLastSync) / 1000
            Log.d(TAG, "Rate Limiter: RED. Hold short for $remaining seconds.")
            false
        }
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
