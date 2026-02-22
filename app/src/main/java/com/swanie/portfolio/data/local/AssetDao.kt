package com.swanie.portfolio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(assets: List<AssetEntity>)

    // THE FIX: Order by the lastUpdated timestamp in descending order.
    @Query("SELECT * FROM assets ORDER BY lastUpdated DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT coinId FROM assets")
    suspend fun getAllCoinIds(): List<String>

    @Query("DELETE FROM assets WHERE coinId = :coinId")
    suspend fun deleteAsset(coinId: String)

    @Transaction
    suspend fun updateAssetOrder(assets: List<AssetEntity>) {
        assets.forEachIndexed { index, asset ->
            updateAsset(asset.copy(displayOrder = index))
        }
    }

    @Query("UPDATE assets SET amountHeld = :amount, currentPrice = :price, change24h = :change WHERE coinId = :coinId")
    suspend fun updateAsset(coinId: String, amount: Double, price: Double, change: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAsset(asset: AssetEntity)
}
