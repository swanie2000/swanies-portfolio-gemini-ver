package com.swanie.portfolio.ui.features

import android.app.Activity
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
import com.swanie.portfolio.security.SecurityManager
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun HomeScreen(navController: NavHostController, mainViewModel: MainViewModel) {
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

    val radiusPercent by animateFloatAsState(targetValue = if (animationStarted) 1.5f else 0f, animationSpec = tween(1200), label = "")
    val swanYOffset by animateDpAsState(targetValue = if (animationStarted) (-120).dp else (-600).dp, animationSpec = tween(1500, delayMillis = 200), finishedListener = { animateText = true }, label = "")
    val alpha by animateFloatAsState(targetValue = if (animationStarted) 1f else 0f, animationSpec = tween(1000, delayMillis = 200), label = "")

    val configuration = LocalConfiguration.current
    val minDimension = min(configuration.screenWidthDp, configuration.screenHeightDp)
    val logoSize = (minDimension * 0.7f).dp

    LaunchedEffect(Unit) {
        delay(300)
        animationStarted = true
    }

    Box(modifier = Modifier.fillMaxSize().background(userThemeBgColor)) {
        // --- ✨ TWINKLE LAYER ---
        TwinkleStars(color = userThemeTextColor.copy(alpha = 0.3f))

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

        AnimatedVisibility(visible = animateText, enter = fadeIn(tween(800, 100)), modifier = Modifier.align(Alignment.Center).offset(y = 20.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "SWANIE'S", style = MaterialTheme.typography.headlineLarge, color = userThemeTextColor, fontWeight = FontWeight.Light, letterSpacing = 8.sp)
                Text(text = "PORTFOLIO", style = MaterialTheme.typography.headlineLarge, color = userThemeTextColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "SOVEREIGN ASSET VAULT", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Thin, fontSize = 10.sp, letterSpacing = 5.sp), color = userThemeTextColor.copy(alpha = 0.6f))
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

@Composable
fun TwinkleStars(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha1 by infiniteTransition.animateFloat(initialValue = 0.2f, targetValue = 0.8f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "")
    val alpha2 by infiniteTransition.animateFloat(initialValue = 0.8f, targetValue = 0.1f, animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse), label = "")

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Simple star pattern
        val starPositions = listOf(
            Offset(0.2f, 0.1f), Offset(0.8f, 0.2f), Offset(0.5f, 0.05f),
            Offset(0.1f, 0.4f), Offset(0.9f, 0.45f), Offset(0.3f, 0.8f),
            Offset(0.7f, 0.9f), Offset(0.4f, 0.6f), Offset(0.6f, 0.3f)
        )
        
        starPositions.forEachIndexed { index, pos ->
            drawCircle(
                color = color.copy(alpha = if (index % 2 == 0) alpha1 else alpha2),
                radius = 2.dp.toPx(),
                center = Offset(pos.x * size.width, pos.y * size.height)
            )
        }
    }
}
