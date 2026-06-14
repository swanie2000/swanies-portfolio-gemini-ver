package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class WeexExchangeInfo(
    @SerializedName("symbols") val symbols: List<WeexSymbol>
)

data class WeexSymbol(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("baseAsset") val baseAsset: String,
    @SerializedName("quoteAsset") val quoteAsset: String,
    @SerializedName("status") val status: String? = null
)

data class WeexTicker24h(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("lastPrice") val lastPrice: String,
    @SerializedName("priceChangePercent") val priceChangePercent: String
)

interface WeexApiService {
  @GET("api/v3/exchangeInfo")
  suspend fun getExchangeInfo(
      @Query("symbolStatus") symbolStatus: String = "TRADING",
  ): WeexExchangeInfo

  @GET("api/v3/market/ticker/24hr")
  suspend fun getTicker24h(@Query("symbol") symbol: String): Response<WeexTicker24h>

  @GET("api/v3/market/klines")
  suspend fun getKlines(
      @Query("symbol") symbol: String,
      @Query("interval") interval: String,
      @Query("limit") limit: Int = 168,
  ): List<List<Any>>
}
