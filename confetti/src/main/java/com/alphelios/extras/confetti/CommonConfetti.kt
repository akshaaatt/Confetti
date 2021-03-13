package com.alphelios.extras.confetti

import android.graphics.Rect
import android.view.ViewGroup
import com.github.jinatonic.confetti.R
import com.alphelios.extras.confetti.confetto.BitmapConfetto
import com.alphelios.extras.confetti.confetto.Confetto
import java.util.*

class CommonConfetti private constructor(container: ViewGroup) {
    // endregion
    var confettiManager: ConfettiManager? = null

    /**
     * Starts a one-shot animation that emits all of the confetti at once.
     *
     * @return the resulting [ConfettiManager] that's performing the animation.
     */
    fun oneShot(): ConfettiManager {
        return confettiManager!!.setNumInitialCount(100)
                .setEmissionDuration(0)
                .animate()
    }

    /**
     * Starts a stream of confetti that animates for the provided duration.
     *
     * @param durationInMillis how long to animate the confetti for.
     * @return the resulting [ConfettiManager] that's performing the animation.
     */
    fun stream(durationInMillis: Long): ConfettiManager {
        return confettiManager!!.setNumInitialCount(0)
                .setEmissionDuration(durationInMillis)
                .setEmissionRate(50f)
                .animate()
    }

    /**
     * Starts an infinite stream of confetti.
     *
     * @return the resulting [ConfettiManager] that's performing the animation.
     */
    fun infinite(): ConfettiManager {
        return confettiManager!!.setNumInitialCount(0)
                .setEmissionDuration(ConfettiManager.INFINITE_DURATION)
                .setEmissionRate(50f)
                .animate()
    }

    private fun getDefaultGenerator(colors: IntArray): ConfettoGenerator {
        val bitmaps = Utils.generateConfettiBitmaps(colors, defaultConfettiSize)
        val numBitmaps = bitmaps.size
        return object : ConfettoGenerator {
            override fun generateConfetto(random: Random?): Confetto {
                return BitmapConfetto(bitmaps[random!!.nextInt(numBitmaps)])
            }
        }
    }

    private fun configureRainingConfetti(container: ViewGroup, confettiSource: ConfettiSource,
                                         colors: IntArray) {
        val context = container.context
        val generator = getDefaultGenerator(colors)
        confettiManager = ConfettiManager(context, generator, confettiSource, container)
                .setVelocityX(0f, defaultVelocitySlow.toFloat())
                .setVelocityY(defaultVelocityNormal.toFloat(), defaultVelocitySlow.toFloat())
                .setInitialRotation(180, 180)
                .setRotationalAcceleration(360f, 180f)
                .setTargetRotationalVelocity(360f)
    }

    private fun configureExplosion(container: ViewGroup, x: Int, y: Int, colors: IntArray) {
        val context = container.context
        val generator = getDefaultGenerator(colors)
        val confettiSource = ConfettiSource(x, y)
        confettiManager = ConfettiManager(context, generator, confettiSource, container)
                .setTTL(1000)
                .setBound(Rect(
                        x - explosionRadius, y - explosionRadius,
                        x + explosionRadius, y + explosionRadius
                ))
                .setVelocityX(0f, defaultVelocityFast.toFloat())
                .setVelocityY(0f, defaultVelocityFast.toFloat())
                .enableFadeOut(Utils.defaultAlphaInterpolator)
                .setInitialRotation(180, 180)
                .setRotationalAcceleration(360f, 180f)
                .setTargetRotationalVelocity(360f)
    }

    companion object {
        private var defaultConfettiSize = 0
        private var defaultVelocitySlow = 0
        private var defaultVelocityNormal = 0
        private var defaultVelocityFast = 0
        private var explosionRadius = 0
        // region Pre-configured confetti animations
        /**
         * @see .rainingConfetti
         * @param container the container viewgroup to host the confetti animation.
         * @param colors the set of colors to colorize the confetti bitmaps.
         * @return the created common confetti object.
         */
        fun rainingConfetti(container: ViewGroup, colors: IntArray): CommonConfetti {
            val commonConfetti = CommonConfetti(container)
            val confettiSource = ConfettiSource(0, -defaultConfettiSize,
                    container.width, -defaultConfettiSize)
            commonConfetti.configureRainingConfetti(container, confettiSource, colors)
            return commonConfetti
        }

        /**
         * Configures a confetti manager that has confetti falling from the provided confetti source.
         *
         * @param container the container viewgroup to host the confetti animation.
         * @param confettiSource the source of the confetti animation.
         * @param colors the set of colors to colorize the confetti bitmaps.
         * @return the created common confetti object.
         */
        @JvmStatic
        fun rainingConfetti(container: ViewGroup,
                            confettiSource: ConfettiSource, colors: IntArray): CommonConfetti {
            val commonConfetti = CommonConfetti(container)
            commonConfetti.configureRainingConfetti(container, confettiSource, colors)
            return commonConfetti
        }

        /**
         * Configures a confetti manager that has confetti exploding out in all directions from the
         * provided x and y coordinates.
         *
         * @param container the container viewgroup to host the confetti animation.
         * @param x the x coordinate of the explosion source.
         * @param y the y coordinate of the explosion source.
         * @param colors the set of colors to colorize the confetti bitmaps.
         * @return the created common confetti object.
         */
        @JvmStatic
        fun explosion(container: ViewGroup, x: Int, y: Int, colors: IntArray): CommonConfetti {
            val commonConfetti = CommonConfetti(container)
            commonConfetti.configureExplosion(container, x, y, colors)
            return commonConfetti
        }

        private fun ensureStaticResources(container: ViewGroup) {
            if (defaultConfettiSize == 0) {
                val res = container.resources
                defaultConfettiSize = res.getDimensionPixelSize(R.dimen.default_confetti_size)
                defaultVelocitySlow = res.getDimensionPixelOffset(R.dimen.default_velocity_slow)
                defaultVelocityNormal = res.getDimensionPixelOffset(R.dimen.default_velocity_normal)
                defaultVelocityFast = res.getDimensionPixelOffset(R.dimen.default_velocity_fast)
                explosionRadius = res.getDimensionPixelOffset(R.dimen.default_explosion_radius)
            }
        }
    }

    init {
        ensureStaticResources(container)
    }
}