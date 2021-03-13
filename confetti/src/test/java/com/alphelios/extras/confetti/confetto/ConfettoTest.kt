package com.alphelios.extras.confetti.confetto

import com.alphelios.extras.confetti.confetto.Confetto.Companion.computeBound
import com.alphelios.extras.confetti.confetto.Confetto.Companion.computeMillisToReachTarget
import org.junit.Assert
import org.junit.Test

class ConfettoTest {
    @Test
    fun test_computeMillisToReachTarget() {
        var time = computeMillisToReachTarget(null, 0f, 0f)
        Assert.assertNull(time)
        time = computeMillisToReachTarget(0f, 10f, 10f)
        Assert.assertEquals(0, time!!.toLong())
        time = computeMillisToReachTarget(20f, 10f, 10f)
        Assert.assertEquals(1, time!!.toLong())
        time = computeMillisToReachTarget(30f, 0f, 10f)
        Assert.assertEquals(3, time!!.toLong())
        time = computeMillisToReachTarget(20f, 10f, 0f)
        Assert.assertNull(time)
    }

    @Test
    fun test_computeBound_noAcceleration() {
        // Normal velocity
        var time = computeBound(0f, 0.01f, 0f, null, null, -10000, 100)
        Assert.assertEquals(10000, time)
        time = computeBound(0f, -0.01f, 0f, null, null, -100, 10000)
        Assert.assertEquals(10000, time)
        time = computeBound(10f, 0.01f, 0f, null, null, -10000, 100)
        Assert.assertEquals(9000, time)
        time = computeBound(10f, -0.01f, 0f, null, null, -100, 10000)
        Assert.assertEquals(11000, time)

        // Normal velocity with non-null unreachable target velocity
        time = computeBound(0f, 0.01f, 0f, null, 0.02f, -10000, 100)
        Assert.assertEquals(10000, time)
        time = computeBound(0f, -0.01f, 0f, null, 0.02f, -100, 10000)
        Assert.assertEquals(10000, time)

        // Normal velocity with non-null already-reached target velocity
        time = computeBound(0f, 0.01f, 0f, 0L, -0.01f, -100, 10000)
        Assert.assertEquals(10000, time)

        // Normal velocity with the initial position past bound
        time = computeBound(-100f, 0.01f, 0f, null, null, -50, 100)
        Assert.assertEquals(20000, time)
    }

    @Test
    fun test_computeBound_withAcceleration() {
        // 100 = 0.5 * 0.01 * t * t, t = sqrt(20000) or 141
        var time = computeBound(0f, 0f, 0.01f, null, null, -10000, 100)
        Assert.assertEquals(141, time)
        time = computeBound(0f, 0f, -0.01f, null, null, -100, 10000)
        Assert.assertEquals(141, time)

        // 100 = 10 + 0.01 * t + 0.5 * 0.01 * t * t, t 3.358
        time = computeBound(10f, 0.01f, 0.01f, null, null, -10000, 100)
        Assert.assertEquals(133, time)
        time = computeBound(-10f, -0.01f, -0.01f, null, null, -100, 10000)
        Assert.assertEquals(133, time)
    }

    @Test
    fun test_computeBound_withAccelerationAndTargetVelocity() {
        // 100 = 0.5 * 0.01 * 3 * 3 + 0.03 * (t - 3)
        var time = computeBound(0f, 0f, 0.01f, 3L, 0.03f, -10000, 100)
        Assert.assertEquals(3334, time)
        time = computeBound(0f, 0f, -0.01f, 3L, -0.03f, -100, 10000)
        Assert.assertEquals(3334, time)

        // 100 = 10 + 0.01 * 3 + 0.5 * 0.01 * 3 * 3 + 0.04 * (t - 3)
        time = computeBound(10f, 0.01f, 0.01f, 3L, 0.04f, -10000, 100)
        Assert.assertEquals(2251, time)
        time = computeBound(10f, -0.01f, -0.01f, 3L, -0.04f, -100, 10000)
        Assert.assertEquals(2251, time)
    }
}