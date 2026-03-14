package com.swanie.portfolio.data.api.impl

import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchSymbol
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.network.CoinGeckoApiService
import javax.inject.Inject

class CoinGeckoSearchProvider @Inject constructor(
    private val coinGeckoApiService: CoinGeckoApiService
) : SearchProvider {
    override val name: String = "CoinGecko"

    override suspend fun search(query: String): List<SearchSymbol> {
        return try {
            val result = coinGeckoApiService.search(query)
            result.coins.map { coin ->
                SearchSymbol(
                    id = coin.id,
                    symbol = coin.symbol ?: "",
                    name = coin.name ?: "",
                    imageUrl = coin.large ?: "",
                    category = AssetCategory.CRYPTO
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
