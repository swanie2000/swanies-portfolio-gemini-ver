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
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearchBusy by viewModel.isSearchBusy.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val providers = viewModel.getAvailableProviders()

    var expanded by remember { mutableStateOf(false) }

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
            
            // THE GATEKEEPER: Provider Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = selectedProvider ?: "Select Price Provider...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Search Source", color = textColor.copy(alpha = 0.7f)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = Color.Yellow,
                        unfocusedBorderColor = textColor.copy(alpha = 0.2f),
                        focusedLabelColor = Color.Yellow,
                        unfocusedLabelColor = textColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(bgColor.copy(alpha = 0.95f))
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider, color = textColor) },
                            onClick = {
                                viewModel.selectProvider(provider)
                                expanded = false
                                searchQuery = "" // Reset query when switching
                            }
                        )
                    }
                }
            }

            // THE SEARCH BOX: Enabled only after provider selection
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchCoins(it)
                },
                enabled = selectedProvider != null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { 
                    Text(
                        if (selectedProvider == null) "Select provider first..." else "Search assets...", 
                        color = textColor.copy(alpha = 0.5f)
                    ) 
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = if (selectedProvider != null) textColor else textColor.copy(alpha = 0.3f)) },
                trailingIcon = {
                    if (isSearchBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Yellow)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Yellow,
                    unfocusedBorderColor = textColor.copy(alpha = 0.2f),
                    disabledBorderColor = textColor.copy(alpha = 0.1f),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    disabledTextColor = textColor.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchResults) { asset ->
                    AssetPickerItem(asset, textColor) {
                        Log.d("ADD_TRACE", "STEP 1: USER_SELECTED: ID=${asset.apiId} SOURCE=${asset.priceSource}")
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(asset.symbol.uppercase(), fontSize = 12.sp, color = textColor.copy(alpha = 0.5f))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = asset.priceSource, 
                    fontSize = 10.sp, 
                    color = Color.Yellow.copy(alpha = 0.7f),
                    modifier = Modifier.background(Color.Yellow.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)
                )
            }
        }
    }
}
