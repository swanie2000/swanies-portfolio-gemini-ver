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
    private var lastSyncTimestamp = 0L

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    fun canRefresh(force: Boolean = false): Boolean {
        if (force) {
            Log.d(TAG, "Rate Limiter: BYPASS (Forced Sync).")
            return true
        }
        val currentTime = System.currentTimeMillis()
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
