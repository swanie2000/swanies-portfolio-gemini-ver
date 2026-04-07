package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaults")
data class VaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val baseCurrency: String = "USD",
    val vaultColor: String = "#000416",
    val selectedWidgetAssets: String = "",
    val widgetBgColor: String = "#1C1C1E",
    val widgetBgTextColor: String = "#FFFFFF",
    val widgetCardColor: String = "#2C2C2E",
    val widgetCardTextColor: String = "#FFFFFF"
)
