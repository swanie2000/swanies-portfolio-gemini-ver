package com.swanie.portfolio.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    // 🛡️ V7 Compatibility Property: Points to the 'MAIN' portfolio by default
    @Query("SELECT * FROM assets WHERE portfolioId = 'MAIN' ORDER BY displayOrder ASC")
    fun getAllAssetsFlow(): Flow<List<AssetEntity>>

    // 🚀 V8 Multi-Portfolio Function
    @Query("SELECT * FROM assets WHERE portfolioId = :pId ORDER BY displayOrder ASC")
    fun getAssetsByPortfolio(pId: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE portfolioId = :pId ORDER BY displayOrder ASC")
    suspend fun getAllAssetsOnce(pId: String = "MAIN"): List<AssetEntity>

    @Query("SELECT * FROM assets WHERE showOnWidget = 1 AND portfolioId = :pId ORDER BY widgetOrder ASC")
    suspend fun getWidgetAssets(pId: String = "MAIN"): List<AssetEntity>

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

    @Query("UPDATE assets SET widgetOrder = :order WHERE coinId = :id")
    suspend fun updateWidgetOrder(id: String, order: Int)
}