package com.swanie.portfolio.data.local

import android.util.Log
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSparkline(value: String?): List<Double> {
        val result = value?.split(",")?.mapNotNull { it.toDoubleOrNull() } ?: emptyList()
        Log.d("ADD_TRACE", "CONVERTER_READ: Reading string from DB into list of size ${result.size}")
        return result
    }

    @TypeConverter
    fun toSparkline(list: List<Double>?): String {
        Log.d("ADD_TRACE", "CONVERTER_WRITE: Saving list of size ${list?.size} to DB string.")
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toAssetCategory(value: String) = enumValueOf<AssetCategory>(value)

    @TypeConverter
    fun fromAssetCategory(value: AssetCategory) = value.name
}
