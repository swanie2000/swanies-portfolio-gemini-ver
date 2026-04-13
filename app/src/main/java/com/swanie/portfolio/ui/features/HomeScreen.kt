package com.swanie.portfolio.ui.features

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.alpha
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
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
    val authState by authViewModel.authState.collectAsState()

    val userThemeBgColor = remember(siteBgHex) {
        try { Color(android.graphics.Color.parseColor(siteBgHex)) }
        catch (e: Exception) { Color(0xFF000416) }
    }
    val userThemeTextColor = remember(siteTextHex) {
        try { Color(android.graphics.Color.parseColor(siteTextHex)) }
        catch (e: Exception) { Color.White }
    }

    // 🛰️ STABILIZED HANDSHAKE (No Auto-Navigate)
    LaunchedEffect(Unit) {
        delay(1200)
        authViewModel.performSilentHandshake()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("AUTH_DEBUG", "Google Sign-In Success: ${account.email}")
                authViewModel.handleSignInResult(account)
            } catch (e: ApiException) {
                val statusCode = e.statusCode
                val statusMessage = com.google.android.gms.common.api.CommonStatusCodes.getStatusCodeString(statusCode)
                Log.e("AUTH_DEBUG", "Google Sign-In Failed! Status: $statusCode ($statusMessage)")
                
                // Show Error specifically for Developer/Configuration issues
                if (statusCode == 10 || statusCode == 12501) {
                    Log.e("AUTH_DEBUG", "CRITICAL: Check SHA-1 and Web Client ID in Firebase Console.")
                }

                authViewModel.handleSignInResult(null)
            }
        }
    }

    // 🛡️ NAVIGATION ENGINE (Manual Control Only)
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated) {
            navController.navigate(Routes.HOLDINGS) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    }

    // --- ANIMATION STATE ---
    var animationStarted by remember { mutableStateOf(false) }
    var animateText by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    val radiusPercent by animateFloatAsState(targetValue = if (animationStarted) 1.5f else 0f, animationSpec = tween(800), label = "")
    val swanYOffset by animateDpAsState(targetValue = if (animationStarted) (-100).dp else (-600).dp, animationSpec = tween(900, delayMillis = 120), finishedListener = { animateText = true }, label = "")
    val alpha by animateFloatAsState(targetValue = if (animationStarted) 1f else 0f, animationSpec = tween(700, delayMillis = 120), label = "")

    val configuration = LocalConfiguration.current
    val minDimension = min(configuration.screenWidthDp, configuration.screenHeightDp)
    val logoSize = (minDimension * 0.66f).dp

    LaunchedEffect(Unit) {
        delay(50); animationStarted = true
    }

    Box(modifier = Modifier.fillMaxSize().background(userThemeBgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerPoint = Offset(size.width / 2f, size.height / 2f)
            val maxDim = size.width.coerceAtLeast(size.height)
            if (radiusPercent > 0.01f) {
                drawCircle(brush = Brush.radialGradient(colors = listOf(userThemeBgColor.copy(alpha = 0.35f), Color.Transparent), center = centerPoint, radius = maxDim * radiusPercent), radius = maxDim * radiusPercent, center = centerPoint)
            }
        }

        if (!isExiting) {
            Box(modifier = Modifier.align(Alignment.Center).offset(y = swanYOffset).zIndex(1f), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(logoSize).graphicsLayer(alpha = alpha))
            }

            AnimatedVisibility(visible = animateText, enter = fadeIn(tween(500, 50)), modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Swanie's Portfolio", style = MaterialTheme.typography.headlineLarge, color = userThemeTextColor, fontWeight = FontWeight.Bold)
                    Text(text = "Crypto & Precious Metals", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Thin, fontSize = 12.sp, letterSpacing = 3.sp), color = userThemeTextColor.copy(alpha = 0.8f))
                }
            }
        }

        // --- 🚀 MODIFIED AUTH TRAY ---
        AnimatedVisibility(
            visible = animateText && !isExiting,
            enter = fadeIn(tween(500, 200)) + slideInVertically(initialOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp)
        ) {
            AuthTray(
                authState = authState,
                onLoginClick = {
                    if (authState is AuthViewModel.AuthState.VaultFound) {
                        navController.navigate(Routes.UNLOCK_VAULT)
                    } else {
                        val client = authViewModel.googleDriveService.getGoogleSignInClient()
                        googleSignInLauncher.launch(client.signInIntent)
                    }
                },
                onCreateAccountClick = {
                    isExiting = true
                    navController.navigate(Routes.CREATE_ACCOUNT)
                },
                trayTextColor = userThemeTextColor
            )
        }
    }
}

@Composable
fun AuthTray(
    authState: AuthViewModel.AuthState,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    trayTextColor: Color
) {
    val isLoading = authState is AuthViewModel.AuthState.Loading
    val isVaultFound = authState is AuthViewModel.AuthState.VaultFound

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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVaultFound) Color(0xFFFFD700) else Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Text(if (isVaultFound) "UNLOCK VAULT" else "LOGIN", fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onCreateAccountClick) {
                Text("Create New Vault", color = trayTextColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}