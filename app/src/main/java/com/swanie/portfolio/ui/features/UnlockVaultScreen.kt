@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.zIndex
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import com.swanie.portfolio.ui.settings.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UnlockVaultScreen(
    navController: NavController
) {
    val activity = LocalContext.current as FragmentActivity
    val mainViewModel: MainViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel(activity)
    val context = activity
    val scope = rememberCoroutineScope()
    val siteBgHex by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextHex by themeViewModel.siteTextColor.collectAsState()
    val cardBgHex by themeViewModel.cardBackgroundColor.collectAsState()
    val siteBg = Color(siteBgHex.ifBlank { "#000416" }.toColorInt())
    val siteText = Color(siteTextHex.ifBlank { "#FFFFFF" }.toColorInt())
    val dialogBg = Color(cardBgHex.ifBlank { "#121212" }.toColorInt())
    val accentSilver = siteText.copy(alpha = 0.82f)
    var nameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }
    var biometricLockedUntilPassword by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryUserName by remember { mutableStateOf("") }
    var recoveryEmail by remember { mutableStateOf("") }
    var recoveredHint by remember { mutableStateOf<String?>(null) }
    var isRecovering by remember { mutableStateOf(false) }
    var recoveryMessage by remember { mutableStateOf<String?>(null) }

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val requirePasswordAfterBiometricFailure by settingsViewModel
        .requirePasswordAfterBiometricFailure
        .collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val contentAlpha = remember { Animatable(0f) }

    // UI-only intro fade; authentication is explicit via password or biometric button tap.
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(800))
    }

    // Navigate immediately after a successful biometric authentication.
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthViewModel.AuthState.Authenticated -> {
                navController.navigate(Routes.HOLDINGS) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthViewModel.AuthState.Error -> {
                loginError = state.message
                if (requirePasswordAfterBiometricFailure) {
                    biometricLockedUntilPassword = true
                }
                authViewModel.clearError()
            }
            else -> Unit
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentSilver)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.swanie_foreground),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "PORTFOLIO LOCKED",
                color = siteText,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "IDENTITY VERIFICATION",
                color = siteText.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha.value },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = VisualTransformation.None,
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
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
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
                    supportingText = {
                        if (passwordInput.isBlank()) {
                            TextButton(
                                onClick = { showRecoveryDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "FORGOT PASSWORD?",
                                    color = accentSilver,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }
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
                            var success = false
                            try {
                                val normalizedUserName = nameInput.trim().replace("\\s".toRegex(), "")
                                val normalizedPassword = passwordInput.trim().replace("\\s".toRegex(), "")
                                success = mainViewModel.verifyCredentials(normalizedUserName, normalizedPassword)
                            } catch (_: Exception) {
                                loginError = "Authentication failed. Please try again."
                            } finally {
                                delay(2000)
                                isLoggingIn = false
                            }

                            if (success) {
                                    biometricLockedUntilPassword = false
                                    authViewModel.setAuthenticated()
                                    navController.navigate(Routes.HOLDINGS) {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                        launchSingleTop = true
                                    }
                            } else {
                                loginError = "Incorrect Name or Password"
                            }
                         }
                    },
                    modifier = Modifier.fillMaxWidth(0.94f).height(50.dp),
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

                Spacer(modifier = Modifier.height(10.dp))

                if (isBiometricEnabled && !biometricLockedUntilPassword) {
                    // Show biometric option only when user enables it in settings.
                    Button(
                        onClick = {
                            (context as? FragmentActivity)?.let { activity ->
                                authViewModel.triggerBiometricUnlock(activity, forcePrompt = true)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.94f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = siteText, contentColor = siteBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("USE BIOMETRICS", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                } else if (isBiometricEnabled && biometricLockedUntilPassword) {
                    Text(
                        text = "Use password to continue before trying biometrics again.",
                        color = siteText.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                TextButton(onClick = { navController.navigate(Routes.CREATE_ACCOUNT) }) {
                    Text(
                        "CREATE ACCOUNT",
                        color = accentSilver,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isRecovering) {
                    showRecoveryDialog = false
                    recoveryUserName = ""
                    recoveryEmail = ""
                    recoveredHint = null
                    recoveryMessage = null
                }
            },
            title = { Text("Recover Access") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter your username and email. Then verify biometrics to reveal your hint.",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = recoveryUserName,
                        onValueChange = {
                            recoveryUserName = it
                            recoveryMessage = null
                        },
                        label = { Text("Username") },
                        singleLine = true,
                        enabled = !isRecovering && recoveredHint == null
                    )
                    OutlinedTextField(
                        value = recoveryEmail,
                        onValueChange = {
                            recoveryEmail = it
                            recoveryMessage = null
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        enabled = !isRecovering && recoveredHint == null
                    )
                    recoveredHint?.let {
                        Text("Hint: $it", fontWeight = FontWeight.SemiBold)
                    }
                    recoveryMessage?.let { Text(it, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isRecovering,
                    onClick = {
                        if (isRecovering) return@TextButton
                        scope.launch {
                            isRecovering = true
                            try {
                                if (recoveredHint == null) {
                                    if (recoveryUserName.isBlank() || recoveryEmail.isBlank()) {
                                        recoveryMessage = "Please enter both username and email."
                                    } else {
                                        authViewModel.authenticateForRecovery(
                                            activity = activity,
                                            onSuccess = {
                                                scope.launch {
                                                    val hint = mainViewModel.getPasswordHintForRecovery(
                                                        userName = recoveryUserName,
                                                        email = recoveryEmail
                                                    )
                                                    recoveryMessage = if (hint == null) {
                                                        "Recovery verification failed."
                                                    } else {
                                                        recoveredHint = hint
                                                        "Hint revealed."
                                                    }
                                                }
                                            },
                                            onError = { message ->
                                                recoveryMessage = message
                                            }
                                        )
                                    }
                                } else {
                                    showRecoveryDialog = false
                                    recoveryMessage = null
                                }
                            } finally {
                                isRecovering = false
                            }
                        }
                    }
                ) {
                    Text(if (recoveredHint == null) "VERIFY & SHOW HINT" else "DONE")
                }
            },
            containerColor = dialogBg,
            dismissButton = null
        )
    }
}
