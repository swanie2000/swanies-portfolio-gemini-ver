package com.swanie.portfolio.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.ui.navigation.Routes

/**
 * RestoreVaultScreen
 * Final gate for Sovereign Vault recovery.
 * Matches the UI Perfection aesthetic of V7.4.3.
 */
@Composable
fun RestoreVaultScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel() // Reference is now local to this package
) {
    val authState by authViewModel.authState.collectAsState()
    val isRestoring by authViewModel.isRestoring.collectAsState()
    val error by authViewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = Color.Cyan,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SOVEREIGN VAULT DETECTED",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "We found a backup on your Google Drive. Restore your holdings to this device?",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    authViewModel.restoreVaultFromCloud {
                        navController.navigate(Routes.HOLDINGS) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                shape = RoundedCornerShape(12.dp),
                enabled = !isRestoring
            ) {
                if (isRestoring) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("RESTORE HOLDINGS", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            TextButton(
                onClick = {
                    navController.navigate(Routes.HOLDINGS) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("SKIP AND START EMPTY", color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}