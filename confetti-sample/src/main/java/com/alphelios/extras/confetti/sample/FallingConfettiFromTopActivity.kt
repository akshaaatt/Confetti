package com.alphelios.extras.confetti.sample

import com.alphelios.extras.confetti.CommonConfetti.Companion.rainingConfetti
import com.alphelios.extras.confetti.ConfettiManager

class FallingConfettiFromTopActivity : AbstractActivity() {
    override fun generateOnce(): ConfettiManager {
        return rainingConfetti(container!!, colors)
                .oneShot()
    }

    override fun generateStream(): ConfettiManager {
        return rainingConfetti(container!!, colors)
                .stream(3000)
    }

    override fun generateInfinite(): ConfettiManager {
        return rainingConfetti(container!!, colors)
                .infinite()
    }
}