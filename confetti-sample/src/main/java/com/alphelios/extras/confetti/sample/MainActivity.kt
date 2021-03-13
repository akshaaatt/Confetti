package com.alphelios.extras.confetti.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.jinatonic.confetti.sample.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(android.R.id.list)
        val adapter: ListAdapter = object : ArrayAdapter<ConfettiSample?>(this,
                R.layout.item_confetti_sample, SAMPLES) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val sample = getItem(position)
                (view as TextView).setText(sample!!.nameResId)
                view.setOnClickListener { startActivity(Intent(this@MainActivity, sample.targetActivityClass)) }
                return view
            }
        }
        listView.adapter = adapter
    }

    private class ConfettiSample(val nameResId: Int, val targetActivityClass: Class<out Activity?>)
    companion object {
        private val SAMPLES = arrayOf(
                ConfettiSample(
                        R.string.falling_confetti_from_top,
                        FallingConfettiFromTopActivity::class.java
                ),
                ConfettiSample(
                        R.string.falling_confetti_from_point,
                        FallingConfettiFromPointActivity::class.java
                ),
                ConfettiSample(
                        R.string.explosion_confetti,
                        ExplosionActivity::class.java
                ),
                ConfettiSample(
                        R.string.shimmering_confetti,
                        ShimmeringActivity::class.java
                ),
                ConfettiSample(
                        R.string.ice_flakes_with_touch,
                        FallingWithTouchActivity::class.java
                ),
                ConfettiSample(
                        R.string.ice_flakes_with_listener,
                        FallingConfettiWithListenerActivity::class.java
                ))
    }
}