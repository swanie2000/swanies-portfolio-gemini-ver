package com.swanie.portfolio.data.repository

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE VAULT & TOWER COORDINATOR
 * Level 4 Control Center for all Data Synchronization.
 */
@Singleton
class DataSyncCoordinator @Inject constructor() {

    private val TAG = "SWAN_SYNC"

    // The "Leaky Bucket" - Minimum time (in ms) between network refreshes
    // Set to 30 seconds to prevent API bans and UI flickering
    private val SYNC_THRESHOLD = 30_000L 
    
    private var lastSyncTimestamp = 0L

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    /**
     * Checks if the "Bucket" has room for a new request.
     */
    fun canRefresh(): Boolean {
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

    /**
     * Signals that a sync has started.
     */
    fun startSync() {
        _syncStatus.value = SyncStatus.Syncing
    }

    /**
     * Signals that a sync has finished and resets the bucket timer.
     */
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