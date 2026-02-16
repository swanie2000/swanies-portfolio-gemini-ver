package com.swanie.portfolio.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApiService {

    @GET("simple/price")
    suspend fun getPrices(
        @Query("ids") ids: String, // Comma-separated list of coin IDs (e.g., "bitcoin,ripple,ethereum")
        @Query("vs_currencies") vsCurrencies: String = "usd"
    ): Map<String, Map<String, Double>>
}
