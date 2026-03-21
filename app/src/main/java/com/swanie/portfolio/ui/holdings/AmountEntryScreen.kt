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
    symbol: String,
    name: String,
    imageUrl: String,
    category: AssetCategory,
    officialSpotPrice: Double, // ALIGNED V6
    priceSource: String,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    val bgColor = remember(siteBgHex) { Color(android.graphics.Color.parseColor(siteBgHex.ifBlank { "#000416" })) }
    val textColor = remember(siteTextHex) { Color(android.graphics.Color.parseColor(siteTextHex.ifBlank { "#FFFFFF" })) }

    val viewModel: AmountEntryViewModel = hiltViewModel()

    var visualProgress by remember { mutableFloatStateOf(0f) }
    var visualStatus by remember { mutableStateOf("Initializing...") }
    var isSaving by remember { mutableStateOf(false) }
    var isActualWorkDone by remember { mutableStateOf(false) }
    var showCheckmark by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var showExitDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val milestones = remember(name, symbol, priceSource) {
        listOf(
            0.15f to "Securing $name in local vault...",
            0.35f to "Connecting to $priceSource servers...",
            0.55f to when(priceSource) {
                "MEXC" -> "Checking MEXC order book for $symbol price..."
                "YahooFinance" -> "Fetching Yahoo Finance market data for $symbol..."
                else -> "Fetching live $symbol spot price from $priceSource..."
            },
            0.75f to "Downloading 24h market metrics...",
            0.90f to "Synchronizing portfolio DB...",
            1.00f to "Asset secured successfully!"
        )
    }

    LaunchedEffect(isSaving) {
        if (isSaving) {
            milestones.forEach { milestone ->
                val (target, text) = milestone
                visualStatus = text

                // SLOWED DOWN: Randomize duration between 800ms and 1500ms per step
                val stepDuration = (800L..1500L).random()
                val startProgress = visualProgress
                val totalSteps = 20

                for (i in 1..totalSteps) {
                    delay(stepDuration / totalSteps)
                    visualProgress = startProgress + (target - startProgress) * (i.toFloat() / totalSteps)
                }
            }

            showCheckmark = true
            while (!isActualWorkDone) { delay(100) }
            delay(1200) // Extra soak time for the Checkmark
            onSave()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun executeSave() {
        val amountValue = amountText.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            errorMessage = "Please enter a valid amount."
            return
        }
        isSaving = true
        val asset = AssetEntity(
            coinId = coinId, symbol = symbol, name = name,
            amountHeld = amountValue, officialSpotPrice = officialSpotPrice, // ALIGNED V6
            category = category, imageUrl = imageUrl,
            lastUpdated = System.currentTimeMillis(),
            apiId = coinId, iconUrl = imageUrl, baseSymbol = symbol,
            priceSource = priceSource
        )
        viewModel.performSurgicalAdd(asset) { isActualWorkDone = true }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = bgColor,
            title = { Text("Discard Asset?", color = textColor) },
            text = { Text("Are you sure you want to discard this new asset?", color = textColor.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text("Yes", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("No", color = textColor)
                }
            }
        )
    }

    BackHandler { if (!isSaving) showExitDialog = true }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = bgColor) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { if (!isSaving) showExitDialog = true },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                }
                Spacer(Modifier.height(32.dp))
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "$name Icon",
                    modifier = Modifier.size(120.dp).clip(CircleShape)
                )
                Spacer(Modifier.height(16.dp))
                Text(text = name, style = MaterialTheme.typography.displaySmall, color = textColor)
                Spacer(Modifier.height(32.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (!isSaving) { amountText = it; errorMessage = null } },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    label = { Text("Enter Amount for $symbol", color = textColor.copy(alpha = 0.6f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { executeSave() }),
                    singleLine = true,
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor, unfocusedTextColor = textColor,
                        cursorColor = textColor, focusedBorderColor = textColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.5f)
                    )
                )
                errorMessage?.let {
                    Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { executeSave() },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                    enabled = amountText.isNotBlank() && !isSaving
                ) {
                    Text("Save Asset", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        AnimatedVisibility(
            visible = isSaving,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.zIndex(10f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(90.dp).scale(if (showCheckmark) 1f else pulseScale)
                    )
                    Spacer(Modifier.height(40.dp))
                    Box(contentAlignment = Alignment.Center) {
                        if (showCheckmark) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color.Green,
                                modifier = Modifier.size(140.dp).animateEnterExit(
                                    enter = scaleIn(tween(500, easing = OvershootInterpolator().toEasing()))
                                )
                            )
                        } else {
                            CircularProgressIndicator(
                                progress = { visualProgress },
                                modifier = Modifier.size(140.dp),
                                color = Color.Yellow,
                                strokeWidth = 10.dp,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                        if (!showCheckmark) {
                            Text(
                                text = "${(visualProgress * 100).toInt()}%",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = visualStatus,
                        color = if (showCheckmark) Color.Green else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                    
                    if (showCheckmark) {
                        Text(
                            text = "Initial transaction logged to Black Box.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

fun android.view.animation.Interpolator.toEasing() = Easing { x -> getInterpolation(x) }
class OvershootInterpolator : android.view.animation.OvershootInterpolator()