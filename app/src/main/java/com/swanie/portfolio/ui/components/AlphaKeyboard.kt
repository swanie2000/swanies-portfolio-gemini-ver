package com.swanie.portfolio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swanie.portfolio.R

/**
 * A custom keyboard layout using a balanced, proportional weight system.
 * Each row's contents (keys + spacers) are designed to add up to a similar total weight,
 * ensuring that the touch targets scale correctly and align with the background image.
 */
@Composable
fun AlphaKeyboard(
    onKeyClick: (String) -> Unit,
    onBackSpace: () -> Unit,
    onClear: () -> Unit
) {
    val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P") // 10 keys
    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")   // 9 keys
    val row3Mid = listOf("X", "C", "V", "B", "N", "M")               // 6 keys

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.85f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.keyboard_bg),
            contentDescription = "Keyboard Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Container for all touch targets, with a vertical offset to align properly.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp) // Increased vertical offset
        ) {

            // Top Row (for CLEAR and ENTER buttons) - This row's weight is relative to the key rows
            Row(modifier = Modifier.weight(1.2f).fillMaxWidth()) {
                TransparentKey(onClick = onClear, modifier = Modifier.weight(1.5f))
                Spacer(modifier = Modifier.weight(7f))
                TransparentKey(onClick = { /* TODO: Enter action */ }, modifier = Modifier.weight(1.5f))
            }

            // Row 1: Q-P (10 keys). Total weight = 10 * 1f = 10f.
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                row1.forEach { key ->
                    TransparentKey(onClick = { onKeyClick(key) }, modifier = Modifier.weight(1f))
                }
            }

            // Row 2: A-L (9 keys). Centered with spacers.
            // Total weight = 0.5f (start) + 9*1f (keys) + 0.5f (end) = 10f.
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(0.5f))
                row2.forEach { key ->
                    TransparentKey(onClick = { onKeyClick(key) }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }

            // Row 3: Z-Backspace. Using the specific geometric rules provided.
            // Total weight = 1.2f (Z) + 6*1f (X-M) + 3f (Backspace) = 10.2f.
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                TransparentKey(onClick = { onKeyClick("Z") }, modifier = Modifier.weight(1.2f))
                row3Mid.forEach { key ->
                    TransparentKey(onClick = { onKeyClick(key) }, modifier = Modifier.weight(1f))
                }
                TransparentKey(onClick = onBackSpace, modifier = Modifier.weight(3f))
            }
        }
    }
}

@Composable
private fun TransparentKey(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.Cyan.copy(alpha = 0.3f)),
                onClick = onClick
            )
    )
}
