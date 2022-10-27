package com.lee.album.inter

import com.lee.album.entity.GalleryInfoEntity

/**
 * Author : 李嘉伦
 * e-mail : lijialun@angogo.cn
 * date   : 2022/1/2514:43
 * Package: com.lee.album.inter
 * desc   :
 */
interface OnGalleryListener {
    /**
     * 发送照片组
     */
    fun sendOrigenPictures(list: MutableList<String>?)

    fun sendCompressPictures(list: MutableList<String>?)
}