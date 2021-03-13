package com.alphelios.extras.confetti.confetto

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

open class BitmapConfetto(private val bitmap: Bitmap) : Confetto() {
    private val bitmapCenterX: Float = bitmap.width / 2f
    private val bitmapCenterY: Float = bitmap.height / 2f
    override val width: Int
        get() = bitmap.width
    override val height: Int
        get() = bitmap.height

    override fun drawInternal(canvas: Canvas, matrix: Matrix, paint: Paint, x: Float, y: Float,
                              rotation: Float, percentageAnimated: Float) {
        matrix.preTranslate(x, y)
        matrix.preRotate(rotation, bitmapCenterX, bitmapCenterY)
        canvas.drawBitmap(bitmap, matrix, paint)
    }

}