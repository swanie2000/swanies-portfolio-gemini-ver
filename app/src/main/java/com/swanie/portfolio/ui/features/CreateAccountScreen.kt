@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // --- 🚀 THE SPACE-KILLER LOGIC ---
    val cleanPassword = password.replace("\\s".toRegex(), "")
    val cleanConfirm = confirmPassword.replace("\\s".toRegex(), "")

    // --- 🛡️ VALIDATION CRITERIA ---
    val hasMinLength = cleanPassword.length >= 8
    val hasNumber = cleanPassword.any { it.isDigit() }
    val hasSymbol = cleanPassword.any { !it.isLetterOrDigit() }
    val passwordsMatch = cleanPassword == cleanConfirm && cleanPassword.isNotEmpty()
    
    val isFormValid = fullName.length > 2 && hasMinLength && hasNumber && hasSymbol && passwordsMatch

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated) {
            navController.navigate(Routes.HOLDINGS) {
                popUpTo(Routes.CREATE_ACCOUNT) { inclusive = true }
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Image(
                painter = painterResource(id = R.drawable.swanie_foreground),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "SWANIE'S VAULT", color = siteText, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text(text = "Sovereign Asset Protection", color = siteText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(40.dp))

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentGold,
                unfocusedBorderColor = siteText.copy(0.1f),
                focusedTextColor = siteText,
                unfocusedTextColor = siteText,
                cursorColor = accentGold,
                focusedLabelColor = accentGold,
                unfocusedLabelColor = siteText.copy(0.4f)
            )

            InputLabel("FULL NAME / ALIAS", siteText)
            OutlinedTextField(
                value = fullName, 
                onValueChange = { fullName = it }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(12.dp), 
                colors = textFieldColors, 
                singleLine = true,
                placeholder = { Text("e.g. Satoshi Nakamoto", color = siteText.copy(0.2f)) }
            )

            Spacer(Modifier.height(20.dp))
            InputLabel("VAULT ACCESS KEY (PASSWORD)", siteText)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.replace("\\s".toRegex(), "") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = siteText.copy(0.6f))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // --- 🛡️ CRITERIA CHIPS ---
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CriteriaChip("8+ Chars", hasMinLength, accentGold, siteText)
                CriteriaChip("Number", hasNumber, accentGold, siteText)
                CriteriaChip("Special", hasSymbol, accentGold, siteText)
            }

            Spacer(Modifier.height(20.dp))
            InputLabel("CONFIRM KEY", siteText)
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it.replace("\\s".toRegex(), "") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                singleLine = true,
                isError = confirmPassword.isNotEmpty() && !passwordsMatch
            )

            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(text = "Keys do not match", color = Color.Red.copy(0.7f), fontSize = 11.sp, modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 4.dp))
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { authViewModel.setAuthenticated() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = accentGold, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(text = "INITIALIZE SECURE VAULT", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
            }
            
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Your data is stored locally and never leaves this device.",
                color = siteText.copy(0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CriteriaChip(label: String, isMet: Boolean, activeColor: Color, baseColor: Color) {
    Surface(
        shape = CircleShape,
        color = if (isMet) activeColor.copy(alpha = 0.15f) else baseColor.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, if (isMet) activeColor else baseColor.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isMet) {
                Icon(Icons.Default.Check, contentDescription = null, tint = activeColor, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(text = label, color = if (isMet) activeColor else baseColor.copy(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InputLabel(label: String, color: Color) {
    Text(
        text = label,
        color = color.copy(alpha = 0.5f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp)
    )
}