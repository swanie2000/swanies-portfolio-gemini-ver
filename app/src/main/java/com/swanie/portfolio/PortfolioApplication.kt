package com.swanie.portfolio

import android.app.Application
import com.swanie.portfolio.billing.RevenueCatInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * This class is the "generator" for Hilt's dependency graph.
 * Without @HiltAndroidApp, the @AndroidEntryPoint in MainActivity
 * will have nothing to connect to.
 */
@HiltAndroidApp
class PortfolioApplication : Application() {
    @Inject
    lateinit var revenueCatInitializer: RevenueCatInitializer

    override fun onCreate() {
        super.onCreate()
        revenueCatInitializer.initialize(this)
    }
}