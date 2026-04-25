package com.swanie.portfolio.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetSnapshotDao {
    @Query("SELECT * FROM widget_snapshot WHERE id = 1 LIMIT 1")
    fun getSnapshotFlow(): Flow<WidgetSnapshotEntity?>

    @Query("SELECT * FROM widget_snapshot WHERE id = 1 LIMIT 1")
    suspend fun getSnapshot(): WidgetSnapshotEntity?

    @Upsert
    suspend fun upsertSnapshot(snapshot: WidgetSnapshotEntity)
}
