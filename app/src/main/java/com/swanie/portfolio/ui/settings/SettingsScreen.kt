package com.swanie.portfolio.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.security.AuthPolicy
import com.swanie.portfolio.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    mainViewModel: MainViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val timeoutOptionsSeconds = listOf(-1, 15, 30, 60, 300, 900)
    fun formatTimeoutLabel(seconds: Int): String = when {
        seconds < 0 -> "Never"
        seconds < 60 -> "$seconds seconds"
        seconds % 60 == 0 -> "${seconds / 60} minutes"
        else -> "$seconds seconds"
    }

    val context = LocalContext.current
    val isCompactMode by settingsViewModel.isCompactViewEnabled.collectAsState()
    val isHighVisibilityMode by settingsViewModel.isHighVisibilityMode.collectAsState()
    val confirmDelete by mainViewModel.confirmDelete.collectAsState(initial = true)
    val currentVaultId by mainViewModel.currentVaultId.collectAsState(initial = 1)
    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val requirePasswordAfterBiometricFailure by settingsViewModel
        .requirePasswordAfterBiometricFailure
        .collectAsState()
    val loginResumeTimeoutSeconds by settingsViewModel.loginResumeTimeoutSeconds.collectAsState()

    val useGradient by mainViewModel.useGradient.collectAsState()
    val gradientAmount by mainViewModel.gradientAmount.collectAsState()

    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val safeBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    var isExiting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordChangeBusy by remember { mutableStateOf(false) }
    var passwordChangeMessage by remember { mutableStateOf<String?>(null) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }

    var showFactoryResetDialog by remember { mutableStateOf(false) }

    val passwordStrength = AuthPolicy.evaluatePasswordStrength(newPassword)
    val cleanNewPassword = passwordStrength.normalized
    val hasMinLength = passwordStrength.hasMinLength
    val hasCapital = passwordStrength.hasCapital
    val hasNumber = passwordStrength.hasNumber
    val hasSymbol = passwordStrength.hasSymbol
    val cleanConfirmNewPassword = confirmNewPassword.trim().replace("\\s".toRegex(), "")
    val passwordsMatch = cleanNewPassword == cleanConfirmNewPassword && cleanNewPassword.isNotEmpty()

    if (showFactoryResetDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            title = {
                Text(
                    text = "RESET VAULT TO FACTORY?",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "This will completely wipe the current vault, including all assets and history. This action is sovereign and permanent.",
                    color = safeText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            settingsViewModel.factoryResetAllData()
                            showFactoryResetDialog = false
                            isExiting = true
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) {
                    Text("RESET EVERYTHING", color = Color.Red, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetDialog = false }) {
                    Text("CANCEL", color = safeText.copy(alpha = 0.6f))
                }
            },
            containerColor = safeBg
        )
    }

    if (showPasswordResetDialog) {
        AlertDialog(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding(),
            onDismissRequest = {
                if (!passwordChangeBusy) {
                    showPasswordResetDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                    currentPasswordVisible = false
                    newPasswordVisible = false
                    confirmPasswordVisible = false
                    passwordChangeMessage = null
                }
            },
            title = { Text("Change Password") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Requires current password and biometric confirmation.",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            passwordChangeMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it.replace("\\s".toRegex(), "")
                            passwordChangeMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val dim = safeText.copy(alpha = 0.3f)
                        Text(text = "• 8+ Chars", color = if (hasMinLength) safeText else dim, fontSize = 9.sp)
                        Text(text = "• Upper", color = if (hasCapital) safeText else dim, fontSize = 9.sp)
                        Text(text = "• Number", color = if (hasNumber) safeText else dim, fontSize = 9.sp)
                        Text(text = "• Special", color = if (hasSymbol) safeText else dim, fontSize = 9.sp)
                    }
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = {
                            confirmNewPassword = it.replace("\\s".toRegex(), "")
                            passwordChangeMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    passwordChangeMessage?.let { message ->
                        Text(
                            text = message,
                            color = safeText.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !passwordChangeBusy,
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity == null) {
                            passwordChangeMessage = "Unable to open biometric prompt."
                            return@TextButton
                        }
                        val normalizedCurrent = currentPassword.trim().replace("\\s".toRegex(), "")
                        val normalizedNew = newPassword.trim().replace("\\s".toRegex(), "")
                        val normalizedConfirm = confirmNewPassword.trim().replace("\\s".toRegex(), "")

                        when {
                            normalizedCurrent.isBlank() || normalizedNew.isBlank() || normalizedConfirm.isBlank() -> {
                                passwordChangeMessage = "Please complete all password fields."
                                return@TextButton
                            }
                            !passwordStrength.isValid -> {
                                passwordChangeMessage = "New password must be 8+ chars with uppercase, number, and symbol."
                                return@TextButton
                            }
                            normalizedNew != normalizedConfirm -> {
                                passwordChangeMessage = "New password and confirmation do not match."
                                return@TextButton
                            }
                        }

                        passwordChangeBusy = true
                        settingsViewModel.authenticateForSensitiveAction(
                            activity = activity,
                            onSuccess = {
                                scope.launch {
                                    try {
                                        val changed = mainViewModel.changePassword(
                                            currentPassword = normalizedCurrent,
                                            newPassword = normalizedNew
                                        )
                                        if (changed) {
                                            showPasswordResetDialog = false
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmNewPassword = ""
                                            currentPasswordVisible = false
                                            newPasswordVisible = false
                                            confirmPasswordVisible = false
                                            passwordChangeMessage = null
                                            Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            passwordChangeMessage = "Unable to update password. Check your current password."
                                        }
                                    } finally {
                                        passwordChangeBusy = false
                                    }
                                }
                            },
                            onError = { message ->
                                passwordChangeMessage = message
                                passwordChangeBusy = false
                            }
                        )
                    }
                ) {
                    Text(if (passwordChangeBusy) "UPDATING..." else "UPDATE")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !passwordChangeBusy,
                    onClick = {
                        showPasswordResetDialog = false
                        currentPassword = ""
                        newPassword = ""
                        confirmNewPassword = ""
                        currentPasswordVisible = false
                        newPasswordVisible = false
                        confirmPasswordVisible = false
                        passwordChangeMessage = null
                    }
                ) {
                    Text("CANCEL")
                }
            },
            containerColor = safeBg,
            titleContentColor = safeText,
            textContentColor = safeText
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (!isExiting) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp)) {
                    Text(text = "SETTINGS", color = safeText, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {

                    // --- SECURITY ---
                    Text("SECURITY", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    TextButton(
                        onClick = { showPasswordResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "RESET PASSWORD",
                            color = safeText,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    SettingsCheckboxItem(
                        title = "LOGIN OPTION",
                        subtitle = "Allow biometrics for login.",
                        checked = isBiometricEnabled,
                        onCheckedChange = { enabled ->
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                settingsViewModel.setBiometricEnabled(activity, enabled) { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        themeColor = safeText
                    )

                    SettingsToggleItem(
                        title = "Require Password After Biometric Failure",
                        subtitle = "After biometric failure, require password before retrying biometrics.",
                        checked = requirePasswordAfterBiometricFailure,
                        onCheckedChange = { settingsViewModel.saveRequirePasswordAfterBiometricFailure(it) },
                        themeColor = safeText
                    )

                    val selectedTimeoutIndex = timeoutOptionsSeconds
                        .indices
                        .minByOrNull { index ->
                            if (loginResumeTimeoutSeconds < 0) {
                                if (timeoutOptionsSeconds[index] < 0) 0 else Int.MAX_VALUE
                            } else if (timeoutOptionsSeconds[index] < 0) {
                                Int.MAX_VALUE
                            } else {
                                kotlin.math.abs(timeoutOptionsSeconds[index] - loginResumeTimeoutSeconds)
                            }
                        } ?: 2
                    var timeoutSliderValue by remember(loginResumeTimeoutSeconds) {
                        mutableStateOf(selectedTimeoutIndex.toFloat())
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "LOGIN TIMEOUT",
                            color = safeText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Current: ${formatTimeoutLabel(loginResumeTimeoutSeconds)}",
                            color = safeText.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (loginResumeTimeoutSeconds < 0) {
                                "Never require login again from background timeout."
                            } else {
                                "Require login again after ${formatTimeoutLabel(loginResumeTimeoutSeconds)} in background."
                            },
                            color = safeText.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Applies when returning to the app from background.",
                            color = safeText.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Slider(
                            value = timeoutSliderValue,
                            onValueChange = { timeoutSliderValue = it },
                            onValueChangeFinished = {
                                val mappedIndex = timeoutSliderValue.toInt().coerceIn(0, timeoutOptionsSeconds.lastIndex)
                                settingsViewModel.saveLoginResumeTimeoutSeconds(timeoutOptionsSeconds[mappedIndex])
                            },
                            valueRange = 0f..timeoutOptionsSeconds.lastIndex.toFloat(),
                            steps = timeoutOptionsSeconds.size - 2,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Yellow,
                                activeTrackColor = Color.Yellow,
                                inactiveTrackColor = safeText.copy(alpha = 0.2f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- INTERFACE ---
                    Text("INTERFACE", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    SettingsToggleItem(
                        title = "Use Compact Cards",
                        subtitle = "Shrink asset cards to show more on screen",
                        checked = isCompactMode,
                        onCheckedChange = { settingsViewModel.saveIsCompactViewEnabled(it) },
                        themeColor = safeText
                    )

                    // Syncs to ThemePreferences + MainViewModel; holdings/widget previews read the same flow.
                    SettingsToggleItem(
                        title = "High-Visibility Compact Cards",
                        subtitle = "Makes compact cards easier to read with larger, clearer text.",
                        checked = isHighVisibilityMode,
                        onCheckedChange = { settingsViewModel.saveIsHighVisibilityMode(it) },
                        themeColor = safeText
                    )

                    SettingsToggleItem(
                        title = "Confirm Deletion",
                        subtitle = "Show confirmation before removing an asset",
                        checked = confirmDelete,
                        onCheckedChange = { mainViewModel.setConfirmDelete(it) },
                        themeColor = safeText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- THEME & DEPTH ---
                    Text("THEME & DEPTH", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    SettingsToggleItem(
                        title = "Background Gradient",
                        subtitle = "Enable dynamic color shifting",
                        checked = useGradient,
                        onCheckedChange = { mainViewModel.setUseGradient(it) },
                        themeColor = safeText
                    )

                    if (useGradient) {
                        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
                            Text(text = "Gradient Intensity", color = safeText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = gradientAmount,
                                onValueChange = { mainViewModel.setGradientAmount(it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Yellow,
                                    activeTrackColor = Color.Yellow,
                                    inactiveTrackColor = safeText.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- MANAGEMENT ---
                    Text("MANAGEMENT", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Button(
                        onClick = {
                            isExiting = true
                            navController.navigate(Routes.PORTFOLIO_MANAGER)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("PORTFOLIO MANAGER", color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isExiting = true
                            navController.navigate(Routes.THEME_STUDIO)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("THEME MANAGER", color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // --- SYSTEM ACTIONS ---
                    Text("SYSTEM ACTIONS", color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Button(
                        onClick = { showFactoryResetDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("FACTORY DEFAULT", color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, themeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = themeColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = themeColor.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Yellow,
                checkmarkColor = themeColor
            )
        )
    }
}

@Composable
fun SettingsCheckboxItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = themeColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = themeColor.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Yellow,
                checkmarkColor = themeColor
            )
        )
    }
}
