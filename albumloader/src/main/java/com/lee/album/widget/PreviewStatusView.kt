package com.lee.album.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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


}