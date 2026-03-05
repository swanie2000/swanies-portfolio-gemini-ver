package com.swanie.portfolio.di

import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.YahooFinanceApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("CoinGecko")
    fun provideCoinGeckoRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("Yahoo")
    fun provideYahooRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://query1.finance.yahoo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideCoinGeckoApiService(
        @Named("CoinGecko") retrofit: Retrofit
    ): CoinGeckoApiService = retrofit.create(CoinGeckoApiService::class.java)

    @Provides
    @Singleton
    fun provideYahooApiService(
        @Named("Yahoo") retrofit: Retrofit
    ): YahooFinanceApiService = retrofit.create(YahooFinanceApiService::class.java)
}