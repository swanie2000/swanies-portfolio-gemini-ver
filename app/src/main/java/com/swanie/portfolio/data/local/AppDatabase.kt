package com.swanie.portfolio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AssetEntity::class,
        TransactionEntity::class,
        PortfolioEntity::class,
        UserProfileEntity::class,
        WidgetSnapshotEntity::class,
        UserConfigEntity::class,
        SystemLogEntity::class,
        VaultEntity::class,
        PriceHistoryEntity::class
    ],
    // 🛡️ V11 VAULT KEY: adds userName to user profile
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun userConfigDao(): UserConfigDao
    abstract fun userDao(): UserDao
    abstract fun widgetSnapshotDao(): WidgetSnapshotDao
    abstract fun vaultDao(): VaultDao
    abstract fun priceHistoryDao(): PriceHistoryDao

    companion object {
        const val DB_NAME = "swanie_vault_v11_final"
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // 🛡️ CLEAN-SLATE PROTOCOL: Automatically drops and rebuilds on version change
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed the default vault
                            CoroutineScope(Dispatchers.IO).launch {
                                // Direct SQL insert for the seed to avoid DAO circular dependency on creation
                                // Updated to include sortOrder, appWidgetId, and showWidgetTotal columns
                                db.execSQL("INSERT INTO vaults (id, name, isStarred, baseCurrency, vaultColor, selectedWidgetAssets, widgetBgColor, widgetBgTextColor, widgetCardColor, widgetCardTextColor, showWidgetTotal, sortOrder, appWidgetId) VALUES (1, 'MAIN PORTFOLIO', 1, 'USD', '#000416', '', '#1C1C1E', '#FFFFFF', '#2C2C2E', '#FFFFFF', 1, 0, NULL)")
                                db.execSQL("INSERT INTO user_profile (id, userName, displayName, email, loginPassword, hasAcceptedTOS, subscriptionTier, languageCode, preferredWeightUnit) VALUES (1, '', '', '', '', 0, 0, 'en', 'OZ')")
                                db.execSQL("INSERT INTO widget_snapshot (id, serializedData, lastUpdated, activeVaultName) VALUES (1, '', 0, '')")
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
