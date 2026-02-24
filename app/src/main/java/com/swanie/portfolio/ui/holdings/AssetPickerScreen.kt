package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush

@Composable
fun AssetPickerScreen(
    navController: NavController,
    onAssetSelected: (coinId: String, symbol: String, name: String, imageUrl: String, category: AssetCategory, price: Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

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
            .background(brush = LocalBackgroundBrush.current)
            .padding(16.dp)
    ) {
        TextButton(onClick = { navController.navigate(Routes.MANUAL_ASSET_ENTRY) }) {
            Text("Manual Add Asset")
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
            placeholder = { Text("Search (e.g., Bitcoin, Gold)") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        )

        if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.2f))
                Image(
                    painter = painterResource(id = R.drawable.swan_launcher_icon),
                    contentDescription = "Empty Search Results",
                    modifier = Modifier
                        .size(200.dp)
                        .alpha(0.1f)
                )
                Spacer(modifier = Modifier.weight(3f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                items(searchResults) { asset ->
                    CoinItem(asset = asset, onAssetSelected = onAssetSelected)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun CoinItem(
    asset: AssetEntity,
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
            Text("${asset.name} (${asset.symbol.uppercase()})", color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.width(8.dp))
            Chip(label = asset.category.name)
        }
    }
}

@Composable
fun Chip(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}