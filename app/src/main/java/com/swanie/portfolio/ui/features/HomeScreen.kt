package com.swanie.portfolio.ui.features

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavHostController) {
    var animateStart by remember { mutableStateOf(false) }
    val offsetY by animateDpAsState(
        targetValue = if (animateStart) (-150).dp else 0.dp,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "SwanGlide"
    )

    LaunchedEffect(Unit) {
        delay(400)
        animateStart = true
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF000416)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.swanie_splash),
                contentDescription = "Swan",
                modifier = Modifier.size(120.dp).offset(y = offsetY)
            )

            AnimatedVisibility(
                visible = animateStart,
                enter = fadeIn(animationSpec = tween(1000))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 160.dp) // Adjust padding to reveal text
                ) {
                    Text("Swanie's Portfolio", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("Crypto & Precious Metals", style = MaterialTheme.typography.bodyLarge, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = { navController.navigate(Routes.HOLDINGS) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("View Holdings", color = Color.White)
                    }
                }
            }
        }
    }
}