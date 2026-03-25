package com.swanie.portfolio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricePoint(pricePoint: PriceHistoryEntity)

    @Query("SELECT * FROM price_history WHERE assetId = :assetId ORDER BY timestamp DESC LIMIT 168")
    suspend fun getRecentHistory(assetId: String): List<PriceHistoryEntity>

    @Query("DELETE FROM price_history WHERE assetId = :assetId AND timestamp NOT IN (SELECT timestamp FROM price_history WHERE assetId = :assetId ORDER BY timestamp DESC LIMIT 168)")
    suspend fun pruneHistory(assetId: String)
}
