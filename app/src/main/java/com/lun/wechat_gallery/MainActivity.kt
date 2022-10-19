package com.lun.wechat_gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.lee.album.inter.OnGalleryListener
import com.lee.album.router.GalleryEngine

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GalleryEngine.from(this@MainActivity)
            .setGalleryBuilder(this@MainActivity)
            .widthListPictureMargin(2)
            .widthListPictureColumnSpace(2)
            .widthListPictureRowSpace(2)
            .widthListPictureCorner(1)
            .withShouldLoadPaging(false)
            .widthPageSize(30)
            .widthListPicturePlaceholder(com.google.android.material.R.color.design_bottom_navigation_shadow_color)
            .widthOnGalleryListener(object : OnGalleryListener {
                override fun clickGallery(path: String?, position: Int) {
                    Toast.makeText(
                        this@MainActivity,
                        "------->PATH=$path------->position=$position",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun bottomSheetState(isOpen: Boolean, fromUser: Boolean) {
                    Toast.makeText(this@MainActivity, "抽屉状态$isOpen", Toast.LENGTH_SHORT).show()
                }

                override fun clickBadPicture(path: String?, position: Int) {
                    Toast.makeText(
                        this@MainActivity,
                        "------->点击了 损坏图片 PATH=$path------->position=$position",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .startGallery()
    }
}