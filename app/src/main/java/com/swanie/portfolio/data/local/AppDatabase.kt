package com.swanie.portfolio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AssetEntity::class,
        TransactionEntity::class,
        PortfolioEntity::class,
        UserConfigEntity::class,
        SystemLogEntity::class
    ],
    version = 9, // Incremented version for UserConfig update
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userConfigDao(): UserConfigDao

    companion object {
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to user_config
                db.execSQL("ALTER TABLE user_config ADD COLUMN showWidgetTotal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_config ADD COLUMN selectedWidgetAssets TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE assets ADD COLUMN portfolioId TEXT NOT NULL DEFAULT 'MAIN'")
                db.execSQL("ALTER TABLE assets ADD COLUMN widgetOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS portfolios (id TEXT NOT NULL, name TEXT NOT NULL, colorHex TEXT NOT NULL, isDefault INTEGER NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE TABLE IF NOT EXISTS user_config (id INTEGER NOT NULL, preferredCurrency TEXT NOT NULL, languageCode TEXT NOT NULL, isBiometricActive INTEGER NOT NULL, subscriptionLevel TEXT NOT NULL, PRIMARY KEY(id))")
                db.execSQL("INSERT OR IGNORE INTO user_config (id, preferredCurrency, languageCode, isBiometricActive, subscriptionLevel) VALUES (1, 'USD', 'en', 0, 'FREE')")
                db.execSQL("CREATE TABLE IF NOT EXISTS system_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, level TEXT NOT NULL, tag TEXT NOT NULL, message TEXT NOT NULL)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "swanie_portfolio_v8_final"
                )
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
