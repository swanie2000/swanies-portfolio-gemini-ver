package com.swanie.portfolio.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.holdings.AssetViewModel
import com.swanie.portfolio.ui.holdings.MetalIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetManagerScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    assetViewModel: AssetViewModel = hiltViewModel()
) {
    val userConfig by settingsViewModel.userConfig.collectAsStateWithLifecycle(null)
    val assets by assetViewModel.holdings.collectAsStateWithLifecycle(emptyList())

    // Presets
    val colorPresets = listOf(
        "#000000", // Black
        "#000416", // Midnight Blue
        "#1A1C1E", // Dark Grey
        "#2F353B"  // Stealth Silver
    )

    val selectedIds = remember(userConfig?.selectedWidgetAssets) {
        userConfig?.selectedWidgetAssets?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Scaffold(containerColor = Color(0xFF1C1C1E)) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER (Mirroring Theme Studio) ---
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp).clickable { navController.popBackStack() }
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            settingsViewModel.updateWidgetBgColor("#000000")
                            settingsViewModel.updateWidgetCardColor("#1A1C1E")
                            settingsViewModel.updateShowWidgetTotal(false)
                        }
                    ) {
                        Text("RESET", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("WIDGET", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.swanie_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // --- STEALTH TOGGLE ---
                item {
                    StudioSectionHeader("PRIVACY MODE")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(0.3f))
                            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("STEALTH PULSE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Hide totals on home screen", color = Color.Gray, fontSize = 12.sp)
                            }
                            Switch(
                                checked = !(userConfig?.showWidgetTotal ?: false),
                                onCheckedChange = { settingsViewModel.updateShowWidgetTotal(!it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.Yellow)
                            )
                        }
                    }
                }

                // --- WIDGET COLORS ---
                item {
                    StudioSectionHeader("BACKGROUND COLOR")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorPresets.forEach { color ->
                            ColorTile(
                                color = color,
                                isSelected = userConfig?.widgetBgColor == color,
                                onClick = { settingsViewModel.updateWidgetBgColor(color) }
                            )
                        }
                    }
                }

                item {
                    StudioSectionHeader("CARD COLOR")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorPresets.forEach { color ->
                            ColorTile(
                                color = color,
                                isSelected = userConfig?.widgetCardColor == color,
                                onClick = { settingsViewModel.updateWidgetCardColor(color) }
                            )
                        }
                    }
                }

                // --- ASSET SELECTION (Max 10) ---
                item {
                    Row(verticalAlignment = Alignment.Bottom) {
                        StudioSectionHeader("DISPLAY ASSETS")
                        Spacer(Modifier.width(8.dp))
                        Text("(${selectedIds.size}/10)", color = if(selectedIds.size >= 10) Color.Red else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }

                items(assets) { asset ->
                    val isSelected = selectedIds.contains(asset.coinId)
                    val orderIndex = if (isSelected) selectedIds.indexOf(asset.coinId) + 1 else null

                    WidgetStudioAssetItem(
                        asset = asset,
                        isSelected = isSelected,
                        orderIndex = orderIndex,
                        onToggle = {
                            val newList = if (isSelected) {
                                selectedIds.filter { it != asset.coinId }
                            } else {
                                if (selectedIds.size < 10) selectedIds + asset.coinId else selectedIds
                            }
                            settingsViewModel.updateSelectedWidgetAssets(newList.joinToString(","))
                        }
                    )
                }
            }

            // Preview Pulse
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(bottom = 8.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Yellow),
                contentAlignment = Alignment.Center
            ) {
                Text("WIDGET UPDATED IN REAL-TIME", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StudioSectionHeader(text: String) {
    Text(
        text = text,
        color = Color.Yellow,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ColorTile(color: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(color.toColorInt()))
            .border(2.dp, if (isSelected) Color.White else Color.White.copy(0.1f), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun WidgetStudioAssetItem(
    asset: AssetEntity,
    isSelected: Boolean,
    orderIndex: Int?,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White.copy(0.05f) else Color.Transparent)
            .border(1.dp, if (isSelected) Color.White.copy(0.3f) else Color.White.copy(0.05f), RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            if (asset.category == AssetCategory.METAL) {
                MetalIcon(name = asset.name, weight = asset.weight, size = 28, category = asset.category)
            } else {
                AsyncImage(model = asset.imageUrl, contentDescription = null, modifier = Modifier.size(28.dp).clip(CircleShape))
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.symbol.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(asset.name.replace("\n", " "), color = Color.Gray, fontSize = 11.sp)
        }

        if (isSelected && orderIndex != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow),
                contentAlignment = Alignment.Center
            ) {
                Text(orderIndex.toString(), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(0.2f), CircleShape)
            )
        }
    }
}
