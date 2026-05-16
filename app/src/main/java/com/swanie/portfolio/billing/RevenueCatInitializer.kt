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

        // Release + sandbox key → RevenueCat SDK force-closes the app. Skip configure so you can
        // open the app and fix the next signed bundle (REVENUECAT_PUBLIC_API_KEY must be goog_…).
        if (!BuildConfig.DEBUG && apiKey.startsWith("test_")) {
            Log.e(
                LOG_TAG,
                "Release build contains sandbox RevenueCat key (test_…). " +
                    "Set REVENUECAT_PUBLIC_API_KEY to the Play production goog_… key in local.properties, " +
                    "then Build → Clean Project → Generate Signed App Bundle (release) and upload a new AAB.",
            )
            return
        }

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

