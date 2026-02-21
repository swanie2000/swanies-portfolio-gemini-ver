package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AppDatabase

@Composable
fun AssetPickerScreen(onAssetSelected: (coinId: String, symbol: String, name: String, imageUrl: String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: AssetViewModel = viewModel(
        factory = AssetViewModelFactory(db.assetDao())
    )
    val searchResults by viewModel.searchResults.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.searchCoins("") // Clear previous search results
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000416))
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchCoins(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text("Search (e.g., Bitcoin, Gold)", color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        if (searchResults.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.2f)) // Drastically reduced weight to move image much higher
                Image(
                    painter = painterResource(id = R.drawable.swan_launcher_icon),
                    contentDescription = "Empty Search Results",
                    colorFilter = ColorFilter.tint(Color.White), // Keep white for visibility
                    modifier = Modifier
                        .size(200.dp)
                        .alpha(0.2f) // Restore ghost effect
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
                    TextButton(
                        onClick = { onAssetSelected(asset.coinId, asset.symbol.uppercase(), asset.name, asset.imageUrl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AsyncImage(
                                model = asset.imageUrl,
                                contentDescription = "${asset.name} icon",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text("${asset.name} (${asset.symbol.uppercase()})", color = Color.White)
                        }
                    }
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}
