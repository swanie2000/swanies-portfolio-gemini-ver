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
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.swanie.portfolio.data.local.AssetCategory
import com.swanie.portfolio.data.local.AssetEntity
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.onboarding.HoldingsWalkthroughViewModel
import com.swanie.portfolio.ui.onboarding.WalkthroughAnchor
import com.swanie.portfolio.ui.onboarding.walkthroughAnchor
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
    onSave: (vaultId: Int, savedCoinId: String) -> Unit,
    onCancel: () -> Unit,
    onNavigateToArchitect: (String, Double, String) -> Unit // 🛠️ V7.2.5 Handshake
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()
    val cardBgHex by themeViewModel.cardBackgroundColor.collectAsState()

    val bgColor = remember(siteBgHex) { Color(siteBgHex.ifBlank { "#000416" }.toColorInt()) }
    val textColor = remember(siteTextHex) { Color(siteTextHex.ifBlank { "#FFFFFF" }.toColorInt()) }
    val dialogBg = remember(cardBgHex) { Color(cardBgHex.ifBlank { "#121212" }.toColorInt()) }

    val viewModel: AmountEntryViewModel = hiltViewModel()
    val activity = LocalActivity.current as AppCompatActivity
    val walkthroughViewModel: HoldingsWalkthroughViewModel = hiltViewModel(activity)
    val walkthroughController = walkthroughViewModel.controller
    val tourActive = walkthroughController.isActive()
    val deferAmountFocus = walkthroughController.shouldDeferKeyboardFocus()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    var visualProgress by remember { mutableFloatStateOf(0f) }
    var visualStatus by remember(context.resources.configuration) {
        mutableStateOf(context.getString(R.string.amount_entry_status_initializing))
    }
    var isSaving by remember { mutableStateOf(false) }
    var isActualWorkDone by remember { mutableStateOf(false) }
    var showCheckmark by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    fun submitAmount() {
        if (amountText.isBlank() || isSaving) return
        walkthroughController.onAmountEntrySubmitted()
        isSaving = true
    }
    var showExitDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val milestones = remember(name, apiId, priceSource, context.resources.configuration) {
        listOf(
            0.15f to context.getString(R.string.amount_entry_milestone_securing, apiId),
            0.35f to context.getString(R.string.amount_entry_milestone_connecting, priceSource),
            0.55f to context.getString(R.string.amount_entry_milestone_fetching),
            0.75f to context.getString(R.string.amount_entry_milestone_validating),
            0.90f to context.getString(R.string.amount_entry_milestone_syncing),
            1.00f to context.getString(R.string.amount_entry_milestone_done),
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
                viewModel.performSurgicalAdd(
                    asset = asset,
                    pinToTopOfVault = walkthroughController.shouldPinSavedAssetToTop(),
                ) { isActualWorkDone = true }
            }

            showCheckmark = true
            while (!isActualWorkDone) { delay(100) }
            delay(800)
            val activeVaultId = viewModel.currentVaultId()
            onSave(activeVaultId, uniqueId)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(animation = tween(1100, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(category, symbol, officialSpotPrice, priceSource) {
        if (category == AssetCategory.METAL) {
            onNavigateToArchitect(symbol, officialSpotPrice, priceSource)
            onCancel()
        }
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
                        IconButton(
                            onClick = {
                                if (!isSaving && !tourActive) showExitDialog = true
                            },
                            modifier = Modifier.align(Alignment.CenterStart),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.amount_entry_back), tint = textColor)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .walkthroughAnchor(
                                anchor = WalkthroughAnchor.AMOUNT_INPUT,
                                controller = walkthroughController,
                            ),
                        label = { Text(stringResource(R.string.amount_entry_label, symbol), color = textColor.copy(alpha = 0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitAmount() }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, cursorColor = textColor, focusedBorderColor = textColor, unfocusedBorderColor = textColor.copy(alpha = 0.5f))
                    )
                    errorMessage?.let { Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp)) }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { submitAmount() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .height(56.dp)
                            .walkthroughAnchor(
                                anchor = WalkthroughAnchor.AMOUNT_SAVE_BUTTON,
                                controller = walkthroughController,
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                        enabled = amountText.isNotBlank() && !isSaving
                    ) {
                        Text(stringResource(R.string.amount_entry_save_asset), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                                Text(
                                    text = stringResource(R.string.amount_entry_visual_progress_percent, (visualProgress * 100).toInt()),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 28.sp
                                )
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
            containerColor = dialogBg,
            title = { Text(stringResource(R.string.amount_entry_discard_title), color = textColor) },
            text = { Text(stringResource(R.string.amount_entry_discard_body), color = textColor.copy(alpha = 0.7f)) },
            confirmButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.amount_entry_yes), color = Color.Red, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text(stringResource(R.string.amount_entry_no), color = textColor) } }
        )
    }

    BackHandler {
        if (!isSaving && !tourActive) showExitDialog = true
    }
    LaunchedEffect(category, deferAmountFocus) {
        if (deferAmountFocus) return@LaunchedEffect
        if (category == AssetCategory.CRYPTO) focusRequester.requestFocus()
    }
}
