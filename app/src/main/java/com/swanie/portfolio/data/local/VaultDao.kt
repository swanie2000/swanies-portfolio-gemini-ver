package com.swanie.portfolio.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vaults ORDER BY id ASC")
    fun getAllVaultsFlow(): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vaults WHERE id = :id")
    suspend fun getVaultById(id: Int): VaultEntity?

    @Upsert
    suspend fun upsertVault(vault: VaultEntity)

    @Query("UPDATE vaults SET name = :newName WHERE id = :id")
    suspend fun updateVaultName(id: Int, newName: String)

    @Query("UPDATE vaults SET baseCurrency = :code WHERE id = :id")
    suspend fun updateVaultCurrency(id: Int, code: String)

    @Delete
    suspend fun deleteVault(vault: VaultEntity)
}
