package com.swanie.portfolio.ui.settings

import android.util.Log
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.MainViewModel
import com.swanie.portfolio.R
import com.swanie.portfolio.data.feedback.BugReportSubmitter
import com.swanie.portfolio.security.AuthPolicy
import com.swanie.portfolio.ui.components.showPortfolioToast
import com.swanie.portfolio.ui.i18n.languageDisplayNameForOption
import com.swanie.portfolio.ui.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    mainViewModel: MainViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val timeoutOptionsSeconds = listOf(-1, 15, 30, 60, 300, 900)
    val context = LocalContext.current
    fun formatTimeoutLabel(seconds: Int): String = when {
        seconds < 0 -> context.getString(R.string.settings_timeout_never)
        seconds < 60 -> context.getString(R.string.settings_timeout_seconds, seconds)
        seconds % 60 == 0 -> context.getString(R.string.settings_timeout_minutes, seconds / 60)
        else -> context.getString(R.string.settings_timeout_seconds, seconds)
    }
    val isCompactMode by settingsViewModel.isCompactViewEnabled.collectAsState()
    val isHighVisibilityMode by settingsViewModel.isHighVisibilityMode.collectAsState()
    val confirmDelete by mainViewModel.confirmDelete.collectAsState(initial = true)
    val currentVaultId by mainViewModel.currentVaultId.collectAsState(initial = 1)
    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val requirePasswordAfterBiometricFailure by settingsViewModel
        .requirePasswordAfterBiometricFailure
        .collectAsState()
    val loginResumeTimeoutSeconds by settingsViewModel.loginResumeTimeoutSeconds.collectAsState()
    val languageCode by settingsViewModel.languageCode.collectAsState()
    val effectiveLanguageCode = if (languageCode == "system") "en" else languageCode

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

    var showFactoryResetFirstDialog by remember { mutableStateOf(false) }
    var showFactoryResetSecondDialog by remember { mutableStateOf(false) }
    var showFactoryResetProgress by remember { mutableStateOf(false) }
    var factoryResetProgress by remember { mutableStateOf(0f) }
    var factoryResetStatus by remember { mutableStateOf("") }
    var languageExpanded by remember { mutableStateOf(false) }
    var showBugReportDialog by remember { mutableStateOf(false) }
    var bugReportMessage by remember { mutableStateOf("") }
    var bugReportSending by remember { mutableStateOf(false) }
    val settingsScrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }

    val passwordStrength = AuthPolicy.evaluatePasswordStrength(newPassword)
    val cleanNewPassword = passwordStrength.normalized
    val hasMinLength = passwordStrength.hasMinLength
    val hasCapital = passwordStrength.hasCapital
    val hasNumber = passwordStrength.hasNumber
    val hasSymbol = passwordStrength.hasSymbol
    val cleanConfirmNewPassword = confirmNewPassword.trim().replace("\\s".toRegex(), "")
    val passwordsMatch = cleanNewPassword == cleanConfirmNewPassword && cleanNewPassword.isNotEmpty()

    if (showFactoryResetFirstDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetFirstDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_factory_reset_title),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = safeText
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_factory_reset_body),
                    color = safeText.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFactoryResetFirstDialog = false
                        showFactoryResetSecondDialog = true
                    }
                ) {
                    Text(
                        stringResource(R.string.settings_factory_reset_continue),
                        color = safeText,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetFirstDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = safeText.copy(alpha = 0.7f))
                }
            },
            containerColor = safeBg,
            titleContentColor = safeText,
            textContentColor = safeText
        )
    }

    if (showFactoryResetSecondDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetSecondDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_factory_reset_sure_title),
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = safeText
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_factory_reset_sure_body),
                    color = safeText.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFactoryResetSecondDialog = false
                        showFactoryResetProgress = true
                        factoryResetProgress = 0f
                        factoryResetStatus = ""
                        settingsViewModel.factoryResetAllDataWithProgress(
                            onProgress = { p, msg ->
                                factoryResetProgress = p
                                factoryResetStatus = msg
                            },
                            onFinished = {
                                showFactoryResetProgress = false
                                settingsViewModel.coldRestartApplication()
                            }
                        )
                    }
                ) {
                    Text(
                        stringResource(R.string.settings_factory_reset_confirm_erase),
                        color = Color.Red,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetSecondDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = safeText.copy(alpha = 0.7f))
                }
            },
            containerColor = safeBg,
            titleContentColor = safeText,
            textContentColor = safeText
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
            title = { Text(stringResource(R.string.settings_change_password_title)) },
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
                        text = stringResource(R.string.settings_change_password_subtitle),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            passwordChangeMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_current_password)) },
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
                        label = { Text(stringResource(R.string.settings_new_password)) },
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
                        Text(text = stringResource(R.string.password_rule_min_chars), color = if (hasMinLength) safeText else dim, fontSize = 9.sp)
                        Text(text = stringResource(R.string.password_rule_upper), color = if (hasCapital) safeText else dim, fontSize = 9.sp)
                        Text(text = stringResource(R.string.password_rule_number), color = if (hasNumber) safeText else dim, fontSize = 9.sp)
                        Text(text = stringResource(R.string.password_rule_special), color = if (hasSymbol) safeText else dim, fontSize = 9.sp)
                    }
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = {
                            confirmNewPassword = it.replace("\\s".toRegex(), "")
                            passwordChangeMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_confirm_new_password)) },
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
                            passwordChangeMessage = context.getString(R.string.settings_password_bio_prompt_unavailable)
                            return@TextButton
                        }
                        val normalizedCurrent = currentPassword.trim().replace("\\s".toRegex(), "")
                        val normalizedNew = newPassword.trim().replace("\\s".toRegex(), "")
                        val normalizedConfirm = confirmNewPassword.trim().replace("\\s".toRegex(), "")

                        when {
                            normalizedCurrent.isBlank() || normalizedNew.isBlank() || normalizedConfirm.isBlank() -> {
                                passwordChangeMessage = context.getString(R.string.settings_password_complete_fields)
                                return@TextButton
                            }
                            !passwordStrength.isValid -> {
                                passwordChangeMessage = context.getString(R.string.settings_password_rule_error)
                                return@TextButton
                            }
                            normalizedNew != normalizedConfirm -> {
                                passwordChangeMessage = context.getString(R.string.settings_password_mismatch)
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
                                            context.showPortfolioToast(context.getString(R.string.settings_password_updated))
                                        } else {
                                            passwordChangeMessage = context.getString(R.string.settings_password_update_failed)
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
                    Text(
                        if (passwordChangeBusy) stringResource(R.string.settings_updating)
                        else stringResource(R.string.settings_update)
                    )
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
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = safeBg,
            titleContentColor = safeText,
            textContentColor = safeText
        )
    }

    if (showBugReportDialog) {
        AlertDialog(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding(),
            onDismissRequest = {
                showBugReportDialog = false
                bugReportMessage = ""
                bugReportSending = false
            },
            title = { Text(stringResource(R.string.settings_bug_report_dialog_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_bug_report_dialog_subtitle),
                        fontSize = 13.sp,
                        color = safeText.copy(alpha = 0.8f)
                    )
                    OutlinedTextField(
                        value = bugReportMessage,
                        onValueChange = { if (it.length <= 500) bugReportMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_bug_report_field_label)) },
                        supportingText = {
                            Text(
                                stringResource(R.string.settings_bug_report_char_count, bugReportMessage.length),
                                color = safeText.copy(alpha = 0.55f)
                            )
                        },
                        minLines = 4,
                        maxLines = 10,
                        enabled = !bugReportSending,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !bugReportSending,
                    onClick = {
                        if (bugReportMessage.isBlank()) {
                            context.showPortfolioToast(
                                context.getString(R.string.settings_bug_report_required),
                            )
                            return@TextButton
                        }
                        scope.launch {
                            try {
                                bugReportSending = true
                                Log.i(BugReportSubmitter.LOG_TAG, "UI: submit tapped, len=${bugReportMessage.trim().length}")
                                val result = settingsViewModel.submitBugReport(
                                    bugReportMessage.trim(),
                                    effectiveLanguageCode,
                                )
                                if (result.isSuccess) {
                                    context.showPortfolioToast(
                                        context.getString(R.string.settings_bug_report_sent),
                                        Toast.LENGTH_LONG
                                    )
                                    bugReportMessage = ""
                                    showBugReportDialog = false
                                } else {
                                    val err = result.exceptionOrNull()
                                    Log.e(
                                        BugReportSubmitter.LOG_TAG,
                                        "UI: submit returned failure: ${err?.message}",
                                        err,
                                    )
                                    context.showPortfolioToast(
                                        context.getString(R.string.settings_bug_report_failed),
                                        Toast.LENGTH_LONG
                                    )
                                }
                            } catch (e: Throwable) {
                                Log.e(BugReportSubmitter.LOG_TAG, "UI: submit threw before Result", e)
                                context.showPortfolioToast(
                                    context.getString(R.string.settings_bug_report_failed),
                                    Toast.LENGTH_LONG
                                )
                            } finally {
                                bugReportSending = false
                            }
                        }
                    }
                ) {
                    Text(
                        if (bugReportSending) {
                            stringResource(R.string.settings_bug_report_sending)
                        } else {
                            stringResource(R.string.settings_bug_report_submit)
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !bugReportSending,
                    onClick = {
                        showBugReportDialog = false
                        bugReportMessage = ""
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
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
                    .verticalScroll(settingsScrollState)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp)) {
                    Text(text = stringResource(R.string.settings_title), color = safeText, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    val languageOptions = listOf(
                        "en", "es", "pt-BR", "fr", "de", "ja", "ko", "zh-CN", "hi", "ar",
                        "zh-TW", "it", "ru", "tr", "id", "vi", "th", "pl", "nl", "uk"
                    )
                    val selectedLanguageLabel = languageDisplayNameForOption(effectiveLanguageCode)
                    ExposedDropdownMenuBox(
                        expanded = languageExpanded,
                        onExpandedChange = { languageExpanded = !languageExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedLanguageLabel,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text(stringResource(R.string.language_selector_title)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = safeText,
                                unfocusedTextColor = safeText,
                                focusedBorderColor = safeText.copy(alpha = 0.6f),
                                unfocusedBorderColor = safeText.copy(alpha = 0.3f),
                                cursorColor = safeText
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            languageOptions.forEach { option ->
                                val optionLabel = languageDisplayNameForOption(option)
                                DropdownMenuItem(
                                    text = { Text(optionLabel) },
                                    onClick = {
                                        languageExpanded = false
                                        scope.launch {
                                            settingsViewModel.saveLanguageCodeNow(option)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // --- SECURITY ---
                    Text(stringResource(R.string.settings_security), color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    TextButton(
                        onClick = { showPasswordResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_reset_password),
                            color = safeText,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    SettingsCheckboxItem(
                        title = stringResource(R.string.settings_login_option),
                        subtitle = stringResource(R.string.settings_login_option_subtitle),
                        checked = isBiometricEnabled,
                        onCheckedChange = { enabled ->
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                settingsViewModel.setBiometricEnabled(activity, enabled) { error ->
                                    context.showPortfolioToast(error)
                                }
                            }
                        },
                        themeColor = safeText
                    )

                    SettingsToggleItem(
                        title = stringResource(R.string.settings_require_password_after_bio_fail),
                        subtitle = stringResource(R.string.settings_require_password_after_bio_fail_subtitle),
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
                            text = stringResource(R.string.settings_login_timeout),
                            color = safeText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(
                                R.string.settings_current_timeout,
                                formatTimeoutLabel(loginResumeTimeoutSeconds)
                            ),
                            color = safeText.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (loginResumeTimeoutSeconds < 0) {
                                stringResource(R.string.settings_timeout_never_desc)
                            } else {
                                stringResource(
                                    R.string.settings_timeout_bg_desc,
                                    formatTimeoutLabel(loginResumeTimeoutSeconds)
                                )
                            },
                            color = safeText.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(R.string.settings_timeout_applies_desc),
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
                    Text(stringResource(R.string.settings_interface), color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBugReportDialog = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_bug_report_button),
                            color = safeText,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_bug_report_hint),
                        color = safeText.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )

                    SettingsToggleItem(
                        title = stringResource(R.string.settings_compact_cards),
                        subtitle = stringResource(R.string.settings_compact_cards_subtitle),
                        checked = isCompactMode,
                        onCheckedChange = { settingsViewModel.saveIsCompactViewEnabled(it) },
                        themeColor = safeText
                    )

                    // Syncs to ThemePreferences + MainViewModel; holdings/widget previews read the same flow.
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_high_visibility_cards),
                        subtitle = stringResource(R.string.settings_high_visibility_cards_subtitle),
                        checked = isHighVisibilityMode,
                        onCheckedChange = { settingsViewModel.saveIsHighVisibilityMode(it) },
                        themeColor = safeText
                    )

                    SettingsToggleItem(
                        title = stringResource(R.string.settings_confirm_deletion),
                        subtitle = stringResource(R.string.settings_confirm_deletion_subtitle),
                        checked = confirmDelete,
                        onCheckedChange = { mainViewModel.setConfirmDelete(it) },
                        themeColor = safeText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- THEME & DEPTH ---
                    Text(stringResource(R.string.settings_theme_depth), color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    SettingsToggleItem(
                        title = stringResource(R.string.settings_background_gradient),
                        subtitle = stringResource(R.string.settings_background_gradient_subtitle),
                        checked = useGradient,
                        onCheckedChange = { mainViewModel.setUseGradient(it) },
                        themeColor = safeText
                    )

                    if (useGradient) {
                        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
                            Text(text = stringResource(R.string.settings_gradient_intensity), color = safeText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                    Text(stringResource(R.string.settings_management), color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Button(
                        onClick = {
                            isExiting = true
                            navController.navigate(Routes.UPGRADE_TO_PRO)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.settings_upgrade_to_pro_now),
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isExiting = true
                            navController.navigate(Routes.PORTFOLIO_MANAGER)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.settings_portfolio_manager), color = safeText, fontWeight = FontWeight.Bold)
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
                        Text(stringResource(R.string.settings_theme_manager), color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            isExiting = true
                            navController.navigate(Routes.BACKUP_RESTORE)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.settings_backup_restore_nav), color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(stringResource(R.string.settings_legal_section), color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Button(
                        onClick = {
                            isExiting = true
                            navController.navigate(Routes.ABOUT)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(stringResource(R.string.settings_nav_about), color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // --- SYSTEM ACTIONS ---
                    Text(stringResource(R.string.settings_system_actions), color = safeText.copy(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Button(
                        onClick = { showFactoryResetFirstDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.settings_factory_default), color = safeText, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        if (showFactoryResetProgress) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(4f)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = safeBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.settings_factory_default),
                            color = safeText,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { factoryResetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = Color(0xFFFFD54F),
                            trackColor = safeText.copy(alpha = 0.15f),
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = factoryResetStatus.ifBlank {
                                stringResource(R.string.settings_factory_reset_progress_database)
                            },
                            color = safeText.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
