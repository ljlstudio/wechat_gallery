package com.lee.album.inter

import androidx.annotation.IntDef

@IntDef(
    flag = true,
    value = [LoaderStatus.LOADING, LoaderStatus.LOADER_IDE, LoaderStatus.LOADER_ERROR]
)
annotation class LoaderStatus {

    companion object {
        const val LOADING = 1 //加载中
        const val LOADER_IDE = 2 //加载完成
        const val LOADER_ERROR = 3//加载错误
    }
}