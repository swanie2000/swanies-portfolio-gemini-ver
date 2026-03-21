package com.swanie.portfolio.data.network

import retrofit2.http.GET
import retrofit2.http.Path

interface CoinbaseApiService {

    // 1. Gets the master list of all tradeable crypto assets
    @GET("https://api.exchange.coinbase.com/currencies")
    suspend fun getExchangeCurrencies(): List<CoinbaseExchangeCurrency>

    // 2. Gets real-time spot price (e.g., BTC-USD)
    @GET("https://api.coinbase.com/v2/prices/{pair}/spot")
    suspend fun getSpotPrice(@Path("pair") pair: String): CoinbasePriceResponse

    /**
     * SURGICAL: Restores Sparklines.
     * Fetches historical price points (Candles) for the last 24 hours.
     * Granularity 3600 = 1 hour intervals.
     */
    @GET("https://api.exchange.coinbase.com/products/{pair}/candles?granularity=3600")
    suspend fun getExchangeCandles(@Path("pair") pair: String): List<List<Double>>
}

// --- DATA MODELS ---

data class CoinbaseExchangeCurrency(
    val id: String,         // e.g., "BTC"
    val name: String,       // e.g., "Bitcoin"
    val status: String
)

data class CoinbasePriceResponse(val data: CoinbasePriceData)
data class CoinbasePriceData(val amount: String, val currency: String)