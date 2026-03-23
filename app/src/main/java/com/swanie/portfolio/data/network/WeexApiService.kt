package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// --- WRAPPER MODELS ---

data class WeexResponse<T>(
    @SerializedName("code") val code: String,
    @SerializedName("msg") val msg: String,
    @SerializedName("data") val data: T
)

// --- DATA MODELS ---

data class WeexSymbol(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("baseAsset") val baseAsset: String,
    @SerializedName("quoteAsset") val quoteAsset: String
)

data class WeexTicker(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("lastPrice") val lastPrice: String,
    @SerializedName("priceChangePercent") val priceChangePercent: String
)

interface WeexApiService {
    // Standard WEEX V1 Public Market endpoints
    @GET("api/v1/market/symbols")
    suspend fun getExchangeInfo(): WeexResponse<List<WeexSymbol>>

    @GET("api/v1/market/ticker")
    suspend fun getTicker24h(@Query("symbol") symbol: String): WeexResponse<WeexTicker>

    @GET("api/v1/market/candles")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "1h",
        @Query("limit") limit: Int = 24
    ): WeexResponse<List<List<String>>>
}
