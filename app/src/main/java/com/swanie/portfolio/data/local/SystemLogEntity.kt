package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_logs")
data class SystemLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // INFO, WARN, ERROR
    val tag: String,
    val message: String
)
