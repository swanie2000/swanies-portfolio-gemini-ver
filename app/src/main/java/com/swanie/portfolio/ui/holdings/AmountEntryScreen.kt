package com.swanie.portfolio.ui.holdings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay

@Composable
fun AmountEntryScreen(
    coinId: String,
    apiId: String,
    symbol: String,
    name: String,
    imageUrl: String,
    category: AssetCategory,
    officialSpotPrice: Double,
    priceSource: String,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onNavigateToArchitect: (String, Double, String) -> Unit // 🛠️ V7.2.5 Handshake
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    val bgColor = remember(siteBgHex) { Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" })) }
    val textColor = remember(siteTextHex) { Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" })) }

    val viewModel: AmountEntryViewModel = hiltViewModel()
    val keyboardController = LocalSoftwareKeyboardController.current

    var visualProgress by remember { mutableFloatStateOf(0f) }
    var visualStatus by remember { mutableStateOf("Initializing...") }
    var isSaving by remember { mutableStateOf(false) }
    var isActualWorkDone by remember { mutableStateOf(false) }
    var showCheckmark by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    var metalEntityBuffer by remember { mutableStateOf<AssetEntity?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    var showExitDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val milestones = remember(name, apiId, priceSource) {
        listOf(
            0.15f to "Securing $apiId in local vault...",
            0.35f to "Connecting to $priceSource servers...",
            0.55f to "Fetching live market metrics...",
            0.75f to "Validating entry sequence...",
            0.90f to "Synchronizing portfolio DB...",
            1.00f to "Asset secured successfully!"
        )
    }

    LaunchedEffect(isSaving) {
        if (isSaving) {
            // RETRACT KEYBOARD IMMEDIATELY BEFORE ANIMATION
            keyboardController?.hide()
            
            milestones.forEach { milestone ->
                val (target, text) = milestone
                visualStatus = text
                val stepDuration = (600L..1000L).random()
                val startProgress = visualProgress
                val totalSteps = 20
                for (i in 1..totalSteps) {
                    delay(stepDuration / totalSteps)
                    visualProgress = startProgress + (target - startProgress) * (i.toFloat() / totalSteps)
                }
            }
            
            val timestamp = System.currentTimeMillis()
            val uniqueId = "${coinId}_$timestamp" 

            if (category == AssetCategory.CRYPTO) {
                val asset = AssetEntity(
                    coinId = uniqueId, 
                    symbol = symbol, 
                    name = name,
                    amountHeld = amountText.toDoubleOrNull() ?: 0.0, 
                    officialSpotPrice = officialSpotPrice,
                    category = category, 
                    imageUrl = imageUrl,
                    lastUpdated = timestamp,
                    apiId = apiId, 
                    iconUrl = imageUrl, 
                    baseSymbol = symbol,
                    priceSource = priceSource
                )
                viewModel.performSurgicalAdd(asset) { isActualWorkDone = true }
            } else {
                metalEntityBuffer?.let { buffer ->
                    val refinedAsset = buffer.copy(
                        coinId = uniqueId,
                        lastUpdated = timestamp
                    )
                    viewModel.performSurgicalAdd(refinedAsset) { isActualWorkDone = true }
                }
            }

            showCheckmark = true
            while (!isActualWorkDone) { delay(100) }
            delay(800)
            onSave()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(animation = tween(1100, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    if (category == AssetCategory.METAL && !isSaving) {
        MetalSelectionFunnel(
            initialMetal = symbol,
            initialForm = "",
            initialWeight = 1.0,
            initialQty = "",
            initialPrem = "",
            initialManualPrice = officialSpotPrice.toString(),
            onDismiss = onCancel,
            onConfirmed = { type, desc, weight, unit, qty, prem, icon, isManual, manualPrice ->
                metalEntityBuffer = AssetEntity(
                    coinId = coinId,
                    symbol = type,
                    name = desc,
                    category = AssetCategory.METAL,
                    weight = weight,
                    weightUnit = unit, // 🛠️ V18: Explicit Unit Capture
                    physicalForm = desc.split("\n").firstOrNull() ?: "Coin", // 🛡️ V18: Explicit Form Capture from Funnel
                    amountHeld = qty.toDoubleOrNull() ?: 0.0,
                    premium = prem.toDoubleOrNull() ?: 0.0,
                    imageUrl = icon ?: "",
                    officialSpotPrice = if(isManual) (manualPrice.toDoubleOrNull() ?: 0.0) else officialSpotPrice,
                    priceSource = priceSource,
                    apiId = apiId,
                    baseSymbol = symbol
                )
                isSaving = true 
            },
            onNavigateToArchitect = {
                onNavigateToArchitect(symbol, officialSpotPrice, priceSource)
            }
        )
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // REMOVE GHOST: Only show content if NOT saving
            if (category == AssetCategory.CRYPTO && !isSaving) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { if (!isSaving) showExitDialog = true }, modifier = Modifier.align(Alignment.CenterStart)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.size(120.dp).clip(CircleShape))
                    Spacer(Modifier.height(16.dp))
                    Text(text = name, style = MaterialTheme.typography.displaySmall, color = textColor)
                    Spacer(Modifier.height(32.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (!isSaving) { amountText = it; errorMessage = null } },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        label = { Text("Enter Amount for $symbol", color = textColor.copy(alpha = 0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if(amountText.isNotBlank()) isSaving = true }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, cursorColor = textColor, focusedBorderColor = textColor, unfocusedBorderColor = textColor.copy(alpha = 0.5f))
                    )
                    errorMessage?.let { Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp)) }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { isSaving = true },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                        enabled = amountText.isNotBlank() && !isSaving
                    ) {
                        Text("Save Asset", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            AnimatedVisibility(visible = isSaving, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.zIndex(10f)) {
                // REMOVE GHOST: Use fully opaque black background
                Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.size(90.dp).scale(if (showCheckmark) 1f else pulseScale))
                        Spacer(Modifier.height(40.dp))
                        Box(contentAlignment = Alignment.Center) {
                            if (showCheckmark) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(140.dp))
                            } else {
                                CircularProgressIndicator(progress = { visualProgress }, modifier = Modifier.size(140.dp), color = Color.Yellow, strokeWidth = 10.dp, trackColor = Color.White.copy(alpha = 0.1f))
                            }
                            if (!showCheckmark) {
                                Text(text = "${(visualProgress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
                            }
                        }
                        Spacer(Modifier.height(40.dp))
                        Text(text = visualStatus, color = if (showCheckmark) Color.Green else Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Discard Asset?", color = Color.White) },
            text = { Text("Are you sure you want to discard this new asset?", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = { TextButton(onClick = onCancel) { Text("Yes", color = Color.Red, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("No", color = Color.White) } }
        )
    }

    BackHandler { if (!isSaving) showExitDialog = true }
    LaunchedEffect(category) { if(category == AssetCategory.CRYPTO) focusRequester.requestFocus() }
}
