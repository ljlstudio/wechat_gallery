package com.lee.album.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout

class ViewDragLayout : RelativeLayout {

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        onCreate(context,attrs)
    }


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        onCreate(context,attrs)
    }

    var viewDragHelper: ViewDragHelper? = null

    private fun onCreate(context: Context, attrs: AttributeSet?) {
        //创建viewDragHelper
        viewDragHelper = ViewDragHelper.create(this, callback)

    }


    val callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {

            return true
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return viewDragHelper?.shouldInterceptTouchEvent(ev) == true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewDragHelper?.processTouchEvent(event)
        return true
    }
}