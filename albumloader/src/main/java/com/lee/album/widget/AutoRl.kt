package com.lee.album.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.lee.album.R
import com.lee.album.activity.normal.NormalGalleryActivity
import com.lee.album.activity.normal.NormalGalleryViewModel

class AutoRl : RelativeLayout {


    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(context, attrs)
    }


    val single = 1
    var multi = 2
    var mode: Int = single


    var gestureDetector: GestureDetector? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun initialize(context: Context, attrs: AttributeSet?) {

        gestureDetector = GestureDetector(simpleOnGestureListener)

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector?.onTouchEvent(event)


        return true
    }

    //拦截事件
    private var simpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {

            Log.i("XXX", "XXX")
            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

    }


}