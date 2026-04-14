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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel

@Composable
fun CreateAccountScreen(
    navController: NavController
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()

    val siteBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val siteText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val accentGold = Color(0xFFFFD700)

    val authState by authViewModel.authState.collectAsState()

    // --- FULL FORM STATE ---
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordHint by remember { mutableStateOf("") }
    var syncToDrive by remember { mutableStateOf(true) }

    var isPasswordVisible by remember { mutableStateOf(false) }

    // --- VALIDATION ---
    val cleanPass = password.replace("\\s".toRegex(), "")
    val hasMinLength = cleanPass.length >= 8
    val hasCapital = cleanPass.any { it.isUpperCase() }
    val hasNumber = cleanPass.any { it.isDigit() }
    val hasSymbol = cleanPass.any { !it.isLetterOrDigit() }
    val passwordsMatch = cleanPass == confirmPassword && cleanPass.isNotEmpty()

    val isFormValid = fullName.length > 2 && email.contains("@") &&
            hasMinLength && hasCapital && hasNumber && hasSymbol &&
            passwordsMatch

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated) {
            // 🚀 JUMP TO HOLDINGS: Clear backstack so setup form is unreachable
            navController.navigate(Routes.HOLDINGS) {
                popUpTo(Routes.HOME) { inclusive = true }
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

    Box(modifier = Modifier.fillMaxSize().background(siteBg).statusBarsPadding().imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(60.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "CREATE ACCOUNT", color = siteText, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(32.dp))

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentGold,
                unfocusedBorderColor = siteText.copy(0.15f),
                focusedTextColor = siteText,
                unfocusedTextColor = siteText,
                cursorColor = accentGold
            )

            InputLabel("FULL NAME", siteText)
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true)

            Spacer(Modifier.height(16.dp))
            InputLabel("EMAIL ADDRESS", siteText)
            OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true)

            Spacer(Modifier.height(16.dp))
            InputLabel("PASSWORD", siteText)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.replace("\\s".toRegex(), "") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = siteText.copy(0.4f))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val dim = siteText.copy(alpha = 0.3f)
                Text(text = "• 8+ Chars", color = if (hasMinLength) accentGold else dim, fontSize = 9.sp)
                Text(text = "• Upper", color = if (hasCapital) accentGold else dim, fontSize = 9.sp)
                Text(text = "• Number", color = if (hasNumber) accentGold else dim, fontSize = 9.sp)
                Text(text = "• Special", color = if (hasSymbol) accentGold else dim, fontSize = 9.sp)
            }

            Spacer(Modifier.height(16.dp))
            InputLabel("CONFIRM PASSWORD", siteText)
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it.replace("\\s".toRegex(), "") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true)

            Spacer(Modifier.height(20.dp))
            InputLabel("RECOVERY HINT", siteText)
            OutlinedTextField(value = passwordHint, onValueChange = { passwordHint = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true)

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { syncToDrive = !syncToDrive }) {
                Checkbox(checked = syncToDrive, onCheckedChange = { syncToDrive = it }, colors = CheckboxDefaults.colors(checkedColor = accentGold))
                Text("Sync Vault to Google Drive", color = siteText.copy(0.7f), fontSize = 12.sp)
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    // 🏛️ SOVEREIGN INITIALIZATION: Provision the vault and navigate
                    val vaultName = if (fullName.isNotBlank()) "$fullName's Vault" else "Sovereign Vault"
                    authViewModel.initializeNewVault(vaultName)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = siteText, contentColor = siteBg, disabledContainerColor = siteText.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "CREATE ACCOUNT", fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
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