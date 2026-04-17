package com.swanie.portfolio.data.di

import android.content.Context
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.*
import com.swanie.portfolio.data.repository.AssetRepository
import com.swanie.portfolio.data.repository.DataSyncCoordinator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideAssetDao(database: AppDatabase): AssetDao {
        return database.assetDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideUserConfigDao(database: AppDatabase): UserConfigDao {
        return database.userConfigDao()
    }

    @Provides
    @Singleton
    fun provideVaultDao(database: AppDatabase): VaultDao {
        return database.vaultDao()
    }

    @Provides
    @Singleton
    fun providePriceHistoryDao(database: AppDatabase): PriceHistoryDao {
        return database.priceHistoryDao()
    }

    @Provides
    @Singleton
    fun provideAssetRepository(
        @ApplicationContext context: Context,
        assetDao: AssetDao,
        priceHistoryDao: PriceHistoryDao,
        userConfigDao: UserConfigDao,
        searchRegistry: SearchEngineRegistry,
        syncCoordinator: DataSyncCoordinator
    ): AssetRepository {
        return AssetRepository(context, assetDao, priceHistoryDao, userConfigDao, searchRegistry, syncCoordinator)
    }
}
