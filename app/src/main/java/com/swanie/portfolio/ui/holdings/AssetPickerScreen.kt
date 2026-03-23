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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UNIFIED VAULT SEARCH", fontWeight = FontWeight.Black, color = textColor, fontSize = 16.sp, letterSpacing = 1.sp) },
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
            
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(64.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; viewModel.searchCoins(it) },
                    modifier = Modifier.fillMaxSize().focusRequester(focusRequester),
                    placeholder = { Text("Search Assets...", color = cardText.copy(alpha = 0.4f)) },
                    leadingIcon = { 
                        if (isSearchBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Yellow)
                        } else {
                            Icon(Icons.Default.Search, null, tint = cardText.copy(alpha = 0.6f))
                        }
                    },
                    trailingIcon = {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            Surface(
                                onClick = { menuExpanded = true },
                                color = bgColor,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(150.dp).height(34.dp), 
                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val displayLabel = if (selectedProvider == "YahooFinance") "PRECIOUS METALS" else (selectedProvider ?: "SOURCE")
                                    Text(
                                        text = displayLabel.uppercase(),
                                        color = textColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
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
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = cardText.copy(alpha = 0.5f),
                        unfocusedBorderColor = cardText.copy(alpha = 0.15f),
                        focusedTextColor = cardText,
                        unfocusedTextColor = cardText
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (selectedProvider == "YahooFinance") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val metals = listOf("XAU" to "GOLD", "XAG" to "SILVER", "XPT" to "PLAT", "XPD" to "PALL")
                            metals.forEach { (ticker, label) ->
                                Surface(
                                    onClick = {
                                        // 🛠️ DATA INTEGRITY FIX: Use technical tickers (XAG/XAU) for background fetching
                                        // Use label for name, ticker for internal mapping logic.
                                        onAssetSelected(AssetEntity(
                                            coinId = ticker, 
                                            symbol = ticker, // технический ticker
                                            name = label, // User friendly name
                                            category = AssetCategory.METAL, 
                                            priceSource = "YahooFinance",
                                            baseSymbol = ticker, 
                                            apiId = ticker
                                        ))
                                    },
                                    color = cardBg.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, cardText.copy(alpha = 0.1f)),
                                    modifier = Modifier.weight(1f).height(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = label, color = cardText, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults) { asset ->
                            AssetPickerItem(asset, textColor) {
                                onAssetSelected(asset)
                            }
                        }
                    }
                }

                if (searchResults.isEmpty() && !isSearchBusy) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.swanie_foreground),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 30.dp)
                                .size(240.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
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