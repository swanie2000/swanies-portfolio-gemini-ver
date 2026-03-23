package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.MetalIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    assetViewModel: AssetViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val userConfig by settingsViewModel.userConfig.collectAsStateWithLifecycle(null)
    val assets by assetViewModel.holdings.collectAsStateWithLifecycle(emptyList())
    
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    var isExiting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WIDGET STUDIO", fontWeight = FontWeight.Black, fontSize = 20.sp, color = safeText) },
                navigationIcon = {
                    IconButton(onClick = { isExiting = true; navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = safeText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (!isExiting) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "PRIVACY",
                        color = safeText.copy(0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    SettingsToggleItem(
                        title = "Show Total Value",
                        subtitle = "Display the big yellow number on home screen",
                        checked = userConfig?.showWidgetTotal ?: false,
                        onCheckedChange = { settingsViewModel.updateShowWidgetTotal(it) },
                        themeColor = safeText
                    )
                }

                item {
                    Text(
                        "DISPLAY ASSETS (TOP 3)",
                        color = safeText.copy(0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                val selectedIds = userConfig?.selectedWidgetAssets?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

                items(assets) { asset ->
                    val isSelected = selectedIds.contains(asset.coinId)
                    WidgetAssetSelectItem(
                        asset = asset,
                        isSelected = isSelected,
                        onToggle = {
                            val newList = if (isSelected) {
                                selectedIds.filter { it != asset.coinId }
                            } else {
                                if (selectedIds.size < 3) selectedIds + asset.coinId else selectedIds
                            }
                            settingsViewModel.updateSelectedWidgetAssets(newList.joinToString(","))
                        },
                        themeColor = safeText
                    )
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun WidgetAssetSelectItem(
    asset: AssetEntity,
    isSelected: Boolean,
    onToggle: () -> Unit,
    themeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) themeColor.copy(0.1f) else Color.Transparent)
            .border(1.dp, if (isSelected) themeColor.copy(0.3f) else themeColor.copy(0.1f), RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            if (asset.category == AssetCategory.METAL) {
                MetalIcon(name = asset.name, weight = asset.weight, size = 32, category = asset.category)
            } else {
                AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape))
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.symbol.uppercase(), color = themeColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(asset.name.replace("\n", " "), color = themeColor.copy(0.6f), fontSize = 12.sp)
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = Color.Yellow, checkmarkColor = Color.Black, uncheckedColor = themeColor.copy(0.3f))
        )
    }
}
