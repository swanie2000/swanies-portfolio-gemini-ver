package com.swanie.portfolio.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface AzbitApiService {
    @GET("api/currencies")
    suspend fun getCurrencies(): List<String>

    @GET("api/tickers")
    suspend fun getTickers(
        @Query("currencyPairCode") currencyPairCode: String? = null
    ): List<AzbitTicker>

    @GET("api/ohlc")
    suspend fun getOhlc(
        @Query("interval") interval: String = "hour",
        @Query("currencyPairCode") currencyPairCode: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): List<AzbitOhlcPoint>
}

data class AzbitTicker(
    val currencyPairCode: String,
    val price: Double?,
    val priceChangePercentage24h: Double?
)

data class AzbitOhlcPoint(
    val close: Double?
)

