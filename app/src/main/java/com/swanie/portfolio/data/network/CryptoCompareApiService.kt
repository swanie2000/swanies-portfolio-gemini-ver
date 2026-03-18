package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class CryptoCompareSearchResponse(
    @SerializedName("Data") val data: Map<String, CryptoCompareCoin>
)

data class CryptoCompareCoin(
    @SerializedName("Id") val id: String,
    @SerializedName("Symbol") val symbol: String,
    @SerializedName("FullName") val fullName: String,
    @SerializedName("ImageUrl") val imageUrl: String?
)

data class CryptoComparePriceResponse(
    @SerializedName("RAW") val raw: Map<String, Map<String, CryptoComparePriceData>>
)

data class CryptoComparePriceData(
    @SerializedName("PRICE") val price: Double?,
    @SerializedName("CHANGEPCT24HOUR") val changePct24h: Double?,
    @SerializedName("IMAGEURL") val imageUrl: String?
)

data class CryptoCompareHistoryResponse(
    @SerializedName("Data") val data: CryptoCompareHistoryData
)

data class CryptoCompareHistoryData(
    @SerializedName("Data") val data: List<CryptoCompareHistoryPoint>
)

data class CryptoCompareHistoryPoint(
    @SerializedName("close") val close: Double
)

interface CryptoCompareApiService {
    @GET("data/all/coinlist")
    suspend fun getAllCoins(): CryptoCompareSearchResponse

    @GET("data/pricemultifull")
    suspend fun getPriceFull(
        @Query("fsyms") fsyms: String,
        @Query("tsyms") tsyms: String = "USD"
    ): Response<CryptoComparePriceResponse>

    @GET("data/v2/histohour")
    suspend fun getHistoryHour(
        @Query("fsym") fsym: String,
        @Query("tsym") tsym: String = "USD",
        @Query("limit") limit: Int = 168
    ): Response<CryptoCompareHistoryResponse>
}
