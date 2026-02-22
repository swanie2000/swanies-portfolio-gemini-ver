package com.swanie.portfolio.ui.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun HomeScreen(navController: NavHostController, mainViewModel: MainViewModel) {
    var animateSwan by remember { mutableStateOf(false) }
    var animateText by remember { mutableStateOf(false) }
    var showSparkleOnS by remember { mutableStateOf(false) }
    var showSparkleOnSwanHead by remember { mutableStateOf(false) }

    val themeColorHex by mainViewModel.themeColorHex.collectAsStateWithLifecycle()

    val navyBackground = Color(0xFF000416)
    val userSelectedColor = try {
        Color(themeColorHex.toColorInt())
    } catch (e: IllegalArgumentException) {
        navyBackground // Fallback to navy if hex is invalid
    }

    // Guarantees the animation starts from Navy
    var animatedTargetColor by remember { mutableStateOf(navyBackground) }
    LaunchedEffect(userSelectedColor) {
        animatedTargetColor = userSelectedColor
    }

    // The animation state
    val backgroundColor by animateColorAsState(
        targetValue = animatedTargetColor,
        animationSpec = tween(durationMillis = 800), // Smooth 0.8s morph
        label = "BackgroundMorph"
    )

    val configuration = LocalConfiguration.current
    val minDimension = min(configuration.screenWidthDp, configuration.screenHeightDp)
    val logoSize = (minDimension * 0.66f).dp

    val offsetY by animateDpAsState(
        targetValue = if (animateSwan) -60.dp else -500.dp,
        animationSpec = tween(
            durationMillis = 1200,
            easing = CubicBezierEasing(0.165f, 0.84f, 0.44f, 1f)
        ),
        label = "SwanGlide",
        finishedListener = { animateText = true }
    )

    val alpha by animateFloatAsState(
        targetValue = if (animateSwan) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "SwanAlpha"
    )

    LaunchedEffect(animateText) {
        if (animateText) {
            delay(1000) // Wait for text fade-in. Total delay: 1200 (glide) + 1000 = 2200ms
            showSparkleOnS = true
            delay(500) // Total delay: 2200 + 500 = 2700ms
            showSparkleOnSwanHead = true
        }
    }

    LaunchedEffect(Unit) {
        delay(100) // Sync with background morph
        animateSwan = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor // Apply the animated color here
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Container for Swan and Glow
            Box(
                modifier = Modifier
                    .offset(y = offsetY)
                    .graphicsLayer(clip = false) // Technical Guardrail
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                // The Glow
                Spacer(
                    modifier = Modifier
                        .size(logoSize)
                        .graphicsLayer(alpha = alpha * 0.8f)
                        .drawBehind {
                            val radius = size.minDimension * 0.4f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                                    radius = radius
                                ),
                                radius = radius
                            )
                        }
                )

                // The Swan
                Image(
                    painter = painterResource(id = R.drawable.swanie_foreground),
                    contentDescription = "Swan Logo",
                    modifier = Modifier
                        .size(logoSize)
                        .graphicsLayer(alpha = alpha)
                )

                // Sparkle 2: On Swan's head
                if (showSparkleOnSwanHead) {
                    MetallicShimmer(
                        modifier = Modifier
                            .offset(x = 45.dp, y = -50.dp)
                            .zIndex(2f)
                    )
                }
            }

            AnimatedVisibility(
                visible = animateText,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000, delayMillis = 200)),
                modifier = Modifier.offset(y = 80.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    // Sparkle 1: Anchor to 'S'
                    Box(modifier = Modifier.graphicsLayer(clip = false)) { // Technical Guardrail
                        Text(
                            text = "Swanie's Portfolio",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (showSparkleOnS) {
                            MetallicShimmer(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = 18.dp, y = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Fine-Point Typography
                    Text(
                        text = "Crypto & Precious Metals",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Thin,
                            fontSize = 12.sp,
                            letterSpacing = 3.sp
                        ),
                        color = Color.LightGray.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(60.dp))
                    Button(
                        onClick = { navController.navigate(Routes.HOLDINGS) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("ENTER DASHBOARD", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetallicShimmer(modifier: Modifier = Modifier) {
    var scaleState by remember { mutableStateOf(0f) }
    var rotationState by remember { mutableStateOf(45f) }

    val scale by animateFloatAsState(
        targetValue = scaleState,
        animationSpec = tween(durationMillis = 400, easing = CubicBezierEasing(0.17f, 0.89f, 0.32f, 1.28f)),
        label = "ShimmerScale"
    )

    val rotation by animateFloatAsState(
        targetValue = rotationState,
        animationSpec = tween(durationMillis = 700),
        label = "ShimmerRotation"
    )

    LaunchedEffect(Unit) {
        // Pop in
        scaleState = 1.4f
        rotationState = 180f
        delay(400L)
        scaleState = 0f
    }

    Box(
        modifier = modifier
            .size(4.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation
            )
            .background(Color.White) // Diamond shape via rotation
    )
}
