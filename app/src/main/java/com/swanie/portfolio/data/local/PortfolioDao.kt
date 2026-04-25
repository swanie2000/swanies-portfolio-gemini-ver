package com.swanie.portfolio.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolios ORDER BY name ASC")
    fun getAllPortfoliosFlow(): Flow<List<PortfolioEntity>>

    @Upsert
    suspend fun upsertPortfolio(portfolio: PortfolioEntity)

    @Query("UPDATE portfolios SET isStarred = 0")
    suspend fun clearStarredPortfolioFlag()

    @Query("UPDATE portfolios SET isStarred = 1 WHERE vaultId = :vaultId")
    suspend fun setStarredPortfolioFlag(vaultId: Int)

    @Transaction
    suspend fun setStarredPortfolio(vaultId: Int) {
        clearStarredPortfolioFlag()
        setStarredPortfolioFlag(vaultId)
    }
}
