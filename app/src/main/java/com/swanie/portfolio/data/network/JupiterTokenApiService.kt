package com.swanie.portfolio.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Public Jupiter Lite API — used to resolve icons for exchange-only tickers
 * (e.g. Solana pump.fun tokens) that are not on CoinCap.
 */
interface JupiterTokenApiService {
    @GET("tokens/v2/search")
    suspend fun search(@Query("query") query: String): List<JupiterTokenSearchItem>
}

data class JupiterTokenSearchItem(
    val id: String,
    val name: String,
    val symbol: String,
    val icon: String?,
    val liquidity: Double? = null,
    val mcap: Double? = null,
    val holderCount: Int? = null
)
