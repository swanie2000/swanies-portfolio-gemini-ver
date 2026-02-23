package com.swanie.portfolio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swanie.portfolio.R

@Composable
fun CustomToast(message: String) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Parent container for the icon
        Box(
            modifier = Modifier.size(28.dp), // Total size for the icon area
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.swan_launcher_icon),
                contentDescription = "Swan Icon",
                contentScale = ContentScale.Fit, // Ensure the icon fits without cropping
                modifier = Modifier.padding(end = 8.dp) // Added end padding
            )
        }
        Text(text = message, color = Color.White, modifier = Modifier.padding(start = 8.dp))
    }
}
