package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class MexcExchangeInfo(
    @SerializedName("symbols") val symbols: List<MexcSymbol>
)

data class MexcSymbol(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("baseAsset") val baseAsset: String,
    @SerializedName("quoteAsset") val quoteAsset: String
)

data class MexcTickerPrice(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: String
)

data class MexcTicker24h(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("lastPrice") val lastPrice: String,
    @SerializedName("priceChangePercent") val priceChangePercent: String
)

interface MexcApiService {
    @GET("api/v3/exchangeInfo")
    suspend fun getExchangeInfo(): MexcExchangeInfo

    @GET("api/v3/ticker/price")
    suspend fun getTickerPrice(@Query("symbol") symbol: String): MexcTickerPrice

    @GET("api/v3/ticker/24hr")
    suspend fun getTicker24h(@Query("symbol") symbol: String): Response<MexcTicker24h>
    
    @GET("api/v3/ticker/24hr")
    suspend fun getTickers24h(): Response<List<MexcTicker24h>>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 168
    ): List<List<Any>>
}
