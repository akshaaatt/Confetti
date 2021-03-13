package com.alphelios.extras.confetti

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import com.github.jinatonic.confetti.R
import com.alphelios.extras.confetti.confetto.Confetto

/**
 * A helper temporary view that helps render the confetti. This view will attach itself to the
 * view root, perform the animation, and then once all of the confetti has completed its animation,
 * it will automatically remove itself from the parent.
 */
class ConfettiView(context: Context?, attrs: AttributeSet?) : View(context, attrs), OnLayoutChangeListener {
    private var confetti: MutableList<Confetto?>? = null
    private var terminated = false
    private var touchEnabled = false
    private var draggedConfetto: Confetto? = null

    /**
     * Sets the list of confetti to be animated by this view.
     *
     * @param confetti the list of confetti to be animated.
     */
    fun bind(confetti: MutableList<Confetto?>) {
        this.confetti = confetti
    }

    /**
     * @see ConfettiManager.setTouchEnabled
     * @param touchEnabled whether or not to enable touch
     */
    fun setTouchEnabled(touchEnabled: Boolean) {
        this.touchEnabled = touchEnabled
    }

    /**
     * Terminate the current running animation (if any) and remove this view from the parent.
     */
    fun terminate() {
        if (!terminated) {
            terminated = true
            parent.requestLayout()
        }
    }

    /**
     * Reset the internal state of this view to allow for a new confetti animation.
     */
    fun reset() {
        terminated = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val parent = parent as ViewGroup
        parent.removeOnLayoutChangeListener(this)
        parent.addOnLayoutChangeListener(this)

        // If we did not bind before attaching to the window, that means this ConfettiView no longer
        // has a ConfettiManager backing it and should just be terminated.
        if (confetti == null) {
            terminate()
        }
    }

    override fun onLayoutChange(view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int,
                                i7: Int) {
        if (terminated) {
            val parent = parent
            if (parent != null && parent is ViewGroup) {
                val vg = parent
                vg.removeViewInLayout(this)
                vg.removeOnLayoutChangeListener(this)
                vg.invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!terminated) {
            canvas.save()
            for (confetto in confetti!!) {
                confetto!!.draw(canvas)
            }
            canvas.restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false
        if (touchEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> for (confetto in confetti!!) {
                    if (confetto!!.onTouchDown(event)) {
                        draggedConfetto = confetto
                        handled = true
                        break
                    }
                }
                MotionEvent.ACTION_MOVE -> if (draggedConfetto != null) {
                    draggedConfetto!!.onTouchMove(event)
                    handled = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (draggedConfetto != null) {
                    draggedConfetto!!.onTouchUp(event)
                    draggedConfetto = null
                    handled = true
                }
            }
        }
        return handled || super.onTouchEvent(event)
    }

    companion object {
        @JvmStatic
        fun newInstance(context: Context): ConfettiView {
            val confettiView = ConfettiView(context, null)
            confettiView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val defaultElevation = context.resources.getDimensionPixelOffset(
                        R.dimen.confetti_default_elevation)
                confettiView.elevation = defaultElevation.toFloat()
            }
            return confettiView
        }
    }
}