package com.lee.album.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet

class RotateImageView : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    fun rotate(selected: Boolean) {
        val start: Float
        var end: Float
        if (selected) {
            start = 0f
            end = 180f
        } else {
            start = 180f
            end = 360f
        }

        val animation = ObjectAnimator.ofFloat(this, "rotation", start, end)
        animation.duration = 400 // ms

        animation.start()
    }
}