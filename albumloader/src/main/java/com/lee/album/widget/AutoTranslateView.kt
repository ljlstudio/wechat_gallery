package com.lee.album.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

class AutoTranslateView : RelativeLayout {

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }


    private var first: Boolean = false

    private var mHeight: Int = 0

    fun startTranslate(visible: Boolean) {
        measure(0, 0)
        if (!first) {
            mHeight = measuredHeight
            first = true
            return
        }
        val start: Float
        val end: Float
        if (visible) {
            start = 0f
            end = 1f
        } else {
            start = 1f
            end = 0f
        }


        Log.i("mHeight", "" + mHeight)


        val valueAnimator = ValueAnimator()


        valueAnimator.setFloatValues(start, end)
        valueAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Float

            val sum = mHeight * animatedValue

            Log.i("SUM", "" + sum)
            this.apply {
                layoutParams.height = sum.toInt()
                requestLayout()
            }

        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = 200
        valueAnimator.start()
    }
}