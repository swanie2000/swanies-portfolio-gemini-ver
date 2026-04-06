@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes
import com.swanie.portfolio.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CreateAccountScreen(
    navController: NavController
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()

    val siteBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val siteText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val cardBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val accentGold = Color(0xFFFFD700)

    val authState by authViewModel.authState.collectAsState()

    // --- FORM STATE ---
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordHint by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var hasAcceptedTerms by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                authViewModel.handleSignInResult(account)
            } catch (e: ApiException) {
                authViewModel.handleSignInResult(null)
            }
        }
    }

    // --- 🛡️ SPACE-KILLER LOGIC: Physically prevents spaces from entering the state ---
    val cleanPass = password.replace("\\s".toRegex(), "")
    val cleanConfirm = confirmPassword.replace("\\s".toRegex(), "")

    val hasMinLength = cleanPass.length >= 8
    val hasNumber = cleanPass.any { it.isDigit() }
    val hasSymbol = cleanPass.any { !it.isLetterOrDigit() }
    val passwordsMatch = cleanPass == cleanConfirm && cleanPass.isNotEmpty()

    val isFormValid = fullName.length > 2 &&
            email.contains("@") &&
            hasMinLength && hasNumber && hasSymbol &&
            passwordsMatch && hasAcceptedTerms

    val formAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(300)
        formAlpha.animateTo(1f, tween(800))
    }

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
        Box(modifier = Modifier.fillMaxWidth().height(80.dp).zIndex(10f).padding(horizontal = 16.dp)) {
            Image(painter = painterResource(id = R.drawable.swanie_foreground), contentDescription = null, modifier = Modifier.size(60.dp).align(Alignment.Center))
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(scrollState), horizontalAlignment = Alignment.Start) {
            Spacer(modifier = Modifier.height(80.dp))
            Text(text = "CREATE ACCOUNT", color = siteText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(30.dp))

            Column(modifier = Modifier.graphicsLayer { alpha = formAlpha.value }) {
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = siteText.copy(0.2f),
                    focusedTextColor = siteText,
                    unfocusedTextColor = siteText,
                    cursorColor = accentGold
                )

                // Labels are now external to prevent "Disappearing Box" glitch
                InputLabel("FULL NAME", siteText)
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true)

                Spacer(Modifier.height(16.dp))
                InputLabel("EMAIL ADDRESS", siteText)
                OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

                Spacer(Modifier.height(16.dp))

                // --- 🛡️ PASSWORD 1 ---
                InputLabel("PASSWORD", siteText)
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = true, imeAction = ImeAction.Next)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, start = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val dim = siteText.copy(alpha = 0.4f)
                    Text(text = "• 8+ Chars", color = if (hasMinLength) accentGold else dim, fontSize = 11.sp)
                    Text(text = "• Number", color = if (hasNumber) accentGold else dim, fontSize = 11.sp)
                    Text(text = "• Special", color = if (hasSymbol) accentGold else dim, fontSize = 11.sp)
                }

                Spacer(Modifier.height(16.dp))

                // --- 🛡️ PASSWORD 2 (CONFIRM) ---
                InputLabel("CONFIRM PASSWORD", siteText)
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it.replace("\\s".toRegex(), "") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = siteText.copy(0.6f))
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = true, imeAction = ImeAction.Done)
                )

                if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    Text(text = "Passwords do not match", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(Modifier.height(20.dp))
                InputLabel("RECOVERY HINT", siteText)
                OutlinedTextField(value = passwordHint, onValueChange = { passwordHint = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors, singleLine = true)

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth().clickable { hasAcceptedTerms = !hasAcceptedTerms }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasAcceptedTerms, onCheckedChange = { hasAcceptedTerms = it }, colors = CheckboxDefaults.colors(checkedColor = accentGold))
                    Text(text = "Save data to my Google Drive.", color = siteText.copy(alpha = 0.7f), fontSize = 13.sp)
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        val client = authViewModel.googleDriveService.getGoogleSignInClient()
                        googleSignInLauncher.launch(client.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(containerColor = siteText, contentColor = siteBg, disabledContainerColor = siteText.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (authState is AuthViewModel.AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = siteBg, strokeWidth = 2.dp)
                    } else {
                        Text(text = "CREATE ACCOUNT", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(300.dp))
        }

        // Vault Found Dialog
        if (authState is AuthViewModel.AuthState.VaultFound) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = cardBg,
                title = { Text(text = "Vault Found", color = siteText, fontWeight = FontWeight.Bold) },
                text = { Text(text = "An existing vault was found. Login or overwrite?", color = siteText.copy(0.7f)) },
                confirmButton = {
                    TextButton(onClick = { navController.navigate(Routes.UNLOCK_VAULT) }) {
                        Text(text = "LOGIN", color = accentGold, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        authViewModel.initializeNewVault(VaultMetadata(fullName, "USD", "EN", passwordHint, cleanPass))
                    }) { Text(text = "OVERWRITE", color = Color.Red) }
                }
            )
        }
    }
}

@Composable
fun InputLabel(label: String, color: Color) {
    Text(
        text = label,
        color = color.copy(alpha = 0.5f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun CriteriaChip(label: String, isValid: Boolean, siteTextColor: Color) {
    Surface(
        shape = CircleShape,
        color = if (isValid) siteTextColor.copy(alpha = 0.2f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isValid) siteTextColor else siteTextColor.copy(alpha = 0.3f)),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = if (isValid) Icons.Default.Check else Icons.Default.Close, contentDescription = null, tint = if (isValid) siteTextColor else siteTextColor.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = label, color = if (isValid) siteTextColor else siteTextColor.copy(alpha = 0.5f), fontSize = 10.sp)
        }
    }
}