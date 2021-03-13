package com.alphelios.extras.confetti.confetto

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

/**
 * A lightly more optimal way to draw a circle shape that doesn't require the use of a bitmap.
 */
class CircleConfetto(private val color: Int, private val radius: Float, override val width: Int) : Confetto() {
    override val height: Int = (radius * 2).toInt()
    override fun configurePaint(paint: Paint) {
        super.configurePaint(paint)
        paint.style = Paint.Style.FILL
        paint.color = color
    }

    override fun drawInternal(canvas: Canvas, matrix: Matrix, paint: Paint, x: Float, y: Float,
                              rotation: Float, percentageAnimated: Float) {
        canvas.drawCircle(x + radius, y + radius, radius, paint)
    }

}