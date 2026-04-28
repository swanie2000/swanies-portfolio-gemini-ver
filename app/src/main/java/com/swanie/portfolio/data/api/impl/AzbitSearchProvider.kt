package com.swanie.portfolio.data.api.impl

import android.util.Log
import com.swanie.portfolio.data.api.SearchProvider
import com.swanie.portfolio.data.api.SearchResult
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.data.network.AzbitApiService
import com.swanie.portfolio.data.network.JupiterTokenApiService
import com.swanie.portfolio.data.network.JupiterTokenSearchItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AzbitSearchProvider @Inject constructor(
    private val azbitApi: AzbitApiService,
    private val jupiterTokens: JupiterTokenApiService
) : SearchProvider {

    /** Positive Jupiter icon hits only — avoids hammering the API on repeat refreshes. */
    private val jupiterIconBySymbol = ConcurrentHashMap<String, String>()
    override val name: String = "Azbit"

    override suspend fun search(query: String): List<SearchResult> {
        return try {
            val cleanQuery = query.trim()
            if (cleanQuery.isBlank()) return emptyList()

            val pairs = azbitApi.getCurrencies()
            pairs
                .mapNotNull { code ->
                    // Example code from docs: BTC_USDT
                    val base = code.substringBefore("_").trim().uppercase()
                    if (base.isBlank()) null else base
                }
                .distinct()
                .filter { base -> base.contains(cleanQuery, ignoreCase = true) }
                .take(20)
                .let { bases ->
                    // One Jupiter search per user query — parallel per-row calls were rate-limited / failed,
                    // leaving only CoinCap URLs (404 for most Azbit-only tickers) and an empty-looking picker.
                    val jupiterBatch = runCatching { jupiterTokens.search(cleanQuery) }.getOrNull().orEmpty()
                    bases.map { base ->
                        val upper = base.uppercase(Locale.ROOT)
                        SearchResult(
                            id = "AZ_$upper",
                            symbol = upper,
                            name = upper,
                            imageUrl = resolvePickerIcon(upper, jupiterBatch),
                            category = AssetCategory.CRYPTO,
                            priceSource = name
                        )
                    }
                }
        } catch (e: Exception) {
            Log.e("AZBIT_SEARCH", "Search failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPrices(ids: String): List<AssetEntity> {
        return try {
            val symbols = ids.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { raw -> raw.removePrefix("AZ_").uppercase() }

            if (symbols.isEmpty()) return emptyList()

            val tickers = azbitApi.getTickers()
            symbols.mapNotNull { symbol ->
                val ticker = selectTickerForSymbol(tickers, symbol) ?: return@mapNotNull null
                val sparkline = fetchSparkline(ticker.currencyPairCode)
                val icon = iconUrlForSymbol(symbol)
                AssetEntity(
                    coinId = "AZ_$symbol",
                    symbol = symbol,
                    name = symbol,
                    imageUrl = icon,
                    category = AssetCategory.CRYPTO,
                    officialSpotPrice = ticker.price ?: 0.0,
                    priceChange24h = ticker.priceChangePercentage24h ?: 0.0,
                    sparklineData = sparkline,
                    priceSource = name,
                    lastUpdated = System.currentTimeMillis(),
                    apiId = "AZ_$symbol",
                    iconUrl = icon
                )
            }
        } catch (e: Exception) {
            Log.e("AZBIT_PRICE", "Price fetch failed: ${e.message}")
            emptyList()
        }
    }

    private fun selectTickerForSymbol(tickers: List<com.swanie.portfolio.data.network.AzbitTicker>, symbol: String): com.swanie.portfolio.data.network.AzbitTicker? {
        val byBase = tickers.filter { t ->
            t.currencyPairCode.substringBefore("_").equals(symbol, ignoreCase = true)
        }
        if (byBase.isEmpty()) return null

        val preferredQuoteOrder = listOf("USDT", "MUSDT", "USD", "USDC", "BTC", "ETH")
        for (quote in preferredQuoteOrder) {
            byBase.firstOrNull {
                it.currencyPairCode.substringAfter("_", "").equals(quote, ignoreCase = true)
            }?.let { return it }
        }
        return byBase.firstOrNull()
    }

    private fun coinCapFallback(symbolUpper: String): String =
        "https://assets.coincap.io/assets/icons/${symbolUpper.lowercase(Locale.ROOT)}@2x.png"

    /**
     * Picker: one batched Jupiter response + CDN fallbacks (no N parallel Jupiter calls).
     */
    private fun resolvePickerIcon(symbolUpper: String, jupiterBatch: List<JupiterTokenSearchItem>): String {
        jupiterIconBySymbol[symbolUpper]?.let { return it }
        bestJupiterIcon(symbolUpper, jupiterBatch)?.let { raw ->
            val normalized = normalizeTokenIconUrl(raw)
            jupiterIconBySymbol[symbolUpper] = normalized
            return normalized
        }
        return coinCapFallback(symbolUpper)
    }

    private suspend fun iconUrlForSymbol(symbol: String): String {
        val upper = symbol.uppercase(Locale.ROOT)
        jupiterIconBySymbol[upper]?.let { return it }
        val fromJupiter = runCatching { fetchBestJupiterIcon(upper) }.getOrNull()
        if (!fromJupiter.isNullOrBlank()) {
            val normalized = normalizeTokenIconUrl(fromJupiter)
            jupiterIconBySymbol[upper] = normalized
            return normalized
        }
        return coinCapFallback(upper)
    }

    private fun bestJupiterIcon(
        symbolUpper: String,
        items: List<JupiterTokenSearchItem>
    ): String? {
        val matches = items.filter {
            it.symbol.equals(symbolUpper, ignoreCase = true) && !it.icon.isNullOrBlank()
        }
        val best = matches.maxWithOrNull(
            compareByDescending<JupiterTokenSearchItem> { it.liquidity ?: 0.0 }
                .thenByDescending { it.mcap ?: 0.0 }
                .thenByDescending { it.holderCount ?: 0 }
        )
        return best?.icon
    }

    private suspend fun fetchBestJupiterIcon(symbolUpper: String): String? {
        val items = runCatching { jupiterTokens.search(symbolUpper) }.getOrNull().orEmpty()
        return bestJupiterIcon(symbolUpper, items)
    }

    private suspend fun fetchSparkline(currencyPairCode: String): List<Double> {
        return try {
            val (start, end) = ohlcUtcRange(hoursBack = 72)
            azbitApi.getOhlc(
                interval = "hour",
                currencyPairCode = currencyPairCode,
                start = start,
                end = end
            )
                .mapNotNull { it.close }
                .takeLast(24)
        } catch (e: Exception) {
            Log.w("AZBIT_PRICE", "Sparkline fetch failed for $currencyPairCode: ${e.message}")
            emptyList()
        }
    }

    /**
     * Azbit returns an empty array for /api/ohlc unless [start] and [end] are provided (docs: dateTime query params).
     */
    private fun ohlcUtcRange(hoursBack: Int): Pair<String, String> {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val end = endCal.time
        endCal.add(Calendar.HOUR_OF_DAY, -hoursBack)
        val start = endCal.time
        return fmt.format(start) to fmt.format(end)
    }

    /** Public ipfs.io gateways are flaky on some networks; Cloudflare IPFS is usually more reliable for Coil. */
    private fun normalizeTokenIconUrl(url: String): String {
        val prefix = "https://ipfs.io/ipfs/"
        if (url.startsWith(prefix)) return "https://cf-ipfs.com/ipfs/" + url.removePrefix(prefix)
        return url
    }
}

