package com.swanie.portfolio.di

import com.swanie.portfolio.billing.MonetizationManager
import com.swanie.portfolio.billing.RevenueCatMonetizationManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonetizationModule {

    @Binds
    @Singleton
    abstract fun bindMonetizationManager(
        impl: RevenueCatMonetizationManager
    ): MonetizationManager
}

