@file:OptIn(ExperimentalMaterial3Api::class)

package com.swanie.portfolio.ui.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.swanie.portfolio.BuildConfig
import com.swanie.portfolio.R
import com.swanie.portfolio.ui.navigation.Routes

@Composable
fun AboutScreen(navController: NavController) {
    val siteBg = Color(0xFF000416)
    val siteText = Color(0xFFFFFFFF)
    val accent = Color(0xFFFFD700)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.about_title), fontSize = 16.sp, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_back), tint = siteText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = siteBg, titleContentColor = siteText),
            )
        },
        containerColor = siteBg,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                color = accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.about_intro),
                color = siteText.copy(alpha = 0.78f),
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.about_legal_header),
                color = accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate(Routes.TERMS_CONDITIONS) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.about_open_privacy_terms), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
