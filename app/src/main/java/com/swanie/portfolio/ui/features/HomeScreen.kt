package com.swanie.portfolio.ui.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun HomeScreen(navController: NavHostController) {
    var animateSwan by remember { mutableStateOf(false) }
    var animateText by remember { mutableStateOf(false) }
    var animateTwinkle by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val minDimension = min(configuration.screenWidthDp, configuration.screenHeightDp)
    val logoSize = (minDimension * 0.66f).dp

    // Stage 1: The High Slide (The Swan)
    val offsetY by animateDpAsState(
        targetValue = if (animateSwan) -60.dp else -500.dp,
        animationSpec = tween(
            durationMillis = 1200,
            easing = CubicBezierEasing(0.165f, 0.84f, 0.44f, 1f) // EaseOutQuart
        ),
        label = "SwanGlide"
    )
    val swanAlpha by animateFloatAsState(
        targetValue = if (animateSwan) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "SwanAlpha"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (animateSwan) 0.4f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "GlowAlpha"
    )

    // Stage 3: The Twinkle
    var twinkleTargetAlpha by remember { mutableStateOf(0f) }
    val twinkleAlpha by animateFloatAsState(
        targetValue = twinkleTargetAlpha,
        animationSpec = tween(durationMillis = 400),
        label = "TwinkleAlpha"
    )

    LaunchedEffect(Unit) {
        delay(500) // Initial delay for window transition
        animateSwan = true
        delay(1200 * 0.8.toLong()) // 80% of swan animation
        animateText = true
        delay(600) // Wait a bit after text appears
        animateTwinkle = true
    }

    LaunchedEffect(animateTwinkle) {
        if (animateTwinkle) {
            twinkleTargetAlpha = 0.8f
            delay(500)
            twinkleTargetAlpha = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000416)),
        contentAlignment = Alignment.Center
    ) {
        // Middle Layer: The Glow
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = glowAlpha)
                .offset(y = offsetY)
                .drawBehind {
                    val radius = logoSize.toPx() * 0.3f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color.Transparent),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
        )

        // Stage 3: The Twinkles
        if (animateTwinkle) {
            // Twinkle 1 (Left Wing)
            Spacer(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
                    .offset(x = -logoSize * 0.2f, y = offsetY + logoSize * 0.1f)
                    .graphicsLayer(alpha = twinkleAlpha)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color.White, Color.Transparent)),
                            radius = size.minDimension / 2
                        )
                    }
            )
            // Twinkle 2 (Head)
            Spacer(
                modifier = Modifier
                    .size(15.dp)
                    .align(Alignment.Center)
                    .offset(x = logoSize * 0.05f, y = offsetY - logoSize * 0.3f)
                    .graphicsLayer(alpha = twinkleAlpha)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color.White, Color.Transparent)),
                            radius = size.minDimension / 2
                        )
                    }
            )
        }

        // Top Layer: The Swan
        Image(
            painter = painterResource(id = R.drawable.swanie_foreground),
            contentDescription = "Swan Logo",
            modifier = Modifier
                .size(logoSize)
                .graphicsLayer(alpha = swanAlpha)
                .offset(y = offsetY)
        )

        // Stage 2: The Brand Reveal
        AnimatedVisibility(
            visible = animateText,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
            modifier = Modifier.offset(y = 80.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(
                    text = "Swanie's Portfolio",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Text(
                    text = "Crypto & Precious Metals",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(60.dp))
                Button(
                    onClick = { navController.navigate(Routes.HOLDINGS) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055FF))
                ) {
                    Text("ENTER DASHBOARD", color = Color.White)
                }
            }
        }
    }
}
