package com.swanie.portfolio.data.repository

import android.util.Log
import com.swanie.portfolio.data.MetalsProvider
import com.swanie.portfolio.data.local.AssetDao
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.network.CoinGeckoApiService
import com.swanie.portfolio.data.network.CoinMarketResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val coinGeckoApiService: CoinGeckoApiService
) {

    val allAssets = assetDao.getAllAssets()

    suspend fun refreshAssets() {
        val allLocalAssets = allAssets.first()
        if (allLocalAssets.isEmpty()) return

        val cryptoAssets = allLocalAssets.filter { it.category == AssetCategory.CRYPTO }
        val metalAssets = allLocalAssets.filter { it.category == AssetCategory.METAL }

        val cryptoIds = cryptoAssets.map { it.coinId }

        val updatedAssets = mutableListOf<AssetEntity>()

        if (cryptoIds.isNotEmpty()) {
            try {
                val marketData: List<CoinMarketResponse> = coinGeckoApiService.getCoinMarkets(ids = cryptoIds.joinToString(","))
                val marketDataMap = marketData.associateBy { it.id }

                cryptoAssets.forEach { currentAsset ->
                    val marketInfo = marketDataMap[currentAsset.coinId]
                    if (marketInfo != null) {
                        updatedAssets.add(currentAsset.copy(
                            currentPrice = marketInfo.currentPrice ?: currentAsset.currentPrice,
                            priceChange24h = marketInfo.priceChange24h ?: currentAsset.priceChange24h,
                            marketCapRank = marketInfo.marketCapRank ?: currentAsset.marketCapRank,
                            sparklineData = marketInfo.sparklineIn7d?.price ?: currentAsset.sparklineData,
                            lastUpdated = System.currentTimeMillis()
                        ))
                    } else {
                        updatedAssets.add(currentAsset)
                    }
                }
            } catch (e: Exception) {
                Log.e("AssetRepository", "Crypto refresh failed", e)
                updatedAssets.addAll(cryptoAssets)
            }
        }

        metalAssets.forEach { currentMetal ->
            if (currentMetal.isCustom) {
                val spotPrice = MetalsProvider.preciousMetals.find { it.symbol == currentMetal.baseSymbol }?.currentPrice ?: 0.0
                val pricePerUnit = (spotPrice + currentMetal.premium)
                updatedAssets.add(currentMetal.copy(
                    currentPrice = pricePerUnit,
                    lastUpdated = System.currentTimeMillis()
                ))
            } else {
                val providerData = MetalsProvider.preciousMetals.find { it.coinId == currentMetal.coinId }
                if (providerData != null) {
                    updatedAssets.add(currentMetal.copy(
                        currentPrice = providerData.currentPrice,
                        lastUpdated = System.currentTimeMillis()
                    ))
                } else {
                    updatedAssets.add(currentMetal)
                }
            }
        }

        if (updatedAssets.isNotEmpty()) {
            assetDao.upsertAll(updatedAssets)
        }
    }

    suspend fun searchCoins(query: String): List<AssetEntity> {
        if (query.length < 2) return emptyList()
        return try {
            val searchResult = coinGeckoApiService.search(query)
            searchResult.coins.map { coin ->
                AssetEntity(
                    coinId = coin.id,
                    symbol = coin.symbol,
                    name = coin.name,
                    imageUrl = coin.large,
                    amountHeld = 0.0,
                    currentPrice = 0.0,
                    change24h = 0.0,
                    displayOrder = 0,
                    lastUpdated = 0L,
                    sparklineData = emptyList(),
                    marketCapRank = 0,
                    priceChange24h = 0.0,
                    category = AssetCategory.CRYPTO
                )
            }
        } catch (e: Exception) {
            Log.e("AssetRepository", "Failed to search coins", e)
            emptyList()
        }
    }

    suspend fun saveAsset(asset: AssetEntity) {
        val existingAsset = assetDao.getAssetById(asset.coinId)
        if (existingAsset != null) {
            val updatedAsset = existingAsset.copy(
                amountHeld = existingAsset.amountHeld + asset.amountHeld,
                lastUpdated = System.currentTimeMillis()
            )
            assetDao.upsertAsset(updatedAsset)
        } else {
            assetDao.upsertAsset(asset.copy(lastUpdated = System.currentTimeMillis()))
        }
    }

    // FIXED: Passing asset.coinId instead of the whole asset object
    suspend fun deleteAsset(asset: AssetEntity) {
        assetDao.deleteAsset(asset.coinId)
    }

    suspend fun updateAssetOrder(assets: List<AssetEntity>) {
        assetDao.updateAssetOrder(assets)
    }
}