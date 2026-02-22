package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// --- Data Models for API Responses ---

data class CoinSearchResult(
    @SerializedName("coins") val coins: List<CoinSummary>
)

data class CoinSummary(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("large") val large: String
)

// --- API Service Interface ---

interface CoinGeckoApiService {

    @GET("simple/price")
    suspend fun getSimplePrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd"
    ): Map<String, Map<String, Double>>

    @GET("search")
    suspend fun search(
        @Query("query") query: String
    ): CoinSearchResult

    @GET("coins/markets")
    suspend fun getCoinMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("ids") ids: String,
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChange: String = "24h"
    ): List<CoinMarketResponse>
}