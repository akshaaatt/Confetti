package com.alphelios.extras.confetti.sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.alphelios.extras.confetti.ConfettiManager
import com.alphelios.extras.confetti.ConfettiManager.ConfettiAnimationListener
import com.alphelios.extras.confetti.ConfettiSource
import com.alphelios.extras.confetti.ConfettoGenerator
import com.alphelios.extras.confetti.confetto.BitmapConfetto
import com.alphelios.extras.confetti.confetto.Confetto
import com.github.jinatonic.confetti.sample.R
import java.util.*

class FallingConfettiWithListenerActivity : AbstractActivity(), ConfettoGenerator, ConfettiAnimationListener {
    private var numConfettiTxt: TextView? = null
    private var numConfettiOnScreen = 0
    private var size = 0
    private var velocitySlow = 0
    private var velocityNormal = 0
    private var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        numConfettiTxt = findViewById(R.id.num_confetti_txt)
        val res = resources
        size = res.getDimensionPixelSize(R.dimen.big_confetti_size)
        velocitySlow = res.getDimensionPixelOffset(R.dimen.default_velocity_slow)
        velocityNormal = res.getDimensionPixelOffset(R.dimen.default_velocity_normal)
        bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(res, R.drawable.snowflake),
                size,
                size,
                false
        )
    }

    override val layoutRes: Int get() = R.layout.activity_confetti_with_listener

    override fun generateOnce(): ConfettiManager {
        return confettiManager.setNumInitialCount(20)
                .setEmissionDuration(0)
                .setConfettiAnimationListener(this)
                .animate()
    }

    override fun generateStream(): ConfettiManager {
        return confettiManager.setNumInitialCount(0)
                .setEmissionDuration(3000)
                .setEmissionRate(20f)
                .setConfettiAnimationListener(this)
                .animate()
    }

    override fun generateInfinite(): ConfettiManager {
        return confettiManager.setNumInitialCount(0)
                .setEmissionDuration(ConfettiManager.INFINITE_DURATION)
                .setEmissionRate(20f)
                .setConfettiAnimationListener(this)
                .animate()
    }

    private val confettiManager: ConfettiManager
        get() {
            val source = ConfettiSource(0, -size, container!!.width, -size)
            return ConfettiManager(this, this, source, container!!)
                    .setVelocityX(0f, velocitySlow.toFloat())
                    .setVelocityY(velocityNormal.toFloat(), velocitySlow.toFloat())
                    .setRotationalVelocity(180f, 90f)
                    .setTouchEnabled(true)
        }

    override fun generateConfetto(random: Random?): Confetto {
        return BitmapConfetto(bitmap!!)
    }

    override fun onAnimationStart(confettiManager: ConfettiManager?) {
        Toast.makeText(this, "Starting confetti animation", Toast.LENGTH_SHORT).show()
    }

    override fun onAnimationEnd(confettiManager: ConfettiManager?) {
        numConfettiOnScreen = 0
        updateNumConfettiTxt()
        Toast.makeText(this, "Ending confetti animation", Toast.LENGTH_SHORT).show()
    }

    override fun onConfettoEnter(confetto: Confetto?) {
        numConfettiOnScreen++
        updateNumConfettiTxt()
    }

    override fun onConfettoExit(confetto: Confetto?) {
        numConfettiOnScreen--
        updateNumConfettiTxt()
    }

    private fun updateNumConfettiTxt() {
        numConfettiTxt!!.text = getString(R.string.num_confetti_desc, numConfettiOnScreen)
    }
}