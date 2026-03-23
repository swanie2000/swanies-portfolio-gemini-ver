package com.swanie.portfolio.data.di

import android.content.Context
import com.swanie.portfolio.data.api.SearchEngineRegistry
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.TransactionDao
import com.swanie.portfolio.data.local.UserConfigDao
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
    fun provideAssetDao(database: AppDatabase): AssetDao {
        return database.assetDao()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideUserConfigDao(database: AppDatabase): UserConfigDao {
        return database.userConfigDao()
    }

    @Provides
    @Singleton
    fun provideAssetRepository(
        assetDao: AssetDao,
        searchRegistry: SearchEngineRegistry,
        syncCoordinator: DataSyncCoordinator
    ): AssetRepository {
        return AssetRepository(assetDao, searchRegistry, syncCoordinator)
    }
}
