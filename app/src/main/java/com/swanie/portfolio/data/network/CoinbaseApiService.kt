package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class CoinbasePriceResponse(
    val data: CoinbasePriceData
)

data class CoinbasePriceData(
    val base: String,
    val currency: String,
    val amount: String
)

data class CoinbaseCurrencyResponse(
    val data: List<CoinbaseCurrency>
)

data class CoinbaseCurrency(
    val id: String,
    val name: String,
    val min_size: String
)

interface CoinbaseApiService {
    @GET("prices/{symbol}/spot")
    suspend fun getSpotPrice(@Path("symbol") symbol: String): CoinbasePriceResponse

    // MASTER LIST: Using the Exchange API's public currencies list for full crypto coverage
    @GET("https://api.exchange.coinbase.com/currencies")
    suspend fun getExchangeCurrencies(): List<CoinbaseExchangeCurrency>

    // CANDLES: Using the Exchange API for sparkline data
    @GET("https://api.exchange.coinbase.com/products/{symbol}/candles")
    suspend fun getCandles(
        @Path("symbol") symbol: String,
        @Query("granularity") granularity: Int = 3600
    ): List<List<Double>>
}

data class CoinbaseExchangeCurrency(
    val id: String,
    val name: String,
    val status: String
)
