package com.swanie.portfolio.data.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * API Service for Yahoo Finance Chart Data.
 * Fetches market data for metals using symbols like GC=F (Gold).
 */
interface YahooFinanceApiService {
    @Headers(
        "User-Agent: Mozilla/5.0 (Android 14; Mobile; rv:124.0) Gecko/124.0 Firefox/124.0",
        "Accept: application/json"
    )
    @GET("v8/finance/chart/{ticker}?interval=1h&range=7d")
    suspend fun getTickerData(@Path("ticker") ticker: String): YahooFinanceResponse
}
