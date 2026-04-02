@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import android.app.Activity
import androidx.compose.animation.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swanie.portfolio.R
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
    val scope = rememberCoroutineScope()

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val siteBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val siteText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val accentGold = Color(0xFFFFD700)

    val authState by authViewModel.authState.collectAsState()
    val passwordHint by authViewModel.passwordHint.collectAsState()
    val metadata by authViewModel.vaultMetadata.collectAsState()

    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }

    val shieldScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)
        scope.launch {
            shieldScale.animateTo(1.1f, spring(Spring.DampingRatioMediumBouncy))
            shieldScale.animateTo(1f, spring())
        }
        contentAlpha.animateTo(1f, tween(800))
    }

    // Unlocking Logic
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated) {
            navController.navigate(Routes.HOLDINGS) {
                popUpTo(Routes.UNLOCK_VAULT) { inclusive = true }
            }
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
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
        // --- 🏰 UNIVERSAL HEADER ---
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .zIndex(10f)
            .padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.swanie_foreground),
                contentDescription = null,
                modifier = Modifier.size(100.dp).align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .scale(shieldScale.value)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentGold)
                    .border(2.dp, siteBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Security, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer { alpha = contentAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            Text(
                text = "VAULT LOCKED",
                color = siteText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "ENTER PASSWORD TO DECRYPT",
                color = siteText.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Welcome back, ${metadata?.fullName ?: "User"}",
                color = siteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = siteText,
                unfocusedBorderColor = siteText.copy(0.2f),
                focusedLabelColor = siteText,
                unfocusedLabelColor = siteText.copy(0.4f),
                cursorColor = siteText,
                focusedTextColor = siteText,
                unfocusedTextColor = siteText
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; if (authState is AuthViewModel.AuthState.Error) authViewModel.clearError() },
                label = { Text("PASSWORD", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = siteText.copy(0.6f)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true,
                isError = authState is AuthViewModel.AuthState.Error
            )

            if (authState is AuthViewModel.AuthState.Error) {
                Text(
                    text = (authState as AuthViewModel.AuthState.Error).message,
                    color = Color.Red,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp).fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- 💡 RECOVERY HINT LOGIC ---
            if (!showHint) {
                TextButton(onClick = { showHint = true }) {
                    Text("FORGOT PASSWORD?", color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(siteText.copy(alpha = 0.05f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("HINT:", color = accentGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text(text = passwordHint.ifBlank { "No hint stored." }, color = siteText.copy(0.8f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // --- 🚀 UNLOCK BUTTON ---
            Button(
                onClick = { authViewModel.verifyAndUnlockVault(password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = password.length >= 8 && authState !is AuthViewModel.AuthState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = siteText,
                    contentColor = siteBg,
                    disabledContainerColor = siteText.copy(0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = siteBg, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "UNLOCK VAULT",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "SWITCH ACCOUNT",
                    color = siteText.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
