@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import kotlinx.coroutines.Dispatchers
import com.swanie.portfolio.ui.settings.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UnlockVaultScreen(
    navController: NavController
) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val siteBg = Color(0xFF000416)
    val siteText = Color.White
    val accentSilver = Color(0xFFC0C0C0)
    var nameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()

    val contentAlpha = remember { Animatable(0f) }

    // --- 🛡️ TRIGGER BIOMETRICS ON ENTRY ---
    LaunchedEffect(isBiometricEnabled) {
        if (!isBiometricEnabled) {
            // Direct pass: when biometric login is disabled, immediately unlock.
            authViewModel.setAuthenticated()
        } else {
            delay(500)
            (context as? FragmentActivity)?.let { activity ->
                authViewModel.triggerBiometricUnlock(activity)
            }
        }
        contentAlpha.animateTo(1f, tween(800))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentSilver)
                }
                Text(
                    text = "VAULT LOCKED",
                    color = siteText,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }

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

                Spacer(Modifier.height(40.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it.trim().replace("\\s".toRegex(), "")
                        loginError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username", color = siteText.copy(alpha = 0.7f)) },
                    placeholder = { Text("Username", color = siteText.copy(alpha = 0.45f)) },
                    textStyle = LocalTextStyle.current.copy(color = siteText),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentSilver,
                        unfocusedBorderColor = siteText.copy(alpha = 0.2f),
                        focusedTextColor = siteText,
                        unfocusedTextColor = siteText,
                        cursorColor = accentSilver
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it.replace("\\s".toRegex(), ""); loginError = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password", color = siteText.copy(alpha = 0.7f)) },
                    textStyle = LocalTextStyle.current.copy(color = siteText),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = accentSilver
                            )
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentSilver,
                        unfocusedBorderColor = siteText.copy(alpha = 0.2f),
                        focusedTextColor = siteText,
                        unfocusedTextColor = siteText,
                        cursorColor = accentSilver
                    )
                )
                loginError?.let {
                    Text(
                        text = it,
                        color = siteText.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (isLoggingIn) return@Button
                        isLoggingIn = true
                        scope.launch {
                            val normalizedUserName = nameInput.trim().replace("\\s".toRegex(), "")
                            val normalizedPassword = passwordInput.trim().replace("\\s".toRegex(), "")
                            val success = mainViewModel.verifyCredentials(normalizedUserName, normalizedPassword)
                            withContext(Dispatchers.Main) {
                                isLoggingIn = false
                                if (success) {
                                    navController.navigate(Routes.HOLDINGS) {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    loginError = "Incorrect Name or Password"
                                    nameInput = ""
                                    passwordInput = ""
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoggingIn,
                    colors = ButtonDefaults.buttonColors(containerColor = siteText, contentColor = siteBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoggingIn) {
                        Text("VERIFYING...", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    } else {
                        Text("LOGIN", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }

                // --- 🚀 UNLOCK BUTTON (TRIPWIRE) ---
                Button(
                    onClick = {
                        if (!isBiometricEnabled) {
                            authViewModel.setAuthenticated()
                        } else {
                            (context as? FragmentActivity)?.let { activity ->
                                authViewModel.triggerBiometricUnlock(activity, forcePrompt = true)
                            }
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

                Spacer(modifier = Modifier.height(14.dp))
                TextButton(onClick = { navController.navigate(Routes.CREATE_ACCOUNT) }) {
                    Text(
                        "Don't have an account? Create one here",
                        color = accentSilver,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
