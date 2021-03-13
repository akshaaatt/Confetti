package com.alphelios.extras.confetti.sample

import com.alphelios.extras.confetti.CommonConfetti
import com.alphelios.extras.confetti.CommonConfetti.Companion.explosion
import com.alphelios.extras.confetti.ConfettiManager

class ExplosionActivity : AbstractActivity() {
    override fun generateOnce(): ConfettiManager {
        return commonConfetti.oneShot()
    }

    override fun generateStream(): ConfettiManager {
        return commonConfetti.stream(3000)
    }

    override fun generateInfinite(): ConfettiManager {
        return commonConfetti.infinite()
    }

    private val commonConfetti: CommonConfetti
        get() {
            val centerX = container!!.width / 2
            val centerY = container!!.height / 5 * 2
            return explosion(container!!, centerX, centerY, colors)
        }
}