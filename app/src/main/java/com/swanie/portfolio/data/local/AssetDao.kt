package com.swanie.portfolio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Upsert
    suspend fun upsertAsset(asset: AssetEntity)

    @Upsert
    suspend fun upsertAll(assets: List<AssetEntity>)

    @Query("SELECT * FROM assets ORDER BY lastUpdated DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE coinId = :coinId")
    suspend fun getAssetById(coinId: String): AssetEntity?

    @Query("SELECT coinId FROM assets")
    suspend fun getAllCoinIds(): List<String>

    @Query("DELETE FROM assets WHERE coinId = :coinId")
    suspend fun deleteAsset(coinId: String)

    @Transaction
    suspend fun updateAssetOrder(assets: List<AssetEntity>) {
        assets.forEachIndexed { index, asset ->
            val updatedAsset = asset.copy(displayOrder = index)
            upsertAsset(updatedAsset)
        }
    }

    @Query("UPDATE assets SET amountHeld = :amount, currentPrice = :price, change24h = :change WHERE coinId = :coinId")
    suspend fun updateAsset(coinId: String, amount: Double, price: Double, change: Double)
}
