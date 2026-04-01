@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    // Standard Swanie Theme logic would go here
    val siteBg = Color(0xFF000416)
    val siteText = Color(0xFFFFFFFF)
    val accentGold = Color(0xFFFFD700)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PRIVACY & TERMS", fontSize = 16.sp, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = siteText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = siteBg, titleContentColor = siteText)
            )
        },
        containerColor = siteBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TermSection("1. ZERO-KNOWLEDGE ARCHITECTURE",
                "We do not store your data. Period. Swanie's Portfolio is a bridge between your assets and your private Google Drive. We have no servers and no access to your vault.")

            TermSection("2. DATA RECOVERY LIMITATIONS",
                "Because of our encryption model, the creator CANNOT recover your password. If you lose your 'Vault Password' and your 'Recovery Hint,' your data is gone. This is the cost of absolute privacy.")

            TermSection("3. GOOGLE DRIVE INTEGRATION",
                "The app utilizes a hidden 'App Data' folder on your personal Google Drive. You grant the app permission to read/write only to this specific folder. We cannot see your personal photos or documents.")

            TermSection("4. SUBSCRIPTION SURVIVABILITY",
                "Lifetime and monthly subscriptions are tied to your Google Play Identity. While your asset data is private, your 'Pro Status' is recoverable via any device signed into your Google Account.")

            TermSection("5. NO FINANCIAL ADVICE",
                "This app is a tracking tool. Price data is pulled from third-party APIs (CoinGecko, etc). We are not responsible for market fluctuations or the accuracy of external data providers.")

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Last Updated: April 2026",
                color = siteText.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TermSection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(text = title, color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = body, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, lineHeight = 20.sp)
    }
}