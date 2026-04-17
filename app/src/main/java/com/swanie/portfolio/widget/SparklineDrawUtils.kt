package com.swanie.portfolio.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object SparklineDrawUtils {

    /**
     * Draws a high-fidelity continuous path sparkline into a Bitmap.
     * Dimensions: 120px x 60px (equivalent to 60dp x 30dp @ 2x density)
     * 🛠️ V7.2.0 Optimization: Uses RGB_565 to reduce memory footprint by 50%.
     */
    fun drawSparklineBitmap(points: List<Double>, color: Color): Bitmap {
        val width = 120
        val height = 60
        // 🚀 BOUTIQUE UPGRADE: Use ARGB_8888 for transparency support
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Transparent background
        canvas.drawColor(android.graphics.Color.TRANSPARENT)

        if (points.size < 2) return bitmap

        val min = points.minOrNull() ?: 0.0
        val max = points.maxOrNull() ?: 1.0
        val range = if (max - min > 0) max - min else 1.0

        val paint = Paint().apply {
            this.color = color.toArgb()
            this.style = Paint.Style.STROKE
            this.strokeWidth = 2.5f // Surgical Sweet Spot
            this.isAntiAlias = true
            this.strokeCap = Paint.Cap.ROUND
            this.strokeJoin = Paint.Join.ROUND
        }

        val path = Path()
        val stepX = width.toFloat() / (points.size - 1)

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = ((value - min) / range).toFloat()
            val y = height - (normalizedY * (height - 8) + 4) // 4px padding top/bottom

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        canvas.drawPath(path, paint)
        return bitmap
    }
}
