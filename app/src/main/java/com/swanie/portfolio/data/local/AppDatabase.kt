package com.swanie.portfolio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AssetEntity::class,
        TransactionEntity::class,
        PortfolioEntity::class,
        UserConfigEntity::class,
        SystemLogEntity::class,
        VaultEntity::class,
        PriceHistoryEntity::class // V15: 168-Point History
    ],
    version = 15, // Incremented to V15 for Global Vista (Price History + Icons)
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userConfigDao(): UserConfigDao
    abstract fun vaultDao(): VaultDao
    abstract fun priceHistoryDao(): PriceHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "swanie_portfolio_v8_final"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed the default vault
                            CoroutineScope(Dispatchers.IO).launch {
                                // Direct SQL insert for the seed to avoid DAO circular dependency on creation
                                db.execSQL("INSERT INTO vaults (id, name, baseCurrency, vaultColor) VALUES (1, 'MAIN PORTFOLIO', 'USD', '#000416')")
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
