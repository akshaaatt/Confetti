package com.alphelios.extras.confetti

import com.alphelios.extras.confetti.confetto.Confetto
import java.util.*

interface ConfettoGenerator {
    /**
     * Generate a random confetto to animate.
     *
     * @param random a [Random] that can be used to generate random confetto.
     * @return the randomly generated confetto.
     */
    fun generateConfetto(random: Random?): Confetto?
}