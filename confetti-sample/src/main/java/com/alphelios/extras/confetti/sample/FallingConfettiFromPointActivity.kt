package com.alphelios.extras.confetti.sample

import com.alphelios.extras.confetti.CommonConfetti
import com.alphelios.extras.confetti.CommonConfetti.Companion.rainingConfetti
import com.alphelios.extras.confetti.ConfettiManager
import com.alphelios.extras.confetti.ConfettiSource
import com.github.jinatonic.confetti.sample.R

class FallingConfettiFromPointActivity : AbstractActivity() {
    override fun generateOnce(): ConfettiManager {
        return commonConfetti.oneShot()
    }

    override fun generateStream(): ConfettiManager {
        return commonConfetti.stream(3000)
    }

    override fun generateInfinite(): ConfettiManager {
        return commonConfetti.infinite()
    }

    // Further configure it
    private val commonConfetti: CommonConfetti
        get() {
            val size = resources.getDimensionPixelSize(R.dimen.default_confetti_size)
            val confettiSource = ConfettiSource(-size, -size)
            val commonConfetti = rainingConfetti(container!!, confettiSource, colors)
            val res = resources
            val velocitySlow = res.getDimensionPixelOffset(R.dimen.default_velocity_slow)
            val velocityNormal = res.getDimensionPixelOffset(R.dimen.default_velocity_normal)
            val velocityFast = res.getDimensionPixelOffset(R.dimen.default_velocity_fast)

            // Further configure it
            commonConfetti.confettiManager!!
                    .setVelocityX(velocityFast.toFloat(), velocityNormal.toFloat())
                    .setAccelerationX(-velocityNormal.toFloat(), velocitySlow.toFloat())
                    .setTargetVelocityX(0f, velocitySlow / 2f)
                    .setVelocityY(velocityNormal.toFloat(), velocitySlow.toFloat())
            return commonConfetti
        }
}