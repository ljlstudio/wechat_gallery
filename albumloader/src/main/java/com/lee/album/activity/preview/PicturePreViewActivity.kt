package com.lee.album.activity.preview

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.lee.album.Constants
import com.lee.album.R
import com.lee.album.activity.normal.NormalGalleryActivity
import com.lee.album.activity.normal.NormalGalleryViewModel
import com.lee.album.databinding.PicturePreviewLayoutBinding
import com.lee.album.widget.ViewDragHelper
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity

class PicturePreViewActivity : RxAppCompatActivity() {

    var binding: PicturePreviewLayoutBinding? = null
    private var first: Boolean = false
    private val normalModel =
        NormalGalleryActivity.activity?.viewModels<NormalGalleryViewModel>()?.value
    private var layoutChangedListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        immersionBar {
            statusBarColor(com.kt.ktmvvm.lib.R.color.color_3C3B39)
            hideBar(BarHide.FLAG_HIDE_BAR)

        }

        binding = DataBindingUtil.setContentView(this, R.layout.picture_preview_layout)

        initParam()


    }


    private fun initParam() {

        binding?.viewpagerHorizontal?.offscreenPageLimit = 3
        binding?.model = normalModel


        normalModel?.status?.observe(this, observer = {
            binding?.previewStatus?.setStatus()
        })

        normalModel?.alpha?.observe(this, observer = {
            it?.let {
                Log.i("xx", "alpha =$it")
                binding?.viewpagerHorizontal?.background?.mutate()?.alpha = it.toInt()
                binding?.previewStatus?.setAlphaStatus(it)
            }
        })

        normalModel?.leftFinish?.observe(this, observer = {
            finish()
        })
        layoutChangedListener = ViewTreeObserver.OnGlobalLayoutListener {

            if (!first) {
                first = true
                val extras = intent.extras
                extras?.let {
                    val position = it.getInt(Constants.KEY_POSITION, -1)
                    binding?.viewpagerHorizontal?.setCurrentItem(position, false)
//                normalModel?.pageCurrentItem?.set(position)

                }
            }

            binding?.viewpagerHorizontal?.viewTreeObserver?.removeOnGlobalLayoutListener { layoutChangedListener }
            layoutChangedListener = null
        }


        binding?.viewpagerHorizontal?.viewTreeObserver
            ?.addOnGlobalLayoutListener(layoutChangedListener)


    }


}