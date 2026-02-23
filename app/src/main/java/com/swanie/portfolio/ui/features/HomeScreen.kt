package com.swanie.portfolio.ui.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.theme.LocalBackgroundBrush
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun HomeScreen(navController: NavHostController, mainViewModel: MainViewModel) {
    var animateSwan by remember { mutableStateOf(false) }
    var animateText by remember { mutableStateOf(false) }
    var showSparkleOnS by remember { mutableStateOf(false) }
    var showSparkleOnSwanHead by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val minDimension = min(configuration.screenWidthDp, configuration.screenHeightDp)
    val logoSize = (minDimension * 0.66f).dp

    val swanYOffset by animateDpAsState(
        targetValue = if (animateSwan) (-100).dp else (-600).dp,
        animationSpec = tween(1000, easing = CubicBezierEasing(0.165f, 0.84f, 0.44f, 1f)),
        finishedListener = { animateText = true }
    )

    val alpha by animateFloatAsState(targetValue = if (animateSwan) 1f else 0f, animationSpec = tween(1000))

    LaunchedEffect(animateText) {
        if (animateText) {
            delay(600)
            showSparkleOnS = true
            delay(300)
            showSparkleOnSwanHead = true
        }
    }

    LaunchedEffect(Unit) {
        delay(50)
        animateSwan = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LocalBackgroundBrush.current) // Use the theme brush
    ) {
        // 1. THE SWAN
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = swanYOffset)
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            Spacer(modifier = Modifier.size(logoSize).graphicsLayer(alpha = alpha * 0.8f).drawBehind {
                drawCircle(brush = Brush.radialGradient(colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent), radius = size.minDimension * 0.4f))
            })
            Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(logoSize).graphicsLayer(alpha = alpha))

            if (showSparkleOnSwanHead) {
                MetallicShimmer(modifier = Modifier.offset(x = 45.dp, y = -50.dp).zIndex(2f))
            }
        }

        // 2. THE TEXT
        AnimatedVisibility(
            visible = animateText,
            enter = fadeIn(animationSpec = tween(700, 100)),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-10).dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.graphicsLayer(clip = false)) {
                    Text(
                        text = "Swanie's Portfolio",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White, // Lock to white for branding
                        fontWeight = FontWeight.Bold
                    )
                    if (showSparkleOnS) {
                        MetallicShimmer(modifier = Modifier.align(Alignment.TopStart).offset(x = 14.dp, y = 2.dp))
                    }
                }
                Text(
                    text = "Crypto & Precious Metals",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Thin, fontSize = 12.sp, letterSpacing = 3.sp),
                    color = Color.White.copy(alpha = 0.8f) // Lock to white for branding
                )
            }
        }

        // 3. THE AUTH TRAY
        AnimatedVisibility(
            visible = animateText,
            enter = fadeIn(animationSpec = tween(700, 400)) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(700, 400)
                    ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            AuthTray(
                onLoginClick = { navController.navigate(Routes.HOLDINGS) },
                onCreateAccountClick = { navController.navigate(Routes.CREATE_ACCOUNT) }
            )
        }
    }
}

@Composable
fun AuthTray(onLoginClick: () -> Unit, onCreateAccountClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(0.90f),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("LOGIN", color = Color.Black, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onCreateAccountClick) {
                Text("Create Account", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { /* TODO */ }) {
                Text("Forgot Password?", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MetallicShimmer(modifier: Modifier = Modifier) {
    var scaleState by remember { mutableStateOf(0f) }
    var rotationState by remember { mutableStateOf(45f) }
    val scale by animateFloatAsState(targetValue = scaleState, animationSpec = tween(300, easing = CubicBezierEasing(0.17f, 0.89f, 0.32f, 1.28f)))
    val rotation by animateFloatAsState(targetValue = rotationState, animationSpec = tween(500))
    LaunchedEffect(Unit) {
        scaleState = 1.4f
        rotationState = 180f
        delay(300L)
        scaleState = 0f
    }
    Box(modifier = modifier.size(4.dp).graphicsLayer(scaleX = scale, scaleY = scale, rotationZ = rotation).background(Color.White))
}