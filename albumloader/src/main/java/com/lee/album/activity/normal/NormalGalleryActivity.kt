package com.lee.album.activity.normal

import android.Manifest
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.kt.ktmvvm.basic.BaseActivity
import com.lee.album.BR
import com.lee.album.R
import com.lee.album.databinding.NormalGalleryActivityBinding
import com.lee.album.permission.PermissionUtils
import com.lee.album.router.GalleryParam
import com.lee.album.widget.VerticalDrawerLayout
import com.permissionx.guolindev.PermissionX

class NormalGalleryActivity : BaseActivity<NormalGalleryActivityBinding, NormalGalleryViewModel>() {
    override fun initVariableId(): Int {
        return BR.model
    }

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.normal_gallery_activity
    }

    companion object {
        var activity: NormalGalleryActivity? = null
    }

    override fun initParam() {
        immersionBar {

            statusBarColor(com.kt.ktmvvm.lib.R.color.color_3C3B39)
            fitsSystemWindows(false)
            statusBarView(binding?.view)
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
        activity = this
        binding?.classifyLayout?.setDrawerLockMode(VerticalDrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding?.classifyLayout?.setDrawerShadow(R.color.alpha_60_black, Gravity.BOTTOM)
        binding?.layout?.setDrawerLockMode(VerticalDrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun initViewObservable() {
        super.initViewObservable()


        viewModel?.params?.observe(this, observer = {
            binding?.recycler?.apply {
                (layoutParams as RelativeLayout.LayoutParams).leftMargin =
                    GalleryParam.instance.listPictureMargin
                (layoutParams as RelativeLayout.LayoutParams).rightMargin =
                    GalleryParam.instance.listPictureMargin
            }
        })

        viewModel?.loadStart?.observe(this, observer = {
            val permissionsChecking = PermissionUtils.permissionsChecking(
                this,
                PermissionUtils.CLEAN_STORAGE_PERMISSIONS
            )
            if (permissionsChecking) {
                viewModel?.loadData(this)
            } else {
                PermissionX.init(this)
                    .permissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .request { allGranted, _, _ ->
                        if (allGranted) {
                            viewModel?.loadData(this)
                        }
                    }
            }
        })
        viewModel?.classifyLayoutEvent?.observe(this, observer = {
            if (it == true) {
                if (binding?.classifyLayout?.isDrawerOpen(binding?.classifyRecycler) == false) {
                    binding?.classifyLayout?.openDrawer(binding?.classifyRecycler)
                }

            } else {
                if (binding?.classifyLayout?.isDrawerOpen(binding?.classifyRecycler) == true) {
                    binding?.classifyLayout?.closeDrawer(binding?.classifyRecycler)

                }
            }
        })

    }


}