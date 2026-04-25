package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget_snapshot")
data class WidgetSnapshotEntity(
    @PrimaryKey val id: Int = 1,
    val serializedData: String = "",
    val lastUpdated: Long = 0L,
    val activeVaultName: String = ""
)
