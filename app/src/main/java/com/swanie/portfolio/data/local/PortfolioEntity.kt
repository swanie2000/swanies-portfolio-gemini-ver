package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolios")
data class PortfolioEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val isDefault: Boolean
)
