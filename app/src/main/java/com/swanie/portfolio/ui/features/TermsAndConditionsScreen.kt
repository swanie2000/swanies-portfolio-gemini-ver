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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.swanie.portfolio.R

@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    // Standard Swanie Theme logic would go here
    val siteBg = Color(0xFF000416)
    val siteText = Color(0xFFFFFFFF)
    val accentGold = Color(0xFFFFD700)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.terms_title), fontSize = 16.sp, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_back), tint = siteText)
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
            TermSection(
                stringResource(R.string.terms_section_1_title),
                stringResource(R.string.terms_section_1_body)
            )

            TermSection(
                stringResource(R.string.terms_section_2_title),
                stringResource(R.string.terms_section_2_body)
            )

            TermSection(
                stringResource(R.string.terms_section_3_title),
                stringResource(R.string.terms_section_3_body)
            )

            TermSection(
                stringResource(R.string.terms_section_4_title),
                stringResource(R.string.terms_section_4_body)
            )

            TermSection(
                stringResource(R.string.terms_section_5_title),
                stringResource(R.string.terms_section_5_body)
            )

            TermSection(
                stringResource(R.string.terms_section_6_title),
                stringResource(R.string.terms_section_6_body)
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = stringResource(R.string.terms_last_updated),
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