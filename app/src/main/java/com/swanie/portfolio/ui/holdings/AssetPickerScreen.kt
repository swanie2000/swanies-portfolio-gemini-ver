package com.swanie.portfolio.ui.holdings

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay

/**
 * 🛠️ V7.2.6 MISSION: UI RESTORATION & METAL TRIGGER FIX
 * Restores the unified search UI while providing an instant funnel for Metals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetPickerScreen(
    navController: NavController,
    onAssetSelected: (AssetEntity) -> Unit
) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()
    val cardBgHex by themeViewModel.cardBackgroundColor.collectAsState()
    val cardTextHex by themeViewModel.cardTextColor.collectAsState()

    val bgColor = Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" }))
    val textColor = Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" }))
    val cardBg = Color(android.graphics.Color.parseColor(cardBgHex.ifBlank { "#121212" }))
    val cardText = Color(android.graphics.Color.parseColor(cardTextHex.ifBlank { "#FFFFFF" }))

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearchBusy by viewModel.isSearchBusy.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    
    val providers = remember(viewModel) {
        viewModel.getAvailableProviders().sortedBy { it == "YahooFinance" }
    }

    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(Unit) {
        if (selectedProvider == null) viewModel.selectProvider("CoinGecko")
        delay(300)
        if (selectedProvider != "YahooFinance") focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VAULT SELECTOR", fontWeight = FontWeight.Black, color = textColor, fontSize = 16.sp, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // --- UNIFIED INPUT & PROVIDER AREA ---
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (selectedProvider == "YahooFinance") {
                    // 🚀 METAL MODE: Selector + Funnel replace the search bar
                    Column {
                        SourceSelectorSurface(
                            displayLabel = "PRECIOUS METALS",
                            icon = Icons.Default.Shield,
                            onClick = { menuExpanded = true },
                            bgColor = cardBg,
                            textColor = cardText,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Text(
                            "SELECT METAL TYPE",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
                        )
                        
                        FunnelGrid(
                            options = listOf("Gold", "Silver", "Platinum", "Palladium", "Custom"),
                            selected = ""
                        ) { choice ->
                            if (choice == "Custom") {
                                navController.navigate("asset_architect/CUSTOM/0.0/Manual")
                            } else {
                                val ticker = when (choice) {
                                    "Gold" -> "XAU"
                                    "Silver" -> "XAG"
                                    "Platinum" -> "XPT"
                                    "Palladium" -> "XPD"
                                    else -> choice.uppercase()
                                }
                                onAssetSelected(AssetEntity(
                                    coinId = ticker,
                                    symbol = ticker,
                                    name = choice.uppercase(),
                                    category = AssetCategory.METAL,
                                    priceSource = "YahooFinance",
                                    baseSymbol = ticker,
                                    apiId = ticker,
                                    isMetal = true,
                                    physicalForm = "Coin"
                                ))
                            }
                        }
                    }
                } else {
                    // 🔍 CRYPTO MODE: Standard Search Bar with integrated Dropdown
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; viewModel.searchCoins(it) },
                        modifier = Modifier.fillMaxWidth().height(56.dp).focusRequester(focusRequester),
                        placeholder = { Text("Search Crypto...", color = cardText.copy(alpha = 0.4f)) },
                        leadingIcon = {
                            if (isSearchBusy) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Yellow)
                            } else {
                                Icon(Icons.Default.Search, null, tint = cardText.copy(alpha = 0.6f))
                            }
                        },
                        trailingIcon = {
                            SourceSelectorSurface(
                                displayLabel = selectedProvider ?: "SOURCE",
                                icon = Icons.Default.Search,
                                onClick = { menuExpanded = true },
                                bgColor = Color.White.copy(alpha = 0.05f),
                                textColor = cardText,
                                modifier = Modifier.width(140.dp).padding(end = 8.dp).height(36.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg,
                            focusedBorderColor = Color.Yellow.copy(alpha = 0.5f),
                            unfocusedBorderColor = cardText.copy(alpha = 0.1f),
                            focusedTextColor = cardText,
                            unfocusedTextColor = cardText
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }

                // Unified Dropdown Menu
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A)).border(1.dp, Color.White.copy(0.1f))
                ) {
                    providers.forEach { provider ->
                        val menuLabel = if (provider == "YahooFinance") "PRECIOUS METALS" else provider
                        DropdownMenuItem(
                            text = { Text(menuLabel.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            onClick = {
                                viewModel.selectProvider(provider)
                                menuExpanded = false
                                searchQuery = ""
                            }
                        )
                    }
                }
            }

            // --- CONTENT AREA (Results or Branding) ---
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (selectedProvider != "YahooFinance") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp)
                        ) {
                            items(searchResults) { asset ->
                                AssetPickerItem(asset, textColor) {
                                    onAssetSelected(asset)
                                }
                            }
                        }
                    }

                    if (searchResults.isEmpty() && !isSearchBusy) {
                        BrandedSwanBranding(alpha = 0.3f)
                    }
                } else {
                    // Metal Mode Background Branding
                    BrandedSwanBranding(alpha = 0.1f)
                }
            }
        }
    }
}

@Composable
fun SourceSelectorSurface(
    displayLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = displayLabel.uppercase(),
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Icon(Icons.Default.ArrowDropDown, null, tint = textColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun BrandedSwanBranding(alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.swanie_foreground),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 60.dp)
                .size(240.dp)
                .alpha(alpha),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun AssetPickerItem(asset: AssetEntity, textColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = asset.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f))
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.name, fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)
            Text(asset.symbol.uppercase(), fontSize = 11.sp, color = textColor.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Default.Add, null, tint = Color.Yellow.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
    }
}
