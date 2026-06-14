package com.swanie.portfolio.di

import com.swanie.portfolio.BuildConfig
import com.swanie.portfolio.data.network.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
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

    /** Form relays often reject API-style user agents; use a normal mobile browser fingerprint. */
    @Provides
    @Singleton
    @Named("Feedback")
    fun provideFeedbackOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36",
                )
                .header("Accept", "application/json, text/plain, text/html, */*;q=0.8")
                .build()
            chain.proceed(req)
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
    fun provideCryptoCompareOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
                .header("User-Agent", "SwaniesPortfolio/1.0")
                .header("Accept", "application/json")
            val apiKey = BuildConfig.CRYPTOCOMPARE_API_KEY.trim()
            if (apiKey.isNotEmpty()) {
                builder.header("authorization", "Apikey $apiKey")
            }
            chain.proceed(builder.build())
        }
        .build()

    @Provides @Singleton @Named("CryptoCompare")
    fun provideCryptoCompareRetrofit(@Named("CryptoCompare") client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://min-api.cryptocompare.com/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton @Named("Azbit")
    fun provideAzbitRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://data.azbit.com/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    /** MEXC public API — browser UA helps some regional blocks (see MexcSearchProvider). */
    @Provides @Singleton @Named("MEXC")
    fun provideMexcOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36",
                )
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides @Singleton @Named("MEXC")
    fun provideMexcRetrofit(@Named("MEXC") client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.mexc.com/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides @Singleton @Named("Jupiter")
    fun provideJupiterLiteRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://lite-api.jup.ag/").client(client)
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

    @Provides
    @Singleton
    fun provideAzbitApiService(@Named("Azbit") r: Retrofit): AzbitApiService =
        r.create(AzbitApiService::class.java)

    @Provides
    @Singleton
    fun provideMexcApiService(@Named("MEXC") r: Retrofit): MexcApiService =
        r.create(MexcApiService::class.java)

    @Provides
    @Singleton
    fun provideJupiterTokenApiService(@Named("Jupiter") r: Retrofit): JupiterTokenApiService =
        r.create(JupiterTokenApiService::class.java)
}