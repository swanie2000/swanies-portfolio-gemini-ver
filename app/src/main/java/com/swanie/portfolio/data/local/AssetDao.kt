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

    // 🌐 GLOBAL VISTA: Multi-Vault Filtering
    @Query("SELECT * FROM assets WHERE vaultId = :vaultId ORDER BY displayOrder ASC")
    fun getAssetsByVault(vaultId: Int): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE vaultId = :vaultId ORDER BY displayOrder ASC")
    suspend fun getAssetsByVaultOnce(vaultId: Int): List<AssetEntity>

    @Query("SELECT * FROM assets WHERE portfolioId = :pId ORDER BY displayOrder ASC")
    suspend fun getAllAssetsOnce(pId: String = "MAIN"): List<AssetEntity>

    /**
     * 🛰️ GLOBAL SYNC PAYLOAD: Fetches every asset across all vaults and portfolios.
     * Used for the 'Sovereign Vault' cloud backup to prevent data loss.
     */
    @Query("SELECT * FROM assets ORDER BY displayOrder ASC")
    suspend fun getAllAssetsGlobal(): List<AssetEntity>

    /**
     * 🎯 V38.5 IRON GRAVITY: RAW PIPE
     * Unfiltered stream of all assets across all vaults.
     */
    @Query("SELECT * FROM assets ORDER BY widgetOrder ASC, displayOrder ASC")
    fun getAllAssetsGlobalFlow(): Flow<List<AssetEntity>>

    /**
     * 🎯 V38.14 TRANSACTIONAL GRAVITY: EXPLICIT WIDGET ORDER
     * Definitive source of truth for ordered widget assets.
     */
    @Query("SELECT * FROM assets WHERE vaultId = :vId ORDER BY widgetOrder ASC")
    fun getAssetsOrderedByWidget(vId: Int): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE showOnWidget = 1 AND vaultId = :vId ORDER BY widgetOrder ASC")
    suspend fun getWidgetAssetsByVault(vId: Int): List<AssetEntity>

    @Query("SELECT * FROM assets WHERE showOnWidget = 1 AND portfolioId = :pId ORDER BY widgetOrder ASC")
    suspend fun getWidgetAssets(pId: String = "MAIN"): List<AssetEntity>

    @Upsert
    suspend fun upsertAsset(asset: AssetEntity)

    @Upsert
    suspend fun upsertAll(assets: List<AssetEntity>)

    @Query("DELETE FROM assets WHERE coinId = :id")
    suspend fun deleteAssetById(id: String)

    @Query("DELETE FROM assets")
    suspend fun deleteAll()

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

    /**
     * 🎯 V38.12 SEQUENTIAL HAMMER: Atomic Bulk Re-indexing
     * Enforces a hard sequential write for the entire set to break any index deadlocks.
     */
    @Transaction
    suspend fun updateWidgetOrderBulk(assetIds: List<String>) {
        assetIds.forEachIndexed { index, id ->
            updateWidgetOrder(id, index)
        }
    }

    @Query("UPDATE assets SET displayOrder = :order WHERE coinId = :id")
    suspend fun updateAssetDisplayOrder(id: String, order: Int)

    @Query("UPDATE assets SET widgetOrder = :order WHERE coinId = :id")
    suspend fun updateWidgetOrder(id: String, order: Int)
}
