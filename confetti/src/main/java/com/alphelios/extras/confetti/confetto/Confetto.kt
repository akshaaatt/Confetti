/*
 * Copyright (C) 2016 Robinhood Markets, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alphelios.extras.confetti.confetto

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.animation.Interpolator
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Abstract class that represents a single confetto on the screen. This class holds all of the
 * internal states for the confetto to help it animate.
 *
 *
 * All of the configured states are in milliseconds, e.g. pixels per millisecond for velocity.
 */
abstract class Confetto {
    private val matrix = Matrix()
    private val workPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val workPairs = FloatArray(2)

    // Configured coordinate states
    private var bound: Rect? = null
    private var initialDelay: Long = 0
    private var initialX = 0f
    private var initialY = 0f
    private var initialVelocityX = 0f
    private var initialVelocityY = 0f
    private var accelerationX = 0f
    private var accelerationY = 0f
    private var targetVelocityX: Float? = null
    private var targetVelocityY: Float? = null
    private var millisToReachTargetVelocityX: Long? = null
    private var millisToReachTargetVelocityY: Long? = null

    // Configured rotation states
    private var initialRotation = 0f
    private var initialRotationalVelocity = 0f
    private var rotationalAcceleration = 0f
    private var targetRotationalVelocity: Float? = null
    private var millisToReachTargetRotationalVelocity: Long? = null

    // Configured animation states
    private var ttl: Long = 0
    private var fadeOutInterpolator: Interpolator? = null
    private var millisToReachBound = 0f
    private var percentageAnimated = 0f

    // Current draw states
    private var currentX = 0f
    private var currentY = 0f
    private var currentRotation = 0f
    private var currentVelocityX = 0f
    private var currentVelocityY = 0f
    private var currentRotationalVelocity = 0f

    // alpha is [0, 255]
    private var alpha = 0
    private var startedAnimation = false
    private var terminated = false

    // Touch events
    private var touchOverride = false
    private var velocityTracker: VelocityTracker? = null
    private var overrideX = 0f
    private var overrideY = 0f
    private var overrideVelocityX = 0f
    private var overrideVelocityY = 0f
    private var overrideDeltaX = 0f
    private var overrideDeltaY = 0f

    /**
     * This method should be called after all of the confetto's state variables are configured
     * and before the confetto gets animated.
     *
     * @param bound the space in which the confetto can display in.
     */
    fun prepare(bound: Rect?) {
        this.bound = bound
        millisToReachTargetVelocityX = computeMillisToReachTarget(targetVelocityX,
                initialVelocityX, accelerationX)
        millisToReachTargetVelocityY = computeMillisToReachTarget(targetVelocityY,
                initialVelocityY, accelerationY)
        millisToReachTargetRotationalVelocity = computeMillisToReachTarget(targetRotationalVelocity,
                initialRotationalVelocity, rotationalAcceleration)

        // Compute how long it would take to reach x/y bounds or reach TTL.
        millisToReachBound = if (ttl >= 0) ttl.toFloat() else Long.MAX_VALUE.toFloat()
        val timeToReachXBound = computeBound(initialX, initialVelocityX, accelerationX,
                millisToReachTargetVelocityX, targetVelocityX,
                bound!!.left - width, bound.right)
        millisToReachBound = min(timeToReachXBound.toFloat(), millisToReachBound)
        val timeToReachYBound = computeBound(initialY, initialVelocityY, accelerationY,
                millisToReachTargetVelocityY, targetVelocityY,
                bound.top - height, bound.bottom)
        millisToReachBound = min(timeToReachYBound.toFloat(), millisToReachBound)
        configurePaint(workPaint)
    }

    private fun doesLocationIntercept(x: Float, y: Float): Boolean {
        return currentX <= x && x <= currentX + width && currentY <= y && y <= currentY + height
    }

    fun onTouchDown(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        return if (doesLocationIntercept(x, y)) {
            touchOverride = true
            overrideX = x
            overrideY = y
            overrideDeltaX = currentX - x
            overrideDeltaY = currentY - y
            velocityTracker = VelocityTracker.obtain()
            velocityTracker!!.addMovement(event)
            true
        } else {
            false
        }
    }

    fun onTouchMove(event: MotionEvent) {
        overrideX = event.x
        overrideY = event.y
        velocityTracker!!.addMovement(event)
        velocityTracker!!.computeCurrentVelocity(1)
        overrideVelocityX = velocityTracker!!.xVelocity
        overrideVelocityY = velocityTracker!!.yVelocity
    }

    fun onTouchUp(event: MotionEvent) {
        velocityTracker!!.addMovement(event)
        velocityTracker!!.computeCurrentVelocity(1)
        initialDelay = RESET_ANIMATION_INITIAL_DELAY
        initialX = event.x + overrideDeltaX
        initialY = event.y + overrideDeltaY
        initialVelocityX = velocityTracker!!.xVelocity
        initialVelocityY = velocityTracker!!.yVelocity
        initialRotation = currentRotation
        velocityTracker!!.recycle()
        velocityTracker = null
        prepare(bound)
        touchOverride = false
    }

    /**
     * @return the width of the confetto.
     */
    abstract val width: Int

    /**
     * @return the height of the confetto.
     */
    abstract val height: Int

    /**
     * Reset this confetto object's internal states so that it can be re-used.
     */
    fun reset() {
        initialDelay = 0
        initialY = 0f
        initialX = initialY
        initialVelocityY = 0f
        initialVelocityX = initialVelocityY
        accelerationY = 0f
        accelerationX = accelerationY
        targetVelocityY = null
        targetVelocityX = targetVelocityY
        millisToReachTargetVelocityY = null
        millisToReachTargetVelocityX = millisToReachTargetVelocityY
        initialRotation = 0f
        initialRotationalVelocity = 0f
        rotationalAcceleration = 0f
        targetRotationalVelocity = null
        millisToReachTargetRotationalVelocity = null
        ttl = 0
        millisToReachBound = 0f
        percentageAnimated = 0f
        fadeOutInterpolator = null
        currentY = 0f
        currentX = currentY
        currentVelocityY = 0f
        currentVelocityX = currentVelocityY
        currentRotation = 0f
        alpha = MAX_ALPHA
        startedAnimation = false
        terminated = false
    }

    /**
     * Hook to configure the global paint states before any animation happens.
     *
     * @param paint the paint object that will be used to perform all draw operations.
     */
    protected open fun configurePaint(paint: Paint) {
        paint.alpha = alpha
    }

    /**
     * Update the confetto internal state based on the provided passed time.
     *
     * @param passedTime time since the beginning of the animation.
     * @return whether this particular confetto is still animating.
     */
    fun applyUpdate(passedTime: Long): Boolean {
        if (initialDelay == RESET_ANIMATION_INITIAL_DELAY) {
            initialDelay = passedTime
        }
        val animatedTime = passedTime - initialDelay
        startedAnimation = animatedTime >= 0
        if (startedAnimation && !terminated) {
            computeDistance(workPairs, animatedTime, initialX, initialVelocityX, accelerationX,
                    millisToReachTargetVelocityX, targetVelocityX)
            currentX = workPairs[0]
            currentVelocityX = workPairs[1]
            computeDistance(workPairs, animatedTime, initialY, initialVelocityY, accelerationY,
                    millisToReachTargetVelocityY, targetVelocityY)
            currentY = workPairs[0]
            currentVelocityY = workPairs[1]
            computeDistance(workPairs, animatedTime, initialRotation,
                    initialRotationalVelocity, rotationalAcceleration,
                    millisToReachTargetRotationalVelocity, targetRotationalVelocity)
            currentRotation = workPairs[0]
            currentRotationalVelocity = workPairs[1]
            alpha = if (fadeOutInterpolator != null) {
                val interpolatedTime = fadeOutInterpolator!!.getInterpolation(animatedTime / millisToReachBound)
                (interpolatedTime * MAX_ALPHA).toInt()
            } else {
                MAX_ALPHA
            }
            terminated = !touchOverride && animatedTime >= millisToReachBound
            percentageAnimated = min(1f, animatedTime / millisToReachBound)
        }
        return !terminated
    }

    private fun computeDistance(pair: FloatArray, t: Long, xi: Float, vi: Float, ai: Float, targetTime: Long?,
                                vTarget: Float?) {
        // velocity with constant acceleration
        val vX = ai * t + vi
        pair[1] = vX
        if (targetTime == null || t < targetTime) {
            // distance covered with constant acceleration
            // distance = xi + vi * t + 1/2 * a * t^2
            val x = xi + vi * t + 0.5f * ai * t * t
            pair[0] = x
        } else {
            // distance covered with constant acceleration + distance covered with max velocity
            // distance = xi + vi * targetTime + 1/2 * a * targetTime^2
            //     + (t - targetTime) * vTarget;
            val x = xi + vi * targetTime + 0.5f * ai * targetTime * targetTime + (t - targetTime) * vTarget!!
            pair[0] = x
        }
    }

    /**
     * Primary method for rendering this confetto on the canvas.
     *
     * @param canvas the canvas to draw on.
     */
    fun draw(canvas: Canvas) {
        if (touchOverride) {
            // Replace time-calculated velocities with touch-velocities
            currentVelocityX = overrideVelocityX
            currentVelocityY = overrideVelocityY
            draw(canvas, overrideX + overrideDeltaX, overrideY + overrideDeltaY, currentRotation, percentageAnimated)
        } else if (startedAnimation && !terminated) {
            draw(canvas, currentX, currentY, currentRotation, percentageAnimated)
        }
    }

    private fun draw(canvas: Canvas, x: Float, y: Float, rotation: Float, percentageAnimated: Float) {
        canvas.save()
        canvas.clipRect(bound!!)
        matrix.reset()
        workPaint.alpha = alpha
        drawInternal(canvas, matrix, workPaint, x, y, rotation, percentageAnimated)
        canvas.restore()
    }

    /**
     * Subclasses need to override this method to optimize for the way to draw the appropriate
     * confetto on the canvas.
     *
     * @param canvas the canvas to draw on.
     * @param matrix an identity matrix to use for draw manipulations.
     * @param paint the paint to perform canvas draw operations on. This paint has already been
     * configured via [.configurePaint].
     * @param x the x position of the confetto relative to the canvas.
     * @param y the y position of the confetto relative to the canvas.
     * @param rotation the rotation (in degrees) to draw the confetto.
     * @param percentAnimated the percentage [0f, 1f] of animation progress for this confetto.
     */
    protected abstract fun drawInternal(canvas: Canvas, matrix: Matrix, paint: Paint, x: Float,
                                        y: Float, rotation: Float, percentAnimated: Float)

    // region Helper methods to set all of the necessary values for the confetto.
    fun setInitialDelay(`val`: Long) {
        initialDelay = `val`
    }

    fun setInitialX(`val`: Float) {
        initialX = `val`
    }

    fun setInitialY(`val`: Float) {
        initialY = `val`
    }

    fun setInitialVelocityX(`val`: Float) {
        initialVelocityX = `val`
    }

    fun setInitialVelocityY(`val`: Float) {
        initialVelocityY = `val`
    }

    fun setAccelerationX(`val`: Float) {
        accelerationX = `val`
    }

    fun setAccelerationY(`val`: Float) {
        accelerationY = `val`
    }

    fun setTargetVelocityX(`val`: Float?) {
        targetVelocityX = `val`
    }

    fun setTargetVelocityY(`val`: Float?) {
        targetVelocityY = `val`
    }

    fun setInitialRotation(`val`: Float) {
        initialRotation = `val`
    }

    fun setInitialRotationalVelocity(`val`: Float) {
        initialRotationalVelocity = `val`
    }

    fun setRotationalAcceleration(`val`: Float) {
        rotationalAcceleration = `val`
    }

    fun setTargetRotationalVelocity(`val`: Float?) {
        targetRotationalVelocity = `val`
    }

    fun setTTL(`val`: Long) {
        ttl = `val`
    }

    fun setFadeOut(fadeOutInterpolator: Interpolator?) {
        this.fadeOutInterpolator = fadeOutInterpolator
    } // endregion

    companion object {
        private const val MAX_ALPHA = 255
        private const val RESET_ANIMATION_INITIAL_DELAY: Long = -1

        // Visible for testing
        @JvmStatic
        fun computeMillisToReachTarget(targetVelocity: Float?, initialVelocity: Float,
                                       acceleration: Float): Long? {
            return if (targetVelocity != null) {
                if (acceleration != 0f) {
                    val time = ((targetVelocity - initialVelocity) / acceleration).toLong()
                    if (time > 0) time else 0
                } else {
                    if (targetVelocity < initialVelocity) {
                        0L
                    } else {
                        null
                    }
                }
            } else {
                null
            }
        }

        // Visible for testing
        @JvmStatic
        fun computeBound(initialPos: Float, velocity: Float, acceleration: Float,
                         targetTime: Long?, targetVelocity: Float?, minBound: Int, maxBound: Int): Long {
            return if (acceleration != 0f) {
                // non-zero acceleration
                val bound = if (acceleration > 0) maxBound else minBound
                if (targetTime == null || targetTime < 0) {
                    // https://www.wolframalpha.com/input/
                    // ?i=solve+for+t+in+(d+%3D+x+%2B+v+*+t+%2B+0.5+*+a+*+t+*+t)
                    val tmp = sqrt((
                            2 * acceleration * bound - 2 * acceleration * initialPos
                                    + velocity * velocity).toDouble())
                    val firstTime = (-tmp - velocity) / acceleration
                    if (firstTime > 0) {
                        return firstTime.toLong()
                    }
                    val secondTime = (tmp - velocity) / acceleration
                    if (secondTime > 0) {
                        secondTime.toLong()
                    } else Long.MAX_VALUE
                } else {
                    // d = x + v * tm + 0.5 * a * tm * tm + tv * (t - tm)
                    // d - x - v * tm - 0.5 * a * tm * tm = tv * t - tv * tm
                    // d - x - v * tm - 0.5 * a * tm * tm + tv * tm = tv * t
                    // t = (d - x - v * tm - 0.5 * a * tm * tm + tv * tm) / tv
                    val time = (bound - initialPos - velocity * targetTime - 0.5 * acceleration * targetTime * targetTime +
                            targetVelocity!! * targetTime) /
                            targetVelocity
                    if (time > 0) time.toLong() else Long.MAX_VALUE
                }
            } else {
                val actualVelocity = if (targetTime == null) velocity else targetVelocity!!
                val bound = if (actualVelocity > 0) maxBound else minBound
                if (actualVelocity != 0f) {
                    val time = ((bound - initialPos) / actualVelocity).toDouble()
                    if (time > 0) time.toLong() else Long.MAX_VALUE
                } else {
                    Long.MAX_VALUE
                }
            }
        }
    }
}