package com.swanie.portfolio.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface KuCoinApiService {
    @GET("api/v1/symbols")
    suspend fun getSymbols(): KuCoinSymbolsResponse

    @GET("api/v1/market/orderbook/level1")
    suspend fun getTicker(@Query("symbol") symbol: String): KuCoinTickerResponse

    // SURGICAL: Restores Sparklines (Last 24 hours of data)
    @GET("api/v1/market/candles")
    suspend fun getKLines(
        @Query("symbol") symbol: String,
        @Query("type") type: String = "1hour"
    ): KuCoinKLineResponse
}

data class KuCoinSymbolsResponse(val data: List<KuCoinSymbol>)
data class KuCoinSymbol(val symbol: String, val name: String, val baseCurrency: String)

data class KuCoinTickerResponse(val data: KuCoinTickerData)
data class KuCoinTickerData(val price: String)

data class KuCoinKLineResponse(
    val data: List<List<String>> // KuCoin returns: [time, open, close, high, low, vol, turnover]
)