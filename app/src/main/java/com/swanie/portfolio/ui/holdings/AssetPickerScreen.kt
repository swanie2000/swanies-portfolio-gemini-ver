package com.swanie.portfolio.ui.holdings

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.components.BoutiqueHeader
import com.swanie.portfolio.ui.onboarding.HoldingsWalkthroughStep
import com.swanie.portfolio.ui.onboarding.HoldingsWalkthroughViewModel
import com.swanie.portfolio.ui.onboarding.WalkthroughAnchor
import com.swanie.portfolio.ui.onboarding.walkthroughAnchor
import com.swanie.portfolio.ui.settings.ThemeViewModel
import java.net.URLEncoder
import kotlinx.coroutines.delay

/**
 * 🛠️ V7.3.0 MISSION: PRECISION STAMP & THEME LOCK
 * Final polish for the Vault Selector with theme-aware inputs and a bespoke custom build button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetPickerScreen(
    navController: NavController,
    vaultId: Int,
    onAssetSelected: (AssetEntity) -> Unit,
) {
    val viewModel: AssetViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val activity = LocalActivity.current as AppCompatActivity
    val walkthroughViewModel: HoldingsWalkthroughViewModel = hiltViewModel(activity)
    val walkthroughController = walkthroughViewModel.controller
    val walkthroughStep by walkthroughController.step.collectAsState()
    val deferSearchFocus = walkthroughController.shouldDeferKeyboardFocus()
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
    }

    LaunchedEffect(selectedProvider, deferSearchFocus) {
        if (deferSearchFocus) return@LaunchedEffect
        delay(300)
        if (selectedProvider != "YahooFinance") focusRequester.requestFocus()
    }

    LaunchedEffect(searchQuery, searchResults) {
        if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
            walkthroughController.onPickerSearchResultsReady()
        }
    }

    val useInlineProviderMenu =
        walkthroughStep == HoldingsWalkthroughStep.PICKER_CHOOSE_PROVIDER && menuExpanded

    val onProviderSelected: (String) -> Unit = { provider ->
        menuExpanded = false
        searchQuery = ""
        if (provider == "YahooFinance") {
            walkthroughController.onMetalProviderSelected()
            viewModel.selectProvider("CoinGecko")
            val enc = URLEncoder.encode("Manual", "UTF-8")
            navController.navigate("asset_architect/CUSTOM/0.0/$enc/$vaultId")
        } else {
            walkthroughController.onPickerProviderSelected()
            viewModel.selectProvider(provider)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(bgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            
            // --- 🦢 BOUTIQUE HEADER ---
            BoutiqueHeader(
                title = stringResource(R.string.add_asset_screen_title),
                onBack = { navController.popBackStack() },
                textColor = textColor
            )

            // --- Search + provider (METAL opens architect blueprint directly) ---
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
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
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .walkthroughAnchor(
                                        anchor = WalkthroughAnchor.PICKER_SEARCH_BOX,
                                        controller = walkthroughController,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
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
                                            text = stringResource(R.string.holdings_tab_crypto),
                                            color = cardText.copy(alpha = 0.4f),
                                            fontSize = 10.5.sp,
                                            letterSpacing = (-0.3).sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                            Box(modifier = Modifier.padding(start = 8.dp)) {
                                Surface(
                                    onClick = {
                                        walkthroughController.onPickerDropdownOpened()
                                        menuExpanded = true
                                    },
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(32.dp)
                                        .walkthroughAnchor(
                                            anchor = WalkthroughAnchor.PICKER_PROVIDER_BUTTON,
                                            controller = walkthroughController,
                                        ),
                                    border = BorderStroke(1.dp, cardText.copy(alpha = 0.1f)),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        Text(
                                            text = (selectedProvider ?: stringResource(R.string.settings_system_actions)).uppercase(),
                                            color = cardText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            null,
                                            tint = cardText.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                )

                if (useInlineProviderMenu) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 60.dp)
                            .width(200.dp)
                            .walkthroughAnchor(
                                anchor = WalkthroughAnchor.PICKER_DROPDOWN,
                                controller = walkthroughController,
                            ),
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    ) {
                        Column {
                            providers.forEach { provider ->
                                val menuLabel = if (provider == "YahooFinance") {
                                    stringResource(R.string.holdings_tab_metal)
                                } else {
                                    provider
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onProviderSelected(provider) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = menuLabel.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded && !useInlineProviderMenu,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(Color(0xFF1A1A1A))
                        .border(1.dp, Color.White.copy(0.1f)),
                ) {
                    providers.forEach { provider ->
                        val menuLabel = if (provider == "YahooFinance") stringResource(R.string.holdings_tab_metal) else provider
                        DropdownMenuItem(
                            text = { Text(menuLabel.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            onClick = { onProviderSelected(provider) },
                        )
                    }
                }
            }

            // --- CONTENT AREA ---
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
            }
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
        val iconKey = "${asset.coinId}|${asset.imageUrl}"
        var imageFailed by remember(iconKey) { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (asset.imageUrl.isNotBlank() && !imageFailed) {
                AsyncImage(
                    model = asset.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = { imageFailed = true }
                )
            } else {
                val letter = asset.symbol.trim().take(1).ifBlank { "?" }.uppercase()
                Text(
                    text = letter,
                    color = Color.Yellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.name, fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)
            Text(asset.symbol.uppercase(), fontSize = 11.sp, color = textColor.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Default.Add, null, tint = textColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
    }
}
