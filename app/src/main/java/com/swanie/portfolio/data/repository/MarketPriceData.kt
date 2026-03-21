package com.swanie.portfolio.data.repository

data class MarketPriceData(
    val officialSpotPrice: Double = 0.0,
    val dayHigh: Double = 0.0,
    val dayLow: Double = 0.0,
    val changePercent: Double = 0.0,
    val sparkline: List<Double> = emptyList()
)