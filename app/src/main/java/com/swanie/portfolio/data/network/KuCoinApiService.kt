package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class KuCoinResponse<T>(
    val code: String,
    val data: T
)

data class KuCoinCurrency(
    val currency: String,
    val name: String,
    val fullName: String,
    val precision: Int,
    val confessionRatios: String?,
    val isMarginEnabled: Boolean,
    val isDebitEnabled: Boolean,
    val isWithdrawEnabled: Boolean,
    val isDepositEnabled: Boolean,
    val isPreConvert: Boolean,
    val iconUrl: String?
)

data class KuCoinTicker(
    val symbol: String?,
    val last: String?,
    val changeRate: String?
)

data class KuCoinSymbolsResponse(
    val symbol: String,
    val name: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val market: String
)

interface KuCoinApiService {
    @GET("api/v1/currencies")
    suspend fun getCurrencies(): KuCoinResponse<List<KuCoinCurrency>>

    @GET("api/v1/market/allTickers")
    suspend fun getAllTickers(): KuCoinResponse<KuCoinTickersData>

    @GET("api/v1/market/stats")
    suspend fun getTicker(@Query("symbol") symbol: String): KuCoinResponse<KuCoinTicker>

    @GET("api/v1/symbols")
    suspend fun getSymbols(): KuCoinResponse<List<KuCoinSymbolsResponse>>

    @GET("api/v1/market/candles")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("type") type: String = "1hour",
        @Query("startAt") startAt: Long? = null,
        @Query("endAt") endAt: Long? = null
    ): KuCoinResponse<List<List<String>>>
}

data class KuCoinTickersData(
    val time: Long,
    val ticker: List<KuCoinTicker>
)
