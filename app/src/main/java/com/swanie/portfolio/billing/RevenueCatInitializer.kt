package com.swanie.portfolio.billing

import android.app.Application
import android.util.Log
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.swanie.portfolio.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueCatInitializer @Inject constructor() {

    fun initialize(application: Application) {
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isBlank()) {
            Log.w(LOG_TAG, "REVENUECAT_API_KEY is blank — billing disabled")
            return
        }

        val keyKind =
            when {
                apiKey.startsWith("goog_") -> "production"
                apiKey.startsWith("test_") -> "sandbox"
                else -> "unknown"
            }
        Log.i(LOG_TAG, "configure Purchases keyKind=$keyKind prefix=${apiKey.take(8)}…")

        if (runCatching { Purchases.sharedInstance }.isSuccess) return

        Purchases.logLevel = LogLevel.INFO
        Purchases.configure(
            PurchasesConfiguration.Builder(application, apiKey).build()
        )
    }

    companion object {
        const val LOG_TAG = "SwanieRevenueCat"
    }
}

