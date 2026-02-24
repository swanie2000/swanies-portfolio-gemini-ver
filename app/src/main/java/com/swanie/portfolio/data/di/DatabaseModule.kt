package com.swanie.portfolio.data.di

import android.content.Context
import androidx.room.Room
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.AppDatabase
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.RetrofitClient
import com.swanie.portfolio.data.repository.AssetRepository
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "portfolio_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAssetDao(database: AppDatabase): AssetDao {
        return database.assetDao()
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }

    @Provides
    @Singleton
    fun provideCoinGeckoApiService(): CoinGeckoApiService {
        return RetrofitClient.instance
    }

    @Provides
    @Singleton
    fun provideAssetRepository(
        assetDao: AssetDao,
        apiService: CoinGeckoApiService
    ): AssetRepository {
        return AssetRepository(assetDao, apiService)
    }
}