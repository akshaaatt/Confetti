package com.alphelios.extras.confetti.confetto

import android.animation.ArgbEvaluator
import android.graphics.*
import android.os.SystemClock
import java.util.*
import kotlin.math.abs

class ShimmeringConfetto(bitmap: Bitmap, private val fromColor: Int, private val toColor: Int, private val waveLength: Long,
                         random: Random) : BitmapConfetto(bitmap) {
    private val evaluator = ArgbEvaluator()
    private val halfWaveLength: Long = waveLength / 2
    private val randomStart: Long
    override fun drawInternal(canvas: Canvas, matrix: Matrix, paint: Paint, x: Float, y: Float,
                              rotation: Float, percentageAnimated: Float) {
        val currTime = SystemClock.elapsedRealtime()
        val fraction = (currTime - randomStart) % waveLength
        val animated = if (fraction < halfWaveLength) fraction.toFloat() / halfWaveLength else (waveLength.toFloat() - fraction) / halfWaveLength
        val color = evaluator.evaluate(animated, fromColor, toColor) as Int
        val colorFilter: ColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        paint.colorFilter = colorFilter
        super.drawInternal(canvas, matrix, paint, x, y, rotation, percentageAnimated)
    }

    init {
        val currentTime = abs(SystemClock.elapsedRealtime().toInt())
        randomStart = (currentTime - random.nextInt(currentTime)).toLong()
    }
}