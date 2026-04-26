package com.swanie.portfolio.billing

import android.app.Application
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
        if (apiKey.isBlank()) return

        if (runCatching { Purchases.sharedInstance }.isSuccess) return

        Purchases.logLevel = LogLevel.INFO
        Purchases.configure(
            PurchasesConfiguration.Builder(application, apiKey).build()
        )
    }
}

