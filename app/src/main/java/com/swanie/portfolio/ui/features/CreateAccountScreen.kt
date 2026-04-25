@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.local.UserProfileEntity
import com.swanie.portfolio.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun CreateAccountScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    val siteBg = Color(0xFF000416)
    val siteText = Color.White
    val accentSilver = Color(0xFFC0C0C0)

    val scope = rememberCoroutineScope()

    // --- FULL FORM STATE ---
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordHint by remember { mutableStateOf("") }
    var syncToDrive by remember { mutableStateOf(true) }
    val isTosChecked = remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isCreatingAccount by remember { mutableStateOf(false) }
    var receiptProfile by remember { mutableStateOf<UserProfileEntity?>(null) }

    // --- VALIDATION ---
    val cleanPass = password.trim().replace("\\s".toRegex(), "")
    val hasMinLength = cleanPass.length >= 8
    val hasCapital = cleanPass.any { it.isUpperCase() }
    val hasNumber = cleanPass.any { it.isDigit() }
    val hasSymbol = cleanPass.any { !it.isLetterOrDigit() }
    val cleanConfirmPass = confirmPassword.trim().replace("\\s".toRegex(), "")
    val passwordsMatch = cleanPass == cleanConfirmPass && cleanPass.isNotEmpty()

    val isFormValid = fullName.length > 2 && email.contains("@") &&
            hasMinLength && hasCapital && hasNumber && hasSymbol &&
            passwordsMatch

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isDark = ColorUtils.calculateLuminance(siteBg.toArgb()) < 0.5
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
    }

    Box(modifier = Modifier.fillMaxSize().background(siteBg).statusBarsPadding().imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentSilver)
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
            Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(60.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "CREATE ACCOUNT", color = siteText, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(32.dp))

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = siteText,
                unfocusedBorderColor = siteText.copy(0.15f),
                focusedTextColor = siteText,
                unfocusedTextColor = siteText,
                cursorColor = siteText
            )

            InputLabel("USERNAME", siteText)
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it.replace("\\s".toRegex(), "") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true,
                placeholder = { Text("Username", color = siteText.copy(alpha = 0.45f)) }
            )

            Spacer(Modifier.height(16.dp))
            InputLabel("EMAIL ADDRESS", siteText)
            OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true)

            Spacer(Modifier.height(16.dp))
            InputLabel("PASSWORD", siteText)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.replace("\\s".toRegex(), "") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
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
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val dim = siteText.copy(alpha = 0.3f)
                Text(text = "• 8+ Chars", color = if (hasMinLength) accentSilver else dim, fontSize = 9.sp)
                Text(text = "• Upper", color = if (hasCapital) accentSilver else dim, fontSize = 9.sp)
                Text(text = "• Number", color = if (hasNumber) accentSilver else dim, fontSize = 9.sp)
                Text(text = "• Special", color = if (hasSymbol) accentSilver else dim, fontSize = 9.sp)
            }

            Spacer(Modifier.height(16.dp))
            InputLabel("CONFIRM PASSWORD", siteText)
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it.replace("\\s".toRegex(), "") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = accentSilver
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))
            InputLabel("RECOVERY HINT", siteText)
            OutlinedTextField(value = passwordHint, onValueChange = { passwordHint = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true)

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { syncToDrive = !syncToDrive }) {
                Checkbox(checked = syncToDrive, onCheckedChange = { syncToDrive = it }, colors = CheckboxDefaults.colors(checkedColor = accentSilver, checkmarkColor = siteBg))
                Text("Sync Vault to Google Drive", color = siteText.copy(0.7f), fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isTosChecked.value,
                    onCheckedChange = { isTosChecked.value = it },
                    colors = CheckboxDefaults.colors(checkedColor = accentSilver, checkmarkColor = siteBg)
                )
                Text(
                    text = "I accept the ",
                    color = siteText.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                TextButton(onClick = { navController.navigate(Routes.TERMS_CONDITIONS) }) {
                    Text(
                        "Terms and Conditions",
                        color = accentSilver,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (isCreatingAccount) return@Button
                    scope.launch {
                        isCreatingAccount = true
                        val vaultName = if (fullName.isNotBlank()) "$fullName's Vault" else "Sovereign Vault"
                        val stored = mainViewModel.createOrUpdateUserProfileAndFetchFirst(
                            displayName = fullName,
                            email = email,
                            password = cleanPass,
                            acceptedTOS = true
                        )
                        if (stored != null) {
                            mainViewModel.updateTOSAccepted(true)
                            authViewModel.initializeNewVault(vaultName)
                            receiptProfile = stored
                        } else {
                            isCreatingAccount = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isFormValid && isTosChecked.value && !isCreatingAccount,
                colors = ButtonDefaults.buttonColors(containerColor = siteText, contentColor = siteBg, disabledContainerColor = siteText.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isCreatingAccount) "CREATING..." else "CREATE ACCOUNT",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
            TextButton(onClick = { navController.navigate(Routes.UNLOCK_VAULT) }) {
                Text(
                    "Already have an account? Login here",
                    color = accentSilver,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    receiptProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Account Created Successfully") },
            text = {
                Text(
                    "Stored Username: ${profile.userName}\n\n" +
                        "Stored Email: ${profile.email}\n\n" +
                        "Stored Password: ${profile.loginPassword}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        receiptProfile = null
                        isCreatingAccount = false
                        navController.navigate(Routes.UNLOCK_VAULT) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("I've Verified It - Proceed to Login")
                }
            }
        )
    }
}

@Composable
private fun InputLabel(label: String, color: Color) {
    Text(
        text = label,
        color = color.copy(alpha = 0.4f),
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp)
    )
}