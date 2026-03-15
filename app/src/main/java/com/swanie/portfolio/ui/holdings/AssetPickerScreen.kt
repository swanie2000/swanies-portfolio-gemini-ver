package com.swanie.portfolio.ui.holdings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.settings.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetPickerScreen(
    navController: NavController,
    onAssetSelected: (AssetEntity) -> Unit
) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()

    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()
    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))

    var searchQuery by remember { mutableStateOf("") }
    val cryptoResults by viewModel.searchResults.collectAsState()

    val metalList = listOf(
        AssetEntity(coinId = "gold-spot", symbol = "XAU", name = "Gold", category = AssetCategory.METAL, baseSymbol = "XAU", apiId = "gold-spot"),
        AssetEntity(coinId = "silver-spot", symbol = "XAG", name = "Silver", category = AssetCategory.METAL, baseSymbol = "XAG", apiId = "silver-spot"),
        AssetEntity(coinId = "platinum-spot", symbol = "XPT", name = "Platinum", category = AssetCategory.METAL, baseSymbol = "XPT", apiId = "platinum-spot"),
        AssetEntity(coinId = "palladium-spot", symbol = "XPD", name = "Palladium", category = AssetCategory.METAL, baseSymbol = "XPD", apiId = "palladium-spot")
    )

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("CRYPTO", "METALS")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Asset", fontWeight = FontWeight.Black, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (selectedTab == 0) viewModel.searchCoins(it)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search assets...", color = textColor.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = textColor) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Yellow,
                    unfocusedBorderColor = textColor.copy(alpha = 0.2f),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.Yellow,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, color = if(selectedTab == index) Color.Yellow else textColor.copy(alpha = 0.4f)) }
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                val currentList = if (selectedTab == 0) {
                    cryptoResults
                } else {
                    metalList.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.symbol.contains(searchQuery, ignoreCase = true)
                    }
                }

                items(currentList) { asset ->
                    AssetPickerItem(asset, textColor) {
                        // CLEAN SLATE: Instant navigation, no logic calls
                        Log.d("ADD_TRACE", "STEP 1: USER_SELECTED: ID=${asset.apiId}")
                        onAssetSelected(asset)
                    }
                }
            }
        }
    }
}

@Composable
fun AssetPickerItem(asset: AssetEntity, textColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = asset.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(asset.name, fontWeight = FontWeight.Bold, color = textColor)
            Text(asset.symbol.uppercase(), fontSize = 12.sp, color = textColor.copy(alpha = 0.5f))
        }
    }
}
