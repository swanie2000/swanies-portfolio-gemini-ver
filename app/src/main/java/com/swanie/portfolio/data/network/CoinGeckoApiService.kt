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
    @SerializedName("symbol") val symbol: String
)

// Rich data model for /coins/markets endpoint
data class MarketData(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("current_price") val currentPrice: Double?,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double?
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

    // New endpoint for fetching detailed market data
    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("ids") ids: String // Comma-separated list of coin IDs
    ): List<MarketData>
}
