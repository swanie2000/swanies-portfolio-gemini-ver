package com.swanie.portfolio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swanie.portfolio.R

@Composable
fun AlphaKeyboard(
    onKeyClick: (String) -> Unit,
    onBackSpace: () -> Unit,
    onClear: () -> Unit
) {
    // QWERTY rows to match your keyboard_bg.jpg
    val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
    val row3 = listOf("Z", "X", "C", "V", "B", "N", "M")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.85f) // Matches the wide landscape orientation of your image
    ) {
        // 1. Your Premium Background Image
        Image(
            painter = painterResource(id = R.drawable.keyboard_bg),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )

        // 2. Invisible Touch Overlay
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Row for CLEAR and ENTER buttons in your image
            Row(modifier = Modifier.weight(1.2f).fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { onClear() })
                Spacer(modifier = Modifier.weight(1f)) // Gap between top buttons
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable { /* Enter Action */ })
            }

            // QWERTY Rows
            KeyboardRow(keys = row1, onKeyClick = onKeyClick, modifier = Modifier.weight(1f))
            KeyboardRow(keys = row2, onKeyClick = onKeyClick, modifier = Modifier.weight(1f))

            // Bottom Row (Z-M + Backspace)
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                row3.forEach { key ->
                    TransparentKey(onClick = { onKeyClick(key) }, modifier = Modifier.weight(1f))
                }
                // Matches the backspace/arrow icon on the far right of your image
                TransparentKey(onClick = onBackSpace, modifier = Modifier.weight(1.4f))
            }
        }
    }
}

@Composable
fun KeyboardRow(keys: List<String>, onKeyClick: (String) -> Unit, modifier: Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        keys.forEach { key ->
            TransparentKey(onClick = { onKeyClick(key) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TransparentKey(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                // Cyan ripple to match the neon aesthetic of your keyboard image
                indication = ripple(bounded = true, color = Color.Cyan.copy(alpha = 0.3f)),
                onClick = onClick
            )
    )
}