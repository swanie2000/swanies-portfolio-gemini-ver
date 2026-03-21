package com.swanie.portfolio.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY displayOrder ASC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets ORDER BY displayOrder ASC")
    suspend fun getAllAssetsOnce(): List<AssetEntity>

    @Query("SELECT * FROM assets WHERE showOnWidget = 1 ORDER BY displayOrder ASC")
    suspend fun getWidgetAssets(): List<AssetEntity>

    @Upsert
    suspend fun upsertAsset(asset: AssetEntity)

    @Upsert
    suspend fun upsertAll(assets: List<AssetEntity>)

    @Query("DELETE FROM assets WHERE coinId = :id")
    suspend fun deleteAssetById(id: String)

    @Update
    suspend fun updateAssetEntity(asset: AssetEntity)

    @Query("UPDATE assets SET showOnWidget = :isVisible WHERE coinId = :id")
    suspend fun updateWidgetVisibility(id: String, isVisible: Boolean)

    @Transaction
    suspend fun updateAssetOrder(assets: List<AssetEntity>) {
        assets.forEachIndexed { index, asset ->
            updateAssetDisplayOrder(asset.coinId, index)
        }
    }

    @Query("UPDATE assets SET displayOrder = :order WHERE coinId = :id")
    suspend fun updateAssetDisplayOrder(id: String, order: Int)
}