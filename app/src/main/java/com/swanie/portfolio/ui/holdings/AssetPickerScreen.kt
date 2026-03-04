package com.swanie.portfolio.ui.holdings

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.MetalsProvider
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.settings.ThemeViewModel
import java.util.UUID

@Composable
fun AssetPickerScreen(
    navController: NavController,
    onAssetSelected: (AssetEntity) -> Unit
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))

    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val viewModel: AssetViewModel = hiltViewModel()
    val searchResults by viewModel.searchResults.collectAsState()

    var showFunnel by remember { mutableStateOf(false) }
    var funnelInitialMetal by remember { mutableStateOf("Silver") }

    LaunchedEffect(Unit) {
        viewModel.searchCoins("")
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().background(bgColor).padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; viewModel.searchCoins(it) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            placeholder = { Text("Search (e.g., Bitcoin, Gold)", color = textColor.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = textColor, unfocusedBorderColor = textColor.copy(alpha = 0.5f))
        )

        AnimatedVisibility(visible = searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.Center) {
                TextButton(onClick = { funnelInitialMetal = "Silver"; showFunnel = true }) {
                    Text("Manual Add Asset", color = textColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        if (searchResults.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.weight(0.1f))
                Image(painter = painterResource(id = R.drawable.swan_launcher_icon), contentDescription = null, modifier = Modifier.size(220.dp).alpha(0.08f), colorFilter = ColorFilter.tint(textColor))
                Spacer(modifier = Modifier.weight(3.0f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                items(searchResults) { asset ->
                    CoinItem(asset = asset, textColor = textColor, onAssetSelected = {
                        if (it.category == AssetCategory.METAL) {
                            funnelInitialMetal = it.name.split(" ").first()
                            showFunnel = true
                        } else { onAssetSelected(it) }
                    })
                    HorizontalDivider(color = textColor.copy(alpha = 0.1f), thickness = 1.dp)
                }
            }
        }
    }

    if (showFunnel) {
        MetalSelectionFunnel(
            initialMetal = funnelInitialMetal,
            initialForm = "Bars", initialWeight = 1.0, initialQty = "", initialPrem = "0.0", initialManualPrice = "0.0",
            onDismiss = { showFunnel = false },
            onConfirmed = { funnelMetal, funnelForm, funnelWeight, isKilo, funnelQty, funnelPrem, iconUri, isTrueCustom, manualPrice ->

                // IF TRUE CUSTOM: funnelMetal = Identity (BOY), funnelForm = Descriptions (BARS1\nBARS2)
                val baseSymbol = if(isTrueCustom) "CUSTOM" else when (funnelMetal) { "Gold" -> "XAU"; "Silver" -> "XAG"; "Platinum" -> "XPT"; "Palladium" -> "XPD"; else -> "CUSTOM" }
                val spotPrice = if(isTrueCustom) (manualPrice.toDoubleOrNull() ?: 0.0) else MetalsProvider.preciousMetals.find { it.symbol == baseSymbol }?.currentPrice ?: 0.0

                val displaySymbol = if(isTrueCustom) funnelMetal.uppercase() else when (funnelMetal) { "Gold" -> "GOLD"; "Silver" -> "SILV"; "Platinum" -> "PLAT"; "Palladium" -> "PALL"; else -> "CUST" }
                val finalName = if(isTrueCustom) funnelForm else "${funnelMetal.uppercase()}\n${if (isKilo) "KILO" else funnelForm.uppercase()}"

                val newAsset = AssetEntity(
                    coinId = "custom_${UUID.randomUUID()}",
                    symbol = displaySymbol,
                    name = finalName,
                    amountHeld = funnelQty.toDoubleOrNull() ?: 1.0,
                    currentPrice = spotPrice,
                    change24h = 0.0,
                    displayOrder = 0,
                    lastUpdated = System.currentTimeMillis(),
                    imageUrl = iconUri ?: if(isTrueCustom) "SWAN_DEFAULT" else "",
                    category = AssetCategory.METAL,
                    weight = funnelWeight,
                    premium = funnelPrem.toDoubleOrNull() ?: 0.0,
                    isCustom = true,
                    baseSymbol = baseSymbol,
                    decimalPreference = 2
                )
                onAssetSelected(newAsset)
                showFunnel = false
            }
        )
    }
}

@Composable
fun CoinItem(asset: AssetEntity, textColor: Color, onAssetSelected: (AssetEntity) -> Unit) {
    TextButton(onClick = { onAssetSelected(asset) }, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (asset.category == AssetCategory.METAL) {
                MetalIcon(asset.name, size = 30, imageUrl = asset.imageUrl)
            } else {
                AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(text = "${asset.name.replace("\n", " ")} (${asset.symbol.uppercase()})", color = textColor, fontWeight = FontWeight.Medium)
        }
    }
}