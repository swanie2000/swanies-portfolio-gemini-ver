package com.swanie.portfolio.ui.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.R
import com.swanie.portfolio.data.backup.VaultBackupEngine
import com.swanie.portfolio.ui.holdings.AutoResizingText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val cardBgColor by themeViewModel.cardBackgroundColor.collectAsState()
    val siteTextColor by themeViewModel.siteTextColor.collectAsState()
    val dialogBg = Color(cardBgColor.ifBlank { "#121212" }.toColorInt())
    val safeText = Color(siteTextColor.ifBlank { "#FFFFFF" }.toColorInt())

    var pendingExportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showBackupExportPassphraseDialog by remember { mutableStateOf(false) }
    var showBackupImportPassphraseDialog by remember { mutableStateOf(false) }
    var backupPassphraseField by remember { mutableStateOf("") }
    var backupPassphraseVisible by remember { mutableStateOf(false) }
    var backupOperationBusy by remember { mutableStateOf(false) }
    var exportPickerAwaitingResult by remember { mutableStateOf(false) }
    var importPickerAwaitingResult by remember { mutableStateOf(false) }

    val exportDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        exportPickerAwaitingResult = false
        if (uri != null) {
            pendingExportUri = uri
            backupPassphraseField = ""
            backupPassphraseVisible = false
            showBackupExportPassphraseDialog = true
        }
    }

    val importDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        importPickerAwaitingResult = false
        if (uri != null) {
            pendingImportUri = uri
            backupPassphraseField = ""
            backupPassphraseVisible = false
            showBackupImportPassphraseDialog = true
        }
    }

    if (showBackupExportPassphraseDialog && pendingExportUri != null) {
        VaultBackupPassphraseDialog(
            title = stringResource(R.string.settings_backup_passphrase_title_export),
            subtitle = null,
            confirmText = if (backupOperationBusy) {
                stringResource(R.string.settings_updating)
            } else {
                stringResource(R.string.settings_backup_confirm_export)
            },
            safeText = safeText,
            dialogBg = dialogBg,
            passphrase = backupPassphraseField,
            onPassphraseChange = { new ->
                backupPassphraseField = new.filter { ch -> ch != '\n' && ch != '\r' }
            },
            passphraseVisible = backupPassphraseVisible,
            onTogglePassphraseVisible = { backupPassphraseVisible = !backupPassphraseVisible },
            operationBusy = backupOperationBusy,
            onDismiss = {
                showBackupExportPassphraseDialog = false
                pendingExportUri = null
                backupPassphraseField = ""
            },
            onConfirm = exportConfirm@{
                val trimmed = backupPassphraseField.trim()
                if (trimmed.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.settings_backup_error_empty_passphrase), Toast.LENGTH_SHORT).show()
                    return@exportConfirm
                }
                val uri = pendingExportUri ?: return@exportConfirm
                val pass = trimmed.toCharArray()
                backupOperationBusy = true
                settingsViewModel.exportVaultBackup(uri, pass) { result ->
                    backupOperationBusy = false
                    showBackupExportPassphraseDialog = false
                    pendingExportUri = null
                    backupPassphraseField = ""
                    result.fold(
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.settings_backup_export_ok), Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(
                                context,
                                it.message ?: context.getString(R.string.settings_backup_error_generic),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            },
        )
    }

    if (showBackupImportPassphraseDialog && pendingImportUri != null) {
        VaultBackupPassphraseDialog(
            title = stringResource(R.string.settings_backup_passphrase_title_import),
            subtitle = stringResource(R.string.settings_backup_import_subtitle),
            confirmText = if (backupOperationBusy) {
                stringResource(R.string.settings_updating)
            } else {
                stringResource(R.string.settings_backup_confirm_import)
            },
            safeText = safeText,
            dialogBg = dialogBg,
            passphrase = backupPassphraseField,
            onPassphraseChange = { new ->
                backupPassphraseField = new.filter { ch -> ch != '\n' && ch != '\r' }
            },
            passphraseVisible = backupPassphraseVisible,
            onTogglePassphraseVisible = { backupPassphraseVisible = !backupPassphraseVisible },
            operationBusy = backupOperationBusy,
            onDismiss = {
                showBackupImportPassphraseDialog = false
                pendingImportUri = null
                backupPassphraseField = ""
            },
            onConfirm = restoreConfirm@{
                val trimmed = backupPassphraseField.trim()
                if (trimmed.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.settings_backup_error_empty_passphrase), Toast.LENGTH_SHORT).show()
                    return@restoreConfirm
                }
                val uri = pendingImportUri ?: return@restoreConfirm
                val pass = trimmed.toCharArray()
                backupOperationBusy = true
                settingsViewModel.importVaultBackup(uri, pass) { result ->
                    backupOperationBusy = false
                    result.fold(
                        onSuccess = {
                            showBackupImportPassphraseDialog = false
                            pendingImportUri = null
                            backupPassphraseField = ""
                            Toast.makeText(context, context.getString(R.string.settings_backup_import_ok), Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { err ->
                            val wrongPass = err.message == VaultBackupEngine.WRONG_BACKUP_PASSPHRASE_MARKER
                            if (!wrongPass) {
                                showBackupImportPassphraseDialog = false
                                pendingImportUri = null
                            }
                            backupPassphraseField = ""
                            Toast.makeText(
                                context,
                                backupImportFailureMessage(err, context),
                                Toast.LENGTH_LONG
                            ).show()
                        },
                    )
                }
            },
        )
    }

    val buttonLabelStyle = remember(siteTextColor, safeText) {
        TextStyle(
            color = safeText,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = safeText
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.swanie_foreground),
                        contentDescription = null,
                        modifier = Modifier.height(72.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.settings_backup_screen_title),
                        color = safeText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Text(
                    text = stringResource(R.string.settings_backup_screen_intro),
                    color = safeText.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Text(
                    text = stringResource(R.string.settings_backup_export_subtitle),
                    color = safeText.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = {
                        if (exportPickerAwaitingResult || backupOperationBusy) return@Button
                        exportPickerAwaitingResult = true
                        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                        exportDocLauncher.launch("swanie_vault_backup_$stamp.swpb")
                    },
                    enabled = !exportPickerAwaitingResult && !backupOperationBusy,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    AutoResizingText(
                        text = stringResource(R.string.settings_backup_export),
                        style = buttonLabelStyle,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        minFontSize = 8.sp,
                        maxFontSize = 15.sp,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.settings_backup_import_subtitle),
                    color = safeText.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = {
                        if (importPickerAwaitingResult || backupOperationBusy) return@Button
                        importPickerAwaitingResult = true
                        importDocLauncher.launch(arrayOf("*/*"))
                    },
                    enabled = !importPickerAwaitingResult && !backupOperationBusy,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = safeText.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    AutoResizingText(
                        text = stringResource(R.string.settings_backup_import),
                        style = buttonLabelStyle,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        minFontSize = 8.sp,
                        maxFontSize = 15.sp,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private fun backupImportFailureMessage(t: Throwable, context: Context): String =
    if (t.message == VaultBackupEngine.WRONG_BACKUP_PASSPHRASE_MARKER) {
        context.getString(R.string.settings_backup_error_wrong_passphrase)
    } else {
        t.message ?: context.getString(R.string.settings_backup_error_generic)
    }

@Composable
private fun VaultBackupPassphraseDialog(
    title: String,
    subtitle: String?,
    confirmText: String,
    safeText: Color,
    dialogBg: Color,
    passphrase: String,
    onPassphraseChange: (String) -> Unit,
    passphraseVisible: Boolean,
    onTogglePassphraseVisible: () -> Unit,
    operationBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            if (!operationBusy) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val scroll = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(scroll),
                shape = RoundedCornerShape(24.dp),
                color = dialogBg,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                ) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        color = safeText,
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            color = safeText.copy(alpha = 0.75f),
                            lineHeight = 20.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passphrase,
                        onValueChange = onPassphraseChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 112.dp),
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        label = {
                            Text(
                                text = stringResource(R.string.settings_backup_passphrase_label),
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        singleLine = false,
                        enabled = !operationBusy,
                        visualTransformation = if (passphraseVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePassphraseVisible) {
                                Icon(
                                    imageVector = if (passphraseVisible) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
                                    contentDescription = null,
                                    tint = safeText,
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = safeText,
                            unfocusedTextColor = safeText,
                            focusedBorderColor = safeText.copy(alpha = 0.6f),
                            unfocusedBorderColor = safeText.copy(alpha = 0.3f),
                            cursorColor = safeText,
                            focusedLabelColor = safeText.copy(alpha = 0.8f),
                            unfocusedLabelColor = safeText.copy(alpha = 0.6f),
                            focusedTrailingIconColor = safeText,
                            unfocusedTrailingIconColor = safeText.copy(alpha = 0.75f),
                        ),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            enabled = !operationBusy,
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = safeText,
                                disabledContentColor = safeText.copy(alpha = 0.38f),
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.action_cancel),
                                fontWeight = FontWeight.SemiBold,
                                color = safeText,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = !operationBusy,
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = safeText,
                                contentColor = dialogBg,
                                disabledContainerColor = safeText.copy(alpha = 0.35f),
                                disabledContentColor = dialogBg.copy(alpha = 0.55f),
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                text = confirmText,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
