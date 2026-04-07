package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaults")
data class VaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val baseCurrency: String = "USD",
    val vaultColor: String = "#000416",
    val selectedWidgetAssets: String = ""
)
