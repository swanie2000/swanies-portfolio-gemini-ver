package com.swanie.portfolio.ui.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val isDarkMode = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row {
            Text(text = "Dark Mode")
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isDarkMode.value,
                onCheckedChange = { isDarkMode.value = it }
            )
        }
        Text(text = "Theme Selection (Placeholder)")
    }
}