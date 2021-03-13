package com.alphelios.extras.confetti

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.animation.Interpolator
import java.util.*

object Utils {
    private val PAINT = Paint()
    var defaultAlphaInterpolator: Interpolator? = null
        get() {
            if (field == null) {
                field = Interpolator { v -> if (v >= 0.9f) 1f - (v - 0.9f) * 10f else 1f }
            }
            return field
        }
        private set

    @JvmStatic
    fun generateConfettiBitmaps(colors: IntArray, size: Int): List<Bitmap> {
        val bitmaps: MutableList<Bitmap> = ArrayList()
        for (color in colors) {
            bitmaps.add(createCircleBitmap(color, size))
            bitmaps.add(createSquareBitmap(color, size))
            bitmaps.add(createTriangleBitmap(color, size))
        }
        return bitmaps
    }

    fun createCircleBitmap(color: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        PAINT.color = color
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, PAINT)
        return bitmap
    }

    fun createSquareBitmap(color: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        PAINT.color = color
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(size.toFloat(), 0f)
        path.lineTo(size.toFloat(), size.toFloat())
        path.lineTo(0f, size.toFloat())
        path.close()
        canvas.drawPath(path, PAINT)
        return bitmap
    }

    fun createTriangleBitmap(color: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        PAINT.color = color

        // Generate equilateral triangle (http://mathworld.wolfram.com/EquilateralTriangle.html).
        val path = Path()
        val point = Math.tan(15f / 180f * Math.PI).toFloat() * size
        path.moveTo(0f, 0f)
        path.lineTo(size.toFloat(), point)
        path.lineTo(point, size.toFloat())
        path.close()
        canvas.drawPath(path, PAINT)
        return bitmap
    }

    init {
        PAINT.style = Paint.Style.FILL
    }
}