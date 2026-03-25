package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_config")
data class UserConfigEntity(
    @PrimaryKey val id: Int = 1,
    val preferredCurrency: String = "USD",
    val languageCode: String = "en",
    val isBiometricActive: Boolean = false,
    val subscriptionLevel: String = "FREE",
    val showWidgetTotal: Boolean = false,
    val selectedWidgetAssets: String = "", // Comma-separated asset IDs
    val widgetBgColor: String = "#000416",
    val widgetBgTextColor: String = "#FFFFFF",
    val widgetCardColor: String = "#363636",
    val widgetCardTextColor: String = "#C3C3C3"
)
