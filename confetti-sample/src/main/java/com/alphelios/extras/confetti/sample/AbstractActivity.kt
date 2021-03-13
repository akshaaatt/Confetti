package com.alphelios.extras.confetti.sample

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.alphelios.extras.confetti.ConfettiManager
import com.github.jinatonic.confetti.sample.R
import java.util.*

abstract class AbstractActivity : AppCompatActivity(), View.OnClickListener {
    protected var container: ViewGroup? = null
    protected var goldDark = 0
    private var goldMed = 0
    private var gold = 0
    protected var goldLight = 0
    protected lateinit var colors: IntArray
    private val activeConfettiManagers: MutableList<ConfettiManager> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
        container = findViewById(R.id.container)
        findViewById<View>(R.id.generate_confetti_once_btn).setOnClickListener(this)
        findViewById<View>(R.id.generate_confetti_stream_btn).setOnClickListener(this)
        findViewById<View>(R.id.generate_confetti_infinite_btn).setOnClickListener(this)
        findViewById<View>(R.id.generate_confetti_stop_btn).setOnClickListener(this)
        val res = resources
        goldDark = res.getColor(R.color.gold_dark)
        goldMed = res.getColor(R.color.gold_med)
        gold = res.getColor(R.color.gold)
        goldLight = res.getColor(R.color.gold_light)
        colors = intArrayOf(goldDark, goldMed, gold, goldLight)
    }

    @get:LayoutRes
    protected open val layoutRes: Int get() = R.layout.activity_confetti

    override fun onClick(view: View) {
        when (view.id) {
            R.id.generate_confetti_once_btn -> {
                activeConfettiManagers.add(generateOnce())
            }
            R.id.generate_confetti_stream_btn -> {
                activeConfettiManagers.add(generateStream())
            }
            R.id.generate_confetti_infinite_btn -> {
                activeConfettiManagers.add(generateInfinite())
            }
            else -> {
                for (confettiManager in activeConfettiManagers) {
                    confettiManager.terminate()
                }
                activeConfettiManagers.clear()
            }
        }
    }

    protected abstract fun generateOnce(): ConfettiManager
    protected abstract fun generateStream(): ConfettiManager
    protected abstract fun generateInfinite(): ConfettiManager
}