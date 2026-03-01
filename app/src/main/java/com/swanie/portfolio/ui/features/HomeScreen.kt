package com.swanie.portfolio.ui.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.components.BottomNavigationBar
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun HomeScreen(navController: NavHostController, mainViewModel: MainViewModel) {
    // --- THEME BINDING ---
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()

    val userThemeBgColor = remember(siteBgHex) {
        try { Color(android.graphics.Color.parseColor(siteBgHex)) }
        catch (e: Exception) { Color(0xFF000416) }
    }
    val userThemeTextColor = remember(siteTextHex) {
        try { Color(android.graphics.Color.parseColor(siteTextHex)) }
        catch (e: Exception) { Color.White }
    }

    // --- ANIMATION STATE ---
    var animationStarted by remember { mutableStateOf(false) }
    var animateText by remember { mutableStateOf(false) }
    var showSparkleOnS by remember { mutableStateOf(false) }
    var showSparkleOnSwanHead by remember { mutableStateOf(false) }

    val radiusPercent by animateFloatAsState(
        targetValue = if (animationStarted) 1.5f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "RadialBurst"
    )

    val swanYOffset by animateDpAsState(
        targetValue = if (animationStarted) (-100).dp else (-600).dp,
        animationSpec = tween(900, delayMillis = 120, easing = CubicBezierEasing(0.165f, 0.84f, 0.44f, 1f)),
        finishedListener = { animateText = true },
        label = "SwanGlide"
    )

    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(700, delayMillis = 120),
        label = "SwanAlpha"
    )

    val configuration = LocalConfiguration.current
    val minDimension = min(configuration.screenWidthDp, configuration.screenHeightDp)
    val logoSize = (minDimension * 0.66f).dp

    LaunchedEffect(animateText) {
        if (animateText) {
            delay(400)
            showSparkleOnS = true
            delay(200)
            showSparkleOnSwanHead = true
        }
    }

    LaunchedEffect(Unit) {
        delay(50)
        animationStarted = true
    }

    // --- UI LAYOUT WRAPPED IN SCAFFOLD ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Unified 4-Icon Navigation
            BottomNavigationBar(navController = navController)
        },
        containerColor = Color(0xFF000416) // Splash handoff color
    ) { innerPadding ->
        // innerPadding is respected to keep content from sliding behind the nav bar
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // ANIMATED RADIAL REVEAL
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerPoint = Offset(size.width / 2f, size.height / 2f)
                val maxDim = size.width.coerceAtLeast(size.height)
                drawCircle(color = userThemeBgColor, radius = maxDim * radiusPercent, center = centerPoint)
            }

            // THE SWAN LOGO
            Box(modifier = Modifier.align(Alignment.Center).offset(y = swanYOffset).zIndex(1f), contentAlignment = Alignment.Center) {
                Spacer(modifier = Modifier.size(logoSize).graphicsLayer(alpha = alpha * 0.8f).drawBehind {
                    drawCircle(brush = Brush.radialGradient(colors = listOf(userThemeTextColor.copy(alpha = 0.3f), Color.Transparent), radius = size.minDimension * 0.4f))
                })
                Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(logoSize).graphicsLayer(alpha = alpha))
                if (showSparkleOnSwanHead) MetallicShimmer(modifier = Modifier.offset(x = 45.dp, y = (-50).dp).zIndex(2f))
            }

            // BRAND TEXT
            AnimatedVisibility(visible = animateText, enter = fadeIn(tween(500, 50)), modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.graphicsLayer(clip = false)) {
                        Text(text = "Swanie's Portfolio", style = MaterialTheme.typography.headlineLarge, color = userThemeTextColor, fontWeight = FontWeight.Bold)
                        if (showSparkleOnS) MetallicShimmer(modifier = Modifier.align(Alignment.TopStart).offset(x = 14.dp, y = 2.dp))
                    }
                    Text(text = "Crypto & Precious Metals", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Thin, fontSize = 12.sp, letterSpacing = 3.sp), color = userThemeTextColor.copy(alpha = 0.8f))
                }
            }

            // AUTH TRAY (Login, Create, and RESTORED Forgot Password)
            AnimatedVisibility(
                visible = animateText,
                enter = fadeIn(tween(500, 200)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500, 200)),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
            ) {
                AuthTray(
                    onLoginClick = { navController.navigate(Routes.HOLDINGS) },
                    onCreateAccountClick = { navController.navigate(Routes.CREATE_ACCOUNT) },
                    trayTextColor = userThemeTextColor
                )
            }
        }
    }
}

@Composable
fun AuthTray(onLoginClick: () -> Unit, onCreateAccountClick: () -> Unit, trayTextColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(0.90f),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = trayTextColor.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, trayTextColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(vertical = 30.dp, horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("LOGIN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onCreateAccountClick) {
                Text("Create Account", color = trayTextColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            // RESTORED LINK
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { /* Handle Forgot Password logic here */ }) {
                Text("Forgot Password?", color = trayTextColor.copy(alpha = 0.5f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MetallicShimmer(modifier: Modifier = Modifier, shimmerColor: Color = Color.White) {
    var scaleState by remember { mutableStateOf(0f) }
    var rotationState by remember { mutableStateOf(45f) }
    val scale by animateFloatAsState(targetValue = scaleState, animationSpec = tween(300, easing = CubicBezierEasing(0.17f, 0.89f, 0.32f, 1.28f)), label = "ShimmerScale")
    val rotation by animateFloatAsState(targetValue = rotationState, animationSpec = tween(500), label = "ShimmerRotation")
    LaunchedEffect(Unit) { scaleState = 1.4f; rotationState = 180f; delay(300L); scaleState = 0f }
    Box(modifier = modifier.size(4.dp).graphicsLayer(scaleX = scale, scaleY = scale, rotationZ = rotation).background(shimmerColor))
}