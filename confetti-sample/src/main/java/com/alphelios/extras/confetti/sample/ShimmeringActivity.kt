package com.alphelios.extras.confetti.sample

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import com.alphelios.extras.confetti.ConfettiManager
import com.alphelios.extras.confetti.ConfettiSource
import com.alphelios.extras.confetti.ConfettoGenerator
import com.alphelios.extras.confetti.Utils.generateConfettiBitmaps
import com.alphelios.extras.confetti.confetto.Confetto
import com.alphelios.extras.confetti.confetto.ShimmeringConfetto
import com.github.jinatonic.confetti.sample.R
import java.util.*

class ShimmeringActivity : AbstractActivity(), ConfettoGenerator {
    private var size = 0
    private var velocitySlow = 0
    private var velocityNormal = 0
    private var confettoBitmaps: List<Bitmap>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val res = resources
        size = res.getDimensionPixelSize(R.dimen.default_confetti_size)
        velocitySlow = res.getDimensionPixelOffset(R.dimen.default_velocity_slow)
        velocityNormal = res.getDimensionPixelOffset(R.dimen.default_velocity_normal)

        // The color here doesn't matter, it's simply needed to generate the bitmaps
        val colors = intArrayOf(Color.BLACK)
        confettoBitmaps = generateConfettiBitmaps(colors, size)
    }

    override fun generateOnce(): ConfettiManager {
        return confettiManager.setNumInitialCount(100)
                .setEmissionDuration(0)
                .animate()
    }

    override fun generateStream(): ConfettiManager {
        return confettiManager.setNumInitialCount(0)
                .setEmissionDuration(3000)
                .setEmissionRate(50f)
                .animate()
    }

    override fun generateInfinite(): ConfettiManager {
        return confettiManager.setNumInitialCount(0)
                .setEmissionDuration(ConfettiManager.INFINITE_DURATION)
                .setEmissionRate(50f)
                .animate()
    }

    private val confettiManager: ConfettiManager
        get() {
            val confettiSource = ConfettiSource(0, -size, container!!.width,
                    -size)
            return ConfettiManager(this, this, confettiSource, container!!)
                    .setVelocityX(0f, velocitySlow.toFloat())
                    .setVelocityY(velocityNormal.toFloat(), velocitySlow.toFloat())
                    .setInitialRotation(180, 180)
                    .setRotationalAcceleration(360f, 180f)
                    .setTargetRotationalVelocity(360f)
        }

    override fun generateConfetto(random: Random?): Confetto {
        return ShimmeringConfetto(
                confettoBitmaps!![random!!.nextInt(confettoBitmaps!!.size)],
                goldLight, goldDark, 1000, random)
    }
}