package com.swanie.portfolio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * This class is the "generator" for Hilt's dependency graph.
 * Without @HiltAndroidApp, the @AndroidEntryPoint in MainActivity
 * will have nothing to connect to.
 */
@HiltAndroidApp
class PortfolioApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize global libraries here if needed
    }
}