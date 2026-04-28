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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.components.BoutiqueHeader
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay

/**
 * 🛠️ V7.3.0 MISSION: PRECISION STAMP & THEME LOCK
 * Final polish for the Vault Selector with theme-aware inputs and a bespoke custom build button.
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

    val bgColor = Color(siteBgHex.ifBlank { "#000416" }.toColorInt())
    val textColor = Color(siteTextHex.ifBlank { "#FFFFFF" }.toColorInt())
    val cardBg = Color(cardBgHex.ifBlank { "#121212" }.toColorInt())
    val cardText = Color(cardTextHex.ifBlank { "#FFFFFF" }.toColorInt())

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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(bgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            
            // --- 🦢 BOUTIQUE HEADER ---
            BoutiqueHeader(
                title = "VAULT SELECTOR",
                onBack = { navController.popBackStack() },
                textColor = textColor
            )

            // --- UNIFIED INPUT AREA (One Window Strategy) ---
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (selectedProvider == "YahooFinance") {
                    // 🚀 METAL MODE: The selection area appears directly
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        UnifiedSourceHeader(
                            displayLabel = "PRECIOUS METALS",
                            icon = Icons.Default.Security,
                            onClick = { menuExpanded = true },
                            bgColor = cardBg,
                            textColor = cardText
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text(
                            "SELECT METAL TYPE",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // 🛠️ 2x2 Grid for standard metals
                        val metals = listOf("Gold", "Silver", "Platinum", "Palladium")
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            metals.chunked(2).forEach { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    rowItems.forEach { choice ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(55.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(0.05f))
                                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                                                .clickable {
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
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = choice.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 🚀 BESPOKE BUILD BUTTON: Styled to match but full width
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(0.05f))
                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                                .clickable { navController.navigate("asset_architect/CUSTOM/0.0/Manual") },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "BUILD A CUSTOM ASSET CARD", 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Black, 
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                } else {
                    // 🔍 CRYPTO MODE: Standard Search Bar with integrated Dropdown
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; viewModel.searchCoins(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .focusRequester(focusRequester)
                            .background(cardBg, RoundedCornerShape(16.dp))
                            .border(1.dp, textColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                        textStyle = LocalTextStyle.current.copy(
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        ),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(textColor),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSearchBusy) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = textColor)
                                } else {
                                    Icon(Icons.Default.Search, null, tint = cardText.copy(alpha = 0.6f))
                                }
                                Spacer(Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search Crypto...",
                                            color = cardText.copy(alpha = 0.4f),
                                            fontSize = 10.5.sp,
                                            letterSpacing = (-0.3).sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                                Box(modifier = Modifier.padding(start = 8.dp)) {
                                    Surface(
                                        onClick = { menuExpanded = true },
                                        color = Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.width(130.dp).height(32.dp),
                                        border = BorderStroke(1.dp, cardText.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = (selectedProvider ?: "SOURCE").uppercase(), color = cardText, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                            Icon(Icons.Default.ArrowDropDown, null, tint = cardText.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    )
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

            // --- CONTENT AREA ---
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
                        BrandingLogo(alpha = 0.3f)
                    }
                } else {
                    BrandingLogo(alpha = 0.1f)
                }
            }
        }
    }
}

@Composable
fun UnifiedSourceHeader(
    displayLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    bgColor: Color,
    textColor: Color
) {
    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = displayLabel.uppercase(), color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, null, tint = textColor.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun BrandingLogo(alpha: Float) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Image(
            painter = painterResource(id = R.drawable.swanie_foreground),
            contentDescription = null,
            modifier = Modifier.padding(top = 60.dp).size(240.dp).alpha(alpha),
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
        Icon(Icons.Default.Add, null, tint = textColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
    }
}
