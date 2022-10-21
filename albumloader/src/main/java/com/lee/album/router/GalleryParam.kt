package com.lee.album.router

import com.lee.album.inter.CheckMode
import com.lee.album.inter.OnGalleryListener
import com.lee.album.router.GalleryParam

/**
 * Author : 李嘉伦
 * e-mail : lijialun@angogo.cn
 * date   : 2022/1/2514:00
 * Package: com.lee.album.router
 * desc   :
 */
class GalleryParam private constructor() {
    /**
     * 列表图片圆角
     */
    @kotlin.jvm.JvmField
    var listPictureCorner = 0

    /**
     * 列表图片列间距（图片之间）
     */
    @kotlin.jvm.JvmField
    var listPictureColumnSpace = 0

    /**
     * 列表图片行间距（图片之间）
     */
    @kotlin.jvm.JvmField
    var listPictureRowSpace = 0

    /**
     * 列表图片左右距离屏幕间距
     */
    @kotlin.jvm.JvmField
    var listPictureMargin = 0

    /**
     * 列表图片占位
     */
    @kotlin.jvm.JvmField
    var listPicturePlaceholder = -1

    /**
     * 相册接口回调
     */
    @kotlin.jvm.JvmField
    var onGalleryListener: OnGalleryListener? = null

    /**
     * 是否分页
     */
    @kotlin.jvm.JvmField
    var shouldLoadPaging = true

    /**
     * 分页加载一次每次返回条数 建议10-30之间
     */
    @kotlin.jvm.JvmField
    var pageSize = 0

    /**
     * 是否点击图片关闭bottomSheet
     */
    @kotlin.jvm.JvmField
    var shouldClickCloseBottomSheet = false

    /**
     * 是否可以触摸拖拽
     */
    @kotlin.jvm.JvmField
    var canTouchDrag = false

    /**
     * 选择模式，单选or多选，默认单选
     */
    @kotlin.jvm.JvmField
    var checkMode = CheckMode.SINGLE_MODE


    /**
     * 多选模式下最大可选为9
     */
    @kotlin.jvm.JvmField
    var checkMaxCount = 9

    /**
     * 重置数据
     */
    private fun reset() {
        checkMaxCount = 9
        checkMode = CheckMode.SINGLE_MODE
        listPictureCorner = 0
        listPictureColumnSpace = 0
        listPictureMargin = 0
        listPictureRowSpace = 0
        listPicturePlaceholder = -1
        shouldLoadPaging = false
        pageSize = 10
        shouldClickCloseBottomSheet = false
        canTouchDrag = false
    }

    companion object {
        /**
         * 获取相机配置参数
         *
         * @return
         */
        val instance = GalleryParam()
    }

    init {
        reset()
    }
}