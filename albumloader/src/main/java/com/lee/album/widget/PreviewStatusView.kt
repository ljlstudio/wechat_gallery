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
import com.lee.album.databinding.PreviewStatusLayoutBinding

class PreviewStatusView : RelativeLayout {

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


    var binding: PreviewStatusLayoutBinding? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun initialize(context: Context, attrs: AttributeSet?) {
        val normalModel =
            NormalGalleryActivity.activity?.viewModels<NormalGalleryViewModel>()?.value
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.preview_status_layout,
            this,
            true
        )
        binding?.model = normalModel


    }


    /**
     * 设置状态
     */
    fun setStatus() {
        if (lastAlpha) {
            lastAlpha = false
            return
        }
        if (binding?.top?.visibility == View.GONE) {
            binding?.top?.visibility = View.VISIBLE
            binding?.bottom?.visibility = View.VISIBLE
        } else {
            binding?.top?.visibility = View.GONE
            binding?.bottom?.visibility = View.GONE
        }
        Log.i("xx", "it=xxxx")
    }


    var lastAlpha = false
    fun setAlphaStatus(it: Float) {


        if (it == 255f) {
            lastAlpha = true
            binding?.top?.visibility = View.VISIBLE
            binding?.bottom?.visibility = View.VISIBLE
        } else {
            binding?.top?.visibility = View.GONE
            binding?.bottom?.visibility = View.GONE
        }
    }


}