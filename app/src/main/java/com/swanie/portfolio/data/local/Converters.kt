package com.swanie.portfolio.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSparkline(value: String?): List<Double> =
        value?.split(",")?.mapNotNull { it.toDoubleOrNull() } ?: emptyList()

    @TypeConverter
    fun toSparkline(list: List<Double>?): String =
        list?.joinToString(",") ?: ""

    @TypeConverter
    fun toAssetCategory(value: String) = enumValueOf<AssetCategory>(value)

    @TypeConverter
    fun fromAssetCategory(value: AssetCategory) = value.name
}
