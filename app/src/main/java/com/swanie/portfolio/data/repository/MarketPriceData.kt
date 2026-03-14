package com.swanie.portfolio.data.repository

data class MarketPriceData(
    val current: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val changePercent: Double,
    val sparkline: List<Double>
)