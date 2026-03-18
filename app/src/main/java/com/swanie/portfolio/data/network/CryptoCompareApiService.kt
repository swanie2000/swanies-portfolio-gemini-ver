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

interface CryptoCompareApiService {
    @GET("data/all/coinlist")
    suspend fun getAllCoins(): CryptoCompareSearchResponse

    @GET("data/pricemultifull")
    suspend fun getPriceFull(
        @Query("fsyms") fsyms: String,
        @Query("tsyms") tsyms: String = "USD"
    ): Response<CryptoComparePriceResponse>
}
