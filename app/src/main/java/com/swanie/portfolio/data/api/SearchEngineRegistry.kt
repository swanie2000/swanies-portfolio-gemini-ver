package com.swanie.portfolio.data.api

import com.swanie.portfolio.data.api.impl.CoinGeckoSearchProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchEngineRegistry @Inject constructor(
    coinGeckoProvider: CoinGeckoSearchProvider
) {
    private val providers = mutableMapOf<String, SearchProvider>()

    init {
        providers[coinGeckoProvider.name] = coinGeckoProvider
    }

    fun getProvider(name: String): SearchProvider? = providers[name]
    
    fun getDefaultProvider(): SearchProvider = providers["CoinGecko"] 
        ?: throw IllegalStateException("Default Search Provider not found")

    fun getAvailableProviders(): List<String> = providers.keys.toList()
}
