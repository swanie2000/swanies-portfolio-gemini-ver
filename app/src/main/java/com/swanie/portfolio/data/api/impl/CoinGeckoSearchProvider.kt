package com.swanie.portfolio.data.api.impl

import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchSymbol
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.CoinGeckoApiService
import kotlinx.coroutines.delay
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
                    symbol = coin.symbol,
                    name = coin.name,
                    imageUrl = coin.large,
                    category = AssetCategory.CRYPTO
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        return try {
            // Safety Delay to mitigate 429 Rate Limits
            delay(500)
            val marketData = coinGeckoApiService.getCoinMarkets(ids = ids)
            marketData.map { fresh ->
                AssetEntity(
                    coinId = fresh.id,
                    symbol = fresh.symbol,
                    name = fresh.name,
                    imageUrl = fresh.image ?: "",
                    category = AssetCategory.CRYPTO,
                    currentPrice = fresh.currentPrice ?: 0.0,
                    priceChange24h = fresh.priceChangePercentage24h ?: 0.0,
                    sparklineData = fresh.sparklineIn7d?.price ?: emptyList(),
                    baseSymbol = fresh.symbol
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
