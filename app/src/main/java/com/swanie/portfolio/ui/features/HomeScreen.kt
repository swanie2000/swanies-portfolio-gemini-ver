package com.swanie.portfolio.ui.features

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun HomeScreen(navController: NavHostController, mainViewModel: MainViewModel) {
    val activity = LocalContext.current as FragmentActivity
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel(activity)
    val scope = rememberCoroutineScope()

    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    val navyColor = Color(0xFF000416)
    val userThemeBgColor = remember(siteBgHex) {
        try { Color(android.graphics.Color.parseColor(siteBgHex)) }
        catch (e: Exception) { navyColor }
    }
    val userThemeTextColor = remember(siteTextHex) {
        try { Color(android.graphics.Color.parseColor(siteTextHex)) }
        catch (e: Exception) { Color.White }
    }

    var animationStarted by remember { mutableStateOf(false) }
    var animateText by remember { mutableStateOf(false) }
    var showSparkles by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    // 🌊 SEAMLESS NAVY FADE: The background color reveals itself in sync with the Swan
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "BackgroundFade"
    )

    // ⚡ SNAPPY REVEAL: 1200ms -> 960ms (20% reduction)
    val radiusPercent by animateFloatAsState(targetValue = if (animationStarted) 1.5f else 0f, animationSpec = tween(960), label = "")

    // ⚡ SNAPPY REVEAL: 1500ms -> 1200ms, delay 200ms -> 160ms (20% reduction)
    val swanYOffset by animateDpAsState(
        targetValue = if (animationStarted) (-80).dp else (-600).dp,
        animationSpec = tween(1200, delayMillis = 160),
        finishedListener = {
            animateText = true
        },
        label = ""
    )
    
    // ⚡ SNAPPY REVEAL: 1000ms -> 800ms, delay 200ms -> 160ms (20% reduction)
    val alpha by animateFloatAsState(targetValue = if (animationStarted) 1f else 0f, animationSpec = tween(800, delayMillis = 160), label = "")

    val configuration = LocalConfiguration.current
    val minDimension = min(configuration.screenWidthDp, configuration.screenHeightDp)
    val logoSize = (minDimension * 0.7f).dp

    LaunchedEffect(Unit) {
        // ⚡ SNAPPY REVEAL: Initial delay 300ms -> 240ms (20% reduction)
        delay(240)
        animationStarted = true
    }

    LaunchedEffect(animateText) {
        if (animateText) {
            // ⚡ SNAPPY REVEAL: Sparkle delay 1100ms -> 880ms (20% reduction)
            delay(880)
            showSparkles = true
        }
    }

    // --- 🛡️ SUCCESS NAVIGATION: Graceful Fade into Holdings ---
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated) {
            navController.navigate(Routes.HOLDINGS) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(navyColor), // Root background is fixed Navy
        contentAlignment = Alignment.Center
    ) {
        // 🎨 COLOR LAYER: Fades in the user's theme color over the navy base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = backgroundAlpha)
                .background(userThemeBgColor)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerPoint = Offset(size.width / 2f, size.height / 2f)
            val maxDim = size.width.coerceAtLeast(size.height)
            if (radiusPercent > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(userThemeBgColor.copy(alpha = 0.35f * backgroundAlpha), Color.Transparent),
                        center = centerPoint,
                        radius = maxDim * radiusPercent
                    ),
                    radius = maxDim * radiusPercent,
                    center = centerPoint
                )
            }
        }

        Box(
            modifier = Modifier
                .size(logoSize)
                .offset(y = swanYOffset)
                .zIndex(2f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.swanie_foreground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().graphicsLayer(alpha = alpha)
            )

            if (showSparkles) {
                MetallicShimmer(
                    delayMs = 0,
                    modifier = Modifier
                        .offset(x = (-113).dp, y = 53.dp)
                        .zIndex(3f)
                )

                // ⚡ SNAPPY REVEAL: Stagger 500ms -> 400ms (20% reduction)
                MetallicShimmer(
                    delayMs = 400,
                    modifier = Modifier
                        .offset(x = 50.dp, y = (-55).dp)
                        .zIndex(3f)
                )
            }
        }

        AnimatedVisibility(
            visible = animateText,
            enter = fadeIn(tween(640, 80)), // ⚡ SNAPPY REVEAL: 800, 100 -> 640, 80
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (logoSize / 2) - 125.dp)
                .zIndex(1f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Swanie's Portfolio",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crypto & Precious Metals",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray.copy(alpha = 0.8f)
                )
            }
        }

        AnimatedVisibility(
            visible = animateText,
            enter = fadeIn(tween(800, 1600)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(800, 1600) // ⚡ SNAPPY REVEAL: 2000ms -> 1600ms as requested
            ),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.8f), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        navController.navigate(Routes.UNLOCK_VAULT) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = userThemeTextColor, contentColor = userThemeBgColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("LOGIN", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))

                TextButton(onClick = { navController.navigate(Routes.CREATE_ACCOUNT) }) {
                    Text("CREATE ACCOUNT", color = userThemeTextColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}


@Composable
private fun MetallicShimmer(delayMs: Int, modifier: Modifier = Modifier) {
    var scaleState by remember { mutableStateOf(0f) }
    var rotationState by remember { mutableStateOf(45f) }

    val scale by animateFloatAsState(
        targetValue = scaleState,
        animationSpec = tween(240, easing = LinearOutSlowInEasing), // ⚡ SNAPPY REVEAL: 300 -> 240
        label = "SparkleScale"
    )

    val rotation by animateFloatAsState(
        targetValue = rotationState,
        animationSpec = tween(480), // ⚡ SNAPPY REVEAL: 600 -> 480
        label = "SparkleRotation"
    )

    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        scaleState = 1f
        rotationState = 135f
        delay(320) // ⚡ SNAPPY REVEAL: 400 -> 320
        scaleState = 0f
    }

    Box(
        modifier = modifier
            .size(6.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation,
                alpha = scale
            )
            .background(Color.White)
    )
}