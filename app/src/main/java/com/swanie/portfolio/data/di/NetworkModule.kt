package com.swanie.portfolio.di

import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.YahooFinanceApiService
import com.swanie.portfolio.data.network.KuCoinApiService
import com.swanie.portfolio.data.network.CoinbaseApiService
import com.swanie.portfolio.data.network.CryptoCompareApiService
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
    @Named("KuCoin")
    fun provideKuCoinRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.kucoin.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("Coinbase")
    fun provideCoinbaseRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.coinbase.com/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("CryptoCompare")
    fun provideCryptoCompareRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://min-api.cryptocompare.com/")
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

    @Provides
    @Singleton
    fun provideKuCoinApiService(
        @Named("KuCoin") retrofit: Retrofit
    ): KuCoinApiService = retrofit.create(KuCoinApiService::class.java)

    @Provides
    @Singleton
    fun provideCoinbaseApiService(
        @Named("Coinbase") retrofit: Retrofit
    ): CoinbaseApiService = retrofit.create(CoinbaseApiService::class.java)

    @Provides
    @Singleton
    fun provideCryptoCompareApiService(
        @Named("CryptoCompare") retrofit: Retrofit
    ): CryptoCompareApiService = retrofit.create(CryptoCompareApiService::class.java)
}