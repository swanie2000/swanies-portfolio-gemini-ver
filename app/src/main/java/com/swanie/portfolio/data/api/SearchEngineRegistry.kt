package com.swanie.portfolio.data.api

import com.swanie.portfolio.data.api.impl.CoinGeckoSearchProvider
import com.swanie.portfolio.data.api.impl.MetalSearchProvider
import com.swanie.portfolio.data.api.impl.KuCoinSearchProvider
import com.swanie.portfolio.data.api.impl.CoinbaseSearchProvider
import com.swanie.portfolio.data.api.impl.CryptoCompareSearchProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchEngineRegistry @Inject constructor(
    coinGeckoProvider: CoinGeckoSearchProvider,
    metalProvider: MetalSearchProvider,
    kuCoinProvider: KuCoinSearchProvider,
    coinbaseProvider: CoinbaseSearchProvider,
    cryptoCompareProvider: CryptoCompareSearchProvider
) {
    private val providers = mutableMapOf<String, SearchProvider>()

    init {
        providers[coinGeckoProvider.name] = coinGeckoProvider
        providers[metalProvider.name] = metalProvider
        providers[kuCoinProvider.name] = kuCoinProvider
        providers[coinbaseProvider.name] = coinbaseProvider
        providers[cryptoCompareProvider.name] = cryptoCompareProvider
    }

    fun getProvider(name: String): SearchProvider? = providers[name]
    
    fun getDefaultProvider(): SearchProvider = providers["CoinGecko"] 
        ?: throw IllegalStateException("Default Search Provider not found")

    fun getMetalProvider(): SearchProvider = providers["YahooFinance"]
        ?: throw IllegalStateException("Metal Provider not found")

    fun getAvailableProviders(): List<String> = providers.keys.toList()
}
