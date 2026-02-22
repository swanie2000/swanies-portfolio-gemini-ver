package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName

data class CoinMarketResponse(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("current_price") val currentPrice: Double?,
    @SerializedName("market_cap_rank") val marketCapRank: Int?,
    @SerializedName("price_change_24h") val priceChange24h: Double?,
    @SerializedName("sparkline_in_7d") val sparklineIn7d: SparklineData?
)

data class SparklineData(
    @SerializedName("price") val price: List<Double>?
)
