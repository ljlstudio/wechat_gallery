package com.lee.album.widget

import android.view.animation.AnimationUtils
import android.view.animation.Interpolator

class ScaleHelper {

    private var mStartTime: Long = 0
    private var mInterpolator: Interpolator? = null
    private var mScale = 0f
    private var mToScale = 0f
    private var mStartX = 0
    private var mDuration = 0
    private var mFinished = true
    private var mStartY = 0

    fun startScale(scale: Float, toScale: Float, x: Int, y: Int, interpolator: Interpolator?) {
        mStartTime = AnimationUtils.currentAnimationTimeMillis()
        mInterpolator = interpolator
        mScale = scale
        mToScale = toScale
        mStartX = x
        mStartY = y
        var d: Float
        d = if (toScale > scale) {
            toScale / scale
        } else {
            scale / toScale
        }
        if (d > 4) {
            d = 4f
        }
        //倍数差值越大 执行时间越久 280 - 340
        mDuration = (220 + Math.sqrt((d * 3600).toDouble())).toInt()
        mFinished = false
    }

    /**
     * Call this when you want to know the new location. If it returns true, the
     * animation is not yet finished.
     */
    fun computeScrollOffset(): Boolean {
        if (isFinished()) {
            return false
        }
        val time = AnimationUtils.currentAnimationTimeMillis()
        // Any scroller can be used for time, since they were started
        // together in scroll mode. We use X here.
        val elapsedTime = time - mStartTime
        val duration = mDuration
        if (elapsedTime < duration) {
            val q = mInterpolator!!.getInterpolation(elapsedTime / duration.toFloat())
            mScale += q * (mToScale - mScale)
        } else {
            mScale = mToScale
            mFinished = true
        }
        return true
    }

    private fun isFinished(): Boolean {
        return mFinished
    }

    fun getCurScale(): Float {
        return mScale
    }

    fun getStartX(): Int {
        return mStartX
    }

    fun getStartY(): Int {
        return mStartY
    }
}