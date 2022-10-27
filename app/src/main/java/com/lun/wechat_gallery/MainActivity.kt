package com.lun.wechat_gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.lee.album.entity.GalleryInfoEntity
import com.lee.album.inter.CheckMode
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
            .widthPageSize(15)
            .widthCheckMode(CheckMode.MULTIPLE_MODE)
            .widthListPicturePlaceholder(com.kt.ktmvvm.lib.R.color.color_3C3B39)
            .widthOnGalleryListener(object : OnGalleryListener {
                override fun sendOrigenPictures(list: MutableList<String>?) {
                    //原图
                }

                override fun sendCompressPictures(list: MutableList<String>?) {
                    //压缩后的图片路径
                }
            })
            .startGallery()


        finish()
    }
}