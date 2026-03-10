package com.swanie.portfolio.data.di

import android.content.Context
import com.swanie.portfolio.data.ThemePreferences // ADDED IMPORT
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.YahooFinanceApiService
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

    // NEW: Tells Hilt how to create ThemePreferences using the App Context
    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }

    @Provides
    @Singleton
    fun provideAssetRepository(
        assetDao: AssetDao,
        coinGeckoApiService: CoinGeckoApiService,
        yahooFinanceApiService: YahooFinanceApiService,
        syncCoordinator: DataSyncCoordinator
    ): AssetRepository {
        return AssetRepository(
            assetDao,
            coinGeckoApiService,
            yahooFinanceApiService,
            syncCoordinator
        )
    }
}