package com.alphelios.extras.confetti

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.view.animation.Interpolator
import com.alphelios.extras.confetti.ConfettiView.Companion.newInstance
import com.alphelios.extras.confetti.confetto.Confetto
import java.util.*

/**
 * A helper manager class for configuring a set of confetti and displaying them on the UI.
 */
class ConfettiManager(private val confettoGenerator: ConfettoGenerator,
                      private val confettiSource: ConfettiSource, private val parentView: ViewGroup, private val confettiView: ConfettiView) {
    private val random = Random()
    private val recycledConfetti: Queue<Confetto?> = LinkedList()
    private val confetti: MutableList<Confetto?> = ArrayList(300)
    private var animator: ValueAnimator? = null
    private var lastEmittedTimestamp: Long = 0

    // All of the below configured values are in milliseconds despite the setter methods take them
    // in seconds as the parameters. The parameters for the setters are in seconds to allow for
    // users to better understand/visualize the dimensions.
    // Configured attributes for the entire confetti group
    private var numInitialCount = 0
    private var emissionDuration: Long = 0
    private var emissionRate = 0f
    private var emissionRateInverse = 0f
    private var fadeOutInterpolator: Interpolator? = null
    private var bound: Rect

    // Configured attributes for each confetto
    private var velocityX = 0f
    private var velocityDeviationX = 0f
    private var velocityY = 0f
    private var velocityDeviationY = 0f
    private var accelerationX = 0f
    private var accelerationDeviationX = 0f
    private var accelerationY = 0f
    private var accelerationDeviationY = 0f
    private var targetVelocityX: Float? = null
    private var targetVelocityXDeviation: Float? = null
    private var targetVelocityY: Float? = null
    private var targetVelocityYDeviation: Float? = null
    private var initialRotation = 0
    private var initialRotationDeviation = 0
    private var rotationalVelocity = 0f
    private var rotationalVelocityDeviation = 0f
    private var rotationalAcceleration = 0f
    private var rotationalAccelerationDeviation = 0f
    private var targetRotationalVelocity: Float? = null
    private var targetRotationalVelocityDeviation: Float? = null
    private var ttl: Long
    private var animationListener: ConfettiAnimationListener? = null

    constructor(context: Context?, confettoGenerator: ConfettoGenerator,
                confettiSource: ConfettiSource, parentView: ViewGroup) : this(confettoGenerator, confettiSource, parentView, newInstance(context!!)) {
    }

    /**
     * The number of confetti initially emitted before any time has elapsed.
     *
     * @param numInitialCount the number of initial confetti.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setNumInitialCount(numInitialCount: Int): ConfettiManager {
        this.numInitialCount = numInitialCount
        return this
    }

    /**
     * Configures how long this manager will emit new confetti after the animation starts.
     *
     * @param emissionDurationInMillis how long to emit new confetti in millis. This value can be
     * [.INFINITE_DURATION] for a never-ending emission.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setEmissionDuration(emissionDurationInMillis: Long): ConfettiManager {
        emissionDuration = emissionDurationInMillis
        return this
    }

    /**
     * Configures how frequently this manager will emit new confetti after the animation starts
     * if [.emissionDuration] is a positive value.
     *
     * @param emissionRate the rate of emission in # of confetti per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setEmissionRate(emissionRate: Float): ConfettiManager {
        this.emissionRate = emissionRate / 1000f
        emissionRateInverse = 1f / this.emissionRate
        return this
    }

    /**
     * @see .setVelocityX
     * @param velocityX the X velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setVelocityX(velocityX: Float): ConfettiManager {
        return setVelocityX(velocityX, 0f)
    }

    /**
     * Set the velocityX used by this manager. This value defines the initial X velocity
     * for the generated confetti. The actual confetti's X velocity will be
     * (velocityX +- [0, velocityDeviationX]).
     *
     * @param velocityX the X velocity in pixels per second.
     * @param velocityDeviationX the deviation from X velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setVelocityX(velocityX: Float, velocityDeviationX: Float): ConfettiManager {
        this.velocityX = velocityX / 1000f
        this.velocityDeviationX = velocityDeviationX / 1000f
        return this
    }

    /**
     * @see .setVelocityY
     * @param velocityY the Y velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setVelocityY(velocityY: Float): ConfettiManager {
        return setVelocityY(velocityY, 0f)
    }

    /**
     * Set the velocityY used by this manager. This value defines the initial Y velocity
     * for the generated confetti. The actual confetti's Y velocity will be
     * (velocityY +- [0, velocityDeviationY]). A positive Y velocity means that the velocity
     * is going down (because Y coordinate increases going down).
     *
     * @param velocityY the Y velocity in pixels per second.
     * @param velocityDeviationY the deviation from Y velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setVelocityY(velocityY: Float, velocityDeviationY: Float): ConfettiManager {
        this.velocityY = velocityY / 1000f
        this.velocityDeviationY = velocityDeviationY / 1000f
        return this
    }

    /**
     * @see .setAccelerationX
     * @param accelerationX the X acceleration in pixels per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setAccelerationX(accelerationX: Float): ConfettiManager {
        return setAccelerationX(accelerationX, 0f)
    }

    /**
     * Set the accelerationX used by this manager. This value defines the X acceleration
     * for the generated confetti. The actual confetti's X acceleration will be
     * (accelerationX +- [0, accelerationDeviationX]).
     *
     * @param accelerationX the X acceleration in pixels per second^2.
     * @param accelerationDeviationX the deviation from X acceleration in pixels per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setAccelerationX(accelerationX: Float, accelerationDeviationX: Float): ConfettiManager {
        this.accelerationX = accelerationX / 1000000f
        this.accelerationDeviationX = accelerationDeviationX / 1000000f
        return this
    }

    /**
     * @see .setAccelerationY
     * @param accelerationY the Y acceleration in pixels per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setAccelerationY(accelerationY: Float): ConfettiManager {
        return setAccelerationY(accelerationY, 0f)
    }

    /**
     * Set the accelerationY used by this manager. This value defines the Y acceleration
     * for the generated confetti. The actual confetti's Y acceleration will be
     * (accelerationY +- [0, accelerationDeviationY]). A positive Y acceleration means that the
     * confetto will be accelerating downwards.
     *
     * @param accelerationY the Y acceleration in pixels per second^2.
     * @param accelerationDeviationY the deviation from Y acceleration in pixels per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setAccelerationY(accelerationY: Float, accelerationDeviationY: Float): ConfettiManager {
        this.accelerationY = accelerationY / 1000000f
        this.accelerationDeviationY = accelerationDeviationY / 1000000f
        return this
    }

    /**
     * @see .setTargetVelocityX
     * @param targetVelocityX the target X velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setTargetVelocityX(targetVelocityX: Float): ConfettiManager {
        return setTargetVelocityX(targetVelocityX, 0f)
    }

    /**
     * Set the target X velocity that confetti can reach during the animation. The actual confetti's
     * target X velocity will be (targetVelocityX +- [0, targetVelocityXDeviation]).
     *
     * @param targetVelocityX the target X velocity in pixels per second.
     * @param targetVelocityXDeviation  the deviation from target X velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setTargetVelocityX(targetVelocityX: Float,
                           targetVelocityXDeviation: Float): ConfettiManager {
        this.targetVelocityX = targetVelocityX / 1000f
        this.targetVelocityXDeviation = targetVelocityXDeviation / 1000f
        return this
    }

    /**
     * @see .setTargetVelocityY
     * @param targetVelocityY the target Y velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setTargetVelocityY(targetVelocityY: Float): ConfettiManager {
        return setTargetVelocityY(targetVelocityY, 0f)
    }

    /**
     * Set the target Y velocity that confetti can reach during the animation. The actual confetti's
     * target Y velocity will be (targetVelocityY +- [0, targetVelocityYDeviation]).
     *
     * @param targetVelocityY the target Y velocity in pixels per second.
     * @param targetVelocityYDeviation  the deviation from target Y velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setTargetVelocityY(targetVelocityY: Float,
                           targetVelocityYDeviation: Float): ConfettiManager {
        this.targetVelocityY = targetVelocityY / 1000f
        this.targetVelocityYDeviation = targetVelocityYDeviation / 1000f
        return this
    }

    /**
     * @see .setInitialRotation
     * @param initialRotation the initial rotation in degrees.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setInitialRotation(initialRotation: Int): ConfettiManager {
        return setInitialRotation(initialRotation, 0)
    }

    /**
     * Set the initialRotation used by this manager. This value defines the initial rotation in
     * degrees for the generated confetti. The actual confetti's initial rotation will be
     * (initialRotation +- [0, initialRotationDeviation]).
     *
     * @param initialRotation the initial rotation in degrees.
     * @param initialRotationDeviation the deviation from initial rotation in degrees.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setInitialRotation(initialRotation: Int, initialRotationDeviation: Int): ConfettiManager {
        this.initialRotation = initialRotation
        this.initialRotationDeviation = initialRotationDeviation
        return this
    }

    /**
     * @see .setRotationalVelocity
     * @param rotationalVelocity the initial rotational velocity in degrees per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setRotationalVelocity(rotationalVelocity: Float): ConfettiManager {
        return setRotationalVelocity(rotationalVelocity, 0f)
    }

    /**
     * Set the rotationalVelocity used by this manager. This value defines the the initial
     * rotational velocity for the generated confetti. The actual confetti's initial
     * rotational velocity will be (rotationalVelocity +- [0, rotationalVelocityDeviation]).
     *
     * @param rotationalVelocity the initial rotational velocity in degrees per second.
     * @param rotationalVelocityDeviation the deviation from initial rotational velocity in
     * degrees per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setRotationalVelocity(rotationalVelocity: Float,
                              rotationalVelocityDeviation: Float): ConfettiManager {
        this.rotationalVelocity = rotationalVelocity / 1000f
        this.rotationalVelocityDeviation = rotationalVelocityDeviation / 1000f
        return this
    }

    /**
     * @see .setRotationalAcceleration
     * @param rotationalAcceleration the rotational acceleration in degrees per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setRotationalAcceleration(rotationalAcceleration: Float): ConfettiManager {
        return setRotationalAcceleration(rotationalAcceleration, 0f)
    }

    /**
     * Set the rotationalAcceleration used by this manager. This value defines the the
     * acceleration of the rotation for the generated confetti. The actual confetti's rotational
     * acceleration will be (rotationalAcceleration +- [0, rotationalAccelerationDeviation]).
     *
     * @param rotationalAcceleration the rotational acceleration in degrees per second^2.
     * @param rotationalAccelerationDeviation the deviation from rotational acceleration in degrees
     * per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setRotationalAcceleration(rotationalAcceleration: Float,
                                  rotationalAccelerationDeviation: Float): ConfettiManager {
        this.rotationalAcceleration = rotationalAcceleration / 1000000f
        this.rotationalAccelerationDeviation = rotationalAccelerationDeviation / 1000000f
        return this
    }

    /**
     * @see .setTargetRotationalVelocity
     * @param targetRotationalVelocity the target rotational velocity in degrees per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setTargetRotationalVelocity(targetRotationalVelocity: Float): ConfettiManager {
        return setTargetRotationalVelocity(targetRotationalVelocity, 0f)
    }

    /**
     * Set the target rotational velocity that confetti can reach during the animation. The actual
     * confetti's target rotational velocity will be
     * (targetRotationalVelocity +- [0, targetRotationalVelocityDeviation]).
     *
     * @param targetRotationalVelocity the target rotational velocity in degrees per second.
     * @param targetRotationalVelocityDeviation the deviation from target rotational velocity
     * in degrees per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setTargetRotationalVelocity(targetRotationalVelocity: Float,
                                    targetRotationalVelocityDeviation: Float): ConfettiManager {
        this.targetRotationalVelocity = targetRotationalVelocity / 1000f
        this.targetRotationalVelocityDeviation = targetRotationalVelocityDeviation / 1000f
        return this
    }

    /**
     * Specifies a custom bound that the confetti will clip to. By default, the confetti will be
     * able to animate throughout the entire screen. The dimensions specified in bound is
     * global dimensions, e.g. x=0 is the top of the screen, rather than relative dimensions.
     *
     * @param bound the bound that clips the confetti as they animate.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setBound(bound: Rect): ConfettiManager {
        this.bound = bound
        return this
    }

    /**
     * Specifies a custom time to live for the confetti generated by this manager. When a confetti
     * reaches its time to live timer, it will disappear and terminate its animation.
     *
     *
     * The time to live value does not include the initial delay of the confetti.
     *
     * @param ttlInMillis the custom time to live in milliseconds.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setTTL(ttlInMillis: Long): ConfettiManager {
        ttl = ttlInMillis
        return this
    }

    /**
     * Enables fade out for all of the confetti generated by this manager. Fade out means that
     * the confetti will animate alpha according to the fadeOutInterpolator according
     * to its TTL or, if TTL is not set, its bounds.
     *
     * @param fadeOutInterpolator an interpolator that interpolates animation progress [0, 1] into
     * an alpha value [0, 1], 0 being transparent and 1 being opaque.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun enableFadeOut(fadeOutInterpolator: Interpolator?): ConfettiManager {
        this.fadeOutInterpolator = fadeOutInterpolator
        return this
    }

    /**
     * Disables fade out for all of the confetti generated by this manager.
     *
     * @return the confetti manager so that the set calls can be chained.
     */
    fun disableFadeOut(): ConfettiManager {
        fadeOutInterpolator = null
        return this
    }

    /**
     * Enables or disables touch events for the confetti generated by this manager. By enabling
     * touch, the user can touch individual confetto and drag/fling them on the screen independent
     * of their original animation state.
     *
     * @param touchEnabled whether or not to enable touch.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setTouchEnabled(touchEnabled: Boolean): ConfettiManager {
        confettiView.setTouchEnabled(touchEnabled)
        return this
    }

    /**
     * Sets a [ConfettiAnimationListener] for this confetti manager.
     *
     * @param listener the animation listener, or null to clear out the existing listener.
     * @return the confetti manager so that the set calls can be chained.
     */
    fun setConfettiAnimationListener(listener: ConfettiAnimationListener?): ConfettiManager {
        animationListener = listener
        return this
    }

    /**
     * Start the confetti animation configured by this manager.
     *
     * @return the confetti manager itself that just started animating.
     */
    fun animate(): ConfettiManager {
        if (animationListener != null) {
            animationListener!!.onAnimationStart(this)
        }
        cleanupExistingAnimation()
        attachConfettiViewToParent()
        addNewConfetti(numInitialCount, 0)
        startNewAnimation()
        return this
    }

    /**
     * Terminate the currently running animation if there is any.
     */
    fun terminate() {
        if (animator != null) {
            animator!!.cancel()
        }
        confettiView.terminate()
        if (animationListener != null) {
            animationListener!!.onAnimationEnd(this)
        }
    }

    private fun cleanupExistingAnimation() {
        if (animator != null) {
            animator!!.cancel()
        }
        lastEmittedTimestamp = 0
        val iterator = confetti.iterator()
        while (iterator.hasNext()) {
            removeConfetto(iterator.next())
            iterator.remove()
        }
    }

    private fun attachConfettiViewToParent() {
        val currentParent = confettiView.parent
        if (currentParent != null) {
            if (currentParent !== parentView) {
                (currentParent as ViewGroup).removeView(confettiView)
                parentView.addView(confettiView)
            }
        } else {
            parentView.addView(confettiView)
        }
        confettiView.reset()
    }

    private fun addNewConfetti(numConfetti: Int, initialDelay: Long) {
        for (i in 0 until numConfetti) {
            var confetto = recycledConfetti.poll()
            if (confetto == null) {
                confetto = confettoGenerator.generateConfetto(random)
            }
            confetto!!.reset()
            configureConfetto(confetto, confettiSource, random, initialDelay)
            confetto.prepare(bound)
            addConfetto(confetto)
        }
    }

    private fun startNewAnimation() {
        // Never-ending animator, we will cancel once the termination condition is reached.
        animator = ValueAnimator.ofInt(0)
                .setDuration(Long.MAX_VALUE)
        animator!!.addUpdateListener { valueAnimator ->
            val elapsedTime = valueAnimator.currentPlayTime
            processNewEmission(elapsedTime)
            updateConfetti(elapsedTime)
            if (confetti.size == 0 && elapsedTime >= emissionDuration) {
                terminate()
            } else {
                confettiView.invalidate()
            }
        }
        animator!!.start()
    }

    private fun processNewEmission(elapsedTime: Long) {
        if (elapsedTime < emissionDuration) {
            if (lastEmittedTimestamp == 0L) {
                lastEmittedTimestamp = elapsedTime
            } else {
                val timeSinceLastEmission = elapsedTime - lastEmittedTimestamp

                // Randomly determine how many confetti to emit
                val numNewConfetti = (random.nextFloat() * emissionRate * timeSinceLastEmission).toInt()
                if (numNewConfetti > 0) {
                    lastEmittedTimestamp += Math.round(emissionRateInverse * numNewConfetti).toLong()
                    addNewConfetti(numNewConfetti, elapsedTime)
                }
            }
        }
    }

    private fun updateConfetti(elapsedTime: Long) {
        val iterator = confetti.iterator()
        while (iterator.hasNext()) {
            val confetto = iterator.next()
            if (!confetto!!.applyUpdate(elapsedTime)) {
                iterator.remove()
                removeConfetto(confetto)
            }
        }
    }

    private fun addConfetto(confetto: Confetto?) {
        confetti.add(confetto)
        if (animationListener != null) {
            animationListener!!.onConfettoEnter(confetto)
        }
    }

    private fun removeConfetto(confetto: Confetto?) {
        if (animationListener != null) {
            animationListener!!.onConfettoExit(confetto)
        }
        recycledConfetti.add(confetto)
    }

    protected fun configureConfetto(confetto: Confetto?, confettiSource: ConfettiSource,
                                    random: Random, initialDelay: Long) {
        confetto!!.setInitialDelay(initialDelay)
        confetto.setInitialX(confettiSource.getInitialX(random.nextFloat()))
        confetto.setInitialY(confettiSource.getInitialY(random.nextFloat()))
        confetto.setInitialVelocityX(getVarianceAmount(velocityX, velocityDeviationX, random))
        confetto.setInitialVelocityY(getVarianceAmount(velocityY, velocityDeviationY, random))
        confetto.setAccelerationX(getVarianceAmount(accelerationX, accelerationDeviationX, random))
        confetto.setAccelerationY(getVarianceAmount(accelerationY, accelerationDeviationY, random))
        confetto.setTargetVelocityX(if (targetVelocityX == null) null else getVarianceAmount(targetVelocityX!!, targetVelocityXDeviation!!, random))
        confetto.setTargetVelocityY(if (targetVelocityY == null) null else getVarianceAmount(targetVelocityY!!, targetVelocityYDeviation!!, random))
        confetto.setInitialRotation(
                getVarianceAmount(initialRotation.toFloat(), initialRotationDeviation.toFloat(), random))
        confetto.setInitialRotationalVelocity(
                getVarianceAmount(rotationalVelocity, rotationalVelocityDeviation, random))
        confetto.setRotationalAcceleration(
                getVarianceAmount(rotationalAcceleration, rotationalAccelerationDeviation, random))
        confetto.setTargetRotationalVelocity(if (targetRotationalVelocity == null) null else getVarianceAmount(targetRotationalVelocity!!, targetRotationalVelocityDeviation!!,
                random))
        confetto.setTTL(ttl)
        confetto.setFadeOut(fadeOutInterpolator)
    }

    private fun getVarianceAmount(base: Float, deviation: Float, random: Random): Float {
        // Normalize random to be [-1, 1] rather than [0, 1]
        return base + deviation * (random.nextFloat() * 2 - 1)
    }

    interface ConfettiAnimationListener {
        fun onAnimationStart(confettiManager: ConfettiManager?)
        fun onAnimationEnd(confettiManager: ConfettiManager?)
        fun onConfettoEnter(confetto: Confetto?)
        fun onConfettoExit(confetto: Confetto?)
    }

    class ConfettiAnimationListenerAdapter : ConfettiAnimationListener {
        override fun onAnimationStart(confettiManager: ConfettiManager?) {}
        override fun onAnimationEnd(confettiManager: ConfettiManager?) {}
        override fun onConfettoEnter(confetto: Confetto?) {}
        override fun onConfettoExit(confetto: Confetto?) {}
    }

    companion object {
        const val INFINITE_DURATION = Long.MAX_VALUE
    }

    init {
        confettiView.bind(confetti)
        confettiView.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                terminate()
            }
        })

        // Set the defaults
        ttl = -1
        bound = Rect(0, 0, parentView.width, parentView.height)
    }
}