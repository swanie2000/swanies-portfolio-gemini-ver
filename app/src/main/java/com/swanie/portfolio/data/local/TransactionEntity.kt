package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val assetId: String,        // links to AssetEntity.coinId
    val type: String,           // INITIAL_ADD, MANUAL_EDIT, SYNC_UPDATE
    val amount: Double,         // Amount held at time of log
    val priceAtTime: Double,    // Price at specific moment
    val timestamp: Long,
    val source: String          // CoinGecko, MEXC, YahooFinance
)
