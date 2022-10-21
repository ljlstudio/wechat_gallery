package com.lee.album.activity.preview

import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.lee.album.Constants
import com.lee.album.R
import com.lee.album.activity.normal.NormalGalleryActivity
import com.lee.album.activity.normal.NormalGalleryViewModel
import com.lee.album.databinding.PicturePreviewLayoutBinding
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity

class PicturePreViewActivity : RxAppCompatActivity() {

    var binding: PicturePreviewLayoutBinding? = null

    private val normalModel = NormalGalleryActivity.activity?.viewModels<NormalGalleryViewModel>()?.value
    private var layoutChangedListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = DataBindingUtil.setContentView(this, R.layout.picture_preview_layout)

        initParam()
    }


//

    private fun initParam() {

        binding?.viewpagerHorizontal?.offscreenPageLimit = 3
        binding?.model = normalModel



        layoutChangedListener=ViewTreeObserver.OnGlobalLayoutListener {

            val extras = intent.extras
            extras?.let {
                val position = it.getInt(Constants.KEY_POSITION, -1)
                val classifyName = it.getString(Constants.KEY_NAME, "")
                val id = it.getString(Constants.KEY_ID, "")
                normalModel?.pageCurrentItem?.set(position)
                Log.i("position",""+position)

            }
            binding?.viewpagerHorizontal?.viewTreeObserver?.removeOnGlobalLayoutListener {layoutChangedListener }
            layoutChangedListener = null
        }


        binding?.viewpagerHorizontal?.viewTreeObserver
            ?.addOnGlobalLayoutListener(layoutChangedListener)


    }




}