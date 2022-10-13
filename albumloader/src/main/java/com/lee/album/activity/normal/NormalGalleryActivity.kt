package com.lee.album.activity.normal

import android.Manifest
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.kt.ktmvvm.basic.BaseActivity
import com.lee.album.BR
import com.lee.album.R
import com.lee.album.databinding.NormalGalleryActivityBinding
import com.lee.album.permission.PermissionUtils
import com.lee.album.router.GalleryParam
import com.permissionx.guolindev.PermissionX

class NormalGalleryActivity : BaseActivity<NormalGalleryActivityBinding, NormalGalleryViewModel>() {
    override fun initVariableId(): Int {
        return BR.model
    }

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.normal_gallery_activity
    }

    override fun initParam() {

    }

    override fun initViewObservable() {
        super.initViewObservable()
        viewModel?.params?.observe(this, observer = {
            binding?.recycler?.apply {
                (layoutParams as RelativeLayout.LayoutParams).leftMargin = GalleryParam.instance.listPictureMargin
                (layoutParams as RelativeLayout.LayoutParams).rightMargin = GalleryParam.instance.listPictureMargin
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
    }
}