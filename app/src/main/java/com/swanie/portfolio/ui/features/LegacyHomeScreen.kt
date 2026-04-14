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
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun LegacyHomeScreen(navController: NavHostController, mainViewModel: MainViewModel) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()

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

    var animationStarted by remember { mutableStateOf(false) }
    var animateText by remember { mutableStateOf(false) }
    var animateTwinkle by remember { mutableStateOf(false) }

    val radiusPercent by animateFloatAsState(targetValue = if (animationStarted) 1.5f else 0f, animationSpec = tween(1200), label = "")
    val swanYOffset by animateDpAsState(targetValue = if (animationStarted) (-120).dp else (-600).dp, animationSpec = tween(1500, delayMillis = 200), finishedListener = { animateText = true; animateTwinkle = true }, label = "")
    val alpha by animateFloatAsState(targetValue = if (animationStarted) 1f else 0f, animationSpec = tween(1000, delayMillis = 200), label = "")

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val configuration = LocalConfiguration.current
    val minDimension = min(configuration.screenWidthDp, configuration.screenHeightDp)
    val logoSize = (minDimension * 0.7f).dp

    LaunchedEffect(Unit) {
        delay(300)
        animationStarted = true
    }

    Box(modifier = Modifier.fillMaxSize().background(userThemeBgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerPoint = Offset(size.width / 2f, size.height / 2f)
            val maxDim = size.width.coerceAtLeast(size.height)
            if (radiusPercent > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(userThemeBgColor.copy(alpha = 0.35f), Color.Transparent),
                        center = centerPoint,
                        radius = maxDim * radiusPercent
                    ),
                    radius = maxDim * radiusPercent,
                    center = centerPoint
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.Center).offset(y = swanYOffset).zIndex(1f), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(logoSize).graphicsLayer(alpha = alpha))
        }

        // --- ✨ THE TWINKLES (RESTORED MONTH-OLD STYLE) ---
        if (animateTwinkle) {
            // Twinkle 1: Hits the 'S' in Swanie's
            Spacer(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center)
                    .offset(x = (-105).dp, y = 55.dp) 
                    .graphicsLayer(alpha = twinkleAlpha)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color.White, Color.Transparent)),
                            radius = size.minDimension / 2
                        )
                    }
            )
            // Twinkle 2: Hits the Swan's Head
            Spacer(
                modifier = Modifier
                    .size(15.dp)
                    .align(Alignment.Center)
                    .offset(x = 12.dp, y = swanYOffset - 72.dp)
                    .graphicsLayer(alpha = twinkleAlpha)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color.White, Color.Transparent)),
                            radius = size.minDimension / 2
                        )
                    }
            )
        }

        // --- 🖋️ THE BRANDING (RESTORED MONTH-OLD STYLE) ---
        AnimatedVisibility(visible = animateText, enter = fadeIn(tween(800, 100)), modifier = Modifier.align(Alignment.Center).offset(y = 60.dp)) {
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

        // --- 🚀 THE RITUAL BUTTONS ---
        AnimatedVisibility(
            visible = animateText,
            enter = fadeIn(tween(800, 400)) + slideInVertically(initialOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.8f), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { navController.navigate(Routes.UNLOCK_VAULT) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = userThemeTextColor, contentColor = userThemeBgColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("LOGIN TO VAULT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { navController.navigate(Routes.CREATE_ACCOUNT) }) {
                    Text("CREATE NEW ACCOUNT", color = userThemeTextColor.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}
