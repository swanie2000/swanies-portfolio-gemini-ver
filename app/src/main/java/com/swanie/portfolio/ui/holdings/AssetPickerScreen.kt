package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester // ADDED
import androidx.compose.ui.focus.focusRequester // ADDED
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel

@Composable
fun AssetPickerScreen(
    navController: NavController,
    onAssetSelected: (coinId: String, symbol: String, name: String, imageUrl: String, category: AssetCategory, price: Double) -> Unit
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    val bgColor = remember(siteBgHex) { Color(android.graphics.Color.parseColor(siteBgHex)) }
    val textColor = remember(siteTextHex) { Color(android.graphics.Color.parseColor(siteTextHex)) }

    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() } // Now recognized

    val viewModel: AssetViewModel = hiltViewModel()
    val searchResults by viewModel.searchResults.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.searchCoins("")
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(bgColor)
            .padding(16.dp)
    ) {
        TextButton(onClick = { navController.navigate(Routes.MANUAL_ASSET_ENTRY) }) {
            Text("Manual Add Asset", color = textColor)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchCoins(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text("Search (e.g., Bitcoin, Gold)", color = textColor.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = textColor,
                focusedBorderColor = textColor,
                unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                focusedLabelColor = textColor.copy(alpha = 0.7f),
                unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        if (searchResults.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.1f))
                Image(
                    painter = painterResource(id = R.drawable.swan_launcher_icon),
                    contentDescription = "Ghost Swan",
                    modifier = Modifier
                        .size(220.dp)
                        .alpha(0.08f),
                    colorFilter = ColorFilter.tint(textColor)
                )
                Spacer(modifier = Modifier.weight(3.0f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                items(searchResults) { asset ->
                    CoinItem(
                        asset = asset,
                        textColor = textColor,
                        onAssetSelected = onAssetSelected
                    )
                    HorizontalDivider(
                        color = textColor.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
fun CoinItem(
    asset: AssetEntity,
    textColor: Color,
    onAssetSelected: (coinId: String, symbol: String, name: String, imageUrl: String, category: AssetCategory, price: Double) -> Unit
) {
    TextButton(
        onClick = {
            onAssetSelected(
                asset.coinId,
                asset.symbol.uppercase(),
                asset.name,
                asset.imageUrl,
                asset.category,
                asset.currentPrice
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            when (asset.category) {
                AssetCategory.METAL -> {
                    val color = when (asset.symbol) {
                        "XAU" -> Color(0xFFFFD700)
                        "XAG" -> Color(0xFFC0C0C0)
                        "XPT" -> Color(0xFFE5E4E2)
                        "XPD" -> Color(0xFFE5E4E2)
                        else -> Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
                AssetCategory.CRYPTO -> {
                    AsyncImage(
                        model = asset.imageUrl,
                        contentDescription = "${asset.name} icon",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "${asset.name} (${asset.symbol.uppercase()})",
                color = textColor
            )
            Spacer(Modifier.width(8.dp))
            Chip(label = asset.category.name, textColor = textColor)
        }
    }
}

@Composable
fun Chip(label: String, textColor: Color) {
    Box(
        modifier = Modifier
            .background(textColor.copy(alpha = 0.1f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}