package com.swanie.portfolio.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.swanie.portfolio.R
import kotlin.math.roundToInt

/** Rasterize drawable to a square bitmap for Compose. */
private fun rasterizeDrawable(context: Context, @DrawableRes resId: Int, sizePx: Int): Bitmap {
    val px = sizePx.coerceIn(48, 512)
    val drawable = ContextCompat.getDrawable(context, resId)?.mutate()
    if (drawable == null) {
        return Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    }
    drawable.setBounds(0, 0, px, px)
    val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)
    return bitmap
}

@Composable
fun CustomToast(message: String) {
    val appContext = LocalContext.current.applicationContext
    val density = LocalDensity.current.density
    val slotDp = 18f
    val iconBitmap = remember(density) {
        val px = (slotDp * density).roundToInt().coerceAtLeast(48)
        rasterizeDrawable(appContext, R.drawable.ic_toast_swan, px)
    }
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded rect clips less aggressively than a circle on a wide swan raster.
        Image(
            bitmap = iconBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = message, color = Color.White)
    }
}
