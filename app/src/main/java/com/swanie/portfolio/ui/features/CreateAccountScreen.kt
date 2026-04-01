@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import android.app.Activity
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun CreateAccountScreen(
    navController: NavController
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    val siteBgColor by themeViewModel.siteBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()

    val siteBg = Color(siteBgColor.ifBlank { "#000416" }.toColorInt())
    val siteText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())
    val cardBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val accentGold = Color(0xFFFFD700)

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordHint by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Privacy States
    var hasAcceptedTerms by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    var selectedCurrency by remember { mutableStateOf("USD") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()

    // Password Criteria Logic
    val hasMinLength = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    val passwordCriteriaMet = hasMinLength && hasNumber && hasSymbol

    val shieldScale = remember { Animatable(0f) }
    val formAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)
        scope.launch {
            shieldScale.animateTo(1.1f, spring(Spring.DampingRatioMediumBouncy))
            shieldScale.animateTo(1f, spring())
        }
        formAlpha.animateTo(1f, tween(800))
    }

    val isFormValid = fullName.length > 2 && email.contains("@") && passwordCriteriaMet

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
        // --- 🏰 STATIC UNIVERSAL HEADER ---
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .zIndex(10f)
            .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = { showLanguageSheet = true },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.Language, null, tint = siteText.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
            }

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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(110.dp))

            Text("SWANIE'S PORTFOLIO", color = siteText, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, maxLines = 1)
            Text("SECURE ASSET VAULT", color = siteText.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)

            Spacer(Modifier.height(40.dp))

            Column(modifier = Modifier.graphicsLayer { alpha = formAlpha.value }) {
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
                    value = fullName, onValueChange = { fullName = it },
                    label = { Text("FULL NAME", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("EMAIL ADDRESS", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("VAULT PASSWORD", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = siteText.copy(0.6f))
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true
                )

                // --- 🔑 PASSWORD CRITERIA INDICATORS ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CriteriaChip("8+ Chars", hasMinLength, siteText)
                    CriteriaChip("1 Number", hasNumber, siteText)
                    CriteriaChip("1 Symbol", hasSymbol, siteText)
                }

                Spacer(Modifier.height(16.dp))

                // --- 💡 RECOVERY HINT ---
                OutlinedTextField(
                    value = passwordHint,
                    onValueChange = { passwordHint = it },
                    label = { Text("RECOVERY HINT", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true,
                    placeholder = { Text("e.g. My first car", fontSize = 12.sp, color = siteText.copy(0.3f)) }
                )
                Text(
                    text = "This hint is stored on your Google Drive for device recovery.",
                    color = siteText.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                // --- 🛡️ THE PRIVACY PLEDGE ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasAcceptedTerms = !hasAcceptedTerms },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasAcceptedTerms,
                        onCheckedChange = { hasAcceptedTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = accentGold, uncheckedColor = siteText.copy(0.4f))
                    )
                    Text(
                        text = "I acknowledge my data is stored ONLY on my personal Google Drive. The creator holds NO access to my info.",
                        color = siteText.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                // ⚖️ LINKED TO THE NEW TERMS & CONDITIONS PAGE
                TextButton(onClick = { navController.navigate(Routes.TERMS_CONDITIONS) }) {
                    Text("VIEW FULL TERMS & PRIVACY PLEDGE", color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))

                Text("BASE CURRENCY", color = siteText, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("USD", "EUR", "GBP", "JPY").forEach { curr ->
                        val isSelected = selectedCurrency == curr
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) siteText.copy(0.15f) else Color.Transparent)
                                .border(1.dp, if (isSelected) siteText else siteText.copy(0.2f), RoundedCornerShape(8.dp))
                                .clickable { selectedCurrency = curr },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(curr, color = siteText, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal)
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))

                // --- 🚀 CONNECT BUTTON ---
                Button(
                    onClick = { /* Init Google Drive Sync */ },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    enabled = isFormValid && hasAcceptedTerms,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = siteText,
                        contentColor = siteBg,
                        disabledContainerColor = siteText.copy(0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "CONNECT GOOGLE VAULT",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text("ALREADY HAVE A VAULT? SIGN IN", color = siteText.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(350.dp))
        }

        // --- 🌎 LANGUAGE SHEET ---
        if (showLanguageSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLanguageSheet = false },
                sheetState = sheetState,
                containerColor = cardBg
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("SELECT LANGUAGE", color = siteText, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(Modifier.height(16.dp))
                    listOf("English", "Español", "Français", "Deutsch", "日本語").forEach { lang ->
                        TextButton(onClick = { selectedLanguage = lang; showLanguageSheet = false }, modifier = Modifier.fillMaxWidth()) {
                            Text(lang, color = siteText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun CriteriaChip(label: String, isMet: Boolean, themeColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else themeColor.copy(alpha = 0.3f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            color = if (isMet) themeColor else themeColor.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = if (isMet) FontWeight.Bold else FontWeight.Normal
        )
    }
}