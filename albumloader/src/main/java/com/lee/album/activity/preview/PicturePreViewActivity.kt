package com.lee.album.activity.preview

import android.os.Bundle
import com.kt.ktmvvm.basic.BaseActivity
import com.lee.album.BR
import com.lee.album.Constants
import com.lee.album.R
import com.lee.album.databinding.PicturePreviewLayoutBinding

class PicturePreViewActivity : BaseActivity<PicturePreviewLayoutBinding, PicturePreViewModel>() {
    override fun initVariableId(): Int {
        return BR.model
    }

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.picture_preview_layout
    }

    override fun initParam() {
        val extras = intent.extras
        extras?.let {
            val position = it.getInt(Constants.KEY_POSITION, -1)
            val classifyName = it.getString(Constants.KEY_NAME, "")
            val id = it.getString(Constants.KEY_ID, "")
            viewModel?.initData()
            viewModel?.loadData(this, classifyName, id, position)
        }

        binding?.viewpagerHorizontal?.offscreenPageLimit=3
    }


}