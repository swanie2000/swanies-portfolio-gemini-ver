package com.swanie.portfolio.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProLockBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    var badgeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        badgeVisible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (badgeVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "proLockBadgeAlpha"
    )

    Row(
        modifier = modifier.alpha(alpha),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = ProPalette.Accent
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = ProPalette.Accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}
