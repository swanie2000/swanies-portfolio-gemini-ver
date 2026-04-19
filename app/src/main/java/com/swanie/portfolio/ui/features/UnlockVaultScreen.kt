@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.swanie.portfolio.security.SecurityManager
import androidx.compose.ui.zIndex
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.components.BoutiqueHeader
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UnlockVaultScreen(
    navController: NavController
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val siteBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val siteText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    val authState by authViewModel.authState.collectAsState()

    val contentAlpha = remember { Animatable(0f) }

    // --- 🛡️ TRIGGER BIOMETRICS ON ENTRY ---
    LaunchedEffect(Unit) {
        delay(500)
        (context as? FragmentActivity)?.let { activity ->
            authViewModel.triggerBiometricUnlock(activity)
        }
        contentAlpha.animateTo(1f, tween(800))
    }

    // --- 🛡️ NAVIGATION LOGIC ---
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated) {
            navController.navigate(Routes.HOLDINGS) {
                popUpTo(Routes.UNLOCK_VAULT) { inclusive = true }
            }
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? FragmentActivity)?.window ?: (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isDark = ColorUtils.calculateLuminance(siteBg.toArgb()) < 0.5
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(siteBg)
        .statusBarsPadding()
        .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 🦢 BOUTIQUE HEADER ---
            BoutiqueHeader(
                title = "VAULT LOCKED",
                onBack = { navController.popBackStack() },
                textColor = siteText
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .graphicsLayer { alpha = contentAlpha.value },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                
                Text(
                    text = "IDENTITY VERIFICATION",
                    color = siteText.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(100.dp))

                // --- 🚀 UNLOCK BUTTON (TRIPWIRE) ---
                Button(
                    onClick = {
                        (context as? FragmentActivity)?.let { activity ->
                            authViewModel.triggerBiometricUnlock(activity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = siteText, contentColor = siteBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("RETRY BIOMETRICS", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
