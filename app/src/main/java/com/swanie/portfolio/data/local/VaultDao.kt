package com.swanie.portfolio.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vaults ORDER BY sortOrder ASC")
    fun getAllVaultsFlow(): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vaults ORDER BY sortOrder ASC")
    suspend fun getAllVaults(): List<VaultEntity>

    @Query("SELECT * FROM vaults WHERE id = :id")
    suspend fun getVaultById(id: Int): VaultEntity?

    @Upsert
    suspend fun upsertVault(vault: VaultEntity)

    @Query("UPDATE vaults SET name = :newName WHERE id = :id")
    suspend fun updateVaultName(id: Int, newName: String)

    @Query("UPDATE vaults SET baseCurrency = :code WHERE id = :id")
    suspend fun updateVaultCurrency(id: Int, code: String)

    @Query("UPDATE vaults SET selectedWidgetAssets = :assets WHERE id = :vaultId")
    suspend fun updateSelectedWidgetAssets(vaultId: Int, assets: String)

    @Query("UPDATE vaults SET widgetBgColor = :bg, widgetBgTextColor = :bgTxt, widgetCardColor = :card, widgetCardTextColor = :cardTxt WHERE id = :vaultId")
    suspend fun updateWidgetColors(vaultId: Int, bg: String, bgTxt: String, card: String, cardTxt: String)

    @Query("UPDATE vaults SET appWidgetId = :appWidgetId WHERE id = :vaultId")
    suspend fun updateAppWidgetId(vaultId: Int, appWidgetId: Int?)

    @Query("UPDATE vaults SET appWidgetId = NULL WHERE appWidgetId = :appWidgetId")
    suspend fun clearAppWidgetId(appWidgetId: Int)

    @Query("UPDATE vaults SET showWidgetTotal = :show WHERE id = :vaultId")
    suspend fun updateShowWidgetTotal(vaultId: Int, show: Boolean)

    @Query("SELECT * FROM vaults WHERE appWidgetId = :appWidgetId LIMIT 1")
    suspend fun getVaultByAppWidgetId(appWidgetId: Int): VaultEntity?

    @Delete
    suspend fun deleteVault(vault: VaultEntity)
}
