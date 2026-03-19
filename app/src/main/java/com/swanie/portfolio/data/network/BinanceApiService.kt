package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class BinanceExchangeInfo(
    val symbols: List<BinanceSymbol>
)

data class BinanceSymbol(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String
)

data class BinanceTicker(
    val symbol: String,
    val lastPrice: String,
    val priceChangePercent: String
)

interface BinanceApiService {
    @GET("api/v3/exchangeInfo")
    suspend fun getExchangeInfo(): BinanceExchangeInfo

    @GET("api/v3/ticker/24hr")
    suspend fun getTicker24h(@Query("symbol") symbol: String): Response<BinanceTicker>

    @GET("api/v3/ticker/24hr")
    suspend fun getTickers24h(): Response<List<BinanceTicker>>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 168
    ): List<List<Any>>
}
