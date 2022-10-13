package com.lee.album.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.lee.album.R
import com.lee.album.databinding.WechatTitleLayoutBinding

class WechatTitleBar : RelativeLayout {

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

    private fun initialize(context: Context, attrs: AttributeSet?) {
        val binding = DataBindingUtil.inflate<WechatTitleLayoutBinding>(
            LayoutInflater.from(context),
            R.layout.wechat_title_layout,
            this,
            true
        )
    }

}