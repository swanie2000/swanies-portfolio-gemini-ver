package com.swanie.portfolio.ui.holdings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swanie.portfolio.data.ThemePreferences
import com.swanie.portfolio.data.local.*
import com.swanie.portfolio.data.remote.GoogleDriveService
import com.swanie.portfolio.data.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AmountEntryViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val themePreferences: ThemePreferences,
    private val iconManager: IconManager,
    private val priceHistoryDao: PriceHistoryDao,
    private val googleDriveService: GoogleDriveService, // 🛡️ Local-First Stub
    private val assetDao: AssetDao // 🛡️ Inject Dao for Global Sync
) : ViewModel() {

    private val _amount = MutableStateFlow(0.0f)
    val amount: StateFlow<Float> = _amount.asStateFlow()

    private val _isValid = MutableStateFlow(true)
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    suspend fun currentVaultId(): Int = themePreferences.currentVaultId.first()

    fun updateAmount(input: String) {
        val parsed = input.toFloatOrNull()
        if (parsed != null) {
            _amount.value = parsed
            _isValid.value = true
        } else {
            _amount.value = 0.0f
            _isValid.value = false
        }
    }

    /**
     * SURGICAL: Core DB insertion logic.
     * Updated to fetch live price and sparkline data before saving,
     * immediately seed history, and download icons on-demand.
     */
    fun performSurgicalAdd(asset: AssetEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            // 🌐 GLOBAL VISTA: Dynamically fetch currentVaultId from ThemePreferences
            val activeVaultId = themePreferences.currentVaultId.first()

            // 1. Download Icon On-Demand
            val localPath = iconManager.downloadIcon(asset.symbol, asset.imageUrl)
            if (localPath != null) {
                Log.d("IconManager", "Saved icon to: $localPath")
            }

            // 2. Fetch live data and apply correct vaultId + local icon path
            val populatedAsset = repository.fetchLiveAssetData(asset).copy(
                vaultId = activeVaultId,
                localIconPath = localPath
            )
            
            // 3. Save the complete asset to the database
            repository.executeSurgicalAdd(populatedAsset) { success, _ ->
                if (success) {
                    // 🚀 SEED PRICE HISTORY: Ensure sparkline appears immediately
                    seedPriceHistory(populatedAsset.coinId, populatedAsset.officialSpotPrice)
                    
                    // 🛡️ LOCAL SYNC: Keeping code structure for future local backup logic
                    viewModelScope.launch {
                        try {
                            val allAssets = assetDao.getAllAssetsGlobal()
                            googleDriveService.uploadFullVaultBackup(allAssets)
                        } catch (e: Exception) {
                            Log.e("VAULT_DEBUG", "Local Backup Failed", e)
                        }
                    }

                    onComplete()
                }
            }
        }
    }

    private fun seedPriceHistory(assetId: String, currentPrice: Double) {
        viewModelScope.launch {
            if (currentPrice <= 0) return@launch
            
            val seedPoints = mutableListOf<PriceHistoryEntity>()
            val now = System.currentTimeMillis()
            
            // Generate 20 random points around the current price (Mock 7-day trend)
            for (i in 0 until 20) {
                val variance = (Random.nextDouble() - 0.5) * 0.05 // +/- 2.5% variance
                val historicalPrice = currentPrice * (1 + variance)
                seedPoints.add(
                    PriceHistoryEntity(
                        assetId = assetId,
                        price = historicalPrice,
                        timestamp = now - (i * 3600000L * 8) // Spaced roughly 8 hours apart
                    )
                )
            }
            
            seedPoints.forEach { priceHistoryDao.insertPricePoint(it) }
            Log.d("VM_SEED", "Seeded 20 points for $assetId")
        }
    }
}
