package com.lee.album.activity.normal

import android.Manifest
import android.os.Bundle
import android.view.Gravity
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
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

    override fun initParam() {
        binding?.classifyLayout?.setDrawerLockMode(VerticalDrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding?.classifyLayout?.setDrawerShadow(R.color.alpha_60_black,Gravity.BOTTOM)
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