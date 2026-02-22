package com.swanie.portfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun SaturationValueBox(
    hue: Float,
    saturation: Float,
    value: Float,
    onSatValChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val boxWidth = constraints.maxWidth.toFloat()
        val boxHeight = constraints.maxHeight.toFloat()

        val satValBrush = Brush.horizontalGradient(listOf(Color.hsv(hue, 0f, 1f), Color.hsv(hue, 1f, 1f)))
        val valueBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))

        Spacer(modifier = Modifier.fillMaxSize().background(satValBrush))
        Spacer(modifier = Modifier.fillMaxSize().background(valueBrush))

        Box(
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures {
                    change, _ ->
                    val x = change.position.x.coerceIn(0f, boxWidth)
                    val y = change.position.y.coerceIn(0f, boxHeight)
                    onSatValChanged(x / boxWidth, 1f - (y / boxHeight))
                }
            }
        )

        val selectorX = saturation * boxWidth
        val selectorY = (1f - value) * boxHeight
        ColorSelector(Offset(selectorX, selectorY))
    }
}

@Composable
fun HueSlider(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val boxWidth = constraints.maxWidth.toFloat()
        val hueColors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
        val hueBrush = Brush.horizontalGradient(hueColors)

        Spacer(modifier = Modifier.fillMaxSize().background(hueBrush))

        Box(
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures {
                    change, _ ->
                    val x = change.position.x.coerceIn(0f, boxWidth)
                    onHueChanged((x / boxWidth) * 360f)
                }
            }
        )

        val selectorX = (hue / 360f) * boxWidth
        ColorSelector(Offset(selectorX, constraints.maxHeight.toFloat() / 2f))
    }
}

@Composable
private fun ColorSelector(offset: Offset) {
    val offsetXDp = with(LocalDensity.current) { offset.x.toDp() }
    val offsetYDp = with(LocalDensity.current) { offset.y.toDp() }

    Box(
        modifier = Modifier
            .offset(x = offsetXDp - 12.dp, y = offsetYDp - 12.dp)
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color.Black, CircleShape)
    )
}

fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv
}
