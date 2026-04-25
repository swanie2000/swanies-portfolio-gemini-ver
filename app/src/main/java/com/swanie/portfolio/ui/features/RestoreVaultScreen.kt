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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.swanie.portfolio.ui.navigation.Routes

/**
 * RestoreVaultScreen - Sovereign Edition
 * Simplified to remove legacy Google Auth dependencies for V19 PRO REBUILD.
 */
@Composable
fun RestoreVaultScreen(
    navController: NavHostController
) {
    val activity = LocalContext.current as androidx.fragment.app.FragmentActivity
    val authViewModel: AuthViewModel = hiltViewModel(activity)
    val authState by authViewModel.authState.collectAsState()

    // --- Temporary Placeholders for Build Stability ---
    val isRestoring = false
    val error: String? = null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000416)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = Color(0xFFFFD700), // Swanie Gold
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "VAULT RECOVERY",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "Cloud restoration is being optimized for the Sovereign Shield. Please start with a fresh vault or setup your local security.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Button(
                onClick = {
                    authViewModel.setAuthenticated()
                    navController.navigate(Routes.HOLDINGS) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("START FRESH VAULT", fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("BACK TO START", color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}