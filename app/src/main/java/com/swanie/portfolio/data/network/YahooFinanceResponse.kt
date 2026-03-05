package com.swanie.portfolio.data.network

import com.google.gson.annotations.SerializedName

/**
 * Yahoo Finance Chart API Response Model.
 * Single source of truth for Precious Metals (XAU, XAG, XPT, XPD).
 */
data class YahooFinanceResponse(
    @SerializedName("chart") val chart: ChartData
)

data class ChartData(
    @SerializedName("result") val result: List<ChartResult>?
)

data class ChartResult(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("indicators") val indicators: Indicators?
)

data class Meta(
    @SerializedName("symbol") val symbol: String?,
    @SerializedName("regularMarketPrice") val regularMarketPrice: Double?,
    @SerializedName("chartPreviousClose") val chartPreviousClose: Double?,
    @SerializedName("regularMarketDayHigh") val regularMarketDayHigh: Double?,
    @SerializedName("regularMarketDayLow") val regularMarketDayLow: Double?,
    @SerializedName("regularMarketChangePercent") val regularMarketChangePercent: Double?
)

data class Indicators(
    @SerializedName("quote") val quote: List<Quote>?
)

data class Quote(
    @SerializedName("close") val close: List<Double?>?
)
