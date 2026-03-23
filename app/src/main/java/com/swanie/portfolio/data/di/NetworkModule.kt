package com.swanie.portfolio.di

import com.swanie.portfolio.data.network.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "SwaniesPortfolio/1.0")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides @Singleton @Named("CoinGecko")
    fun provideCoinGeckoRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/api/v3/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton @Named("Yahoo")
    fun provideYahooRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://query1.finance.yahoo.com/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton @Named("KuCoin")
    fun provideKuCoinRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.kucoin.com/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton @Named("Coinbase")
    fun provideCoinbaseRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.coinbase.com/v2/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton @Named("CryptoCompare")
    fun provideCryptoCompareRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://min-api.cryptocompare.com/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    // --- API Service Providers ---

    @Provides
    @Singleton
    fun provideCoinGeckoApiService(@Named("CoinGecko") r: Retrofit): CoinGeckoApiService =
        r.create(CoinGeckoApiService::class.java)

    @Provides
    @Singleton
    fun provideCoinbaseApiService(@Named("Coinbase") r: Retrofit): CoinbaseApiService =
        r.create(CoinbaseApiService::class.java)

    @Provides
    @Singleton
    fun provideKuCoinApiService(@Named("KuCoin") r: Retrofit): KuCoinApiService =
        r.create(KuCoinApiService::class.java)

    @Provides
    @Singleton
    fun provideYahooApiService(@Named("Yahoo") r: Retrofit): YahooFinanceApiService =
        r.create(YahooFinanceApiService::class.java)

    @Provides
    @Singleton
    fun provideCryptoCompareApiService(@Named("CryptoCompare") r: Retrofit): CryptoCompareApiService =
        r.create(CryptoCompareApiService::class.java)
}